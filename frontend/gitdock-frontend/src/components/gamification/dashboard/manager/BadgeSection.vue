<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { badgeService } from '@/services/BadgeService'
import { xpConfigService } from '@/services/XpConfigService'

// --- TYPES ---
interface Badge {
  id?: string;
  title: string;
  description: string;
  xp: number;
  icon: string;
  color: string;
  type: string;
}

const iconPresets = [
  'mdi-trophy-outline', 'mdi-bug-outline', 'mdi-git', 'mdi-github', 'mdi-code-braces',
  'mdi-star-outline', 'mdi-lightning-bolt', 'mdi-rocket-launch', 'mdi-medal-outline', 'mdi-xml',
  'mdi-database', 'mdi-api', 'mdi-server', 'mdi-incognito', 'mdi-fire',
  'mdi-robot-outline', 'mdi-shield-check-outline', 'mdi-auto-fix', 'mdi-coffee', 'mdi-crown-outline'
]

const colorPresets = ['#5b13ec', '#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#ec4899']

// --- STATES ---
const searchQuery = ref('')
const showForm = ref(false)
const isEditing = ref(false)
const currentBadgeId = ref<string | null>(null)
const badges = ref<Badge[]>([])
const loading = ref(false)
const configXP = ref({ commitXp: 10, prXp: 50, bugFixXp: 100 })

// --- NOTIFICATIONS & DIALOGS ---
const snackbar = ref({ show: false, message: '', color: 'success' })
const deleteDialog = ref({ show: false, badgeId: null as string | null, badgeTitle: '' })

const form = ref<Badge>({
  title: '',
  description: '',
  xp: 100,
  icon: 'mdi-trophy-outline',
  color: '#5b13ec',
  type: 'Auto'
})

// --- LOGIC: NOTIFICATIONS ---
const showMsg = (msg: string, color: string = 'success') => {
  snackbar.value = { show: true, message: msg, color: color }
}

// --- LOGIC: CONFIG ---
const fetchConfig = async () => {
  try {
    const data = await xpConfigService.get();
    configXP.value = { commitXp: data.commitXp, prXp: data.prXp, bugFixXp: data.bugFixXp }
  } catch (e) { console.error("Load error:", e) }
}

const saveConfig = async () => {
  try {
    await xpConfigService.update({ ...configXP.value });
    showMsg("Engine settings saved successfully! 🚀");
  } catch (e) { showMsg("Failed to save settings", "error") }
}

// --- LOGIC: BADGES ---
const fetchBadges = async () => {
  loading.value = true;
  try {
    const data = await badgeService.getAll();
    badges.value = data.map((b: any) => ({
      ...b,
      type: (b.type === 0 || b.type === 'Manual') ? 'Manual' : 'Auto'
    }));
  } finally { loading.value = false; }
}

const saveBadge = async () => {
  if (!form.value.title) return;
  try {
    const payload = {
      ...form.value,
      xp: Number(form.value.xp),
      type: form.value.type
    };
    if (isEditing.value && currentBadgeId.value) {
      await badgeService.update(currentBadgeId.value, payload as any);
      showMsg("Badge updated successfully");
    } else {
      await badgeService.create(payload as any);
      showMsg("New badge created!");
    }
    await fetchBadges();
    showForm.value = false;
  } catch (e) { showMsg("Error saving badge", "error") }
}

// --- LOGIC: DELETE ---
const openConfirmDelete = (badge: Badge) => {
  deleteDialog.value = { show: true, badgeId: badge.id!, badgeTitle: badge.title }
}

const executeDelete = async () => {
  if (!deleteDialog.value.badgeId) return;
  try {
    await badgeService.delete(deleteDialog.value.badgeId);
    showMsg(`Badge "${deleteDialog.value.badgeTitle}" deleted`, "info");
    await fetchBadges();
  } catch (e) { showMsg("Delete failed", "error") }
  finally { deleteDialog.value.show = false; }
}

onMounted(() => { fetchBadges(); fetchConfig(); })

const filteredBadges = computed(() => badges.value.filter(b => b.title.toLowerCase().includes(searchQuery.value.toLowerCase())));

const autoBadges = computed(() => filteredBadges.value.filter(b => b.type === 'Auto'))
const manualBadges = computed(() => filteredBadges.value.filter(b => b.type === 'Manual'))

const openCreate = (defaultType: string = 'Auto') => {
  isEditing.value = false;
  currentBadgeId.value = null;
  form.value = { title: '', description: '', xp: 100, icon: 'mdi-trophy-outline', color: '#5b13ec', type: defaultType };
  showForm.value = true;
}

const openEdit = (badge: Badge) => {
  isEditing.value = true;
  currentBadgeId.value = badge.id!;
  form.value = { ...badge };
  showForm.value = true;
}
</script>

<template>
  <div class="flex flex-col h-full bg-slate-50 overflow-hidden font-sans">

    <!-- HEADER -->
    <header v-if="!showForm" class="h-16 bg-white border-b flex items-center gap-3 px-8 shrink-0">
      <v-text-field v-model="searchQuery" prepend-inner-icon="mdi-magnify" placeholder="Search badges..." variant="solo" flat hide-details bg-color="#F1F5F9" rounded="lg" class="max-w-xs" density="compact"></v-text-field>

      <div class="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 ml-auto mr-3">
        <v-icon icon="mdi-medal-outline" color="#5b13ec" size="15"></v-icon>
        <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Total</span>
        <span class="text-sm font-black text-slate-800">{{ badges.length }}</span>
        <span class="w-px h-4 bg-slate-200 mx-1"></span>
        <span class="text-[10px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">{{ autoBadges.length }} Auto</span>
        <span class="text-[10px] font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">{{ manualBadges.length }} Manual</span>
      </div>

      <v-btn @click="openCreate()" color="#5b13ec" class="text-none rounded-lg font-bold text-white px-5" elevation="0" height="38">
        <v-icon icon="mdi-plus" class="mr-1" size="18"></v-icon>
        New Badge
      </v-btn>
    </header>

    <div class="flex-1 overflow-y-auto p-6 relative">
      <v-progress-linear v-if="loading" indeterminate color="#5b13ec" absolute top></v-progress-linear>

      <!-- VIEW: GRID -->
      <div v-if="!showForm" class="max-w-7xl mx-auto pb-44 space-y-8">

        <!-- SECTION AUTO BADGES -->
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div class="w-2 h-2 rounded-full bg-emerald-400"></div>
            <h2 class="text-base font-bold text-slate-700">Auto Badges</h2>
            <span class="text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-100 px-2 py-0.5 rounded-full">{{ autoBadges.length }}</span>
          </div>

          <div v-if="autoBadges.length === 0 && !loading" class="text-sm text-slate-400 italic py-4 px-2">Aucun badge automatique.</div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div v-for="badge in autoBadges" :key="badge.id" class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all group relative">
              <div class="absolute top-3 right-3 flex gap-2 z-10">
                <button @click.stop="openEdit(badge)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center hover:bg-slate-200"><v-icon icon="mdi-pencil" size="14"></v-icon></button>
                <button @click.stop="openConfirmDelete(badge)" class="w-8 h-8 rounded-lg bg-red-50 text-red-600 flex items-center justify-center hover:bg-red-100"><v-icon icon="mdi-delete" size="14"></v-icon></button>
              </div>
              <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-4" :style="{ backgroundColor: badge.color + '10' }">
                <v-icon :icon="badge.icon" size="26" :style="{ color: badge.color }"></v-icon>
              </div>
              <h3 class="text-base font-bold mb-1 text-slate-800 truncate">{{ badge.title }}</h3>
              <span class="text-[9px] font-bold uppercase px-1.5 py-0.5 rounded border inline-block mb-3" :style="{ color: badge.color, borderColor: badge.color + '30' }">{{ badge.type }}</span>
              <p class="text-xs text-slate-500 line-clamp-2 h-8">{{ badge.description }}</p>
              <div class="flex items-center gap-1 font-bold text-xs mt-3" :style="{ color: badge.color }">
                <v-icon icon="mdi-lightning-bolt" size="14"></v-icon> {{ badge.xp }} XP
              </div>
            </div>

            <button @click="openCreate('Auto')" class="border-2 border-dashed border-slate-200 rounded-2xl flex flex-col items-center justify-center p-5 hover:border-emerald-400 hover:bg-white transition-all group min-h-[180px] bg-slate-50/50">
              <v-icon icon="mdi-plus" color="#94a3b8" size="28" class="mb-2"></v-icon>
              <span class="text-slate-400 font-bold text-sm">Create Auto Badge</span>
            </button>
          </div>
        </div>

        <div class="border-t border-slate-200"></div>

        <!-- SECTION MANUAL BADGES -->
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div class="w-2 h-2 rounded-full bg-blue-400"></div>
            <h2 class="text-base font-bold text-slate-700">Manual Badges</h2>
            <span class="text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-100 px-2 py-0.5 rounded-full">{{ manualBadges.length }}</span>
          </div>

          <div v-if="manualBadges.length === 0 && !loading" class="text-sm text-slate-400 italic py-4 px-2">Aucun badge manuel.</div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div v-for="badge in manualBadges" :key="badge.id" class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all group relative">
              <div class="absolute top-3 right-3 flex gap-2 z-10">
                <button @click.stop="openEdit(badge)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center hover:bg-slate-200"><v-icon icon="mdi-pencil" size="14"></v-icon></button>
                <button @click.stop="openConfirmDelete(badge)" class="w-8 h-8 rounded-lg bg-red-50 text-red-600 flex items-center justify-center hover:bg-red-100"><v-icon icon="mdi-delete" size="14"></v-icon></button>
              </div>
              <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-4" :style="{ backgroundColor: badge.color + '10' }">
                <v-icon :icon="badge.icon" size="26" :style="{ color: badge.color }"></v-icon>
              </div>
              <h3 class="text-base font-bold mb-1 text-slate-800 truncate">{{ badge.title }}</h3>
              <span class="text-[9px] font-bold uppercase px-1.5 py-0.5 rounded border inline-block mb-3" :style="{ color: badge.color, borderColor: badge.color + '30' }">{{ badge.type }}</span>
              <p class="text-xs text-slate-500 line-clamp-2 h-8">{{ badge.description }}</p>
              <div class="flex items-center gap-1 font-bold text-xs mt-3" :style="{ color: badge.color }">
                <v-icon icon="mdi-lightning-bolt" size="14"></v-icon> {{ badge.xp }} XP
              </div>
            </div>

            <button @click="openCreate('Manual')" class="border-2 border-dashed border-slate-200 rounded-2xl flex flex-col items-center justify-center p-5 hover:border-blue-400 hover:bg-white transition-all group min-h-[180px] bg-slate-50/50">
              <v-icon icon="mdi-plus" color="#94a3b8" size="28" class="mb-2"></v-icon>
              <span class="text-slate-400 font-bold text-sm">Create Manual Badge</span>
            </button>
          </div>
        </div>

      </div>

      <!-- FORM VIEW -->
      <div v-else class="h-full flex items-center justify-center py-4">
        <div class="w-full max-w-lg bg-white border rounded-[1.5rem] p-6 shadow-2xl relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-1" :style="{ backgroundColor: form.color }"></div>
          <h3 class="text-lg font-bold mb-5 text-slate-800 flex items-center gap-2">
            <v-icon :icon="isEditing ? 'mdi-pencil' : 'mdi-plus-circle'" :color="form.color" size="20"></v-icon>
            {{ isEditing ? 'Edit Badge' : 'Create Badge' }}
          </h3>
          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <v-text-field v-model="form.title" label="Badge Title" variant="outlined" rounded="lg" hide-details density="compact"></v-text-field>
              <v-select v-model="form.type" :items="['Manual', 'Auto']" label="Type" variant="outlined" rounded="lg" hide-details density="compact"></v-select>
            </div>
            <v-textarea v-model="form.description" label="Description" variant="outlined" rounded="lg" rows="2" hide-details density="compact"></v-textarea>

            <div class="grid grid-cols-2 gap-3">
              <v-text-field v-model.number="form.xp" type="number" label="XP Reward" variant="outlined" rounded="lg" hide-details density="compact" prepend-inner-icon="mdi-lightning-bolt"></v-text-field>

              <!-- COLOR SELECTOR SECTION -->
              <div class="flex items-center gap-1.5 p-1.5 bg-slate-50 rounded-lg border h-[40px]">
                <button v-for="c in colorPresets" :key="c" @click="form.color = c" :style="{ backgroundColor: c }" :class="['w-4 h-4 rounded-full transition-all', form.color === c ? 'ring-2 ring-slate-800' : 'opacity-60']"></button>

                <!-- Separator -->
                <div class="w-px h-4 bg-slate-300 mx-0.5"></div>

                <!-- Custom Color Picker -->
                <div class="relative w-5 h-5 flex items-center justify-center group cursor-pointer">
                  <v-icon icon="mdi-palette-outline" size="16" class="text-slate-400 group-hover:text-slate-600"></v-icon>
                  <input type="color" v-model="form.color" class="absolute inset-0 opacity-0 cursor-pointer w-full h-full" title="Custom color" />
                </div>
              </div>
            </div>

            <div class="grid grid-cols-10 gap-1 p-2 bg-slate-50 rounded-lg border max-h-32 overflow-y-auto">
              <button v-for="icon in iconPresets" :key="icon" @click="form.icon = icon"
                      :class="['w-8 h-8 rounded-md flex items-center justify-center transition-all', form.icon === icon ? 'bg-slate-800 text-white' : 'bg-white text-slate-400 border hover:border-slate-300']">
                <v-icon :icon="icon" size="16"></v-icon>
              </button>
            </div>
            <div class="flex gap-2 pt-4">
              <v-btn @click="showForm = false" variant="text" class="flex-1 rounded-lg">Cancel</v-btn>
              <v-btn @click="saveBadge" :color="form.color" class="flex-1 text-white font-bold rounded-lg" elevation="0">Confirm</v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ENGINE CONFIG FOOTER -->
    <footer v-if="!showForm" class="fixed bottom-6 left-72 right-8 z-20">
      <div class="bg-white/95 backdrop-blur border rounded-2xl p-4 flex items-center shadow-xl max-w-6xl mx-auto">
        <div class="flex items-center gap-3 px-4 border-r mr-auto">
          <v-icon icon="mdi-tune" color="#5b13ec" size="22"></v-icon>
          <span class="font-bold text-[11px] text-slate-700 uppercase tracking-wider text-black">Engine Settings</span>
        </div>
        <div class="flex gap-8 items-center px-6">
          <div class="flex flex-col"><span class="text-[9px] font-bold text-blue-500 uppercase mb-1">Commit</span><input v-model.number="configXP.commitXp" class="w-12 font-bold text-sm text-blue-700 bg-blue-50 rounded px-1 text-center" type="number"/></div>
          <div class="flex flex-col"><span class="text-[9px] font-bold text-emerald-500 uppercase mb-1">PR</span><input v-model.number="configXP.prXp" class="w-12 font-bold text-sm text-emerald-700 bg-emerald-50 rounded px-1 text-center" type="number"/></div>
          <div class="flex flex-col"><span class="text-[9px] font-bold text-red-500 uppercase mb-1">BugFix</span><input v-model.number="configXP.bugFixXp" class="w-12 font-bold text-sm text-red-700 bg-red-50 rounded px-1 text-center" type="number"/></div>
        </div>
        <v-btn @click="saveConfig" color="#5b13ec" class="text-none rounded-xl text-white font-bold px-8" elevation="0" height="42">Update</v-btn>
      </div>
    </footer>

    <!-- MODAL DE SUPPRESSION -->
    <v-dialog v-model="deleteDialog.show" max-width="450">
      <v-card class="rounded-[1.5rem] p-4 overflow-hidden">
        <v-card-title class="text-xl font-bold text-slate-800 pt-4 px-6">Delete Badge?</v-card-title>
        <v-card-text class="text-slate-500 px-6 py-4">
          Are you sure you want to delete <span class="font-bold text-slate-800">"{{ deleteDialog.badgeTitle }}"</span>?
          This action is permanent and cannot be undone.
        </v-card-text>
        <v-card-actions class="px-6 pb-6 pt-2 flex justify-end gap-3">
          <v-btn variant="tonal" color="slate" class="rounded-xl px-6 text-none font-bold" height="44" @click="deleteDialog.show = false">Cancel</v-btn>
          <v-btn color="#ef4444" class="rounded-xl px-8 text-none text-white font-bold" elevation="0" height="44" @click="executeDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" rounded="pill">
      <div class="text-center font-bold">{{ snackbar.message }}</div>
    </v-snackbar>

  </div>
</template>