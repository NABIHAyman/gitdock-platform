import api from './api'
import { useAuthStore } from '@/stores/authStore'

export const userBadgeService = {

  // GET /api/UserBadge/my-badges?userId={id}
  // Ton backend attend userId en query param (pas JWT)
  async getMyBadges() {
    const authStore = useAuthStore()
    const userId = authStore.userId
    if (!userId) throw new Error('userId introuvable')
    const response = await api.get('/UserBadge/my-badges', {
      params: { userId }
    })
    return response.data
  },

  // GET /api/Badges  (BadgesController → [controller] = "Badges")
  async getAllAvailableBadges() {
    const response = await api.get('/Badges')
    return response.data
  },

  // POST /api/UserBadge/award
  async awardBadgeManual(userId: number, badgeId: string) {
    return await api.post('/UserBadge/award', { userId, badgeId })
  }
}