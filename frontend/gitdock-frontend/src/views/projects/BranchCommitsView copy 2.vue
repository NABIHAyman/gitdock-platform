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
                <td class="px-6 py-4 text-sm text-gray-900 max-w-xs truncate">
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

  <Teleport to="body">
    <Transition name="diff-modal">
      <div v-if="isModalOpen" class="fixed inset-0 z-[300] bg-white flex flex-col overflow-hidden">
        
        <div class="flex-none bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between shadow-sm">
          <div class="min-w-0 flex-1 mr-4">
            <h2 class="text-base font-bold text-gray-900 truncate">{{ selectedCommit?.message }}</h2>
            <div class="flex items-center gap-3 mt-0.5 text-xs text-gray-500">
              <span class="font-medium text-gray-700">{{ selectedCommit?.authorName }}</span>
              <span>·</span>
              <span>{{ formatDate(selectedCommit?.date) }}</span>
              <span>·</span>
              <code class="bg-gray-100 px-1.5 py-0.5 rounded font-mono text-gray-700">
                {{ selectedCommit?.hash?.substring(0, 8) }}
              </code>
              <span v-if="selectedCommit?.additions" class="text-green-700 font-bold">+{{ selectedCommit.additions }}</span>
              <span v-if="selectedCommit?.deletions" class="text-red-700 font-bold">-{{ selectedCommit.deletions }}</span>
            </div>
          </div>
          
          <div class="flex items-center gap-3 flex-none">
            <div class="flex bg-gray-100 rounded-lg p-0.5 text-xs font-bold">
              <button
                @click="viewMode = 'line-by-line'"
                :class="['px-3 py-1.5 rounded-md transition-all', viewMode === 'line-by-line' ? 'bg-white shadow text-gray-900' : 'text-gray-500 hover:text-gray-700']"
              >
                Unifié
              </button>
              <button
                @click="viewMode = 'side-by-side'"
                :class="['px-3 py-1.5 rounded-md transition-all', viewMode === 'side-by-side' ? 'bg-white shadow text-gray-900' : 'text-gray-500 hover:text-gray-700']"
              >
                Split
              </button>
            </div>
            <button @click="closeModal" class="p-2 text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-all" title="Fermer (Échap)">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="flex-1 overflow-auto bg-[#f6f8fa]">
          
          <div v-if="isDiffLoading" class="flex flex-col items-center justify-center h-full text-gray-400 gap-3">
            <span class="animate-spin text-4xl text-blue-500">⟳</span>
            <span class="text-sm font-medium">Chargement du code depuis GitHub...</span>
          </div>
          
          <div v-else-if="!selectedCommit?.diff" class="flex flex-col items-center justify-center h-full text-gray-400 gap-2">
            <v-icon icon="mdi-file-hidden" size="48" class="opacity-30"></v-icon>
            <p class="text-sm">Aucun changement détaillé disponible pour ce commit.</p>
          </div>
          
          <div v-else class="diff2html-wrapper" v-html="renderedDiff" />
          
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import AppLayout from '@/layouts/AppLayout.vue'
import { projectService } from '@/services/projectService'

// Import de diff2html
import { html as diff2html } from 'diff2html'
import 'diff2html/bundles/css/diff2html.min.css'

const route = useRoute()
const projectStore = useProjectStore()

// ── State ──────────────────────────────────────────────────────
const selectedCommit = ref(null)
const isModalOpen    = ref(false)
const isDiffLoading  = ref(false)
const viewMode       = ref('side-by-side') // Par défaut en Split View !

// ── diff2html ──────────────────────────────────────────────────
const renderedDiff = computed(() => {
  if (!selectedCommit.value?.diff) return ''
  return diff2html(selectedCommit.value.diff, {
    drawFileList:  true,
    matching:      'lines',
    outputFormat:  viewMode.value, // 'line-by-line' ou 'side-by-side'
    colorScheme:   'light',
  })
})

// ── Ouvrir la modale + charger le diff depuis le proxy Java ──
const openCommitDetails = async (commit) => {
  selectedCommit.value = { ...commit, diff: null }
  isModalOpen.value    = true
  isDiffLoading.value  = true
  
  try {
    const projectId = route.params.id
    selectedCommit.value.diff = await projectService.getCommitDiff(projectId, commit.hash)
  } catch (e) {
    console.error('Erreur lors du chargement du diff:', e)
    selectedCommit.value.diff = ''
  } finally {
    isDiffLoading.value = false
  }
}

const closeModal = () => {
  isModalOpen.value = false
  setTimeout(() => { selectedCommit.value = null }, 200)
}

// ── Fermer avec Échap ──────────────────────────────────────────
const onKeydown = (e) => { if (e.key === 'Escape') closeModal() }
onMounted(()  => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))

// ── Charger plus ───────────────────────────────────────────────
const loadMoreCommits = async () => {
  const projectId = parseInt(route.params.id)
  const branchId  = parseInt(route.params.branchId)
  const nextPage = projectStore.currentCommitPage + 1
  await projectStore.fetchBranchCommits(projectId, branchId, nextPage)
}

// ── Helpers ────────────────────────────────────────────────────
const getAuthorInitials = (author) => {
  if (!author) return '??'
  const parts = author.split(' ')
  return parts.length >= 2
    ? (parts[0][0] + parts[1][0]).toUpperCase()
    : author.substring(0, 2).toUpperCase()
}

const formatDate = (dateValue) => {
  if (!dateValue) return 'Date inconnue'
  const dateObj = Array.isArray(dateValue)
    ? new Date(dateValue[0], dateValue[1] - 1, dateValue[2], dateValue[3] ?? 0, dateValue[4] ?? 0)
    : new Date(dateValue)
  return isNaN(dateObj.getTime()) ? 'Date inconnue'
    : dateObj.toLocaleDateString('fr-FR', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// ── Init ───────────────────────────────────────────────────────
onMounted(async () => {
  const projectId = parseInt(route.params.id)
  const branchId  = parseInt(route.params.branchId)
  if (projectId && branchId) {
    await projectStore.fetchBranchCommits(projectId, branchId, 0)
  }
})
</script>

<style>
/* 🎨 CUSTOMISATION CSS DE DIFF2HTML POUR MATCH GITHUB PERFECTLY */
.diff2html-wrapper {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.d2h-file-list-wrapper {
  margin-bottom: 1.5rem;
  border-radius: 6px;
  border: 1px solid #d0d7de;
}

.d2h-file-header {
  background-color: #f6f8fa !important;
  border-bottom: 1px solid #d0d7de;
  color: #24292f;
  font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Helvetica,Arial,sans-serif;
}

.d2h-file-wrapper {
  margin-bottom: 2rem;
  border-radius: 6px;
  border: 1px solid #d0d7de;
}

.d2h-code-line, .d2h-code-side-line {
  font-family: ui-monospace,SFMono-Regular,SF Mono,Menlo,Consolas,Liberation Mono,monospace;
  font-size: 12px;
  line-height: 20px;
}

/* Couleurs Vert/Rouge exactes de GitHub */
.d2h-ins { background-color: #e6ffec !important; }
.d2h-ins .d2h-code-line { background-color: #e6ffec !important; }
.d2h-del { background-color: #ffebe9 !important; }
.d2h-del .d2h-code-line { background-color: #ffebe9 !important; }

/* Highlight du texte spécifique qui a changé */
.d2h-ins .d2h-code-line-ctn ins { background-color: #abf2bc; text-decoration: none; }
.d2h-del .d2h-code-line-ctn del { background-color: #ffc0bb; text-decoration: none; }

/* Numéros de ligne */
.d2h-code-linenumber { 
  color: #6e7781; 
  background-color: #ffffff; 
  border-color: #d0d7de; 
}
.d2h-ins .d2h-code-linenumber { background-color: #ccffd8; border-color: #b4f2c8; color: #24292f; }
.d2h-del .d2h-code-linenumber { background-color: #ffdce0; border-color: #fdaeb7; color: #24292f; }

/* Égalité des colonnes en mode Split */
.d2h-code-side-line { width: 50%; }
</style>

<style scoped>
.diff-modal-enter-active, .diff-modal-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.diff-modal-enter-from, .diff-modal-leave-to {
  opacity: 0;
  transform: scale(0.98);
}
</style>