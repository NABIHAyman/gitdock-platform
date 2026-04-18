<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <div class="flex items-center space-x-4 mb-4">
            <router-link :to="`/projects/${$route.params.id}/branches`" class="text-sm text-accent hover:underline">
              ← Retour aux branches
            </router-link>
          </div>
          <h1 class="text-2xl font-semibold tracking-tight text-text">
            Historique des Commits
          </h1>
          <p class="mt-1 text-sm text-muted">
            Projet: <span class="font-medium">{{ projectStore.currentProject?.name }}</span>
          </p>
        </div>
      </div>

      <div class="gd-card overflow-hidden">
        <div v-if="projectStore.isLoading" class="p-8 text-center">
          <span class="animate-spin text-2xl">⟳</span>
          <p class="text-muted mt-2">Chargement des commits...</p>
        </div>

        <div v-else-if="projectStore.commits.length === 0" class="p-8 text-center">
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucun commit trouvé</h3>
          <p class="text-gray-500">Cette branche ne contient aucun commit synchronisé.</p>
        </div>

        <div v-else>
          <table class="gd-table">
            <thead class="gd-thead">
              <tr>
                <th class="gd-th">Auteur</th>
                <th class="gd-th">Message</th>
                <th class="gd-th">Date</th>
                <th class="gd-th">SHA</th>
                <th class="gd-th">Stats</th>
              </tr>
            </thead>
            <tbody class="bg-surface divide-y divide-border">
              <tr v-for="commit in projectStore.commits" :key="commit.id" class="gd-tr">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center">
                      <span class="text-xs font-medium text-white">
                        {{ getAuthorInitials(commit.authorName) }}
                      </span>
                    </div>
                    <div class="ml-3 text-sm font-medium text-gray-900">
                      {{ commit.authorName }}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 text-sm text-gray-900">
                  {{ commit.message }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDate(commit.date) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <code class="text-xs bg-gray-100 px-2 py-1 rounded font-mono">
                    {{ (commit.hash || '').substring(0, 8) }}
                  </code>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <div class="flex space-x-2">
                    <span v-if="commit.additions > 0" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">
                      +{{ commit.additions }}
                    </span>
                    <span v-if="commit.deletions > 0" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                      -{{ commit.deletions }}
                    </span>
                    <span v-if="commit.additions === 0 && commit.deletions === 0" class="text-gray-400 text-xs italic">
                      Aucune modif
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          

          <div v-if="projectStore.hasMoreCommits" class="bg-gray-50 px-6 py-4 border-t border-border text-center">
            <button 
              @click="loadMoreCommits" 
              :disabled="projectStore.isFetchingMore"
              class="gd-btn-secondary w-full sm:w-auto"
            >
              <span v-if="projectStore.isFetchingMore" class="animate-spin inline-block mr-2">⟳</span>
              {{ projectStore.isFetchingMore ? 'Chargement...' : 'Charger plus de commits' }}
            </button>
          </div>

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

const getAuthorInitials = (author) => {
  if (!author) return '??'
  const parts = author.split(' ')
  if (parts.length >= 2) return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
  return author.substring(0, 2).toUpperCase()
}

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

const loadMoreCommits = async () => {
  const projectId = parseInt(route.params.id)
  const branchId = parseInt(route.params.branchId)

  const nextPage = projectStore.currentCommitPage  + 1
  
  await projectStore.fetchBranchCommits(projectId, branchId, nextPage)
}

onMounted(async () => {
  const projectId = parseInt(route.params.id)
  const branchId = parseInt(route.params.branchId)

  if (projectId && branchId) {
    await projectStore.fetchBranchCommits(projectId, branchId)
  }
})
</script>