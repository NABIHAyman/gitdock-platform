import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { adminService, type AdminUser } from '@/services/adminService'
import { useNotificationStore } from './notificationStore'

interface KPIs {
  totalCompanies: number
  totalUsers: number
  totalProjects: number
  totalCommits: number
}

interface Subscriptions {
  FREE: number
  PRO: number
  ENTERPRISE: number
}

export const useAdminStore = defineStore('admin', () => {
  const isLoading = ref(false)

  const kpis = ref<KPIs>({
    totalCompanies: 0,
    totalUsers: 0,
    totalProjects: 0,
    totalCommits: 0,
  })

  const subscriptions = ref<Subscriptions>({ FREE: 0, PRO: 0, ENTERPRISE: 0 })
  const users = ref<AdminUser[]>([])

  const totalRevenue = computed(
    () => subscriptions.value.PRO * 29 + subscriptions.value.ENTERPRISE * 99,
  )

  const loadDashboard = async () => {
    isLoading.value = true
    try {
      const [authData, projectData, usersList] = await Promise.all([
        adminService.getAuthKPIs(),
        adminService.getProjectKPIs(),
        adminService.getUsers(),
      ])

      kpis.value.totalUsers = authData.totalUsers
      kpis.value.totalCompanies = authData.totalCompanies
      subscriptions.value = authData.subscriptions
      kpis.value.totalProjects = projectData.totalProjects
      kpis.value.totalCommits = projectData.totalCommits
      users.value = usersList
    } catch (error) {
      useNotificationStore().error('Erreur de synchronisation du Dashboard')
      console.error(error)
    } finally {
      isLoading.value = false
    }
  }

  const toggleUserStatus = async (user: AdminUser) => {
    try {
      if (user.status === 'active') {
        await adminService.suspendUser(user.id)
        user.status = 'suspended'
        useNotificationStore().success('Utilisateur suspendu')
      } else {
        await adminService.reactivateUser(user.id)
        user.status = 'active'
        useNotificationStore().success('Utilisateur réactivé')
      }
    } catch {
      useNotificationStore().error("Erreur lors de l'action")
    }
  }

  const hardDeleteUser = async (id: number | string) => {
    if (!confirm('Suppression définitive. Continuer ?')) return
    try {
      await adminService.deleteUser(id)
      users.value = users.value.filter((u) => u.id !== id)
      kpis.value.totalUsers--
      useNotificationStore().success('Fantôme activé. Utilisateur supprimé.')
    } catch {
      useNotificationStore().error('Erreur de suppression')
    }
  }

  return {
    isLoading,
    kpis,
    subscriptions,
    users,
    totalRevenue,
    loadDashboard,
    toggleUserStatus,
    hardDeleteUser,
  }
})