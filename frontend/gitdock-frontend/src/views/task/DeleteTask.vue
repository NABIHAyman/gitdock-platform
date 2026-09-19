<template>
  <div class="space-y-6">

    <!-- HEADER -->
    <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-6 shadow-xl">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 right-0 w-64 h-64 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
      </div>
      <div class="relative flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <div class="w-8 h-8 bg-white/20 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-trash-can-outline" color="white" size="18"></v-icon>
            </div>
            <span class="text-white/70 text-xs font-bold uppercase tracking-widest">Task Management</span>
          </div>
          <h1 class="text-2xl font-black text-white">Delete Task</h1>
          <p class="text-white/60 text-xs mt-0.5">Cette action est réversible (soft delete).</p>
        </div>
        <button
            class="flex items-center gap-2 px-5 py-2.5 bg-white text-[#5b13ec] rounded-xl text-xs font-black shadow-lg hover:bg-purple-50 transition-all"
            @click="router.push('/dashboardtask')"
        >
          <v-icon icon="mdi-arrow-left" size="16"></v-icon>
          RETOUR
        </button>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="bg-white rounded-2xl border border-slate-100 p-16 flex flex-col items-center gap-4">
      <div class="w-12 h-12 rounded-full border-4 border-purple-100 border-t-[#5b13ec] animate-spin"></div>
      <p class="text-sm font-bold text-slate-400">Chargement...</p>
    </div>

    <!-- MODAL CARD -->
    <div v-else class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">

      <!-- Barre rouge top -->
      <div class="h-1 bg-gradient-to-r from-red-500 to-red-400"></div>

      <div class="p-8 max-w-lg mx-auto">

        <!-- Titre -->
        <div class="flex items-center gap-3 mb-8">
          <div class="w-9 h-9 rounded-xl bg-red-500 flex items-center justify-center">
            <v-icon icon="mdi-trash-can-outline" color="white" size="18"></v-icon>
          </div>
          <h2 class="text-xl font-black text-slate-800">Delete Task</h2>
        </div>

        <!-- Task preview -->
        <div class="mb-6">
          <label class="text-xs text-slate-400 font-medium mb-3 block">Task to delete</label>
          <div class="flex items-center gap-4 px-5 py-4 bg-slate-50 rounded-xl border border-slate-200">
            <div class="w-10 h-10 rounded-xl bg-purple-100 flex items-center justify-center flex-shrink-0">
              <v-icon icon="mdi-clipboard-text-outline" color="#5b13ec" size="20"></v-icon>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-black text-slate-800 truncate">{{ task.title || '—' }}</p>
              <div class="flex items-center gap-2 mt-1">
                <span v-if="task.status" :class="['text-xs font-bold px-2 py-0.5 rounded-lg', statusClass(task.status ?? '')]">
                  {{ task.status }}
                </span>
                <span v-if="task.dueDate" class="text-xs text-slate-400">
                  {{ formatDate(task.dueDate) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Warning message -->
        <div class="mb-8 px-4 py-3 bg-amber-50 rounded-xl border border-amber-100 flex items-start gap-3">
          <v-icon icon="mdi-alert-outline" color="#d97706" size="16" class="flex-shrink-0 mt-0.5"></v-icon>
          <p class="text-xs text-amber-700 leading-relaxed">
            Cette tâche sera archivée via un <strong>soft delete</strong>. Elle restera en base et pourra être restaurée.
          </p>
        </div>

        <!-- ACTIONS -->
        <div class="flex items-center justify-between pt-6 border-t border-slate-100">
          <button
              type="button"
              @click="router.push('/dashboardtask')"
              class="px-8 py-3 rounded-xl text-sm font-bold text-slate-500 hover:text-slate-700 hover:bg-slate-50 transition-all"
          >
            Cancel
          </button>
          <button
              type="button"
              @click="softDeleteTask"
              :disabled="deleting"
              class="flex items-center gap-2 px-10 py-3 rounded-xl bg-red-500 text-white text-sm font-black shadow-lg hover:bg-red-600 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <v-icon v-if="deleting" icon="mdi-loading" size="16" class="animate-spin"></v-icon>
            <v-icon v-else icon="mdi-trash-can-outline" size="16"></v-icon>
            {{ deleting ? 'Suppression...' : 'Confirm' }}
          </button>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { TaskService, type TaskListItemDTO } from '@/services/TaskService'

const router  = useRouter()
const route   = useRoute()

const loading  = ref(true)
const deleting = ref(false)
const task     = ref<Partial<TaskListItemDTO>>({})

const fetchTask = async (): Promise<void> => {
  try {
    task.value = await TaskService.getById(route.params.id as string)
  } catch (err) {
    console.error(err)
    alert('Impossible de charger la tâche.')
  } finally {
    loading.value = false
  }
}

const softDeleteTask = async (): Promise<void> => {
  deleting.value = true
  try {
    await TaskService.softDelete(route.params.id as string)
    alert('Tâche supprimée avec succès.')
    router.push('/dashboardtask')
  } catch (err) {
    console.error(err)
    alert('Échec de la suppression.')
  } finally {
    deleting.value = false
  }
}

const statusClass = (s: string) => {
  if (s === 'Done')        return 'bg-green-100 text-green-700'
  if (s === 'In Progress') return 'bg-purple-100 text-purple-700'
  if (s === 'Overdue')     return 'bg-red-100 text-red-700'
  return 'bg-orange-100 text-orange-700'
}

const formatDate = (d: any): string =>
    d ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'

onMounted(fetchTask)
</script>