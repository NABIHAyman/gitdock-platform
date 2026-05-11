<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useRole } from '@/composables/useRole'
import { TaskService } from '@/services/TaskService'
import { useNotificationStore } from '@/stores/notificationStore'

const router = useRouter()
const authStore = useAuthStore()
const { isDevUser, canCreateTask } = useRole()
const notifStore = useNotificationStore()

const tasks = ref<any[]>([])
const loading = ref(true)
const markingId = ref<number | null>(null)

const stats = computed(() => {
  const now = new Date()
  return {
    total:      tasks.value.length,
    inProgress: tasks.value.filter(t => t.status === 'in_progress').length,
    done:       tasks.value.filter(t => t.status === 'done').length,
    overdue:    tasks.value.filter(t => t.status !== 'done' && t.due_date && new Date(t.due_date) < now).length,
  }
})

const isOverdue = (task: any) => {
  if (!task.due_date || task.status === 'done') return false
  return new Date(task.due_date) < new Date()
}

const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })

const statusLabel = (status: string) => ({
  done: 'Terminé',
  in_progress: 'En cours',
  todo: 'À faire',
}[status] || status)

const fetchTasks = async () => {
  loading.value = true
  try {
    if (isDevUser.value && authStore.userId) {
      tasks.value = await TaskService.getByAssignee(authStore.userId)
    } else {
      tasks.value = await TaskService.list()
    }
  } catch (e) {
    notifStore.error('Erreur lors du chargement des tâches.')
  } finally {
    loading.value = false
  }
}

const markDone = async (task: any) => {
  markingId.value = task.id
  try {
    await TaskService.markAsDone(task.id)
    task.status = 'done'
    notifStore.success(`Tâche "${task.title}" terminée !`)
  } finally {
    markingId.value = null
  }
}

const openAssignModal = () => router.push('/addtask')
const editTask = (task: any) => router.push(`/task-edit/${task.id}`)
const confirmDelete = (task: any) => router.push(`/task-delete/${task.id}`)

onMounted(fetchTasks)
</script>

<template>
  <!-- Garde ton template tel quel, les fonctions sont maintenant liées -->
</template>