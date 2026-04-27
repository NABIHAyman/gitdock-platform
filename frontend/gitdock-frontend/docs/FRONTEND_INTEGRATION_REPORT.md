# Frontend Integration Report (GitDock)

Date: 2026-04-13

## Mission 0 — Préparation de l'environnement

- Création / utilisation du dossier `docs/` à la racine de `gitdock-frontend`.
- Création de ce fichier `docs/FRONTEND_INTEGRATION_REPORT.md` pour tracer les actions d’intégration.

## Mission 1 — Extraction et standardisation de Task (TypeScript)

### Constats (anti-pattern)

- Des appels API étaient codés en dur dans les vues `src/views/task/**` via `fetch` et `axios`.
- URLs en dur détectées : `http://localhost/api/...`

### Actions réalisées

- Création d’un service TypeScript unique `src/services/TaskService.ts`.
- Standardisation des endpoints conformément à `API_CONTRACTS.md` :
  - `GET /api/tasks`
  - `GET /api/tasks/{id}`
  - `POST /api/tasks`
  - `PUT /api/tasks/{id}`
  - `PUT /api/tasks/{id}/soft-delete`
  - `GET /api/tasks/dashboard`
  - `GET /api/tasks/form-data`
- Connexion via la Gateway avec `import.meta.env.VITE_API_BASE_URL`.
- Ajout systématique du header `Authorization: Bearer <JWT>` (aligné ensuite sur `authStore` via `src/services/api.ts` — voir audit).
- Refactor des vues `src/views/task/**` pour utiliser `TaskService` (suppression des URLs codées en dur et du `fetch` direct).

### Fichiers impactés

- `src/services/TaskService.ts` (nouveau)
- `src/views/task/AddTask.vue`
- `src/views/task/UpdateTask.vue`
- `src/views/task/DeleteTask.vue`
- `src/views/task/DashboardTask.vue`
- `src/views/task/ViewTask/TaskAll.vue`

## Mission 2 — Recâblage de Gamification

### Actions réalisées

- Recâblage des services vers la Gateway (suppression de `https://localhost:7261`).
  - `src/services/BadgeService.ts` → `/api/badges`
  - `src/services/LevelService.ts` → `/api/Levels`
  - `src/services/TagService.ts` → `/api/tags`
- Ajout d’un service `src/services/XpConfigService.ts` pour `/api/xpconfig` (utilisé par le dashboard manager).
- Standardisation : utilisation de l’instance Axios globale (`src/services/api.ts`) qui ajoute automatiquement le JWT (voir audit `.cursorrules` ci-dessous pour l’évolution `api.js` → `api.ts`).
- Correction des imports dans les vues/components gamification :
  - Vues : `src/views/gamification/*.vue`
  - Components : `src/components/gamification/dashboard/manager/*`

### Fichiers impactés

- `src/services/BadgeService.ts`
- `src/services/LevelService.ts`
- `src/services/TagService.ts`
- `src/services/XpConfigService.ts` (nouveau)
- `src/components/gamification/dashboard/manager/BadgeSection.vue`
- `src/components/gamification/dashboard/manager/TagSection.vue`
- `src/components/gamification/dashboard/manager/LevelSection.vue`
- `src/views/gamification/DashboardView.vue`
- `src/views/gamification/UserDashboardView.vue`

## Mission 3 — Unification du Router

### Actions réalisées

- Ajout d’un layout routeur dédié `src/layouts/RouterAppLayout.vue` (Header + Sidebar + `<router-view />`) pour permettre des routes imbriquées sans modifier les vues existantes qui utilisent déjà `AppLayout` via `<slot />`.
- Ajout d’une route parent `/dashboard` protégée par `requiresAuth: true` avec des enfants pour Task et Gamification, afin qu’ils bénéficient de la navigation globale.
- Ajout de redirections “legacy” pour préserver les liens existants dans les vues Task :
  - `/dashboardtask` → `/dashboard/tasks`
  - `/TaskAll` → `/dashboard/tasks/all`
  - `/addtask` → `/dashboard/tasks/add`
- Ajout de liens dans la sidebar globale :
  - `Tasks` → `/dashboard/tasks`
  - `Gamification` → `/dashboard/gamification`

### Fichiers impactés

- `src/layouts/RouterAppLayout.vue` (nouveau)
- `src/router/index.js`
- `src/components/navigation/AppSideBar.vue`

## Mission 4 — Gestion de l'état local vs global

### Actions réalisées

- Remplacement des valeurs codées en dur / lues directement depuis `localStorage` par l’état global `authStore` (Pinia) pour les vues intégrées.
  - Task : `assignedBy` est maintenant dérivé de `authStore.userId`.
  - Gamification : remplacement des placeholders “Admin PFA” / “Rihab B.” par `authStore.firstName`, `authStore.lastName`, `authStore.role`.

### Fichiers impactés

- `src/views/task/AddTask.vue`
- `src/views/gamification/DashboardView.vue`
- `src/views/gamification/UserDashboardView.vue`

---

## Audit de conformité — `.cursorrules` (frontend)

Référence : `gitdock-frontend/.cursorrules` (Gateway unique, JWT via store, pas d’URL codées en dur, pas d’axios/fetch dans les vues, services en logique dédiée).

### Écarts constatés (avant correction)

- **URL en dur** : fallback `http://localhost:8080/api` dans l’ancien `api.js`, `http://localhost:8000/api` dans l’ancien `api.ts`, et `http://localhost:8085` pour notifications + WebSocket.
- **JWT** : intercepteurs basés sur `localStorage` au lieu de **`useAuthStore().token`** (règle « Authentification globale »).
- **Double préfixe `/api`** : avec `VITE_API_BASE_URL` déjà suffixé par `/api` (ex. `.env`), des chemins du type `/api/tasks` produisaient `/api/api/tasks`.

### Corrections appliquées

- **Instance Axios unique en TypeScript** : `src/services/api.ts` remplace `api.js` (supprimé) ; `baseURL` = **`import.meta.env.VITE_API_BASE_URL` uniquement** (aucun fallback localhost).
- **Bearer JWT** : injecté depuis **`useAuthStore()`** dans l’intercepteur via **`import()` dynamique** pour éviter le cycle de modules `api` → `authStore` → `authService` → `api` (repli silencieux si Pinia pas encore prêt).
- **Task + Gamification** : chemins relatifs au base URL sans redoubler `/api` (`/tasks`, `/badges`, `/Levels`, `/tags`, `/xpconfig`, etc.).
- **Notifications** : `notificationService.js` utilise la même instance `api` + chemins `/notifications…` ; WebSocket : **`VITE_NOTIFICATION_WS_URL`** ou dérivation de l’origine de **`VITE_API_BASE_URL`** + `/ws-notifications` (plus de port 8085 codé en dur).

### Fichiers impactés (audit)

- `src/services/api.ts` (remplace `api.js`)
- `src/services/TaskService.ts`
- `src/services/BadgeService.ts`, `LevelService.ts`, `TagService.ts`, `XpConfigService.ts`
- `src/services/authService.js`, `ProjectService.js`, `adminService.js`
- `src/services/notificationService.js`
- `src/stores/notificationStore.js`

**Optionnel côté déploiement** : si le bus STOMP n’est pas sur le même host que l’API, définir explicitement `VITE_NOTIFICATION_WS_URL` dans `.env`.

