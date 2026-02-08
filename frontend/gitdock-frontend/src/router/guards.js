import { useAuthStore } from '@/stores/authStore'

/**
 * Configure les protections de routes (Navigation Guards)
 * @param {Router} router - L'instance du routeur Vue
 */
export function setupRouterGuards(router) {
    router.beforeEach((to, from, next) => {
        const authStore = useAuthStore()
        const isAuthenticated = authStore.isAuthenticated

        // 1. Si la route demande une authentification et que l'user n'est pas connecté
        if (to.meta.requiresAuth && !isAuthenticated) {
            next({ name: 'login' })
            return
        }

        // 3. Dans tous les autres cas, on laisse passer
        next()
    })
}