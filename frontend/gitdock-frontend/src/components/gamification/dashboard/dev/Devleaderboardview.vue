<template>
  <div class="p-6">

    <!-- LOADING -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-20 gap-4">
      <v-progress-circular indeterminate color="#5b13ec" size="40"></v-progress-circular>
      <span class="text-xs font-bold text-slate-400 animate-pulse">Chargement du classement...</span>
    </div>

    <template v-else>

      <!-- TITRE -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="text-xl font-black text-slate-800 flex items-center gap-2">
            <v-icon icon="mdi-podium" color="#5b13ec" size="24"></v-icon>
            Classement de vos projets
          </h2>
          <p class="text-slate-400 text-xs mt-0.5">
            Devs qui travaillent avec vous — classés par XP
          </p>
        </div>
        <span class="text-[10px] font-black bg-[#5b13ec]/10 text-[#5b13ec] px-3 py-1.5 rounded-full uppercase tracking-widest">
          {{ totalParticipants }} participants
        </span>
      </div>

      <!-- PODIUM TOP 3 -->
      <div v-if="leaderboard.length >= 3" class="grid grid-cols-3 gap-4 mb-8">
        <!-- 2ème place -->
        <div class="flex flex-col items-center pt-6">
          <div class="relative mb-3">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-slate-300 to-slate-400 flex items-center justify-center text-white font-black text-xl shadow-lg"
                 :class="{ 'ring-4 ring-[#5b13ec] ring-offset-2': leaderboard[1].userId === currentUserId }">
              {{ getInitials(leaderboard[1].fullName) }}
            </div>
            <div class="absolute -top-2 -right-2 w-6 h-6 bg-slate-400 rounded-full flex items-center justify-center text-white font-black text-[10px] shadow">
              2
            </div>
          </div>
          <p class="text-xs font-black text-slate-700 text-center truncate w-full px-2">{{ leaderboard[1].fullName }}</p>
          <p class="text-[10px] text-[#5b13ec] font-black">{{ leaderboard[1].xp }} XP</p>
          <div class="flex gap-1 mt-1 flex-wrap justify-center">
            <v-tooltip v-for="badge in leaderboard[1].badges.slice(0, 3)" :key="badge" :text="badge" location="top">
              <template v-slot:activator="{ props }">
                <div v-bind="props" class="w-5 h-5 bg-slate-100 rounded-md flex items-center justify-center">
                  <v-icon icon="mdi-medal" size="12" color="#94a3b8"></v-icon>
                </div>
              </template>
            </v-tooltip>
          </div>
          <div class="w-full bg-slate-200 rounded-t-xl mt-3" style="height: 60px;"></div>
        </div>

        <!-- 1ère place -->
        <div class="flex flex-col items-center">
          <div class="relative mb-3">
            <div class="absolute -top-4 left-1/2 -translate-x-1/2">
              <v-icon icon="mdi-crown" color="#f59e0b" size="24"></v-icon>
            </div>
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white font-black text-2xl shadow-xl mt-2"
                 :class="{ 'ring-4 ring-[#5b13ec] ring-offset-2': leaderboard[0].userId === currentUserId }">
              {{ getInitials(leaderboard[0].fullName) }}
            </div>
            <div class="absolute -top-0 -right-2 w-6 h-6 bg-amber-400 rounded-full flex items-center justify-center text-white font-black text-[10px] shadow">
              1
            </div>
          </div>
          <p class="text-sm font-black text-slate-800 text-center">{{ leaderboard[0].fullName }}</p>
          <p class="text-xs text-amber-600 font-black">{{ leaderboard[0].xp }} XP</p>
          <div class="flex gap-1 mt-1 flex-wrap justify-center">
            <v-tooltip v-for="badge in leaderboard[0].badges.slice(0, 4)" :key="badge" :text="badge" location="top">
              <template v-slot:activator="{ props }">
                <div v-bind="props" class="w-5 h-5 bg-amber-50 rounded-md flex items-center justify-center">
                  <v-icon icon="mdi-medal" size="12" color="#f59e0b"></v-icon>
                </div>
              </template>
            </v-tooltip>
          </div>
          <div class="w-full bg-amber-300 rounded-t-xl mt-3" style="height: 90px;"></div>
        </div>

        <!-- 3ème place -->
        <div class="flex flex-col items-center pt-10">
          <div class="relative mb-3">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-orange-300 to-amber-500 flex items-center justify-center text-white font-black text-xl shadow-lg"
                 :class="{ 'ring-4 ring-[#5b13ec] ring-offset-2': leaderboard[2].userId === currentUserId }">
              {{ getInitials(leaderboard[2].fullName) }}
            </div>
            <div class="absolute -top-2 -right-2 w-6 h-6 bg-orange-400 rounded-full flex items-center justify-center text-white font-black text-[10px] shadow">
              3
            </div>
          </div>
          <p class="text-xs font-black text-slate-700 text-center truncate w-full px-2">{{ leaderboard[2].fullName }}</p>
          <p class="text-[10px] text-orange-500 font-black">{{ leaderboard[2].xp }} XP</p>
          <div class="flex gap-1 mt-1 flex-wrap justify-center">
            <v-tooltip v-for="badge in leaderboard[2].badges.slice(0, 3)" :key="badge" :text="badge" location="top">
              <template v-slot:activator="{ props }">
                <div v-bind="props" class="w-5 h-5 bg-orange-50 rounded-md flex items-center justify-center">
                  <v-icon icon="mdi-medal" size="12" color="#f97316"></v-icon>
                </div>
              </template>
            </v-tooltip>
          </div>
          <div class="w-full bg-orange-200 rounded-t-xl mt-3" style="height: 40px;"></div>
        </div>
      </div>

      <!-- LISTE COMPLÈTE -->
      <div class="space-y-2">
        <div v-for="(player, index) in leaderboard" :key="player.userId"
             :class="['flex items-center gap-4 p-4 rounded-2xl border-2 transition-all duration-200',
               player.userId === currentUserId
                 ? 'bg-[#5b13ec]/5 border-[#5b13ec]/30 shadow-sm'
                 : 'bg-white border-slate-100 hover:border-slate-200 hover:shadow-sm']">

          <!-- RANG -->
          <div :class="['w-9 h-9 rounded-xl flex items-center justify-center font-black text-sm flex-shrink-0',
            index === 0 ? 'bg-amber-400 text-white' :
            index === 1 ? 'bg-slate-300 text-white' :
            index === 2 ? 'bg-orange-300 text-white' :
            'bg-slate-100 text-slate-500']">
            {{ player.rank }}
          </div>

          <!-- AVATAR -->
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-[#5b13ec] to-indigo-400
                      flex items-center justify-center text-white font-black text-sm flex-shrink-0 shadow-sm">
            {{ getInitials(player.fullName) }}
          </div>

          <!-- NOM + LEVEL -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <p class="font-black text-slate-800 text-sm truncate">{{ player.fullName }}</p>
              <span v-if="player.userId === currentUserId"
                    class="text-[9px] font-black bg-[#5b13ec] text-white px-2 py-0.5 rounded-full uppercase">
                Vous
              </span>
            </div>
            <p class="text-[10px] text-slate-400 font-bold">{{ player.levelLabel }}</p>
          </div>

          <!-- BADGES (compétences) -->
          <div class="hidden sm:flex items-center gap-1 flex-wrap max-w-[180px] justify-end">
            <v-tooltip v-for="badge in player.badges.slice(0, 4)" :key="badge" :text="badge" location="top">
              <template v-slot:activator="{ props }">
                <div v-bind="props"
                     class="w-7 h-7 bg-indigo-50 rounded-lg flex items-center justify-center border border-indigo-100
                            hover:-translate-y-0.5 transition-transform">
                  <v-icon icon="mdi-medal-outline" size="14" color="#6366f1"></v-icon>
                </div>
              </template>
            </v-tooltip>
            <span v-if="player.badges.length > 4"
                  class="text-[10px] font-black text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded-lg">
              +{{ player.badges.length - 4 }}
            </span>
            <span v-if="!player.badges.length" class="text-[10px] text-slate-300 italic">Aucun badge</span>
          </div>

          <!-- TOP SKILLS -->
          <div class="hidden md:flex items-center gap-1 flex-wrap max-w-[160px] justify-end">
            <span v-for="skill in player.topSkills" :key="skill.tagName"
                  class="text-[9px] font-black bg-violet-50 text-violet-600 border border-violet-100 px-2 py-0.5 rounded-full">
              {{ skill.tagName }} ×{{ skill.count }}
            </span>
          </div>

          <!-- XP -->
          <div class="text-right flex-shrink-0 min-w-[70px]">
            <div class="flex items-center gap-1 justify-end">
              <v-icon icon="mdi-lightning-bolt" size="14" color="#f59e0b"></v-icon>
              <span class="font-black text-amber-600 text-sm">{{ player.xp }}</span>
            </div>
            <p class="text-[10px] text-slate-300 font-bold">XP</p>
          </div>
        </div>
      </div>

      <!-- AUCUN RÉSULTAT -->
      <div v-if="!leaderboard.length" class="flex flex-col items-center justify-center py-20 gap-3">
        <v-icon icon="mdi-account-group-outline" size="56" class="text-slate-200"></v-icon>
        <p class="font-black text-slate-300">Aucun collaborateur trouvé dans vos projets</p>
      </div>

    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

const props = defineProps<{
  currentUserId: number | null | undefined
}>()

const loading     = ref(true)
const leaderboard = ref<any[]>([])

const totalParticipants = computed(() => leaderboard.value.length)

const getInitials = (name: string) =>
    name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)

onMounted(async () => {
  loading.value = true
  try {
    // Appel au endpoint leaderboard (filtre par projets de l'user dans le backend)
    // Le backend renvoie les contributeurs des projets de l'utilisateur connecté
    const response = await api.get('/Leaderboard', {
      params: { count: 50 } // on prend tous les devs du projet
    })
    leaderboard.value = response.data
  } catch (err) {
    console.error('[DevLeaderboardView] Erreur:', err)
  } finally {
    loading.value = false
  }
})
</script>