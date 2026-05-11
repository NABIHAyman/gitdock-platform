<template>
  <AppLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight text-text">
            {{ currentView === 'projects' ? 'Liste des projets' : 'Liste des collaborateurs' }}
          </h1>
          <p class="mt-1 text-sm text-muted">
            {{ currentView === 'projects' ? 'Gérez tous vos projets Git' : 'Gérez les collaborateurs de vos projets' }}
          </p>
        </div>
        <div class="flex items-center space-x-4">
          <!-- Toggle View -->
          <div class="gd-segment">
            <button
                @click="currentView = 'projects'"
                :class="[
                'gd-segment-btn',
                currentView === 'projects'
                  ? 'gd-segment-btn-active'
                  : ''
              ]"
            >
              Projets
            </button>
            <button
                @click="currentView = 'collaborators'"
                :class="[
                'gd-segment-btn',
                currentView === 'collaborators'
                  ? 'gd-segment-btn-active'
                  : ''
              ]"
            >
              Collaborateurs
            </button>
          </div>

          <!-- View Mode Toggle (only for projects) -->
          <div v-if="currentView === 'projects'" class="gd-segment">
            <button
                @click="viewMode = 'list'"
                :class="[
                'gd-segment-btn',
                viewMode === 'list'
                  ? 'gd-segment-btn-active'
                  : ''
              ]"
              title="Vue liste"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
              </svg>
            </button>
            <button
                @click="viewMode = 'cards'"
                :class="[
                'gd-segment-btn',
                viewMode === 'cards'
                  ? 'gd-segment-btn-active'
                  : ''
              ]"
              title="Vue cartes"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Projects View -->
      <div v-if="currentView === 'projects'">
        <div class="mb-4 flex justify-between items-center">
          <button
              @click="showCreateModal = true"
              class="gd-btn-primary"
          >
            Ajouter un projet
          </button>
          <div class="flex items-center space-x-2">
            <label class="text-sm text-muted">Trier par date:</label>
            <button
                @click="sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'"
                class="gd-btn-secondary px-3 py-1"
            >
              {{ sortOrder === 'asc' ? '↑ Croissant' : '↓ Décroissant' }}
            </button>
          </div>
        </div>

        <!-- Projects Table -->
        <div v-if="viewMode === 'list' && sortedProjects.length > 0" class="gd-card overflow-hidden">
          <table class="gd-table">
            <thead class="gd-thead">
            <tr>
              <th class="gd-th">
                Nom du projet
              </th>
              <th class="gd-th">
                URL
              </th>
              <th class="gd-th">
                Plateforme
              </th>
              <th class="gd-th">
                Géré par
              </th>
              <th class="gd-th text-right">
                Actions
              </th>
            </tr>
            </thead>
            <tbody class="bg-surface divide-y divide-border">
            <tr
                v-for="project in sortedProjects"
                :key="project.id"
                class="gd-tr cursor-pointer"
                @click="navigateToProject(project.id)"
            >
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-10 w-10 flex items-center justify-center rounded-full bg-accent/10 mr-3">
                    <svg class="h-6 w-6 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
                    </svg>
                  </div>
                  <div>
                    <div class="text-sm font-medium text-text">{{ project.name }}</div>
                    <div class="flex items-center text-xs text-muted">
                      <svg class="flex-shrink-0 mr-1 h-4 w-4 text-muted/70" fill="none" stroke="currentColor"
                           viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                      </svg>
                      {{ new Date(project.createdAt).toLocaleDateString('fr-FR') }}
                    </div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <a
                      :href="project.url"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="inline-flex items-center text-sm text-accent hover:text-accentHover hover:underline"
                      @click.stop
                  >
                    {{ getProjectHostname(project.url) }}
                    <svg class="ml-1 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
                    </svg>
                  </a>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <span v-if="project.platform === 'github'"
                          class="gd-badge">
                      <svg class="h-4 w-4 mr-1" viewBox="0 0 24 24" fill="currentColor">
                        <path fill-rule="evenodd" clip-rule="evenodd"
                              d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.254-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.202 2.393.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z"></path>
                      </svg>
                      GitHub
                    </span>
                  <span v-else-if="project.platform === 'gitlab'"
                        class="gd-badge-warning">
                      <svg class="h-4 w-4 mr-1" viewBox="0 0 24 24" fill="currentColor">
                        <path
                            d="M22.65 14.39L12 22.13 1.35 14.39a.84.84 0 01-.3-.94l1.22-3.78 2.44-7.51A.42.42 0 014.82 2a.43.43 0 01.58 0 .42.42 0 01.11.18l2.44 7.49h8.1l2.44-7.51a.42.42 0 01.11-.18.43.43 0 01.58 0 .42.42 0 01.11.27l2.44 7.51L23 13.45a.84.84 0 01-.35.94z"></path>
                      </svg>
                      GitLab
                    </span>
                  <span v-else-if="project.platform === 'bitbucket'"
                        class="gd-badge-accent">
                      <svg class="h-4 w-4 mr-1" viewBox="0 0 24 24" fill="currentColor">
                        <path
                            d="M.778 1.213a.768.768 0 00-.768.892l3.263 19.81c.084.5.515.868 1.022.873H19.95a.77.77 0 00.77-.646l3.27-20.03a.768.768 0 00-.768-.891zM14.52 15.53H9.522L8.17 8.466h7.561z"></path>
                      </svg>
                      Bitbucket
                    </span>
                  <span v-else-if="project.platform === 'azure'"
                        class="gd-badge-accent">
                      <svg class="h-4 w-4 mr-1" viewBox="0 0 24 24" fill="currentColor">
                        <path
                            d="M22.45 5.11a1 1 0 00-.8-1.6H2.35a1 1 0 00-.8 1.6L10.5 13l.5.6.5-.6 8.95-8.89zM11.5 14.4l-2.5 2.5-2.5-2.5-7.6 7.6a1 1 0 00.8 1.6h18.6a1 1 0 00.8-1.6l-7.6-7.6z"></path>
                      </svg>
                      Azure
                    </span>
                  <span v-else class="gd-badge">
                      {{ project.platform }}
                    </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-muted">
                  <span v-if="project.manager">
                    {{ project.manager.firstName }} {{ project.manager.lastName }}
                  </span>
                <span v-else class="text-muted/60">-</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex justify-end gap-2">
                  <button
                      @click.stop="navigateToProject(project.id)"
                      class="gd-btn-ghost px-2 py-2 text-accent hover:text-accentHover"
                      title="Voir les détails"
                  >
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                  </button>
                  <button
                      @click.stop="handleDeleteProject(project.id)"
                      class="gd-btn-ghost px-2 py-2 text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                      title="Supprimer le projet"
                  >
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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

        <!-- Projects Cards -->
        <div v-if="viewMode === 'cards' && sortedProjects.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
              v-for="project in sortedProjects"
              :key="project.id"
              class="gd-card gd-card-hover cursor-pointer transition-all duration-200 hover:scale-[1.02]"
              @click="navigateToProject(project.id)"
          >
            <div class="p-6">
              <!-- Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-12 w-12 flex items-center justify-center rounded-full bg-accent/10 mr-3">
                    <svg class="h-7 w-7 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
                    </svg>
                  </div>
                  <div class="min-w-0 flex-1">
                    <h3 class="text-lg font-semibold text-text truncate">{{ project.projectName }}</h3>
                    <div class="flex items-center text-sm text-muted mt-1">
                      <svg class="flex-shrink-0 mr-1 h-4 w-4 text-muted/70" fill="none" stroke="currentColor"
                           viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                      </svg>
                      {{ new Date(project.createdAt).toLocaleDateString('fr-FR') }}
                    </div>
                  </div>
                </div>
                
                <!-- Actions -->
                <div class="flex items-center space-x-1">
                  <button
                      @click.stop="navigateToProject(project.id)"
                      class="gd-btn-ghost p-2 text-accent hover:text-accentHover"
                      title="Voir les détails"
                  >
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                  </button>
                  <button
                      @click.stop="handleDeleteProject(project.id)"
                      class="gd-btn-ghost p-2 text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                      title="Supprimer le projet"
                  >
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- URL -->
              <div class="mb-4">
                <a
                    :href="project.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="inline-flex items-center text-sm text-accent hover:text-accentHover hover:underline"
                    @click.stop
                >
                  <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
                  </svg>
                  {{ getProjectHostname(project.url) }}
                  <svg class="ml-1 w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
                  </svg>
                </a>
              </div>

              <!-- Platform & Manager -->
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <span v-if="project.platform === 'github'"
                        class="gd-badge">
                    <svg class="h-3 w-3 mr-1" viewBox="0 0 24 24" fill="currentColor">
                      <path fill-rule="evenodd" clip-rule="evenodd"
                            d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.254-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.202 2.393.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z"></path>
                    </svg>
                    GitHub
                  </span>
                  <span v-else-if="project.platform === 'gitlab'"
                        class="gd-badge-warning">
                    <svg class="h-3 w-3 mr-1" viewBox="0 0 24 24" fill="currentColor">
                      <path
                          d="M22.65 14.39L12 22.13 1.35 14.39a.84.84 0 01-.3-.94l1.22-3.78 2.44-7.51A.42.42 0 014.82 2a.43.43 0 01.58 0 .42.42 0 01.11.18l2.44 7.49h8.1l2.44-7.51a.42.42 0 01.11-.18.43.43 0 01.58 0 .42.42 0 01.11.27l2.44 7.51L23 13.45a.84.84 0 01-.35.94z"></path>
                    </svg>
                    GitLab
                  </span>
                  <span v-else-if="project.platform === 'bitbucket'"
                        class="gd-badge-accent">
                    <svg class="h-3 w-3 mr-1" viewBox="0 0 24 24" fill="currentColor">
                      <path
                          d="M.778 1.213a.768.768 0 00-.768.892l3.263 19.81c.084.5.515.868 1.022.873H19.95a.77.77 0 00.77-.646l3.27-20.03a.768.768 0 00-.768-.891zM14.52 15.53H9.522L8.17 8.466h7.561z"></path>
                    </svg>
                    Bitbucket
                  </span>
                  <span v-else-if="project.platform === 'azure'"
                        class="gd-badge-accent">
                    <svg class="h-3 w-3 mr-1" viewBox="0 0 24 24" fill="currentColor">
                      <path
                          d="M22.45 5.11a1 1 0 00-.8-1.6H2.35a1 1 0 00-.8 1.6L10.5 13l.5.6.5-.6 8.95-8.89zM11.5 14.4l-2.5 2.5-2.5-2.5-7.6 7.6a1 1 0 00.8 1.6h18.6a1 1 0 00.8-1.6l-7.6-7.6z"></path>
                    </svg>
                    Azure
                  </span>
                  <span v-else class="gd-badge">
                    {{ project.platform }}
                  </span>
                </div>
                
                <div class="text-right">
                  <div class="text-xs text-muted">Géré par</div>
                  <div class="text-sm font-medium text-text">
                    <span v-if="project.manager">
                      {{ project.manager.firstName }} {{ project.manager.lastName }}
                    </span>
                    <span v-else class="text-muted/60">-</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="sortedProjects.length === 0" class="gd-card p-12 text-center">
          <svg class="mx-auto h-16 w-16 text-muted/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1"
                  d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
          </svg>
          <h3 class="mt-4 text-lg font-semibold text-text">Aucun projet trouvé</h3>
          <p class="mt-1 text-sm text-muted">Vous n'avez pas encore de projets. Commencez par en créer un nouveau
            pour gérer vos dépôts de code.</p>
          <div class="mt-6">
            <button @click="showCreateModal = true" class="gd-btn-primary">
              <svg class="-ml-1 mr-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
              </svg>
              Créer un projet
            </button>
          </div>
        </div>
      </div>

      <!-- Collaborators View -->
      <div v-if="currentView === 'collaborators'">
        <div class="mb-4">
          <button
              @click="showAddForm = !showAddForm"
              class="gd-btn-primary"
          >
            {{ showAddForm ? 'Annuler' : 'Ajouter un collaborateur' }}
          </button>
        </div>

        <!-- Add/Edit Form -->
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

        <!-- Collaborators Grouped by Project -->
        <div v-if="projectStore.collaboratorsGrouped.length > 0" class="space-y-6">
          <div
              v-for="group in projectStore.collaboratorsGrouped"
              :key="group.projectId || 'no-project'"
              class="gd-card overflow-hidden"
          >
            <!-- Group Header -->
            <div class="gd-thead px-6 py-3 border-b border-border">
              <h3 class="text-base font-semibold text-text">
                {{ group.projectName || 'Sans projet' }}
              </h3>
            </div>

            <!-- Collaborators Table -->
            <table class="gd-table">
              <thead class="gd-thead">
              <tr>
                <th class="gd-th">
                  Nom complet
                </th>
                <th class="gd-th">
                  Email
                </th>
                <th class="gd-th">
                  Rôle
                </th>
                <th class="gd-th">
                  État
                </th>
                <th class="gd-th text-right">
                  Actions
                </th>
              </tr>
              </thead>
              <tbody class="bg-surface divide-y divide-border">
              <tr
                  v-for="collaborator in group.collaborators"
                  :key="collaborator.id"
                  class="gd-tr"
              >
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-text">
                    {{ collaborator.firstName }} {{ collaborator.lastName }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-muted">{{ collaborator.email }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span
                        class="gd-badge-accent">
                      {{ getRoleLabel(collaborator.role) }}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span
                        :class="{
                          'gd-badge-success': collaborator.status === 'active',
                          'gd-badge-warning': collaborator.status === 'pending',
                          'gd-badge-danger': collaborator.status === 'disabled'
                        }"
                    >
                      {{ 
                         collaborator.status === 'active' ? 'Actif' : 
                        (collaborator.status === 'pending' ? 'En attente' : 'Désactivé') 
                      }}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <span v-if="collaborator.id === authStore.userId" class="text-muted/60 italic text-xs mr-4">
                    (Vous)
                  </span>
                  <div v-else class="flex items-center justify-end gap-2">
                    <button v-if="canPerform('edit')"
                            @click="handleEditCollaborator(collaborator)"
                            class="gd-btn-ghost px-2 py-2 text-accent hover:text-accentHover"
                            title="Modifier les informations"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>

                    <button
                        v-if="canUnlink(group.projectId)"
                        @click="handleRemoveCollaborator(group.projectId, collaborator.id)"
                        class="gd-btn-ghost px-2 py-2 text-amber-700 hover:text-amber-800 hover:bg-amber-50"
                        title="Retirer du projet"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M13 7a4 4 0 11-8 0 4 4 0 018 0zM9 14a6 6 0 00-6 6v1h12v-1a6 6 0 00-6-6zM21 12h-6"/>
                      </svg>
                    </button>

                    <button v-if="collaborator.status === 'active' && canPerform('soft-delete')"
                            @click="handleSoftDelete(collaborator.id)"
                            class="gd-btn-ghost px-2 py-2 text-amber-700 hover:text-amber-800 hover:bg-amber-50"
                            title="Désactiver l'accès"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                      </svg>
                    </button>

                    <button
                        v-if="collaborator.status !== 'active' && canPerform('restore')"
                        @click="handleRestore(collaborator.id)"
                        class="gd-btn-ghost px-2 py-2 text-emerald-700 hover:text-emerald-800 hover:bg-emerald-50"
                        title="Restaurer l'accès"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                      </svg>
                    </button>

                    <button v-if="canPerform('reset-password')"
                            @click="handleResetPassword(collaborator.email)"
                            class="gd-btn-ghost px-2 py-2 text-violet-700 hover:text-violet-800 hover:bg-violet-50"
                            title="Envoyer email réinitialisation"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                      </svg>
                    </button>

                    <button
                        v-if="(collaborator.status !== 'active' || authStore.role === 'ROLE_SUPER_ADMIN') && canPerform('hard-delete')"
                        @click="handleHardDelete(collaborator.id)"
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

        <!-- Empty State -->
        <div v-else class="gd-card p-12 text-center">
          <svg class="mx-auto h-12 w-12 text-muted/50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
          </svg>
          <h3 class="mt-4 text-lg font-semibold text-text">Aucun collaborateur</h3>
          <p class="mt-1 text-sm text-muted">Commencez par ajouter un nouveau collaborateur.</p>
        </div>
      </div>
    </div>
  </AppLayout>

  <CreateProjectModal
      :is-open="showCreateModal"
      @close="showCreateModal = false"
  />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import { useProjectStore } from '@/stores/projectStore'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore' // Ajout pour la notification

import CreateProjectModal from '@/components/project/CreateProjectModal.vue'
import ProjectForm from '@/components/project/ProjectForm.vue'

import { authService } from '@/services/authService'


const route = useRoute()

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// Suppression du typage : 'hard-delete' | 'soft-delete' ...
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

const showCreateModal = ref(false)

const router = useRouter()
const projectStore = useProjectStore()

// Suppression des `<'projects' | 'collaborators'>`, etc.
const currentView = ref('projects')
const viewMode = ref('list')
const sortOrder = ref('desc')
const showAddForm = ref(false)
const editingCollaborator = ref(undefined) // Suppression de <Collaborator | undefined>

const sortedProjects = computed(() => {
  const projects = [...(projectStore.projects || [])]
  return projects.sort((a, b) => {
    const dateA = new Date(a.createdAt).getTime()
    const dateB = new Date(b.createdAt).getTime()
    return sortOrder.value === 'asc' ? dateA - dateB : dateB - dateA
  })
})

onMounted(async () => {
  
  try {
    if (route.query.openModal === 'true') {
      showCreateModal.value = true;
      // On nettoie l'URL silencieusement
      router.replace('/projects');
    }
    await projectStore.fetchProjects()
    // Commenté temporairement car ces méthodes ne sont peut-être pas encore implémentées dans ton projectStore
    await projectStore.fetchCollaboratorsGrouped();
    // await projectStore.fetchAvailableRoles()
  } catch(e) {
    console.error("Erreur au chargement des données initiales", e)
  }
})

// Suppression de : number
const navigateToProject = (id) => {
  router.push(`/projects/${id}`)
}

// Suppression de : number
const handleDeleteProject = async (id) => {
  const project = projectStore.projects.find(p => p.id === id)
  if (!project) return

  if (confirm(`Êtes-vous sûr de vouloir supprimer le projet "${project.name}" ? Cette action est irréversible.`)) {
    try {
      // S'assurer que projectStore a bien une méthode deleteProject
      if(projectStore.deleteProject) {
        await projectStore.deleteProject(id)
      } else {
        notificationStore.info("Fonctionnalité en développement")
      }
    } catch (error) {
      console.error(error)
    }
  }
}

// Suppression de : CreateUserRequest | UpdateUserRequest
const handleFormSubmit = async (data) => {
  try {
    if (editingCollaborator.value) {
      await projectStore.updateUser(editingCollaborator.value.id, {
        firstName: data.firstName,
        lastName: data.lastName,
        role: data.role
      });
      showAddForm.value = false;
      editingCollaborator.value = undefined;
    } else {
      // ✅ La création avec le bon payload
      if (data.projectId) {
        const payload = {
          email: data.email,
          role: data.role,
          firstName: data.firstName,
          lastName: data.lastName
        };
        await projectStore.addCollaborator(data.projectId, payload);        
        await projectStore.fetchCollaboratorsGrouped();
        
        showAddForm.value = false;
        editingCollaborator.value = undefined;
      } else {
        notificationStore.error("Veuillez sélectionner un projet");
      }
    }
  } catch (error) {
    // 🚨 C'est ce bloc qui avait disparu et qui faisait planter Vite !
    console.error("Erreur du formulaire", error);
  }
}

const handleFormCancel = () => {
  showAddForm.value = false
  editingCollaborator.value = undefined
}

// Suppression de : Collaborator
const handleEditCollaborator = (collaborator) => {
  editingCollaborator.value = collaborator
  showAddForm.value = false
}

// Suppression de : number
const handleSoftDelete = async (id) => {
  if (confirm('Êtes-vous sûr de vouloir désactiver ce collaborateur ?')) {
    try {
      if(projectStore.softDeleteUser) await projectStore.softDeleteUser(id)
    } catch (error) {
      console.error(error)
    }
  }
}

// Suppression de : number
const handleRestore = async (id) => {
  try {
    if(projectStore.restoreUser) await projectStore.restoreUser(id)
  } catch (error) {
    console.error(error)
  }
}

// Suppression de : number
const handleHardDelete = async (id) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer définitivement ce collaborateur ? Cette action est irréversible.')) {
    try {
      if(projectStore.hardDeleteUser) await projectStore.hardDeleteUser(id)
    } catch (error) {
      console.error(error)
    }
  }
}

// Suppression de : string
const handleResetPassword = async (email) => {
  if (confirm(`Envoyer un email de réinitialisation de mot de passe à ${email} ?`)) {
    try {
      await authService.requestPasswordReset(email)
      notificationStore.success('Email de réinitialisation envoyé avec succès')
    } catch (error) {
      console.error(error)
    }
  }
}

// Suppression de : string
const getRoleLabel = (role) => {
  const roleMap = {
    SUPER_ADMIN: 'Super Admin',
    ADMIN: 'Admin',
    MANAGER: 'Manager',
    DEVELOPER: 'Développeur',
  }
  return roleMap[role] || role
}

// Suppression de : string
const getProjectHostname = (urlString) => {
  if(!urlString) return 'N/A'
  try {
    return new URL(urlString).hostname.replace('www.', '')
  } catch (e) {
    return urlString
  }
}

// Suppression de : number | null
const canUnlink = (projectId) => {
  if (!projectId) return false

  if (authStore.role === 'ROLE_SUPER_ADMIN') return true

  const project = projectStore.projects.find(p => p.id === projectId)

  if (!project) return false

  if (project.manager && project.manager.id === authStore.userId) {
    return true
  }

  return false
}

// Suppression de : number | null, : number
const handleRemoveCollaborator = async (projectId, userId) => {
  if (confirm('Voulez-vous retirer ce collaborateur du projet ?')) {
    await projectStore.removeCollaborator(projectId, userId);
  }
};
</script>

