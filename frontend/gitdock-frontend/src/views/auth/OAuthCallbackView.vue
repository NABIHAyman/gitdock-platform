<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-50">
    <div class="text-center space-y-6 bg-white p-10 rounded-xl shadow-sm border border-gray-100 max-w-md w-full">
      <div class="relative w-20 h-20 mx-auto">
        <svg class="animate-spin text-blue-600 w-full h-full" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <div class="absolute inset-0 flex items-center justify-center">
          <svg class="w-8 h-8 text-gray-900" fill="currentColor" viewBox="0 0 24 24">
            <!-- Icône GitHub générique -->
            <path fill-rule="evenodd" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.254-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.202 2.393.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z" clip-rule="evenodd" />
          </svg>
        </div>
      </div>
      
      <div>
        <h2 class="text-xl font-bold text-gray-900">Connexion sécurisée...</h2>
        <p class="text-sm text-gray-500 mt-2">Nous finalisons la liaison de votre compte.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'
import { projectService } from '@/services/projectService'
import { useProjectStore } from '@/stores/projectStore'

const projectStore = useProjectStore()

const route = useRoute()
const router = useRouter()
const notifStore = useNotificationStore()

onMounted(async () => {
  const code = route.query.code
  const platform = route.params.platform || 'github'
  
  if (!code) {
    notifStore.error("Échec de l'authentification : Code manquant.")
    router.push('/projects')
    return
  }

  try {
    // 1. On envoie le code au backend (via le service existant)
    await projectService.handleOAuthCallback(platform, code)
    
    // notifStore.success(`Compte ${platform.toUpperCase()} lié avec succès !`)
    
    // 2. On lit le "post-it"
    const origin = localStorage.getItem('oauth_origin')
    const pendingProjectStr = localStorage.getItem('pending_project')

    localStorage.removeItem('oauth_origin') // On nettoie
    localStorage.removeItem('pending_project')
    
    if (origin === 'auto_sync' && pendingProjectStr) {
      // notifStore.info("Compte lié. Relance de la synchronisation...")
      const pendingProject = JSON.parse(pendingProjectStr)
      
      // On crée le projet avec le nouveau token fraîchement acquis !
      await projectStore.createProject(pendingProject)
      notifStore.success("Compte lié. Projet privé créé et en cours d'analyse !")
      
      router.push('/projects')
      return
    }

    // 3. Redirection intelligente
    if (origin === 'create_project') {
      router.push('/projects?openModal=true') // On rajoutera une logique pour ré-ouvrir la modale
    } else if (origin === 'profile') {
      router.push('/profile')
    } else {
      router.push('/projects')
    }
    
  } catch (error) {
    const msg = error.response?.data?.error || "Impossible de lier le compte."
    notifStore.error(msg)
    router.push('/projects')
  }
})
</script>