<template>
  <v-container class="py-12 d-flex justify-center">
    <v-col cols="12" md="6">
      <v-card class="pa-6 rounded-2xl" elevation="6">
        <h2 class="text-h5 font-weight-bold mb-4">Delete Task</h2>
        <p>Are you sure you want to delete this task? This action is reversible (soft delete).</p>

        <v-alert type="info" class="my-4">
          Task: {{ task.title || "Loading..." }}
        </v-alert>

        <div class="d-flex justify-center gap-4">
          <v-btn variant="outlined" color="grey" @click="cancel">Cancel</v-btn>
          <v-btn color="red" @click="softDeleteTask">Delete Task</v-btn>
        </div>
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

const task = ref({ title: '' })

// ---------- Fetch Task ----------
const fetchTask = async () => {
  try {
    const res = await axios.get(`http://localhost/api/tasks/${route.params.id}`)
    task.value = res.data
  } catch (err) {
    console.error('Error fetching task:', err)
    alert('Failed to load task')
  }
}

// ---------- Soft Delete ----------
const softDeleteTask = async () => {
  if (!confirm('Do you really want to delete this task?')) return

  try {
    await axios.put(`http://localhost/api/tasks/${route.params.id}/soft-delete`)
    alert('Task deleted successfully (soft delete)')
    router.push('/dashboardtask')
  } catch (err) {
    console.error('Error deleting task:', err)
    alert('Failed to delete task')
  }
}

const cancel = () => router.push('/dashboardtask')

// ---------- Init ----------
onMounted(fetchTask)
</script>