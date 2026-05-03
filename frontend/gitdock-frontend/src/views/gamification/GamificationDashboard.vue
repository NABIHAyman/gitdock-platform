<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- 1. EN-TÊTE DE TA SECTION -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Gamification GitDock</h1>
          <p class="text-sm text-gray-600 mt-1">Gérez les badges, les tags et les niveaux de progression.</p>
        </div>

        <!-- Navigation interne (Tabs) pour le Manager -->
        <div v-if="isManager" class="flex bg-white p-1 rounded-lg border shadow-sm">
          <button @click="setSection('badges')"
                  :class="[activeSection === 'badges' ? 'bg-indigo-600 text-white' : 'text-gray-500 hover:bg-gray-50']"
                  class="px-4 py-2 rounded-md text-sm font-bold transition-all">
            Badges
          </button>
          <button @click="setSection('tags')"
                  :class="[activeSection === 'tags' ? 'bg-indigo-600 text-white' : 'text-gray-500 hover:bg-gray-50']"
                  class="px-4 py-2 rounded-md text-sm font-bold transition-all">
            Tags
          </button>
          <button @click="setSection('levels')"
                  :class="[activeSection === 'levels' ? 'bg-indigo-600 text-white' : 'text-gray-500 hover:bg-gray-50']"
                  class="px-4 py-2 rounded-md text-sm font-bold transition-all">
            Niveaux
          </button>
        </div>
      </div>

      <!-- 2. CONTENU DYNAMIQUE (MANAGER) -->
      <div v-if="isManager" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <transition name="fade" mode="out-in">
          <div :key="activeSection">
            <BadgeSection v-if="activeSection === 'badges'" />
            <TagSection v-else-if="activeSection === 'tags'" />
            <LevelSection v-else-if="activeSection === 'levels'" />
          </div>
        </transition>
      </div>

      <!-- 3. CONTENU DYNAMIQUE (UTILISATEUR) -->
      <div v-else class="space-y-6">
        <!-- Section XP & Niveau (ton code utilisateur) -->
        <section v-if="stats" class="bg-gradient-to-r from-indigo-600 to-purple-700 rounded-xl shadow-sm p-6 text-white">
          <div class="flex justify-between items-end mb-4">
            <div>
              <p class="text-sm opacity-80 uppercase font-bold tracking-wider">Progression Actuelle</p>
              <h2 class="text-3xl font-black">{{ stats.levelName }} <span class="text-xl font-normal opacity-70">(Lvl {{ stats.currentLevel }})</span></h2>
            </div>
            <div class="text-right font-bold">{{ stats.currentXp }} XP</div>
          </div>
          <div class="w-full bg-white/20 rounded-full h-3 overflow-hidden border border-white/10">
            <div class="bg-white h-full transition-all duration-1000" :style="{ width: (stats.currentXp / stats.nextLevelXp * 100) + '%' }"></div>
          </div>
        </section>

        <!-- Galerie des Badges -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Mes Badges Débloqués</h3>
          <div v-if="myBadges.length > 0" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
            <div v-for="badge in myBadges" :key="badge.id" class="text-center p-4 rounded-lg bg-gray-50 hover:bg-indigo-50 transition-colors border border-transparent hover:border-indigo-100">
              <img :src="badge.iconUrl" :alt="badge.name" class="w-12 h-12 mx-auto mb-2" />
              <p class="text-xs font-bold text-gray-800">{{ badge.name }}</p>
            </div>
          </div>
          <div v-else class="text-center py-10 text-gray-500 italic">Aucun badge pour le moment.</div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { useAuthStore } from '@/stores/authStore'
import { userProgressService } from '@/services/UserProgressService'
import { userBadgeService } from '@/services/UserBadgeService'

// Composants Manager
import BadgeSection from '@/components/gamification/dashboard/manager/BadgeSection.vue'
import TagSection from '@/components/gamification/dashboard/manager/TagSection.vue'
import LevelSection from '@/components/gamification/dashboard/manager/LevelSection.vue'

// --- LOGIQUE ---
const authStore = useAuthStore()
const activeSection = ref('badges')
const stats = ref<any>(null)
const myBadges = ref<any[]>([])

const isManager = computed(() => {
  const roles = authStore.user?.roles || []
  return roles.includes('ROLE_MANAGER') || roles.includes('ROLE_ADMIN')
})

const setSection = (s: string) => activeSection.value = s

onMounted(async () => {
  if (!isManager.value) {
    try {
      const [p, b] = await Promise.all([
        userProgressService.getProgress(),
        userBadgeService.getMyBadges()
      ])
      stats.value = p
      myBadges.value = b
    } catch (err) {
      console.error("Erreur de chargement", err)
    }
  }
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>