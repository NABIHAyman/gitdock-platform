<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { contributorService, type Contributor, type ContributorsByProject, type Badge } from '@/services/ContributorService'

// --- ÉTATS ---
const projectGroups = ref<ContributorsByProject[]>([])
const allBadges = ref<Badge[]>([])
const loading = ref(true)
const searchQuery = ref('')

// --- BADGES MANUELS UNIQUEMENT ---
const manualBadgesOnly = computed(() =>
    allBadges.value.filter(b => b.type === 'Manual' || b.type === 0)
)

// --- FILTRAGE PAR RECHERCHE ---
const filteredGroups = computed(() => {
  if (!searchQuery.value.trim()) return projectGroups.value
  return projectGroups.value.filter(p =>
      p.projectName.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// --- MODAL ---
const showModal = ref(false)
const selectedContributor = ref<Contributor | null>(null)
const selectedBadgeId = ref<string | null>(null)
const awarding = ref(false)

// --- SNACKBAR ---
const snackbar = ref(false)
const snackMessage = ref('')
const snackColor = ref('success')

const showNotify = (message: string, color = 'success') => {
  snackMessage.value = message
  snackColor.value = color
  snackbar.value = true
}

// --- CHARGEMENT ---
const fetchData = async () => {
  loading.value = true
  try {
    const [groups, badges] = await Promise.all([
      contributorService.getByProject(),
      contributorService.getAllBadges()
    ])
    projectGroups.value = groups
    allBadges.value = badges
  } catch (error) {
    console.error('[ContributorSection] Erreur:', error)
    showNotify('Erreur de chargement.', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// --- MODAL ACTIONS ---
const openAwardModal = (contributor: Contributor) => {
  selectedContributor.value = contributor
  selectedBadgeId.value = null
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedContributor.value = null
  selectedBadgeId.value = null
}

const awardBadge = async () => {
  if (!selectedContributor.value || !selectedBadgeId.value) return
  awarding.value = true
  try {
    await contributorService.awardBadge(selectedContributor.value.userId, selectedBadgeId.value)
    showNotify('Badge attribué avec succès !')
    closeModal()
    setTimeout(fetchData, 500)
  } catch {
    showNotify("Impossible d'attribuer le badge.", 'error')
  } finally {
    awarding.value = false
  }
}

// --- HELPERS ---
const getInitials = (fullName: string) =>
    fullName.split(' ').map(n => n.charAt(0)).join('').toUpperCase().slice(0, 2)

const totalContributors = computed(() =>
    projectGroups.value.reduce((acc, p) => acc + p.contributors.length, 0)
)
</script>

<template>
  <div class="flex flex-col h-full bg-slate-50/30">

    <!-- HEADER -->
    <header class="h-16 border-b border-slate-200 flex items-center justify-between px-6 bg-white/50 backdrop-blur-md sticky top-0 z-10">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center">
          <v-icon icon="mdi-account-group" color="#5b13ec" size="18"></v-icon>
        </div>
        <h2 class="text-lg font-bold text-slate-800">Équipe & Mérites</h2>
      </div>
      <span class="text-[10px] font-black text-[#5b13ec] bg-[#5b13ec]/10 px-3 py-1 rounded-full uppercase tracking-widest">
        {{ totalContributors }} Membres
      </span>
    </header>

    <!-- CONTENU -->
    <div class="flex-1 overflow-y-auto p-6 pb-20 custom-scroll">

      <!-- LOADING -->
      <div v-if="loading" class="flex flex-col items-center justify-center p-20 gap-4">
        <v-progress-circular indeterminate color="#5b13ec" size="40"></v-progress-circular>
        <span class="text-xs font-bold text-slate-400 animate-pulse">Synchronisation...</span>
      </div>

      <template v-else>

        <!-- BARRE DE RECHERCHE -->
        <div class="max-w-5xl mx-auto mb-6">
          <div class="relative">
            <v-icon icon="mdi-magnify" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size="20"></v-icon>
            <input
                v-model="searchQuery"
                placeholder="Rechercher un projet..."
                class="w-full pl-11 pr-4 py-3 bg-white border border-slate-200 rounded-2xl text-sm focus:outline-none focus:border-[#5b13ec]/50 shadow-sm transition-all"
            />
          </div>
        </div>

        <!-- LISTE DES PROJETS -->
        <div class="flex flex-col gap-6 max-w-5xl mx-auto">

          <!-- PROJET CARD -->
          <div v-for="project in filteredGroups" :key="project.projectId"
               class="bg-white border border-slate-200 rounded-[2rem] overflow-hidden shadow-sm hover:shadow-md transition-shadow">

            <!-- HEADER PROJET -->
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-[#5b13ec]/10 rounded-xl flex items-center justify-center">
                  <v-icon icon="mdi-folder-outline" color="#5b13ec" size="20"></v-icon>
                </div>
                <div>
                  <h3 class="font-black text-slate-800">{{ project.projectName }}</h3>
                  <p class="text-[10px] text-slate-400 font-medium">{{ project.contributors.length }} développeur(s)</p>
                </div>
              </div>
              <span class="text-[10px] font-black text-[#5b13ec] bg-[#5b13ec]/10 px-3 py-1 rounded-full">
                {{ project.contributors.length }} DEV
              </span>
            </div>

            <!-- CONTRIBUTEURS DU PROJET -->
            <div class="divide-y divide-slate-50">

              <div v-for="contributor in project.contributors" :key="contributor.userId"
                   class="flex items-center justify-between gap-4 px-6 py-4 hover:bg-slate-50/50 transition-colors">

                <!-- AVATAR + INFO -->
                <div class="flex items-center gap-4">
                  <div class="relative flex-shrink-0">
                    <div v-if="contributor.avatarUrl" class="w-12 h-12 rounded-2xl overflow-hidden border-2 border-white shadow-md">
                      <img :src="contributor.avatarUrl" class="w-full h-full object-cover" />
                    </div>
                    <div v-else class="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#5b13ec] to-indigo-400 flex items-center justify-center text-white font-black text-lg shadow-md">
                      {{ getInitials(contributor.fullName) }}
                    </div>
                    <div class="absolute -bottom-1 -right-1 bg-white border border-slate-100 text-[9px] font-black px-1.5 py-0.5 rounded-md shadow-sm text-[#5b13ec]">
                      Lv{{ contributor.currentLevel }}
                    </div>
                  </div>

                  <div>
                    <h4 class="font-black text-slate-800 text-sm">{{ contributor.fullName }}</h4>
                    <div class="flex items-center gap-2 mt-0.5">
                      <span class="text-[10px] text-slate-400 font-bold uppercase tracking-tight">{{ contributor.levelName }}</span>
                      <span class="text-slate-200">•</span>
                      <v-icon icon="mdi-lightning-bolt" size="11" class="text-amber-500"></v-icon>
                      <span class="text-[11px] font-black text-amber-600">{{ contributor.totalExperience }} XP</span>
                    </div>
                  </div>
                </div>

                <!-- BADGES + BOUTON RÉCOMPENSER -->
                <div class="flex items-center gap-4">

                  <!-- BADGES OBTENUS -->
                  <div class="flex -space-x-1">
                    <v-tooltip
                        v-for="badge in contributor.badges.slice(0, 4)"
                        :key="badge.badgeId"
                        :text="badge.title"
                        location="top">
                      <template v-slot:activator="{ props }">
                        <div v-bind="props"
                             class="w-8 h-8 rounded-xl flex items-center justify-center border-2 border-white shadow-sm hover:-translate-y-1 transition-transform"
                             :style="{ backgroundColor: badge.color + '20' }">
                          <v-icon :icon="badge.icon" :color="badge.color" size="16"></v-icon>
                        </div>
                      </template>
                    </v-tooltip>

                    <!-- +N si plus de 4 badges -->
                    <div v-if="contributor.badges.length > 4"
                         class="w-8 h-8 rounded-xl bg-slate-100 border-2 border-white flex items-center justify-center text-[10px] font-black text-slate-500">
                      +{{ contributor.badges.length - 4 }}
                    </div>

                    <!-- Aucun badge -->
                    <span v-if="!contributor.badges.length" class="text-[10px] text-slate-300 font-bold italic">
                      Aucun badge
                    </span>
                  </div>

                  <!-- BOUTON -->
                  <v-btn
                      @click="openAwardModal(contributor)"
                      variant="tonal"
                      color="#5b13ec"
                      class="rounded-xl font-black text-[11px] h-9 px-3"
                      elevation="0">
                    <v-icon start icon="mdi-trophy" size="14"></v-icon>
                    Récompenser
                  </v-btn>
                </div>
              </div>

              <!-- AUCUN DEV -->
              <div v-if="!project.contributors.length" class="px-6 py-8 text-center">
                <v-icon icon="mdi-account-off" size="32" class="text-slate-200 mb-2"></v-icon>
                <p class="text-sm text-slate-300 font-bold">Aucun développeur dans ce projet</p>
              </div>
            </div>
          </div>

          <!-- AUCUN PROJET TROUVÉ -->
          <div v-if="!filteredGroups.length" class="flex flex-col items-center justify-center py-20 gap-3">
            <v-icon icon="mdi-folder-search-outline" size="56" class="text-slate-200"></v-icon>
            <p class="font-bold text-slate-300">Aucun projet trouvé</p>
            <p v-if="searchQuery" class="text-xs text-slate-300">
              Aucun résultat pour "<span class="text-[#5b13ec]">{{ searchQuery }}</span>"
            </p>
          </div>

        </div>
      </template>
    </div>

    <!-- MODAL BADGES MANUELS -->
    <v-dialog v-model="showModal" max-width="560" transition="dialog-bottom-transition">
      <div class="bg-white rounded-[2.5rem] p-8 shadow-2xl relative overflow-hidden">

        <div class="flex justify-between items-start mb-6">
          <div>
            <h3 class="text-xl font-black text-slate-900">Attribuer une distinction</h3>
            <p class="text-sm text-slate-400">
              Badges manuels pour
              <span class="text-[#5b13ec] font-bold">{{ selectedContributor?.fullName }}</span>
            </p>
          </div>
          <v-btn icon="mdi-close" variant="text" @click="closeModal" rounded="xl" size="small"></v-btn>
        </div>

        <div class="grid grid-cols-3 gap-4 max-h-[380px] overflow-y-auto pr-2 mb-8 custom-scroll">
          <button v-for="badge in manualBadgesOnly" :key="badge.id"
                  @click="selectedBadgeId = badge.id"
                  :class="[
                    'relative p-4 border-2 rounded-[2rem] flex flex-col items-center justify-center transition-all duration-300',
                    selectedBadgeId === badge.id
                      ? 'border-[#5b13ec] bg-[#5b13ec]/5 shadow-inner'
                      : 'border-slate-50 bg-slate-50 hover:border-slate-200 hover:bg-white hover:shadow-md'
                  ]">
            <div v-if="selectedBadgeId === badge.id" class="absolute top-3 right-3">
              <v-icon icon="mdi-check-circle" color="#5b13ec" size="18"></v-icon>
            </div>
            <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-3"
                 :style="{ backgroundColor: badge.color + '20' }">
              <v-icon :icon="badge.icon" :color="badge.color" size="30"></v-icon>
            </div>
            <span class="text-[10px] font-black text-center uppercase tracking-tighter text-slate-700 leading-tight">
              {{ badge.title }}
            </span>
            <span class="text-[9px] text-amber-600 font-bold mt-1">+{{ badge.xp }} XP</span>
          </button>
        </div>

        <div class="flex gap-3">
          <v-btn @click="closeModal" variant="text" class="rounded-2xl font-bold text-slate-400 flex-1" height="50">
            Annuler
          </v-btn>
          <v-btn @click="awardBadge"
                 :disabled="!selectedBadgeId"
                 :loading="awarding"
                 color="#5b13ec"
                 class="rounded-2xl font-black text-white flex-[2]"
                 height="50"
                 elevation="0">
            Confirmer le mérite
          </v-btn>
        </div>
      </div>
    </v-dialog>

    <!-- SNACKBAR -->
    <v-snackbar v-model="snackbar" :color="snackColor" :timeout="3000" location="top" rounded="xl">
      <div class="flex items-center gap-3">
        <v-icon :icon="snackColor === 'success' ? 'mdi-check-circle' : 'mdi-alert'"></v-icon>
        <span class="font-bold">{{ snackMessage }}</span>
      </div>
    </v-snackbar>

  </div>
</template>

<style scoped>
.custom-scroll::-webkit-scrollbar { width: 5px; }
.custom-scroll::-webkit-scrollbar-track { background: transparent; }
.custom-scroll::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
.v-btn { text-transform: none !important; letter-spacing: 0; }
</style>