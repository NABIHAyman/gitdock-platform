<template>
  <div class="p-6 space-y-6">

    <!-- HEADER -->
    <div class="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl p-8 text-white flex items-center justify-between">
      <div>
        <p class="text-xs font-semibold tracking-widest uppercase opacity-80 mb-1">TASK MANAGEMENT</p>
        <h1 class="text-2xl font-bold">{{ isDevUser ? 'My Tasks' : 'Task Management' }}</h1>
        <p class="opacity-70 text-sm mt-1">
          {{ isDevUser ? 'Tasks assigned to you.' : "Manage your team's assignments by project." }}
        </p>
      </div>
      <button
          v-if="canCreateTask"
          @click="router.push('/dashboard/tasks/add')"
          class="flex items-center gap-2 px-4 py-2 bg-white/20 hover:bg-white/30 rounded-lg text-sm font-medium transition-colors"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        New Task
      </button>
    </div>

    <!-- STATS CARDS -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
        <p class="text-xs text-gray-500 uppercase font-semibold tracking-wide mb-1">Total</p>
        <p class="text-2xl font-bold text-gray-900">{{ stats.total }}</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
        <p class="text-xs text-gray-500 uppercase font-semibold tracking-wide mb-1">In Progress</p>
        <p class="text-2xl font-bold text-blue-600">{{ stats.inProgress }}</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
        <p class="text-xs text-gray-500 uppercase font-semibold tracking-wide mb-1">Completed</p>
        <p class="text-2xl font-bold text-green-600">{{ stats.done }}</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
        <p class="text-xs text-gray-500 uppercase font-semibold tracking-wide mb-1">Overdue</p>
        <p class="text-2xl font-bold text-red-600">{{ stats.overdue }}</p>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400 text-sm">Loading...</div>

    <!-- DEVELOPER VIEW -->
    <template v-else-if="isDevUser">
      <div v-if="tasks.length === 0" class="bg-white rounded-xl border border-gray-100 p-12 text-center text-gray-400 italic">
        No tasks assigned at the moment.
      </div>
      <div v-else class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full">
          <thead>
          <tr class="border-b border-gray-100 bg-slate-50">
            <th class="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Task</th>
            <th class="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Status</th>
            <th class="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Deadline</th>
            <th class="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Action</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="task in tasks" :key="task.id" class="border-b border-gray-50 hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4">
              <p class="text-sm font-medium text-gray-900">{{ task.title }}</p>
              <p v-if="task.description" class="text-xs text-gray-400 mt-0.5 truncate max-w-xs">{{ task.description }}</p>
              <p class="text-[10px] text-indigo-500 font-bold mt-1 uppercase">{{ getProjectName(task.projectId) }}</p>
            </td>
            <td class="px-6 py-4">
              <select
                  v-model="task.status"
                  @change="updateStatus(task)"
                  class="text-xs font-bold px-2 py-1 rounded-full border-0 outline-none cursor-pointer"
                  :class="{
                    'bg-green-100 text-green-700':   task.status === 'done',
                    'bg-blue-100 text-blue-700':     task.status === 'in_progress',
                    'bg-yellow-100 text-yellow-700': task.status === 'in_review',
                    'bg-gray-100 text-gray-600':     task.status === 'todo',
                  }"
              >
                <option value="todo">To Do</option>
                <option value="in_progress">In Progress</option>
                <option value="in_review">In Review</option>
                <option value="done">Completed</option>
              </select>
            </td>
            <td class="px-6 py-4">
                <span v-if="task.dueDate" class="text-sm" :class="isOverdue(task) ? 'text-red-600 font-semibold' : 'text-gray-600'">
                  {{ formatDate(task.dueDate) }}
                </span>
              <span v-else class="text-gray-400 text-sm">—</span>
            </td>
            <td class="px-6 py-4 text-right">
              <span v-if="updatingId === task.id" class="text-xs text-indigo-500 font-medium">Updating...</span>
              <span v-else-if="task.status === 'done'" class="inline-flex items-center gap-1 px-3 py-1.5 bg-gray-100 text-gray-500 text-xs font-medium rounded-lg">
                  ✅ Done
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- MANAGER / ADMIN VIEW -->
    <template v-else>
      <div v-if="tasksByProject.length === 0" class="bg-white rounded-xl border border-gray-100 p-12 text-center text-gray-400 italic">
        No tasks found.
      </div>

      <div v-for="group in tasksByProject" :key="group.projectId" class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden mb-8">
        <div class="px-6 py-4 bg-slate-50 border-b border-gray-100 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-100">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
              </svg>
            </div>
            <div>
              <h3 class="text-sm font-black text-gray-900 uppercase tracking-widest">{{ group.projectName }}</h3>
              <p class="text-[10px] text-gray-400 font-bold uppercase tracking-tighter">{{ group.tasks.length }} task{{ group.tasks.length > 1 ? 's' : '' }} total</p>
            </div>
          </div>
          <div class="flex gap-2">
            <span class="text-[10px] px-3 py-1 bg-blue-100 text-blue-700 rounded-lg font-black uppercase">
              {{ group.tasks.filter(t => t.status === 'in_progress').length }} in progress
            </span>
            <span class="text-[10px] px-3 py-1 bg-green-100 text-green-700 rounded-lg font-black uppercase">
              {{ group.tasks.filter(t => t.status === 'done').length }} completed
            </span>
          </div>
        </div>

        <table class="w-full">
          <thead>
          <tr class="border-b border-gray-50">
            <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Task</th>
            <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Status</th>
            <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Assigned To</th>
            <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Deadline</th>
            <th class="text-right px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Actions</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
          <tr v-for="task in group.tasks" :key="task.id" class="hover:bg-slate-50/50 transition-colors">
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <span v-if="isOverdue(task)" class="w-2 h-2 bg-red-500 rounded-full animate-pulse shadow-sm shadow-red-200"/>
                <div>
                  <p class="text-sm font-bold text-gray-900 leading-none">{{ task.title }}</p>
                  <p v-if="task.description" class="text-xs text-gray-400 mt-1.5 truncate max-w-xs">{{ task.description }}</p>
                </div>
              </div>
            </td>
            <td class="px-6 py-4">
              <span class="inline-flex items-center px-2 py-1 rounded-md text-[10px] font-black uppercase tracking-tighter"
                    :class="{
                    'bg-green-100 text-green-700':   task.status === 'done',
                    'bg-blue-100 text-blue-700':     task.status === 'in_progress',
                    'bg-yellow-100 text-yellow-700': task.status === 'in_review',
                    'bg-gray-100 text-gray-600':     task.status === 'todo',
                  }">
                {{ statusLabel(task.status) }}
              </span>
            </td>
            <td class="px-6 py-4">
              <template v-if="task.assignedTo">
                <div class="flex items-center gap-2">
                  <div class="w-7 h-7 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center text-[10px] font-black ring-2 ring-white">
                    {{ assignedInitial(task.assignedTo) }}
                  </div>
                  <span class="text-xs font-semibold text-gray-700">{{ assignedName(task.assignedTo) }}</span>
                </div>
              </template>
              <span v-else class="text-gray-300 text-xs">—</span>
            </td>
            <td class="px-6 py-4">
              <span v-if="task.dueDate" class="text-xs font-bold" :class="isOverdue(task) ? 'text-red-500' : 'text-gray-500'">
                {{ formatDate(task.dueDate) }}
                <span v-if="isOverdue(task)" class="ml-1 text-[9px] uppercase">(overdue)</span>
              </span>
              <span v-else class="text-gray-300 text-xs">—</span>
            </td>
            <td class="px-6 py-4 text-right">
              <div class="flex items-center justify-end gap-2">
                <button @click="openEditModal(task)" class="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-all">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                </button>
                <button @click="confirmDelete(task)" class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- EDIT MODAL -->
    <Teleport to="body">
      <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="closeEditModal">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
          <div class="bg-gradient-to-r from-[#5b13ec] to-violet-700 rounded-t-2xl p-6 flex items-center justify-between">
            <div>
              <h2 class="text-lg font-black text-white uppercase tracking-widest">Edit Task</h2>
              <p class="text-white/60 text-[10px] font-bold uppercase mt-0.5 tracking-tighter">Unique ID #{{ editingTask?.id }}</p>
            </div>
            <button @click="closeEditModal" class="text-white/70 hover:text-white transition">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="p-8 space-y-6">
            <div v-if="modalLoading" class="flex justify-center py-8">
              <div class="w-8 h-8 border-4 border-[#5b13ec] border-t-transparent rounded-full animate-spin"></div>
            </div>
            <template v-else>
              <div>
                <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Task Title *</label>
                <input v-model="editForm.title" type="text" class="w-full border-b-2 border-slate-100 py-3 focus:border-[#5b13ec] outline-none text-base font-bold bg-transparent transition-colors placeholder:text-slate-300" placeholder="Ex: API Integration..."/>
              </div>
              <div>
                <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Notes & Details</label>
                <textarea v-model="editForm.description" rows="3" class="w-full border border-slate-100 rounded-xl px-4 py-3 text-sm bg-slate-50/50 resize-none outline-none focus:border-[#5b13ec] focus:bg-white transition-all"></textarea>
              </div>
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Progress</label>
                  <select v-model="editForm.status" class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 text-xs font-black uppercase outline-none focus:border-[#5b13ec] focus:bg-white transition-all">
                    <option value="todo">To Do</option>
                    <option value="in_progress">In Progress</option>
                    <option value="in_review">In Review</option>
                    <option value="done">Completed</option>
                  </select>
                </div>
                <div>
                  <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Priority</label>
                  <select v-model="editForm.priority" class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 text-xs font-black uppercase outline-none focus:border-[#5b13ec] focus:bg-white transition-all">
                    <option value="low">🟢 Low</option>
                    <option value="medium">🟡 Moderate</option>
                    <option value="high">🔴 High</option>
                    <option value="urgent">🔥 Critical</option>
                  </select>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Due Date</label>
                  <input v-model="editForm.dueDate" type="date" class="w-full border-b-2 border-slate-100 py-2.5 text-sm font-bold outline-none focus:border-[#5b13ec] bg-transparent"/>
                </div>
                <div>
                  <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Assignee</label>
                  <select v-model="editForm.assigneeId" class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 text-xs font-bold outline-none focus:border-[#5b13ec] focus:bg-white transition-all">
                    <option :value="null">— Not assigned —</option>
                    <option v-for="u in modalCollaborators" :key="u.id" :value="u.id">{{ u.fullName }}</option>
                  </select>
                </div>
              </div>
            </template>
          </div>
          <div class="px-8 pb-8 pt-4 flex justify-end gap-4 border-t border-slate-50">
            <button @click="closeEditModal" class="px-6 py-3 text-[10px] font-black text-slate-400 hover:text-slate-600 uppercase tracking-widest transition-colors">Cancel</button>
            <button @click="handleUpdate" :disabled="submitting || !editForm.title || modalLoading"
                    class="px-10 py-3 rounded-xl bg-[#5b13ec] text-white text-[10px] font-black uppercase tracking-widest shadow-xl shadow-indigo-100 hover:bg-indigo-700 active:scale-95 transition-all disabled:opacity-50">
              {{ submitting ? 'Processing...' : 'Save Changes' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- DELETE MODAL -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="closeDeleteModal">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
          <div class="h-1.5 bg-red-500"></div>
          <div class="p-8">
            <div class="flex items-center gap-4 mb-6">
              <div class="w-12 h-12 rounded-2xl bg-red-50 flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/></svg>
              </div>
              <div>
                <h2 class="text-lg font-black text-slate-800 uppercase tracking-tight">Delete Task?</h2>
                <p class="text-xs text-slate-400 font-medium">This action can be undone later.</p>
              </div>
            </div>
            <div class="px-5 py-4 bg-slate-50 rounded-2xl border border-slate-100 mb-8">
              <p class="text-[9px] text-slate-400 font-black uppercase tracking-widest mb-1">Target</p>
              <p class="text-sm font-black text-slate-800">{{ deletingTask?.title }}</p>
            </div>
            <div class="flex items-center justify-end gap-3">
              <button @click="closeDeleteModal" class="px-6 py-3 text-[10px] font-black text-slate-400 hover:text-slate-600 uppercase tracking-widest">Keep It</button>
              <button @click="handleDelete" :disabled="deleting"
                      class="px-8 py-3 rounded-xl bg-red-500 text-white text-[10px] font-black uppercase tracking-widest shadow-xl shadow-red-100 hover:bg-red-600 active:scale-95 transition-all disabled:opacity-50">
                {{ deleting ? '...' : 'Confirm' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useRole } from '@/composables/useRole'
import { TaskService, type TaskCreateUpdatePayload } from '@/services/TaskService'
import { useNotificationStore } from '@/stores/notificationStore'

const router     = useRouter()
const authStore  = useAuthStore()
const { isDevUser, canCreateTask } = useRole()
const notifStore = useNotificationStore()

// ─── State ────────────────────────────────────────────────────────────────
const tasks        = ref<any[]>([])
const allProjects  = ref<any[]>([])
const loading      = ref(true)
const updatingId   = ref<number | null>(null)

// ─── Modals State ─────────────────────────────────────────────────────────
const showEditModal      = ref(false)
const modalLoading       = ref(false)
const submitting         = ref(false)
const editingTask        = ref<any>(null)
const modalCollaborators = ref<any[]>([])
const showDeleteModal    = ref(false)
const deletingTask       = ref<any>(null)
const deleting           = ref(false)

const editForm = ref({
  title:      '',
  description:'',
  dueDate:    '',
  priority:   'medium',
  status:     'todo',
  assigneeId: null as number | null,
})

// ─── Stats ────────────────────────────────────────────────────────────────
const stats = computed(() => {
  const now = new Date()
  return {
    total:      tasks.value.length,
    inProgress: tasks.value.filter(t => t.status === 'in_progress').length,
    done:       tasks.value.filter(t => t.status === 'done').length,
    overdue:    tasks.value.filter(t => t.status !== 'done' && t.dueDate && new Date(t.dueDate) < now).length,
  }
})

// ─── Project Name Logic ───────────────────────────────────────────────────
const getProjectName = (projectId: number | string | null) => {
  if (!projectId || projectId === 'sans-projet') return 'Unassigned'
  const found = allProjects.value.find(p => String(p.id) === String(projectId))
  return found ? found.name : `Project #${projectId}`
}

// ─── Grouping by Project ──────────────────────────────────────────────────
const tasksByProject = computed(() => {
  const map = new Map<number | string, { projectId: number | string; projectName: string; tasks: any[] }>()

  for (const task of tasks.value) {
    const key = task.projectId ?? 'sans-projet'
    if (!map.has(key)) {
      map.set(key, {
        projectId:   key,
        projectName: getProjectName(key),
        tasks:       [],
      })
    }
    map.get(key)!.tasks.push(task)
  }
  return Array.from(map.values())
})

// ─── Helpers ──────────────────────────────────────────────────────────────
const isOverdue = (task: any) => {
  if (!task.dueDate || task.status === 'done') return false
  return new Date(task.dueDate) < new Date()
}

const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' })

const statusLabel = (s: string) => ({
  done: 'Completed', in_progress: 'In Progress', in_review: 'In Review', todo: 'To Do',
} as Record<string, string>)[s] ?? s

const assignedName = (a: any): string => {
  if (!a) return '—'
  if (typeof a === 'object') {
    if (a.fullName) return a.fullName
    if (a.firstName || a.lastName) return `${a.firstName ?? ''} ${a.lastName ?? ''}`.trim()
  }
  return String(a)
}
const assignedInitial = (a: any): string => assignedName(a).charAt(0).toUpperCase()

// ─── Lifecycle ────────────────────────────────────────────────────────────
onMounted(async () => {
  try {
    tasks.value = isDevUser.value && authStore.userId
        ? await TaskService.getByAssignee(authStore.userId)
        : await TaskService.list()

    const formData = await TaskService.getFormData()
    allProjects.value = formData.projects || []

  } catch (e) {
    notifStore.error('Sync failed.')
  } finally {
    loading.value = false
  }
})

// ─── Actions ──────────────────────────────────────────────────────────────
const updateStatus = async (task: any) => {
  updatingId.value = task.id
  try {
    const payload: TaskCreateUpdatePayload = {
      title:       task.title,
      description: task.description || '',
      dueDate:     task.dueDate ? String(task.dueDate).split('T')[0] : null,
      priority:    task.priority || 'medium',
      status:      task.status,
      assignedTo:  task.assignedTo ? (typeof task.assignedTo === 'object' ? String(task.assignedTo.id) : String(task.assignedTo)) : null,
      projectId:   task.projectId || 0,
      epicId:      null,
      levelId:     null,
    }
    await TaskService.update(task.id, payload)
    notifStore.success('Status updated.')
  } catch (e) {
    notifStore.error('Update failed.')
  } finally {
    updatingId.value = null
  }
}

const openEditModal = async (task: any) => {
  editingTask.value   = task
  showEditModal.value = true
  modalLoading.value  = true

  const assigneeId = task.assignedTo ? (typeof task.assignedTo === 'object' ? task.assignedTo.id : Number(task.assignedTo)) : null

  editForm.value = {
    title:       task.title || '',
    description: task.description || '',
    dueDate:     task.dueDate ? String(task.dueDate).split('T')[0] : '',
    priority:    task.priority || 'medium',
    status:      task.status || 'todo',
    assigneeId,
  }

  try {
    const project = allProjects.value.find((p: any) => p.id === task.projectId)
    modalCollaborators.value = project?.collaborators ?? []
  } catch (e) {
    modalCollaborators.value = []
  } finally {
    modalLoading.value = false
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingTask.value = null
}

const handleUpdate = async () => {
  if (!editForm.value.title || submitting.value) return
  submitting.value = true
  try {
    const payload: TaskCreateUpdatePayload = {
      title:       editForm.value.title,
      description: editForm.value.description,
      dueDate:     editForm.value.dueDate || null,
      priority:    editForm.value.priority,
      status:      editForm.value.status,
      assignedTo:  editForm.value.assigneeId ? String(editForm.value.assigneeId) : null,
      projectId:   editingTask.value?.projectId || 0,
      epicId:      null,
      levelId:     null,
    }
    await TaskService.update(editingTask.value.id, payload)

    const idx = tasks.value.findIndex(t => t.id === editingTask.value.id)
    if (idx !== -1) {
      const collaborator = modalCollaborators.value.find(u => u.id === editForm.value.assigneeId)
      tasks.value[idx] = {
        ...tasks.value[idx],
        ...payload,
        assignedTo: collaborator || tasks.value[idx].assignedTo
      }
    }
    notifStore.success('Changes saved.')
    closeEditModal()
  } catch (e) {
    notifStore.error('Error during save.')
  } finally {
    submitting.value = false
  }
}

const confirmDelete = (task: any) => {
  deletingTask.value = task
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
}

const handleDelete = async () => {
  if (!deletingTask.value || deleting.value) return
  deleting.value = true
  try {
    await TaskService.softDelete(deletingTask.value.id)
    tasks.value = tasks.value.filter(t => t.id !== deletingTask.value.id)
    notifStore.success('Task deleted.')
    closeDeleteModal()
  } catch (e) {
    notifStore.error('Deletion failed.')
  } finally {
    deleting.value = false
  }
}
</script>