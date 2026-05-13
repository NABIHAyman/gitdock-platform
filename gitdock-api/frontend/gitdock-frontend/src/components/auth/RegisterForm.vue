<template>
  <div class="bg-white py-8 px-6 shadow-lg rounded-lg">
    <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">
      Inscription
    </h2>

    <form @submit.prevent="handleSubmit" class="space-y-5">
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
            placeholder="Votre prénom"
            :class="{ 'border-red-500': errors.first_name }"
        />
        <p v-if="errors.first_name" class="mt-1 text-sm text-red-600">
          {{ errors.first_name }}
        </p>
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
            placeholder="Votre nom"
            :class="{ 'border-red-500': errors.last_name }"
        />
        <p v-if="errors.last_name" class="mt-1 text-sm text-red-600">
          {{ errors.last_name }}
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
        <span v-if="isLoading">Inscription en cours...</span>
        <span v-else>S'inscrire</span>
      </button>

      <div class="text-center text-sm text-gray-600">
        Déjà un compte ?
        <router-link
            to="/login"
            class="text-blue-600 hover:text-blue-800 font-medium"
        >
          Se connecter
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const formData = reactive({
  firstName: '',
  lastName: '',
  email: '',
})

const errors = reactive({
  first_name: '',
  last_name: '',
  email: '',
})

const isLoading = ref(false)

const validateForm = () => {
  let isValid = true

  errors.first_name = ''
  errors.last_name = ''
  errors.email = ''

  if (!formData.firstName.trim()) {
    errors.first_name = 'Le prénom est obligatoire'
    isValid = false
  }

  if (!formData.lastName.trim()) {
    errors.last_name = 'Le nom est obligatoire'
    isValid = false
  }

  if (!formData.email.trim()) {
    errors.email = "L'email est obligatoire"
    isValid = false
  } else if (!formData.email.includes('@')) {
    errors.email = "Format d'email invalide"
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true

  try {
    const result = await authStore.signup({
      firstName: formData.firstName.trim(),
      lastName: formData.lastName.trim(),
      email: formData.email.trim(),
    })

    // Note: Le chargement est arrêté uniquement si succès ou erreur spécifique
    // car le store gère normalement la redirection ou l'affichage de message
    if (!result || !result.success) {
      isLoading.value = false
    }
  } catch (error) {
    isLoading.value = false
  }
}
</script>