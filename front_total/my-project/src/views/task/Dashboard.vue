<template>
  <v-app>
    <div class="min-h-screen bg-[#eef2f5] d-flex justify-center pa-4">

      <v-sheet
        class="d-flex rounded-3xl overflow-hidden"
        max-width="1360"
        width="100%"
        elevation="24"
        style="background: white;"
      >

        <!-- ================= SIDEBAR ================= -->
        <aside class="pa-7 sidebar">

          <div class="text-h6 font-weight-bold">
            TaskFlow
          </div>

          <div class="text-caption text-uppercase mt-1 text-grey">
            Workspace
          </div>

          <!-- SEARCH -->
          <div class="search-box mt-6">
            <v-icon>mdi-magnify</v-icon>
            <input placeholder="Search..." />
          </div>

          <router-link to="/tasks/add" class="mt-4">
            <v-btn block color="primary" rounded="pill">
              + Assign Task
            </v-btn>
          </router-link>

        </aside>

        <!-- ================= MAIN ================= -->
        <main class="flex-grow-1 pa-8">

          <!-- ================= STATS ================= -->
          <v-row dense>
            <v-col v-for="s in stats" :key="s.title" cols="12" md="3">
              <v-card class="pa-5 rounded-xl">
                <div class="text-grey">{{ s.title }}</div>
                <div class="text-h4 font-weight-bold">{{ s.value }}</div>
              </v-card>
            </v-col>
          </v-row>

          <!-- ================= TABLE ================= -->
          <v-card class="pa-5 mt-6 rounded-xl">

            <b class="mb-4 d-block">Recent Tasks</b>

            <v-table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>User</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                <tr v-for="task in tasks" :key="task.id">

                  <td>{{ task.id }}</td>
                  <td>{{ task.title }}</td>

                  <td>
                    <v-chip :color="getStatusColor(task.status)" size="small">
                      {{ task.status }}
                    </v-chip>
                  </td>

                  <td>
                    <v-chip :color="getPriorityColor(task.priority)" size="small">
                      {{ task.priority }}
                    </v-chip>
                  </td>

                  <td>{{ task.user }}</td>

                  <td class="d-flex gap-2">

                    <router-link :to="{ name: 'task-edit', params: { id: task.id } }">
                      <v-btn size="small" color="blue">Edit</v-btn>
                    </router-link>

                    <v-btn size="small" color="red" @click="deleteTask(task.id)">
                      Delete
                    </v-btn>

                  </td>
                </tr>
              </tbody>
            </v-table>

          </v-card>

        </main>

      </v-sheet>

    </div>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'

/* ================= TYPES ================= */
type TaskStatus =
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'TESTING'
  | 'DONE'
  | 'CANCELED'

type TaskPriority =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'

/* ================= STATE ================= */
const tasks = ref<any[]>([])

const stats = ref([
  { title: 'TOTAL', value: 0 },
  { title: 'TODO', value: 0 },
  { title: 'IN_PROGRESS', value: 0 },
  { title: 'IN_REVIEW', value: 0 },
  { title: 'TESTING', value: 0 },
  { title: 'DONE', value: 0 },
  { title: 'CANCELED', value: 0 },
])

/* ================= COLORS ================= */
const getStatusColor = (status: TaskStatus) => {
  switch (status) {
    case 'DONE': return 'green'
    case 'IN_PROGRESS': return 'blue'
    case 'IN_REVIEW': return 'orange'
    case 'TESTING': return 'purple'
    case 'CANCELED': return 'red'
    case 'TODO': return 'grey'
    default: return 'grey'
  }
}

const getPriorityColor = (priority: TaskPriority) => {
  switch (priority) {
    case 'HIGH': return 'red'
    case 'MEDIUM': return 'orange'
    case 'LOW': return 'green'
    default: return 'grey'
  }
}

/* ================= STATS ================= */
const countStatus = (status: TaskStatus) =>
  tasks.value.filter(t => t.status === status).length

const updateStats = () => {
  stats.value = [
    { title: 'TOTAL', value: tasks.value.length },
    { title: 'TODO', value: countStatus('TODO') },
    { title: 'IN_PROGRESS', value: countStatus('IN_PROGRESS') },
    { title: 'IN_REVIEW', value: countStatus('IN_REVIEW') },
    { title: 'TESTING', value: countStatus('TESTING') },
    { title: 'DONE', value: countStatus('DONE') },
    { title: 'CANCELED', value: countStatus('CANCELED') },
  ]
}

/* ================= FETCH ================= */
const fetchTasks = async () => {
  try {
    const res = await api.get('/tasks')

    const raw = Array.isArray(res.data)
      ? res.data
      : res.data?.data ?? []

    tasks.value = raw.map((t: any) => ({
      id: t.id,
      title: t.title,
      status: (t.status ?? 'TODO') as TaskStatus,
      priority: (t.priority ?? 'MEDIUM') as TaskPriority,

      // 🔥 affichage user correct
      user: t.assignedUser
        ? `${t.assignedUser.name ?? ''} (${t.assignedUser.email ?? ''})`
        : 'Unassigned',
    }))

    updateStats()

  } catch (err) {
    console.error('FETCH ERROR:', err)
  }
}

/* ================= DELETE ================= */
const deleteTask = async (id: number) => {
  try {
    await api.delete(`/tasks/${id}`)

    tasks.value = tasks.value.filter(t => t.id !== id)
    updateStats()

  } catch (err) {
    console.error('DELETE ERROR:', err)
  }
}

onMounted(fetchTasks)
</script>

<style scoped>
.sidebar {
  width: 280px;
  background: #f8fafd;
  border-right: 1px solid #e9edf2;
}

.search-box {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #dee4eb;
  border-radius: 999px;
  background: white;
}

.search-box input {
  border: none;
  outline: none;
  width: 100%;
  margin-left: 8px;
}
</style>