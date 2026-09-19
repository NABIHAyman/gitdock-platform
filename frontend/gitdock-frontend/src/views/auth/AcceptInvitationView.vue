<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <div class="mx-auto h-12 w-12 bg-blue-600 rounded-lg flex items-center justify-center">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Accepter l'invitation
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Définissez votre mot de passe pour rejoindre GitDock
        </p>
      </div>

      <div v-if="isAccepted" class="rounded-md bg-green-50 p-4">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-green-800">Compte activé !</h3>
            <div class="mt-2 text-sm text-green-700">
              <p>Vous pouvez maintenant vous connecter à votre compte.</p>
            </div>
            <div class="mt-4">
              <router-link to="/login" class="text-sm font-medium text-green-800 underline hover:text-green-600">
                Aller à la connexion
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <form v-else class="mt-8 space-y-6" @submit.prevent="acceptInvitation">

        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="password" class="sr-only">Nouveau mot de passe</label>
            <input
              id="password"
              v-model="password"
              type="password"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="Nouveau mot de passe"
            />
          </div>
          <div>
            <label for="confirmPassword" class="sr-only">Confirmer le mot de passe</label>
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="Confirmer le mot de passe"
            />
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="isSubmitting"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3" v-if="!isSubmitting">
              <svg class="h-5 w-5 text-blue-500 group-hover:text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
              </svg>
            </span>
            {{ isSubmitting ? 'Activation en cours...' : 'Activer mon compte' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const router = useRouter()
const notificationStore = useNotificationStore()
const authStore = useAuthStore()

const token = ref('')
const password = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const isAccepted = ref(false)

onMounted(() => {
  token.value = route.query.token || ''
  if (!token.value) {
    notificationStore.error('Lien d\'invitation invalide.')
    router.push('/login')
  }
})

const acceptInvitation = async () => {
  if (password.value !== confirmPassword.value) {
    notificationStore.error('Les mots de passe ne correspondent pas.')
    return
  }

  if (password.value.length < 6) {
    notificationStore.error('Le mot de passe doit contenir au moins 6 caractères.')
    return
  }

  isSubmitting.value = true
  try {
    // Appel au store pour activer le compte
    await authStore.activateInvitedAccount(token.value, password.value)
    
    isAccepted.value = true
    notificationStore.success('Compte activé avec succès !')
    
    setTimeout(() => {
      router.push('/login')
    }, 3000)
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Erreur lors de l\'activation.')
  } finally {
    isSubmitting.value = false
  }
}
</script>