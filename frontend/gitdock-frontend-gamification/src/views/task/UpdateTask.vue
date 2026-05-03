<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { TaskService, type TaskListItemDTO } from '@/services/TaskService'

interface SelectionItem {
  id: number; // Forcer en number pour correspondre au service
  fullName?: string;
  title?: string;
  name?: string;
}

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
  assignee: null as SelectionItem | null,
  epic: null as SelectionItem | null,
  part: null as SelectionItem | null,
  level: null as SelectionItem | null
})

const taskInfo = ref<any>({
  id: null,
  createdAt: null,
  updatedAt: null
})

const statusOptions = ['To Do', 'In Progress', 'Done']
const teamMembers = ref<SelectionItem[]>([])
const epics = ref<SelectionItem[]>([])
const parts = ref<SelectionItem[]>([])
const levels = ref<SelectionItem[]>([])

const fetchReferenceData = async (): Promise<void> => {
  try {
    const data = await TaskService.getFormData()
    teamMembers.value = (data.users ?? []).map((u: any) => ({
      id: Number(u.id),
      fullName: `${u.firstName} ${u.lastName}`.trim()
    }))
    epics.value = (data.epics ?? []).map((e: any) => ({ ...e, id: Number(e.id) }))
    parts.value = (data.parts ?? []).map((p: any) => ({ ...p, id: Number(p.id) }))
    levels.value = (data.levels ?? []).map((l: any) => ({ ...l, id: Number(l.id) }))
  } catch (error) {
    console.error(error)
  }
}

const fetchTaskDetails = async (id: string | number): Promise<void> => {
  try {
    const task = (await TaskService.getById(id)) as TaskListItemDTO
    formData.value = {
      title: task.title ?? '',
      description: task.description ?? '',
      dueDate: task.dueDate ? String(task.dueDate).split(' ')[0] : '',
      priority: task.priority || 'Medium',
      status: task.status || 'To Do',
      assignee: task.assignedTo ? teamMembers.value.find(u => u.id === Number(task.assignedTo)) || null : null,
      epic: task.epic ? epics.value.find(e => e.id === Number(task.epic)) || null : null,
      part: task.part ? parts.value.find(p => p.id === Number(task.part)) || null : null,
      level: task.level ? levels.value.find(l => l.id === Number(task.level)) || null : null
    }
    taskInfo.value = task
  } catch (error) {
    console.error(error)
  }
}

const updateTask = async (): Promise<void> => {
  if (!formData.value.title) return
  submitting.value = true
  try {
    // Cast explicite en Number | null pour TS2322
    await TaskService.update(taskInfo.value.id, {
      title: formData.value.title,
      description: formData.value.description,
      status: formData.value.status,
      dueDate: formData.value.dueDate || null,
      assignedTo: formData.value.assignee ? Number(formData.value.assignee.id) : null,
      epic: formData.value.epic ? Number(formData.value.epic.id) : null,
      part: formData.value.part ? Number(formData.value.part.id) : null,
      level: formData.value.level ? Number(formData.value.level.id) : null,
    })
    alert('Success ✅')
    await router.push('/dashboardtask') // Ajout du await pour TS2322 (Promise)
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const cancel = async (): Promise<void> => {
  await router.push('/dashboardtask')
}

// Utilisation de formatDate dans le template pour éviter TS6133
const formatDate = (d: any): string => (d ? new Date(d).toLocaleString() : 'N/A')

onMounted(async () => {
  await fetchReferenceData()
  if (route.params.id) {
    await fetchTaskDetails(route.params.id as string)
  }
  loading.value = false
})
</script>

<template>
  <v-container class="py-12 d-flex justify-center">
    <v-col cols="12" md="6">
      <v-card class="pa-6 rounded-2xl" elevation="6">
        <h2 class="text-h5 font-weight-bold mb-2">Edit Task</h2>

        <v-alert v-if="taskInfo.createdAt" type="info" density="compact" variant="tonal" class="mb-4 text-caption">
          Updated: {{ formatDate(taskInfo.updatedAt) }}
        </v-alert>

        <v-progress-linear v-if="loading" indeterminate color="#1d4ed8" class="mb-4"></v-progress-linear>

        <v-form @submit.prevent="updateTask" v-else>
          <v-text-field v-model="formData.title" label="Task Name" variant="outlined" class="mb-4" />
          <v-textarea v-model="formData.description" label="Description" variant="outlined" class="mb-4" />
          <v-text-field v-model="formData.dueDate" label="Due Date" type="date" variant="outlined" class="mb-4" />
          <v-select v-model="formData.status" :items="statusOptions" label="Status" variant="outlined" class="mb-4" />

          <v-select v-model="formData.assignee" :items="teamMembers" label="Assign To" return-object item-title="fullName" item-value="id" variant="outlined" class="mb-4" />
          <v-select v-model="formData.epic" :items="epics" label="Epic" return-object item-title="title" item-value="id" variant="outlined" class="mb-4" />
          <v-select v-model="formData.part" :items="parts" label="Part" return-object item-title="name" item-value="id" variant="outlined" class="mb-4" />
          <v-select v-model="formData.level" :items="levels" label="Level" return-object item-title="name" item-value="id" variant="outlined" class="mb-4" />

          <div class="d-flex justify-center gap-4 mt-6">
            <v-btn @click="cancel" variant="outlined">Cancel</v-btn>
            <v-btn type="submit" color="#1d4ed8" :loading="submitting">Update Task</v-btn>
          </div>
        </v-form>
      </v-card>
    </v-col>
  </v-container>
</template>