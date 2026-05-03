<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// --- API CONFIGURATION ---
const API_URL = "https://localhost:7261/api/badges"
const CONFIG_API_URL = "https://localhost:7261/api/xpconfig"

// --- TYPES & INTERFACES ---
interface Badge {
  id?: string;
  title: string;
  description: string;
  xp: number;
  icon: string;
  color: string;
  type: string;
}

// --- UI PRESETS ---
const badgeTypes = [
  { title: 'Bug Fix', value: 'BugFix' },
  { title: 'Contribution', value: 'Contribution' },
  { title: 'Technical', value: 'Technical' },
  { title: 'Community', value: 'Community' }
]

const iconPresets = [
  'mdi-trophy-outline', 'mdi-bug-outline', 'mdi-git', 'mdi-github',
  'mdi-code-braces', 'mdi-source-repository', 'mdi-account-group-outline',
  'mdi-star-outline', 'mdi-lightning-bolt', 'mdi-rocket-launch',
  'mdi-shield-check', 'mdi-book-open-variant'
]

const colorPresets = ['#5b13ec', '#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#ec4899']

// --- STATES (REFS) ---
const searchQuery = ref('')
const showForm = ref(false)
const isEditing = ref(false)
const currentBadgeId = ref<string | null>(null)
const badges = ref<Badge[]>([])

// XP Engine State
const configXP = ref({
  commitXp: 10,
  prXp: 50,
  bugFixXp: 100
})

// Badge Form State
const form = ref<Badge>({
  title: '',
  description: '',
  xp: 100,
  icon: 'mdi-trophy-outline',
  color: '#5b13ec',
  type: 'Contribution'
})

// --- NOTIFICATION SYSTEM ---
const snackbar = ref(false)
const snackMessage = ref('')
const snackColor = ref('success')

const showNotify = (message: string, color: string = 'success') => {
  snackMessage.value = message
  snackColor.value = color
  snackbar.value = true
}

// --- DELETE CONFIRMATION DIALOG STATE ---
const confirmDialog = ref(false)
const badgeToDeleteId = ref<string | null>(null)

// --- LOGIC: XP ENGINE ---
const fetchConfig = async () => {
  try {
    const response = await axios.get(CONFIG_API_URL)
    if (response.data) {
      configXP.value = {
        commitXp: response.data.commitXp,
        prXp: response.data.prXp,
        bugFixXp: response.data.bugFixXp
      }
    }
  } catch (error) {
    console.error("Error loading XP config", error)
  }
}

const saveConfig = async () => {
  try {
    await axios.put(CONFIG_API_URL, configXP.value)
    showNotify("System configuration synchronized. XP rules updated.")
  } catch (error) {
    showNotify("Failed to update system parameters.", "error")
  }
}

// --- LOGIC: BADGES (CRUD) ---
const fetchBadges = async () => {
  try {
    const response = await axios.get(API_URL)
    badges.value = response.data
  } catch (error) {
    console.error("Error loading badges", error)
  }
}

const saveBadge = async () => {
  if (!form.value.title) return

  try {
    const badgeData = { ...form.value, xp: Number(form.value.xp) }

    if (isEditing.value && currentBadgeId.value) {
      await axios.put(`${API_URL}/${currentBadgeId.value}`, badgeData)
      showNotify("Badge repository updated successfully.")
    } else {
      await axios.post(API_URL, badgeData)
      showNotify("New badge successfully deployed.")
    }

    await fetchBadges()
    closeForm()
  } catch (error) {
    showNotify("Error during data persistence.", "error")
  }
}

// Open custom dialog instead of native confirm()
const openConfirmDialog = (id: string) => {
  badgeToDeleteId.value = id
  confirmDialog.value = true
}

const confirmDeleteBadge = async () => {
  confirmDialog.value = false
  if (badgeToDeleteId.value) {
    try {
      await axios.delete(`${API_URL}/${badgeToDeleteId.value}`)
      badges.value = badges.value.filter(b => b.id !== badgeToDeleteId.value)
      showNotify("Badge successfully revoked from the system.")
    } catch (error) {
      showNotify("Error during entity deletion.", "error")
    } finally {
      badgeToDeleteId.value = null
    }
  }
}

// --- LIFECYCLE ---
onMounted(() => {
  fetchBadges()
  fetchConfig()
})

// --- NAVIGATION ---
const filteredBadges = computed(() => {
  return badges.value.filter(b =>
      b.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      b.description.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const openCreateForm = () => {
  isEditing.value = false
  form.value = { title: '', description: '', xp: 100, icon: 'mdi-trophy-outline', color: '#5b13ec', type: 'Contribution' }
  showForm.value = true
}

const openEditForm = (badge: Badge) => {
  isEditing.value = true
  currentBadgeId.value = badge.id!
  form.value = { ...badge }
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  isEditing.value = false
  currentBadgeId.value = null
}
</script>

<template>
  <div class="flex flex-col h-full overflow-hidden">

    <header v-if="!showForm" class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-8 shrink-0 z-10">
      <div class="max-w-xl w-full">
        <v-text-field v-model="searchQuery" prepend-inner-icon="mdi-magnify" placeholder="Search badges..." variant="solo" flat hide-details bg-color="#F1F5F9" rounded="xl"></v-text-field>
      </div>
      <v-btn @click="openCreateForm" color="#5b13ec" class="text-none rounded-xl font-bold px-5 text-white" elevation="0" height="44">
        <v-icon start icon="mdi-plus"></v-icon> Create New Badge
      </v-btn>
    </header>

    <div class="flex-1 overflow-y-auto relative">
      <div v-if="!showForm" class="p-8 pb-40">
        <div class="max-w-7xl mx-auto">
          <h2 class="text-2xl font-bold mb-6 tracking-tight text-slate-900">Achievement Management</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">

            <div v-for="badge in filteredBadges" :key="badge.id" class="bg-white border border-slate-200 rounded-3xl p-6 shadow-sm hover:shadow-xl transition-all group relative">
              <div class="absolute top-4 right-4 flex gap-1 z-10">
                <button @click.stop="openEditForm(badge)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 hover:text-[#5b13ec] flex items-center justify-center border border-slate-100">
                  <v-icon icon="mdi-pencil" size="16"></v-icon>
                </button>
                <button @click.stop="openConfirmDialog(badge.id!)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 hover:text-red-600 flex items-center justify-center border border-slate-100">
                  <v-icon icon="mdi-delete" size="16"></v-icon>
                </button>
              </div>

              <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-5" :style="{ backgroundColor: badge.color + '15' }">
                <v-icon :icon="badge.icon" size="30" :style="{ color: badge.color }"></v-icon>
              </div>

              <h3 class="text-lg font-bold mb-1 tracking-tight">{{ badge.title }}</h3>
              <div class="mb-3">
                <span class="text-[10px] font-black uppercase px-2 py-1 rounded-md border" :style="{ backgroundColor: badge.color + '15', color: badge.color, borderColor: badge.color + '30' }">
                  {{ badge.type }}
                </span>
              </div>
              <p class="text-sm text-slate-500 mb-6 line-clamp-2 leading-relaxed h-10">{{ badge.description }}</p>
              <div class="flex items-center gap-1.5 font-bold text-sm" :style="{ color: badge.color }">
                <v-icon icon="mdi-lightning-bolt" size="18"></v-icon> {{ badge.xp }} XP
              </div>
            </div>

            <button @click="openCreateForm" class="flex flex-col items-center justify-center border-2 border-dashed border-slate-200 rounded-3xl p-6 hover:border-[#5b13ec]/40 group transition-all min-h-[240px] bg-slate-50/30">
              <v-icon icon="mdi-plus" size="30" class="text-slate-300 group-hover:text-[#5b13ec] mb-3"></v-icon>
              <span class="text-sm font-bold text-slate-400 group-hover:text-[#5b13ec]">Add Achievement</span>
            </button>
          </div>
        </div>

        <footer class="fixed bottom-6 left-72 right-8 z-20">
          <div class="bg-white border border-slate-200 rounded-[2rem] p-6 flex items-center justify-between gap-6 shadow-2xl shadow-indigo-900/10">
            <div class="flex items-center gap-5">
              <div class="w-12 h-12 bg-[#5b13ec]/10 text-[#5b13ec] rounded-2xl flex items-center justify-center">
                <v-icon icon="mdi-lightning-bolt" size="28"></v-icon>
              </div>
              <div>
                <h4 class="text-lg font-bold leading-tight">XP Engine</h4>
                <p class="text-xs text-slate-500 font-medium italic">Global Git action rules</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div class="bg-slate-50 px-5 py-2.5 rounded-2xl flex items-center gap-3 border border-slate-100">
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Commit</span>
                <input v-model.number="configXP.commitXp" class="w-10 bg-transparent border-none p-0 text-md font-bold text-slate-900 focus:ring-0 outline-none text-center" type="number"/>
                <span class="text-[10px] font-bold text-[#5b13ec]">XP</span>
              </div>
              <div class="bg-slate-50 px-5 py-2.5 rounded-2xl flex items-center gap-3 border border-slate-100">
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">PR</span>
                <input v-model.number="configXP.prXp" class="w-10 bg-transparent border-none p-0 text-md font-bold text-slate-900 focus:ring-0 outline-none text-center" type="number"/>
                <span class="text-[10px] font-bold text-[#5b13ec]">XP</span>
              </div>
              <div class="bg-slate-50 px-5 py-2.5 rounded-2xl flex items-center gap-3 border border-slate-100">
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">BugFix</span>
                <input v-model.number="configXP.bugFixXp" class="w-10 bg-transparent border-none p-0 text-md font-bold text-slate-900 focus:ring-0 outline-none text-center" type="number"/>
                <span class="text-[10px] font-bold text-[#5b13ec]">XP</span>
              </div>
              <v-btn @click="saveConfig" color="#5b13ec" class="text-none rounded-2xl font-bold px-8 text-white" height="48" elevation="0">Save Config</v-btn>
            </div>
          </div>
        </footer>
      </div>

      <div v-else class="h-full flex items-center justify-center p-8 bg-slate-50/50 backdrop-blur-sm animate-fade-in">
        <div class="w-full max-w-2xl bg-white border border-slate-200 rounded-[3rem] p-12 shadow-2xl relative overflow-hidden">
          <div class="absolute top-0 left-0 w-3 h-full" :style="{ backgroundColor: form.color }"></div>
          <div class="flex justify-between items-start mb-10">
            <div>
              <h3 class="text-3xl font-black tracking-tight text-slate-900">{{ isEditing ? 'Edit Badge' : 'New Reward' }}</h3>
              <p class="text-slate-400 font-medium mt-1">Configure acquisition metrics</p>
            </div>
            <v-btn icon="mdi-close" variant="tonal" @click="closeForm" rounded="xl"></v-btn>
          </div>

          <div class="space-y-6">
            <div class="grid grid-cols-2 gap-6">
              <v-text-field v-model="form.title" label="Title" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-text-field>
              <v-select v-model="form.type" :items="badgeTypes" label="Category" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-select>
            </div>
            <v-textarea v-model="form.description" label="Description" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details rows="2"></v-textarea>

            <div>
              <p class="text-[11px] font-black uppercase text-slate-400 ml-1 mb-3 tracking-widest">Visual Identity</p>
              <div class="grid grid-cols-6 gap-3 bg-slate-50 p-4 rounded-2xl border border-slate-100">
                <button v-for="ico in iconPresets" :key="ico" @click="form.icon = ico"
                        :class="['w-10 h-10 flex items-center justify-center rounded-xl transition-all', form.icon === ico ? 'bg-white shadow-md scale-110 border border-slate-200' : 'text-slate-400 hover:bg-slate-100']">
                  <v-icon :icon="ico" size="20" :style="{ color: form.icon === ico ? form.color : '' }"></v-icon>
                </button>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-8">
              <v-text-field v-model.number="form.xp" type="number" label="XP Value" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-text-field>
              <div class="flex items-center gap-3 bg-slate-50 p-2 px-3 rounded-xl border border-slate-100 h-[56px]">
                <div class="flex gap-2 border-r pr-3">
                  <button v-for="c in colorPresets" :key="c" @click="form.color = c" :style="{ backgroundColor: c }" :class="['w-6 h-6 rounded-full border-2 transition-all', form.color === c ? 'border-slate-900 scale-125' : 'border-white']"></button>
                </div>
                <input type="color" v-model="form.color" class="w-8 h-8 cursor-pointer border-none bg-transparent">
              </div>
            </div>

            <div class="pt-8 flex gap-4">
              <v-btn @click="closeForm" variant="text" class="text-none rounded-2xl font-bold px-8 text-slate-400" height="64">Cancel</v-btn>
              <v-btn @click="saveBadge" :style="{ backgroundColor: form.color }" class="text-none rounded-2xl font-bold px-10 text-white flex-1 shadow-lg" height="64" elevation="0">
                {{ isEditing ? 'Update Resource' : 'Deploy Resource' }}
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <v-dialog v-model="confirmDialog" max-width="500">
      <div class="bg-white border border-slate-200 rounded-[2.5rem] p-10 shadow-2xl relative overflow-hidden">
        <div class="absolute top-0 left-0 w-3 h-full bg-red-500"></div>
        <div class="flex flex-col items-center text-center">
          <div class="w-20 h-20 bg-red-100 rounded-3xl flex items-center justify-center mb-8 border border-red-200 shadow-inner">
            <v-icon icon="mdi-alert-octagon-outline" size="40" class="text-red-600"></v-icon>
          </div>
          <h3 class="text-3xl font-black tracking-tight text-slate-900 mb-3">Permanent Revocation?</h3>
          <p class="text-slate-500 font-medium mb-12 max-w-sm leading-relaxed">Are you absolutely certain you want to revoke this badge? <br> This action cannot be undone.</p>
        </div>
        <div class="flex gap-4">
          <v-btn variant="text" @click="confirmDialog = false" class="text-none rounded-2xl font-bold px-8 text-slate-400" height="64">Cancel</v-btn>
          <v-btn @click="confirmDeleteBadge" color="#ef4444" class="text-none rounded-2xl font-bold px-10 text-white flex-1 shadow-lg" height="64" elevation="0">Revoke Badge</v-btn>
        </div>
      </div>
    </v-dialog>

    <v-snackbar v-model="snackbar" :color="snackColor" :timeout="3000" location="top" variant="flat" rounded="xl" elevation="12" class="mt-4">
      <div class="flex items-center justify-center gap-3 w-full">
        <v-icon :icon="snackColor === 'success' ? 'mdi-check-circle' : 'mdi-alert-circle'" color="white" size="22"></v-icon>
        <span class="font-bold text-white">{{ snackMessage }}</span>
      </div>
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar = false" icon="mdi-close" color="white" size="small"></v-btn>
      </template>
    </v-snackbar>

  </div>
</template>

<style scoped>
:deep(.v-field) { border: 1px solid #f1f5f9 !important; }
::-webkit-scrollbar { width: 0px; }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>