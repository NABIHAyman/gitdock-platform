import api from '@/services/api'
import { useAuthStore } from '@/stores/authStore'
// 1. On change l'import ici
import type { RawAxiosRequestHeaders } from 'axios'

export interface InAppNotification {
  id: number
  title: string
  message: string
  read: boolean
  createdAt: string
}

// 2. On utilise RawAxiosRequestHeaders ici
const notifHeaders = (): RawAxiosRequestHeaders => {
  const authStore = useAuthStore()
  // On retourne un objet simple, TypeScript sera content
  return { 'X-User-Id': String(authStore.userId) }
}

export const notificationService = {
  async getMyNotifications(): Promise<InAppNotification[]> {
    // 3. On précise bien le type de retour <InAppNotification[]> pour éviter l'erreur sur 'T'
    const response = await api.get<InAppNotification[]>('/notifications', {
      headers: notifHeaders(),
    })
    return response.data
  },

  async markAsRead(id: number): Promise<void> {
    await api.patch(`/notifications/${id}/read`, null, {
      headers: notifHeaders()
    })
  },

  async markAllAsRead(): Promise<void> {
    await api.patch('/notifications/read-all', null, {
      headers: notifHeaders()
    })
  },
}