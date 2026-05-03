<template>
  <v-app>
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 to-slate-100 p-6">

      <v-card class="w-full max-w-md rounded-2xl shadow-xl overflow-hidden">

        <!-- HEADER -->
        <div class="bg-red-500 text-white p-5">
          <h2 class="text-xl font-bold">Delete Task</h2>
          <p class="text-sm opacity-80">This action cannot be undone</p>
        </div>

        <!-- CONTENT -->
        <div class="p-6 space-y-4">

          <div class="bg-red-50 border border-red-200 rounded-xl p-4">
            <p class="text-gray-700 text-sm">
              Are you sure you want to delete this task?
            </p>

            <p class="font-bold text-gray-900 mt-2">
              {{ taskTitle }}
            </p>
          </div>

          <!-- BUTTONS -->
          <div class="flex justify-between gap-3 pt-4">

            <v-btn
              variant="outlined"
              class="rounded-xl w-full"
              @click="cancel"
            >
              Cancel
            </v-btn>

            <v-btn
              color="red"
              class="rounded-xl text-white w-full"
              @click="confirmDelete"
            >
              Delete
            </v-btn>

          </div>

        </div>
      </v-card>

    </div>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()

const taskId = Number(route.params.id)
const taskTitle = ref('Loading...')

// ================= LOAD TASK =================
const loadTask = async () => {
  try {
    const res = await api.get(`/tasks/${taskId}`)
    taskTitle.value = res.data.title
  } catch (err) {
    console.error('LOAD ERROR:', err)
    taskTitle.value = 'Unknown task'
  }
}

// ================= DELETE =================
const confirmDelete = async () => {
  try {
    await api.delete(`/tasks/${taskId}`)

    router.push('/tasks')
  } catch (err) {
    console.error('DELETE ERROR:', err)
  }
}

// ================= CANCEL =================
const cancel = () => {
  router.push('/tasks')
}

onMounted(loadTask)
</script>