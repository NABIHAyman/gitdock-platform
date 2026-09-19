<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center space-x-4 mb-4">
          <router-link
            :to="`/projects/${$route.params.id}`"
            class="inline-flex items-center text-sm text-gray-500 hover:text-gray-700"
          >
            ← Retour au projet
          </router-link>
        </div>
        
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">
              Parties du projet
            </h1>
            <p class="mt-1 text-sm text-gray-500">
              Organisez votre projet en parties avec des niveaux et des exigences de tags
            </p>
          </div>
          
          <button
            @click="showCreateModal = true"
            class="gd-btn-primary"
          >
            Ajouter une partie
          </button>
        </div>
      </div>

      <!-- Parts List -->
      <div class="space-y-6">
        <div v-if="isLoading" class="text-center py-8">
          <span class="animate-spin text-2xl">⟳</span>
          <p class="text-gray-500 mt-2">Chargement des parties...</p>
        </div>

        <div v-else-if="parts.length === 0" class="gd-card p-12 text-center">
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucune partie</h3>
          <p class="text-gray-500 mb-4">Créez votre première partie pour organiser votre projet.</p>
          <button @click="showCreateModal = true" class="gd-btn-primary">
            Ajouter une partie
          </button>
        </div>

        <div v-else>
          <div v-for="part in parts" :key="part.id" class="bg-white shadow rounded-lg mb-4">
            <div class="px-4 py-5 sm:p-6">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3 class="text-lg font-medium text-gray-900">{{ part.name }}</h3>
                  <p v-if="part.description" class="text-sm text-gray-500">{{ part.description }}</p>
                </div>
                <div class="flex items-center space-x-2">
                  <button @click="editPart(part)" class="text-blue-600 hover:text-blue-800 p-2">Modifier</button>
                  <button @click="togglePartExpansion(part.id)" class="text-gray-600 hover:text-gray-800 p-2">
                    {{ expandedParts.has(part.id) ? 'Réduire' : 'Voir Niveaux' }}
                  </button>
                  <button @click="deletePart(part)" class="text-red-600 hover:text-red-800 p-2">Supprimer</button>
                </div>
              </div>

              <!-- Levels -->
              <div v-if="expandedParts.has(part.id)" class="mt-4 border-t pt-4">
                <div v-if="part.levels && part.levels.length > 0" class="space-y-3">
                  <div v-for="level in part.levels" :key="level.id" class="border border-gray-200 rounded-lg p-4">
                    <div class="flex items-center justify-between mb-2">
                      <h4 class="text-md font-medium text-gray-800">{{ level.name }}</h4>
                      <span class="text-xs text-gray-500">Niveau {{ level.order || 1 }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="text-sm text-gray-500 italic">
                  Aucun niveau défini pour cette partie
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Create/Edit Modal -->
      <div v-if="showCreateModal || editingPart" class="fixed inset-0 z-50 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
          <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="closeModal"></div>
          <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">
                {{ editingPart ? 'Modifier la partie' : 'Créer une nouvelle partie' }}
              </h3>
              
              <form @submit.prevent="savePart" class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">Nom de la partie</label>
                  <input v-model="partForm.name" type="text" required class="gd-input mt-1" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">Description</label>
                  <textarea v-model="partForm.description" rows="3" class="gd-input mt-1"></textarea>
                </div>
              </form>
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
              <button @click="savePart" :disabled="!partForm.name || isLoading" class="gd-btn-primary sm:ml-3 sm:w-auto">
                {{ isLoading ? 'Enregistrement...' : 'Enregistrer' }}
              </button>
              <button @click="closeModal" class="gd-btn-secondary mt-3 sm:mt-0 sm:w-auto">
                Annuler
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'
import AppLayout from '@/layouts/AppLayout.vue'

const route = useRoute()
const notificationStore = useNotificationStore()

const isLoading = ref(false)
const parts = ref([])
const expandedParts = ref(new Set())
const showCreateModal = ref(false)
const editingPart = ref(null)

const partForm = reactive({
  name: '',
  description: ''
})

const togglePartExpansion = (partId) => {
  if (expandedParts.value.has(partId)) {
    expandedParts.value.delete(partId)
  } else {
    expandedParts.value.add(partId)
  }
}

const editPart = (part) => {
  editingPart.value = part
  partForm.name = part.name
  partForm.description = part.description || ''
}

const closeModal = () => {
  showCreateModal.value = false
  editingPart.value = null
  partForm.name = ''
  partForm.description = ''
}

const savePart = async () => {
  if (!partForm.name.trim()) return

  isLoading.value = true
  try {
    if (editingPart.value) {
      // Simulation: Update logic
      const index = parts.value.findIndex(p => p.id === editingPart.value.id)
      if (index !== -1) {
        parts.value[index] = { ...parts.value[index], name: partForm.name, description: partForm.description }
      }
      notificationStore.success('Partie modifiée avec succès')
    } else {
      // Simulation: Create logic
      parts.value.push({
        id: Date.now(),
        name: partForm.name,
        description: partForm.description,
        levels: []
      })
      notificationStore.success('Partie créée avec succès')
    }
    closeModal()
  } catch (error) {
    notificationStore.error('Erreur lors de l\'enregistrement')
  } finally {
    isLoading.value = false
  }
}

const deletePart = async (part) => {
  if (!confirm(`Êtes-vous sûr de vouloir supprimer la partie "${part.name}" ?`)) return
  try {
    parts.value = parts.value.filter(p => p.id !== part.id)
    expandedParts.value.delete(part.id)
    notificationStore.success('Partie supprimée avec succès')
  } catch (error) {
    notificationStore.error('Erreur lors de la suppression')
  }
}

const loadParts = async () => {
  isLoading.value = true
  try {
    // Mock data based on your data.sql
    parts.value = [
      { id: 1, name: 'Accounting Module', description: 'Financial accounting module', levels: [{id: 1, name: 'Débutant', order: 1}] },
      { id: 2, name: 'Reporting Module', description: 'Financial reporting module', levels: [] }
    ]
  } catch (error) {
    notificationStore.error('Erreur lors du chargement des parties')
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadParts()
})
</script>