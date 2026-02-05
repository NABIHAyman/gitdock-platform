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
      <!-- Main Register Card -->
      <v-card width="550" elevation="12" rounded="xl" class="pa-10 mx-auto text-center">
        <h1 class="text-3xl font-bold text-slate-900 mb-2">Create Account</h1>
        <p class="text-slate-500 mb-8">Modern Git contribution management</p>

        <!-- Messages UX -->
        <div v-if="errorMessage" class="text-red-600 mb-4 text-sm font-bold">
          {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="text-green-600 mb-4 text-sm font-bold">
          {{ successMessage }}
        </div>

        <!-- Registration Form -->
        <v-form @submit.prevent="handleRegister" class="text-left">
          <!-- Nom et Prénom -->
          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-2">Prénom</label>
              <v-text-field v-model="form.firstName" placeholder="John" variant="outlined" density="comfortable" base-color="#e2e8f0" persistent-placeholder hide-details />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-2">Nom</label>
              <v-text-field v-model="form.lastName" placeholder="Doe" variant="outlined" density="comfortable" base-color="#e2e8f0" persistent-placeholder hide-details />
            </div>
          </div>

          <!-- Email -->
          <div class="mb-4">
            <label class="block text-sm font-bold text-slate-700 mb-2">Work Email</label>
            <v-text-field v-model="form.email" type="email" placeholder="name@company.com" variant="outlined" density="comfortable" base-color="#e2e8f0" persistent-placeholder hide-details />
          </div>

          <!-- Password -->
          <div class="mb-4">
            <label class="block text-sm font-bold text-slate-700 mb-2">Password</label>
            <v-text-field v-model="form.password" type="password" placeholder="••••••••" variant="outlined" density="comfortable" base-color="#e2e8f0" persistent-placeholder hide-details />
          </div>

          <div class="mb-8">
            <label class="block text-sm font-bold text-slate-700 mb-2">Confirm Password</label>
            <v-text-field v-model="form.confirmPassword" type="password" placeholder="••••••••" variant="outlined" density="comfortable" base-color="#e2e8f0" persistent-placeholder hide-details />
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
            Create my Account
          </v-btn>
        </v-form>

        <p class="mt-8 text-sm text-slate-500">
          Already have an account? 
          <router-link to="/login" class="text-indigo-600 font-bold hover:underline">
            Sign in
          </router-link>
        </p>
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
import { reactive, ref } from 'vue'
import axios from 'axios'

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

const handleRegister = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  // Validation simple côté front
  if (!form.firstName || !form.lastName || !form.email || !form.password || !form.confirmPassword) {
    errorMessage.value = 'Please fill all fields.'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMessage.value = 'Passwords do not match.'
    return
  }

  loading.value = true

  try {
    await axios.post('http://localhost:8080/api/auth/register', {
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      password: form.password
    })

    // Si backend renvoie succès
    successMessage.value = 'Registration successful! Check your email to activate your account.'
    form.firstName = ''
    form.lastName = ''
    form.email = ''
    form.password = ''
    form.confirmPassword = ''
  } catch (error) {
  if (error.response && error.response.data?.message) {
    errorMessage.value = error.response.data.message
  } else {
    errorMessage.value = 'An error occurred. Please try again.'
  }
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
