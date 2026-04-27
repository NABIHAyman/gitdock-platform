<template>
  <v-container class="py-12 d-flex justify-center">
    <v-col cols="12" md="6">
      <v-card class="pa-6 rounded-2xl" elevation="6" style="background: #f9fcff; border-color: #edf2f7;">
        <h2 class="text-h5 font-weight-bold mb-2" style="color: #1e293b;">Edit Task</h2>
        <p class="text-subtitle-1 mb-6" style="color: #5a6f8c;">
          Update your task details and manage assignments.
        </p>

        <v-progress-linear
          v-if="loading"
          indeterminate
          color="#1d4ed8"
          class="mb-4"
        ></v-progress-linear>

        <v-form @submit.prevent="updateTask" v-else>

          <!-- Task Title -->
          <v-text-field
            v-model="formData.title"
            label="Task Name"
            variant="outlined"
            class="mb-4"
            :rules="[v => !!v || 'Task name is required']"
          />

          <!-- Description -->
          <v-textarea
            v-model="formData.description"
            label="Description"
            variant="outlined"
            class="mb-4"
          />

          <!-- Due Date -->
          <v-text-field
            v-model="formData.dueDate"
            label="Due Date"
            type="date"
            variant="outlined"
            class="mb-4"
          />

          <!-- Priority -->
          <div class="mb-4">
            <div class="font-weight-medium mb-2">Priority</div>
            <v-radio-group v-model="formData.priority" row>
              <v-radio label="Low" value="Low"></v-radio>
              <v-radio label="Medium" value="Medium"></v-radio>
              <v-radio label="High" value="High"></v-radio>
            </v-radio-group>
          </div>

          <!-- Status -->
          <v-select
            v-model="formData.status"
            :items="statusOptions"
            label="Status"
            variant="outlined"
            class="mb-4"
          />

          <!-- Assign To -->
          <v-select
            v-model="formData.assignee"
            :items="teamMembers"
            label="Assign To"
            return-object
            item-title="fullName"
            item-value="id"
            variant="outlined"
            class="mb-4"
          />

          <!-- Epic -->
          <v-select
            v-model="formData.epic"
            :items="epics"
            label="Epic"
            return-object
            item-title="title"
            item-value="id"
            variant="outlined"
            class="mb-4"
          />

          <!-- Part -->
          <v-select
            v-model="formData.part"
            :items="parts"
            label="Part"
            return-object
            item-title="name"
            item-value="id"
            variant="outlined"
            class="mb-4"
          />

          <!-- Level -->
          <v-select
            v-model="formData.level"
            :items="levels"
            label="Level"
            return-object
            item-title="name"
            item-value="id"
            variant="outlined"
            class="mb-4"
          />

          <!-- Info -->
          <v-alert v-if="taskInfo.createdAt" type="info" class="mb-4">
            Created: {{ formatDate(taskInfo.createdAt) }}<br>
            Last updated: {{ formatDate(taskInfo.updatedAt) }}
          </v-alert>

          <!-- Buttons -->
          <div class="d-flex justify-center gap-4">
            <v-btn @click="cancel">Cancel</v-btn>
            <v-btn type="submit" color="#1d4ed8" :loading="submitting">
              Update Task
            </v-btn>
          </div>

        </v-form>
      </v-card>
    </v-col>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const submitting = ref(false)

const formData = ref({
  title: '',
  description: '',
  dueDate: '',
  priority: 'Medium',
  status: 'To Do',
  assignee: null,
  epic: null,
  part: null,
  level: null
})

const taskInfo = ref({
  id: null,
  createdAt: null,
  updatedAt: null
})

const statusOptions = ['To Do', 'In Progress', 'Done']

const teamMembers = ref([])
const epics = ref([])
const parts = ref([])
const levels = ref([])

// ---------------- FETCH REFERENCE DATA ----------------
const fetchReferenceData = async () => {
  try {
    const res = await axios.get('http://localhost/api/tasks/form-data')
    const data = res.data

    // Mapper pour v-select
    teamMembers.value = data.users.map(u => ({ id: u.id, fullName: u.fullName }))
    epics.value = data.epics.map(e => ({ id: e.id, title: e.title }))
    parts.value = data.parts.map(p => ({ id: p.id, name: p.name }))
    levels.value = data.levels.map(l => ({ id: l.id, name: l.name }))
  } catch (error) {
    console.error('Error fetching reference data:', error)
  }
}

// ---------------- FETCH TASK DETAILS ----------------
const fetchTaskDetails = async (id) => {
  try {
    const res = await axios.get(`http://localhost/api/tasks/${id}`)
    const task = res.data

    formData.value = {
      title: task.title || '',
      description: task.description || '',
      dueDate: task.dueDate ? task.dueDate.split(' ')[0] : '',
      priority: task.priority || 'Medium',
      status: task.status || 'To Do',
      assignee: task.assignedTo
        ? teamMembers.value.find(u => u.id === task.assignedTo)
        : null,
      epic: task.epic ? epics.value.find(e => e.id === task.epic) : null,
      part: task.part ? parts.value.find(p => p.id === task.part) : null,
      level: task.level ? levels.value.find(l => l.id === task.level) : null
    }

    taskInfo.value = task
  } catch (error) {
    console.error('Error fetching task:', error)
  }
}

// ---------------- UPDATE TASK ----------------
const updateTask = async () => {
  if (!formData.value.title) {
    alert('Task name is required')
    return
  }

  submitting.value = true

  try {
    await axios.put(`http://localhost/api/tasks/${taskInfo.value.id}`, {
      title: formData.value.title,
      description: formData.value.description,
      dueDate: formData.value.dueDate || null,
      priority: formData.value.priority,
      status: formData.value.status,
      assignedTo: formData.value.assignee?.id || null,
      epic: formData.value.epic?.id || null,
      part: formData.value.part?.id || null,
      level: formData.value.level?.id || null
    })

    alert('Task updated successfully ✅')
    router.push('/dashboardtask')
  } catch (error) {
    console.error('Error updating task:', error)
    alert('Failed to update task ❌')
  } finally {
    submitting.value = false
  }
}

// ---------------- UTILS ----------------
const cancel = () => router.push('/dashboardtask')
const formatDate = (d) => (d ? new Date(d).toLocaleString() : 'N/A')

// ---------------- INIT ----------------
onMounted(async () => {
  await fetchReferenceData()
  await fetchTaskDetails(route.params.id)
  loading.value = false
})
</script>