import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ActivateAccount from '@/views/ActivateAccount.vue';
import ForgotPassword from '@/views/ForgotPassword.vue';
import ResetPassword from '@/views/ResetPassword.vue';
import Dashboard from '@/views/Dashboard.vue';



const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/activate', name: 'ActivateAccount', component: ActivateAccount },
  { path: '/forgotPassword', name: 'ForgotPassword', component: ForgotPassword },
  { path: '/reset-password', name: 'ResetPassword', component: ResetPassword},
   { path: '/dashboard', name: 'dashboard', component: Dashboard},
  { path: '/', redirect: '/login' } 
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
