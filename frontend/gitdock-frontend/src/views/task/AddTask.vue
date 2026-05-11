<template>
  <div class="max-w-4xl mx-auto space-y-4">
    <!-- HEADER -->
    <div class="bg-gradient-to-r from-[#5b13ec] to-violet-700 rounded-2xl p-5 text-white shadow-lg flex justify-between items-center">
      <div>
        <h1 class="text-xl font-black">Ajouter une mission</h1>
        <p class="text-white/70 text-[10px]">Créez une nouvelle tâche pour votre projet GitDock.</p>
      </div>
      <button
          @click="router.push('/dashboardtask')"
          class="bg-white/20 hover:bg-white/30 backdrop-blur-md text-white px-5 py-2 rounded-xl text-[11px] font-black transition-all"
      >
        RETOUR
      </button>
    </div>

    <!-- FORMULAIRE -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-100 p-8">

      <!-- LIGNE 1 : Titre -->
      <div class="mb-8">
        <label class="text-[11px] font-black text-slate-400 uppercase tracking-wider block mb-1">Titre de la tâche *</label>
        <input
            v-model="task.title"
            type="text"
            placeholder="Ex: Intégration API Spring Boot..."
            class="w-full border-b-2 border-slate-100 py-3 focus:border-[#5b13ec] outline-none text-base font-bold transition-colors bg-transparent"
        />
      </div>

      <!-- LIGNE 2 : Description -->
      <div class="mb-8">
        <label class="text-[11px] font-black text-slate-400 uppercase tracking-wider block mb-1">Description</label>
        <textarea
            v-model="task.description"
            rows="3"
            placeholder="Décrivez la tâche en détail..."
            class="w-full border border-slate-100 rounded-xl px-4 py-3 mt-1 focus:border-[#5b13ec] outline-none text-sm bg-slate-50/50 resize-none transition-colors"
        ></textarea>
      </div>

      <!-- LIGNE 3 : Projet + Date -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
        <div>
          <label class="text-[11px] font-black text-slate-400 uppercase tracking-wider block mb-1">Projet *</label>
          <select
              v-model="selectedProjectId"
              @change="onProjectChange"
              class="w-full border-b-2 border-slate-100 py-3 outline-none text-sm bg-transparent cursor-pointer focus:border-[#5b13ec]"
          >
            <option :value="null">-- Sélectionnez un projet --</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">
              {{ p.name }}
            </option>
          </select>
        </div>

        <div>
          <label class="text-[11px] font-black text-slate-400 uppercase tracking-wider block mb-1">Date limite</label>
          <input
              v-model="task.dueDate"
              type="date"
              class="w-full border-b-2 border-slate-100 py-3 outline-none text-sm bg-transparent focus:border-[#5b13ec]"
          />
        </div>
      </div>

      <!-- LIGNE 4 : Assigné à -->
      <div class="mb-8">
        <label class="text-[11px] font-black text-slate-400 uppercase tracking-wider block mb-1">
          Assigner à
          <span v-if="!selectedProjectId" class="text-amber-500 ml-1 text-[9px]">
            (Sélectionnez un projet d'abord)
          </span>
        </label>
        <select
            v-model="task.assignedTo"
            :disabled="!selectedProjectId || loadingFormData"
            class="w-full border-b-2 border-slate-100 py-3 outline-none text-sm bg-transparent cursor-pointer focus:border-[#5b13ec] disabled:opacity-40"
        >
          <option :value="null">Non assigné</option>
          <option v-for="u in collaborators" :key="u.id" :value="String(u.id)">
            {{ u.fullName }}
          </option>
        </select>
      </div>

      <!-- OPTIONS SECONDAIRES -->
      <div class="grid grid-cols-2 md:grid-cols-2 gap-4 mb-10 p-6 bg-slate-50 rounded-2xl border border-slate-100">
        <div>
          <label class="text-[10px] font-black text-slate-400 uppercase block mb-2">Statut</label>
          <select
              v-model="task.status"
              class="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs font-bold outline-none focus:ring-2 focus:ring-[#5b13ec]/20"
          >
            <option value="todo">À faire</option>
            <option value="in_progress">En cours</option>
            <option value="in_review">En révision</option>
            <option value="done">Terminé</option>
          </select>
        </div>

        <div>
          <label class="text-[10px] font-black text-slate-400 uppercase block mb-2">Priorité</label>
          <select
              v-model="task.priority"
              class="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs font-bold outline-none focus:ring-2 focus:ring-[#5b13ec]/20"
          >
            <option value="low">🟢 Low</option>
            <option value="medium">🟡 Medium</option>
            <option value="high">🔴 High</option>
            <option value="urgent">🔥 Urgent</option>
          </select>
        </div>
      </div>

      <!-- ACTIONS -->
      <div class="flex flex-col items-end gap-3 pt-6 border-t border-slate-100">
        <button
            @click="handleCreate"
            :disabled="submitting || !task.title || !selectedProjectId"
            class="w-full md:w-auto bg-[#5b13ec] text-white px-12 py-4 rounded-xl text-xs font-black shadow-xl shadow-purple-200 transition-all hover:bg-indigo-700 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="submitting">CRÉATION EN COURS...</span>
          <span v-else>CONFIRMER LA MISSION</span>
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TaskService, type TaskCreateUpdatePayload } from '@/services/TaskService'

const router = useRouter()
const submitting = ref(false)
const loadingFormData = ref(true)

// Liste des projets chargés depuis /form-data
const projects = ref<any[]>([])

// Collaborateurs du projet sélectionné
const collaborators = ref<any[]>([])

// ID du projet sélectionné (séparé de task pour gérer le changement)
const selectedProjectId = ref<number | null>(null)

const task = ref<TaskCreateUpdatePayload>({
  title: '',
  description: '',
  status: 'todo',
  priority: 'medium',
  dueDate: null,
  assignedTo: null,
  projectId: 0,
  epicId: null,
  levelId: null,
})

onMounted(async () => {
  try {
    const data = await TaskService.getFormData()
    // Notre API retourne { success, projects: [{ id, name, collaborators }] }
    projects.value = data.projects ?? []
  } catch (err) {
    console.error('Erreur chargement form-data:', err)
  } finally {
    loadingFormData.value = false
  }
})

const onProjectChange = () => {
  // Reset l'assigné quand on change de projet
  task.value.assignedTo = null

  if (selectedProjectId.value) {
    // Met à jour projectId dans le payload
    task.value.projectId = selectedProjectId.value

    // Trouve les collaborateurs du projet sélectionné (déjà chargés)
    const project = projects.value.find(p => p.id === selectedProjectId.value)
    collaborators.value = project?.collaborators ?? []
  } else {
    task.value.projectId = 0
    collaborators.value = []
  }
}

const handleCreate = async () => {
  if (!task.value.title || !selectedProjectId.value || submitting.value) return
  submitting.value = true
  try {
    await TaskService.create(task.value)
    router.push('/dashboardtask')
  } catch (err: any) {
    console.error('Erreur création tâche:', err)
    alert('Impossible de créer la tâche. Vérifiez la console.')
  } finally {
    submitting.value = false
  }
}
</script>