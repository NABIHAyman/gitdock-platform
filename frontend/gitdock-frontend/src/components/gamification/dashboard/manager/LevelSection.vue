<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { levelService } from '@/services/LevelService'
import { tagService, type Tag } from '@/services/TagService'

// --- STATES ---
const levels = ref<any[]>([])
const tags = ref<Tag[]>([])
const loading = ref(true)
const showForm = ref(false)
const isEditing = ref(false)
const currentLevelId = ref<string | null>(null)

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
const levelToDeleteId = ref<string | null>(null)

// --- FORM STRUCTURE ---
const form = ref({
  name: '',
  levelRank: 1,
  requiredXP: 100,
  items: [] as any[]
})

// --- DATA FETCHING ---
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
    console.error("Loading error", error)
    showNotify("Failed to synchronize progression data.", "error")
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// --- TAG REQUIREMENTS LOGIC ---
const addRequirement = () => {
  form.value.items.push({ tagId: '', requiredOccurrences: 1 })
}

const removeRequirement = (index: number) => {
  form.value.items.splice(index, 1)
}

// --- SAVE ACTIONS ---
const saveLevel = async () => {
  try {
    const payload = {
      name: form.value.name,
      levelRank: form.value.levelRank,
      requiredXP: form.value.requiredXP,
      requirements: form.value.items
          .filter(r => r.tagId !== '')
          .map(r => ({
            tagId: r.tagId,
            requiredOccurrences: r.requiredOccurrences
          }))
    }

    if (isEditing.value && currentLevelId.value) {
      await levelService.update(currentLevelId.value, payload as any)
      showNotify("Progression level updated successfully.")
    } else {
      await levelService.create(payload as any)
      showNotify("New progression level deployed.")
    }

    await fetchData()
    closeForm()
  } catch (error: any) {
    showNotify("API Error: Check console for details.", "error")
  }
}

// --- DELETE LOGIC ---
const openConfirmDialog = (id: string) => {
  levelToDeleteId.value = id
  confirmDialog.value = true
}

const confirmDeleteLevel = async () => {
  confirmDialog.value = false
  if (levelToDeleteId.value) {
    try {
      await levelService.delete(levelToDeleteId.value)
      await fetchData()
      showNotify("Level successfully removed from path.")
    } catch (error) {
      showNotify("Error during level deletion.", "error")
    } finally {
      levelToDeleteId.value = null
    }
  }
}

// --- NAVIGATION ---
const openCreateForm = () => {
  isEditing.value = false
  currentLevelId.value = null
  form.value = {
    name: '',
    levelRank: levels.value.length + 1,
    requiredXP: 1000,
    items: []
  }
  showForm.value = true
}

const openEditForm = (level: any) => {
  isEditing.value = true
  currentLevelId.value = level.id
  form.value = {
    name: level.name,
    levelRank: level.levelRank,
    requiredXP: level.requiredXP,
    items: level.levelTagRequirements ? level.levelTagRequirements.map((r: any) => ({
      tagId: r.tagId,
      requiredOccurrences: r.requiredOccurrences
    })) : []
  }
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  isEditing.value = false
  currentLevelId.value = null
}

const getTagName = (tagId: string) => {
  const tag = tags.value.find(t => t.id === tagId)
  return tag ? tag.name : 'Unknown'
}
</script>

<template>
  <div class="flex flex-col h-full overflow-hidden bg-slate-50/30">

    <header v-if="!showForm" class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-8 shrink-0 z-10">
      <h2 class="text-xl font-bold tracking-tight text-slate-900">Progression Path</h2>
      <v-btn @click="openCreateForm" color="#5b13ec" class="text-none rounded-xl font-bold px-5 text-white" elevation="0" height="44">
        <v-icon start icon="mdi-plus"></v-icon> Add New Level
      </v-btn>
    </header>

    <div class="flex-1 overflow-y-auto relative">
      <div v-if="!showForm" class="p-8 pb-40">
        <div v-if="loading" class="flex justify-center p-20">
          <v-progress-circular indeterminate color="#5b13ec"></v-progress-circular>
        </div>

        <div v-else class="max-w-4xl mx-auto space-y-4">
          <div v-for="level in levels" :key="level.id"
               class="bg-white border border-slate-200 rounded-[2.5rem] p-8 shadow-sm flex items-center justify-between relative group hover:shadow-md transition-all">

            <div class="absolute top-4 right-8 flex gap-1">
              <button @click="openEditForm(level)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 hover:text-[#5b13ec] flex items-center justify-center border border-slate-100 transition-colors">
                <v-icon icon="mdi-pencil" size="16"></v-icon>
              </button>
              <button @click="openConfirmDialog(level.id!)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 hover:text-red-600 flex items-center justify-center border border-slate-100 transition-colors">
                <v-icon icon="mdi-delete" size="16"></v-icon>
              </button>
            </div>

            <div class="flex items-center gap-6">
              <div class="w-16 h-16 bg-slate-50 rounded-3xl flex items-center justify-center border border-slate-100 shadow-inner">
                <span class="text-2xl font-black text-[#5b13ec]">{{ level.levelRank }}</span>
              </div>
              <div>
                <h3 class="text-xl font-bold text-slate-900">{{ level.name }}</h3>
                <div class="flex items-center gap-2">
                  <v-icon icon="mdi-lightning-bolt" size="16" class="text-amber-500"></v-icon>
                  <p class="text-sm text-slate-400 font-bold uppercase tracking-tight">Required: {{ level.requiredXP }} XP</p>
                </div>
              </div>
            </div>

            <div class="flex flex-wrap gap-2 justify-end max-w-md">
               <span v-for="req in level.levelTagRequirements" :key="req.tagId"
                     class="bg-[#5b13ec]/5 text-[#5b13ec] px-4 py-2 rounded-xl text-[10px] font-black border border-[#5b13ec]/10 uppercase tracking-tight">
                 {{ getTagName(req.tagId) }} x{{ req.requiredOccurrences }}
               </span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="h-full flex items-center justify-center p-8 bg-slate-50/50 backdrop-blur-sm animate-fade-in">
        <div class="w-full max-w-3xl bg-white border border-slate-200 rounded-[3rem] p-12 shadow-2xl relative overflow-hidden overflow-y-auto max-h-[90vh]">
          <div class="absolute top-0 left-0 w-3 h-full bg-[#5b13ec]"></div>

          <div class="flex justify-between items-start mb-10">
            <div>
              <h3 class="text-3xl font-black tracking-tight text-slate-900">{{ isEditing ? 'Edit Level' : 'New Level' }}</h3>
              <p class="text-slate-400 font-medium mt-1">Configure progression thresholds and milestones</p>
            </div>
            <v-btn icon="mdi-close" variant="tonal" @click="closeForm" rounded="xl"></v-btn>
          </div>

          <div class="space-y-6">
            <div class="grid grid-cols-2 gap-6">
              <v-text-field v-model="form.name" label="Level Name" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-text-field>
              <v-text-field v-model.number="form.levelRank" type="number" label="Rank Order" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-text-field>
            </div>

            <v-text-field v-model.number="form.requiredXP" type="number" label="Required XP" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details prepend-inner-icon="mdi-lightning-bolt"></v-text-field>

            <div class="mt-8">
              <div class="flex items-center justify-between mb-4">
                <p class="text-[11px] font-black uppercase text-slate-400 ml-1 tracking-widest">Tag Milestone Requirements</p>
                <v-btn @click="addRequirement" size="small" variant="text" color="#5b13ec" class="font-bold text-none">
                  <v-icon start icon="mdi-plus-circle-outline"></v-icon> Add Requirement
                </v-btn>
              </div>

              <div class="space-y-3">
                <div v-for="(req, index) in form.items" :key="index"
                     class="flex gap-4 bg-slate-50 p-3 rounded-2xl border border-slate-100 items-center animate-fade-in">
                  <v-select
                      v-model="req.tagId"
                      :items="tags"
                      item-title="name"
                      item-value="id"
                      label="Select Tag"
                      variant="solo" flat rounded="lg" hide-details class="flex-1"
                  ></v-select>
                  <v-text-field v-model.number="req.requiredOccurrences" type="number" label="Qty" variant="solo" flat rounded="lg" hide-details class="w-28 text-center"></v-text-field>
                  <v-btn @click="removeRequirement(index)" icon="mdi-trash-can-outline" variant="text" color="red-lighten-2" density="comfortable"></v-btn>
                </div>
              </div>
            </div>

            <div class="pt-8 flex gap-4">
              <v-btn @click="closeForm" variant="text" class="text-none rounded-2xl font-bold px-8 text-slate-400" height="64">Cancel</v-btn>
              <v-btn @click="saveLevel" color="#5b13ec" class="text-none rounded-2xl font-bold px-10 text-white flex-1 shadow-lg" height="64" elevation="0">
                {{ isEditing ? 'Update Level' : 'Deploy Level' }}
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
          <h3 class="text-3xl font-black tracking-tight text-slate-900 mb-3">Delete Level?</h3>
          <p class="text-slate-500 font-medium mb-12 max-w-sm leading-relaxed">Are you sure you want to remove this level? <br> User progression paths might be affected.</p>
        </div>
        <div class="flex gap-4">
          <v-btn variant="text" @click="confirmDialog = false" class="text-none rounded-2xl font-bold px-8 text-slate-400" height="64">Cancel</v-btn>
          <v-btn @click="confirmDeleteLevel" color="#ef4444" class="text-none rounded-2xl font-bold px-10 text-white flex-1 shadow-lg" height="64" elevation="0">Confirm Delete</v-btn>
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
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
::-webkit-scrollbar { width: 0px; }
</style>