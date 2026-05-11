import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
    projectService,
    type Branch,
    type Collaborator,
    type CollaboratorGroup,
    type CommitPage,
    type Project,
    type AddCollaboratorPayload,
    type UpdateUserPayload,
} from '@/services/projectService'
import { useNotificationStore } from './notificationStore'
import { authService } from '@/services/authService'

export const useProjectStore = defineStore('project', () => {
    const projects = ref<Project[]>([])
    const currentProject = ref<Project | null>(null)
    const branches = ref<Branch[]>([])
    const commits = ref<CommitPage['content']>([])
    const currentCommitPage = ref(0)
    const hasMoreCommits = ref(true)
    const isFetchingMore = ref(false)
    const isLoading = ref(false)
    const collaboratorsGrouped = ref<CollaboratorGroup[]>([])
    const projectCollaborators = ref<Collaborator[]>([])

    const availableRoles = ref([
        { role: 'MANAGER', label: 'Manager' },
        { role: 'DEVELOPER', label: 'Développeur' },
        { role: 'TESTER', label: 'Testeur' }
    ])

    const fetchProjects = async () => {
        isLoading.value = true
        try {
            projects.value = await projectService.getAllProjects()
        } catch (error) {
            useNotificationStore().error('Erreur lors du chargement des projets')
            console.error(error)
        } finally {
            isLoading.value = false
        }
    }

    const deleteProject = async (id: number | string) => {
        isLoading.value = true
        try {
            await projectService.deleteProject(id)
            projects.value = projects.value.filter((p) => p.id !== id)
            useNotificationStore().success('Projet supprimé avec succès')
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            const msg = axiosError.response?.data?.message ?? 'Erreur lors de la suppression du projet'
            useNotificationStore().error(msg)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    const createProject = async (projectData: Partial<Project>) => {
        isLoading.value = true
        try {
            const newProject = await projectService.createProject(projectData)
            projects.value.push(newProject)
            useNotificationStore().success('Projet créé avec succès !')
            return newProject
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            const msg = axiosError.response?.data?.message ?? 'Erreur lors de la création du projet'
            if (msg !== 'OAUTH_REQUIRED') useNotificationStore().error(msg)
            throw error
        } finally {
            isLoading.value = false
        }
    }

    const fetchProjectBranches = async (projectId: number | string) => {
        isLoading.value = true
        try {
            const [projectData, branchesData] = await Promise.all([
                projectService.getProjectById(projectId),
                projectService.getProjectBranches(projectId).catch(() => [] as Branch[]),
            ])
            currentProject.value = projectData
            branches.value = branchesData
        } catch (error) {
            useNotificationStore().error('Impossible de charger les détails du projet')
            console.error(error)
        } finally {
            isLoading.value = false
        }
    }

    const fetchBranchCommits = async (
        projectId: number | string,
        branchId: number | string,
        page = 0,
    ) => {
        if (page === 0) {
            isLoading.value = true
            commits.value = []
        } else {
            isFetchingMore.value = true
        }

        try {
            if (!currentProject.value || currentProject.value.id !== projectId) {
                currentProject.value = await projectService.getProjectById(projectId)
            }

            const response = await projectService.getBranchCommits(projectId, branchId, page)

            if (page === 0) {
                commits.value = response.content
            } else {
                commits.value.push(...response.content)
            }

            currentCommitPage.value = page
            hasMoreCommits.value = !response.last
        } catch (error) {
            useNotificationStore().error('Impossible de charger les commits de cette branche')
            console.error(error)
        } finally {
            isLoading.value = false
            isFetchingMore.value = false
        }
    }

    const fetchCollaboratorsGrouped = async () => {
        isLoading.value = true
        try {
            collaboratorsGrouped.value = await projectService.getCollaboratorsGrouped()
        } catch (error) {
            useNotificationStore().error('Erreur lors du chargement des collaborateurs')
            console.error("Détail de l'erreur collaborateurs :", error)
        } finally {
            isLoading.value = false
        }
    }

    const removeCollaborator = async (projectId: number | string, userId: number | string) => {
        try {
            await projectService.removeCollaborator(projectId, userId)
            useNotificationStore().success('Collaborateur retiré avec succès')
            await fetchCollaboratorsGrouped()
        } catch (error) {
            console.error('Erreur critique lors de la suppression :', error)
            useNotificationStore().error('Erreur lors du retrait du collaborateur')
        }
    }

    const fetchProjectCollaborators = async (projectId: number | string) => {
        isLoading.value = true
        try {
            projectCollaborators.value = await projectService.getProjectCollaborators(projectId)
        } catch {
            useNotificationStore().error('Impossible de charger les collaborateurs')
        } finally {
            isLoading.value = false
        }
    }

    const addCollaborator = async (
        projectId: number | string,
        payload: AddCollaboratorPayload,
    ) => {
        isLoading.value = true
        try {
            await projectService.addCollaborator(projectId, payload)
            useNotificationStore().success('Collaborateur ajouté avec succès')
            await fetchProjectCollaborators(projectId)
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            useNotificationStore().error(axiosError.response?.data?.message ?? "Erreur lors de l'ajout")
        } finally {
            isLoading.value = false
        }
    }

    const updateCollaboratorStatusLocally = (userId: number, newStatus: string) => {
        collaboratorsGrouped.value.forEach((group) => {
            const collaborator = group.collaborators.find((c) => c.id === userId)
            if (collaborator) collaborator.status = newStatus
        })
    }

    const updateUser = async (id: number | string, userData: UpdateUserPayload) => {
        isLoading.value = true
        try {
            await projectService.updateUser(id, userData)
            useNotificationStore().success('Collaborateur modifié avec succès')
            await fetchCollaboratorsGrouped()
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            useNotificationStore().error(
                axiosError.response?.data?.message ?? 'Erreur lors de la modification',
            )
        } finally {
            isLoading.value = false
        }
    }

    const softDeleteUser = async (id: number | string) => {
        isLoading.value = true
        try {
            await projectService.softDeleteUser(id)
            updateCollaboratorStatusLocally(Number(id), 'disabled')
            useNotificationStore().success('Collaborateur désactivé avec succès')
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            useNotificationStore().error(
                axiosError.response?.data?.message ?? 'Erreur lors de la désactivation',
            )
        } finally {
            isLoading.value = false
        }
    }

    const restoreUser = async (id: number | string) => {
        isLoading.value = true
        try {
            await projectService.restoreUser(id)
            updateCollaboratorStatusLocally(Number(id), 'active')
            useNotificationStore().success('Collaborateur restauré avec succès')
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            useNotificationStore().error(
                axiosError.response?.data?.message ?? 'Erreur lors de la restauration',
            )
        } finally {
            isLoading.value = false
        }
    }

    const hardDeleteUser = async (id: number | string) => {
        isLoading.value = true
        try {
            await projectService.hardDeleteUser(id)
            useNotificationStore().success('Collaborateur supprimé définitivement')
            await fetchCollaboratorsGrouped()
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { message?: string } } }
            useNotificationStore().error(
                axiosError.response?.data?.message ?? 'Erreur lors de la suppression',
            )
        } finally {
            isLoading.value = false
        }
    }

    const requestPasswordReset = async (email: string) => {
        isLoading.value = true
        try {
            await authService.requestPasswordReset(email)
            useNotificationStore().success('Email de réinitialisation envoyé avec succès')
        } catch (error: unknown) {
            const axiosError = error as { response?: { data?: { detail?: string } } }
            useNotificationStore().error(
                axiosError.response?.data?.detail ?? "Erreur lors de l'envoi de l'email",
            )
            console.error('Erreur Reset Password :', error)
        } finally {
            isLoading.value = false
        }
    }

    return {
        projects,
        currentProject,
        branches,
        commits,
        isLoading,
        collaboratorsGrouped,
        availableRoles,
        projectCollaborators,
        currentCommitPage,
        hasMoreCommits,
        isFetchingMore,
        fetchProjects,
        createProject,
        deleteProject,
        fetchProjectBranches,
        fetchBranchCommits,
        fetchCollaboratorsGrouped,
        addCollaborator,
        removeCollaborator,
        fetchProjectCollaborators,
        updateUser,
        softDeleteUser,
        restoreUser,
        hardDeleteUser,
        requestPasswordReset,
    }
})