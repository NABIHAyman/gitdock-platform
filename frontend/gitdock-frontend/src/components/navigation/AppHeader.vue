<template>
  <header class="bg-white shadow-sm border-b border-gray-200">
    <div class="px-6 py-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <h1 class="text-2xl font-bold text-gray-900">GitDock</h1>
        </div>
        <div class="flex items-center space-x-4">
          <span class="text-sm text-gray-600">
            {{ userFullName }}
          </span>
          <button
              @click="handleLogout"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Déconnexion
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const userFullName = computed(() => {
  if (authStore.firstName && authStore.lastName) {
    return `${authStore.firstName} ${authStore.lastName}`
  }
  return 'Utilisateur'
})

const handleLogout = () => {
  authStore.logout()
}
</script>

