<template>
  <v-app class="login-page-bg">
    <!-- Header / Navigation -->
    <header class="flex justify-between items-center px-6 md:px-12 py-6 bg-transparent">
      <div class="flex items-center gap-2">
        <v-sheet
          color="#2d336b"
          width="32"
          height="32"
          class="rounded flex items-center justify-center"
        >
          <!-- Logo GITDOCK approximatif selon l'image [1] -->
          
        </v-sheet>
        <span class="font-bold text-lg tracking-wider text-[#2d336b]">GITDOCK</span>
      </div>
    </header>

    <v-main class="flex items-center justify-center py-10">
      <!-- Main Login Card -->
      <v-card 
        width="480" 
        elevation="12" 
        rounded="xl" 
        class="pa-10 mx-auto text-center"
      >
        <h1 class="text-3xl font-bold text-slate-900 mb-2">Sign in</h1>
        <p class="text-slate-500 mb-8">Modern Git contribution management</p>

        <!-- Social Buttons -->
        <div class="flex flex-col gap-3 mb-6">
          <v-btn block size="large" color="#181717" class="text-none font-bold" rounded="lg">
            <v-icon start icon="mdi-github" /> Continue with GitHub
          </v-btn>
          
          <v-btn block size="large" variant="outlined" color="grey-lighten-2" class="text-none font-bold text-slate-700" rounded="lg">
            <v-icon start icon="mdi-gitlab" color="orange" /> Continue with GitLab
          </v-btn>
          
          <v-btn block size="large" color="#0052cc" class="text-none font-bold" rounded="lg">
            <v-icon start icon="mdi-bitbucket" /> Continue with Bitbucket
          </v-btn>
        </div>

        <!-- Separator -->
        <div class="flex items-center my-6">
          <v-divider />
          <span class="px-4 text-[13px] font-bold text-slate-400 uppercase whitespace-nowrap">Or use email</span>
          <v-divider />
        </div>

        <!-- Email Form -->
        <v-form @submit.prevent="handleSignIn" class="text-left">
        

        <!-- MESSAGE D’ERREUR -->
          <v-alert
           v-if="errorMessage"
           type="error"
           variant="tonal"
           class="mb-4"
          >
           {{ errorMessage }}
          </v-alert>
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

          <div class="flex justify-between items-center mb-2">
          <label class="text-sm font-bold text-slate-700">Password</label>
          <router-link to="/ForgotPassword" class="text-[11px] font-bold text-indigo-600 hover:underline">
          Forgot password?
          </router-link>
          </div>

          <v-text-field
            v-model="password"
            type="password"
            placeholder="••••••••"
            variant="outlined"
            density="comfortable"
            class="mb-6"
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
            Sign in to Account
          </v-btn>
        </v-form>
        

        <p class="mt-8 text-sm text-slate-500">
        Don't have an account? 
        <router-link to="/register" class="text-indigo-600 font-bold hover:underline">
        Get started for free
        </router-link>
        </p>

      </v-card>
    </v-main>

    <!-- Footer -->
    <footer class="pb-10 pt-4 text-center">
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
import axios from 'axios'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const router = useRouter()

const handleSignIn = async () => {
  errorMessage.value = ''

  if (!email.value || !password.value) {
    errorMessage.value = 'Please fill all fields.'
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      email: email.value,
      password: password.value
    })

   
    router.push('/dashboard')  

  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage.value = error.response.data.message
    } else {
      errorMessage.value = 'An error occurred. Please try again.'
    }
  }
}
</script>


<style scoped>

.login-page-bg {
  background-color: #f8fafc !important;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 24px 24px;
}

/* Ajustements Vuetify pour correspondre au design épuré */
:deep(.v-field__outline) {
  --v-field-border-opacity: 1;
}
:deep(.v-btn) {
  letter-spacing: normal;
}
</style>