<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { tagService, type Tag } from '@/services/TagService'

// --- STATES ---
const tags = ref<Tag[]>([])
const loading = ref(true)
const searchQuery = ref('')
const showForm = ref(false)
const isEditing = ref(false)
const currentTagId = ref<string | null>(null)

const tagTypes = ['Commit', 'Pull Request', 'Issue', 'Documentation']
const colorPresets = ['#5b13ec', '#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#ec4899']

const form = ref<Partial<Tag>>({
  name: '',
  color: '#5b13ec',
  type: 'Commit'
})

// --- NOTIFICATION SYSTEM (TOP SNACKBAR) ---
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
const tagToDeleteId = ref<string | null>(null)

// --- BACKEND ACTIONS ---
const fetchTags = async () => {
  loading.value = true
  try {
    tags.value = await tagService.getAll()
  } catch (error) {
    console.error("Loading error", error)
    showNotify("Failed to load tags.", "error")
  } finally {
    loading.value = false
  }
}

onMounted(fetchTags)

const saveTag = async () => {
  if (form.value.name) {
    try {
      if (isEditing.value && currentTagId.value) {
        await tagService.update(currentTagId.value, form.value as Tag)
        showNotify("Tag updated successfully.")
      } else {
        await tagService.create(form.value as Tag)
        showNotify("New tag created successfully.")
      }
      await fetchTags()
      closeForm()
    } catch (error) {
      showNotify("Error during data persistence.", "error")
    }
  }
}

// Custom Dialog Logic
const openConfirmDialog = (id: string) => {
  tagToDeleteId.value = id
  confirmDialog.value = true
}

const confirmDeleteTag = async () => {
  confirmDialog.value = false
  if (tagToDeleteId.value) {
    try {
      await tagService.delete(tagToDeleteId.value)
      await fetchTags()
      showNotify("Tag successfully deleted.")
    } catch (error) {
      showNotify("Error during tag deletion.", "error")
    } finally {
      tagToDeleteId.value = null
    }
  }
}

// --- UI LOGIC ---
const filteredTags = computed(() => {
  return tags.value.filter(t =>
      t.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const openCreateForm = () => {
  isEditing.value = false
  form.value = { name: '', color: '#5b13ec', type: 'Commit' }
  showForm.value = true
}

const openEditForm = (tag: Tag) => {
  isEditing.value = true
  currentTagId.value = tag.id!
  form.value = { ...tag }
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  isEditing.value = false
  currentTagId.value = null
}
</script>

<template>
  <div class="flex flex-col h-full overflow-hidden">

    <header v-if="!showForm" class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-8 shrink-0 z-10">
      <div class="max-w-xl w-full">
        <v-text-field v-model="searchQuery" prepend-inner-icon="mdi-magnify" placeholder="Search tags..." variant="solo" flat hide-details bg-color="#F1F5F9" rounded="xl"></v-text-field>
      </div>
      <v-btn @click="openCreateForm" color="#5b13ec" class="text-none rounded-xl font-bold px-5 text-white" elevation="0" height="44">
        <v-icon start icon="mdi-plus"></v-icon> Add New Tag
      </v-btn>
    </header>

    <div class="flex-1 overflow-y-auto relative">
      <div v-if="!showForm" class="p-8 pb-40">
        <div class="max-w-7xl mx-auto">
          <h2 class="text-2xl font-bold mb-6 tracking-tight text-slate-900">Tags Management</h2>

          <div v-if="loading" class="flex justify-center p-20">
            <v-progress-circular indeterminate color="#5b13ec"></v-progress-circular>
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">

            <div v-for="tag in filteredTags" :key="tag.id"
                 class="bg-white border border-slate-200 rounded-3xl p-6 shadow-sm hover:shadow-xl transition-all group relative">

              <div class="absolute top-4 right-4 flex gap-1 z-10">
                <button @click.stop="openEditForm(tag)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 hover:text-[#5b13ec] flex items-center justify-center border border-slate-100">
                  <v-icon icon="mdi-pencil" size="16"></v-icon>
                </button>
                <button @click.stop="openConfirmDialog(tag.id!)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 hover:text-red-600 flex items-center justify-center border border-slate-100">
                  <v-icon icon="mdi-delete" size="16"></v-icon>
                </button>
              </div>

              <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-5" :style="{ backgroundColor: tag.color + '15' }">
                <v-icon icon="mdi-tag-outline" size="30" :style="{ color: tag.color }"></v-icon>
              </div>

              <h3 class="text-lg font-bold mb-1 tracking-tight text-slate-900">{{ tag.name }}</h3>

              <div class="mb-3">
                <span class="text-[10px] font-black uppercase px-2 py-1 rounded-md border"
                      :style="{ backgroundColor: tag.color + '15', color: tag.color, borderColor: tag.color + '30' }">
                  {{ tag.type }}
                </span>
              </div>

            </div>

            <button @click="openCreateForm" class="flex flex-col items-center justify-center border-2 border-dashed border-slate-200 rounded-3xl p-6 hover:border-[#5b13ec]/40 group transition-all min-h-[220px] bg-slate-50/30">
              <v-icon icon="mdi-plus" size="30" class="text-slate-300 group-hover:text-[#5b13ec] mb-3"></v-icon>
              <span class="text-sm font-bold text-slate-400 group-hover:text-[#5b13ec]">Create Tag</span>
            </button>
          </div>
        </div>
      </div>

      <div v-else class="h-full flex items-center justify-center p-8 bg-slate-50/50 backdrop-blur-sm animate-fade-in">
        <div class="w-full max-w-2xl bg-white border border-slate-200 rounded-[3rem] p-12 shadow-2xl relative overflow-hidden">
          <div class="absolute top-0 left-0 w-3 h-full" :style="{ backgroundColor: form.color }"></div>

          <div class="flex justify-between items-start mb-10">
            <div>
              <h3 class="text-3xl font-black tracking-tight text-slate-900">{{ isEditing ? 'Edit Tag' : 'New Tag' }}</h3>
              <p class="text-slate-400 font-medium mt-1">Configure tag identity</p>
            </div>
            <v-btn icon="mdi-close" variant="tonal" @click="closeForm" rounded="xl"></v-btn>
          </div>

          <div class="space-y-6">
            <div class="grid grid-cols-2 gap-6">
              <v-text-field v-model="form.name" label="Tag Name" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-text-field>
              <v-select v-model="form.type" :items="tagTypes" label="Action Type" variant="solo" bg-color="slate-50" rounded="xl" flat border hide-details></v-select>
            </div>

            <div>
              <p class="text-[11px] font-black uppercase text-slate-400 ml-1 mb-3 tracking-widest">Select Color Identity</p>
              <div class="flex items-center gap-3 bg-slate-50 p-3 rounded-2xl border border-slate-100 h-[64px]">
                <div class="flex gap-2 border-r pr-3">
                  <button v-for="c in colorPresets" :key="c" @click="form.color = c"
                          :style="{ backgroundColor: c }"
                          :class="['w-8 h-8 rounded-full border-2 transition-all', form.color === c ? 'border-slate-900 scale-125' : 'border-white']"></button>
                </div>
                <input type="color" v-model="form.color" class="w-10 h-10 cursor-pointer border-none bg-transparent">
              </div>
            </div>

            <div class="pt-8 flex gap-4">
              <v-btn @click="closeForm" variant="text" class="text-none rounded-2xl font-bold px-8 text-slate-400" height="64">Cancel</v-btn>
              <v-btn @click="saveTag" :style="{ backgroundColor: form.color }"
                     class="text-none rounded-2xl font-bold px-10 text-white flex-1 shadow-lg" height="64" elevation="0">
                {{ isEditing ? 'Save Changes' : 'Create Tag' }}
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
          <h3 class="text-3xl font-black tracking-tight text-slate-900 mb-3">Delete Tag?</h3>
          <p class="text-slate-500 font-medium mb-12 max-w-sm leading-relaxed">Are you sure you want to permanently remove this tag? <br> This might affect items using it.</p>
        </div>
        <div class="flex gap-4">
          <v-btn variant="text" @click="confirmDialog = false" class="text-none rounded-2xl font-bold px-8 text-slate-400" height="64">Cancel</v-btn>
          <v-btn @click="confirmDeleteTag" color="#ef4444" class="text-none rounded-2xl font-bold px-10 text-white flex-1 shadow-lg" height="64" elevation="0">Confirm Delete</v-btn>
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