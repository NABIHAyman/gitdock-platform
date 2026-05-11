<template>
  <v-container class="pa-6">

    <!-- HEADER -->
    <div class="d-flex justify-space-between align-center mb-6">
      <div>
        <h1 class="text-h4 font-weight-bold">All Tasks</h1>
        <p class="text-grey">Manage all your tasks in one place</p>
      </div>

      <router-link to="/addtask" style="text-decoration: none;">
  <v-btn color="primary" rounded>
    + New Task
  </v-btn>
</router-link>
    </div>

    <!-- SEARCH + FILTER -->
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

    <!-- TASK LIST -->
    <v-row>
      <v-col
        cols="12"
        md="6"
        lg="4"
        v-for="task in filteredTasks"
        :key="task.id"
      >
        <v-card class="pa-4 rounded-xl hover-card" elevation="3">

          <div class="d-flex justify-space-between">
            <div>
              <h3 class="text-subtitle-1 font-weight-bold">
                {{ task.title }}
              </h3>

              <p class="text-caption text-grey">
                {{ task.description }}
              </p>
            </div>

           <v-menu>
  <template #activator="{ props }">
    <v-btn icon v-bind="props">
      <v-icon>mdi-dots-vertical</v-icon>
    </v-btn>
  </template>

  <v-list>
    <!-- EDIT -->
    <v-list-item @click="editTask(task.id)">
      <v-list-item-title>Edit</v-list-item-title>
    </v-list-item>

    <!-- DELETE -->
    <v-list-item @click="deleteTask(task.id)">
      <v-list-item-title>Delete</v-list-item-title>
    </v-list-item>
  </v-list>
</v-menu>
          </div>

          <!-- STATUS -->
          <v-chip
            class="mt-3"
            :color="getStatusColor(task.status)"
            size="small"
            variant="flat"
          >
            {{ task.status }}
          </v-chip>

        </v-card>
      </v-col>
    </v-row>

  </v-container>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// SEARCH + FILTER
const search = ref('')
const statusFilter = ref('All')

// TASK TYPE
interface Task {
  id: number
  title: string
  description?: string
  status: string
}

// STATE
const tasks = ref<Task[]>([])

/* ---------------- NORMALIZE STATUS ---------------- */
const normalizeStatus = (status: string) => {
  if (!status) return 'Pending'

  const s = status.toLowerCase()

  if (s.includes('progress')) return 'In Progress'
  if (s.includes('done') || s.includes('completed')) return 'Done'
  if (s.includes('todo') || s.includes('pending')) return 'Pending'

  return status
}

/* ---------------- FETCH TASKS ---------------- */
const fetchTasks = async () => {
  try {
    // ⚠️ adapte si besoin : 8000 ou pas
    const res = await axios.get('http://localhost/api/tasks')

    tasks.value = res.data.map((t: any) => ({
      id: t.id,
      title: t.title ?? 'No title',
      description: t.description ?? '',
      status: normalizeStatus(t.status)
    }))
  } catch (err) {
    console.error('API ERROR:', err)
  }
}

/* ---------------- FILTERED TASKS ---------------- */
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const matchSearch =
      task.title.toLowerCase().includes(search.value.toLowerCase())

    const matchStatus =
      statusFilter.value === 'All' ||
      task.status === statusFilter.value

    return matchSearch && matchStatus
  })
})

/* ---------------- STATUS COLOR ---------------- */
const getStatusColor = (status: string) => {
  switch (status) {
    case 'Done':
      return 'green'
    case 'In Progress':
      return 'blue'
    case 'Pending':
      return 'orange'
    default:
      return 'grey'
  }
}

/* ---------------- INIT ---------------- */
onMounted(() => {
  fetchTasks()
})
// EDIT TASK → redirect vers page edit
const editTask = (id: number) => {
  router.push(`/tasks/edit/${id}`)
}

// DELETE TASK
const deleteTask = async (id: number) => {
  if (!confirm('Are you sure you want to delete this task?')) return

  try {
    await axios.delete(`http://localhost/api/tasks/${id}`)

    // remove from UI instantly
    tasks.value = tasks.value.filter(t => t.id !== id)

  } catch (err) {
    console.error('DELETE ERROR:', err)
  }
}
</script>

<style scoped>
.hover-card {
  transition: 0.3s ease;
}

.hover-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}
</style>