import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import Login from '@/views/Auth/Login.vue'
import Register from '@/views/auth/Register.vue'
import ForgotPassword from '@/views/auth/ForgotPassword.vue'
import Activation from '@/views/auth/Activation.vue'

import Dashboard from '@/views/task/Dashboard.vue'
import DashboardCompany from '@/views/dashbord/DashboadCompany.vue'

import AddTask from '@/views/task/Addtask.vue'
import UpdateTask from '@/views/task/Updatetask.vue'
import Deletetask from '@/views/task/Deletetask.vue'

/* ================= ROUTES ================= */

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },

  // 🔓 PUBLIC
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  { path: '/forgot-password', name: 'forgot-password', component: ForgotPassword },
  { path: '/activation', name: 'activation', component: Activation },

  // 🔐 USER DASHBOARD
  {
    path: '/dashboard',
    name: 'dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },

  // 🏢 COMPANY DASHBOARD (ROLE PROTECTION)
  {
    path: '/company/dashboard',
    name: 'company-dashboard',
    component: DashboardCompany,
    meta: {
      requiresAuth: true,
      roles: ['ROLE_COMPANY_ADMIN', 'ROLE_SUPER_ADMIN']
    }
  },

  // TASKS
  {
    path: '/tasks/add',
    name: 'task-add',
    component: AddTask,
    meta: { requiresAuth: true }
  },

  {
    path: '/tasks/edit/:id',
    name: 'task-edit',
    component: UpdateTask,
    props: true,
    meta: { requiresAuth: true }
  },

  {
    path: '/tasks/delete/:id',
    name: 'task-delete',
    component: Deletetask,
    props: true,
    meta: { requiresAuth: true }
  },

  { path: '/tasks', redirect: '/dashboard' },

  // ❌ fallback
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

/* ================= ROUTER ================= */

const router = createRouter({
  history: createWebHistory(),
  routes
})

/* ================= TYPES FIX ================= */

interface User {
  role: string
}

/* ================= AUTH GUARD ================= */

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  const user: User | null = JSON.parse(
    localStorage.getItem('user') || 'null'
  )

  // 🔐 check auth
  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  // 🔐 check roles (FIX TS ERROR)
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    if (!user) {
      return next('/login')
    }

    const hasRole = to.meta.roles.some((role: string) =>
      user.role === role
    )

    if (!hasRole) {
      return next('/dashboard')
    }
  }

  next()
})

export default router