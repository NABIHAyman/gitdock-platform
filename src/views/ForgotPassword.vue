<template>
  <v-app class="login-page-bg">
    <v-main class="flex items-center justify-center py-10">
      <v-card width="480" elevation="12" rounded="xl" class="pa-10 mx-auto text-center">
        <h1 class="text-3xl font-bold text-slate-900 mb-2">Reset Password</h1>
        <p class="text-slate-500 mb-8">Enter your email to receive a reset link</p>

        <v-form @submit.prevent="handleForgotPassword" class="text-left">
          <label class="block text-sm font-bold text-slate-700 mb-2">Work Email</label>
          <v-text-field
            v-model="email"
            placeholder="name@company.com"
            variant="outlined"
            density="comfortable"
            class="mb-4"
            base-color="#e2e8f0"
            persistent-placeholder
          />

          <v-btn 
            type="submit" 
            block 
            size="large" 
            color="#2d336b" 
            class="text-none font-bold" 
            rounded="lg"
          >
            Send Reset Link
          </v-btn>

          <p v-if="message" class="mt-4 text-sm text-green-600">{{ message }}</p>
          <p v-if="errorMessage" class="mt-4 text-sm text-red-600">{{ errorMessage }}</p>
        </v-form>
      </v-card>
    </v-main>
  </v-app>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const email = ref('')
const message = ref('')
const errorMessage = ref('')

const handleForgotPassword = async () => {
  message.value = ''
  errorMessage.value = ''

  if (!email.value) {
    errorMessage.value = 'Please enter your email.'
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/auth/forgot-password', {
      email: email.value
    })
    message.value = response.data.message || 'Reset link sent. Check your email!'
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Error sending reset link.'
  }
}
</script>

<style scoped>
.login-page-bg {
  background-color: #f8fafc !important;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 24px 24px;
}
</style>
