<template>
  <AppLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight text-text">
            Utilisateurs du projet
          </h1>
          <div class="mt-1 flex items-center space-x-2">
            <router-link :to="`/projects/${$route.params.id}`" class="text-sm text-accent hover:underline">
              ← Retour au projet
            </router-link>
          </div>
        </div>
        
        <button v-if="!showAddForm" @click="showAddForm = true" class="gd-btn-primary">
          Ajouter un utilisateur
        </button>
        <button v-else @click="handleFormCancel" class="gd-btn-secondary">
          Annuler
        </button>
      </div>

      <!-- Add/Edit Form (Remplaçant l'ancienne modale basique) -->
      <div v-if="showAddForm || editingCollaborator" class="mb-6">
        <ProjectForm
            :projects="projectStore.projects"
            :available-roles="projectStore.availableRoles"
            :collaborator="editingCollaborator"
            :is-loading="projectStore.isLoading"
            @submit="handleFormSubmit"
            @cancel="handleFormCancel"
        />
      </div>

      <!-- Users List -->
      <div class="gd-card overflow-hidden">
        <div v-if="isLoading" class="p-8 text-center">
          <span class="animate-spin text-2xl inline-block mb-2">⟳</span>
          <p class="text-muted">Chargement des utilisateurs...</p>
        </div>

        <div v-else-if="projectUsers.length === 0" class="p-12 text-center">
          <h3 class="text-lg font-semibold text-text mb-2">Aucun utilisateur</h3>
          <p class="text-muted mb-6">Ce projet n'a pas encore d'utilisateurs assignés.</p>
          <button @click="showAddForm = true" class="gd-btn-primary">
            Ajouter le premier utilisateur
          </button>
        </div>

        <div v-else>
          <table class="gd-table">
            <thead class="gd-thead">
              <tr>
                <th class="gd-th">Nom complet</th>
                <th class="gd-th">Email</th>
                <th class="gd-th">Rôle</th>
                <th class="gd-th">État</th>
                <th class="gd-th text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-surface divide-y divide-border">
              <tr v-for="user in projectUsers" :key="user.id" class="gd-tr">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-text">
                    {{ user.firstName }} {{ user.lastName }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-muted">
                  {{ user.email }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="gd-badge-accent">
                    {{ getRoleLabel(user.role) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span
                        :class="{
                          'gd-badge-success': user.status === 'active',
                          'gd-badge-warning': user.status === 'pending',
                          'gd-badge-danger': user.status === 'disabled'
                        }"
                    >
                      {{ 
                         user.status === 'active' ? 'Actif' : 
                        (user.status === 'pending' ? 'En attente' : 'Désactivé') 
                      }}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <span v-if="user.id === authStore.userId" class="text-muted/60 italic text-xs mr-4">
                    (Vous)
                  </span>
                  <div v-else class="flex items-center justify-end gap-2">
                    <button v-if="canPerform('edit')"
                            @click="handleEditCollaborator(user)"
                            class="gd-btn-ghost px-2 py-2 text-accent hover:text-accentHover"
                            title="Modifier les informations"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>

                    <button
                        v-if="canUnlink()"
                        @click="handleRemoveCollaborator(user.id)"
                        class="gd-btn-ghost px-2 py-2 text-amber-700 hover:text-amber-800 hover:bg-amber-50"
                        title="Retirer du projet"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M13 7a4 4 0 11-8 0 4 4 0 018 0zM9 14a6 6 0 00-6 6v1h12v-1a6 6 0 00-6-6zM21 12h-6"/>
                      </svg>
                    </button>

                    <button v-if="user.status === 'active' && canPerform('soft-delete')"
                            @click="handleSoftDelete(user.id)"
                            class="gd-btn-ghost px-2 py-2 text-amber-700 hover:text-amber-800 hover:bg-amber-50"
                            title="Désactiver l'accès"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                      </svg>
                    </button>

                    <button
                        v-if="user.status !== 'active' && canPerform('restore')"
                        @click="handleRestore(user.id)"
                        class="gd-btn-ghost px-2 py-2 text-emerald-700 hover:text-emerald-800 hover:bg-emerald-50"
                        title="Restaurer l'accès"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                      </svg>
                    </button>

                    <button v-if="canPerform('reset-password')"
                            @click="handleResetPassword(user.email)"
                            class="gd-btn-ghost px-2 py-2 text-violet-700 hover:text-violet-800 hover:bg-violet-50"
                            title="Envoyer email réinitialisation"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                      </svg>
                    </button>

                    <button
                        v-if="(user.status !== 'active' || authStore.role === 'ROLE_SUPER_ADMIN') && canPerform('hard-delete')"
                        @click="handleHardDelete(user.id)"
                        class="gd-btn-ghost px-2 py-2 text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                        title="Supprimer définitivement"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                      </svg>
                    </button>
                  </div>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/projectStore'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore'
import { authService } from '@/services/authService'
import AppLayout from '@/layouts/AppLayout.vue'
import ProjectForm from '@/components/project/ProjectForm.vue'

const route = useRoute()
const projectStore = useProjectStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const showAddForm = ref(false)
const editingCollaborator = ref(undefined)

// Permissions
const canPerform = (action) => {
  const myRole = authStore.role
  if (myRole === 'ROLE_SUPER_ADMIN') return true
  if (myRole === 'ROLE_COMPANY_ADMIN') {
    return action !== 'hard-delete'
  }
  if (myRole === 'ROLE_WORKSPACE_OWNER') {
    return false
  }
  return false
}

// Vérifier si l'utilisateur peut retirer quelqu'un du projet
const canUnlink = () => {
  if (authStore.role === 'ROLE_SUPER_ADMIN') return true
  const project = projectStore.currentProject
  if (!project) return false
  if (project.manager && project.manager.id === authStore.userId) {
    return true
  }
  return false
}

const projectUsers = computed(() => projectStore.projectCollaborators || [])
const isLoading = computed(() => projectStore.isLoading)

const getRoleLabel = (role) => {
  const roleMap = {
    'DEVELOPER': 'Développeur',
    'MANAGER': 'Manager',
    'ADMIN': 'Administrateur',
    'SUPER_ADMIN': 'Super Admin'
  }
  return roleMap[role] || role
}

const loadProjectUsers = async () => {
  const projectId = parseInt(route.params.id)
  if (!projectId) return

  await Promise.all([
    projectStore.fetchProjectBranches(projectId), // Pour currentProject
    projectStore.fetchProjectCollaborators(projectId)
  ])
}

const handleFormSubmit = async (data) => {
  const projectId = parseInt(route.params.id)
  try {
    if (editingCollaborator.value) {
      await projectStore.updateUser(editingCollaborator.value.id, {
        firstName: data.firstName,
        lastName: data.lastName,
        role: data.role
      });
    } else {
      // Forcé sur le projectId de la page active
      await projectStore.addCollaborator(projectId, {
        email: data.email,
        role: data.role,
        firstName: data.firstName,
        lastName: data.lastName
      });
    }
    showAddForm.value = false;
    editingCollaborator.value = undefined;
    await loadProjectUsers(); // On rafraîchit
  } catch (error) {
    console.error("Erreur du formulaire", error);
  }
}

const handleFormCancel = () => {
  showAddForm.value = false
  editingCollaborator.value = undefined
}

const handleEditCollaborator = (collaborator) => {
  // On lui ajoute manuellement projectId pour que le ProjectForm se pré-remplisse
  const projectId = parseInt(route.params.id)
  editingCollaborator.value = { ...collaborator, projectId: projectId }
  showAddForm.value = true // Affiche le formulaire
}

const handleSoftDelete = async (id) => {
  if (confirm('Êtes-vous sûr de vouloir désactiver ce collaborateur ?')) {
    await projectStore.softDeleteUser(id)
    await loadProjectUsers()
  }
}

const handleRestore = async (id) => {
  if (confirm('Voulez-vous redonner l\'accès à ce collaborateur ?')) {
    await projectStore.restoreUser(id)
    await loadProjectUsers()
  }
}

const handleHardDelete = async (id) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer définitivement ce collaborateur ? Cette action est irréversible.')) {
    await projectStore.hardDeleteUser(id)
    await loadProjectUsers()
  }
}

const handleResetPassword = async (email) => {
  if (confirm(`Envoyer un email de réinitialisation de mot de passe à ${email} ?`)) {
    try {
      await authService.requestPasswordReset(email)
      notificationStore.success('Email de réinitialisation envoyé avec succès')
    } catch (error) {
      console.error(error)
      notificationStore.error('Erreur lors de l\'envoi de l\'email')
    }
  }
}

const handleRemoveCollaborator = async (userId) => {
  const projectId = parseInt(route.params.id)
  if (confirm('Voulez-vous retirer ce collaborateur du projet ?')) {
    await projectStore.removeCollaborator(projectId, userId);
    await loadProjectUsers()
  }
}

onMounted(() => {
  // Le ProjectForm a besoin de la liste des rôles
  if (projectStore.availableRoles.length === 0) {
    // Si fetchAvailableRoles existe
    // projectStore.fetchAvailableRoles()
  }
  loadProjectUsers()
})
</script>