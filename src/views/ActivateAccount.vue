<template>
  <v-app class="login-page-bg">
    <v-main class="flex items-center justify-center">
      <v-card width="420" class="pa-8 text-center" rounded="xl" elevation="4">

        <h2 class="text-2xl font-bold mb-4">Account Activation</h2>

        <div v-if="loading" class="text-gray-600">
          Activating your account...
        </div>

        <div
          v-if="successMessage"
          class="mt-4 text-green-600 font-semibold"
        >
          {{ successMessage }}
        </div>

        <div
          v-if="errorMessage"
          class="mt-4 text-red-600 font-semibold"
        >
          {{ errorMessage }}
        </div>

        <v-btn
          v-if="successMessage"
          class="mt-6 font-bold"
          color="#2d336b"
          block
          @click="$router.push('/login')"
        >
          Go to login
        </v-btn>

      </v-card>
    </v-main>
  </v-app>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const successMessage = ref('')
const errorMessage = ref('')

onMounted(async () => {
  const token = route.query.token

  if (!token) {
    errorMessage.value = 'Invalid activation link'
    loading.value = false
    return
  }

  try {
    await axios.get(
      'http://localhost:8080/api/auth/activate',
      { params: { token } }
    )
    successMessage.value = 'Your account has been activated successfully!'
  } catch (e) {
    errorMessage.value = 'Activation failed or token expired'
  } finally {
    loading.value = false
  }
})
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