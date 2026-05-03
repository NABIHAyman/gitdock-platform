<template>
  <div class="gd-page space-y-6">

    <!-- HEADER -->
    <div class="gd-header">
      <div>
        <h1 class="gd-title">
          {{ project.name }}
        </h1>
        <p class="gd-subtitle">
          Vue d'ensemble du projet
        </p>
      </div>

      <router-link to="/projects" class="gd-btn-secondary">
        ← Retour
      </router-link>
    </div>

    <!-- ERROR -->
    <div v-if="syncFailed" class="gd-card border-red-500/30 bg-red-500/10">
      <div class="p-5">
        <h3 class="text-red-400 font-semibold">Échec de synchronisation</h3>
        <p class="text-sm text-red-300 mt-1">
          Impossible d’accéder au dépôt Git.
        </p>
      </div>
    </div>

    <!-- STATS -->
    <div class="gd-stats-grid">

      <div class="gd-card">
        <div class="gd-card-body flex items-center gap-4">
          <div class="gd-stat-icon bg-blue-500/10 text-blue-400">📁</div>
          <div>
            <div class="gd-stat-title">Branches</div>
            <div class="gd-stat-value">{{ branches.length }}</div>
          </div>
        </div>
      </div>

      <div class="gd-card">
        <div class="gd-card-body flex items-center gap-4">
          <div class="gd-stat-icon bg-green-500/10 text-green-400">🏷</div>
          <div>
            <div class="gd-stat-title">Tags</div>
            <div class="gd-stat-value">{{ stats.totalTags }}</div>
          </div>
        </div>
      </div>

      <div class="gd-card">
        <div class="gd-card-body flex items-center gap-4">
          <div class="gd-stat-icon bg-purple-500/10 text-purple-400">👥</div>
          <div>
            <div class="gd-stat-title">Collaborateurs</div>
            <div class="gd-stat-value">{{ stats.totalCollaborators }}</div>
          </div>
        </div>
      </div>

      <div class="gd-card">
        <div class="gd-card-body flex items-center gap-4">
          <div class="gd-stat-icon bg-yellow-500/10 text-yellow-400">📝</div>
          <div>
            <div class="gd-stat-title">Commits</div>
            <div class="gd-stat-value">{{ stats.totalCommits }}</div>
          </div>
        </div>
      </div>

    </div>

    <!-- INFO + ACTIVITY -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

      <!-- INFO -->
      <div class="gd-card">
        <div class="gd-card-body">
          <h3 class="font-semibold mb-4">Informations</h3>

          <p class="text-sm text-slate-300">
            <span class="text-slate-500">URL :</span>
            {{ project.url }}
          </p>

          <p class="text-sm text-slate-300 mt-2">
            <span class="text-slate-500">Plateforme :</span>
            {{ project.platform }}
          </p>

          <p class="text-sm text-slate-300 mt-2">
            <span class="text-slate-500">Créé :</span>
            {{ formatDate(project.createdAt) }}
          </p>
        </div>
      </div>

      <!-- ACTIVITY -->
      <div class="gd-card">
        <div class="gd-card-body">
          <h3 class="font-semibold mb-4">Activité récente</h3>

          <div v-for="a in recentActivity" :key="a.id"
               class="text-sm text-slate-300 mb-2">
            <span class="font-medium text-white">{{ a.author }}</span>
            {{ a.action }}
          </div>

        </div>
      </div>

    </div>

  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const project = ref({
  name: 'GitDock Project',
  url: 'https://github.com/example',
  platform: 'github',
  createdAt: new Date()
})

const branches = ref([
  { id: 1, name: 'main' },
  { id: 2, name: 'dev' }
])

const stats = ref({
  totalTags: 3,
  totalCollaborators: 5,
  totalCommits: 120
})

const recentActivity = ref([
  { id: 1, author: 'Système', action: 'Projet initialisé' },
  { id: 2, author: 'Admin', action: 'Nouvelle branche créée' }
])

const syncFailed = ref(false)

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('fr-FR')
}
</script>
