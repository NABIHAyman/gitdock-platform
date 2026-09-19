<script setup lang="ts">
import { ref, onMounted } from 'vue'
// Import des services présents dans ton dossier /services
import { userProgressService } from '@/services/UserProgressService'
import { userBadgeService } from '@/services/UserBadgeService'

// --- INTERFACES POUR FIXER LES ERREURS TS2339 (never) ---
interface Badge {
  id: number;
  name: string;
  iconUrl: string;
  description?: string;
}

interface UserProgress {
  currentXp: number;
  currentLevel: number;
  nextLevelXp: number;
  levelName: string;
}

// --- REFS TYPÉES ---
const stats = ref<UserProgress | null>(null)
const myBadges = ref<Badge[]>([]) // Fixe l'erreur 'never'
const loading = ref(true)
const errorMsg = ref<string | null>(null)

onMounted(async () => {
  try {
    loading.value = true
    // Appel des services liés à ton backend .NET
    const [progressData, badgesData] = await Promise.all([
      userProgressService.getProgress(),
      userBadgeService.getMyBadges()
    ])

    stats.value = progressData
    myBadges.value = badgesData
  } catch (err) {
    console.error("Erreur gamification:", err)
    errorMsg.value = "Impossible de charger vos données de progression."
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="user-gamification-container p-6">
    <!-- Message d'erreur si le backend/CORS bloque -->
    <div v-if="errorMsg" class="bg-red-100 text-red-700 p-4 rounded-lg mb-4">
      {{ errorMsg }}
    </div>

    <div v-if="loading" class="flex justify-center p-10">
      <span>Chargement de vos succès...</span>
    </div>

    <div v-else>
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-800">Ma Progression GitDock</h1>
        <p class="text-gray-600">Suivez vos contributions et débloquez des récompenses.</p>
      </header>

      <!-- Section XP & Niveau -->
      <section v-if="stats" class="bg-white p-6 rounded-xl shadow-sm border mb-8">
        <div class="flex justify-between items-end mb-4">
          <div>
            <span class="text-sm font-semibold text-indigo-600 uppercase tracking-wider">Niveau actuel</span>
            <h2 class="text-3xl font-black text-gray-900">{{ stats.levelName }} (Lvl {{ stats.currentLevel }})</h2>
          </div>
          <div class="text-right">
            <span class="text-gray-500 text-sm">Total XP: <strong>{{ stats.currentXp }}</strong></span>
          </div>
        </div>

        <!-- Barre de progression -->
        <div class="w-full bg-gray-100 rounded-full h-4 overflow-hidden border">
          <div
              class="bg-indigo-600 h-full transition-all duration-1000"
              :style="{ width: (stats.currentXp / stats.nextLevelXp * 100) + '%' }"
          ></div>
        </div>
        <p class="text-xs text-gray-400 mt-2 text-right">
          Plus que {{ stats.nextLevelXp - stats.currentXp }} XP avant le prochain niveau
        </p>
      </section>

      <!-- Galerie des Badges -->
      <section>
        <h3 class="text-xl font-bold text-gray-800 mb-4">Mes Badges Débloqués</h3>

        <div v-if="myBadges.length > 0" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <div
              v-for="badge in myBadges"
              :key="badge.id"
              class="bg-white p-4 rounded-lg border text-center hover:shadow-md transition-shadow"
          >
            <div class="w-16 h-16 mx-auto mb-3 flex items-center justify-center bg-indigo-50 rounded-full">
              <img :src="badge.iconUrl" :alt="badge.name" class="w-10 h-10 object-contain" />
            </div>
            <h4 class="font-bold text-sm text-gray-900">{{ badge.name }}</h4>
            <p v-if="badge.description" class="text-xs text-gray-500 mt-1">{{ badge.description }}</p>
          </div>
        </div>

        <div v-else class="bg-gray-50 p-10 rounded-xl border-2 border-dashed text-center">
          <p class="text-gray-500">Vous n'avez pas encore débloqué de badges. Continuez à contribuer !</p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.user-gamification-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>