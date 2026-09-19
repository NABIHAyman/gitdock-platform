<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight text-text">
            {{ projectStore.currentProject?.name || 'Chargement...' }}
          </h1>
          <p class="mt-1 text-sm text-muted">
            Vue d'ensemble du projet
          </p>
        </div>
        <div class="flex items-center gap-3">
          <button @click="handleSync" :disabled="projectStore.isSyncing" class="gd-btn-primary flex items-center gap-2">
            <span v-if="projectStore.isSyncing" class="animate-spin inline-block">⟳</span>
            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Actualiser
          </button>

          <button @click="handlePremiumSync" class="gd-btn-secondary flex items-center gap-2 group relative overflow-hidden" title="Réservé aux plans PRO">
            <svg class="w-4 h-4 text-amber-500" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M5 2a1 1 0 011 1v1h1a1 1 0 010 2H6v1a1 1 0 01-2 0V6H3a1 1 0 010-2h1V3a1 1 0 011-1zm0 10a1 1 0 011 1v1h1a1 1 0 110 2H6v1a1 1 0 11-2 0v-1H3a1 1 0 110-2h1v-1a1 1 0 011-1zM12 2a1 1 0 01.967.744L14.146 7.2 17.5 9.134a1 1 0 010 1.732l-3.354 1.935-1.18 4.455a1 1 0 01-1.933 0L9.854 12.8 6.5 10.866a1 1 0 010-1.732l3.354-1.935 1.18-4.455A1 1 0 0112 2z" clip-rule="evenodd" />
            </svg>
            Historique Complet
          </button>

          <router-link to="/projects" class="gd-btn-secondary">Retour à la liste</router-link>
        </div>
      </div>

      <!-- ✅ SYNC FAILED banner -->
      <div v-if="syncFailed" class="bg-red-50 border border-red-200 rounded-lg p-5 flex items-start mb-6">
        <div class="flex-shrink-0 mt-0.5">
          <svg class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="ml-4">
          <h3 class="text-base font-medium text-red-900">Échec de la synchronisation</h3>
          <p class="text-sm text-red-700 mt-1">
            L'aspirateur GitDock n'a pas pu accéder à ce dépôt (Erreur 404). S'il s'agit d'un dépôt privé, veuillez lier votre compte GitHub depuis votre profil, puis supprimer et recréer ce projet.
          </p>
        </div>
      </div>

      <!-- ✅ SYNCING banner — only shown when isSyncing AND not failed -->
      <div v-else-if="projectStore.isSyncing"
           class="bg-blue-50 border border-blue-200 rounded-xl p-5 flex items-center gap-4 mb-6 shadow-sm">
        <div class="flex-shrink-0 animate-spin">
          <v-icon icon="mdi-cloud-sync-outline" color="blue" size="28"></v-icon>
        </div>
        <div>
          <h3 class="text-base font-bold text-blue-900">Synchronisation en cours...</h3>
          <p class="text-sm text-blue-700 mt-1">L'aspirateur GitDock télécharge l'historique depuis GitHub. Cela peut prendre quelques secondes.</p>
        </div>
      </div>

      <!-- ✅ EMPTY STATE — shown only when not syncing, not failed, and still empty -->
      <div v-else-if="projectStore.branches.length === 0 && stats.totalCommits === 0"
           class="bg-yellow-50 border border-yellow-200 rounded-xl p-5 flex items-center gap-4 mb-6 shadow-sm">
        <div class="flex-shrink-0">
          <v-icon icon="mdi-source-branch-remove" color="orange" size="28"></v-icon>
        </div>
        <div>
          <h3 class="text-base font-bold text-yellow-900">Aucune donnée disponible</h3>
          <p class="text-sm text-yellow-700 mt-1">Cliquez sur "Actualiser" pour lancer la synchronisation.</p>
        </div>
      </div>

      <!-- Stats cards -->
      <div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <div class="gd-card">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <svg class="h-8 w-8 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-muted truncate">Branches</dt>
                  <dd class="text-lg font-medium text-text">{{ projectStore.branches.length }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="gd-card">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <svg class="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-muted truncate">Tags</dt>
                  <dd class="text-lg font-medium text-text">{{ stats.totalTags }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="gd-card">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <svg class="h-8 w-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-muted truncate">Collaborateurs</dt>
                  <dd class="text-lg font-medium text-text">{{ stats.totalCollaborators }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="gd-card">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <svg class="h-8 w-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-muted truncate">Commits</dt>
                  <dd class="text-lg font-medium text-text">{{ stats.totalCommits }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="gd-card mb-6">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg font-medium text-text mb-4">
            Navigation du projet
          </h3>
          
          <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <router-link
              :to="`/projects/${$route.params.id}/users`"
              class="gd-btn-secondary flex flex-col items-center p-4 text-center hover:bg-blue-50 hover:text-blue-600 hover:border-blue-200 transition-colors"
            >
              <svg class="h-8 w-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <span class="text-sm font-medium">Gérer les Utilisateurs</span>
            </router-link>
            
            <router-link
              :to="`/projects/${$route.params.id}/parts`"
              class="gd-btn-secondary flex flex-col items-center p-4 text-center hover:bg-blue-50 hover:text-blue-600 hover:border-blue-200 transition-colors"
            >
              <svg class="h-8 w-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              <span class="text-sm font-medium">Gérer les Parties</span>
            </router-link>

            <router-link
              :to="`/projects/${$route.params.id}/branches`"
              class="gd-btn-secondary flex flex-col items-center p-4 text-center hover:bg-blue-50 hover:text-blue-600 hover:border-blue-200 transition-colors"
            >
              <svg class="h-8 w-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
              <span class="text-sm font-medium">Branches</span>
            </router-link>
            
            <router-link
              :to="`/projects/${$route.params.id}/tags`"
              class="gd-btn-secondary flex flex-col items-center p-4 text-center opacity-50 cursor-not-allowed"
            >
              <svg class="h-8 w-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
              <span class="text-sm font-medium">Tags</span>
            </router-link>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="gd-card">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg font-medium text-text mb-4">Informations du projet</h3>
            
            <dl class="space-y-4">
              <div>
                <dt class="text-sm font-medium text-muted">URL du dépôt</dt>
                <dd class="mt-1">
                  <a
                    :href="projectStore.currentProject?.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="inline-flex items-center text-sm text-accent hover:text-accentHover hover:underline"
                  >
                    {{ projectStore.currentProject?.url }}
                    <svg class="ml-1 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
                    </svg>
                  </a>
                </dd>
              </div>
              
              <div>
                <dt class="text-sm font-medium text-muted">Plateforme</dt>
                <dd class="mt-1">
                  <span v-if="projectStore.currentProject?.platform === 'github'" class="gd-badge">GitHub</span>
                  <span v-else-if="projectStore.currentProject?.platform === 'gitlab'" class="gd-badge-warning">GitLab</span>
                  <span v-else class="gd-badge">{{ projectStore.currentProject?.platform }}</span>
                </dd>
              </div>
              
              <div>
                <dt class="text-sm font-medium text-muted">Date de création</dt>
                <dd class="mt-1 text-sm text-text">
                  {{ formatDate(projectStore.currentProject?.createdAt) }}
                </dd>
              </div>
              
              <div v-if="projectStore.currentProject?.manager">
                <dt class="text-sm font-medium text-muted">Gestionnaire</dt>
                <dd class="mt-1 text-sm text-text">
                  {{ projectStore.currentProject.manager.firstName }} {{ projectStore.currentProject.manager.lastName }}
                </dd>
              </div>
            </dl>
          </div>
        </div>

        <div class="gd-card">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg font-medium text-text mb-4">Activité récente</h3>
            
            <div v-if="projectStore.isLoading" class="flex justify-center py-8">
              <span class="animate-spin text-2xl">⟳</span>
            </div>
            
            <div v-else-if="recentActivity.length === 0" class="text-center py-8">
              <p class="mt-2 text-sm text-muted">Aucune activité récente</p>
            </div>
            
            <div v-else class="space-y-3">
              <div v-for="activity in recentActivity" :key="activity.id" class="flex items-start space-x-3">
                <div class="flex-shrink-0">
                  <div class="h-8 w-8 rounded-full bg-accent/10 flex items-center justify-center">
                    <svg class="h-4 w-4 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                </div>
                <div class="min-w-0 flex-1">
                  <p class="text-sm text-text">
                    <span class="font-medium">{{ activity.author }}</span> {{ activity.action }}
                  </p>
                  <p class="text-xs text-muted">{{ activity.time }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import AppLayout from '@/layouts/AppLayout.vue'
import { useNotificationStore } from '@/stores/notificationStore'

const route = useRoute()
const projectStore = useProjectStore()
const notifStore = useNotificationStore()

const syncFailed = ref(false)
let pollInterval = null   // fallback polling handle

const stats = ref({
  totalTags: 0,
  totalCollaborators: 0,
  totalCommits: 0
})

const recentActivity = ref([
  { id: 1, author: 'Système', action: 'Projet initialisé', time: 'Récemment' }
])

// ─── helpers ────────────────────────────────────────────────────────────────

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('fr-FR', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
}

const currentProjectId = () => parseInt(route.params.id)

const loadProjectData = async () => {
  const id = currentProjectId()
  if (id) await projectStore.fetchProjectBranches(id)
}

// ─── Called when SYNC_COMPLETE is confirmed ──────────────────────────────────

const onSyncComplete = async () => {
  projectStore.isSyncing = false
  syncFailed.value = false
  stopPolling()
  await nextTick()
  await loadProjectData()
  notifStore.success?.('Projet synchronisé avec succès !')
}

const onSyncFailed = () => {
  projectStore.isSyncing = false
  syncFailed.value = true
  stopPolling()
}

// ─── Polling fallback (runs while isSyncing, in case WebSocket is down) ─────

const startPolling = () => {
  if (pollInterval) return                 // already running
  pollInterval = setInterval(async () => {
    if (!projectStore.isSyncing) { stopPolling(); return }

    try {
    // CORRECTION 1 : La méthode dans ton store s'appelle fetchInAppNotifications !
      if (typeof notifStore.fetchInAppNotifications === 'function') {
      await notifStore.fetchInAppNotifications()
      }
      
    // CORRECTION 2 : Il faut interroger le backend pour voir si les branches sont là
    await loadProjectData() 
      
    } catch (_) { /* silent */ }

    // If branches are now present in the store, the sync already completed
    // but the WebSocket push was missed — refresh data and stop.
    if (projectStore.branches.length > 0) {
      await onSyncComplete()
    }
  }, 5000)   // check every 5 s
}

const stopPolling = () => {
  if (pollInterval) { clearInterval(pollInterval); pollInterval = null }
}

// ─── Core actions ────────────────────────────────────────────────────────────

const handleSync = async () => {
  const id = currentProjectId()
  if (!id) return
  syncFailed.value = false
  await projectStore.triggerManualSync(id)
  startPolling()      // start safety net the moment we trigger a sync
}

const handlePremiumSync = () => {
  notifStore.warning?.("L'aspiration de l'historique complet nécessite un plan PRO ou ENTERPRISE.", 6000)
}

// ─── Watch: notification array length (fires on every push/unshift) ──────────
//
// Bug fix #1 — was `{ deep: true }` on the whole array (Vue coalesces rapid
//   mutations, so fast pushes could be missed).
// Bug fix #2 — was reading `payloadObj?.projectName` which is always undefined
//   because the WebSocket pushes a Notification *entity* (title, message, type,
//   entityId) — NOT a NotificationEventDTO with a payload map.
//   Matching must use `entityId` (= projectId) and/or the type string.
// ─────────────────────────────────────────────────────────────────────────────

watch(
  () => notifStore.inAppNotifications?.length,
  () => {
    const notifs = notifStore.inAppNotifications
    if (!notifs?.length || !projectStore.currentProject) return

    const projectId = currentProjectId()

    // Scan the most recent few notifications (not just [0]) in case the store
    // prepends AND the watcher fires slightly late after two rapid pushes.
    const recent = notifs.slice(0, 5)

    for (const notif of recent) {
      const type = notif.type

      // ── Match by entityId (set by backend for all sync events) ──
      const matchById = notif.entityId != null &&
        Number(notif.entityId) === projectId

      // ── Match by message/title content as secondary fallback ──
      const projectName = projectStore.currentProject?.name ?? ''
      const matchByContent = projectName &&
        (notif.message?.includes(projectName) || notif.title?.includes(projectName))

      if (!matchById && !matchByContent) continue

      if (type === 'TYPE_SYNC_COMPLETE') {
        onSyncComplete()
        break
      }
      if (type === 'TYPE_SYNC_FAILED' || type === 'TYPE_SYNC_DELAYED') {
        onSyncFailed()
        break
      }
      // TYPE_SYNC_STARTED: keep spinner going, no action needed
    }
  }
)

// ─── Lifecycle ───────────────────────────────────────────────────────────────

onMounted(async () => {
  await loadProjectData()

  // If the project data is still empty after initial load, the sync triggered
  // by the Saga is probably still running — start the polling safety net.
  if (projectStore.branches.length === 0 && !syncFailed.value) {
    projectStore.isSyncing = true
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
  if (projectStore.isSyncing) projectStore.isSyncing = false
})
</script>