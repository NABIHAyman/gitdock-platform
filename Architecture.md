# Architecture.md

## 1. VISION GLOBALE

Le système repose sur une architecture microservices avec deux types de communication :

* Communication synchrone : HTTP (Feign)
* Communication asynchrone : RabbitMQ (Events + Listeners)

Principe fondamental :

* Chaque service possède ses données
* Aucun partage direct de base de données
* Toute interaction passe par API ou événements

---

## 2. SERVICES ET RÔLES

### gitdock-auth

* Source de vérité des utilisateurs
* Fournit : userId, roles

---

### gitdock-project

* Gère : projects, commits, branches
* Chaque commit est associé à une tâche (taskId obligatoire)
* Émet : CommitCreatedEvent

---

### gitdock-task

* Gère : tâches uniquement
* Émet : TaskCompletedEvent

---

### gitdock-gamification

* Gère : XP, Level, Badge
* Consomme des événements
* Met à jour la progression utilisateur

---

### gitdock-notification

* Consomme des événements
* Envoie notifications

---

## 3. COMMUNICATION INTER-SERVICES

### 3.1 SYNCHRONE (Feign / HTTP)

#### gitdock-task → gitdock-auth

Type : Feign API Call
Objectif :

* Vérifier existence utilisateur
* Récupérer infos utilisateur

Règle :

* privilégier les appels batch (ex : récupérer plusieurs users en une seule requête)

Endpoint attendu :
GET /users/{id}
GET /users?ids=1,2,3

---

#### gitdock-task → gitdock-project

Type : Feign API Call
Objectif :

* Vérifier existence project
* Vérifier appartenance

Endpoint attendu :
GET /projects/{id}

---

#### gitdock-gamification → gitdock-auth

Type : Feign API Call
Objectif :

* Vérifier user
* Récupérer infos si nécessaire

Règle :

* privilégier les appels batch

---

#### gitdock-project → gitdock-task (optionnel pour affichage enrichi)

Type : Feign API Call
Objectif :

* Récupérer les informations des tâches associées aux commits pour affichage

Endpoint attendu :
GET /tasks/{id}
GET /tasks?ids=1,2,3

---

## 3.2 ASYNCHRONE (RabbitMQ)

---

### Event 1 : CommitCreatedEvent

Émis par :

* gitdock-project

Type :

* RabbitMQ Producer

Payload :
{
"userId": number,
"commitId": number,
"projectId": number,
"taskId": number,
"tags": ["FEATURE", "BUG_FIX"]
}

Consommé par :

* gitdock-gamification (Listener)

Effet :

* +XP basé sur XpConfig
* mise à jour UserTagProgress

---

### Event 2 : TaskCompletedEvent

Émis par :

* gitdock-task

Type :

* RabbitMQ Producer

Payload :
{
"taskId": number,
"userId": number,
"taskLevel": "EASY | MEDIUM | HARD",
"xpReward": number
}

Consommé par :

* gitdock-gamification (Listener)

Effet :

* +XP selon taskLevel
* update UserProgress

---

### Event 3 : UserLevelUpdatedEvent (OPTIONNEL)

Émis par :

* gitdock-gamification

Type :

* RabbitMQ Producer

Payload :
{
"userId": number,
"newLevel": number
}

Consommé par :

* gitdock-notification

Effet :

* notification utilisateur

---

## 4. FLUX COMPLETS

---

### 4.1 FLOW : Commit → Task → Gamification

1. utilisateur fait commit
2. gitdock-project enregistre commit avec taskId obligatoire
3. gitdock-project publie CommitCreatedEvent (RabbitMQ)
4. gitdock-gamification reçoit (Listener)
5. calcule XP via XpConfig
6. met à jour UserProgress
7. met à jour UserTagProgress
8. vérifie level up

Affichage :

* gitdock-project doit être capable d’afficher les commits avec leurs tâches associées
* pour cela, il appelle gitdock-task (Feign, idéalement batch)

---

### 4.2 FLOW : Task → Gamification

1. utilisateur complète une tâche
2. gitdock-task met status = DONE
3. gitdock-task publie TaskCompletedEvent
4. gitdock-gamification reçoit
5. ajoute XP selon taskLevel
6. met à jour UserProgress
7. vérifie level up

---

### 4.3 FLOW : Level Up → Notification

1. gamification détecte level up
2. publie UserLevelUpdatedEvent
3. notification service reçoit
4. envoie notification

---

## 5. MODÈLES MANQUANTS IDENTIFIÉS

---

### Dans gitdock-task

À AJOUTER :

#### TaskLevel

{
"id": number,
"name": string,
"xpReward": number
}

---

### Dans gitdock-gamification

À AJOUTER :

#### UserProgress

{
"userId": number,
"xp": number,
"levelId": number
}

---

#### UserTagProgress

{
"userId": number,
"tag": string,
"count": number
}

---

## 6. DETTES IDENTIFIÉES

---

### gitdock-task dépend de :

* gitdock-auth (userId validation) → Feign (batch recommandé)
* gitdock-project (projectId validation) → Feign

---

### gitdock-gamification dépend de :

* gitdock-task → TaskCompletedEvent (RabbitMQ Listener)
* gitdock-project → CommitCreatedEvent (RabbitMQ Listener)
* gitdock-auth → userId validation (Feign, batch recommandé)

---

### gitdock-project dépend de :

* gitdock-task (pour enrichir l’affichage des commits avec les tâches) → Feign

---

### gitdock-project doit fournir :

* CommitCreatedEvent avec taskId et tags

---

### gitdock-task doit fournir :

* TaskCompletedEvent avec xpReward

---

## 7. RÈGLES D’INTÉGRATION

* Aucun service ne doit accéder à la base de données d’un autre
* Tous les événements doivent être idempotents
* Tous les consumers doivent être tolérants aux erreurs
* Les événements sont la source principale de synchronisation
* Les appels vers gitdock-auth doivent privilégier le batching

---

## 8. POINTS CRITIQUES À CORRIGER

* suppression des relations ORM entre services dans gitdock-task
* suppression des entités dupliquées (User, Project, Commit)
* ajout de TaskLevel
* ajout de taskId dans les commits
* connexion de gamification aux événements
* ajout de UserProgress dans gamification

---

## 9. OBJECTIF FINAL

Un système où :

* chaque commit est lié à une tâche
* les actions (commit, task) produisent des événements
* les événements alimentent gamification
* gamification calcule XP et niveau
* les résultats déclenchent des notifications

---

## 10. RÉSUMÉ TECHNIQUE

Communication utilisée :

* Feign :

  * task → auth (batch recommandé)
  * task → project
  * gamification → auth (batch recommandé)
  * project → task (affichage enrichi)

* RabbitMQ :

  * project → gamification (CommitCreatedEvent)
  * task → gamification (TaskCompletedEvent)
  * gamification → notification (UserLevelUpdatedEvent)

* Listener :

  * gamification écoute commit + task
  * notification écoute level update

---

FIN DU DOCUMENT
