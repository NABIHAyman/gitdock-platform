<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <h3 class="text-lg font-semibold text-gray-900 mb-4">
      {{ isEditing ? 'Modifier le collaborateur' : 'Ajouter un collaborateur' }}
    </h3>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label for="firstName" class="block text-sm font-medium text-gray-700 mb-2">
          Prénom
        </label>
        <input
          id="firstName"
          v-model="formData.firstName"
          type="text"
          required
          class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <div>
        <label for="lastName" class="block text-sm font-medium text-gray-700 mb-2">
          Nom
        </label>
        <input
          id="lastName"
          v-model="formData.lastName"
          type="text"
          required
          class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
          Email
        </label>
        <input
          id="email"
          v-model="formData.email"
          type="email"
          required
          :disabled="isEditing"
          class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100"
        />
      </div>

      <div>
        <label for="role" class="block text-sm font-medium text-gray-700 mb-2">
          Rôle
        </label>
        <select
          id="role"
          v-model="formData.role"
          required
          class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
        >
          <option v-for="role in availableRoles" :key="role.role" :value="role.role">
            {{ role.label }}
          </option>
        </select>
      </div>

      <div>
        <label for="projectId" class="block text-sm font-medium text-gray-700 mb-2">
          Projet (optionnel)
        </label>
        <select
          id="projectId"
          v-model="formData.projectId"
          :disabled="isEditing"
          class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
        >
          <option :value="null">Aucun projet</option>
          <option v-for="project in projects" :key="project.id" :value="project.id">
            {{ project.name || project.projectName }}
          </option>
        </select>
      </div>

      <div
        v-if="managerWarning"
        class="bg-yellow-50 border border-yellow-200 rounded-md p-4"
      >
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <p class="text-sm text-yellow-800">{{ managerWarning }}</p>
          </div>
        </div>
      </div>

      <div class="flex space-x-3">
        <button
          type="submit"
          :disabled="isLoading"
          class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
        >
          {{ isEditing ? 'Modifier' : 'Ajouter' }}
        </button>
        <button
          type="button"
          @click="$emit('cancel')"
          class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
        >
          Annuler
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'

const props = defineProps({
  projects: { type: Array, default: () => [] },
  availableRoles: { type: Array, default: () => [] },
  collaborator: { type: Object, default: null },
  isLoading: { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'cancel'])

const isEditing = computed(() => !!props.collaborator)

const formData = reactive({
  firstName: '',
  lastName: '',
  email: '',
  role: 'ROLE_DEVELOPER',
  projectId: null
})

const managerWarning = ref('')

const selectedProject = computed(() => {
  if (!formData.projectId) return null
  return props.projects.find((p) => p.id === formData.projectId)
})

watch(() => formData.projectId, () => {
  managerWarning.value = ''
  if (formData.projectId && selectedProject.value?.manager) {
    managerWarning.value = `Attention: Ce projet a déjà un manager (${selectedProject.value.manager.firstName} ${selectedProject.value.manager.lastName}).`
  }
})

// Pré-remplir le formulaire si on est en mode édition
watch(() => props.collaborator, (collaborator) => {
  if (collaborator) {
    formData.firstName = collaborator.firstName || collaborator.first_name || ''
    formData.lastName = collaborator.lastName || collaborator.last_name || ''
    formData.email = collaborator.email || ''
    formData.role = collaborator.role || 'ROLE_DEVELOPER'
    formData.projectId = collaborator.projectId || collaborator.project_id || null
  } else {
    // Reset form
    Object.assign(formData, {
      firstName: '',
      lastName: '',
      email: '',
      role: 'ROLE_DEVELOPER',
      projectId: null
    })
  }
}, { immediate: true })

const handleSubmit = () => {
  if (!formData.firstName || !formData.lastName || !formData.email || !formData.role) {
    return
  }

  const data = {
    firstName: formData.firstName,
    lastName: formData.lastName,
    email: formData.email,
    role: formData.role,
    projectId: formData.projectId,
  }

  emit('submit', data)
}
</script>