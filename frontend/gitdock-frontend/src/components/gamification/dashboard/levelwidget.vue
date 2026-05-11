<!--
  ╔══════════════════════════════════════════════════════════════╗
  ║  PATCH : Section "Mon Niveau" dans DashboardHomeView.vue     ║
  ║  Remplace le bloc <section v-if="userStats"> existant        ║
  ║  par ce composant <LevelWidget> autonome.                    ║
  ╚══════════════════════════════════════════════════════════════╝

  UTILISATION dans DashboardHomeView.vue :
  ─────────────────────────────────────────
  1. Importer : import LevelWidget from '@/components/dashboard/LevelWidget.vue'
  2. Remplacer l'ancien bloc "Mon niveau" par : <LevelWidget />
-->

<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 h-full flex flex-col">

    <!-- HEADER -->
    <div class="flex items-center justify-between mb-5">
      <h3 class="text-sm font-black text-slate-700 uppercase tracking-wide flex items-center gap-2">
        <v-icon icon="mdi-trending-up" color="#5b13ec" size="18"></v-icon>
        Mon Niveau
      </h3>
      <router-link to="/dashboard/gamification"
                   class="text-[10px] font-black text-[#5b13ec] hover:underline flex items-center gap-1">
        Voir mes badges →
      </router-link>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="flex flex-col items-center justify-center flex-1 gap-3">
      <v-progress-circular indeterminate color="#5b13ec" size="32"></v-progress-circular>
      <span class="text-xs text-slate-300 font-bold animate-pulse">Chargement...</span>
    </div>

    <!-- DATA -->
    <template v-else-if="progress">

      <!-- CERCLE NIVEAU -->
      <div class="flex flex-col items-center mb-5">
        <div class="relative w-24 h-24 mb-3">
          <!-- Anneau SVG -->
          <svg class="w-full h-full -rotate-90" viewBox="0 0 100 100">
            <!-- Track -->
            <circle cx="50" cy="50" r="40" fill="none" stroke="#f1f5f9" stroke-width="10"/>
            <!-- Progression -->
            <circle cx="50" cy="50" r="40" fill="none"
                    stroke="#5b13ec" stroke-width="10"
                    stroke-linecap="round"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="dashOffset"
                    class="transition-all duration-1000"/>
          </svg>
          <!-- Numéro au centre -->
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <span class="text-2xl font-black text-[#5b13ec]">{{ progress.currentLevel }}</span>
          </div>
        </div>
        <p class="font-black text-slate-800">{{ progress.levelName }}</p>
        <p class="text-xs text-slate-400 mt-0.5">{{ progress.totalExperience }} XP</p>
      </div>

      <!-- BARRE PROGRESSION TEXTE -->
      <div class="mb-4">
        <div class="flex justify-between text-[10px] font-bold text-slate-400 mb-1.5">
          <span>Progrès</span>
          <span>{{ progressPercent }}%</span>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
          <div class="h-full rounded-full bg-gradient-to-r from-[#5b13ec] to-violet-400 transition-all duration-1000"
               :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>

      <!-- INFOS NEXT LEVEL -->
      <div class="bg-slate-50 rounded-xl p-3 text-center border border-slate-100 mb-4">
        <p class="text-[10px] text-slate-400 font-bold mb-0.5">Pour atteindre Lv{{ progress.currentLevel + 1 }}</p>
        <p class="font-black text-slate-700 text-sm">
          encore
          <span class="text-[#5b13ec]">{{ xpToNext }} XP</span>
        </p>
        <p v-if="progress.nextLevelName" class="text-[10px] text-slate-400 mt-0.5">
          → {{ progress.nextLevelName }}
        </p>
      </div>

      <!-- STATS BADGES -->
      <div class="grid grid-cols-2 gap-2">
        <div class="bg-indigo-50 rounded-xl p-3 text-center border border-indigo-100">
          <p class="text-lg font-black text-[#5b13ec]">{{ progress.badgeCount ?? '—' }}</p>
          <p class="text-[10px] text-indigo-400 font-bold">Badges</p>
        </div>
        <div class="bg-amber-50 rounded-xl p-3 text-center border border-amber-100">
          <p class="text-lg font-black text-amber-600">{{ progress.totalExperience }}</p>
          <p class="text-[10px] text-amber-400 font-bold">XP Total</p>
        </div>
      </div>
    </template>

    <!-- AUCUNE DONNÉE -->
    <div v-else class="flex flex-col items-center justify-center flex-1 gap-3 text-center">
      <div class="w-16 h-16 bg-[#5b13ec]/10 rounded-2xl flex items-center justify-center">
        <v-icon icon="mdi-star-outline" color="#5b13ec" size="28"></v-icon>
      </div>
      <p class="text-sm font-black text-slate-400">Aucune progression</p>
      <p class="text-xs text-slate-300">Complétez des tâches pour gagner de l'XP</p>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { userProgressService } from '@/services/UserProgressService'
import { userBadgeService }    from '@/services/UserBadgeService'

const loading  = ref(true)
const progress = ref<any>(null)

// Circonférence cercle SVG (r=40)
const circumference = 2 * Math.PI * 40  // ≈ 251.3

const progressPercent = computed(() => {
  if (!progress.value) return 0
  const { totalExperience, nextLevelXp, prevLevelXp = 0 } = progress.value
  const range   = nextLevelXp - prevLevelXp
  const current = totalExperience - prevLevelXp
  if (range <= 0) return 100
  return Math.min(Math.round((current / range) * 100), 100)
})

const dashOffset = computed(() =>
    circumference - (circumference * progressPercent.value) / 100
)

const xpToNext = computed(() => {
  if (!progress.value) return 0
  return Math.max(0, progress.value.nextLevelXp - progress.value.totalExperience)
})

onMounted(async () => {
  loading.value = true
  try {
    const [prog, badges] = await Promise.all([
      userProgressService.getProgress(),
      userBadgeService.getMyBadges(),
    ])
    progress.value = {
      ...prog,
      badgeCount: badges?.length ?? 0,
    }
  } catch (err) {
    console.error('[LevelWidget] Erreur:', err)
  } finally {
    loading.value = false
  }
})
</script>