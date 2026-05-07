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
        <div>
           <router-link to="/projects" class="gd-btn-secondary">
             Retour à la liste
           </router-link>
        </div>
      </div>

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

      <div v-else-if="projectStore.branches.length === 0 && stats.totalCommits === 0" 
           class="bg-blue-50 border border-blue-200 rounded-lg p-5 flex items-start animate-pulse mb-6">
        </div>

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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import AppLayout from '@/layouts/AppLayout.vue'

import { computed, watch } from 'vue' // Ajoute watch
import { useNotificationStore } from '@/stores/notificationStore'

const route = useRoute()
const projectStore = useProjectStore()

const notifStore = useNotificationStore()
const syncFailed = ref(false)

const stats = ref({
  totalTags: 0,
  totalCollaborators: 0,
  totalCommits: 0
})

const recentActivity = ref([
  { id: 1, author: 'Système', action: 'Projet initialisé', time: 'Récemment' }
])

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const loadProjectData = async () => {
  const projectId = parseInt(route.params.id) 
  if (projectId) {
    await projectStore.fetchProjectBranches(projectId)
  }
}

// On écoute les notifications en direct ! Si on reçoit un échec pour ce projet, on stoppe la roue.
watch(() => notifStore.inAppNotifications, (notifs) => {
  if (!projectStore.currentProject) return;
  const failedNotif = notifs.find(n => {
    if (n.type !== 'TYPE_SYNC_FAILED') return false;

    // Sécurisation de la lecture du payload (selon comment ton backend le renvoie)
    let payloadObj = n.payload;
    if (typeof n.payload === 'string') {
      try { payloadObj = JSON.parse(n.payload) } catch (e) { return false }
    }
    
    return payloadObj?.projectName === projectStore.currentProject.name;
  })
  if (failedNotif) {
    syncFailed.value = true
  }
}, { deep: true })

onMounted(() => {
  loadProjectData()
})
</script>