<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="flex items-center space-x-4 mb-4">
            <router-link :to="`/projects/${$route.params.id}`" class="text-sm text-accent hover:underline">
              ← Retour au projet
            </router-link>
          </div>
          <h1 class="text-2xl font-semibold tracking-tight text-text">
            Branches - {{ projectStore.currentProject?.name || 'Chargement...' }}
          </h1>
          <p class="mt-1 text-sm text-muted">Branches disponibles dans ce projet</p>
        </div>
      </div>

      <div class="gd-card overflow-hidden">
        <div v-if="projectStore.isLoading" class="p-8 text-center">
          <span class="animate-spin text-2xl">⟳</span>
          <p class="text-muted mt-2">Chargement des branches...</p>
        </div>

        <div v-else-if="projectStore.branches.length === 0" class="p-12 text-center">
          <h3 class="text-lg font-semibold text-text mb-2">Aucune branche trouvée</h3>
          <p class="text-muted">Ce projet ne contient aucune branche synchronisée.</p>
        </div>

        <div v-else>
          <table class="gd-table">
            <thead class="gd-thead">
              <tr>
                <th class="gd-th">Nom de la branche</th>
                <th class="gd-th">Dernière mise à jour</th>
                <th class="gd-th text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-surface divide-y divide-border">
              <tr v-for="branch in projectStore.branches" :key="branch.id" class="gd-tr">
                <td class="px-6 py-4 whitespace-nowrap font-medium text-text">
                  <span class="bg-gray-100 px-2 py-1 rounded text-sm font-mono">{{ branch.name }}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-muted">
                  {{ formatDate(branch.lastUpdated || branch.updatedAt) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <!-- Lien vers les commits de CETTE branche -->
                  <router-link
                    :to="`/projects/${$route.params.id}/branches/${branch.id}/commits`"
                    class="text-accent hover:text-accentHover"
                  >
                    Voir les commits
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import AppLayout from '@/layouts/AppLayout.vue'

const route = useRoute()
const projectStore = useProjectStore()

const formatDate = (dateValue) => {
  if (!dateValue) return 'Date inconnue'

  let dateObj;

  // 1. Si Spring Boot envoie un tableau [Année, Mois, Jour, Heure, Minute, Seconde]
  if (Array.isArray(dateValue)) {
    if (dateValue.length === 0 || dateValue.every(v => v === 0)) return 'Date inconnue'
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateValue
    dateObj = new Date(year, month - 1, day, hour, minute, second)
  } 
  // 2. Si c'est un Timestamp ou un String ISO
  else {
    dateObj = new Date(dateValue)
  }

  // Vérification de sécurité (Invalid Date)
  if (isNaN(dateObj.getTime())) return 'Date inconnue'

  return dateObj.toLocaleDateString('fr-FR', {
    year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

onMounted(async () => {
  const projectId = parseInt(route.params.id)
  if (projectId) {
    await projectStore.fetchProjectBranches(projectId)
  }
})
</script>