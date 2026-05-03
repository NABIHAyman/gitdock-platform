<template>
  <v-container class="py-12 d-flex justify-center">
    <v-col cols="12" md="6">
      <v-card class="pa-6 rounded-2xl" elevation="6" style="background: #f9fcff; border-color: #edf2f7;">
        <!-- En-tête Formulaire -->
        <h2 class="text-h5 font-weight-bold mb-2" style="color: #1e293b;">Add New Task</h2>
        <p class="text-subtitle-1 mb-6" style="color: #5a6f8c;">
          Configure your task details and assign it to your team members.
        </p>

        <v-form @submit.prevent="submitTask">
          <!-- Task Title -->
          <v-text-field
            v-model="task.title"
            label="Task Name"
            placeholder="e.g., Redesign landing page"
            variant="outlined"
            class="mb-4"
            density="comfortable"
          >
            <template #append>
              <v-icon color="#3b82f6">mdi-pencil-outline</v-icon>
            </template>
          </v-text-field>

          <!-- Description -->
          <v-textarea
            v-model="task.description"
            label="Description"
            placeholder="Describe the task objectives..."
            variant="outlined"
            class="mb-4"
            rows="3"
            density="comfortable"
          ></v-textarea>

          <!-- Due Date -->
          <v-text-field
            v-model="task.dueDate"
            label="Due Date"
            variant="outlined"
            type="date"
            class="mb-4"
            density="comfortable"
            append-icon="mdi-calendar"
          ></v-text-field>

          <!-- Priority -->
          <div class="mb-4">
            <div class="font-weight-medium mb-2" style="color: #1e293b;">Priority</div>
            <v-radio-group v-model="task.priority" row>
              <v-radio label="Low" value="Low" color="#22c55e"></v-radio>
              <v-radio label="Medium" value="Medium" color="#facc15"></v-radio>
              <v-radio label="High" value="High" color="#ef4444"></v-radio>
            </v-radio-group>
          </div>

          <!-- Assign To -->
          <v-select
            v-model="task.assignee"
            :items="teamMembers"
            label="Assign To"
            placeholder="Select teammate..."
            variant="outlined"
            class="mb-4"
            density="comfortable"
            return-object
            item-title="fullName"
            item-value="id"
          ></v-select>

          <!-- Epic -->
          <v-select
            v-model="task.epic"
            :items="epics"
            label="Epic"
            placeholder="Select Epic..."
            variant="outlined"
            class="mb-4"
            density="comfortable"
            return-object
            item-title="title"
            item-value="id"
          ></v-select>

          <!-- Part -->
          <v-select
            v-model="task.part"
            :items="parts"
            label="Part"
            placeholder="Select Part..."
            variant="outlined"
            class="mb-4"
            density="comfortable"
            return-object
            item-title="name"
            item-value="id"
          ></v-select>

          <!-- Level -->
          <v-select
            v-model="task.level"
            :items="levels"
            label="Level"
            placeholder="Select Level..."
            variant="outlined"
            class="mb-4"
            density="comfortable"
            return-object
            item-title="name"
            item-value="id"
          ></v-select>

          <!-- Status -->
          <v-text-field
            v-model="task.status"
            label="Status"
            variant="outlined"
            readonly
            density="comfortable"
            class="mb-6"
          ></v-text-field>

          <!-- Boutons -->
          <div class="d-flex justify-center gap-4">
            <v-btn
              variant="outlined"
              color="#6f85a2"
              rounded="pill"
              @click="cancel"
            >
              Cancel
            </v-btn>
            <v-btn
              color="#1d4ed8"
              rounded="pill"
              type="submit"
              class="text-none font-weight-bold"
            >
              Add Task
            </v-btn>
            <v-btn
              color="#ef4444"
              rounded="pill"
              class="text-none font-weight-bold"
              @click="deleteTask(task.id)"
              v-if="task.id"
            >
              Delete Task
            </v-btn>
          </div>
        </v-form>
      </v-card>
    </v-col>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TaskService, type TaskFormDataDTO } from '@/services/TaskService'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

// ---------------- Modèle de task ----------------
const task = ref({
  id: null,
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

// ---------------- Données du formulaire ----------------
const epics = ref<TaskFormDataDTO['epics']>([])
const parts = ref<TaskFormDataDTO['parts']>([])
const levels = ref<TaskFormDataDTO['levels']>([])
const teamMembers = ref<TaskFormDataDTO['users']>([])

const fetchFormData = async () => {
  try {
    // MOUCHARD 1 : Est-ce qu'on a bien un token dans le store ?
    console.log("MOUCHARD VUE - Token actuel :", authStore.token ? "Présent" : "ABSENT !");

    const data = await TaskService.getFormData()
    
    // MOUCHARD 2 : Qu'est-ce que Symfony nous a renvoyé ?
    console.log("MOUCHARD VUE - Données reçues de Symfony :", data);

    epics.value = data.epics || []
    parts.value = data.parts || []
    levels.value = data.levels || []
    
    teamMembers.value = (data.users || []).map((user: any) => ({
      ...user,
      fullName: `${user.firstName} ${user.lastName}`.trim()
    }))

  } catch (err) {
    console.error('Erreur lors du fetch des données de formulaire:', err)
  }
}

onMounted(() => {
  fetchFormData()
})

// ---------------- Soumission de la task ----------------
const submitTask = async () => {
  try {
    const assignedToId = (task.value.assignee as any)?.id || null
    const dueDateStr = task.value.dueDate ? task.value.dueDate : null
    const assignedById = authStore.userId ?? null

    const data = await TaskService.create({
      title: task.value.title,
      description: task.value.description,
      status: task.value.status,
      dueDate: dueDateStr,
      assignedTo: assignedToId,
      assignedBy: assignedById,
      epic: (task.value.epic as any)?.id || null,
      part: (task.value.part as any)?.id || null,
      level: (task.value.level as any)?.id || null,
    })

    alert(`Task "${data.title}" créée avec succès !`)
    task.value.id = (data as any).id ?? null // On stocke l'ID pour le bouton DELETE
    cancel()
  } catch (error) {
    console.error('Erreur API:', error)
    alert('Erreur API: ' + (error as any)?.message)
  }
}

// ---------------- DELETE ----------------
const deleteTask = async (id) => {
  if (!id) return alert('Impossible de supprimer : ID manquant')
  if (!confirm('Voulez-vous vraiment supprimer cette tâche ?')) return

  try {
    await TaskService.softDelete(id)

    alert('Tâche supprimée avec succès !')
    cancel() // Réinitialiser le formulaire
  } catch (err) {
    console.error('Erreur API DELETE:', err)
    alert('Erreur lors de la suppression: ' + (err as any)?.message)
  }
}

// ---------------- Réinitialiser le formulaire ----------------
const cancel = () => {
  task.value = {
    id: null,
    title: '',
    description: '',
    dueDate: '',
    priority: 'Medium',
    status: 'To Do',
    assignee: null,
    epic: null,
    part: null,
    level: null
  }
}
</script>

<style scoped>
/* Styles conservés tels quels */
</style>