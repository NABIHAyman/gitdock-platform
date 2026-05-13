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
              <v-icon icon="mdi-clipboard-list-outline" color="white" size="18"></v-icon>
            </div>
            <span class="text-white/70 text-xs font-bold uppercase tracking-widest">Management</span>
          </div>
          <h1 class="text-2xl font-black text-white">All Tasks</h1>
          <p class="text-white/60 text-xs mt-0.5">Visualisez et gérez l'ensemble de vos missions.</p>
        </div>
        <v-btn
            to="/addtask"
            class="flex items-center gap-2 px-5 py-2.5 bg-white text-[#5b13ec] rounded-xl text-xs font-black shadow-lg hover:bg-purple-50 transition-all"
            variant="flat"
        >
          <v-icon icon="mdi-plus" size="16" class="mr-2"></v-icon>
          NEW TASK
        </v-btn>
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

    <!-- FILTERS & TABLE -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[500px]">
      <!-- Search & Filter Bar -->
      <div class="p-6 border-b border-slate-100 bg-slate-50/50">
        <v-row dense>
          <v-col cols="12" md="8">
            <v-text-field
                v-model="search"
                prepend-inner-icon="mdi-magnify"
                label="Search tasks..."
                variant="solo"
                flat
                density="comfortable"
                class="rounded-xl border border-slate-200"
                hide-details
            ></v-text-field>
          </v-col>
          <v-col cols="12" md="4">
            <v-select
                v-model="statusFilter"
                :items="['All', 'Pending', 'In Progress', 'Done']"
                label="Status"
                variant="solo"
                flat
                density="comfortable"
                class="rounded-xl border border-slate-200"
                hide-details
            ></v-select>
          </v-col>
        </v-row>
      </div>

      <v-table class="px-4 pb-4">
        <thead>
        <tr class="text-slate-400 text-xs font-black uppercase">
          <th class="py-4">Task Name</th>
          <th>Status</th>
          <th>Description</th>
          <th class="text-right">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="task in filteredTasks" :key="task.id" class="hover:bg-purple-50/30 transition-colors">
          <td class="py-4 font-bold text-slate-700">{{ task.title }}</td>
          <td>
            <v-chip
                :color="getStatusColor(task.status)"
                size="x-small"
                class="font-black px-3 rounded-lg"
                variant="flat"
            >
              {{ task.status.toUpperCase() }}
            </v-chip>
          </td>
          <td class="text-sm text-slate-500 italic max-w-xs truncate">
            {{ task.description || 'No description' }}
          </td>
          <td class="text-right">
            <div class="flex justify-end gap-2">
              <v-btn
                  icon="mdi-pencil-outline"
                  size="x-small"
                  color="indigo-lighten-4"
                  variant="flat"
                  class="rounded-lg"
                  @click="editTask(task.id)"
              ></v-btn>
              <v-btn
                  icon="mdi-trash-can-outline"
                  size="x-small"
                  color="red-lighten-4"
                  variant="flat"
                  class="rounded-lg"
                  @click="deleteTask(task.id)"
              ></v-btn>
            </div>
          </td>
        </tr>
        </tbody>
      </v-table>

      <!-- EMPTY STATE -->
      <div v-if="filteredTasks.length === 0" class="flex flex-col items-center justify-center py-20 text-slate-400">
        <v-icon icon="mdi-clipboard-off-outline" size="48" class="mb-2 opacity-20"></v-icon>
        <p class="text-sm font-bold italic">Aucune tâche ne correspond à vos critères.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TaskService, type TaskListItemDTO } from '@/services/TaskService'

const router = useRouter()
const tasks = ref<any[]>([])
const search = ref('')
const statusFilter = ref('All')

const stats = ref([
  { title: 'Total Tasks', value: 0, icon: 'mdi-format-list-bulleted', iconColor: '#5b13ec', bgClass: 'bg-purple-100' },
  { title: 'In Progress', value: 0, icon: 'mdi-progress-clock', iconColor: '#6d28d9', bgClass: 'bg-violet-100' },
  { title: 'Completed', value: 0, icon: 'mdi-check-circle', iconColor: '#22c55e', bgClass: 'bg-green-100' },
  { title: 'Pending', value: 0, icon: 'mdi-clock-outline', iconColor: '#f59e0b', bgClass: 'bg-orange-100' },
])

const normalizeStatus = (status: string | undefined): string => {
  if (!status) return 'Pending'
  const s = status.toLowerCase()
  if (s.includes('progress')) return 'In Progress'
  if (s.includes('done') || s.includes('completed')) return 'Done'
  return 'Pending'
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'Done': return 'green'
    case 'In Progress': return 'indigo'
    case 'Pending': return 'orange'
    default: return 'grey'
  }
}

const updateStats = () => {
  stats.value[0].value = tasks.value.length
  stats.value[1].value = tasks.value.filter(t => t.status === 'In Progress').length
  stats.value[2].value = tasks.value.filter(t => t.status === 'Done').length
  stats.value[3].value = tasks.value.filter(t => t.status === 'Pending').length
}

const fetchTasks = async (): Promise<void> => {
  try {
    const data = (await TaskService.list()) as TaskListItemDTO[]
    tasks.value = data.map((t) => ({
      id: t.id,
      title: t.title ?? 'Untitled',
      description: t.description ?? '',
      status: normalizeStatus(t.status)
    }))
    updateStats()
  } catch (err) {
    console.error(err)
  }
}

const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const matchSearch = task.title.toLowerCase().includes(search.value.toLowerCase())
    const matchStatus = statusFilter.value === 'All' || task.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

const editTask = (id: string | number) => router.push(`/tasks/edit/${id}`)
const deleteTask = (id: string | number) => router.push({ name: 'task-delete', params: { id } })

onMounted(fetchTasks)
</script>

<style scoped>
.v-table { background: transparent !important; }
:deep(.v-table__wrapper) { border-radius: 0 0 16px 16px; }
:deep(.v-field__input) { font-size: 0.875rem !important; }
</style>