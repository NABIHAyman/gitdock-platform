<template>
  <AuthLayout>
    <div class="bg-white py-8 px-6 shadow-lg rounded-lg max-w-md mx-auto">
      <h2 class="text-2xl font-bold text-slate-900 mb-6 text-center">
        Réinitialisation du mot de passe
      </h2>

      <div
          v-if="isSuccess"
          class="mb-6 bg-green-50 border border-green-200 rounded-lg p-4"
      >
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <p class="text-sm font-bold text-green-800">
              Mot de passe réinitialisé avec succès !
            </p>
            <p class="mt-1 text-sm text-green-700">
              Vous allez être redirigé vers la page de connexion...
            </p>
          </div>
        </div>
      </div>

      <div
          v-if="errorMessage"
          class="mb-6 bg-red-50 border border-red-200 rounded-lg p-4"
      >
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3 text-sm font-bold text-red-800">
            {{ errorMessage }}
          </div>
        </div>
      </div>

      <form v-if="!isSuccess && !errorMessage" @submit.prevent="handleSubmit" class="space-y-5">
        <div>
          <label for="password" class="block text-sm font-bold text-slate-700 mb-2">
            Nouveau mot de passe
          </label>
          <div class="relative">
            <input
                id="password"
                v-model="formData.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none transition"
                :class="{ 'border-red-500': errors.password }"
                placeholder="••••••••"
                @blur="validatePasswordField"
            />
            <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600"
            >
              <v-icon :icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'" size="small" />
            </button>
          </div>
          <p v-if="errors.password" class="mt-1 text-xs text-red-600 font-medium">{{ errors.password }}</p>

          <div v-if="passwordValidation.errors.length > 0" class="mt-3 p-3 bg-slate-50 rounded-lg text-xs text-slate-600 space-y-1">
            <p class="font-bold">Exigences :</p>
            <ul class="list-disc list-inside space-y-0.5">
              <li v-for="error in passwordValidation.errors" :key="error" class="text-red-600 font-medium">
                {{ error }}
              </li>
            </ul>
          </div>
        </div>

        <div>
          <label for="confirmPassword" class="block text-sm font-bold text-slate-700 mb-2">
            Confirmation du mot de passe
          </label>
          <div class="relative">
            <input
                id="confirmPassword"
                v-model="formData.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                required
                class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none transition"
                :class="{ 'border-red-500': errors.confirmPassword }"
                placeholder="••••••••"
                @blur="validatePasswordMatch"
            />
            <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600"
            >
              <v-icon :icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'" size="small" />
            </button>
          </div>
          <p v-if="errors.confirmPassword" class="mt-1 text-xs text-red-600 font-medium">{{ errors.confirmPassword }}</p>
        </div>

        <v-btn
            type="submit"
            block
            size="large"
            color="#2d336b"
            class="text-none font-bold !text-white mt-6"
            rounded="lg"
            :loading="isLoading"
            :disabled="isLoading"
        >
          Réinitialiser le mot de passe
        </v-btn>

        <div class="text-center mt-4">
          <router-link to="/login" class="text-sm font-bold text-indigo-600 hover:underline">
            Retour à la connexion
          </router-link>
        </div>
      </form>
    </div>
  </AuthLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authService } from '@/services/authService'
import { useNotificationStore } from '@/stores/notificationStore'
import { validatePassword } from '@/utils/passwordValidation.js'
import AuthLayout from '@/layouts/AuthLayout.vue'

const route = useRoute()
const router = useRouter()
const notificationStore = useNotificationStore()

const formData = reactive({
  password: '',
  confirmPassword: '',
})

const errors = reactive({
  password: '',
  confirmPassword: '',
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isLoading = ref(false)
const isSuccess = ref(false)
const errorMessage = ref('')
const resetToken = ref('')

const passwordValidation = computed(() => {
  if (!formData.password) {
    return { isValid: false, errors: [] }
  }
  return validatePassword(formData.password)
})

onMounted(() => {
  const token = route.query.token
  if (!token) {
    errorMessage.value = 'Token de réinitialisation manquant. Veuillez utiliser le lien reçu par email.'
  } else {
    resetToken.value = token
  }
})

const validateForm = () => {
  let isValid = true
  errors.password = ''
  errors.confirmPassword = ''

  if (!formData.password) {
    errors.password = 'Le mot de passe est obligatoire'
    isValid = false
  } else {
    const validation = validatePassword(formData.password)
    if (!validation.isValid) {
      errors.password = validation.errors[0]
      isValid = false
    }
  }

  if (!formData.confirmPassword) {
    errors.confirmPassword = 'La confirmation est obligatoire'
    isValid = false
  } else if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = 'Les mots de passe ne correspondent pas'
    isValid = false
  }

  return isValid
}

const validatePasswordField = () => {
  if (formData.password) {
    const validation = validatePassword(formData.password)
    errors.password = validation.isValid ? '' : validation.errors[0]
  }
}

const validatePasswordMatch = () => {
  if (formData.confirmPassword && formData.password !== formData.confirmPassword) {
    errors.confirmPassword = 'Les mots de passe ne correspondent pas'
  } else {
    errors.confirmPassword = ''
  }
}

const handleSubmit = async () => {
  if (!resetToken.value) {
    errorMessage.value = 'Token de réinitialisation manquant.'
    return
  }

  if (!validateForm()) return

  isLoading.value = true

  try {
    await authService.resetPassword(resetToken.value, formData.password)
    isSuccess.value = true
    notificationStore.success('Mot de passe réinitialisé avec succès')

    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (error) {
    const message = error.response?.data?.detail || error.message || 'Erreur lors de la réinitialisation'
    errorMessage.value = message
    notificationStore.error(message)
  } finally {
    isLoading.value = false
  }
}
</script>