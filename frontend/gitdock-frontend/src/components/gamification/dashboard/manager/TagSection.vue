<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { tagService, type Tag } from '@/services/TagService'

// --- ÉTATS ---
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

// --- NOTIFICATIONS ---
const snackbar = ref({ show: false, message: '', color: 'success' })
const deleteDialog = ref({ show: false, tagId: null as string | null, tagName: '' })

const showMsg = (msg: string, color: string = 'success') => {
  snackbar.value = { show: true, message: msg, color: color }
}

// --- ACTIONS API ---
const fetchTags = async () => {
  loading.value = true
  try {
    const data = await tagService.getAll()
    tags.value = data
  } catch (error) {
    showMsg("Erreur lors du chargement des tags", "error")
  } finally {
    loading.value = false
  }
}

const saveTag = async () => {
  if (!form.value.name) return
  try {
    if (isEditing.value && currentTagId.value) {
      await tagService.update(currentTagId.value, form.value as Tag)
      showMsg("Tag mis à jour avec succès")
    } else {
      await tagService.create(form.value as Tag)
      showMsg("Nouveau tag créé !")
    }
    showForm.value = false
    await fetchTags()
  } catch (error) {
    console.error("Erreur Save:", error)
    showMsg("Erreur lors de l'enregistrement (Vérifiez la console)", "error")
  }
}

const openConfirmDelete = (tag: Tag) => {
  deleteDialog.value = { show: true, tagId: tag.id!, tagName: tag.name }
}

const executeDelete = async () => {
  if (!deleteDialog.value.tagId) return
  try {
    await tagService.delete(deleteDialog.value.tagId)
    showMsg(`Tag "${deleteDialog.value.tagName}" supprimé`, "info")
    await fetchTags()
  } catch (error) {
    showMsg("Échec de la suppression", "error")
  } finally {
    deleteDialog.value.show = false
  }
}

// --- LOGIQUE UI ---
const filteredTags = computed(() => {
  return tags.value.filter(t =>
      t.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// --- COMPUTED: STATS PAR TYPE ---
const tagTypeCount = computed(() => {
  const map: Record<string, number> = {}
  tags.value.forEach(t => { map[t.type] = (map[t.type] || 0) + 1 })
  return map
})

const openCreate = () => {
  isEditing.value = false
  currentTagId.value = null
  form.value = { name: '', color: '#5b13ec', type: 'Commit' }
  showForm.value = true
}

const openEdit = (tag: Tag) => {
  isEditing.value = true
  currentTagId.value = tag.id!
  form.value = { ...tag }
  showForm.value = true
}

onMounted(fetchTags)
</script>

<template>
  <div class="flex flex-col h-full bg-slate-50 overflow-hidden">
    <!-- HEADER -->
    <header v-if="!showForm" class="h-16 bg-white border-b flex items-center gap-3 px-8 shrink-0">
      <v-text-field
          v-model="searchQuery"
          prepend-inner-icon="mdi-magnify"
          placeholder="Rechercher un tag..."
          variant="solo"
          flat hide-details
          bg-color="#F1F5F9"
          rounded="lg"
          class="max-w-xs"
          density="compact"
      ></v-text-field>

      <!-- COMPTEUR STATS -->
      <div class="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 ml-auto mr-3">
        <v-icon icon="mdi-tag-multiple-outline" color="#10b981" size="15"></v-icon>
        <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Total</span>
        <span class="text-sm font-black text-slate-800">{{ tags.length }}</span>
        <span class="w-px h-4 bg-slate-200 mx-1"></span>
        <template v-for="(count, type) in tagTypeCount" :key="type">
          <span class="text-[10px] font-bold text-slate-600 bg-slate-100 px-2 py-0.5 rounded-full">
            {{ count }} {{ type }}
          </span>
        </template>
      </div>

      <v-btn @click="openCreate" color="#5b13ec" class="text-none rounded-lg font-bold text-white px-5" elevation="0" height="38">
        <v-icon icon="mdi-plus" class="mr-1" size="18"></v-icon>
        Nouveau Tag
      </v-btn>
    </header>

    <div class="flex-1 overflow-y-auto p-6 relative">
      <v-progress-linear v-if="loading" indeterminate color="#5b13ec" absolute top></v-progress-linear>

      <!-- GRILLE DES TAGS -->
      <div v-if="!showForm" class="max-w-7xl mx-auto">
        <h2 class="text-xl font-bold mb-5 text-slate-800">Gestion des Tags</h2>

        <div v-if="filteredTags.length === 0 && !loading" class="text-center py-10 text-slate-400">
          Aucun tag trouvé.
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div v-for="tag in filteredTags" :key="tag.id" class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all group relative">
            <div class="absolute top-3 right-3 flex gap-2 z-10">
              <button @click.stop="openEdit(tag)" class="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 flex items-center justify-center hover:bg-slate-200">
                <v-icon icon="mdi-pencil" size="14"></v-icon>
              </button>
              <button @click.stop="openConfirmDelete(tag)" class="w-8 h-8 rounded-lg bg-red-50 text-red-500 flex items-center justify-center hover:bg-red-100">
                <v-icon icon="mdi-delete" size="14"></v-icon>
              </button>
            </div>

            <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-4" :style="{ backgroundColor: tag.color + '10' }">
              <v-icon icon="mdi-tag-outline" size="26" :style="{ color: tag.color }"></v-icon>
            </div>

            <h3 class="text-base font-bold mb-1 text-slate-800 truncate">{{ tag.name }}</h3>
            <span class="text-[9px] font-bold uppercase px-1.5 py-0.5 rounded border inline-block"
                  :style="{ color: tag.color, backgroundColor: tag.color + '05', borderColor: tag.color + '30' }">
              {{ tag.type }}
            </span>
          </div>

          <button @click="openCreate" class="border-2 border-dashed border-slate-200 rounded-2xl flex flex-col items-center justify-center p-5 hover:border-[#5b13ec] hover:bg-white transition-all group min-h-[140px] bg-slate-50/50">
            <v-icon icon="mdi-plus" color="#94a3b8" size="28" class="group-hover:text-[#5b13ec] mb-2"></v-icon>
            <span class="text-slate-400 font-bold text-sm group-hover:text-[#5b13ec]">Créer un Tag</span>
          </button>
        </div>
      </div>

      <!-- FORMULAIRE -->
      <div v-else class="h-full flex items-center justify-center py-4">
        <div class="w-full max-w-lg bg-white border rounded-[1.5rem] p-8 shadow-2xl relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-1" :style="{ backgroundColor: form.color }"></div>
          <h3 class="text-lg font-bold mb-6 text-slate-800">{{ isEditing ? 'Modifier le Tag' : 'Nouveau Tag' }}</h3>

          <div class="space-y-5">
            <v-text-field v-model="form.name" label="Nom du Tag" variant="outlined" rounded="lg"></v-text-field>
            <v-select v-model="form.type" :items="tagTypes" label="Type d'action" variant="outlined" rounded="lg"></v-select>

            <div>
              <p class="text-[10px] font-bold uppercase text-slate-400 mb-2">Couleur</p>
              <div class="flex items-center gap-2 p-2 bg-slate-50 rounded-lg border">
                <button v-for="c in colorPresets" :key="c" @click="form.color = c"
                        :style="{ backgroundColor: c }"
                        :class="['w-6 h-6 rounded-full transition-all', form.color === c ? 'ring-2 ring-black scale-110' : 'opacity-60']"></button>
                <input type="color" v-model="form.color" class="ml-auto w-6 h-6 cursor-pointer bg-transparent border-none">
              </div>
            </div>

            <div class="flex gap-2 pt-4">
              <v-btn @click="showForm = false" variant="text" class="flex-1">Annuler</v-btn>
              <v-btn @click="saveTag" :color="form.color" class="flex-1 text-white font-bold" elevation="0">Confirmer</v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- DIALOGUE SUPPRESSION -->
    <v-dialog v-model="deleteDialog.show" max-width="450">
      <v-card class="rounded-[1.5rem] p-4">
        <v-card-title class="text-xl font-bold pt-4 px-6">Supprimer le tag ?</v-card-title>
        <v-card-text class="px-6 py-4">Êtes-vous sûr de vouloir supprimer <span class="font-bold">"{{ deleteDialog.tagName }}"</span> ?</v-card-text>
        <v-card-actions class="px-6 pb-6 pt-2 flex justify-end gap-3">
          <v-btn variant="tonal" @click="deleteDialog.show = false">Annuler</v-btn>
          <v-btn color="#ef4444" class="text-white font-bold" @click="executeDelete">Supprimer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- SNACKBAR -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" rounded="pill">
      <div class="text-center font-bold">{{ snackbar.message }}</div>
    </v-snackbar>
  </div>
</template>