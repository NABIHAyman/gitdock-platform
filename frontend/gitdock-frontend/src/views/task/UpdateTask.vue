<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <!-- HEADER -->
    <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-6 shadow-xl">
      <div class="relative flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-black text-white">Edit Task</h1>
          <p class="text-white/60 text-xs mt-0.5">Modifiez les détails de votre mission.</p>
        </div>
        <button
            class="px-5 py-2.5 bg-white text-[#5b13ec] rounded-xl text-xs font-black shadow-lg hover:bg-purple-50"
            @click="router.push('/dashboardtask')"
        >
          RETOUR
        </button>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="bg-white rounded-2xl border border-slate-100 p-20 flex flex-col items-center gap-4">
      <div class="w-10 h-10 border-4 border-[#5b13ec] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-sm font-bold text-slate-400">Récupération des données...</p>
    </div>

    <!-- FORM -->
    <div v-else class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="p-8">

        <!-- Meta -->
        <div class="flex items-center justify-between mb-8 pb-4 border-b border-slate-50">
          <span class="px-3 py-1 bg-slate-100 text-slate-500 text-[10px] font-black rounded-lg">
            ID: #{{ route.params.id }}
          </span>
          <span v-if="taskInfo.updatedAt" class="text-[11px] text-slate-400 font-medium">
            Dernière modif : {{ formatDate(taskInfo.updatedAt) }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">

          <!-- Titre + Description -->
          <div class="md:col-span-2 space-y-6">
            <div>
              <label class="text-[11px] text-slate-400 font-black uppercase mb-1.5 block">Titre *</label>
              <input
                  v-model="formData.title"
                  type="text"
                  class="w-full px-0 py-2 border-0 border-b-2 border-slate-100 text-base font-bold text-slate-800 focus:border-[#5b13ec] outline-none bg-transparent"
              />
            </div>
            <div>
              <label class="text-[11px] text-slate-400 font-black uppercase mb-1.5 block">Description</label>
              <textarea
                  v-model="formData.description"
                  rows="4"
                  class="w-full px-4 py-3 rounded-xl border border-slate-100 text-sm bg-slate-50/50 resize-none outline-none focus:border-[#5b13ec]"
              ></textarea>
            </div>
            <div>
              <label class="text-[11px] text-slate-400 font-black uppercase mb-1.5 block">Date limite</label>
              <input
                  v-model="formData.dueDate"
                  type="date"
                  class="w-full px-0 py-2 border-0 border-b-2 border-slate-100 text-sm focus:border-[#5b13ec] outline-none bg-transparent"
              />
            </div>
          </div>

          <!-- Sidebar options -->
          <div class="space-y-6 bg-slate-50/50 p-6 rounded-2xl border border-slate-100">

            <div>
              <label class="text-[11px] text-slate-400 font-black uppercase mb-1.5 block">Statut</label>
              <select
                  v-model="formData.status"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm font-bold outline-none focus:ring-2 focus:ring-[#5b13ec]/20"
              >
                <option value="todo">À faire</option>
                <option value="in_progress">En cours</option>
                <option value="in_review">En révision</option>
                <option value="done">Terminé</option>
              </select>
            </div>

            <div>
              <label class="text-[11px] text-slate-400 font-black uppercase mb-1.5 block">Priorité</label>
              <select
                  v-model="formData.priority"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm font-bold outline-none focus:ring-2 focus:ring-[#5b13ec]/20"
              >
                <option value="low">🟢 Low</option>
                <option value="medium">🟡 Medium</option>
                <option value="high">🔴 High</option>
                <option value="urgent">🔥 Urgent</option>
              </select>
            </div>

            <div>
              <label class="text-[11px] text-slate-400 font-black uppercase mb-1.5 block">
                Assigné à
                <span v-if="teamMembers.length === 0" class="text-amber-500 ml-1 text-[9px]">(aucun collaborateur)</span>
              </label>
              <select
                  v-model="formData.assigneeId"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-[#5b13ec]/20"
              >
                <option :value="null">-- Non assigné --</option>
                <option v-for="u in teamMembers" :key="u.id" :value="u.id">
                  {{ u.fullName }}
                </option>
              </select>
            </div>

          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 mt-10 pt-6 border-t border-slate-100">
          <button
              @click="router.push('/dashboardtask')"
              class="px-8 py-3 text-xs font-bold text-slate-400 hover:text-slate-600"
          >
            Annuler
          </button>
          <button
              @click="handleUpdate"
              :disabled="submitting || !formData.title"
              class="px-10 py-3 rounded-xl bg-[#5b13ec] text-white text-xs font-black shadow-xl shadow-purple-200 hover:bg-indigo-700 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ submitting ? 'SAUVEGARDE...' : 'CONFIRMER LES MODIFICATIONS' }}
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { TaskService, type TaskCreateUpdatePayload } from '@/services/TaskService'

const router = useRouter()
const route  = useRoute()

const loading    = ref(true)
const submitting = ref(false)
const taskInfo   = ref<any>({})
const teamMembers = ref<any[]>([])

// ⚠️ NE PAS nommer la variable locale "formData" dans loadData
// car ça entre en conflit avec le ref ci-dessous
const formData = ref({
  title:      '',
  description:'',
  dueDate:    '',
  priority:   'medium',
  status:     'todo',
  assigneeId: null as number | null,
  epicId:     null as number | null,
  levelId:    null as number | null,
})

const formatDate = (d: string) => {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('fr-FR', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

const loadData = async () => {
  try {
    const id = Number(route.params.id)

    // ✅ Variables locales nommées différemment pour éviter le conflit
    const [task, fd] = await Promise.all([
      TaskService.getById(id),
      TaskService.getFormData()
    ])

    // Collaborateurs du projet de cette tâche
    const project = fd.projects.find((p: any) => p.id === task.projectId)
    teamMembers.value = project?.collaborators ?? []

    // assignedTo peut être un objet { id, firstName, lastName } ou un id direct
    const assigneeId = task.assignedTo
        ? (typeof task.assignedTo === 'object' ? task.assignedTo.id : Number(task.assignedTo))
        : null

    // ✅ On assigne au ref, pas à une variable locale
    formData.value = {
      title:       task.title       || '',
      description: task.description || '',
      dueDate:     task.dueDate ? String(task.dueDate).split('T')[0] : '',
      priority:    task.priority    || 'medium',
      status:      task.status      || 'todo',
      assigneeId,
      epicId:  null,
      levelId: null,
    }

    taskInfo.value = task

  } catch (err) {
    console.error('Erreur chargement tâche:', err)
    alert('Impossible de charger la tâche.')
  } finally {
    loading.value = false
  }
}

const handleUpdate = async () => {
  if (!formData.value.title || submitting.value) return
  submitting.value = true
  try {
    const payload: TaskCreateUpdatePayload = {
      title:       formData.value.title,
      description: formData.value.description,
      dueDate:     formData.value.dueDate || null,
      priority:    formData.value.priority,
      status:      formData.value.status,
      assignedTo:  formData.value.assigneeId ? String(formData.value.assigneeId) : null,
      projectId:   taskInfo.value.projectId || 0,
      epicId:      null,
      levelId:     null,
    }

    await TaskService.update(Number(route.params.id), payload)
    router.push('/dashboardtask')

  } catch (err) {
    console.error('Erreur update tâche:', err)
    alert('Impossible de sauvegarder les modifications.')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>