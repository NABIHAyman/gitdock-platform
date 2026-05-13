<template>
  <AppLayout>
    <div class="space-y-6">
      
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Dashboard Super Admin</h1>
          <p class="text-sm text-gray-600 mt-1">Tour de Contrôle GitDock</p>
        </div>
        <button @click="adminStore.loadDashboard" :disabled="adminStore.isLoading" class="gd-btn-primary flex items-center">
          <span v-if="adminStore.isLoading" class="animate-spin mr-2">⟳</span>
          Actualiser
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <p class="text-sm font-medium text-gray-600">Total Utilisateurs</p>
          <p class="text-3xl font-bold text-gray-900 mt-2">{{ adminStore.kpis.totalUsers }}</p>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <p class="text-sm font-medium text-gray-600">Espaces / Entreprises</p>
          <p class="text-3xl font-bold text-gray-900 mt-2">{{ adminStore.kpis.totalCompanies }}</p>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <p class="text-sm font-medium text-gray-600">Projets Hébergés</p>
          <p class="text-3xl font-bold text-gray-900 mt-2">{{ adminStore.kpis.totalProjects }}</p>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <p class="text-sm font-medium text-gray-600">Volume Commits</p>
          <p class="text-3xl font-bold text-gray-900 mt-2">{{ adminStore.kpis.totalCommits }}</p>
        </div>
      </div>

      <div class="bg-gradient-to-r from-blue-600 to-indigo-700 rounded-xl shadow-sm p-6 text-white flex justify-between items-center">
        <div>
          <h2 class="text-lg font-semibold mb-1">Revenu Mensuel Estimé (MRR)</h2>
          <p class="text-sm opacity-80">Basé sur la répartition des abonnements actifs.</p>
        </div>
        <div class="text-4xl font-bold text-right">
          {{ adminStore.totalRevenue }} € <span class="text-lg font-normal opacity-80">/ mois</span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
          <h2 class="text-lg font-semibold text-gray-900">Annuaire Global des Utilisateurs</h2>
        </div>
        <table class="w-full text-left border-collapse">
          <thead>
            <tr>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Utilisateur</th>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Email</th>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Entreprise</th>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Statut</th>
              <th class="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="user in adminStore.users" :key="user.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 text-sm font-medium text-gray-900">{{ user.name }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ user.email }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ user.company }}</td>
              <td class="px-6 py-4 text-sm">
                <span :class="user.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" 
                      class="px-2 py-1 rounded-full text-xs font-semibold">
                  {{ user.status === 'active' ? 'Actif' : 'Suspendu' }}
                </span>
              </td>
              <td class="px-6 py-4 text-right space-x-3 text-sm">
                <button @click="adminStore.toggleUserStatus(user)" class="text-amber-600 hover:underline font-medium">
                  {{ user.status === 'active' ? 'Suspendre' : 'Réactiver' }}
                </button>
                <button @click="adminStore.hardDeleteUser(user.id)" class="text-red-600 hover:underline font-medium">
                  Supprimer
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="adminStore.users.length === 0" class="p-8 text-center text-gray-500">
          Aucun utilisateur trouvé.
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { useAdminStore } from '@/stores/adminStore'

const adminStore = useAdminStore()

onMounted(() => {
  adminStore.loadDashboard()
})
</script>