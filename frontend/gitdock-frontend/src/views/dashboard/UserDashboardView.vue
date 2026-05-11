<template>
  <div class="p-6">
    <h1 class="text-2xl font-black text-slate-800 mb-6">Tableau de Bord</h1>

    <!-- NIVEAU + XP -->
    <div v-if="progress" class="mb-6 bg-gradient-to-r from-[#5b13ec] to-indigo-500 rounded-2xl p-6 text-white">
      <div class="flex justify-between items-end mb-3">
        <div>
          <p class="text-white/60 text-xs font-bold uppercase tracking-widest mb-1">Mon niveau</p>
          <h2 class="text-2xl font-black">
            {{ progress.levelName || progress.level?.name || 'Junior' }}
            <span class="text-white/60 text-lg ml-1">Lv.{{ progress.currentLevel ?? progress.level?.levelRank ?? 1 }}</span>
          </h2>
        </div>
        <p class="text-2xl font-black">{{ progress.totalExperience ?? progress.xp ?? 0 }} <span class="text-sm opacity-60">XP</span></p>
      </div>
      <div class="w-full bg-white/20 rounded-full h-2">
        <div class="bg-white h-full rounded-full transition-all duration-1000" style="width: 40%"></div>
      </div>
    </div>

    <!-- KANBAN TÂCHES -->
    <v-row>
      <v-col cols="12" md="4">
        <v-card color="grey-lighten-4" variant="flat" class="pa-4 rounded-xl min-h-[400px]">
          <div class="text-overline font-weight-black mb-4 text-grey-darken-1">À FAIRE</div>
          <div v-for="t in tasks.filter(task => task.status === 'todo' || task.status === 'Pending')"
               :key="t.id" class="bg-white pa-4 mb-3 rounded-lg shadow-sm border-s-4 border-grey">
            <div class="font-weight-bold text-slate-700">{{ t.title }}</div>
            <v-btn size="x-small" color="indigo" variant="tonal" class="mt-3 font-weight-black"
                   @click="changeStatus(t, 'in_progress')">DÉMARRER</v-btn>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" md="4">
        <v-card color="blue-lighten-5" variant="flat" class="pa-4 rounded-xl min-h-[400px]">
          <div class="text-overline font-weight-black mb-4 text-blue-darken-2">EN COURS</div>
          <div v-for="t in tasks.filter(task => task.status === 'in_progress' || task.status === 'In Progress')"
               :key="t.id" class="bg-white pa-4 mb-3 rounded-lg shadow-sm border-s-4 border-blue">
            <div class="font-weight-bold text-slate-700">{{ t.title }}</div>
            <v-btn size="x-small" color="success" variant="tonal" class="mt-3 font-weight-black"
                   @click="changeStatus(t, 'done')">TERMINER</v-btn>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" md="4">
        <v-card color="green-lighten-5" variant="flat" class="pa-4 rounded-xl min-h-[400px]">
          <div class="text-overline font-weight-black mb-4 text-green-darken-2">TERMINÉ</div>
          <div v-for="t in tasks.filter(task => task.status === 'done' || task.status === 'Done')"
               :key="t.id" class="bg-white pa-4 mb-3 rounded-lg shadow-sm border-s-4 border-green opacity-70">
            <div class="font-weight-bold text-slate-700 line-through">{{ t.title }}</div>
            <div class="text-[10px] text-green-600 font-bold mt-1">✓ Terminé</div>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TaskService } from '@/services/TaskService'
import { userProgressService } from '@/services/UserProgressService'
import { useAuthStore } from '@/stores/authStore'

const tasks    = ref<any[]>([])
const progress = ref<any>(null)

const fetchUserTasks = async () => {
  try {
    const data = await TaskService.list()
    tasks.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Erreur chargement tâches:', error)
  }
}

onMounted(async () => {
  await fetchUserTasks()
  try {
    const authStore = useAuthStore()
    if (authStore.userId) {
      progress.value = await userProgressService.getProgress(authStore.userId)
      console.log('✅ progress:', JSON.stringify(progress.value))
    }
  } catch (err) {
    console.error('Erreur chargement progression:', err)
  }
})

const changeStatus = async (task: any, newStatus: string) => {
  try {
    await TaskService.update(task.id, { status: newStatus } as any)
    await fetchUserTasks()
  } catch (error) {
    console.error('Erreur changement statut:', error)
  }
}
</script>

<style scoped>
.bg-white { transition: transform 0.2s ease; }
.bg-white:hover { transform: translateY(-2px); }
</style>