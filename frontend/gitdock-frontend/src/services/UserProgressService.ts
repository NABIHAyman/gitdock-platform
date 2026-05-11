import api from './api'

export const userProgressService = {

  // GET /api/UserProgress/{userId}
  async getProgress(userId: number) {
    const response = await api.get(`/UserProgress/${userId}`)
    return response.data
  },

  async getAllContributors() {
    const response = await api.get('/UserProgress/leaderboard')
    return response.data
  }
}