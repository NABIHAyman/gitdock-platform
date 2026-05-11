<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <!-- HEADER -->
    <div class="relative overflow-hidden bg-gradient-to-r from-red-600 to-rose-700 rounded-2xl p-6 shadow-xl">
      <div class="relative flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-black text-white">Delete Task</h1>
          <p class="text-white/60 text-xs mt-0.5">Cette action est réversible (soft delete).</p>
        </div>
        <button class="px-5 py-2.5 bg-white text-red-600 rounded-xl text-xs font-black shadow-lg" @click="router.push('/dashboardtask')">
          RETOUR
        </button>
      </div>
    </div>

    <!-- MODAL CARD -->
    <div v-if="loading" class="bg-white rounded-2xl p-16 flex flex-col items-center">
      <v-progress-circular indeterminate color="red" size="50"></v-progress-circular>
    </div>

    <div v-else class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="h-1 bg-red-500"></div>
      <div class="p-8 max-w-lg mx-auto">
        <div class="flex items-center gap-3 mb-8">
          <div class="w-10 h-10 rounded-xl bg-red-100 flex items-center justify-center">
            <v-icon icon="mdi-alert" color="red"></v-icon>
          </div>
          <h2 class="text-xl font-black text-slate-800">Confirmer la suppression</h2>
        </div>

        <div class="px-5 py-4 bg-slate-50 rounded-xl border border-slate-200 mb-8">
          <p class="text-xs text-slate-400 font-bold uppercase mb-1">Tâche à supprimer :</p>
          <p class="text-sm font-black text-slate-800">{{ task.title }}</p>
        </div>

        <div class="flex items-center justify-between pt-6 border-t">
          <button @click="router.push('/dashboardtask')" class="text-sm font-bold text-slate-500">Annuler</button>
          <button @click="softDeleteTask" :disabled="deleting" class="px-10 py-3 rounded-xl bg-red-500 text-white text-sm font-black shadow-lg hover:bg-red-600">
            {{ deleting ? 'Suppression...' : 'Confirmer' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { TaskService } from '@/services/TaskService'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const deleting = ref(false)
const task = ref<any>({})

const fetchTask = async () => {
  try {
    task.value = await TaskService.getById(Number(route.params.id))
  } finally {
    loading.value = false
  }
}

const softDeleteTask = async () => {
  deleting.value = true
  try {
    await TaskService.softDelete(Number(route.params.id))
    router.push('/dashboardtask')
  } catch (err) {
    console.error('Delete failed', err)
  } finally {
    deleting.value = false
  }
}

onMounted(fetchTask)
</script>