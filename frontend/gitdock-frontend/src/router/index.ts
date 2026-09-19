import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuards } from './guards'

import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import ResetPasswordView from '@/views/auth/ResetPasswordView.vue'
import ActivationView from '@/views/auth/ActivationView.vue'

import ProjectsView from '@/views/projects/ProjectsView.vue'
import ProjectStatsView from '@/views/projects/ProjectStatsView.vue'
import ProjectPartsView from '@/views/projects/ProjectPartsView.vue'
import ProjectUsersView from '@/views/projects/ProjectUsersView.vue'
import ProjectBranchesView from '@/views/projects/ProjectBranchesView.vue'
import BranchCommitsView from '@/views/projects/BranchCommitsView.vue'

import SuperAdminDashboard from '@/views/dashboard/SuperAdminDashboard.vue'
import RouterAppLayout from '@/layouts/RouterAppLayout.vue'

import DashboardTask from '@/views/task/DashboardTask.vue'
import AddTask from '@/views/task/AddTask.vue'
import UpdateTask from '@/views/task/UpdateTask.vue'
import DeleteTask from '@/views/task/DeleteTask.vue'
import TaskAll from '@/views/task/ViewTask/TaskAll.vue'

import GamificationDashboardView from '@/views/gamification/DashboardView.vue'
import GamificationUserDashboardView from '@/views/gamification/UserDashboardView.vue'

import DashboardHomeView from '@/views/dashboard/DashboardHomeView.vue'


// Typage des meta de routes
declare module 'vue-router' {
    interface RouteMeta {
        requiresAuth?: boolean
        requiresSuperAdmin?: boolean
        guest?: boolean
    }
}

const routes: RouteRecordRaw[] = [
    { path: '/', redirect: '/login' },

    // --- Auth (guest only) ---
    { path: '/login', name: 'login', component: LoginView, meta: { guest: true } },
    { path: '/signup', name: 'signup', component: RegisterView, meta: { guest: true } },
    { path: '/auth/forgot-password', name: 'forgot-password', component: ForgotPasswordView, meta: { guest: true } },
    { path: '/reset-password', name: 'reset-password', component: ResetPasswordView, meta: { guest: true } },
    { path: '/activate', name: 'activate', component: ActivationView, meta: { guest: true } },

    // --- Auth (public) ---
    {
        path: '/accept-invitation',
        name: 'AcceptInvitation',
        component: () => import('@/views/auth/AcceptInvitationView.vue'),
        meta: { requiresAuth: false },
    },

    // --- Projects (REMIS À LA RACINE POUR RÉPARER L'ERREUR /projects) ---
    { path: '/projects', name: 'projects', component: ProjectsView, meta: { requiresAuth: true } },
    { path: '/projects/:id', name: 'project-details', component: ProjectStatsView, meta: { requiresAuth: true } },
    { path: '/projects/:id/parts', name: 'project-parts', component: ProjectPartsView, meta: { requiresAuth: true } },
    { path: '/projects/:id/users', name: 'project-users', component: ProjectUsersView, meta: { requiresAuth: true } },
    { path: '/projects/:id/branches', name: 'project-branches', component: ProjectBranchesView, meta: { requiresAuth: true } },
    { path: '/projects/:id/branches/:branchId/commits', name: 'branch-commits', component: BranchCommitsView, meta: { requiresAuth: true } },

    // --- Admin ---
    {
        path: '/admin',
        name: 'admin-dashboard',
        component: SuperAdminDashboard,
        meta: { requiresAuth: true, requiresSuperAdmin: true },
    },

    // --- Sentinel Radar ---
    {
        path: '/sentinel',
        name: 'sentinel-radar',
        component: () => import('@/views/sentinel/CommitRadar.vue'),
        meta: { requiresAuth: true },
    },

    // --- Dashboard (layout avec enfants) ---
    {
        path: '/dashboard',
        component: RouterAppLayout,
        meta: { requiresAuth: true },
        children: [
            { path: '', redirect: '/home' },

            // Tasks
            { path: 'tasks', name: 'dashboard-task', component: DashboardTask },
            { path: 'tasks/all', name: 'task-all', component: TaskAll },
            { path: 'tasks/add', name: 'task-add', component: AddTask },
            { path: 'tasks/edit/:id', name: 'task-edit', component: UpdateTask, props: true },
            { path: 'tasks/delete/:id', name: 'task-delete', component: DeleteTask, props: true },

            // Gamification
            { path: 'gamification', name: 'gamification-dashboard', component: GamificationDashboardView },
            { path: 'gamification/user', name: 'gamification-user-dashboard', component: GamificationUserDashboardView },
            // Dans children :
            { path: 'home', name: 'dashboard-home', component: DashboardHomeView },
        ],
    },

    // --- Legacy redirects ---
    { path: '/dashboardtask', redirect: '/dashboard/tasks' },
    { path: '/TaskAll', redirect: '/dashboard/tasks/all' },
    { path: '/addtask', redirect: '/dashboard/tasks/add' },

    // --- OAuth callback ---
    {
        path: '/oauth/callback/:platform',
        name: 'oauth-callback',
        component: () => import('@/views/auth/OAuthCallbackView.vue'),
        meta: { requiresAuth: true },
    },

    // --- Profile ---
    {
        path: '/profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { requiresAuth: true },
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

setupRouterGuards(router)

export default router