<template>
  <div class="space-y-6">

    <!-- HEADER -->
    <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-6 shadow-xl">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 right-0 w-64 h-64 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
      </div>
      <div class="relative flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <div class="w-8 h-8 bg-white/20 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-pencil-outline" color="white" size="18"></v-icon>
            </div>
            <span class="text-white/70 text-xs font-bold uppercase tracking-widest">Task Management</span>
          </div>
          <h1 class="text-2xl font-black text-white">Edit Task</h1>
          <p class="text-white/60 text-xs mt-0.5">Modifiez les détails et réassignez si nécessaire.</p>
        </div>
        <button
            class="flex items-center gap-2 px-5 py-2.5 bg-white text-[#5b13ec] rounded-xl text-xs font-black shadow-lg hover:bg-purple-50 transition-all"
            @click="router.push('/dashboardtask')"
        >
          <v-icon icon="mdi-arrow-left" size="16"></v-icon>
          RETOUR
        </button>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="bg-white rounded-2xl border border-slate-100 p-16 flex flex-col items-center gap-4">
      <div class="w-12 h-12 rounded-full border-4 border-purple-100 border-t-[#5b13ec] animate-spin"></div>
      <p class="text-sm font-bold text-slate-400">Chargement...</p>
    </div>

    <!-- MODAL CARD -->
    <div v-else class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">

      <!-- Barre mauve top -->
      <div class="h-1 bg-gradient-to-r from-[#5b13ec] to-violet-500"></div>

      <div class="p-8 max-w-2xl mx-auto">

        <!-- Titre + meta -->
        <div class="flex items-center justify-between mb-8">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-xl bg-[#5b13ec] flex items-center justify-center">
              <v-icon icon="mdi-pencil" color="white" size="18"></v-icon>
            </div>
            <h2 class="text-xl font-black text-slate-800">Edit Task</h2>
          </div>
          <span v-if="taskInfo.updatedAt" class="text-xs text-slate-400 flex items-center gap-1">
            <v-icon icon="mdi-clock-outline" size="13"></v-icon>
            {{ formatDate(taskInfo.updatedAt) }}
          </span>
        </div>

        <!-- Ligne 1 : Title + Status -->
        <div class="grid grid-cols-2 gap-6 mb-6">
          <div>
            <label class="text-xs text-slate-400 font-medium mb-1.5 block">Task Title</label>
            <input
                v-model="formData.title"
                type="text"
                placeholder="Nom de la tâche"
                class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 placeholder-slate-300 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent"
            />
          </div>
          <div>
            <label class="text-xs text-slate-400 font-medium mb-1.5 block">Status</label>
            <select
                v-model="formData.status"
                class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent appearance-none cursor-pointer"
            >
              <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>
        </div>

        <!-- Description -->
        <div class="mb-6">
          <label class="text-xs text-slate-400 font-medium mb-1.5 block">Description</label>
          <textarea
              v-model="formData.description"
              placeholder="Décrivez les objectifs..."
              rows="3"
              class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 placeholder-slate-300 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent resize-none"
          ></textarea>
        </div>

        <!-- Due Date + Assignee -->
        <div class="grid grid-cols-2 gap-6 mb-6">
          <div>
            <label class="text-xs text-slate-400 font-medium mb-1.5 block">Due Date</label>
            <input
                v-model="formData.dueDate"
                type="date"
                class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent"
            />
          </div>
          <div>
            <label class="text-xs text-slate-400 font-medium mb-1.5 block">Assigned To</label>
            <select
                v-model="formData.assigneeId"
                class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent appearance-none cursor-pointer"
            >
              <option value="">-- Sélectionner --</option>
              <option v-for="u in teamMembers" :key="u.id" :value="u.id">{{ u.fullName }}</option>
            </select>
          </div>
        </div>

        <!-- Epic + Part -->
        <div class="grid grid-cols-2 gap-6 mb-6">
          <div>
            <label class="text-xs text-slate-400 font-medium mb-1.5 block">Epic</label>
            <select
                v-model="formData.epicId"
                class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent appearance-none cursor-pointer"
            >
              <option value="">-- Sélectionner --</option>
              <option v-for="e in epics" :key="e.id" :value="e.id">{{ e.title }}</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-400 font-medium mb-1.5 block">Part</label>
            <select
                v-model="formData.partId"
                class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent appearance-none cursor-pointer"
            >
              <option value="">-- Sélectionner --</option>
              <option v-for="p in parts" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
        </div>

        <!-- Level -->
        <div class="mb-6">
          <label class="text-xs text-slate-400 font-medium mb-1.5 block">Level</label>
          <select
              v-model="formData.levelId"
              class="w-full px-0 py-2 border-0 border-b-2 border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent appearance-none cursor-pointer"
          >
            <option value="">-- Sélectionner --</option>
            <option v-for="l in levels" :key="l.id" :value="l.id">{{ l.name }}</option>
          </select>
        </div>

        <!-- Priority picker -->
        <div class="mb-8">
          <label class="text-xs text-slate-400 font-medium mb-3 block">Priority</label>
          <div class="flex items-center gap-2">
            <button
                v-for="p in priorities"
                :key="p.value"
                type="button"
                @click="formData.priority = p.value"
                :title="p.label"
                :class="[
                'w-9 h-9 rounded-full transition-all border-2 flex items-center justify-center text-white text-xs font-black',
                formData.priority === p.value
                  ? `${p.bg} border-transparent scale-110 shadow-lg`
                  : `${p.bg} border-white opacity-50 hover:opacity-90 hover:scale-105`
              ]"
            >
              {{ p.label[0] }}
            </button>
            <div class="w-px h-6 bg-slate-200 mx-1"></div>
            <div class="flex items-center gap-1.5 px-3 py-1.5 bg-slate-50 rounded-lg border border-slate-200">
              <span class="text-xs font-bold text-slate-500">Selected:</span>
              <span :class="['text-xs font-black', selectedPriority.textClass]">{{ formData.priority }}</span>
            </div>
          </div>
        </div>

        <!-- ACTIONS -->
        <div class="flex items-center justify-between pt-6 border-t border-slate-100">
          <button
              type="button"
              @click="router.push('/dashboardtask')"
              class="px-8 py-3 rounded-xl text-sm font-bold text-slate-500 hover:text-slate-700 hover:bg-slate-50 transition-all"
          >
            Cancel
          </button>
          <button
              type="button"
              @click="updateTask"
              :disabled="submitting || !formData.title"
              class="flex items-center gap-2 px-10 py-3 rounded-xl bg-[#5b13ec] text-white text-sm font-black shadow-lg hover:bg-violet-700 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <v-icon v-if="submitting" icon="mdi-loading" size="16" class="animate-spin"></v-icon>
            {{ submitting ? 'Mise à jour...' : 'Confirm' }}
          </button>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { TaskService, type TaskListItemDTO, type TaskCreateUpdatePayload } from '@/services/TaskService'

const router = useRouter()
const route  = useRoute()

const loading    = ref(true)
const submitting = ref(false)

const formData = ref({
  title:       '',
  description: '',
  dueDate:     '',
  priority:    'Medium',
  status:      'To Do',
  assigneeId:  '' as number | '',
  epicId:      '' as number | '',
  partId:      '' as number | '',
  levelId:     '' as number | '',
})

const taskInfo      = ref<any>({ id: null, updatedAt: null })
const statusOptions = ['To Do', 'In Progress', 'Done']

const priorities = [
  { value: 'Low',    label: 'Low',    bg: 'bg-green-500', textClass: 'text-green-600' },
  { value: 'Medium', label: 'Medium', bg: 'bg-amber-500', textClass: 'text-amber-600' },
  { value: 'High',   label: 'High',   bg: 'bg-red-500',   textClass: 'text-red-600'   },
]

const selectedPriority = computed(() =>
    priorities.find(p => p.value === formData.value.priority) ?? priorities[1]
)

const teamMembers = ref<Array<{ id: number; fullName: string }>>([])
const epics       = ref<Array<{ id: number; title: string }>>([])
const parts       = ref<Array<{ id: number; name: string }>>([])
const levels      = ref<Array<{ id: number; name: string }>>([])

const fetchReferenceData = async (): Promise<void> => {
  try {
    const data        = await TaskService.getFormData()
    teamMembers.value = data.users  || []
    epics.value       = data.epics  || []
    parts.value       = data.parts  || []
    levels.value      = data.levels || []
  } catch (error) { console.error(error) }
}

const fetchTaskDetails = async (id: string | number): Promise<void> => {
  try {
    const task = await TaskService.getById(id) as TaskListItemDTO
    formData.value = {
      title:       task.title       ?? '',
      description: task.description ?? '',
      dueDate:     task.dueDate     ? String(task.dueDate).split('T')[0] : '',
      priority:    task.priority    || 'Medium',
      status:      task.status      || 'To Do',
      assigneeId:  task.assignedTo  != null ? Number(task.assignedTo) : '',
      epicId:      task.epic        != null ? Number(task.epic)       : '',
      partId:      task.part        != null ? Number(task.part)       : '',
      levelId:     task.level       != null ? Number(task.level)      : '',
    }
    taskInfo.value = task
  } catch (error) { console.error(error) }
}

const updateTask = async (): Promise<void> => {
  if (!formData.value.title) return
  submitting.value = true
  try {
    const payload: TaskCreateUpdatePayload = {
      title:       formData.value.title,
      description: formData.value.description,
      status:      formData.value.status,
      priority:    formData.value.priority,
      dueDate:     formData.value.dueDate || null,
      assignedTo:  formData.value.assigneeId ? Number(formData.value.assigneeId) : null,
      epic:        formData.value.epicId     ? Number(formData.value.epicId)     : null,
      part:        formData.value.partId     ? Number(formData.value.partId)     : null,
      level:       formData.value.levelId    ? Number(formData.value.levelId)    : null,
    }
    await TaskService.update(taskInfo.value.id, payload)
    alert('Tâche mise à jour ✅')
    router.push('/dashboardtask')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const formatDate = (d: any): string => d ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''

onMounted(async () => {
  await fetchReferenceData()
  if (route.params.id) await fetchTaskDetails(route.params.id as string)
  loading.value = false
})
</script>

<style scoped>
select { background-image: none; }
</style>