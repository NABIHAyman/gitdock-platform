<template>
  <aside class="w-64 bg-white shadow-sm border-r border-gray-200 min-h-[calc(100vh-73px)]">
    <nav class="p-4">
      <ul class="space-y-2">
        <!-- Lien Projets (Pour tout le monde) -->
        <li>
          <router-link
              to="/projects"
              class="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-100 transition-colors"
              active-class="bg-blue-50 text-blue-700"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
            Projets
          </router-link>
        </li>

        <!-- Lien Profil -->
        <li>
          <router-link
              to="/profile"
              class="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-100 transition-colors"
              active-class="bg-blue-50 text-blue-700"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            Mon Profil
          </router-link>
        </li>

        <!-- Lien Tasks -->
        <li>
          <router-link
              to="/dashboard/tasks"
              class="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-100 transition-colors"
              active-class="bg-blue-50 text-blue-700"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            Tasks
          </router-link>
        </li>

        <!-- Lien Gamification -->
        <li>
          <router-link
              to="/dashboard/gamification"
              class="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-100 transition-colors"
              active-class="bg-blue-50 text-blue-700"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 4h2a2 2 0 012 2v2h2a2 2 0 012 2v2a8 8 0 11-16 0V12a2 2 0 012-2h2V6a2 2 0 012-2z" />
            </svg>
            Gamification
          </router-link>
        </li>

        <!-- Lien Dashboard Admin (Caché pour les non-admins) 🛡️ -->
        <li v-if="isSuperAdmin">
          <router-link
              to="/admin"
              class="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-100 transition-colors mt-4"
              active-class="bg-purple-50 text-purple-700"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            Dashboard Admin
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

// Vérification réactive du rôle
const isSuperAdmin = computed(() => authStore.role === 'ROLE_SUPER_ADMIN')
</script>