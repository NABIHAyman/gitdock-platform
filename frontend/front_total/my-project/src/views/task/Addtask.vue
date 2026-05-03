<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 to-blue-50 p-6">

    <v-card class="w-full max-w-4xl rounded-3xl shadow-xl bg-white overflow-hidden">

      <!-- HEADER -->
      <div class="bg-gradient-to-r from-blue-600 to-indigo-600 text-white p-6">
        <h2 class="text-2xl font-semibold">Create Task</h2>
        <p class="text-sm opacity-80">Organize your work efficiently</p>
      </div>

      <v-form @submit.prevent="submitTask" class="p-8 space-y-6">

        <!-- TITLE -->
        <div>
          <label class="label">Task Title</label>
          <v-text-field
            v-model="task.title"
            placeholder="Enter task title..."
            variant="outlined"
            density="comfortable"
            hide-details
            class="input"
          />
        </div>

        <!-- DESCRIPTION -->
        <div>
          <label class="label">Description</label>
          <v-textarea
            v-model="task.description"
            placeholder="Describe the task..."
            rows="3"
            variant="outlined"
            hide-details
            class="input"
          />
        </div>

        <!-- DATE + STATUS -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

          <div>
            <label class="label">Due Date</label>
            <v-text-field
              v-model="task.dueDate"
              type="date"
              variant="outlined"
              hide-details
              class="input"
            />
          </div>

          <!-- STATUS (ENUM SYMFONY) -->
          <div>
            <label class="label">Status</label>
            <v-select
              v-model="task.status"
              :items="statusOptions"
              variant="outlined"
              hide-details
              class="input"
            />
          </div>

        </div>

        <!-- PRIORITY (ENUM SYMFONY) -->
        <div>
          <label class="label">Priority</label>
          <v-select
            v-model="task.priority"
            :items="priorityOptions"
            variant="outlined"
            hide-details
            class="input"
          />
        </div>

        <!-- ASSIGNEE -->
        <div>
          <label class="label">Assign To</label>
          <v-select
            v-model="task.assignee"
            :items="teamMembers"
            item-title="fullName"
            item-value="id"
            variant="outlined"
            hide-details
            class="input"
          />
        </div>

        <!-- EPIC / PART / LEVEL -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">

          <div>
            <label class="label">Epic</label>
            <v-select v-model="task.epic" :items="epics" item-title="title" item-value="id" class="input" />
          </div>

          <div>
            <label class="label">Part</label>
            <v-select v-model="task.part" :items="parts" item-title="name" item-value="id" class="input" />
          </div>

          <div>
            <label class="label">Level</label>
            <v-select v-model="task.level" :items="levels" item-title="name" item-value="id" class="input" />
          </div>

        </div>

        <!-- ACTIONS -->
        <div class="flex justify-between items-center pt-6 border-t">

          <v-btn variant="text" class="text-gray-500">
            Cancel
          </v-btn>

          <v-btn color="blue" type="submit" class="px-6 py-2 text-white rounded-xl">
            Save Task 🚀
          </v-btn>

        </div>

      </v-form>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import api from '@/services/api'

/* ================= ENUM SYMFONY MATCH ================= */
const statusOptions = [
  'TODO',
  'IN_PROGRESS',
  'IN_REVIEW',
  'TESTING',
  'DONE',
  'CANCELED'
]

const priorityOptions = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'URGENT'
]

/* ================= FORM ================= */
const task = ref({
  title: '',
  description: '',
  dueDate: '',
  status: 'TODO',
  priority: 'MEDIUM',
  assignee: null,
  epic: null,
  part: null,
  level: null
})

/* ================= MOCK (ou API plus tard) ================= */
const teamMembers = ref([])
const epics = ref([])
const parts = ref([])
const levels = ref([])

/* ================= SUBMIT ================= */
const submitTask = async () => {
  try {
    const payload = {
      title: task.value.title,
      description: task.value.description,

      status: task.value.status,
      priority: task.value.priority,

      dueDate: task.value.dueDate || null,

      assignedTo: task.value.assignee ?? null,
      assignedBy: 1,

      epicId: task.value.epic ?? null,
      partId: task.value.part ?? null,
      levelId: task.value.level ?? null,
      projectId: 1
    }

    console.log('SEND →', payload)

    await api.post('/tasks', payload)

    alert('Task created successfully ✅')

    task.value = {
      title: '',
      description: '',
      dueDate: '',
      status: 'TODO',
      priority: 'MEDIUM',
      assignee: null,
      epic: null,
      part: null,
      level: null
    }

  } catch (err) {
    console.error('ERROR:', err)
    alert('Error creating task ❌')
  }
}
</script>

<style scoped>
.label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
  display: block;
}

.input :deep(.v-field) {
  border-radius: 12px !important;
  border: 1.5px solid #e2e8f0 !important;
  transition: 0.2s;
}

.input :deep(.v-field:hover) {
  border-color: #3b82f6 !important;
}

.input :deep(.v-field--focused) {
  border-color: #2563eb !important;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}
</style>