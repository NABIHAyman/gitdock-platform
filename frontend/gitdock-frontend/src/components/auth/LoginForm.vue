<template>
  <div class="bg-white py-8 px-6 shadow-lg rounded-lg">
    <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">
      Connexion
    </h2>

    <form @submit.prevent="handleSubmit" class="space-y-6">
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

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
          Mot de passe
        </label>
        <div class="relative">
          <input
              id="password"
              v-model="formData.password"
              :type="showPassword ? 'text' : 'password'"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 pr-10"
              placeholder="••••••••"
              :class="{ 'border-red-500': errors.password }"
          />
          <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
          >
            <svg
                v-if="showPassword"
                class="h-5 w-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268-2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
              />
            </svg>
            <svg
                v-else
                class="h-5 w-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
              <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M2.458 12C3.732 7.943 7.522 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.478 0-8.268-2.943-9.542-7z"
              />
            </svg>
          </button>
        </div>
        <p v-if="errors.password" class="mt-1 text-sm text-red-600">
          {{ errors.password }}
        </p>
      </div>

      <div
          v-if="showActivationMessage"
          class="bg-yellow-50 border border-yellow-200 rounded-md p-4"
      >
        <div class="flex">
          <div class="flex-shrink-0">
            <svg
                class="h-5 w-5 text-yellow-400"
                fill="currentColor"
                viewBox="0 0 20 20"
            >
              <path
                  fill-rule="evenodd"
                  d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                  clip-rule="evenodd"
              />
            </svg>
          </div>
          <div class="ml-3 flex-1">
            <p class="text-sm text-yellow-800">
              Votre compte n'est pas encore activé. Voulez-vous recevoir un nouvel email d'activation ?
            </p>
            <div class="mt-3">
              <button
                  type="button"
                  @click="handleResendActivation"
                  :disabled="isResendingActivation"
                  class="text-sm font-medium text-yellow-800 hover:text-yellow-900 underline disabled:opacity-50"
              >
                {{ isResendingActivation ? 'Envoi en cours...' : "Renvoyer l'email d'activation" }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <button
          type="submit"
          :disabled="isLoading"
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span v-if="isLoading">Connexion en cours...</span>
        <span v-else>Se connecter</span>
      </button>

      <div class="text-center space-y-2">
        <router-link
            to="/auth/forgot-password"
            class="text-sm text-blue-600 hover:text-blue-800"
        >
          Mot de passe oublié ?
        </router-link>
        <div class="text-sm text-gray-600">
          Pas encore de compte ?
          <router-link
              to="/signup"
              class="text-blue-600 hover:text-blue-800 font-medium"
          >
            S'inscrire
          </router-link>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const formData = reactive({
  email: '',
  password: '',
})

const errors = reactive({
  email: '',
  password: '',
})

const showPassword = ref(false)
const isLoading = ref(false)
const showActivationMessage = ref(false)
const isResendingActivation = ref(false)

const validateForm = () => {
  errors.email = ''
  errors.password = ''

  if (!formData.email) {
    errors.email = "L'email est obligatoire"
    return false
  }

  if (!formData.email.includes('@')) {
    errors.email = "Format d'email invalide"
    return false
  }

  if (!formData.password) {
    errors.password = 'Le mot de passe est obligatoire'
    return false
  }

  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  showActivationMessage.value = false

  try {
    const result = await authStore.login({
      email: formData.email,
      password: formData.password,
    })

    if (!result.success && result.needsActivation) {
      showActivationMessage.value = true
    }
  } finally {
    isLoading.value = false
  }
}

const handleResendActivation = async () => {
  if (!formData.email) {
    return
  }

  isResendingActivation.value = true
  try {
    await authStore.resendActivationEmail(formData.email)
  } finally {
    isResendingActivation.value = false
  }
}
</script>