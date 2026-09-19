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
              <tr v-for="commit in projectStore.commits" :key="commit.id" 
                  class="gd-tr hover:bg-slate-50 cursor-pointer transition-colors"
                  @click="openCommitDetails(commit)">
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

  <div v-if="isModalOpen" class="relative z-[100]" aria-labelledby="slide-over-title" role="dialog" aria-modal="true">
      <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" @click="closeCommitModal"></div>

      <div class="fixed inset-0 overflow-hidden">
        <div class="absolute inset-0 overflow-hidden">
          <div class="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10 sm:pl-16">
            
            <div class="pointer-events-auto w-screen max-w-3xl transform transition-all duration-300 ease-in-out flex h-full flex-col bg-white shadow-2xl">
              
              <div class="bg-slate-50 px-6 py-6 border-b border-slate-200">
                <div class="flex items-start justify-between">
                  <div>
                    <h2 class="text-xl font-black text-slate-800" id="slide-over-title">
                      {{ selectedCommit?.message }}
                    </h2>
                    <div class="mt-3 flex items-center flex-wrap gap-3 text-sm">
                      <span class="inline-flex items-center gap-1.5 font-bold text-slate-700 bg-white border border-slate-200 px-2.5 py-1 rounded-lg shadow-sm">
                        <v-icon icon="mdi-account-circle" size="16" class="text-indigo-600"></v-icon>
                        {{ selectedCommit?.authorName }}
                      </span>
                      <span class="text-slate-500 flex items-center gap-1">
                        <v-icon icon="mdi-clock-outline" size="14"></v-icon>
                        {{ formatDate(selectedCommit?.date) }}
                      </span>
                      <code class="text-xs bg-indigo-50 border border-indigo-100 text-indigo-700 px-2 py-1 rounded-md font-mono font-bold">
                        <v-icon icon="mdi-source-commit" size="14" class="mr-1"></v-icon>
                        {{ selectedCommit?.hash.substring(0, 8) }}
                      </code>
                    </div>
                  </div>
                  <div class="ml-3 flex h-7 items-center">
                    <button @click="closeCommitModal" type="button" class="relative rounded-lg bg-white p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 border border-slate-200 shadow-sm transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500">
                      <v-icon icon="mdi-close" size="20"></v-icon>
                    </button>
                  </div>
                </div>
              </div>

              <div v-if="isDiffLoading" class="flex flex-col items-center justify-center h-full text-slate-400">
                 <v-icon icon="mdi-code-braces" size="48" class="mb-4 opacity-50 animate-pulse"></v-icon>
                 <p class="text-sm font-bold animate-pulse">Extraction du code classifié...</p>
              </div>
                
                <div v-else-if="!selectedCommit?.diff" class="flex flex-col items-center justify-center h-full text-slate-400">
                 <v-icon icon="mdi-file-hidden" size="48" class="mb-4 opacity-50"></v-icon>
                 <p class="text-sm font-bold">Aucun changement détaillé disponible.</p>
                </div>
                
                <div v-else class="font-mono text-[13px] leading-relaxed">
                  <div v-for="line in parseDiff(selectedCommit.diff)" :key="line.id"
                       class="px-4 py-0.5 whitespace-pre-wrap break-all border-l-2"
                       :class="{
                         'bg-[#042a1f] border-green-500 text-[#7ee787]': line.type === 'add',
                         'bg-[#3a0d15] border-red-500 text-[#ffa198]': line.type === 'remove',
                         'bg-[#1f2937] border-indigo-500 text-[#c9d1d9] font-bold py-3 mt-4 rounded-t-lg': line.type === 'file',
                         'bg-[#1e1e2e] border-slate-600 text-[#a5b4fc] font-bold py-1': line.type === 'header',
                         'border-transparent text-[#8b949e]': line.type === 'context'
                       }">
                    {{ line.content }}
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import AppLayout from '@/layouts/AppLayout.vue'
import { projectService } from '@/services/projectService'

const route = useRoute()
const projectStore = useProjectStore()

// 👇 AJOUTE CES VARIABLES D'ÉTAT
const selectedCommit = ref(null)
const isModalOpen = ref(false)
const isDiffLoading = ref(false)

/*
// 👇 AJOUTE CES FONCTIONS DE GESTION
const openCommitDetails = (commit) => {
  selectedCommit.value = commit
  isModalOpen.value = true
}
*/

const closeCommitModal = () => {
  isModalOpen.value = false
  setTimeout(() => { selectedCommit.value = null }, 300) // Petit délai pour l'animation
}

// Fonction utilitaire pour parser un diff brut en un tableau de lignes typées
const parseDiff = (diffText) => {
  if (!diffText) return []
  return diffText.split('\n').map((line, index) => {
    let type = 'context'
    if (line.startsWith('+') && !line.startsWith('+++')) type = 'add'
    else if (line.startsWith('-') && !line.startsWith('---')) type = 'remove'
    else if (line.startsWith('@@')) type = 'header'
    else if (line.startsWith('---') || line.startsWith('+++')) type = 'file'

    return { id: index, content: line, type }
  })
}

const openCommitDetails = async (commit) => {
    selectedCommit.value = { ...commit, diff: null }
    isModalOpen.value = true
    isDiffLoading.value = true
    
    try {
      const projectId = route.params.id
      const diffCode = await projectService.getCommitDiff(projectId, commit.hash)
      selectedCommit.value.diff = diffCode
    } catch (error) {
      selectedCommit.value.diff = "Erreur lors du chargement du code."
    } finally {
      isDiffLoading.value = false
    }
  }

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
    await projectStore.fetchBranchCommits(projectId, branchId, 0)
  }
})
</script>