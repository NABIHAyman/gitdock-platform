import api from '@/services/api'

export interface Project {
    id: number
    name: string
    description?: string
    [key: string]: unknown
}

export interface Branch {
    id: number
    name: string
    [key: string]: unknown
}

export interface CommitPage {
    content: Commit[]
    last: boolean
    totalElements: number
    totalPages: number
    number: number
}

export interface Commit {
    id: number | string
    message: string
    author?: string
    date?: string
    [key: string]: unknown
}

export interface Collaborator {
    id: number
    email: string
    firstName?: string
    lastName?: string
    role?: string
    status?: string
    [key: string]: unknown
}

export interface CollaboratorGroup {
    collaborators: Collaborator[]
    [key: string]: unknown
}

export interface AddCollaboratorPayload {
    email: string
    role: string
    firstName?: string
    lastName?: string
}

export interface UpdateUserPayload {
    firstName?: string
    lastName?: string
    email?: string
    [key: string]: unknown
}

export const projectService = {
    async getAllProjects(): Promise<Project[]> {
        const response = await api.get<Project[]>('/projects')
        return response.data
    },

    async createProject(projectData: Partial<Project>): Promise<Project> {
        const response = await api.post<Project>('/projects', projectData)
        return response.data
    },

    async getProjectById(id: number | string): Promise<Project> {
        const response = await api.get<Project>(`/projects/${id}`)
        return response.data
    },

    async getProjectBranches(projectId: number | string): Promise<Branch[]> {
        const response = await api.get<Branch[]>(`/projects/${projectId}/branches`)
        return response.data
    },

    async getBranchCommits(
        projectId: number | string,
        branchId: number | string,
        page = 0,
        size = 20,
    ): /*Promise<CommitPage>*/ Promise<any> {
        const response = await api.get<CommitPage>(
            `/projects/${projectId}/branches/${branchId}/commits?page=${page}&size=${size}`,
        )
        return response.data
    },

    async getCommitDiff(projectId: number | string, hash: string): Promise<string> {
       const response = await api.get<{diff: string}>(`/projects/${projectId}/commits/${hash}/diff`)
       return response.data.diff
    },

    async getProjectCollaborators(projectId: number | string): Promise<Collaborator[]> {
        const response = await api.get<Collaborator[]>(`/projects/${projectId}/collaborators`)
        return response.data
    },

    async addCollaborator(
        projectId: number | string,
        data: AddCollaboratorPayload,
    ): Promise<Collaborator> {
        const response = await api.post<Collaborator>(`/projects/${projectId}/collaborators`, data)
        return response.data
    },

    async getCollaboratorsGrouped(): Promise<CollaboratorGroup[]> {
        const response = await api.get<CollaboratorGroup[]>(
            '/projects/dashboard/collaborators-grouped',
        )
        return response.data
    },

    async initOAuthFlow(platform: string): Promise<{ redirectUrl: string }> {
        const response = await api.get<{ redirectUrl: string }>(
            `/auth/oauth/authorize/${platform}`,
        )
        return response.data
    },

    async handleOAuthCallback(platform: string, code: string): Promise<void> {
        await api.post(`/auth/oauth/callback/${platform}`, { code })
    },

    async deleteProject(id: number | string): Promise<void> {
        await api.delete(`/projects/${id}`)
    },

    async updateUser(id: number | string, userData: UpdateUserPayload): Promise<unknown> {
        const response = await api.put(`/auth/users/${id}`, userData)
        return response.data
    },

    async softDeleteUser(id: number | string): Promise<void> {
        await api.put(`/auth/users/soft-delete/${id}`)
    },

    async hardDeleteUser(id: number | string): Promise<void> {
        await api.delete(`/auth/users/${id}`)
    },

    async restoreUser(id: number | string): Promise<void> {
        await api.post(`/auth/users/${id}/restore`)
    },

    async removeCollaborator(
        projectId: number | string,
        userId: number | string,
    ): Promise<void> {
        await api.delete(`/projects/${projectId}/collaborators/${userId}`)
    },

    async triggerManualSync(projectId: number | string): Promise<void> {
        await api.post(`/projects/${projectId}/sync`)
    },
}