<template>
  <v-container fluid class="fill-height d-flex justify-center align-center bg-grey-lighten-4 pa-6">

    <v-card class="pa-8 rounded-xl elevation-10" max-width="850" width="100%">

      <!-- HEADER -->
      <div class="mb-6">
        <h2 class="text-h5 font-weight-bold">✏️ Edit Task</h2>
        <div class="text-grey-darken-1">
          Update task information and assignment
        </div>
      </div>

      <!-- FORM -->
      <v-form @submit.prevent="updateTask">

        <v-row dense>

          <v-col cols="12">
            <v-text-field v-model="formData.title" label="Task Title" variant="outlined" />
          </v-col>

          <v-col cols="12">
            <v-textarea v-model="formData.description" label="Description" variant="outlined" rows="3" />
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field v-model="formData.dueDate" type="date" label="Due Date" variant="outlined" />
          </v-col>

          <!-- PRIORITY ENUM -->
          <v-col cols="12" md="6">
            <v-select
              v-model="formData.priority"
              :items="priorityOptions"
              label="Priority"
              variant="outlined"
            />
          </v-col>

          <!-- STATUS ENUM (CORRECT SYMFONY) -->
          <v-col cols="12" md="6">
            <v-select
              v-model="formData.status"
              :items="statusOptions"
              label="Status"
              variant="outlined"
            />
          </v-col>

          <!-- ASSIGNEE -->
          <v-col cols="12" md="6">
            <v-select
              v-model="formData.assignee"
              :items="teamMembers"
              item-title="fullName"
              item-value="id"
              label="Assign To"
              variant="outlined"
            />
          </v-col>

          <!-- EPIC -->
          <v-col cols="12" md="4">
            <v-select
              v-model="formData.epic"
              :items="epics"
              item-title="title"
              item-value="id"
              label="Epic"
              variant="outlined"
            />
          </v-col>

          <!-- PART -->
          <v-col cols="12" md="4">
            <v-select
              v-model="formData.part"
              :items="parts"
              item-title="name"
              item-value="id"
              label="Part"
              variant="outlined"
            />
          </v-col>

          <!-- LEVEL -->
          <v-col cols="12" md="4">
            <v-select
              v-model="formData.level"
              :items="levels"
              item-title="name"
              item-value="id"
              label="Level"
              variant="outlined"
            />
          </v-col>

        </v-row>

        <!-- BUTTONS -->
        <div class="d-flex justify-end mt-6 gap-3">

          <v-btn variant="outlined" @click="cancel">
            Cancel
          </v-btn>

          <v-btn color="primary" type="submit" :loading="submitting">
            Update Task
          </v-btn>

        </div>

      </v-form>
    </v-card>

  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

const submitting = ref(false)

/* ================= ENUMS SYMFONY ================= */
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
const formData = ref({
  title: '',
  description: '',
  dueDate: '',
  priority: 'MEDIUM',
  status: 'TODO',

  assignee: null,
  epic: null,
  part: null,
  level: null
})

/* ================= DATA ================= */
const teamMembers = ref<any[]>([])
const epics = ref<any[]>([])
const parts = ref<any[]>([])
const levels = ref<any[]>([])

/* ================= LOAD REFERENCE ================= */
const fetchReferenceData = async () => {
  const { data } = await api.get('/tasks/form-data')

  teamMembers.value = data.users || []
  epics.value = data.epics || []
  parts.value = data.parts || []
  levels.value = data.levels || []
}

/* ================= LOAD TASK ================= */
const fetchTaskDetails = async () => {
  const id = route.params.id
  const { data } = await api.get(`/tasks/${id}`)

  formData.value = {
    title: data.title || '',
    description: data.description || '',
    dueDate: data.dueDate ? data.dueDate.split('T')[0] : '',

    priority: data.priority || 'MEDIUM',
    status: data.status || 'TODO',

    assignee: data.assignedTo,
    epic: data.epicId,
    part: data.partId,
    level: data.levelId
  }
}

/* ================= UPDATE ================= */
const updateTask = async () => {
  submitting.value = true

  try {
    const id = route.params.id

    await api.put(`/tasks/${id}`, {
      title: formData.value.title,
      description: formData.value.description,
      dueDate: formData.value.dueDate,

      priority: formData.value.priority,
      status: formData.value.status,

      assignedTo: formData.value.assignee,
      epicId: formData.value.epic,
      partId: formData.value.part,
      levelId: formData.value.level
    })

    router.push('/dashboard')

  } catch (err) {
    console.error('UPDATE ERROR:', err)

  } finally {
    submitting.value = false
  }
}

const cancel = () => router.push('/dashboard')

onMounted(async () => {
  await fetchReferenceData()
  await fetchTaskDetails()
})
</script>