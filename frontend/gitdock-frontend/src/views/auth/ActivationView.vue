<template>
  <AuthLayout>
    <div class="bg-white py-8 px-6 shadow-lg rounded-lg max-w-md mx-auto">
      <!-- Loading State -->
      <div v-if="state === 'loading'" class="text-center">
        <div class="flex justify-center mb-4">
          <svg
              class="animate-spin h-8 w-8 text-blue-600"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
          >
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
        </div>
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">
          Verifying token...
        </h2>
        <p class="text-gray-600">Please wait</p>
      </div>

      <!-- Success State -->
      <div v-else-if="state === 'success'" class="text-center">
        <div class="flex justify-center mb-4">
          <svg class="h-16 w-16 text-green-500" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">
          Account successfully activated!
        </h2>
        <p class="text-gray-600 mb-6">
          Your account has been activated. You will be redirected to the login page shortly...
        </p>
        <router-link to="/login" class="inline-block text-blue-600 hover:text-blue-800 font-medium">
          Go to Login
        </router-link>
      </div>

      <!-- Error State -->
      <div v-else-if="state === 'error'" class="text-center">
        <div class="flex justify-center mb-4">
          <svg class="h-16 w-16 text-red-500" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">
          Activation Error
        </h2>
        <p class="text-gray-600 mb-6">
          {{ errorMessage }}
        </p>
        <router-link to="/login" class="inline-block px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 font-medium">
          Back to Login
        </router-link>
      </div>

      <!-- Form State -->
      <div v-else-if="state === 'form'">
        <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">
          Account Activation
        </h2>
        <p class="text-sm text-gray-600 mb-6 text-center">
          Create a password to activate your account
        </p>

        <form @submit.prevent="handleSubmit" class="space-y-5">
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              Password
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
                  @blur="validatePasswordField"
              />
              <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
              >
                <v-icon :icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'" size="small" />
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>

            <div v-if="passwordValidation.errors.length > 0" class="mt-2 text-xs text-gray-600 space-y-1">
              <p class="font-medium">Requirements:</p>
              <ul class="list-disc list-inside space-y-0.5">
                <li v-for="error in passwordValidation.errors" :key="error" class="text-red-600">
                  {{ error }}
                </li>
              </ul>
            </div>
          </div>

          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-2">
              Confirm Password
            </label>
            <div class="relative">
              <input
                  id="confirmPassword"
                  v-model="formData.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  required
                  class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 pr-10"
                  placeholder="••••••••"
                  :class="{ 'border-red-500': errors.confirmPassword }"
                  @blur="validatePasswordMatch"
              />
              <button type="button" @click="showConfirmPassword = !showConfirmPassword" class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700">
                <v-icon :icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'" size="small" />
              </button>
            </div>
            <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-600">{{ errors.confirmPassword }}</p>
          </div>

          <v-btn
              type="submit"
              block
              size="large"
              color="#2d336b"
              class="text-none font-bold !text-white"
              rounded="lg"
              :loading="isLoading"
          >
            Activate Account
          </v-btn>
        </form>
      </div>
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

// State management
const state = ref('loading')
const resetToken = ref('')
const errorMessage = ref('')

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

const passwordValidation = computed(() => {
  if (!formData.password) {
    return { isValid: false, errors: [] }
  }
  return validatePassword(formData.password)
})

onMounted(async () => {
  const token = route.query.token
  if (!token) {
    state.value = 'error'
    errorMessage.value = "Activation token missing. Please use the link sent to your email."
    return
  }

  resetToken.value = token

  try {
    await authService.validateActivationToken(token)
    state.value = 'form'
  } catch (error) {
    state.value = 'error'
    errorMessage.value = error.response?.data?.detail || error.message || "Invalid or expired activation token."
    notificationStore.error(errorMessage.value)
  }
})

const validateForm = () => {
  let isValid = true
  errors.password = ''
  errors.confirmPassword = ''

  if (!formData.password) {
    errors.password = 'Password is required'
    isValid = false
  } else {
    const validation = validatePassword(formData.password)
    if (!validation.isValid) {
      errors.password = validation.errors[0]
      isValid = false
    }
  }

  if (!formData.confirmPassword) {
    errors.confirmPassword = 'Confirmation is required'
    isValid = false
  } else if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = 'Passwords do not match'
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
    errors.confirmPassword = 'Passwords do not match'
  } else {
    errors.confirmPassword = ''
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return;
  isLoading.value = true;

  try {
    await authService.activateAccount({
      token: resetToken.value,
      password: formData.password,
      confirmPassword: formData.confirmPassword
    });

    state.value = 'success';
    notificationStore.success('Account successfully activated!');

    setTimeout(() => {
      router.push('/login');
    }, 3000);
  } catch (error) {
    const message = error.response?.data?.detail || error.message || "Error during activation";
    errorMessage.value = message;
    state.value = 'error';
    notificationStore.error(message);
  } finally {
    isLoading.value = false;
  }
};
</script>