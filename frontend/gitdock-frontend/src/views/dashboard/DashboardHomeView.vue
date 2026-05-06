<template>
  <div class="space-y-8">

    <!-- HEADER -->
    <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-3xl p-8 shadow-2xl">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 right-0 w-96 h-96 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
        <div class="absolute bottom-0 left-0 w-64 h-64 bg-white rounded-full translate-y-1/2 -translate-x-1/2"></div>
      </div>
      <div class="relative">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
            <v-icon icon="mdi-view-dashboard" color="white" size="22"></v-icon>
          </div>
          <span class="text-white/70 text-sm font-bold uppercase tracking-widest">GitDock</span>
        </div>
        <h1 class="text-3xl font-black text-white mb-1">Bonjour, {{ authStore.firstName }} 👋</h1>
        <p class="text-white/60 text-sm">Vue d'ensemble de vos projets et analyses de securite.</p>
      </div>
    </div>

    <!-- STATS -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-folder-multiple" color="#5b13ec" size="18"></v-icon>
          </div>
          <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Projets</span>
        </div>
        <p class="text-3xl font-black text-slate-800">{{ stats.totalProjects }}</p>
        <p class="text-xs text-slate-400 mt-1">Total enregistres</p>
      </div>
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 bg-emerald-50 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-shield-check" color="#10b981" size="18"></v-icon>
          </div>
          <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Sains</span>
        </div>
        <p class="text-3xl font-black text-emerald-600">{{ stats.healthyProjects }}</p>
        <p class="text-xs text-slate-400 mt-1">Sans anomalie</p>
      </div>
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 bg-red-50 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-alert-circle" color="#ef4444" size="18"></v-icon>
          </div>
          <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Anomalies</span>
        </div>
        <p class="text-3xl font-black text-red-600">{{ stats.totalAnomalies }}</p>
        <p class="text-xs text-slate-400 mt-1">Detectees au total</p>
      </div>
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 bg-amber-50 rounded-xl flex items-center justify-center">
            <v-icon icon="mdi-source-commit" color="#f59e0b" size="18"></v-icon>
          </div>
          <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Commits</span>
        </div>
        <p class="text-3xl font-black text-slate-800">{{ stats.totalCommits }}</p>
        <p class="text-xs text-slate-400 mt-1">Analyses au total</p>
      </div>
    </div>

    <!-- PROJETS + AI -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

      <!-- MES PROJETS -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center">
              <v-icon icon="mdi-folder-multiple" color="#5b13ec" size="16"></v-icon>
            </div>
            <h3 class="font-black text-slate-800 text-sm">Mes Projets</h3>
          </div>
          <router-link to="/projects" class="text-xs font-bold text-[#5b13ec] hover:underline">Voir tout →</router-link>
        </div>
        <div class="divide-y divide-slate-50">
          <div v-if="projects.length === 0" class="p-8 text-center">
            <v-icon icon="mdi-folder-open-outline" color="#cbd5e1" size="36" class="mb-2"></v-icon>
            <p class="text-sm text-slate-400 font-bold">Aucun projet encore</p>
          </div>
          <div v-for="project in projects.slice(0, 5)" :key="project.id"
               @click="goToProject(project.id)"
               class="flex items-center gap-3 px-6 py-3 hover:bg-slate-50 cursor-pointer transition-colors">
            <div class="w-8 h-8 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center flex-shrink-0">
              <v-icon icon="mdi-folder" color="#5b13ec" size="16"></v-icon>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-black text-slate-700 truncate">{{ project.name }}</p>
              <p class="text-xs text-slate-400">{{ getHostname(project.url) }}</p>
            </div>
            <span class="text-[10px] font-black px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
              {{ (project.platform || '').toUpperCase() }}
            </span>
          </div>
        </div>
      </div>

      <!-- GITDOCK AI -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center gap-2">
          <div class="w-7 h-7 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center">
            <v-icon icon="mdi-brain" color="#5b13ec" size="16"></v-icon>
          </div>
          <h3 class="font-black text-slate-800 text-sm">GitDock AI — Analyse</h3>
          <span class="ml-auto bg-[#5b13ec]/10 text-[#5b13ec] text-[10px] font-black px-2 py-0.5 rounded-full">SECURITY</span>
        </div>
        <div class="p-6 space-y-4">
          <div class="flex gap-2">
            <div class="flex-1 relative">
              <v-icon icon="mdi-magnify" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size="16"></v-icon>
              <input v-model="aiProjectName" @keyup.enter="analyzeProject" placeholder="Nom du projet..."
                     class="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:border-[#5b13ec]/50 focus:bg-white transition-all"/>
            </div>
            <button @click="analyzeProject" :disabled="aiLoading || !aiProjectName.trim()"
                    class="px-4 py-2.5 bg-[#5b13ec] text-white rounded-xl font-black text-xs hover:bg-[#4a0fd4] disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1.5 shadow-md transition-all">
              <v-progress-circular v-if="aiLoading" indeterminate size="12" width="2" color="white"></v-progress-circular>
              <v-icon v-else icon="mdi-lightning-bolt" size="14"></v-icon>
              {{ aiLoading ? '...' : 'Analyser' }}
            </button>
          </div>

          <!-- RACCOURCIS -->
          <div v-if="projects.length > 0" class="flex flex-wrap gap-1.5">
            <button v-for="p in projects.slice(0, 4)" :key="p.id"
                    @click="aiProjectName = p.name; analyzeProject()"
                    class="text-[10px] font-black px-2.5 py-1 bg-slate-100 hover:bg-[#5b13ec]/10 hover:text-[#5b13ec] text-slate-500 rounded-full transition-all">
              {{ p.name }}
            </button>
          </div>

          <!-- ERREUR -->
          <div v-if="aiError" class="bg-red-50 border border-red-100 rounded-xl p-3 flex items-center gap-2">
            <v-icon icon="mdi-alert-circle" color="red" size="16"></v-icon>
            <p class="text-red-600 text-xs font-bold">{{ aiError }}</p>
          </div>

          <!-- RESULTATS -->
          <div v-if="aiResult" class="space-y-3">
            <div class="grid grid-cols-3 gap-2">
              <div class="bg-slate-50 rounded-xl p-3 border border-slate-100 text-center">
                <p class="text-xl font-black text-slate-800">{{ aiResult.total }}</p>
                <p class="text-[10px] text-slate-400 font-bold uppercase">Commits</p>
              </div>
              <div :class="['rounded-xl p-3 border text-center', aiResult.anomalies > 0 ? 'bg-red-50 border-red-100' : 'bg-emerald-50 border-emerald-100']">
                <p :class="['text-xl font-black', aiResult.anomalies > 0 ? 'text-red-600' : 'text-emerald-600']">{{ aiResult.anomalies }}</p>
                <p class="text-[10px] text-slate-400 font-bold uppercase">Anomalies</p>
              </div>
              <div :class="['rounded-xl p-3 border text-center', aiResult.anomalies === 0 ? 'bg-emerald-50 border-emerald-100' : 'bg-amber-50 border-amber-100']">
                <p :class="['text-lg font-black', aiResult.anomalies === 0 ? 'text-emerald-600' : 'text-amber-600']">{{ aiResult.anomalies === 0 ? '✓' : '⚠' }}</p>
                <p class="text-[10px] text-slate-400 font-bold uppercase">Statut</p>
              </div>
            </div>
            <div v-if="aiResult.details?.length" class="space-y-1.5 max-h-40 overflow-y-auto">
              <div v-for="a in aiResult.details" :key="a.sha" class="bg-red-50 border border-red-100 rounded-xl p-2.5 flex items-start gap-2">
                <v-icon icon="mdi-bug" color="red" size="14" class="mt-0.5 flex-shrink-0"></v-icon>
                <div class="min-w-0 flex-1">
                  <p class="text-xs font-black text-red-800 truncate">{{ a.message }}</p>
                  <p class="text-[10px] text-red-400 font-mono">{{ a.sha?.slice(0, 8) }} · {{ a.reason }}</p>
                </div>
              </div>
            </div>
            <div v-else class="bg-emerald-50 border border-emerald-100 rounded-xl p-3 text-center">
              <p class="text-emerald-700 font-black text-sm">✓ Projet sain — aucune anomalie</p>
            </div>
          </div>

          <!-- VIDE -->
          <div v-if="!aiResult && !aiLoading && !aiError" class="text-center py-6">
            <div class="w-12 h-12 bg-[#5b13ec]/10 rounded-2xl flex items-center justify-center mx-auto mb-2">
              <v-icon icon="mdi-shield-search" color="#5b13ec" size="22"></v-icon>
            </div>
            <p class="text-xs font-black text-slate-400">Selectionnez un projet pour lancer l'analyse IA</p>
          </div>
        </div>
      </div>
    </div>

    <!-- RAPPORT GLOBAL -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 bg-slate-900 rounded-lg flex items-center justify-center">
            <v-icon icon="mdi-brain" color="white" size="16"></v-icon>
          </div>
          <h3 class="font-black text-slate-800 text-sm">Rapport Global — Tous les Projets</h3>
        </div>
        <button @click="loadGlobalReport" :disabled="globalLoading"
                class="flex items-center gap-1.5 px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-black hover:bg-slate-700 transition-all disabled:opacity-50">
          <v-progress-circular v-if="globalLoading" indeterminate size="12" width="2" color="white"></v-progress-circular>
          <v-icon v-else icon="mdi-refresh" size="14"></v-icon>
          {{ globalLoading ? 'Chargement...' : 'Analyser tout' }}
        </button>
      </div>
      <div class="p-6">
        <div v-if="globalReport.length === 0 && !globalLoading" class="text-center py-8">
          <v-icon icon="mdi-chart-bar" color="#cbd5e1" size="40" class="mb-2"></v-icon>
          <p class="text-sm text-slate-400 font-bold">Cliquez sur "Analyser tout" pour le rapport complet</p>
        </div>
        <div v-if="globalReport.length > 0" class="space-y-3">
          <div v-for="item in globalReport" :key="item.project_name"
               class="flex items-center gap-4 p-4 rounded-xl border"
               :class="item.anomalies_found > 0 ? 'bg-red-50 border-red-100' : 'bg-emerald-50 border-emerald-100'">
            <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                 :class="item.anomalies_found > 0 ? 'bg-red-100' : 'bg-emerald-100'">
              <v-icon :icon="item.anomalies_found > 0 ? 'mdi-alert' : 'mdi-shield-check'"
                      :color="item.anomalies_found > 0 ? 'red' : '#10b981'" size="16"></v-icon>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-black text-slate-700 truncate">{{ item.project_name }}</p>
              <p class="text-xs text-slate-400">{{ item.total_commits }} commits analyses</p>
            </div>
            <div class="text-right flex-shrink-0">
              <p :class="['text-lg font-black', item.anomalies_found > 0 ? 'text-red-600' : 'text-emerald-600']">{{ item.anomalies_found }}</p>
              <p class="text-[10px] text-slate-400 font-bold uppercase">anomalies</p>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'
import { useProjectStore } from '@/stores/projectStore'

const authStore = useAuthStore()
const projectStore = useProjectStore()
const router = useRouter()

const AI_API = 'http://localhost:8000/api/v1'

const stats = ref({ totalProjects: 0, healthyProjects: 0, totalAnomalies: 0, totalCommits: 0 })
const projects = computed(() => (projectStore.projects || []) as any[])

const aiProjectName = ref('')
const aiLoading = ref(false)
const aiError = ref('')
const aiResult = ref<any>(null)
const globalReport = ref<any[]>([])
const globalLoading = ref(false)

const analyzeProject = async () => {
  if (!aiProjectName.value.trim()) return
  aiLoading.value = true
  aiError.value = ''
  aiResult.value = null
  try {
    const res = await axios.get(`${AI_API}/analyze-project`, {
      params: { project_name: aiProjectName.value.trim() }
    })
    aiResult.value = res.data
    if (res.data.anomalies === 0) stats.value.healthyProjects++
    else stats.value.totalAnomalies += res.data.anomalies
    stats.value.totalCommits += res.data.total
  } catch (err: any) {
    aiError.value = err.response?.data?.detail || "Projet introuvable ou erreur d'analyse."
  } finally {
    aiLoading.value = false
  }
}

const loadGlobalReport = async () => {
  globalLoading.value = true
  try {
    // Envoie les URLs des projets (pas les noms)
    const projectUrls = projects.value.map((p: any) => {
      try {
        const url = new URL(p.url)
        return url.pathname.replace('/', '').replace('.git', '')  // ex: RihabAddou/Todo
      } catch {
        return p.name
      }
    }).join(',')

    const res = await axios.get(`${AI_API}/analyze-all`, {
      params: { projects: projectUrls }
    })
    globalReport.value = res.data.report || []
    stats.value.totalAnomalies = globalReport.value.reduce((s: number, r: any) => s + r.anomalies_found, 0)
    stats.value.totalCommits = globalReport.value.reduce((s: number, r: any) => s + r.total_commits, 0)
    stats.value.healthyProjects = globalReport.value.filter((r: any) => r.anomalies_found === 0).length
  } catch (err) {
    console.error(err)
  } finally {
    globalLoading.value = false
  }
}

const goToProject = (id: any) => router.push(`/projects/${id}`)

const getHostname = (url: any) => {
  if (!url) return ''
  try { return new URL(url).hostname.replace('www.', '') } catch { return url }
}

onMounted(async () => {
  await projectStore.fetchProjects()
  stats.value.totalProjects = projects.value.length
})
</script>