import api from '@/services/api';
import { useAuthStore } from '@/stores/authStore';

/*
const gamificationApi = axios.create({
  baseURL: 'http://localhost:5292/api',
  headers: { 'Content-Type': 'application/json' }
});

gamificationApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
*/

export const userProgressService = {
  async getProgress() {
    const authStore = useAuthStore();
    const response = await api.get(`/UserProgress/${authStore.userId}`);
    return response.data;
  },
  async getAllContributors() {
    const response = await api.get('/Leaderboard');
    return response.data;
  }
};