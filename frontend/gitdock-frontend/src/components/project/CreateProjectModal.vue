<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Overlay (fond gris) -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="closeModal"></div>

      <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

      <!-- Modale -->
      <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
        <form @submit.prevent="handleSubmit">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">Nouveau Projet</h3>
            <p class="mt-1 text-sm text-muted">Connectez votre dépôt Git pour l'analyser.</p>

            <div class="mt-4 space-y-4">
              <!-- URL du dépôt -->
              <div>
                <label class="block text-sm font-medium text-gray-700">URL du dépôt (Git)</label>
                <input v-model="form.url" type="url" required placeholder="https://github.com/..." @input="detectPlatform" class="gd-input mt-1">
              </div>

              <!-- Nom du projet -->
              <div>
                <label class="block text-sm font-medium text-gray-700">Nom du projet</label>
                <input v-model="form.name" type="text" required placeholder="Ex: Mon Super Projet" class="gd-input mt-1">
              </div>

              <!-- Description -->
              <div>
                <label class="block text-sm font-medium text-gray-700">Description</label>
                <textarea v-model="form.description" rows="2" class="gd-input mt-1"></textarea>
              </div>

              <!-- Bloc OAuth GitHub -->
              <div v-if="form.platform === 'GITHUB'" class="bg-blue-50 border border-blue-200 rounded-lg p-4 flex items-start">
                <svg class="h-5 w-5 text-blue-600 mt-0.5 mr-3 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div class="flex-1">
                  <h4 class="text-sm font-medium text-blue-900">Intégration GitHub</h4>
                  <p class="text-xs text-blue-700 mt-1">Pour synchroniser un dépôt privé ou créer des webhooks automatiquement, vous devez lier votre compte GitHub.</p>
                  <button type="button" @click="handleGitHubConnect" class="mt-3 text-xs font-semibold bg-white border border-blue-300 text-blue-700 px-3 py-1.5 rounded shadow-sm hover:bg-blue-50 transition-colors">
                    Lier mon compte GitHub
                  </button>
                </div>
              </div>

              <!-- Plateforme & Visibilité -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">Plateforme</label>
                  <select v-model="form.platform" required class="gd-input mt-1">
                    <option value="">Sélectionner</option>
                    <option value="GITHUB">GitHub</option>
                    <option value="GITLAB">GitLab</option>
                    <option value="BITBUCKET">Bitbucket</option>
                    <option value="AZURE">Azure DevOps</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">Visibilité</label>
                  <select v-model="form.visibility" class="gd-input mt-1">
                    <option value="PUBLIC">Public</option>
                    <option value="PRIVATE">Privé</option>
                  </select>
                </div>
              </div>

              <!-- Affichage des erreurs -->
              <div v-if="error" class="bg-red-50 border-l-4 border-red-400 p-4 mt-4">
                <p class="text-sm text-red-700">{{ error }}</p>
              </div>
            </div>
          </div>

          <!-- Boutons d'action -->
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button type="submit" :disabled="isLoading || !form.url || !form.platform || !form.name" class="gd-btn-primary sm:ml-3 sm:w-auto">
              <span v-if="isLoading" class="mr-2 inline-block animate-spin">⟳</span>
              {{ isLoading ? 'Création...' : 'Créer le projet' }}
            </button>
            <button type="button" @click="closeModal" class="gd-btn-secondary mt-3 sm:mt-0 sm:w-auto">
              Annuler
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useProjectStore } from '@/stores/projectStore'
import { ProjectPlatform } from '@/types/project'
import { projectService } from '@/services/projectService'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close'])

const projectStore = useProjectStore()
const isLoading = ref(false)
const error = ref('')

const form = reactive({
  name: '',
  description: '',
  url: '',
  platform: '',
  visibility: 'PRIVATE'
})

const detectPlatform = () => {
  const url = form.url.toLowerCase()
  if (url.includes('github.com')) form.platform = ProjectPlatform.GITHUB
  else if (url.includes('gitlab.com')) form.platform = ProjectPlatform.GITLAB
  else if (url.includes('bitbucket.org')) form.platform = ProjectPlatform.BITBUCKET
  else if (url.includes('dev.azure.com') || url.includes('visualstudio.com')) form.platform = ProjectPlatform.AZURE
}

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.url = ''
  form.platform = ''
  form.visibility = 'PRIVATE'
  error.value = ''
}

const closeModal = () => {
  resetForm()
  emit('close')
}

const handleSubmit = async () => {
  if (!form.url || !form.platform || !form.name) {
    error.value = "L'URL, le nom et la plateforme sont obligatoires."
    return
  }

  error.value = ''
  isLoading.value = true

  try {
    const existingProjects = projectStore.projects || []
    if (existingProjects.some(p => p.url === form.url)) {
      error.value = 'Ce projet existe déjà'
      isLoading.value = false
      return
    }

    if (existingProjects.some(p => p.url === form.url)) {
      error.value = 'Ce projet existe déjà dans GitDock.'
      isLoading.value = false
      return
    }
    if (existingProjects.some(p => p.name.toLowerCase() === form.name.toLowerCase())) {
      error.value = 'Un projet porte déjà ce nom. Veuillez en choisir un autre.'
      isLoading.value = false
      return
    }

    // Appel direct au backend
    await projectStore.createProject({
      name: form.name,
      description: form.description,
      url: form.url,
      platform: form.platform,
      visibility: form.visibility
    })

    // On rafraichit la liste des projets dans le store
    await projectStore.fetchProjects()
    
    closeModal()
  } catch (err) {
    const errorMessage = err.response?.data?.message || err.message;
    
    // 👇 LA MAGIE OPÈRE ICI 👇
    if (errorMessage === 'OAUTH_REQUIRED') {
        // L'utilisateur veut ajouter un repo privé mais n'a pas lié GitHub.
        
        // 1. On sauvegarde TOUT le formulaire pour le recréer automatiquement après
        localStorage.setItem('pending_project', JSON.stringify(form));
        localStorage.setItem('oauth_origin', 'auto_sync'); // 👈 Mot de passe pour OAuthCallbackView
        
        // 2. On l'emmène sur GitHub en silence
        const response = await projectService.initOAuthFlow('github');
        window.location.href = response.url;
        return; // 🛑 On arrête l'exécution ici !
    }
        
    error.value = err.response?.data?.message || err.message || 'Erreur lors de la création'
  } finally {
    isLoading.value = false
  }
}

const handleGitHubConnect = async () => {
  try {
    // On écrit le post-it pour savoir où revenir
    localStorage.setItem('oauth_origin', 'create_project')
    
    // On récupère l'URL d'autorisation depuis le backend
    const response = await projectService.initOAuthFlow('github')
    
    // On quitte GitDock direction GitHub !
    window.location.href = response.url
  } catch (error) {
    error.value = "Impossible d'initialiser la connexion avec GitHub."
  }
}
</script>