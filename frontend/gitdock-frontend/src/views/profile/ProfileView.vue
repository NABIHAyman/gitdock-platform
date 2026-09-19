<template>
  <AppLayout>
    <div class="max-w-4xl mx-auto space-y-6">

      <!-- HERO PROFIL -->
      <div class="relative bg-gradient-to-br from-[#5b13ec] via-indigo-600 to-violet-700 rounded-3xl p-8 overflow-hidden shadow-2xl">
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-0 right-0 w-80 h-80 bg-white rounded-full -translate-y-1/2 translate-x-1/4"></div>
        </div>

        <div class="relative flex items-center gap-6">
          <!-- AVATAR -->
          <div class="relative flex-shrink-0">
            <img v-if="authStore.avatarUrl"
                 :src="authStore.avatarUrl"
                 class="w-24 h-24 rounded-3xl border-4 border-white/30 shadow-xl object-cover" />
            <div v-else
                 class="w-24 h-24 rounded-3xl bg-white/20 backdrop-blur-sm border-4 border-white/30 shadow-xl flex items-center justify-center">
              <span class="text-3xl font-black text-white">{{ userInitials }}</span>
            </div>
            <div class="absolute -bottom-2 -right-2 w-8 h-8 bg-emerald-400 rounded-xl border-2 border-white flex items-center justify-center shadow-lg">
              <v-icon icon="mdi-check" color="white" size="14"></v-icon>
            </div>
          </div>

          <!-- INFOS -->
          <div class="flex-1">
            <h1 class="text-2xl font-black text-white">
              {{ authStore.firstName }} {{ authStore.lastName }}
            </h1>
            <p class="text-white/60 text-sm mt-0.5">{{ authStore.email }}</p>
            <div class="flex items-center gap-3 mt-3">
              <span class="bg-white/20 backdrop-blur-sm text-white text-xs font-black px-3 py-1.5 rounded-full border border-white/30">
                {{ formattedRole }}
              </span>
              <span class="bg-white/10 text-white/70 text-xs font-bold px-3 py-1.5 rounded-full">
                ID #{{ authStore.userId }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- INFORMATIONS PERSONNELLES -->
      <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="px-8 py-5 border-b border-slate-100 flex items-center gap-3">
          <div class="w-8 h-8 bg-[#5b13ec]/10 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-account-outline" color="#5b13ec" size="18"></v-icon>
          </div>
          <h3 class="font-black text-slate-800">Informations Personnelles</h3>
        </div>

        <div class="p-8 grid grid-cols-2 gap-6">
          <div class="space-y-1">
            <p class="text-xs font-black text-slate-400 uppercase tracking-wider">Prénom</p>
            <p class="text-slate-800 font-bold">{{ authStore.firstName || '—' }}</p>
          </div>
          <div class="space-y-1">
            <p class="text-xs font-black text-slate-400 uppercase tracking-wider">Nom</p>
            <p class="text-slate-800 font-bold">{{ authStore.lastName || '—' }}</p>
          </div>
          <div class="space-y-1">
            <p class="text-xs font-black text-slate-400 uppercase tracking-wider">Email</p>
            <p class="text-slate-800 font-bold">{{ authStore.email || '—' }}</p>
          </div>
          <div class="space-y-1">
            <p class="text-xs font-black text-slate-400 uppercase tracking-wider">Rôle</p>
            <span class="inline-flex items-center gap-1.5 bg-[#5b13ec]/10 text-[#5b13ec] text-xs font-black px-3 py-1.5 rounded-full">
              <v-icon icon="mdi-shield-account" size="14"></v-icon>
              {{ formattedRole }}
            </span>
          </div>
        </div>
      </div>

      <!-- INTÉGRATIONS -->
      <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="px-8 py-5 border-b border-slate-100 flex items-center gap-3">
          <div class="w-8 h-8 bg-[#5b13ec]/10 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-connection" color="#5b13ec" size="18"></v-icon>
          </div>
          <div>
            <h3 class="font-black text-slate-800">Intégrations</h3>
            <p class="text-xs text-slate-400">Connectez des services tiers</p>
          </div>
        </div>

        <div class="p-8">
          <!-- GITHUB -->
          <div class="flex items-center justify-between p-5 bg-slate-50 rounded-2xl border border-slate-100 hover:border-slate-200 transition-all group">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-slate-900 rounded-2xl flex items-center justify-center shadow-md">
                <svg class="w-6 h-6 text-white fill-current" viewBox="0 0 24 24">
                  <path fill-rule="evenodd" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.254-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.202 2.393.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div>
                <h4 class="font-black text-slate-800">GitHub</h4>
                <p class="text-xs text-slate-400 mt-0.5">
                  {{ authStore.avatarUrl ? '✓ Compte lié' : 'Synchronisez vos dépôts privés' }}
                </p>
              </div>
            </div>

            <button @click="handleGitHubConnect"
                    class="flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-xl text-sm font-black hover:bg-slate-700 transition-all shadow-md">
              <v-icon icon="mdi-github" size="18" color="white"></v-icon>
              {{ authStore.avatarUrl ? 'Re-lier' : 'Connecter' }}
            </button>
          </div>
        </div>
      </div>

      <!-- ZONE DANGER -->
      <div class="bg-white rounded-3xl border border-red-100 shadow-sm overflow-hidden">
        <div class="px-8 py-5 border-b border-red-100 flex items-center gap-3">
          <div class="w-8 h-8 bg-red-50 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-alert-circle-outline" color="red" size="18"></v-icon>
          </div>
          <h3 class="font-black text-slate-800">Zone de danger</h3>
        </div>
        <div class="p-8 flex items-center justify-between">
          <div>
            <p class="font-bold text-slate-700">Se déconnecter</p>
            <p class="text-xs text-slate-400 mt-0.5">Vous serez redirigé vers la page de connexion</p>
          </div>
          <button @click="authStore.logout()"
                  class="flex items-center gap-2 px-5 py-2.5 bg-red-50 text-red-600 border border-red-200 rounded-xl text-sm font-black hover:bg-red-100 transition-all">
            <v-icon icon="mdi-logout" size="18" color="red"></v-icon>
            Déconnexion
          </button>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore'
import { projectService } from '@/services/projectService'

const authStore = useAuthStore()
const notifStore = useNotificationStore()

const userInitials = computed(() => {
  const f = authStore.firstName?.charAt(0) || ''
  const l = authStore.lastName?.charAt(0) || ''
  return (f + l).toUpperCase() || 'U'
})

const formattedRole = computed(() =>
    authStore.role?.replace('ROLE_', '').replace(/_/g, ' ') || 'Utilisateur'
)

const handleGitHubConnect = async () => {
  try {
    localStorage.setItem('oauth_origin', 'profile')
    const response = await projectService.initOAuthFlow('github')
    window.location.href = response.redirectUrl || (response as any).url
  } catch {
    notifStore.error("Impossible de contacter le service d'authentification.")
  }
}
</script>