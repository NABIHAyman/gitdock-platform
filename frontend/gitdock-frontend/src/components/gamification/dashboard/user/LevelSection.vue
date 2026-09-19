<script setup lang="ts">
import { computed } from 'vue'

// Props pour recevoir l'XP totale depuis le parent (UserStats ou ProfileView)
const props = defineProps<{
  totalXp: number
}>()

// Logique de gamification : 1 niveau tous les 1000 XP
const level = computed(() => Math.floor(props.totalXp / 1000) + 1)
const xpInCurrentLevel = computed(() => props.totalXp % 1000)
const progressPercentage = computed(() => (xpInCurrentLevel.value / 1000) * 100)
const xpRemaining = computed(() => 1000 - xpInCurrentLevel.value)

// Déterminer un titre de rang basé sur le niveau
const rankTitle = computed(() => {
  if (level.value < 5) return 'Novice Développeur'
  if (level.value < 10) return 'Codeur Agile'
  if (level.value < 20) return 'Maître Microservices'
  return 'Légende GitDock'
})
</script>

<template>
  <div class="level-card bg-white rounded-[2.5rem] p-8 border border-slate-100 shadow-sm overflow-hidden relative">
    <div class="absolute -top-10 -right-10 w-40 h-40 bg-[#5b13ec]/5 rounded-full blur-3xl"></div>

    <div class="flex flex-col md:flex-row items-center gap-8 relative z-10">
      <div class="relative flex items-center justify-center">
        <v-progress-circular
            :model-value="progressPercentage"
            :size="120"
            :width="12"
            color="#5b13ec"
            rotate="360"
        >
          <div class="flex flex-col items-center">
            <span class="text-xs font-black uppercase text-slate-400 leading-none">Niveau</span>
            <span class="text-4xl font-black text-slate-900">{{ level }}</span>
          </div>
        </v-progress-circular>
      </div>

      <div class="flex-1 w-full">
        <div class="flex justify-between items-end mb-4">
          <div>
            <h3 class="text-2xl font-black text-slate-900">{{ rankTitle }}</h3>
            <p class="text-slate-500 font-medium">Continue comme ça pour atteindre le niveau {{ level + 1 }} !</p>
          </div>
          <div class="text-right">
            <span class="text-sm font-bold text-[#5b13ec]">{{ xpInCurrentLevel }} / 1000 XP</span>
          </div>
        </div>

        <div class="relative h-4 w-full bg-slate-100 rounded-full overflow-hidden">
          <div
              class="absolute top-0 left-0 h-full bg-[#5b13ec] transition-all duration-1000 ease-out"
              :style="{ width: progressPercentage + '%' }"
          >
            <div class="w-full h-full opacity-30 bg-[linear-gradient(45deg,rgba(255,255,255,.2)_25%,transparent_25%,transparent_50%,rgba(255,255,255,.2)_50%,rgba(255,255,255,.2)_75%,transparent_75%,transparent)] bg-[length:20px_20px] animate-shimmer"></div>
          </div>
        </div>

        <div class="mt-4 flex items-center gap-2 text-sm">
          <v-icon icon="mdi-information-outline" color="slate-300" size="18"></v-icon>
          <span class="text-slate-400 font-medium">
            Il te manque <strong class="text-slate-700">{{ xpRemaining }} XP</strong> pour ton prochain niveau.
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.level-card {
  transition: transform 0.3s ease;
}
.level-card:hover {
  transform: translateY(-5px);
}

@keyframes shimmer {
  0% { background-position: 0 0; }
  100% { background-position: 40px 0; }
}

.animate-shimmer {
  animation: shimmer 2s linear infinite;
}
</style>