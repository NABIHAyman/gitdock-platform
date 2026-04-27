<template>
  <v-app>
    <div class="min-h-screen bg-[#eef2f5] d-flex justify-center align-center pa-4">
      <v-sheet
        class="d-flex rounded-3xl overflow-hidden"
        max-width="1360"
        width="100%"
        elevation="24"
        style="background: white;"
      >
        <!-- Sidebar -->
        <aside
          class="d-flex flex-column gap-6 pa-7"
          style="width: 280px; background: #f8fafd; border-right: 1px solid #e9edf2;"
        >
          <!-- Logo -->
          <div class="d-flex align-center gap-2" style="color: #0b1b33; font-weight: 700; font-size: 1.5rem;">
            <v-avatar size="8" color="#3b82f6"></v-avatar>
            TaskFlow
          </div>
          <div class="text-caption font-weight-bold text-uppercase" style="color: #5e6f88; margin-top: -0.5rem;">
            Workspace
          </div>

          <!-- Search -->
          <div
            class="d-flex align-center rounded-pill px-4 py-2"
            style="border: 1px solid #dee4eb; box-shadow: 0 2px 4px rgba(0,0,0,0.02); background: white;"
          >
            <i class="fa fa-search" style="color: #8b9eb5; font-size: 0.875rem;"></i>
            <input
              type="text"
              placeholder="Search tasks, projects..."
              class="w-100 outline-none"
              style="border: none; background: transparent; font-size: 0.875rem;"
            />
          </div>

          <router-link to="/addtask" style="text-decoration: none; width: 100%;">
  <v-btn
    color="#1d4ed8"
    rounded="pill"
    block
    class="text-none font-weight-bold"
    style="box-shadow: 0 6px 12px rgba(29,78,216,0.25);"
    prepend-icon="fa fa-plus"
  >
    Assign Task
  </v-btn>
</router-link>
          <!-- Four percentage circles -->
          <div class="d-flex justify-space-between">
            <div
              v-for="(item, i) in circles"
              :key="i"
              class="d-flex flex-column align-center"
            >
              <v-avatar
                size="48"
                class="d-flex align-center justify-center font-weight-bold"
                style="background: white; color: #1e293b; border-width: 3px; border-style: solid;"
                :style="circleBorderStyle(i)"
              >
                {{ item.value }}
              </v-avatar>
            </div>
          </div>

          <!-- Navigation -->
          <v-list dense nav>
            <v-list-item
              v-for="(item, index) in navItems"
              :key="item.title"
              :active="index === 0"
              class="rounded-xl"
              active-color="primary"
              style="margin-bottom: 4px;"
            >
              <v-list-item-icon>
                <i :class="item.icon"></i>
              </v-list-item-icon>
              <v-list-item-title>{{ item.title }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </aside>

        <!-- Main Content -->
        <main class="flex-grow-1 d-flex flex-column gap-7 pa-8" style="overflow-y: auto; background: white;">
          <!-- Stats Cards -->
          <v-row dense>
            <v-col
              v-for="stat in stats"
              :key="stat.title"
              cols="12" sm="6" md="3"
            >
              <v-card
                variant="outlined"
                class="pa-5 rounded-2xl"
                style="background: #f9fcff; border-color: #edf2f7;"
              >
                <div class="text-caption font-weight-medium" style="color: #5a6f8c;">{{ stat.title }}</div>
                <div class="text-h3 font-weight-bold" style="color: #0b1b33;">{{ stat.value }}</div>
              </v-card>
            </v-col>
          </v-row>

          <!-- Charts Row -->
          <v-row dense>
            <!-- Task Distribution -->
<v-col cols="12" md="6">
  <v-card variant="outlined" class="pa-6 rounded-2xl" style="background: #f9fcff; border-color: #edf2f7;">
    <div class="d-flex justify-space-between align-center mb-5">
      <span class="font-weight-semibold" style="color: #1e293b;">Task Distribution</span>
      <router-link
        to="/TaskAll"
        class="text-primary text-caption font-medium"
        style="text-decoration: none;"
      >
        View All
      </router-link>
    </div>

    <div class="d-flex flex-column gap-2">
      <!-- To Do -->
      <div class="d-flex align-center gap-2" style="color: #2d3f59;">
        <v-avatar size="12" color="#f97316" class="rounded-circle"></v-avatar>
        To Do
        <span class="ml-auto font-weight-bold">
          {{ getStatusCount('To Do') }}
        </span>
      </div>

      <!-- In Progress -->
      <div class="d-flex align-center gap-2" style="color: #2d3f59;">
        <v-avatar size="12" color="#3b82f6" class="rounded-circle"></v-avatar>
        In Progress
        <span class="ml-auto font-weight-bold">
          {{ getStatusCount('In Progress') }}
        </span>
      </div>

      <!-- Done -->
      <div class="d-flex align-center gap-2" style="color: #2d3f59;">
        <v-avatar size="12" color="#22c55e" class="rounded-circle"></v-avatar>
        Done
        <span class="ml-auto font-weight-bold">
          {{ getStatusCount('Done') }}
        </span>
      </div>
    </div>

    <!-- Bars (optionnel dynamique) -->
    <div class="d-flex align-end gap-3 mt-5" style="height: 60px;">
      <div
        v-for="bar in taskBars"
        :key="bar.color"
        class="flex-grow-1 rounded-xl"
        style="background: #e6edf4; display: flex; align-items: flex-end;"
      >
        <div
          class="w-100 rounded-xl"
          :style="{ height: bar.height + 'px', background: bar.color }"
        ></div>
      </div>
    </div>

  </v-card>
</v-col>

            <!-- Weekly Completion Rate -->
            <v-col cols="12" md="6">
              <v-card variant="outlined" class="pa-6 rounded-2xl" style="background: #f9fcff; border-color: #edf2f7;">
                <div class="font-weight-semibold mb-5" style="color: #1e293b;">Weekly Completion Rate</div>
                <div class="d-flex justify-space-between text-caption font-weight-medium px-1" style="color: #6f85a2;">
                  <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
                </div>
                <div class="d-flex align-end gap-1.5 mt-2" style="height: 140px;">
                  <div v-for="(h, idx) in weeklyHeights" :key="idx" class="flex-grow-1 rounded-2xl" style="background: #e2eaf3; display: flex; align-items: flex-end;">
                    <div class="w-100 rounded-2xl" :style="{ height: h + 'px', background: 'linear-gradient(180deg, #4b7bec, #2f5fcf)' }"></div>
                  </div>
                </div>
              </v-card>
            </v-col>
          </v-row>

          <!-- Recent Activity Table with Edit/Delete Buttons -->
          <v-card variant="outlined" class="pa-6 rounded-2xl" style="background: #f9fcff; border-color: #edf2f7;">
            <div class="d-flex justify-space-between align-center mb-3">
              <span class="font-weight-semibold" style="color: #1e293b;">Recent Activity</span>
              <a href="#" class="text-primary text-caption font-medium" style="text-decoration: none;">View All</a>
            </div>

            <v-simple-table dense>
              <thead>
                <tr style="color: #50657e; font-size: 13px; font-weight: 600; border-bottom: 1px solid #dfe7ef;">
                  <th class="text-left pb-3">Task</th>
                  <th class="text-left pb-3">Action</th>
                  <th class="text-left pb-3">User</th>
                  <th class="text-left pb-3">Time</th>
                  <th class="text-left pb-3">Manager</th>
                  <th class="text-left pb-3">Collaborator</th>
                  <th class="text-left pb-3">Options</th>
                </tr>
              </thead>
              <tbody style="color: #1e2b3e;">
                <tr v-for="(task, index) in tasks" :key="index" style="border-bottom: 1px solid #eaedf2;">
                  <td>{{ task.name }}</td>
                  <td>
                    <v-chip size="small" :color="task.color" text-color="white">{{ task.status }}</v-chip>
                  </td>
                  <td>{{ task.user }}</td>
                  <td>{{ task.time }}</td>
                  <td>{{ task.manager || '—' }}</td>
                  <td>{{ task.collaborator || '—' }}</td>
                  <td class="d-flex gap-2">
                    <v-btn small color="#3b82f6" text @click="editTask(index)">
                      Edit
                    </v-btn>
                    <v-btn small color="#ef4444" text @click="deleteTask(index)">
                      Delete
                    </v-btn>
                  </td>
                </tr>
              </tbody>
            </v-simple-table>
          </v-card>
        </main>
      </v-sheet>
    </div>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// ---------------- NAV ----------------
const navItems = [
  { title: 'Dashboard', icon: 'fa fa-tachometer-alt' },
  { title: 'Projects', icon: 'fa fa-project-diagram' },
  { title: 'Tasks', icon: 'fa fa-tasks' },
  { title: 'Users', icon: 'fa fa-users' },
  { title: 'Settings', icon: 'fa fa-cog' }
]

// ---------------- UI ----------------
const circles = [
  { value: '12%' },
  { value: '5%' },
  { value: '24%' },
  { value: '8%' }
]

const taskBars = ref([
  { color: '#f97316', height: 28 },
  { color: '#3b82f6', height: 21 },
  { color: '#22c55e', height: 14 }
])

const weeklyHeights = ref([42, 28, 56, 34, 62, 25, 47])

const circleBorderStyle = (index: number) => {
  const borders = [
    { top: '#f97316', left: '#f97316' },
    { right: '#3b82f6', bottom: '#3b82f6' },
    { top: '#a855f7', right: '#a855f7' },
    { left: '#10b981', bottom: '#10b981' }
  ]

  return {
    borderTopColor: borders[index]?.top ?? 'transparent',
    borderLeftColor: borders[index]?.left ?? 'transparent',
    borderRightColor: borders[index]?.right ?? 'transparent',
    borderBottomColor: borders[index]?.bottom ?? 'transparent',
    borderWidth: '3px',
    borderStyle: 'solid'
  }
}

// ---------------- TASK TYPE ----------------
interface Task {
  id: number
  name: string
  status: string
  color: string
  user: string
  time: string
  manager?: string
  collaborator?: string
}

// ---------------- STATE ----------------
const tasks = ref<Task[]>([])

const stats = ref([
  { title: 'TOTAL TASKS', value: 0 },
  { title: 'IN PROGRESS', value: 0 },
  { title: 'COMPLETED', value: 0 },
  { title: 'OVERDUE', value: 0 }
])

// ✅ COUNTERS (POUR TON UI)
const todoCount = computed(() =>
  tasks.value.filter(t => t.status === 'To Do').length
)

const inProgressCount = computed(() =>
  tasks.value.filter(t => t.status === 'In Progress').length
)

const doneCount = computed(() =>
  tasks.value.filter(t => t.status === 'Done').length
)

// ---------------- FETCH TASKS ----------------
const fetchTasks = async () => {
  try {
    const res = await axios.get('http://localhost/api/tasks')

    tasks.value = res.data.map((t: any) => ({
      id: t.id,
      name: t.title ?? '—',
      status: t.status ?? 'To Do',
      color:
        t.status === 'Done'
          ? '#22c55e'
          : t.status === 'In Progress'
          ? '#3b82f6'
          : '#f97316',
      user: t.assignedTo ?? '—',
      time: t.dueDate
        ? new Date(t.dueDate).toLocaleString()
        : '—',

      // ✅ FIX IMPORTANT
      manager: t.assignedBy ?? '—',
      collaborator: t.collaborator ?? '—'
    }))

    updateStats()
  } catch (error) {
    console.error('API error:', error)
  }
}

// ---------------- UPDATE STATS ----------------
const updateStats = () => {
  stats.value = [
    { title: 'TOTAL TASKS', value: tasks.value.length },
    { title: 'IN PROGRESS', value: inProgressCount.value },
    { title: 'COMPLETED', value: doneCount.value },
    { title: 'OVERDUE', value: getStatusCount('Overdue') }
  ]
}

// ---------------- SAFE COUNT ----------------
const getStatusCount = (status: string) => {
  return tasks.value.filter(t => t.status === status).length
}

// ---------------- EDIT ----------------
const editTask = (index: number) => {
  const task = tasks.value[index]
  if (!task) return
  router.push(`/tasks/edit/${task.id}`)
}

// ---------------- DELETE ----------------
const deleteTask = (index: number) => {
  const task = tasks.value[index]
  if (!task) return

  tasks.value.splice(index, 1)
  updateStats()
}

// ---------------- INIT ----------------
onMounted(() => {
  fetchTasks()
})
</script>