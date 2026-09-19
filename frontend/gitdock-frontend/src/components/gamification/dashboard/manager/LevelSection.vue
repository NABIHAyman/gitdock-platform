<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { levelService, type CreateLevelDto, type LevelResponseDto } from '@/services/LevelService'
import { tagService, type Tag } from '@/services/TagService'

// --- ÉTATS ---
const levels = ref<LevelResponseDto[]>([])
const tags = ref<Tag[]>([])
const loading = ref(true)
const showForm = ref(false)
const isEditing = ref(false)
const currentLevelId = ref<string | null>(null)

// --- NOTIFICATIONS & DIALOGUES ---
const snackbar = ref({ show: false, message: '', color: 'success' })
const confirmDialog = ref({ show: false, levelId: null as string | null })

const showNotify = (msg: string, color: string = 'success') => {
  snackbar.value = { show: true, message: msg, color: color }
}

// --- FORM STRUCTURE ---
const form = ref({
  name: '',
  levelRank: 1,
  requiredXP: 1000,
  items: [] as { tagId: string; requiredOccurrences: number }[]
})

// --- LOGIQUE DATA ---
const fetchData = async () => {
  loading.value = true
  try {
    const [tagsData, levelsData] = await Promise.all([
      tagService.getAll(),
      levelService.getAll()
    ])
    tags.value = tagsData
    levels.value = levelsData
  } catch (error) {
    showNotify('Erreur de synchronisation', 'error')
  } finally {
    loading.value = false
  }
}

const getTag = (tagId: string) => tags.value.find(t => t.id === tagId)

const addRequirement = () => {
  form.value.items.push({ tagId: '', requiredOccurrences: 1 })
}

const removeRequirement = (index: number) => {
  form.value.items.splice(index, 1)
}

// --- ACTIONS ---
const saveLevel = async () => {
  if (!form.value.name) return

  const payload: CreateLevelDto = {
    name: form.value.name,
    levelRank: form.value.levelRank,
    requiredXP: form.value.requiredXP,
    levelTagRequirements: form.value.items
        .filter(r => r.tagId !== '')
        .map(r => ({
          tagId: r.tagId,
          requiredOccurrences: r.requiredOccurrences
        }))
  }

  try {
    if (isEditing.value && currentLevelId.value) {
      await levelService.update(currentLevelId.value, payload)
      showNotify('Palier mis à jour')
    } else {
      await levelService.create(payload)
      showNotify('Nouveau palier créé')
    }
    await fetchData()
    closeForm()
  } catch (error) {
    showNotify('Erreur lors de l\'enregistrement', 'error')
  }
}

const executeDelete = async () => {
  if (!confirmDialog.value.levelId) return
  try {
    await levelService.delete(confirmDialog.value.levelId)
    showNotify('Niveau supprimé', 'info')
    await fetchData()
  } catch (error) {
    showNotify('Échec de la suppression', 'error')
  } finally {
    confirmDialog.value.show = false
  }
}

// --- NAVIGATION ---
const openCreateForm = () => {
  isEditing.value = false
  currentLevelId.value = null
  form.value = { name: '', levelRank: levels.value.length + 1, requiredXP: 1000, items: [] }
  showForm.value = true
}

const openEditForm = (level: LevelResponseDto) => {
  isEditing.value = true
  currentLevelId.value = level.id
  form.value = {
    name: level.name,
    levelRank: level.levelRank,
    requiredXP: level.requiredXP,
    items: level.levelTagRequirements ? level.levelTagRequirements.map(r => ({ ...r })) : []
  }
  showForm.value = true
}

const closeForm = () => { showForm.value = false }
const maxRank = computed(() => levels.value.reduce((max, l) => Math.max(max, l.levelRank || 0), 0))

onMounted(fetchData)
</script>

<template>
  <div class="flex flex-col h-full bg-slate-50 overflow-hidden">
    <!-- HEADER -->
    <header v-if="!showForm" class="h-16 bg-white border-b flex items-center gap-3 px-8 shrink-0">
      <h2 class="text-lg font-bold text-slate-800">Progression Path</h2>

      <div class="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 ml-auto mr-3">
        <v-icon icon="mdi-stairs-up" color="#5b13ec" size="15"></v-icon>
        <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Total</span>
        <span class="text-sm font-black text-slate-800">{{ levels.length }}</span>
        <span class="w-px h-4 bg-slate-200 mx-1"></span>
        <span class="text-[10px] font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-full">MAX LVL {{ maxRank }}</span>
      </div>

      <v-btn @click="openCreateForm" color="#5b13ec" class="text-none rounded-lg font-bold text-white px-5" elevation="0" height="38">
        <v-icon icon="mdi-plus" class="mr-1" size="18"></v-icon> Nouveau Niveau
      </v-btn>
    </header>

    <div class="flex-1 overflow-y-auto p-6 relative">
      <v-progress-linear v-if="loading" indeterminate color="#5b13ec" absolute top></v-progress-linear>

      <!-- AFFICHAGE EN LISTE (L'UN SOUS L'AUTRE) -->
      <div v-if="!showForm" class="max-w-4xl mx-auto space-y-3">
        <div v-for="level in levels" :key="level.id"
             class="bg-white border border-slate-200 rounded-2xl p-5 flex items-center justify-between group hover:border-[#5b13ec]/30 transition-all shadow-sm">

          <div class="flex items-center gap-5">
            <div class="w-12 h-12 bg-slate-50 rounded-xl flex items-center justify-center border border-slate-100">
              <span class="text-lg font-black text-[#5b13ec]">{{ level.levelRank }}</span>
            </div>
            <div>
              <h3 class="text-base font-bold text-slate-800">{{ level.name }}</h3>
              <div class="flex items-center gap-1.5">
                <v-icon icon="mdi-lightning-bolt" size="14" class="text-amber-500"></v-icon>
                <p class="text-[11px] text-slate-400 font-bold uppercase">{{ level.requiredXP }} XP</p>
              </div>
            </div>
          </div>

          <!-- Tags Requirements -->
          <div class="flex flex-wrap gap-1.5 justify-end max-w-md">
            <span v-for="(req, index) in level.levelTagRequirements" :key="index"
                  :style="{ backgroundColor: getTag(req.tagId)?.color + '10', color: getTag(req.tagId)?.color, borderColor: getTag(req.tagId)?.color + '20' }"
                  class="px-2.5 py-1 rounded-lg text-[9px] font-black border uppercase">
              {{ getTag(req.tagId)?.name || 'Unknown' }} x{{ req.requiredOccurrences }}
            </span>
          </div>

          <!-- Actions -->
          <div class="flex gap-1 ml-6 border-l pl-4">
            <button @click="openEditForm(level)" class="w-8 h-8 rounded-lg text-slate-400 hover:text-[#5b13ec] hover:bg-indigo-50 transition-all">
              <v-icon icon="mdi-pencil" size="16"></v-icon>
            </button>
            <!-- Bouton Corbeille en ROUGE -->
            <button @click="confirmDialog = { show: true, levelId: level.id! }"
                    class="w-8 h-8 rounded-lg text-red-500 opacity-30 group-hover:opacity-100 hover:bg-red-50 transition-all">
              <v-icon icon="mdi-delete" size="16"></v-icon>
            </button>
          </div>
        </div>
      </div>

      <!-- FORMULAIRE (STYLE TAG COMPACT) -->
      <div v-else class="h-full flex items-center justify-center py-4">
        <div class="w-full max-w-lg bg-white border rounded-[1.5rem] p-8 shadow-2xl relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-1 bg-[#5b13ec]"></div>
          <h3 class="text-lg font-bold mb-6 text-slate-800">{{ isEditing ? 'Modifier le Palier' : 'Nouveau Palier' }}</h3>

          <div class="space-y-5">
            <div class="flex gap-3">
              <v-text-field v-model="form.name" label="Nom du Niveau" variant="outlined" rounded="lg" hide-details density="compact" class="flex-[2]"></v-text-field>
              <v-text-field v-model.number="form.levelRank" type="number" label="Rang" variant="outlined" rounded="lg" hide-details density="compact" class="flex-1"></v-text-field>
            </div>

            <v-text-field v-model.number="form.requiredXP" type="number" label="XP Requis" variant="outlined" rounded="lg" hide-details density="compact" prepend-inner-icon="mdi-lightning-bolt"></v-text-field>

            <div>
              <div class="flex items-center justify-between mb-2">
                <p class="text-[10px] font-bold uppercase text-slate-400">Pré-requis par Tag</p>
                <button @click="addRequirement" class="text-[10px] font-bold text-[#5b13ec] hover:underline">+ AJOUTER</button>
              </div>
              <div class="bg-slate-50 rounded-xl border p-2 space-y-2 max-h-44 overflow-y-auto custom-scroll">
                <div v-for="(req, index) in form.items" :key="index" class="flex gap-2 bg-white p-2 rounded-lg border border-slate-100 items-center shadow-sm">
                  <v-select v-model="req.tagId" :items="tags" item-title="name" item-value="id" placeholder="Tag" variant="plain" hide-details density="compact" class="text-xs"></v-select>
                  <input v-model.number="req.requiredOccurrences" type="number" class="w-10 text-center text-xs font-bold bg-slate-50 rounded py-1 outline-none" />
                  <v-btn @click="removeRequirement(index)" icon="mdi-close" variant="text" color="red-lighten-2" size="small" density="comfortable"></v-btn>
                </div>
                <div v-if="form.items.length === 0" class="text-center py-4 text-[11px] text-slate-400 italic">Aucun tag requis</div>
              </div>
            </div>

            <div class="flex gap-2 pt-4">
              <v-btn @click="closeForm" variant="text" class="flex-1 text-none font-bold">Annuler</v-btn>
              <v-btn @click="saveLevel" color="#5b13ec" class="flex-1 text-white font-bold text-none" elevation="0">Confirmer</v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- DIALOGUE SUPPRESSION -->
    <v-dialog v-model="confirmDialog.show" max-width="450">
      <v-card class="rounded-[1.5rem] p-4">
        <v-card-title class="text-xl font-bold pt-4 px-6 text-red-600">Supprimer le niveau ?</v-card-title>
        <v-card-text class="px-6 py-4 text-slate-500 text-sm">Action irréversible.</v-card-text>
        <v-card-actions class="px-6 pb-6 pt-2 flex justify-end gap-3">
          <v-btn variant="tonal" @click="confirmDialog.show = false">Annuler</v-btn>
          <v-btn color="#ef4444" class="text-white font-bold" @click="executeDelete">Supprimer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" rounded="pill">
      <div class="text-center font-bold text-xs">{{ snackbar.message }}</div>
    </v-snackbar>
  </div>
</template>

<style scoped>
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
.custom-scroll::-webkit-scrollbar { width: 4px; }
</style>