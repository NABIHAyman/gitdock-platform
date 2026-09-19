import api from '@/services/api'

export interface AuthKPIs {
  totalUsers: number
  totalCompanies: number
  subscriptions: {
    FREE: number
    PRO: number
    ENTERPRISE: number
  }
}

export interface ProjectKPIs {
  totalProjects: number
  totalCommits: number
}

export interface AdminUser {
  id: number
  email: string
  firstName?: string
  lastName?: string
  status: 'active' | 'suspended' | 'disabled'
  role?: string
  [key: string]: unknown
}

export const adminService = {
  async getAuthKPIs(): Promise<AuthKPIs> {
    const response = await api.get<AuthKPIs>('/auth/admin/kpis')
    return response.data
  },

  async getProjectKPIs(): Promise<ProjectKPIs> {
    const response = await api.get<ProjectKPIs>('/projects/admin/kpis')
    return response.data
  },

  async getUsers(): Promise<AdminUser[]> {
    const response = await api.get<AdminUser[]>('/auth/users')
    return response.data
  },

  async suspendUser(id: number | string): Promise<void> {
    await api.put(`/auth/users/soft-delete/${id}`)
  },

  async reactivateUser(id: number | string): Promise<void> {
    await api.post(`/auth/users/${id}/restore`)
  },

  async deleteUser(id: number | string): Promise<void> {
    await api.delete(`/auth/users/${id}`)
  },
}