<template>
  <v-app class="register-page-bg">
    <!-- Header / Navigation -->
    <header class="flex justify-between items-center px-6 md:px-12 py-8 bg-transparent">
      <div class="flex items-center gap-3">
        <v-sheet 
          color="#2d336b" 
          width="36" 
          height="36" 
          class="rounded-lg flex items-center justify-center"
        >
          <div class="grid grid-cols-2 gap-1">
            <div class="w-2.5 h-2.5 bg-white rounded-sm"></div>
            <div class="w-2.5 h-2.5 bg-white rounded-sm"></div>
            <div class="w-2.5 h-2.5 bg-white rounded-sm"></div>
            <div class="w-2.5 h-2.5 bg-white rounded-sm"></div>
          </div>
        </v-sheet>
        <span class="font-bold text-xl tracking-tight text-[#2d336b]">GITDOCK</span>
      </div>
    </header>

    <v-main class="flex items-center justify-center py-10">
      <!-- Main Card -->
      <v-card width="420" elevation="12" rounded="xl" class="pa-10 mx-auto text-center">
        <h2 class="text-2xl font-bold text-slate-900 mb-2">Reset Password</h2>
        <p class="text-slate-500 mb-6">Enter a new secure password</p>

        <!-- Messages UX -->
        <div v-if="errorMessage" class="text-red-600 mb-4 text-sm font-bold">
          {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="text-green-600 mb-4 text-sm font-bold">
          {{ successMessage }}
        </div>

        <!-- Form -->
        <v-form @submit.prevent="resetPassword" class="text-left">
          <div class="mb-4">
            <label class="block text-sm font-bold text-slate-700 mb-2">New Password</label>
            <v-text-field
              v-model="password"
              type="password"
              placeholder="••••••••"
              variant="outlined"
              density="comfortable"
              base-color="#e2e8f0"
              persistent-placeholder
              hide-details
            />
          </div>

          <div class="mb-8">
            <label class="block text-sm font-bold text-slate-700 mb-2">Confirm Password</label>
            <v-text-field
              v-model="confirmPassword"
              type="password"
              placeholder="••••••••"
              variant="outlined"
              density="comfortable"
              base-color="#e2e8f0"
              persistent-placeholder
              hide-details
            />
          </div>

          <!-- Submit Button -->
          <v-btn
            type="submit"
            block
            size="large"
            color="#2d336b"
            class="text-none font-bold text-white"
            rounded="lg"
            :loading="loading"
            :disabled="loading"
          >
            Reset Password
          </v-btn>
        </v-form>
      </v-card>
    </v-main>

    <!-- Footer -->
    <footer class="pb-10 text-center">
      <div class="flex justify-center gap-6 mb-4 text-slate-400 text-xs font-medium">
        <a href="#" class="hover:underline">Privacy Policy</a>
        <a href="#" class="hover:underline">Terms</a>
        <a href="#" class="hover:underline">Security</a>
        <a href="#" class="hover:underline">Status</a>
      </div>
      <p class="text-[10px] text-slate-400 font-bold tracking-tight uppercase">
        © 2026 GITDOCK INC. STANDARD PROFESSIONAL EDITION
      </p>
    </footer>
  </v-app>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const token = ref(route.query.token || "")
const password = ref("")
const confirmPassword = ref("")
const loading = ref(false)
const errorMessage = ref("")
const successMessage = ref("")

const resetPassword = async () => {
  errorMessage.value = ""
  successMessage.value = ""

  if (!password.value || !confirmPassword.value) {
    errorMessage.value = "All fields are required."
    return
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = "Passwords do not match."
    return
  }

  loading.value = true

  try {
    await axios.post('http://localhost:8080/api/auth/reset-password', {
      token: token.value,
      newPassword: password.value
    })
    successMessage.value = "Password reset successfully"

    setTimeout(() => router.push("/login"), 2000)
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message ||
      "Invalid or expired reset token"
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page-bg {
  background-color: #f8fafc !important;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 24px 24px;
}

:deep(.v-field__outline) {
  --v-field-border-opacity: 1;
}
</style>
