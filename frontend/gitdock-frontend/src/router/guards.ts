import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { DEV_USER_ROLES, ROLES } from '@/composables/useRole'

// Doit matcher EXACTEMENT les paths du router/index.ts
const PUBLIC_ROUTES = [
    '/login',
    '/signup',
    '/forgot-password',
    '/reset-password',
    '/activate',
    '/accept-invitation',
    '/oauth/callback',
]

// Routes réservées aux Managers/Admins
const MANAGER_ONLY_ROUTES = ['/sentinel', '/admin']

const isManagerOnlyPath = (path: string): boolean => {
    if (/^\/projects\/\d+\/users/.test(path)) return true
    if (/^\/projects\/\d+\/parts/.test(path)) return true
    return MANAGER_ONLY_ROUTES.some(r => path.startsWith(r))
}

export function setupGuards(router: Router) {
    router.beforeEach((to, _from, next) => {
        const authStore = useAuthStore()
        const isAuthenticated = authStore.isAuthenticated
        const role = authStore.role ?? ''
        const path = to.path

        // 1. Vérification des routes publiques
        const isPublic = PUBLIC_ROUTES.some(r => path.startsWith(r))

        if (isPublic) {
            // Si l'utilisateur est déjà connecté et tente d'aller sur Login ou Signup
            if (isAuthenticated && (path === '/login' || path === '/signup')) {
                return next('/dashboard/home')
            }
            return next() // On laisse passer vers la page publique
        }

        // 2. Si la route est privée et l'utilisateur n'est pas connecté
        if (!isAuthenticated) {
            return next('/login')
        }

        // 3. Restriction pour les rôles Developer/Tester sur les pages Manager
        if (isManagerOnlyPath(path) && DEV_USER_ROLES.includes(role as any)) {
            return next('/dashboard/home')
        }

        // 4. Restriction Super Admin
        if (path.startsWith('/admin') && role !== ROLES.SUPER_ADMIN) {
            return next('/dashboard/home')
        }

        // 5. Autorisation finale
        return next()
    })
}