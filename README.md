DIAGNOSTIC GLOBAL

Le projet est actuellement dans une situation typique d’“integration hell”. Le problème principal n’est pas technique mais organisationnel. Chaque membre de l’équipe a développé son service de manière isolée, sans coordination, sans contrats d’API communs, et sans architecture d’intégration définie.

Le résultat est le suivant :

* duplication de modèles (User, Project, Commit, etc.)
* absence de contrats de communication inter-services
* absence de stratégie d’intégration
* utilisation de stacks différentes (Spring Boot, Symfony, .NET) augmentant fortement la complexité

Le système ne correspond pas réellement à une architecture microservices, mais plutôt à un ensemble de mini-monolithes indépendants.

ANALYSE DES SERVICES EXISTANTS

Services cohérents et bien intégrés :

* gitdock-auth
* gitdock-project
* gitdock-sync
* gitdock-notification
* gitdock-gateway
* gitdock-discovery

Ces services fonctionnent déjà en symbiose avec :

* communication HTTP (Feign)
* communication asynchrone (RabbitMQ)
* séparation claire des responsabilités

Services problématiques :

* gitdock-task
* gitdock-gamification

Ces deux services sont déconnectés de l’architecture globale et ne respectent pas les principes microservices.

PROBLÈME MAJEUR DANS gitdock-task

Le service gitdock-task contient des entités qui ne lui appartiennent pas :

* User
* Project
* Commit
* Branch

Cela constitue une violation majeure du principe de responsabilité unique. Ces données appartiennent déjà à d’autres services :

* User appartient à gitdock-auth
* Project, Commit, Branch appartiennent à gitdock-project

Le service gitdock-task est donc un mini-monolithe déguisé, ce qui rend l’intégration difficile voire impossible sans refonte.

PROBLÈME DANS gitdock-gamification

Le service gitdock-gamification est relativement propre sur le plan interne (structure Domain, DTOs, Services), mais il est complètement isolé du reste du système.

Problèmes identifiés :

* absence de lien avec les utilisateurs réels (auth)
* absence de lien avec les commits réels (project)
* absence de lien avec les tâches réelles (task)
* base de données autonome sans intégration avec les autres services

Il s’agit d’un service fonctionnel mais non connecté au système global.

DÉCISION STRUCTURELLE

Le projet doit utiliser gitdock-api comme source de vérité absolue.

Cela implique :

* conserver l’architecture existante (gateway, discovery, messaging)
* adapter les autres services à cette architecture
* ne pas tenter de fusionner les architectures existantes entre elles

PLAN D’ATTAQUE

PHASE 0 : FREEZE

Avant toute chose :

* arrêt de tout développement
* arrêt de tout push non coordonné

Objectif :

* stabiliser la situation
* éviter d’aggraver l’intégration

PHASE 1 : REDÉFINITION DES RESPONSABILITÉS

Chaque service doit avoir une responsabilité unique et claire.

gitdock-auth :

* gestion des utilisateurs
* source de vérité des userId, roles, companyId

gitdock-project :

* gestion des projets
* gestion des commits
* gestion des branches
* émission d’événements (ex : CommitCreatedEvent)

gitdock-task :

* gestion du cycle de vie des tâches uniquement

gitdock-gamification :

* calcul de l’XP
* gestion des niveaux
* gestion des badges

gitdock-notification :

* gestion des notifications
* écoute des événements

RÈGLES IMPORTANTES :

* aucun service ne doit dupliquer les données d’un autre
* tous les échanges doivent passer par API ou événements

PROBLÈMES DANS LE MODÈLE ACTUEL DE TASK

Dans Task.php, les relations suivantes sont incorrectes :

* relation directe avec User
* relation avec Epic
* relation avec Part
* relation avec Level (dans sa forme actuelle)

Ces relations doivent être supprimées.

Remplacement :

* User devient assignedToUserId (int)
* User devient assignedByUserId (int)
* Project devient projectId (int)
* Level devient TaskLevel interne au service

INTRODUCTION DU CONCEPT MANQUANT : TaskLevel

Le système actuel ne possède pas de structure cohérente pour représenter la difficulté des tâches.

Il est nécessaire d’introduire une entité TaskLevel dans gitdock-task.

Exemples :

* Easy (xpReward = 10)
* Medium (xpReward = 25)
* Hard (xpReward = 50)

Task doit contenir :

* taskLevelId
* projectId
* assignedToUserId

REFACTORING DE gitdock-task

Le responsable du service gitdock-task doit :

* supprimer les entités User, Project, Commit, Branch
* conserver uniquement la logique métier liée aux tâches
* remplacer toutes les relations par des identifiants simples
* introduire TaskLevel

Stratégie recommandée :

* recréer le module proprement
* conserver la logique métier existante (TaskService)
* ne pas tenter de faire un merge Git complexe

COMMUNICATION ENTRE SERVICES

Communication synchrone (HTTP) :

* task → auth (validation user)
* task → project (validation project)

Communication asynchrone (RabbitMQ) :

* TaskCompletedEvent
* CommitCreatedEvent

LIEN ENTRE TASK ET GAMIFICATION

Lorsqu’une tâche est terminée, le service task doit émettre un événement.

TaskCompletedEvent :

* taskId
* userId
* taskLevel
* xpReward

Cet événement est consommé par gamification.

DTOs ENTRE SERVICES

Task → Gamification :

TaskCompletedEventDTO :

* userId
* taskId
* taskLevel
* xpReward

Project → Gamification :

CommitCreatedEventDTO :

* userId
* commitId
* projectId

Gamification → autres services (optionnel) :

UserLevelUpdatedDTO :

* userId
* newLevel

DETTES ENTRE SERVICES

gitdock-task dépend de :

* gitdock-auth (userId)
* gitdock-project (projectId)

gitdock-gamification dépend de :

* gitdock-task (TaskCompletedEvent)
* gitdock-project (CommitCreatedEvent)
* gitdock-auth (userId)

RÈGLE :
aucune duplication de données n’est autorisée.

PROBLÈMES STRUCTURELS DANS GAMIFICATION

Le modèle actuel contient :

* Level
* Tag
* LevelTagRequirement
* XpConfig
* Badge

Ces éléments sont bien structurés mais ne sont reliés à aucune donnée réelle.

Solution :

Introduire UserProgress :

* userId
* xp
* levelId

Introduire UserTagProgress :

* userId
* tag
* count

Le service doit consommer les événements :

* TaskCompletedEvent
* CommitCreatedEvent

Et mettre à jour :

* XP
* Level
* Badges

ROADMAP PAR RESPONSABLE

Responsable de gitdock-api :

* définir les événements
* intégrer TaskLevel
* définir les DTOs globaux
* maintenir la cohérence architecturelle

Responsable du service gitdock-task :

* supprimer toutes les entités externes
* conserver uniquement Task et sa logique
* introduire TaskLevel
* utiliser des identifiants au lieu de relations
* émettre TaskCompletedEvent

Responsable de gitdock-gamification :

* introduire UserProgress
* introduire UserTagProgress
* consommer les événements
* calculer XP et niveaux
* attribuer badges

FLUX GLOBAL FINAL

1. Un utilisateur effectue un commit
   → gitdock-project
   → émission CommitCreatedEvent
   → consommé par gamification
   → mise à jour XP

2. Un utilisateur complète une tâche
   → gitdock-task
   → émission TaskCompletedEvent
   → consommé par gamification
   → mise à jour XP

3. Le service gamification :

* calcule le niveau
* attribue des badges

CONCLUSION

Le problème principal est organisationnel et architectural.

Les décisions à prendre sont :

* conserver gitdock-api comme source de vérité
* refactorer complètement gitdock-task
* connecter gitdock-gamification au système via événements
* introduire le concept manquant de TaskLevel
* supprimer toute duplication de données

Il faut checker le fichier Architecture.md pour comprendre ce qui est attendu.

Je te laisse d'abord faire un plan. puis on valide le plan. puis on attaque l'intégration.