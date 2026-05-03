<template>
  <header class="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
    <div class="px-6 py-3">
      <div class="flex items-center justify-between">
        
        <!-- Logo -->
        <div class="flex items-center">
          <router-link to="/projects" class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center shadow-sm">
              <svg class="h-4 w-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
            </div>
            <span class="text-xl font-bold text-gray-900">GitDock</span>
          </router-link>
        </div>

        <div class="relative mr-4">
          <button @click="notificationsOpen = !notificationsOpen" class="relative p-2 text-gray-500 hover:text-gray-700 transition-colors focus:outline-none">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            <span v-if="notifStore.unreadCount > 0" class="absolute top-1 right-1 flex items-center justify-center w-4 h-4 text-xs font-bold text-white bg-red-500 rounded-full border-2 border-white">
              {{ notifStore.unreadCount }}
            </span>
          </button>

          <div v-if="notificationsOpen" class="absolute right-0 mt-2 w-80 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200 overflow-hidden">
            <div class="px-4 py-3 border-b border-gray-100 flex justify-between items-center bg-gray-50">
              <span class="font-semibold text-gray-700">Notifications</span>
              <button @click="notifStore.markAllAsRead" class="text-xs text-blue-600 hover:text-blue-800">Tout marquer lu</button>
            </div>
            
            <div class="max-h-96 overflow-y-auto">
              <div v-if="notifStore.inAppNotifications.length === 0" class="p-4 text-center text-gray-500 text-sm">
                Aucune notification
              </div>
              
              <div v-for="notif in notifStore.inAppNotifications" :key="notif.id" 
                   @click="notifStore.markAsRead(notif.id)"
                   :class="{'bg-blue-50/50': notif.read === false}"
                   class="p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors">
                <div class="flex items-start">
                  <div class="w-2 h-2 mt-1.5 rounded-full mr-3 flex-shrink-0"
                       :class="{
                         'bg-green-500': notif.type === 'TYPE_SYNC_COMPLETE',
                         'bg-yellow-500': notif.type === 'TYPE_SYNC_DELAYED',
                         'bg-blue-500': notif.type === 'TYPE_PROJECT_INVITATION',
                         'bg-purple-500': notif.type === 'TYPE_NEW_COMMIT',
                         'bg-gray-300': notif.read === true
                       }">
                  </div>
                  <div>
                    <p class="text-sm font-semibold text-gray-800" :class="{'text-gray-500': notif.read === true}">{{ notif.title }}</p>
                    <p class="text-sm text-gray-600 mt-1 line-clamp-2" :class="{'text-gray-400': notif.read === true}">{{ notif.message }}</p>
                    <p class="text-xs text-gray-400 mt-2">{{ new Date(notif.createdAt).toLocaleDateString('fr-FR', {hour: '2-digit', minute:'2-digit'}) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- User Menu Déroulant -->
        <div class="relative">
          <button
            @click="userMenuOpen = !userMenuOpen"
            class="flex items-center space-x-3 p-1.5 rounded-md hover:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <!-- Pastille avec Initiales -->
            <div class="w-8 h-8 bg-blue-100 border border-blue-200 rounded-full flex items-center justify-center">
              <span class="text-blue-700 text-sm font-bold">
                {{ userInitials }}
              </span>
            </div>
            <span class="hidden sm:block text-sm font-medium text-gray-700">
              {{ userFullName }}
            </span>
            <svg class="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          <!-- Dropdown -->
          <div
            v-if="userMenuOpen"
            class="absolute right-0 mt-2 w-56 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200"
          >
            <div class="px-4 py-3 text-sm text-gray-700 border-b border-gray-100">
              <div class="font-medium text-gray-900">{{ userFullName }}</div>
              <div class="text-xs text-gray-500 truncate">{{ authStore.email }}</div>
            </div>
            <div class="px-4 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
              Rôle: {{ authStore.role?.replace('ROLE_', '').replace('_', ' ') }}
            </div>
            <button
              @click="handleLogout"
              class="flex items-center w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
            >
              <svg class="h-4 w-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              Déconnexion
            </button>
          </div>
        </div>
        
      </div>
    </div>
  </header>
</template>

<!---->
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore'

const authStore = useAuthStore()
const userMenuOpen = ref(false)

const notificationsOpen = ref(false)
const inAppNotifications = ref([])

const notifStore = useNotificationStore()

const userInitials = computed(() => {
  const first = authStore.firstName ? authStore.firstName.charAt(0) : ''
  const last = authStore.lastName ? authStore.lastName.charAt(0) : ''
  return (first + last).toUpperCase() || 'U'
})

const userFullName = computed(() => {
  return `${authStore.firstName || ''} ${authStore.lastName || ''}`.trim() || 'Utilisateur'
})

const handleLogout = () => {
  authStore.logout()
}

// Fermer le menu si on clique ailleurs
const handleClickOutside = (event) => {
  if (!event.target.closest('.relative')) {
    userMenuOpen.value = false
    notificationsOpen.value = false 
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  //if (authStore.isAuthenticated) {
    //notifStore.startPolling() // On lance le moteur
  //}
  if (authStore.isAuthenticated) {
    notifStore.startRealTime() // 👈 REMPLACE startPolling
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  //notifStore.stopPolling()
  notifStore.stopRealTime()
})
</script>