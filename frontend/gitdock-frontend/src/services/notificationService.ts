import api from '@/services/api'
import { useAuthStore } from '@/stores/authStore'
import type { AxiosRequestHeaders } from 'axios'

export interface InAppNotification {
  id: number
  title: string
  message: string
  read: boolean
  createdAt: string
}

const notifHeaders = (): Partial<AxiosRequestHeaders> => {
  const authStore = useAuthStore()
  return { 'X-User-Id': String(authStore.userId) }
}

export const notificationService = {
  async getMyNotifications(): Promise<InAppNotification[]> {
    const response = await api.get<InAppNotification[]>('/notifications', {
      headers: notifHeaders(),
    })
    return response.data
  },

  async markAsRead(id: number): Promise<void> {
    await api.patch(`/notifications/${id}/read`, null, { headers: notifHeaders() })
  },

  async markAllAsRead(): Promise<void> {
    await api.patch('/notifications/read-all', null, { headers: notifHeaders() })
  },
}