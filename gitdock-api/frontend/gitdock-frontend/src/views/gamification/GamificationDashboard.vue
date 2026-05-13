<template>
  <AppLayout>
    <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50/30">

      <!-- HEADER PREMIUM -->
      <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-3xl mx-0 mb-8 p-8 shadow-2xl">
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-0 right-0 w-96 h-96 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
          <div class="absolute bottom-0 left-0 w-64 h-64 bg-white rounded-full translate-y-1/2 -translate-x-1/2"></div>
        </div>

        <div class="relative flex items-center justify-between">
          <div>
            <div class="flex items-center gap-3 mb-2">
              <div class="w-10 h-10 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
                <v-icon icon="mdi-trophy-variant" color="white" size="22"></v-icon>
              </div>
              <span class="text-white/70 text-sm font-bold uppercase tracking-widest">Gamification</span>
            </div>
            <h1 class="text-3xl font-black text-white mb-1">Configuration & Mérites</h1>
            <p class="text-white/60 text-sm">Gérez les règles et récompensez vos développeurs.</p>
          </div>

          <!-- STATS RAPIDES -->
          <div class="hidden md:flex gap-4">
            <div class="bg-white/10 backdrop-blur-sm rounded-2xl px-5 py-3 text-center border border-white/20">
              <p class="text-2xl font-black text-white">{{ stats.badges }}</p>
              <p class="text-white/60 text-xs font-bold uppercase">Badges</p>
            </div>
            <div class="bg-white/10 backdrop-blur-sm rounded-2xl px-5 py-3 text-center border border-white/20">
              <p class="text-2xl font-black text-white">{{ stats.levels }}</p>
              <p class="text-white/60 text-xs font-bold uppercase">Niveaux</p>
            </div>
            <div class="bg-white/10 backdrop-blur-sm rounded-2xl px-5 py-3 text-center border border-white/20">
              <p class="text-2xl font-black text-white">{{ stats.contributors }}</p>
              <p class="text-white/60 text-xs font-bold uppercase">Devs</p>
            </div>
          </div>
        </div>

        <!-- TABS -->
        <div class="relative flex gap-2 mt-8 bg-white/10 backdrop-blur-sm p-1.5 rounded-2xl w-fit border border-white/20">
          <button v-for="tab in tabs" :key="tab.key"
                  @click="activeTab = tab.key"
                  :class="[
                    'flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all duration-300',
                    activeTab === tab.key
                      ? 'bg-white text-[#5b13ec] shadow-lg'
                      : 'text-white/70 hover:text-white hover:bg-white/10'
                  ]">
            <v-icon :icon="tab.icon" size="16"></v-icon>
            {{ tab.label }}
          </button>
        </div>
      </div>

      <!-- CONTENU MANAGER -->
      <div v-if="isManager">
        <transition name="slide-fade" mode="out-in">
          <div :key="activeTab">

            <!-- BADGES / TAGS / LEVELS / CONTRIBUTORS -->
            <div v-if="['badges','tags','levels','contributors'].includes(activeTab)"
                 class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
              <BadgeSection v-if="activeTab === 'badges'" />
              <TagSection v-else-if="activeTab === 'tags'" />
              <LevelSection v-else-if="activeTab === 'levels'" />
              <ContributorSection v-else-if="activeTab === 'contributors'" />
            </div>

            <!-- ONGLET AI -->
            <div v-else-if="activeTab === 'ai'" class="space-y-6">

              <!-- HERO AI -->
              <div class="bg-gradient-to-br from-slate-900 to-slate-800 rounded-3xl p-8 shadow-2xl relative overflow-hidden">
                <div class="absolute top-0 right-0 w-72 h-72 bg-[#5b13ec]/20 rounded-full -translate-y-1/2 translate-x-1/2 blur-3xl"></div>
                <div class="absolute bottom-0 left-0 w-48 h-48 bg-violet-500/20 rounded-full translate-y-1/2 -translate-x-1/2 blur-3xl"></div>

                <div class="relative flex items-start gap-6">
                  <div class="w-16 h-16 bg-[#5b13ec] rounded-2xl flex items-center justify-center shadow-lg flex-shrink-0">
                    <v-icon icon="mdi-brain" color="white" size="32"></v-icon>
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-2">
                      <h2 class="text-2xl font-black text-white">GitDock AI</h2>
                      <span class="bg-[#5b13ec]/30 text-[#a78bfa] text-[10px] font-black px-3 py-1 rounded-full border border-[#5b13ec]/40 uppercase tracking-widest">
                        Security Analysis
                      </span>
                    </div>
                    <p class="text-slate-400 text-sm leading-relaxed max-w-xl">
                      Analyse intelligente de vos dépôts Git. Détectez les anomalies, prédisez les risques et obtenez des insights sur vos commits en temps réel.
                    </p>
                  </div>
                </div>
              </div>

              <!-- RECHERCHE + ANALYSE -->
              <div class="bg-white rounded-3xl border border-slate-200 shadow-sm p-8">
                <h3 class="text-lg font-black text-slate-800 mb-6 flex items-center gap-2">
                  <v-icon icon="mdi-magnify-scan" color="#5b13ec" size="22"></v-icon>
                  Analyser un projet
                </h3>

                <div class="flex gap-3 mb-6">
                  <div class="flex-1 relative">
                    <v-icon icon="mdi-folder-search-outline" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size="20"></v-icon>
                    <input
                        v-model="aiProjectName"
                        @keyup.enter="analyzeProject"
                        placeholder="Nom du projet (ex: gitdock-auth)..."
                        class="w-full pl-11 pr-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm focus:outline-none focus:border-[#5b13ec]/50 focus:bg-white transition-all"
                    />
                  </div>
                  <button @click="analyzeProject"
                          :disabled="aiLoading || !aiProjectName.trim()"
                          class="px-8 py-4 bg-[#5b13ec] text-white rounded-2xl font-black text-sm hover:bg-[#4a0fd4] transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 shadow-lg shadow-[#5b13ec]/30">
                    <v-progress-circular v-if="aiLoading" indeterminate size="16" width="2" color="white"></v-progress-circular>
                    <v-icon v-else icon="mdi-lightning-bolt" size="18"></v-icon>
                    {{ aiLoading ? 'Analyse...' : 'Analyser' }}
                  </button>
                </div>

                <!-- ERREUR -->
                <div v-if="aiError" class="bg-red-50 border border-red-200 rounded-2xl p-4 mb-6 flex items-center gap-3">
                  <v-icon icon="mdi-alert-circle" color="red" size="20"></v-icon>
                  <p class="text-red-700 text-sm font-bold">{{ aiError }}</p>
                </div>

                <!-- RÉSULTATS -->
                <div v-if="aiResult" class="space-y-6">

                  <!-- SUMMARY CARDS -->
                  <div class="grid grid-cols-3 gap-4">
                    <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
                      <p class="text-3xl font-black text-slate-800">{{ aiResult.total }}</p>
                      <p class="text-xs text-slate-500 font-bold uppercase mt-1">Commits analysés</p>
                    </div>
                    <div :class="[
                      'rounded-2xl p-5 border',
                      aiResult.anomalies > 0
                        ? 'bg-red-50 border-red-100'
                        : 'bg-emerald-50 border-emerald-100'
                    ]">
                      <p :class="['text-3xl font-black', aiResult.anomalies > 0 ? 'text-red-600' : 'text-emerald-600']">
                        {{ aiResult.anomalies }}
                      </p>
                      <p class="text-xs text-slate-500 font-bold uppercase mt-1">Anomalies détectées</p>
                    </div>
                    <div :class="[
                      'rounded-2xl p-5 border',
                      aiResult.anomalies === 0
                        ? 'bg-emerald-50 border-emerald-100'
                        : 'bg-amber-50 border-amber-100'
                    ]">
                      <p :class="['text-3xl font-black', aiResult.anomalies === 0 ? 'text-emerald-600' : 'text-amber-600']">
                        {{ aiResult.anomalies === 0 ? '✓ Sain' : '⚠ Risque' }}
                      </p>
                      <p class="text-xs text-slate-500 font-bold uppercase mt-1">Statut global</p>
                    </div>
                  </div>

                  <!-- LISTE DES ANOMALIES -->
                  <div v-if="aiResult.details?.length">
                    <h4 class="font-black text-slate-700 mb-3 flex items-center gap-2">
                      <v-icon icon="mdi-alert-circle-outline" color="red" size="18"></v-icon>
                      Commits suspects
                    </h4>
                    <div class="space-y-3 max-h-80 overflow-y-auto custom-scroll">
                      <div v-for="anomaly in aiResult.details" :key="anomaly.sha"
                           class="bg-red-50 border border-red-100 rounded-2xl p-4 flex items-start gap-3">
                        <div class="w-8 h-8 bg-red-100 rounded-xl flex items-center justify-center flex-shrink-0 mt-0.5">
                          <v-icon icon="mdi-bug" color="red" size="16"></v-icon>
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="text-sm font-black text-red-800 truncate">{{ anomaly.message }}</p>
                          <p class="text-xs text-red-500 font-mono mt-0.5">{{ anomaly.sha?.slice(0, 8) }}</p>
                          <span class="inline-block bg-red-100 text-red-700 text-[10px] font-black px-2 py-0.5 rounded-full mt-1">
                            {{ anomaly.reason }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- TOUT EST SAIN -->
                  <div v-else class="text-center py-10 bg-emerald-50 rounded-2xl border border-emerald-100">
                    <v-icon icon="mdi-shield-check" color="emerald" size="48" class="mb-3"></v-icon>
                    <p class="font-black text-emerald-700">Aucune anomalie détectée !</p>
                    <p class="text-sm text-emerald-500 mt-1">Ce projet est sain.</p>
                  </div>
                </div>

                <!-- ÉTAT VIDE -->
                <div v-if="!aiResult && !aiLoading && !aiError" class="text-center py-16">
                  <div class="w-20 h-20 bg-[#5b13ec]/10 rounded-3xl flex items-center justify-center mx-auto mb-4">
                    <v-icon icon="mdi-brain" color="#5b13ec" size="36"></v-icon>
                  </div>
                  <p class="font-black text-slate-400">Entrez un nom de projet pour lancer l'analyse</p>
                  <p class="text-sm text-slate-300 mt-1">L'IA va scanner les commits et détecter les anomalies</p>
                </div>
              </div>
            </div>

          </div>
        </transition>
      </div>

      <!-- CONTENU USER (non manager) -->
      <div v-else class="space-y-6">
        <section v-if="userStats" class="bg-gradient-to-r from-[#5b13ec] to-indigo-500 rounded-3xl shadow-xl p-8 text-white">
          <div class="flex justify-between items-end mb-4">
            <div>
              <p class="text-sm opacity-70 uppercase font-black tracking-widest">Progression</p>
              <h2 class="text-3xl font-black">{{ userStats.levelName }}
                <span class="text-xl font-normal opacity-60">Lv{{ userStats.currentLevel }}</span>
              </h2>
            </div>
            <p class="text-2xl font-black">{{ userStats.currentXp }} XP</p>
          </div>
          <div class="w-full bg-white/20 rounded-full h-3">
            <div class="bg-white h-full rounded-full transition-all duration-1000"
                 :style="{ width: Math.min((userStats.currentXp / userStats.nextLevelXp) * 100, 100) + '%' }">
            </div>
          </div>
        </section>
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
import api from '@/services/api'

import BadgeSection from '@/components/gamification/dashboard/manager/BadgeSection.vue'
import TagSection from '@/components/gamification/dashboard/manager/TagSection.vue'
import LevelSection from '@/components/gamification/dashboard/manager/LevelSection.vue'
import ContributorSection from '@/components/gamification/dashboard/manager/ContributorSection.vue'

const authStore = useAuthStore()

const isManager = computed(() =>
    ['ROLE_MANAGER', 'ROLE_WORKSPACE_OWNER', 'ROLE_COMPANY_ADMIN'].includes(authStore.role || '')
)

// --- TABS ---
const tabs = [
  { key: 'badges',       label: 'Badges',        icon: 'mdi-medal' },
  { key: 'tags',         label: 'Tags',           icon: 'mdi-tag-multiple' },
  { key: 'levels',       label: 'Niveaux',        icon: 'mdi-chart-line' },
  { key: 'contributors', label: 'Contributeurs',  icon: 'mdi-account-group' },
  { key: 'ai',           label: 'GitDock AI',     icon: 'mdi-brain' },
]
const activeTab = ref('badges')

// --- STATS HEADER ---
const stats = ref({ badges: 0, levels: 0, contributors: 0 })

// --- USER (non manager) ---
const userStats = ref<any>(null)

// --- AI ---
const aiProjectName = ref('')
const aiLoading = ref(false)
const aiError = ref('')
const aiResult = ref<any>(null)

// const AI_API = 'http://localhost:8000' // ton FastAPI Python

const analyzeProject = async () => {
  if (!aiProjectName.value.trim()) return
  aiLoading.value = true
  aiError.value = ''
  aiResult.value = null

  try {
    const response = await api.get('/v1/analyze-project', {
      params: { project_name: aiProjectName.value.trim() }
    })
    aiResult.value = response.data
  } catch (err: any) {
    aiError.value = err.response?.data?.detail || 'Projet introuvable ou erreur d\'analyse.'
  } finally {
    aiLoading.value = false
  }
}

onMounted(async () => {
  if (!isManager.value) {
    try {
      const [p, b] = await Promise.all([
        userProgressService.getProgress(),
        userBadgeService.getMyBadges()
      ])
      userStats.value = p
    } catch (err) {
      console.error(err)
    }
  }
})
</script>

<style scoped>
.slide-fade-enter-active { transition: all 0.2s ease; }
.slide-fade-leave-active { transition: all 0.15s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateY(8px); }
.slide-fade-leave-to { opacity: 0; transform: translateY(-4px); }
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
.v-btn { text-transform: none !important; }
</style>