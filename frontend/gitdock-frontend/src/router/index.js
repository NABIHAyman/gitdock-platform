import { createRouter, createWebHistory } from 'vue-router'
// Importe tes vues ici (ex: Home, Login)
// import LoginView from '../views/LoginView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: () => import('../views/HomeView.vue') // Lazy loading
        },
        // Ajoute tes futures routes ici
    ]
})

export default router