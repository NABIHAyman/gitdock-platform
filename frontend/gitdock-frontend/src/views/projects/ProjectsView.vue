<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- HEADER -->
      <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-6 shadow-xl">
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-0 right-0 w-64 h-64 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
        </div>
        <div class="relative flex items-center justify-between">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <div class="w-8 h-8 bg-white/20 rounded-xl flex items-center justify-center">
                <v-icon icon="mdi-folder-multiple" color="white" size="18"></v-icon>
              </div>
              <span class="text-white/70 text-xs font-bold uppercase tracking-widest">GitDock</span>
            </div>
            <h1 class="text-2xl font-black text-white">
              {{ currentView === 'projects' ? 'Project List' : 'Collaborators' }}
            </h1>
            <p class="text-white/60 text-xs mt-0.5">
              {{ currentView === 'projects' ? 'Manage all your Git repositories' : 'Manage your project collaborators' }}
            </p>
          </div>

          <div class="flex items-center gap-3">
            <!-- TOGGLE PROJECTS / COLLAB — manager only -->
            <div v-if="isManager" class="flex bg-white/10 p-1 rounded-xl border border-white/20">
              <button @click="currentView = 'projects'"
                      :class="['px-3 py-1.5 rounded-lg text-xs font-black transition-all', currentView === 'projects' ? 'bg-white text-[#5b13ec]' : 'text-white/70 hover:text-white']">
                Projects
              </button>
              <button @click="currentView = 'collaborators'"
                      :class="['px-3 py-1.5 rounded-lg text-xs font-black transition-all', currentView === 'collaborators' ? 'bg-white text-[#5b13ec]' : 'text-white/70 hover:text-white']">
                Collaborators
              </button>
            </div>

            <!-- TOGGLE LIST / CARDS -->
            <div v-if="currentView === 'projects'" class="flex bg-white/10 p-1 rounded-xl border border-white/20">
              <button @click="viewMode = 'list'"
                      :class="['p-1.5 rounded-lg transition-all', viewMode === 'list' ? 'bg-white text-[#5b13ec]' : 'text-white/70 hover:text-white']">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"/>
                </svg>
              </button>
              <button @click="viewMode = 'cards'"
                      :class="['p-1.5 rounded-lg transition-all', viewMode === 'cards' ? 'bg-white text-[#5b13ec]' : 'text-white/70 hover:text-white']">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- PROJECTS -->
      <div v-if="currentView === 'projects'">
        <div class="mb-4 flex justify-between items-center">
          <!-- Add button — manager only -->
          <button v-if="isManager" @click="showCreateModal = true"
                  class="flex items-center gap-2 px-5 py-2.5 bg-[#5b13ec] text-white rounded-xl font-black text-sm hover:bg-[#4a0fd4] transition-all shadow-lg shadow-[#5b13ec]/20">
            <v-icon icon="mdi-plus" size="18"></v-icon>
            Add Project
          </button>
          <div v-else></div>

          <button @click="sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'"
                  class="flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 rounded-xl text-sm font-bold text-slate-600 hover:bg-slate-50 transition-all">
            <v-icon :icon="sortOrder === 'asc' ? 'mdi-sort-ascending' : 'mdi-sort-descending'" size="16"></v-icon>
            {{ sortOrder === 'asc' ? 'Ascending' : 'Descending' }}
          </button>
        </div>

        <!-- TABLE VIEW -->
        <div v-if="viewMode === 'list' && sortedProjects.length > 0" class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <table class="w-full">
            <thead>
            <tr class="bg-slate-50 border-b border-slate-200">
              <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase tracking-wider">Project</th>
              <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase tracking-wider">URL</th>
              <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase tracking-wider">Platform</th>
              <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase tracking-wider">Managed By</th>
              <th class="px-6 py-3 text-right text-xs font-black text-slate-400 uppercase tracking-wider">Actions</th>
            </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
            <tr v-for="project in sortedProjects" :key="project.id"
                @click="navigateToProject(project.id)"
                class="hover:bg-slate-50 cursor-pointer transition-colors">
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 bg-[#5b13ec]/10 rounded-xl flex items-center justify-center flex-shrink-0">
                    <v-icon icon="mdi-folder" color="#5b13ec" size="18"></v-icon>
                  </div>
                  <div>
                    <p class="text-sm font-black text-slate-800">{{ project.name }}</p>
                    <p class="text-xs text-slate-400">{{ new Date(project.createdAt).toLocaleDateString('en-US') }}</p>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <a :href="project.url" target="_blank" @click.stop
                   class="inline-flex items-center text-sm text-[#5b13ec] hover:underline font-bold">
                  {{ getProjectHostname(project.url) }}
                  <v-icon icon="mdi-open-in-new" size="14" class="ml-1"></v-icon>
                </a>
              </td>
              <td class="px-6 py-4">
                  <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-black"
                        :class="project.platform === 'github' ? 'bg-slate-100 text-slate-700' : project.platform === 'gitlab' ? 'bg-orange-50 text-orange-700' : 'bg-blue-50 text-blue-700'">
                    {{ (project.platform || '').toUpperCase() }}
                  </span>
              </td>
              <td class="px-6 py-4 text-sm text-slate-500 font-bold">
                <span v-if="project.manager">{{ project.manager.firstName }} {{ project.manager.lastName }}</span>
                <span v-else class="text-slate-300">—</span>
              </td>
              <td class="px-6 py-4">
                <div class="flex justify-end gap-2">
                  <button @click.stop="navigateToProject(project.id)"
                          class="p-2 text-[#5b13ec] hover:bg-[#5b13ec]/10 rounded-xl transition-all" title="View">
                    <v-icon icon="mdi-eye-outline" size="18"></v-icon>
                  </button>
                  <button v-if="isManager" @click.stop="handleDeleteProject(project.id)"
                          class="p-2 text-red-500 hover:bg-red-50 rounded-xl transition-all" title="Delete">
                    <v-icon icon="mdi-trash-can-outline" size="18"></v-icon>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- CARDS VIEW -->
        <div v-if="viewMode === 'cards' && sortedProjects.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="project in sortedProjects" :key="project.id"
               @click="navigateToProject(project.id)"
               class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 cursor-pointer hover:shadow-md hover:border-[#5b13ec]/30 transition-all hover:-translate-y-0.5">
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-[#5b13ec]/10 rounded-xl flex items-center justify-center">
                  <v-icon icon="mdi-folder" color="#5b13ec" size="20"></v-icon>
                </div>
                <div>
                  <p class="font-black text-slate-800">{{ project.name }}</p>
                  <p class="text-xs text-slate-400">{{ new Date(project.createdAt).toLocaleDateString('en-US') }}</p>
                </div>
              </div>
              <div class="flex gap-1">
                <button @click.stop="navigateToProject(project.id)" class="p-1.5 text-[#5b13ec] hover:bg-[#5b13ec]/10 rounded-lg transition-all">
                  <v-icon icon="mdi-eye-outline" size="16"></v-icon>
                </button>
                <button v-if="isManager" @click.stop="handleDeleteProject(project.id)" class="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-all">
                  <v-icon icon="mdi-trash-can-outline" size="16"></v-icon>
                </button>
              </div>
            </div>
            <a :href="project.url" target="_blank" @click.stop class="inline-flex items-center text-xs text-[#5b13ec] hover:underline font-bold mb-3">
              {{ getProjectHostname(project.url) }}
              <v-icon icon="mdi-open-in-new" size="12" class="ml-1"></v-icon>
            </a>
            <div class="flex items-center justify-between mt-2">
              <span class="text-[10px] font-black px-2 py-1 rounded-full"
                    :class="project.platform === 'github' ? 'bg-slate-100 text-slate-600' : 'bg-orange-50 text-orange-600'">
                {{ (project.platform || '').toUpperCase() }}
              </span>
              <span class="text-xs text-slate-400 font-bold">
                {{ project.manager ? project.manager.firstName + ' ' + project.manager.lastName : '—' }}
              </span>
            </div>
          </div>
        </div>

        <!-- EMPTY STATE -->
        <div v-if="sortedProjects.length === 0" class="bg-white rounded-2xl border border-slate-200 p-16 text-center">
          <div class="w-16 h-16 bg-slate-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <v-icon icon="mdi-folder-open-outline" color="#94a3b8" size="32"></v-icon>
          </div>
          <h3 class="font-black text-slate-700 mb-1">No Projects</h3>
          <p class="text-sm text-slate-400 mb-6">
            {{ isManager ? 'Start by creating your first Git project.' : 'No projects are assigned to you at the moment.' }}
          </p>
          <button v-if="isManager" @click="showCreateModal = true"
                  class="px-5 py-2.5 bg-[#5b13ec] text-white rounded-xl font-black text-sm hover:bg-[#4a0fd4] transition-all">
            Create Project
          </button>
        </div>
      </div>

      <!-- COLLABORATORS — manager only -->
      <div v-if="currentView === 'collaborators' && isManager">
        <div class="mb-4">
          <button @click="showAddForm = !showAddForm"
                  class="flex items-center gap-2 px-5 py-2.5 bg-[#5b13ec] text-white rounded-xl font-black text-sm hover:bg-[#4a0fd4] transition-all shadow-lg shadow-[#5b13ec]/20">
            <v-icon :icon="showAddForm ? 'mdi-close' : 'mdi-plus'" size="18"></v-icon>
            {{ showAddForm ? 'Cancel' : 'Add Collaborator' }}
          </button>
        </div>

        <div v-if="showAddForm || editingCollaborator" class="mb-6">
          <ProjectForm :projects="projectStore.projects" :available-roles="projectStore.availableRoles"
                       :collaborator="editingCollaborator" :is-loading="projectStore.isLoading"
                       @submit="handleFormSubmit" @cancel="handleFormCancel" />
        </div>

        <div v-if="projectStore.collaboratorsGrouped.length > 0" class="space-y-4">
          <div v-for="group in projectStore.collaboratorsGrouped" :key="group.projectId || 'no-project'"
               class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div class="px-6 py-3 bg-slate-50 border-b border-slate-200">
              <h3 class="font-black text-slate-700 text-sm">{{ group.projectName || 'Unassigned' }}</h3>
            </div>
            <table class="w-full">
              <thead>
              <tr class="border-b border-slate-100">
                <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase">Name</th>
                <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase">Email</th>
                <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase">Role</th>
                <th class="px-6 py-3 text-left text-xs font-black text-slate-400 uppercase">Status</th>
                <th class="px-6 py-3 text-right text-xs font-black text-slate-400 uppercase">Actions</th>
              </tr>
              </thead>
              <tbody class="divide-y divide-slate-50">
              <tr v-for="collaborator in group.collaborators" :key="collaborator.id" class="hover:bg-slate-50 transition-colors">
                <td class="px-6 py-3 text-sm font-black text-slate-700">{{ collaborator.firstName }} {{ collaborator.lastName }}</td>
                <td class="px-6 py-3 text-sm text-slate-500">{{ collaborator.email }}</td>
                <td class="px-6 py-3">
                  <span class="text-[10px] font-black px-2 py-1 bg-[#5b13ec]/10 text-[#5b13ec] rounded-full">{{ getRoleLabel(collaborator.role) }}</span>
                </td>
                <td class="px-6 py-3">
                    <span class="text-[10px] font-black px-2 py-1 rounded-full"
                          :class="collaborator.status === 'active' ? 'bg-emerald-50 text-emerald-700' : collaborator.status === 'pending' ? 'bg-amber-50 text-amber-700' : 'bg-red-50 text-red-700'">
                      {{ collaborator.status === 'active' ? 'Active' : collaborator.status === 'pending' ? 'Pending' : 'Inactive' }}
                    </span>
                </td>
                <td class="px-6 py-3">
                  <span v-if="collaborator.id === authStore.userId" class="text-xs text-slate-400 italic float-right">(You)</span>
                  <div v-else class="flex justify-end gap-1">
                    <button v-if="canPerform('edit')" @click="handleEditCollaborator(collaborator)"
                            class="p-1.5 text-[#5b13ec] hover:bg-[#5b13ec]/10 rounded-lg transition-all" title="Edit">
                      <v-icon icon="mdi-pencil-outline" size="16"></v-icon>
                    </button>
                    <button v-if="canUnlink(group.projectId)" @click="handleRemoveCollaborator(group.projectId, collaborator.id)"
                            class="p-1.5 text-amber-600 hover:bg-amber-50 rounded-lg transition-all" title="Unlink">
                      <v-icon icon="mdi-account-minus-outline" size="16"></v-icon>
                    </button>
                    <button v-if="collaborator.status === 'active' && canPerform('soft-delete')" @click="handleSoftDelete(collaborator.id)"
                            class="p-1.5 text-amber-600 hover:bg-amber-50 rounded-lg transition-all" title="Disable">
                      <v-icon icon="mdi-cancel" size="16"></v-icon>
                    </button>
                    <button v-if="collaborator.status !== 'active' && canPerform('restore')" @click="handleRestore(collaborator.id)"
                            class="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-lg transition-all" title="Restore">
                      <v-icon icon="mdi-restore" size="16"></v-icon>
                    </button>
                    <button v-if="canPerform('reset-password')" @click="handleResetPassword(collaborator.email)"
                            class="p-1.5 text-violet-600 hover:bg-violet-50 rounded-lg transition-all" title="Reset Password">
                      <v-icon icon="mdi-key-outline" size="16"></v-icon>
                    </button>
                    <button v-if="(collaborator.status !== 'active' || authStore.role === 'ROLE_SUPER_ADMIN') && canPerform('hard-delete')"
                            @click="handleHardDelete(collaborator.id)"
                            class="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-all" title="Delete Permanent">
                      <v-icon icon="mdi-trash-can-outline" size="16"></v-icon>
                    </button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else class="bg-white rounded-2xl border border-slate-200 p-12 text-center">
          <v-icon icon="mdi-account-group-outline" color="#cbd5e1" size="40" class="mb-3"></v-icon>
          <p class="font-black text-slate-500">No collaborators found</p>
        </div>
      </div>

    </div>
  </AppLayout>

  <CreateProjectModal :is-open="showCreateModal" @close="showCreateModal = false" />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import { useProjectStore } from '@/stores/projectStore'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore'
import { useRole } from '@/composables/useRole'
import CreateProjectModal from '@/components/project/CreateProjectModal.vue'
import ProjectForm from '@/components/project/ProjectForm.vue'
import { authService } from '@/services/authService'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()
const notificationStore = useNotificationStore()
const { isManager } = useRole()

const currentView = ref('projects')
const viewMode = ref('list')
const sortOrder = ref('desc')
const showCreateModal = ref(false)
const showAddForm = ref(false)
const editingCollaborator = ref(undefined)

const sortedProjects = computed(() => {
  const projects = [...(projectStore.projects || [])]
  return projects.sort((a, b) => {
    const dateA = new Date(a.createdAt).getTime()
    const dateB = new Date(b.createdAt).getTime()
    return sortOrder.value === 'asc' ? dateA - dateB : dateB - dateA
  })
})

const canPerform = (action) => {
  const myRole = authStore.role
  if (myRole === 'ROLE_SUPER_ADMIN') return true
  if (myRole === 'ROLE_COMPANY_ADMIN') return action !== 'hard-delete'
  return false
}

const canUnlink = (projectId) => {
  if (!projectId) return false
  if (authStore.role === 'ROLE_SUPER_ADMIN') return true
  const project = projectStore.projects.find(p => p.id === projectId)
  if (!project) return false
  return project.manager && project.manager.id === authStore.userId
}

onMounted(async () => {
  try {
    if (route.query.openModal === 'true') {
      showCreateModal.value = true
      router.replace('/projects')
    }
    await projectStore.fetchProjects()
    if (isManager.value) {
      await projectStore.fetchCollaboratorsGrouped()
    }
  } catch (e) { console.error(e) }
})

const navigateToProject = (id) => router.push(`/projects/${id}`)

const handleDeleteProject = async (id) => {
  const project = projectStore.projects.find(p => p.id === id)
  if (!project) return
  if (confirm(`Delete "${project.name}"?`)) {
    try {
      if (projectStore.deleteProject) await projectStore.deleteProject(id)
      else notificationStore.info("Feature in development")
    } catch (error) { console.error(error) }
  }
}

const handleFormSubmit = async (data) => {
  try {
    if (editingCollaborator.value) {
      await projectStore.updateUser(editingCollaborator.value.id, { firstName: data.firstName, lastName: data.lastName, role: data.role })
      showAddForm.value = false
      editingCollaborator.value = undefined
    } else if (data.projectId) {
      await projectStore.addCollaborator(data.projectId, { email: data.email, role: data.role, firstName: data.firstName, lastName: data.lastName })
      await projectStore.fetchCollaboratorsGrouped()
      showAddForm.value = false
      editingCollaborator.value = undefined
    } else {
      notificationStore.error("Please select a project")
    }
  } catch (error) { console.error(error) }
}

const handleFormCancel = () => { showAddForm.value = false; editingCollaborator.value = undefined }
const handleEditCollaborator = (c) => { editingCollaborator.value = c; showAddForm.value = false }
const handleSoftDelete = async (id) => { if (confirm('Disable this collaborator?')) { try { if (projectStore.softDeleteUser) await projectStore.softDeleteUser(id) } catch (e) { console.error(e) } } }
const handleRestore = async (id) => { try { if (projectStore.restoreUser) await projectStore.restoreUser(id) } catch (e) { console.error(e) } }
const handleHardDelete = async (id) => { if (confirm('Delete permanently?')) { try { if (projectStore.hardDeleteUser) await projectStore.hardDeleteUser(id) } catch (e) { console.error(e) } } }
const handleResetPassword = async (email) => { if (confirm(`Send password reset email to ${email}?`)) { try { await authService.requestPasswordReset(email); notificationStore.success('Email sent!') } catch (e) { console.error(e) } } }
const handleRemoveCollaborator = async (projectId, userId) => { if (confirm('Remove this collaborator from project?')) { await projectStore.removeCollaborator(projectId, userId) } }
const getRoleLabel = (role) => ({ SUPER_ADMIN: 'Super Admin', ADMIN: 'Admin', MANAGER: 'Manager', DEVELOPER: 'Developer' }[role] || role)
const getProjectHostname = (urlString) => { if (!urlString) return 'N/A'; try { return new URL(urlString).hostname.replace('www.', '') } catch (e) { return urlString } }
</script>