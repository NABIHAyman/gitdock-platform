<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { contributorService, type Contributor, type Badge } from '@/services/ContributorService'

// --- ÉTATS ---
const contributors = ref<Contributor[]>([])
const allBadges = ref<Badge[]>([]) // Stocke tous les badges (Auto + Manual)
const loading = ref(true)

// --- FILTRAGE : UNIQUEMENT LES BADGES MANUELS POUR LA RÉCOMPENSE ---
const manualBadgesOnly = computed(() => {
  // Filtre pour ne garder que ceux marqués comme 'Manual' (ou 0 selon ton enum)
  return allBadges.value.filter(b => b.type === 'Manual' || b.type === 0)
})

// --- ÉTATS MODAL ---
const showModal = ref(false)
const selectedContributor = ref<Contributor | null>(null)
const selectedBadgeId = ref<string | null>(null)
const awarding = ref(false)

// --- NOTIFICATIONS (SNACKBAR) ---
const snackbar = ref(false)
const snackMessage = ref('')
const snackColor = ref('success')

const showNotify = (message: string, color: string = 'success') => {
  snackMessage.value = message
  snackColor.value = color
  snackbar.value = true
}

// --- CHARGEMENT DES DONNÉES ---
const fetchData = async () => {
  loading.value = true
  try {
    const [contributorsData, badgesData] = await Promise.all([
      contributorService.getAll(),
      contributorService.getAllBadges()
    ])
    contributors.value = contributorsData
    allBadges.value = badgesData
  } catch (error) {
    console.error('[ContributorSection] Erreur:', error)
    showNotify('Erreur de chargement des données.', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// --- ACTIONS MODAL ---
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
    await contributorService.awardBadge(
        selectedContributor.value.userId,
        selectedBadgeId.value
    )

    showNotify(`Badge attribué !`)
    closeModal()

    // Attendre un tout petit peu que le message RabbitMQ / DB soit traité côté back
    setTimeout(async () => {
      await fetchData()
    }, 500)

  } catch (error) {
    showNotify("Impossible d'attribuer le badge.", 'error')
  } finally {
    awarding.value = false
  }
}

// --- HELPERS ---
const getInitials = (fullName: string) => {
  return fullName.split(' ').map(n => n.charAt(0)).join('').toUpperCase().slice(0, 2)
}
</script>

<template>
  <div class="flex flex-col h-full bg-slate-50/30">

    <!-- HEADER INTERNE -->
    <header class="h-16 border-b border-slate-200 flex items-center justify-between px-6 bg-white/50 backdrop-blur-md sticky top-0 z-10">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center">
          <v-icon icon="mdi-account-group" color="#5b13ec" size="18"></v-icon>
        </div>
        <h2 class="text-lg font-bold text-slate-800">Équipe & Mérites</h2>
      </div>
      <span class="text-[10px] font-black text-[#5b13ec] bg-[#5b13ec]/10 px-3 py-1 rounded-full uppercase tracking-widest">
        {{ contributors.length }} Membres
      </span>
    </header>

    <!-- ZONE DE CONTENU -->
    <div class="flex-1 overflow-y-auto p-6 pb-20 custom-scroll">

      <div v-if="loading" class="flex flex-col items-center justify-center p-20 gap-4">
        <v-progress-circular indeterminate color="#5b13ec" size="40"></v-progress-circular>
        <span class="text-xs font-bold text-slate-400 animate-pulse">Synchronisation...</span>
      </div>

      <!-- GRILLE DES CONTRIBUTEURS (DESIGN AVANT) -->
      <div v-else class="grid grid-cols-1 gap-4 max-w-5xl mx-auto">
        <div v-for="contributor in contributors" :key="contributor.userId"
             class="group bg-white border border-slate-200 rounded-[2rem] p-5 shadow-sm hover:shadow-xl hover:border-[#5b13ec]/30 transition-all duration-300">

          <div class="flex items-center justify-between gap-4">
            <!-- INFO UTILISATEUR -->
            <div class="flex items-center gap-4">
              <div class="relative">
                <div v-if="contributor.avatarUrl" class="w-14 h-14 rounded-2xl overflow-hidden border-2 border-white shadow-md">
                  <img :src="contributor.avatarUrl" class="w-full h-full object-cover" />
                </div>
                <div v-else class="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#5b13ec] to-indigo-400 flex items-center justify-center text-white font-black text-xl shadow-lg">
                  {{ getInitials(contributor.fullName) }}
                </div>
                <div class="absolute -bottom-1 -right-1 bg-white border border-slate-100 text-[9px] font-black px-1.5 py-0.5 rounded-md shadow-sm text-[#5b13ec]">
                  Lv{{ contributor.currentLevel }}
                </div>
              </div>

              <div>
                <h3 class="font-black text-slate-800">{{ contributor.fullName }}</h3>
                <div class="flex items-center gap-2 mt-1">
                  <span class="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">{{ contributor.levelName }}</span>
                  <div class="flex items-center gap-0.5">
                    <v-icon icon="mdi-lightning-bolt" size="12" class="text-amber-500"></v-icon>
                    <span class="text-[11px] font-black text-amber-600">{{ contributor.totalExperience }} XP</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- BADGES OBTENUS (AUTO + MANUAL DÉJÀ DÉBLOCUÉS) -->
            <div class="flex items-center gap-4">
              <div class="flex -space-x-2 overflow-hidden">
                <v-tooltip v-for="badge in contributor.badges.slice(0, 5)" :key="badge.badgeId" :text="badge.title" location="top">
                  <template v-slot:activator="{ props }">
                    <div v-bind="props"
                         class="w-10 h-10 rounded-xl bg-white border-2 border-white shadow-sm flex items-center justify-center transition-transform hover:-translate-y-1 hover:z-10"
                         :style="{ backgroundColor: badge.color + '15' }">
                      <v-icon :icon="badge.icon" :color="badge.color" size="20"></v-icon>
                    </div>
                  </template>
                </v-tooltip>
              </div>

              <!-- BOUTON RÉCOMPENSER DESIGN ORIGINAL -->
              <v-btn @click="openAwardModal(contributor)"
                     variant="tonal"
                     color="#5b13ec"
                     class="rounded-xl font-black text-[11px] h-10 px-4"
                     elevation="0">
                <v-icon start icon="mdi-trophy" size="16"></v-icon>
                Récompenser
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL : SEULEMENT BADGES MANUELS -->
    <v-dialog v-model="showModal" max-width="560" transition="dialog-bottom-transition">
      <div class="bg-white rounded-[2.5rem] p-8 shadow-2xl relative overflow-hidden">

        <div class="flex justify-between items-start mb-6">
          <div>
            <h3 class="text-xl font-black text-slate-900">Attribuer une distinction</h3>
            <p class="text-sm text-slate-400">Badges manuels pour <span class="text-[#5b13ec] font-bold">{{ selectedContributor?.fullName }}</span></p>
          </div>
          <v-btn icon="mdi-close" variant="text" @click="closeModal" rounded="xl" size="small" color="slate-300"></v-btn>
        </div>

        <!-- GRILLE DES BADGES (3 COLONNES - FILTRÉES SUR MANUEL) -->
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

            <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-3 transition-transform duration-500"
                 :class="selectedBadgeId === badge.id ? 'scale-110' : ''"
                 :style="{ backgroundColor: badge.color + '20' }">
              <v-icon :icon="badge.icon" :color="badge.color" size="30"></v-icon>
            </div>

            <span class="text-[10px] font-black text-center uppercase tracking-tighter text-slate-700 leading-tight">
              {{ badge.title }}
            </span>
            <span class="text-[9px] text-amber-600 font-bold mt-1">+{{ badge.xp }} XP</span>
          </button>
        </div>

        <!-- ACTIONS FOOTER -->
        <div class="flex gap-3 mt-4">
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

    <!-- NOTIFICATIONS -->
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