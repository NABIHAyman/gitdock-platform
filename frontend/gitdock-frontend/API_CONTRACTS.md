# CONTRATS D'API (Via GitDock Gateway)

**RÈGLE ABSOLUE :** Tous les appels API dans le frontend doivent utiliser l'instance Axios globale ou pointer dynamiquement vers la Gateway via `import.meta.env.VITE_API_BASE_URL`. 
Aucune URL "localhost:7261" ou "localhost:8000" ne doit subsister dans le code.

---

## 1. MICROSERVICE : GITDOCK-GAMIFICATION
*(Les anciens services pointaient vers https://localhost:7261, à remplacer par la Gateway)*

**Badges (`/api/badges`)**
- `GET /api/badges` (Liste tous les badges)
- `POST /api/badges` (Crée un badge)
- `PUT /api/badges/{id}` (Met à jour un badge)
- `DELETE /api/badges/{id}` (Supprime un badge)

**Niveaux (`/api/Levels`)**
- `GET /api/Levels` (Liste tous les niveaux)
- `POST /api/Levels` (Crée un niveau)
- `PUT /api/Levels/{id}` (Met à jour un niveau)
- `DELETE /api/Levels/{id}` (Supprime un niveau)

**Tags (`/api/tags`)**
- `GET /api/tags` (Liste tous les tags)
- `POST /api/tags` (Crée un tag)
- `PUT /api/tags/{id}` (Met à jour un tag)
- `DELETE /api/tags/{id}` (Supprime un tag)

---

## 2. MICROSERVICE : GITDOCK-TASK
*(Ces appels sont actuellement éparpillés dans les composants Vue, ils doivent être regroupés)*

**Tâches Principales (`/api/tasks`)**
- `GET /api/tasks` (Liste des tâches - format restreint)
- `GET /api/tasks/{id}` (Détails d'une tâche spécifique)
- `POST /api/tasks` (Création. Body attendu : title, description, status, dueDate, assignedTo, assignedBy, projectId, epic, part, level)
- `PUT /api/tasks/{id}` (Mise à jour. Même body que POST)
- `PUT /api/tasks/{id}/soft-delete` (Suppression logique)

**Données Annexes (Tâches)**
- `GET /api/tasks/dashboard` (Statistiques pour le DashboardTask)
- `GET /api/tasks/form-data` (Récupère les listes `epics`, `parts` et `levels` pour remplir les menus déroulants dans AddTask/UpdateTask)