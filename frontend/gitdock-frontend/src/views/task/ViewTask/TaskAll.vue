<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TaskService, type TaskListItemDTO } from '@/services/TaskService'

const router = useRouter()

// SEARCH + FILTER
const search = ref('')
const statusFilter = ref('All')

// TASK TYPE - On s'assure que l'ID est bien présent
interface Task {
  id: string | number // Union type pour plus de flexibilité
  title: string
  description?: string
  status: string
}

// STATE
const tasks = ref<Task[]>([])

/* ---------------- NORMALIZE STATUS ---------------- */
const normalizeStatus = (status: string | undefined): string => {
  if (!status) return 'Pending'
  const s = status.toLowerCase()
  if (s.includes('progress')) return 'In Progress'
  if (s.includes('done') || s.includes('completed')) return 'Done'
  return 'Pending'
}

/* ---------------- FETCH TASKS ---------------- */
const fetchTasks = async (): Promise<void> => {
  try {
    const data = (await TaskService.list()) as TaskListItemDTO[]
    tasks.value = data.map((t) => ({
      id: t.id ?? 0, // Fallback à 0 si l'ID est undefined (Fix TS2345)
      title: t.title ?? 'Untitled',
      description: t.description ?? '',
      status: normalizeStatus(t.status)
    }))
  } catch (err) {
    console.error(err)
  }
}

/* ---------------- FILTERED TASKS ---------------- */
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const matchSearch = task.title.toLowerCase().includes(search.value.toLowerCase())
    const matchStatus = statusFilter.value === 'All' || task.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

/* ---------------- STATUS COLOR ---------------- */
const getStatusColor = (status: string) => {
  switch (status) {
    case 'Done': return 'green'
    case 'In Progress': return 'blue'
    case 'Pending': return 'orange'
    default: return 'grey'
  }
}

/* ---------------- ACTIONS ---------------- */

// EDIT TASK - Correction du type de paramètre
const editTask = async (id: string | number | undefined): Promise<void> => {
  if (!id) return
  await router.push(`/tasks/edit/${id}`)
}

// DELETE TASK - Correction du type de paramètre
const deleteTask = async (id: string | number | undefined): Promise<void> => {
  if (!id) return
  if (!confirm('Are you sure you want to delete this task?')) return

  try {
    // On force l'ID en string ou number pour satisfaire le service
    await TaskService.softDelete(id)
    tasks.value = tasks.value.filter(t => t.id !== id)
  } catch (err) {
    console.error(err)
  }
}

onMounted(() => {
  fetchTasks()
})
</script>

<template>
  <v-container class="pa-6">
    <div class="d-flex justify-space-between align-center mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">All Tasks</h1>
        <p class="text-grey">Manage all your tasks in one place</p>
      </div>
      <v-btn color="primary" rounded @click="router.push('/addtask')">
        + New Task
      </v-btn>
    </div>

    <v-row class="mb-4">
      <v-col cols="12" md="6">
        <v-text-field
            v-model="search"
            label="Search task..."
            prepend-inner-icon="mdi-magnify"
            density="comfortable"
            variant="outlined"
        />
      </v-col>
      <v-col cols="12" md="3">
        <v-select
            v-model="statusFilter"
            :items="['All', 'Pending', 'In Progress', 'Done']"
            label="Status"
            density="comfortable"
            variant="outlined"
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12" md="6" lg="4" v-for="task in filteredTasks" :key="task.id">
        <v-card class="pa-4 rounded-xl hover-card" elevation="3">
          <div class="d-flex justify-space-between">
            <div>
              <h3 class="text-subtitle-1 font-weight-bold">{{ task.title }}</h3>
              <p class="text-caption text-grey">{{ task.description }}</p>
            </div>
            <v-menu>
              <template #activator="{ props }">
                <v-btn icon v-bind="props"><v-icon>mdi-dots-vertical</v-icon></v-btn>
              </template>
              <v-list>
                <v-list-item @click="editTask(task.id)">
                  <v-list-item-title>Edit</v-list-item-title>
                </v-list-item>
                <v-list-item @click="deleteTask(task.id)">
                  <v-list-item-title>Delete</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>
          </div>
          <v-chip class="mt-3" :color="getStatusColor(task.status)" size="small" variant="flat">
            {{ task.status }}
          </v-chip>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>