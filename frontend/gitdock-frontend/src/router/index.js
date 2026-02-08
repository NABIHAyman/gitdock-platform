import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'

import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import ResetPasswordView from '@/views/auth/ResetPasswordView.vue'
import ActivationView from '@/views/auth/ActivationView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/login', // Redirige la racine vers le login
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView,
            meta: { guest: true } // Indique que seuls les non-connectés y ont accès
        },
        {
            path: '/signup',
            name: 'signup',
            component: RegisterView, meta: {guest: true}
        },
        {
            path: '/auth/forgot-password',
            name: 'forgot-password',
            component: ForgotPasswordView, meta: {guest: true}
        },
        {
            path: '/reset-password',
            name: 'reset-password',
            component: ResetPasswordView, meta: {guest: true}
        },
        {
            path: '/activate',
            name: 'activate',
            component: ActivationView, meta: {guest: true}
        }
    ],
})

setupRouterGuards(router)

export default router