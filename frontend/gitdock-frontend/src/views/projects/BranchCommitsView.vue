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
          <h1 class="text-2xl font-black tracking-tight text-slate-800">Historique des Commits</h1>
        </div>
      </div>

      <div class="gd-card overflow-hidden border border-slate-200 shadow-sm rounded-2xl bg-white">
        <div v-if="projectStore.isLoading" class="p-12 text-center">
          <span class="animate-spin text-3xl text-indigo-600 inline-block">⟳</span>
          <p class="text-slate-500 mt-4 font-bold">Lecture de la chronologie...</p>
        </div>

        <div v-else>
          <table class="w-full border-collapse" style="table-layout: fixed;">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200">
                <th class="px-6 py-4 text-left text-xs font-black text-slate-400 uppercase tracking-widest">Auteur</th>
                <th class="px-6 py-4 text-left text-xs font-black text-slate-400 uppercase tracking-widest">Message</th>
                <th class="px-6 py-4 text-left text-xs font-black text-slate-400 uppercase tracking-widest">Date</th>
                <th class="px-6 py-4 text-left text-xs font-black text-slate-400 uppercase tracking-widest">SHA</th>
                <th class="px-6 py-4 text-left text-xs font-black text-slate-400 uppercase tracking-widest">Stats</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <template v-for="commit in projectStore.commits" :key="commit.id">
                <tr 
                  @click="toggleCommit(commit)"
                  class="group cursor-pointer transition-all hover:bg-indigo-50/30"
                  :class="{'bg-indigo-50/50': expandedCommitId === commit.hash}"
                >
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-[10px] font-black text-white shadow-sm">
                        {{ getAuthorInitials(commit.authorName) }}
                      </div>
                      <span class="text-sm font-bold text-slate-700">{{ commit.authorName }}</span>
                    </div>
                  </td>
                  <td class="px-6 py-4 text-sm text-slate-600 font-medium truncate max-w-md">
                    {{ commit.message }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-xs text-slate-400 font-bold">
                    {{ formatDate(commit.date) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <code class="text-[10px] bg-slate-100 text-slate-500 px-2 py-1 rounded font-mono group-hover:bg-white group-hover:text-indigo-600 transition-colors">
                      {{ commit.hash.substring(0, 8) }}
                    </code>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center gap-2">
                      <span v-if="commit.additions > 0" class="text-[10px] font-black text-emerald-600">+{{ commit.additions }}</span>
                      <span v-if="commit.deletions > 0" class="text-[10px] font-black text-rose-600">-{{ commit.deletions }}</span>
                    </div>
                  </td>
                </tr>

                <tr v-if="expandedCommitId === commit.hash">
                  <td colspan="5" class="p-0 bg-[#f6f8fa] border-b border-indigo-100 shadow-inner">
                      <div style="width: 100%; overflow: hidden; box-sizing: border-box;" class="p-4 sm:p-6">
                      <div v-if="isDiffLoading" class="flex items-center justify-center py-10 gap-3 text-indigo-600">
                        <span class="animate-spin text-2xl">⟳</span>
                        <span class="text-sm font-black uppercase tracking-tighter">Extraction du code source...</span>
                      </div>

                        <div v-else-if="commit.diff" >
                        <div v-if="isFullscreen" class="fixed inset-0 bg-slate-900/80 z-[9998] backdrop-blur-sm" @click.stop="isFullscreen = false"></div>
                        
                        <div :class="isFullscreen ? 'fixed inset-4 sm:inset-10 z-[9999] rounded-2xl border border-slate-200 overflow-hidden bg-white shadow-2xl flex flex-col' : 'rounded-xl border border-slate-200 overflow-hidden bg-white shadow-lg flex flex-col relative max-h-[70vh] w-full'">
                          
                          <div class="bg-white/95 backdrop-blur-sm px-4 py-3 border-b border-slate-100 flex justify-between items-center sticky top-0 z-20 shadow-sm shrink-0">
                            <span class="text-sm font-black text-slate-700 flex items-center">
                              <v-icon icon="mdi-code-braces" size="18" class="mr-2 text-indigo-500"></v-icon>
                              Changements du code
                            </span>
                            <div class="flex items-center gap-2">
                              <div class="flex gap-1 bg-slate-100 p-1 rounded-lg border border-slate-200">
                                <button @click.stop="viewMode = 'line-by-line'" :class="btnClass(viewMode === 'line-by-line')">Unifié</button>
                                <button @click.stop="viewMode = 'side-by-side'" :class="btnClass(viewMode === 'side-by-side')">Split</button>
                              </div>
                              <button @click.stop="isFullscreen = !isFullscreen" class="p-1.5 ml-1 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors" :title="isFullscreen ? 'Réduire' : 'Agrandir plein écran'">
                                <v-icon :icon="isFullscreen ? 'mdi-fullscreen-exit' : 'mdi-fullscreen'" size="20"></v-icon>
                              </button>
                            </div>
                          </div>

                          <div class="diff2html-container overflow-auto p-2 sm:p-4 scroll-smooth flex-1 min-h-0 bg-white" v-html="renderDiff(commit.diff)"></div>
                        </div>
                      </div>

                      <div v-else class="text-center py-10 text-slate-400 italic text-sm">
                        Aucun détail disponible pour ce commit.
                      </div>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
          
          <div v-if="projectStore.hasMoreCommits" class="p-6 bg-slate-50 border-t border-slate-100 text-center">
            <button @click="loadMoreCommits" :disabled="projectStore.isFetchingMore" class="gd-btn-secondary">
              {{ projectStore.isFetchingMore ? 'Chargement...' : 'Voir les commits plus anciens' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import { projectService } from '@/services/projectService'
import AppLayout from '@/layouts/AppLayout.vue'
import { html as diff2html } from 'diff2html'
import 'diff2html/bundles/css/diff2html.min.css'

const route = useRoute()
const projectStore = useProjectStore()

const expandedCommitId = ref(null)
const isDiffLoading = ref(false)
const viewMode = ref('side-by-side')
const isFullscreen = ref(false)

const btnClass = (active) => `px-4 py-1.5 text-xs font-black rounded-md transition-all ${active ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500 hover:text-slate-800 hover:bg-slate-200'}`

const toggleCommit = async (commit) => {
  if (expandedCommitId.value === commit.hash) {
    expandedCommitId.value = null
    isFullscreen.value = false
    return
  }

  isFullscreen.value = false

  expandedCommitId.value = commit.hash
  if (!commit.diff) {
    isDiffLoading.value = true
    try {
      commit.diff = await projectService.getCommitDiff(route.params.id, commit.hash)
    } catch (e) {
      commit.diff = "Impossible de charger le diff."
    } finally {
      isDiffLoading.value = false
    }
  }
}

const renderDiff = (diffText) => {
  return diff2html(diffText, {
    drawFileList: true,
    matching: 'lines',
    outputFormat: viewMode.value,
    colorScheme: 'light'
  })
}

const getAuthorInitials = (n) => n ? n.split(' ').map(x => x[0]).join('').toUpperCase().substring(0, 2) : '??'

const formatDate = (d) => {
  if (!d) return 'N/A'
  const dateObj = Array.isArray(d) ? new Date(d[0], d[1]-1, d[2], d[3]||0, d[4]||0) : new Date(d)
  return dateObj.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const loadMoreCommits = () => {
  projectStore.fetchBranchCommits(route.params.id, route.params.branchId, projectStore.currentCommitPage + 1)
}

onMounted(() => {
  if (route.params.id && route.params.branchId) {
    projectStore.fetchBranchCommits(route.params.id, route.params.branchId, 0)
  }
})
</script>
<style>

/* ============================================================
   GITDOCK DIFF VIEWER — CSS DÉFINITIF
   ============================================================ */

.diff2html-container {
  font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.5;
  overflow-x: auto;
}

/* FIX #2 : prefix "+" et code sur la même ligne */
.d2h-code-line,
.d2h-code-side-line {
  display: flex !important;
  align-items: center !important;
  white-space: pre !important;
  word-wrap: normal !important;
  padding: 1px 10px !important;
  min-height: 20px !important;
}

.d2h-code-line-prefix {
  flex-shrink: 0;
  user-select: none;
  color: inherit;
}

.d2h-code-line-ctn {
  flex: 1;
  overflow: hidden;
}

/* FIX #3 : Split view — utiliser flex au lieu de inline-block */
.d2h-files-diff {
  display: flex !important;
  width: 100% !important;
}

.d2h-file-side-diff {
  flex: 1 1 50% !important;
  width: 50% !important;
  min-width: 0 !important;       /* ← Clé : empêche flex de déborder */
  overflow-x: auto !important;
  display: block !important;
}

/* Le tableau interne ne doit pas être fixed-layout ici */
.d2h-diff-table {
  width: 100% !important;
  border-collapse: collapse !important;
  table-layout: auto !important;
}

/* Couleurs GitHub exactes */
.d2h-ins { background-color: #e6ffec !important; }
.d2h-del { background-color: #ffebe9 !important; }

/* Placeholders pour fichiers purement ajoutés/supprimés */
.d2h-code-side-emptyplaceholder,
.d2h-emptyplaceholder {
  background-color: #f6f8fa !important;
  border-color: #d0d7de !important;
  height: 20px !important;
}

/* Numéros de ligne */
.d2h-code-linenumber,
.d2h-code-side-linenumber {
  position: static !important;   /* ← Tue la barre verticale fantôme */
  background-color: #f6f8fa !important;
  color: #6e7781 !important;
  border-right: 1px solid #d0d7de !important;
  border-left: none !important;
  width: 48px !important;
  min-width: 48px !important;
  text-align: right !important;
  padding: 0 8px !important;
  vertical-align: top !important;
}

/* Header de fichier sticky */
.d2h-file-header {
  display: flex !important;
  position: sticky !important;
  top: 0;
  z-index: 10;
  background-color: #f6f8fa !important;
  border-bottom: 1px solid #d0d7de !important;
  padding: 6px 12px !important;
  font-size: 12px;
  font-weight: 600;
  color: #24292f;
}

/* Cadre fichier */
.d2h-file-wrapper {
  border: 1px solid #d0d7de !important;
  border-radius: 6px !important;
  margin-bottom: 24px !important;
  overflow: hidden !important;
}

/* Suppression de la barre de scroll native sur la table elle-même */
.d2h-diff-tbody tr td {
  vertical-align: top !important;
}

</style>