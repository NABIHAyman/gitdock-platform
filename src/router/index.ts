import { createRouter, createWebHistory } from 'vue-router'
// Assure-toi que le nom du fichier correspond bien à ce que tu as dans ton dossier views
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
    // On remplace import.meta.env.BASE_URL par '/' pour éviter l'erreur env
    history: createWebHistory('/'),
    routes: [
        {
            path: '/',
            name: 'home',
            component: DashboardView
        },
        {
            path: '/dashboard',
            name: 'dashboard',
            component: DashboardView
        },
        {
            path: '/profile',
            name: 'user-dashboard',
            component: () => import('../views/UserDashboardView.vue')
        }
    ]
})

export default router