<template>
  <div class="p-6">

    <!-- LOADING -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-20 gap-4">
      <v-progress-circular indeterminate color="#5b13ec" size="40"></v-progress-circular>
      <span class="text-xs font-bold text-slate-400 animate-pulse">Chargement des badges...</span>
    </div>

    <template v-else>
      <!-- FILTRES PAR TYPE -->
      <div class="flex items-center gap-2 mb-6 flex-wrap">
        <button @click="filterType = 'all'"
                :class="['px-4 py-1.5 rounded-xl text-xs font-black transition-all',
                  filterType === 'all' ? 'bg-[#5b13ec] text-white shadow-md' : 'bg-slate-100 text-slate-500 hover:bg-slate-200']">
          Tous ({{ processedBadges.length }})
        </button>
        <button @click="filterType = 'owned'"
                :class="['px-4 py-1.5 rounded-xl text-xs font-black transition-all',
                  filterType === 'owned' ? 'bg-emerald-500 text-white shadow-md' : 'bg-slate-100 text-slate-500 hover:bg-slate-200']">
          Obtenus ({{ ownedBadgesCount }})
        </button>
        <button @click="filterType = 'locked'"
                :class="['px-4 py-1.5 rounded-xl text-xs font-black transition-all',
                  filterType === 'locked' ? 'bg-slate-600 text-white shadow-md' : 'bg-slate-100 text-slate-500 hover:bg-slate-200']">
          Verrouillés ({{ lockedBadgesCount }})
        </button>
        <button @click="filterType = 'auto'"
                :class="['px-4 py-1.5 rounded-xl text-xs font-black transition-all',
                  filterType === 'auto' ? 'bg-violet-500 text-white shadow-md' : 'bg-slate-100 text-slate-500 hover:bg-slate-200']">
          Auto
        </button>
        <button @click="filterType = 'manual'"
                :class="['px-4 py-1.5 rounded-xl text-xs font-black transition-all',
                  filterType === 'manual' ? 'bg-blue-500 text-white shadow-md' : 'bg-slate-100 text-slate-500 hover:bg-slate-200']">
          Manuel
        </button>
      </div>

      <!-- SECTION BADGES OBTENUS -->
      <div v-if="filteredOwned.length > 0 && (filterType === 'all' || filterType === 'owned')" class="mb-8">
        <div class="flex items-center gap-2 mb-4">
          <div class="w-2 h-2 rounded-full bg-emerald-400"></div>
          <h3 class="text-sm font-black text-slate-700 uppercase tracking-wide">Badges obtenus</h3>
          <span class="text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-100 px-2 py-0.5 rounded-full">
            {{ filteredOwned.length }}
          </span>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          <div v-for="badge in filteredOwned" :key="badge.id"
               class="group relative bg-white border-2 border-slate-100 rounded-2xl p-5 flex flex-col items-center text-center
                      shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300
                      hover:border-[#5b13ec]/30">

            <!-- BADGE ICON -->
            <div class="w-16 h-16 rounded-2xl flex items-center justify-center mb-3 shadow-sm"
                 :style="{ backgroundColor: badge.color + '15', border: '2px solid ' + badge.color + '30' }">
              <v-icon :icon="badge.icon" :color="badge.color" size="32"></v-icon>
            </div>

            <!-- BADGE TYPE -->
            <span class="text-[9px] font-black uppercase px-2 py-0.5 rounded-full mb-2"
                  :class="badge.badgeType === 'Auto' ? 'bg-violet-50 text-violet-600' : 'bg-blue-50 text-blue-600'">
              {{ badge.badgeType }}
            </span>

            <h4 class="text-xs font-black text-slate-800 leading-tight">{{ badge.title }}</h4>
            <p class="text-[10px] text-slate-400 mt-1 line-clamp-2">{{ badge.description }}</p>

            <!-- XP -->
            <div class="flex items-center gap-1 mt-2">
              <v-icon icon="mdi-lightning-bolt" size="12" color="#f59e0b"></v-icon>
              <span class="text-[11px] font-black text-amber-600">{{ badge.xp }} XP</span>
            </div>

            <!-- BADGE "Obtenu" -->
            <div class="absolute top-2 right-2 w-6 h-6 bg-emerald-500 rounded-full flex items-center justify-center shadow-sm">
              <v-icon icon="mdi-check" color="white" size="12"></v-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- SÉPARATEUR -->
      <div v-if="filterType === 'all' && filteredOwned.length > 0 && filteredLocked.length > 0"
           class="border-t border-slate-100 my-6"></div>

      <!-- SECTION BADGES VERROUILLÉS -->
      <div v-if="filteredLocked.length > 0 && (filterType === 'all' || filterType === 'locked')">
        <div class="flex items-center gap-2 mb-4">
          <div class="w-2 h-2 rounded-full bg-slate-300"></div>
          <h3 class="text-sm font-black text-slate-400 uppercase tracking-wide">Badges verrouillés</h3>
          <span class="text-[10px] font-bold bg-slate-50 text-slate-400 border border-slate-200 px-2 py-0.5 rounded-full">
            {{ filteredLocked.length }}
          </span>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          <div v-for="badge in filteredLocked" :key="badge.id"
               class="relative bg-slate-50 border-2 border-slate-100 rounded-2xl p-5 flex flex-col items-center text-center
                      opacity-50 grayscale cursor-not-allowed select-none">

            <!-- BADGE ICON grisé -->
            <div class="w-16 h-16 rounded-2xl flex items-center justify-center mb-3 bg-slate-200">
              <v-icon :icon="badge.icon" color="#94a3b8" size="32"></v-icon>
            </div>

            <!-- TYPE -->
            <span class="text-[9px] font-black uppercase px-2 py-0.5 rounded-full mb-2 bg-slate-100 text-slate-400">
              {{ badge.badgeType }}
            </span>

            <h4 class="text-xs font-black text-slate-500 leading-tight">{{ badge.title }}</h4>
            <p class="text-[10px] text-slate-300 mt-1 line-clamp-2">{{ badge.description }}</p>

            <div class="flex items-center gap-1 mt-2">
              <v-icon icon="mdi-lightning-bolt" size="12" color="#cbd5e1"></v-icon>
              <span class="text-[11px] font-black text-slate-300">{{ badge.xp }} XP</span>
            </div>

            <!-- CADENAS -->
            <div class="absolute top-2 right-2 w-6 h-6 bg-slate-400 rounded-full flex items-center justify-center shadow-sm">
              <v-icon icon="mdi-lock" color="white" size="12"></v-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- ÉTAT VIDE -->
      <div v-if="processedBadges.length === 0" class="flex flex-col items-center justify-center py-20 gap-3">
        <div class="w-20 h-20 bg-[#5b13ec]/10 rounded-3xl flex items-center justify-center">
          <v-icon icon="mdi-medal-outline" color="#5b13ec" size="36"></v-icon>
        </div>
        <p class="font-black text-slate-400">Aucun badge configuré pour le moment</p>
        <p class="text-sm text-slate-300">Les badges apparaîtront ici une fois ajoutés par votre manager.</p>
      </div>

    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  userProgress: any | null
  myBadges: any[]
  allBadges: any[]
  loading: boolean
}>()

const filterType = ref<'all' | 'owned' | 'locked' | 'auto' | 'manual'>('all')

// IDs des badges que l'user possède
const ownedBadgeIds = computed(() =>
    new Set(props.myBadges.map((b: any) => b.badgeId ?? b.id))
)

// Fusion : tous les badges avec owned = true/false
const processedBadges = computed(() =>
    props.allBadges.map((b: any) => ({
      ...b,
      badgeType: (b.type === 0 || b.type === 'Manual') ? 'Manual' : 'Auto',
      owned: ownedBadgeIds.value.has(b.id),
    }))
)

// Filtres
const filteredOwned  = computed(() => {
  const owned = processedBadges.value.filter(b => b.owned)
  if (filterType.value === 'auto')   return owned.filter(b => b.badgeType === 'Auto')
  if (filterType.value === 'manual') return owned.filter(b => b.badgeType === 'Manual')
  return owned
})

const filteredLocked = computed(() => {
  const locked = processedBadges.value.filter(b => !b.owned)
  if (filterType.value === 'auto')   return locked.filter(b => b.badgeType === 'Auto')
  if (filterType.value === 'manual') return locked.filter(b => b.badgeType === 'Manual')
  return locked
})

const ownedBadgesCount  = computed(() => processedBadges.value.filter(b => b.owned).length)
const lockedBadgesCount = computed(() => processedBadges.value.filter(b => !b.owned).length)

// XP progression
const progressPercent = computed(() => {
  if (!props.userProgress) return 0
  const { totalExperience, nextLevelXp, prevLevelXp = 0 } = props.userProgress
  const range   = nextLevelXp - prevLevelXp
  const current = totalExperience - prevLevelXp
  if (range <= 0) return 100
  return Math.min(Math.round((current / range) * 100), 100)
})

const xpToNext = computed(() => {
  if (!props.userProgress) return 0
  return Math.max(0, props.userProgress.nextLevelXp - props.userProgress.totalExperience)
})
</script>