<template>
  <div class="space-y-4 max-w-4xl mx-auto">
    <!-- HEADER COMPACT -->
    <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-5 shadow-lg">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 right-0 w-48 h-48 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
      </div>
      <div class="relative flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <div class="w-7 h-7 bg-white/20 rounded-lg flex items-center justify-center">
              <v-icon icon="mdi-plus-circle-outline" color="white" size="16"></v-icon>
            </div>
            <span class="text-white/70 text-[10px] font-bold uppercase tracking-widest">Task Management</span>
          </div>
          <h1 class="text-xl font-black text-white">Add New Task</h1>
        </div>
        <button
            @click="router.push('/dashboardtask')"
            class="flex items-center gap-2 px-4 py-2 bg-white text-[#5b13ec] rounded-xl text-[11px] font-black shadow hover:bg-purple-50 transition-all"
        >
          <v-icon icon="mdi-arrow-left" size="14"></v-icon>
          RETOUR
        </button>
      </div>
    </div>

    <!-- FORM CARD -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="h-1.5 bg-gradient-to-r from-[#5b13ec] to-violet-500"></div>

      <div class="p-6">
        <!-- Title & Status -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-5 mb-5">
          <div class="md:col-span-2">
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Task Title</label>
            <input
                v-model="task.title"
                type="text"
                placeholder="Nom de la tâche..."
                class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm font-medium focus:outline-none focus:border-[#5b13ec] transition-colors bg-transparent"
            />
          </div>
          <div>
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Status</label>
            <select
                v-model="task.status"
                class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent cursor-pointer"
            >
              <option value="To Do">To Do</option>
              <option value="In Progress">In Progress</option>
              <option value="Done">Done</option>
            </select>
          </div>
        </div>

        <!-- Description -->
        <div class="mb-5">
          <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Description</label>
          <textarea
              v-model="task.description"
              rows="2"
              placeholder="Détails de la mission..."
              class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent resize-none"
          ></textarea>
        </div>

        <!-- Date & Assignee -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
          <div>
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Due Date</label>
            <input
                v-model="task.dueDate"
                type="date"
                class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent"
            />
          </div>
          <div>
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Assigned To</label>
            <select
                v-model="task.assignedTo"
                class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent"
            >
              <option :value="null">Unassigned</option>
              <option v-for="u in formData.users" :key="u.id" :value="u.id">{{ u.fullName }}</option>
            </select>
          </div>
        </div>

        <!-- Classification Grid (Epic, Part, Level) -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-5 mb-6">
          <div>
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Epic</label>
            <select v-model="task.epic" class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent">
              <option :value="null">None</option>
              <option v-for="e in formData.epics" :key="e.id" :value="e.id">{{ e.title }}</option>
            </select>
          </div>
          <div>
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Part</label>
            <select v-model="task.part" class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent">
              <option :value="null">None</option>
              <option v-for="p in formData.parts" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div>
            <label class="text-[11px] text-slate-400 font-bold uppercase mb-1 block">Level</label>
            <select v-model="task.level" class="w-full px-0 py-2 border-0 border-b border-slate-200 text-sm focus:outline-none focus:border-[#5b13ec] bg-transparent">
              <option :value="null">None</option>
              <option v-for="l in formData.levels" :key="l.id" :value="l.id">{{ l.name }}</option>
            </select>
          </div>
        </div>

        <!-- Priority -->
        <div class="mb-8">
          <label class="text-[11px] text-slate-400 font-bold uppercase mb-3 block">Priority</label>
          <div class="flex items-center gap-3">
            <button
                v-for="p in priorities"
                :key="p.value"
                type="button"
                @click="task.priority = p.value"
                :class="[
                'px-4 py-1.5 rounded-full text-[10px] font-black transition-all border-2',
                task.priority === p.value ? `${p.bg} text-white border-transparent shadow-md scale-105` : `bg-white border-slate-100 text-slate-400 hover:border-slate-200`
              ]"
            >
              {{ p.label }}
            </button>
          </div>
        </div>

        <!-- FOOTER ACTIONS -->
        <div class="flex items-center justify-end gap-3 pt-6 border-t border-slate-50">
          <button
              type="button"
              @click="router.push('/dashboardtask')"
              class="px-6 py-2.5 rounded-xl text-xs font-bold text-slate-400 hover:bg-slate-50 transition-all"
          >
            Cancel
          </button>
          <button
              @click="handleCreate"
              :disabled="submitting || !task.title"
              class="flex items-center gap-2 px-8 py-2.5 rounded-xl bg-[#5b13ec] text-white text-xs font-black shadow-lg hover:bg-violet-700 disabled:opacity-40 transition-all"
          >
            <v-icon v-if="submitting" icon="mdi-loading" size="14" class="animate-spin"></v-icon>
            {{ submitting ? 'En cours...' : 'CONFIRM' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TaskService, type TaskCreateUpdatePayload, type TaskFormDataDTO } from '@/services/TaskService'

const router = useRouter()
const submitting = ref(false)

const task = ref<TaskCreateUpdatePayload>({
  title: '',
  description: '',
  status: 'To Do',
  priority: 'Medium',
  dueDate: null,
  assignedTo: null,
  assignedBy: null,
  projectId: null,
  epic: null,
  part: null,
  level: null
})

const formData = ref<TaskFormDataDTO>({
  epics: [],
  parts: [],
  levels: [],
  users: []
})

const priorities = [
  { value: 'Low', label: 'LOW', bg: 'bg-green-500' },
  { value: 'Medium', label: 'MEDIUM', bg: 'bg-amber-500' },
  { value: 'High', label: 'HIGH', bg: 'bg-red-500' }
]

const loadData = async () => {
  try {
    formData.value = await TaskService.getFormData()
  } catch (err) {
    console.error('Failed to load form data', err)
  }
}

const handleCreate = async () => {
  if (!task.value.title) return
  submitting.value = true
  try {
    await TaskService.create(task.value)
    router.push('/dashboardtask')
  } catch (err) {
    console.error('Creation failed', err)
    alert('Erreur lors de la création de la tâche.')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23cbd5e1' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0px center;
  background-size: 14px;
}
</style>