<script setup lang="ts">
import { ref } from 'vue'
import BadgeSection from '@/components/gamification/dashboard/manager/BadgeSection.vue'
import TagSection from '@/components/gamification/dashboard/manager/TagSection.vue'
import LevelSection from '@/components/gamification/dashboard/manager/LevelSection.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
// L'état qui définit quelle section est affichée par défaut
const activeSection = ref('badges')

// Petite fonction pour changer de section proprement
const setSection = (section: string) => {
  activeSection.value = section
}
</script>

<template>
  <v-app shadow-none>
    <div class="flex h-screen bg-slate-50 overflow-hidden font-sans text-slate-900">

      <aside class="w-64 bg-white border-r border-slate-200 flex flex-col shrink-0">
        <div class="p-6 flex items-center gap-3">
          <div class="w-9 h-9 bg-[#5b13ec] rounded-xl flex items-center justify-center shadow-lg shadow-indigo-100">
            <v-icon icon="mdi-anchor" color="white"></v-icon>
          </div>
          <h1 class="text-xl font-bold tracking-tight text-slate-900">Gitdock</h1>
        </div>

        <nav class="flex-1 px-4 space-y-1.5 mt-4">
          <button @click="setSection('badges')"
                  :class="[activeSection === 'badges' ? 'bg-[#5b13ec]/10 text-[#5b13ec]' : 'text-slate-500 hover:bg-slate-50', 'w-full flex items-center gap-3 px-4 py-2.5 rounded-xl transition-all font-bold text-sm']">
            <v-icon icon="mdi-medal-outline"></v-icon>
            <span>Badge Gallery</span>
          </button>

          <button @click="setSection('tags')"
                  :class="[activeSection === 'tags' ? 'bg-[#5b13ec]/10 text-[#5b13ec]' : 'text-slate-500 hover:bg-slate-50', 'w-full flex items-center gap-3 px-4 py-2.5 rounded-xl transition-all font-bold text-sm']">
            <v-icon icon="mdi-tag-outline"></v-icon>
            <span>Tags Management</span>
          </button>

          <button @click="setSection('levels')"
                  :class="[activeSection === 'levels' ? 'bg-[#5b13ec]/10 text-[#5b13ec]' : 'text-slate-500 hover:bg-slate-50', 'w-full flex items-center gap-3 px-4 py-2.5 rounded-xl transition-all font-bold text-sm']">
            <v-icon icon="mdi-trending-up"></v-icon>
            <span>Levels & Ranks</span>
          </button>
        </nav>

        <div class="p-4 border-t border-slate-100">
          <div class="flex items-center gap-3 px-2 py-2">
            <v-avatar color="slate-200" size="32">
              <v-icon icon="mdi-account" size="20"></v-icon>
            </v-avatar>
            <div class="flex flex-col">
              <span class="text-xs font-bold">
                {{ `${authStore.firstName || ''} ${authStore.lastName || ''}`.trim() || 'Utilisateur' }}
              </span>
              <span class="text-[10px] text-slate-400">
                {{ authStore.role?.replace('ROLE_', '').replace('_', ' ') || '—' }}
              </span>
            </div>
          </div>
        </div>
      </aside>

      <main class="flex-1 flex flex-col min-w-0 overflow-hidden relative">

        <transition name="fade" mode="out-in">
          <div :key="activeSection" class="h-full flex flex-col">
            <BadgeSection v-if="activeSection === 'badges'" />
            <TagSection v-else-if="activeSection === 'tags'" />
            <LevelSection v-else-if="activeSection === 'levels'" />
          </div>
        </transition>

      </main>

    </div>
  </v-app>
</template>

<style scoped>
/* Animation fluide pour le changement de section */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* On cache la scrollbar globale pour garder le design propre */
::-webkit-scrollbar {
  width: 0px;
}
</style>