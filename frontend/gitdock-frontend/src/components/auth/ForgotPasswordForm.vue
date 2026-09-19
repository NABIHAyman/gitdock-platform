<template>
  <div class="bg-white py-8 px-6 shadow-lg rounded-lg">
    <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">
      Mot de passe oublié
    </h2>

    <div
        v-if="isSuccess"
        class="mb-6 bg-green-50 border border-green-200 rounded-md p-4"
    >
      <div class="flex">
        <div class="flex-shrink-0">
          <svg
              class="h-5 w-5 text-green-400"
              fill="currentColor"
              viewBox="0 0 20 20"
          >
            <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
            />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-green-800">
            Email envoyé avec succès !
          </p>
          <p class="mt-1 text-sm text-green-700">
            Un lien de réinitialisation de mot de passe a été envoyé à votre adresse email.
            Veuillez vérifier votre boîte de réception.
          </p>
        </div>
      </div>
    </div>

    <form v-else @submit.prevent="handleSubmit" class="space-y-6">
      <div>
        <p class="text-sm text-gray-600 mb-4">
          Entrez votre adresse email et nous vous enverrons un lien pour réinitialiser votre mot de passe.
        </p>
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
            class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
            placeholder="votre@email.com"
            :class="{ 'border-red-500': errors.email }"
        />
        <p v-if="errors.email" class="mt-1 text-sm text-red-600">
          {{ errors.email }}
        </p>
      </div>

      <button
          type="submit"
          :disabled="isLoading"
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span v-if="isLoading">Envoi en cours...</span>
        <span v-else>Envoyer le lien de réinitialisation</span>
      </button>

      <div class="text-center">
        <router-link
            to="/login"
            class="text-sm text-blue-600 hover:text-blue-800 font-medium"
        >
          Retour à la connexion
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { authService } from '@/services/authService'
import { useNotificationStore } from '@/stores/notificationStore'

const notificationStore = useNotificationStore()

const formData = reactive({
  email: '',
})

const errors = reactive({
  email: '',
})

const isLoading = ref(false)
const isSuccess = ref(false)

const validateForm = () => {
  errors.email = ''

  if (!formData.email.trim()) {
    errors.email = "L'email est obligatoire"
    return false
  }

  if (!formData.email.includes('@')) {
    errors.email = "Format d'email invalide"
    return false
  }

  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true

  try {
    await authService.requestPasswordReset(formData.email.trim())
    isSuccess.value = true
    notificationStore.success('Email de réinitialisation envoyé avec succès')
  } catch (error) {
    const message = error.response?.data?.detail || error.message || "Erreur lors de l'envoi de l'email"
    notificationStore.error(message)
    errors.email = message
  } finally {
    isLoading.value = false
  }
}
</script>