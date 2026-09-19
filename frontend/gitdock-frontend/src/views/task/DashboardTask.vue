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
              <v-icon icon="mdi-clipboard-text-outline" color="white" size="18"></v-icon>
            </div>
            <span class="text-white/70 text-xs font-bold uppercase tracking-widest">Workspace</span>
          </div>
          <h1 class="text-2xl font-black text-white">Task Management</h1>
          <p class="text-white/60 text-xs mt-0.5">Gérez vos tâches et suivez l'avancement de votre équipe.</p>
        </div>
        <router-link to="/addtask" class="no-underline">
          <button class="flex items-center gap-2 px-5 py-2.5 bg-white text-[#5b13ec] rounded-xl text-xs font-black shadow-lg hover:bg-purple-50 transition-all">
            <v-icon icon="mdi-plus" size="16"></v-icon>
            ASSIGN TASK
          </button>
        </router-link>
      </div>
    </div>

    <!-- STATS GRID -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div v-for="stat in stats" :key="stat.title"
           class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
        <div :class="['w-12 h-12 rounded-xl flex items-center justify-center', stat.bgClass]">
          <v-icon :icon="stat.icon" :color="stat.iconColor" size="24"></v-icon>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-bold uppercase tracking-wider">{{ stat.title }}</p>
          <h3 class="text-2xl font-black text-slate-800">{{ stat.value }}</h3>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[450px]">
      <div class="p-6 border-b border-slate-100 flex justify-between items-center">
        <h2 class="text-lg font-black text-slate-800">Recent Activity</h2>
        <v-btn to="/TaskAll" variant="text" color="deep-purple" class="text-xs font-bold">View All</v-btn>
      </div>

      <v-table class="px-4 pb-4">
        <thead>
        <tr class="text-slate-400 text-xs font-black uppercase">
          <th class="py-4">Task</th>
          <th>Status</th>
          <th>Assigned To</th>
          <th>Deadline</th>
          <th class="text-right">Options</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(task, index) in tasks" :key="task.id" class="hover:bg-purple-50/30 transition-colors">
          <td class="py-4 font-bold text-slate-700">{{ task.name }}</td>
          <td>
            <v-chip :color="task.chipColor" size="x-small" class="font-black px-3 rounded-lg" variant="flat">
              {{ task.status.toUpperCase() }}
            </v-chip>
          </td>
          <td class="text-sm text-slate-600 font-medium">{{ task.user }}</td>
          <td class="text-sm text-slate-500 italic">{{ task.time }}</td>
          <td class="text-right">
            <div class="flex justify-end gap-1">
              <v-btn icon="mdi-pencil" size="x-small" color="deep-purple-lighten-4" variant="flat"
                     class="rounded-lg" @click="editTask(index)"></v-btn>
              <v-btn icon="mdi-delete" size="x-small" color="red-lighten-4" variant="flat"
                     class="rounded-lg" @click="goToDelete(index)"></v-btn>
            </div>
          </td>
        </tr>
        </tbody>
      </v-table>

      <div v-if="tasks.length === 0" class="flex flex-col items-center justify-center py-20 text-slate-400">
        <v-icon icon="mdi-clipboard-off-outline" size="48" class="mb-2 opacity-20"></v-icon>
        <p class="text-sm font-bold italic">Aucune tâche trouvée.</p>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TaskService, type TaskListItemDTO } from '@/services/TaskService'

const router = useRouter()
const tasks  = ref<any[]>([])

const stats = ref([
  { title: 'Total Tasks', value: 0, icon: 'mdi-format-list-bulleted', iconColor: '#5b13ec', bgClass: 'bg-purple-100' },
  { title: 'In Progress', value: 0, icon: 'mdi-progress-clock',       iconColor: '#6d28d9', bgClass: 'bg-violet-100' },
  { title: 'Completed',   value: 0, icon: 'mdi-check-circle',          iconColor: '#22c55e', bgClass: 'bg-green-100'  },
  { title: 'Overdue',     value: 0, icon: 'mdi-alert-circle',          iconColor: '#ef4444', bgClass: 'bg-red-100'    },
])

const statusChipColor = (status: string) => {
  if (status === 'Done')        return 'success'
  if (status === 'In Progress') return 'deep-purple'
  if (status === 'Overdue')     return 'error'
  return 'warning'
}

const fetchTasks = async () => {
  try {
    const data = (await TaskService.list()) as TaskListItemDTO[]
    tasks.value = data.map((t: any) => ({
      id:        t.id,
      name:      t.title    ?? '—',
      status:    t.status   ?? 'To Do',
      chipColor: statusChipColor(t.status ?? ''),
      user:      t.assignedTo != null ? String(t.assignedTo) : 'Unassigned',
      time:      t.dueDate  ? new Date(t.dueDate).toLocaleDateString() : '—',
    }))
    updateStats()
  } catch (error) {
    console.error('API Error:', error)
  }
}

const updateStats = () => {
  stats.value[0].value = tasks.value.length
  stats.value[1].value = tasks.value.filter(t => t.status === 'In Progress').length
  stats.value[2].value = tasks.value.filter(t => t.status === 'Done').length
  stats.value[3].value = tasks.value.filter(t => t.status === 'Overdue').length
}

const editTask   = (index: number) => router.push({ name: 'task-edit',   params: { id: tasks.value[index].id } })
const goToDelete = (index: number) => router.push({ name: 'task-delete', params: { id: tasks.value[index].id } })

onMounted(fetchTasks)
</script>

<style scoped>
.v-table { background: transparent !important; }
.v-table :deep(th) { border-bottom: 2px solid #f1f5f9 !important; }
</style>