<template>
  <AppLayout>
    <div class="max-w-4xl mx-auto space-y-6">
      
      <!-- Header -->
      <div>
        <h1 class="text-2xl font-semibold tracking-tight text-gray-900">Mon Profil</h1>
        <p class="mt-1 text-sm text-gray-500">Gérez vos informations personnelles et vos intégrations.</p>
      </div>

      <!-- Carte Informations Personnelles -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-5 border-b border-gray-100 bg-gray-50">
          <h3 class="text-lg font-medium text-gray-900">Informations Personnelles</h3>
        </div>
        <div class="p-6 flex items-center space-x-6">
          
          <!-- Avatar (GitHub ou Initiales) -->
          <div class="flex-shrink-0">
            <img v-if="authStore.avatarUrl" :src="authStore.avatarUrl" alt="Avatar" class="h-24 w-24 rounded-full border-4 border-white shadow-md object-cover">
            <div v-else class="h-24 w-24 rounded-full bg-blue-100 border-4 border-white shadow-md flex items-center justify-center">
              <span class="text-3xl font-bold text-blue-700">{{ userInitials }}</span>
            </div>
          </div>

          <!-- Infos -->
          <div class="flex-1 space-y-2">
            <div>
              <p class="text-sm font-medium text-gray-500">Nom Complet</p>
              <p class="text-lg font-semibold text-gray-900">{{ authStore.firstName }} {{ authStore.lastName }}</p>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500">Email</p>
              <p class="text-gray-900">{{ authStore.email || 'Non renseigné' }}</p>
            </div>
            <div>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 mt-2">
                Rôle: {{ formattedRole }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Carte Intégrations (GitHub) -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-5 border-b border-gray-100 bg-gray-50">
          <h3 class="text-lg font-medium text-gray-900">Intégrations</h3>
          <p class="text-sm text-gray-500">Connectez des services tiers pour enrichir votre expérience GitDock.</p>
        </div>
        
        <div class="p-6">
          <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <div class="flex items-center space-x-4">
              <div class="bg-gray-900 p-2 rounded-lg text-white">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                  <path fill-rule="evenodd" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.254-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.202 2.393.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z" clip-rule="evenodd" />
                </svg>
              </div>
              <div>
                <h4 class="text-base font-semibold text-gray-900">GitHub</h4>
                <p class="text-sm text-gray-500">Liez votre compte pour synchroniser les dépôts privés et synchroniser votre avatar.</p>
              </div>
            </div>
            
            <button @click="handleGitHubConnect" class="px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors">
              {{ authStore.avatarUrl ? 'Re-lier mon compte' : 'Lier mon compte' }}
            </button>
          </div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<script setup>
import { computed } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore'
import { projectService } from '@/services/projectService'

const authStore = useAuthStore()
const notifStore = useNotificationStore()

const userInitials = computed(() => {
  const first = authStore.firstName ? authStore.firstName.charAt(0) : ''
  const last = authStore.lastName ? authStore.lastName.charAt(0) : ''
  return (first + last).toUpperCase() || 'U'
})

const formattedRole = computed(() => {
  return authStore.role?.replace('ROLE_', '').replace('_', ' ') || 'Utilisateur'
})

const handleGitHubConnect = async () => {
  try {
    // 1. On note qu'on vient du profil !
    localStorage.setItem('oauth_origin', 'profile')
    
    // 2. On lance le flux
    const response = await projectService.initOAuthFlow('github')
    
    // 3. Redirection vers GitHub
    window.location.href = response.url
  } catch (error) {
    notifStore.error("Impossible de contacter le service d'authentification.")
  }
}
</script>