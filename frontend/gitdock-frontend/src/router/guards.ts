import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export function setupRouterGuards(router: Router): void {
    router.beforeEach((to, _from, next) => {
        const authStore = useAuthStore()

        // 1. Route protégée → doit être connecté
        if (to.meta.requiresAuth && !authStore.isAuthenticated) {
            return next('/login')
        }

        // 2. Route Super Admin → vérifie le rôle
        if (to.meta.requiresSuperAdmin && authStore.role !== 'ROLE_SUPER_ADMIN') {
            return next('/dashboard/home')
        }

        // 3. Route guest → redirige si déjà connecté
        if (to.meta.guest && authStore.isAuthenticated) {
            return next('/dashboard/home')
        }

        next()
    })
}