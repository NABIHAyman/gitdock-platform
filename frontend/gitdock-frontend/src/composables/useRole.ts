// src/composables/useRole.ts
// Composable central pour la gestion des rôles dans GitDock
// Usage : const { isDevUser, isManager, canEditTask, ... } = useRole()

import { computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'

// Constantes des rôles — correspond exactement à la table `role` en DB
export const ROLES = {
    SUPER_ADMIN:      'ROLE_SUPER_ADMIN',
    COMPANY_ADMIN:    'ROLE_COMPANY_ADMIN',
    WORKSPACE_OWNER:  'ROLE_WORKSPACE_OWNER',
    MANAGER:          'ROLE_MANAGER',
    DEVELOPER:        'ROLE_DEVELOPER',
    TESTER:           'ROLE_TESTER',
    CONSULTANT:       'ROLE_CONSULTANT',
} as const

// Groupe "utilisateur limité" : même profil, mêmes restrictions
export const DEV_USER_ROLES = [
    ROLES.DEVELOPER,
    ROLES.TESTER,
    ROLES.CONSULTANT,
]

// Groupe "gestionnaire" : accès complet aux projets et tasks
export const MANAGER_ROLES = [
    ROLES.SUPER_ADMIN,
    ROLES.COMPANY_ADMIN,
    ROLES.WORKSPACE_OWNER,
    ROLES.MANAGER,
]

export function useRole() {
    const authStore = useAuthStore()

    const currentRole = computed(() => authStore.role ?? '')
    const currentUserId = computed(() => authStore.userId)

    // ─── Vérifications de rôle ─────────────────────────────────────────────────

    /** Developer, Tester ou Consultant → profil utilisateur limité */
    const isDevUser = computed(() =>
        DEV_USER_ROLES.includes(currentRole.value as any)
    )

    /** Manager, Owner, Admin → accès complet */
    const isManager = computed(() =>
        MANAGER_ROLES.includes(currentRole.value as any)
    )

    const isSuperAdmin = computed(() =>
        currentRole.value === ROLES.SUPER_ADMIN
    )

    const isDeveloper = computed(() =>
        currentRole.value === ROLES.DEVELOPER
    )

    const isTester = computed(() =>
        currentRole.value === ROLES.TESTER
    )

    const isConsultant = computed(() =>
        currentRole.value === ROLES.CONSULTANT
    )

    // ─── Permissions par section ───────────────────────────────────────────────

    /** Projects : les devUsers voient seulement leurs projets, sans CRUD */
    const canCreateProject = computed(() => isManager.value)
    const canDeleteProject = computed(() => isManager.value)

    /** Tasks : les devUsers voient seulement leurs tâches assignées */
    const canCreateTask = computed(() => isManager.value)
    const canDeleteTask = computed(() => isManager.value)
    const canAssignTask = computed(() => isManager.value)

    /** Tasks : le devUser peut seulement passer sa tâche en "done" */
    const canMarkTaskDone = computed(() => true) // tout le monde
    const canEditTaskFull = computed(() => isManager.value)

    /** Gamification : les devUsers voient seulement leurs badges/levels/tags */
    const canManageGamification = computed(() => isManager.value)

    /** Sentinel : uniquement managers */
    const canAccessSentinel = computed(() => isManager.value)

    // ─── Helper : est-ce que cette tâche appartient à l'user connecté ? ────────
    const isMyTask = (assignedToId: number | null | undefined): boolean => {
        if (!assignedToId || !currentUserId.value) return false
        return assignedToId === currentUserId.value
    }

    return {
        currentRole,
        currentUserId,
        // Groupes
        isDevUser,
        isManager,
        isSuperAdmin,
        isDeveloper,
        isTester,
        isConsultant,
        // Permissions
        canCreateProject,
        canDeleteProject,
        canCreateTask,
        canDeleteTask,
        canAssignTask,
        canMarkTaskDone,
        canEditTaskFull,
        canManageGamification,
        canAccessSentinel,
        // Helpers
        isMyTask,
    }
}