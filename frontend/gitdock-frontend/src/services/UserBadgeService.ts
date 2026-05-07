import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const gamificationApi = axios.create({
  baseURL: 'http://localhost:5292/api',
  headers: { 'Content-Type': 'application/json' }
});

gamificationApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const userBadgeService = {
  async getMyBadges() {
    const authStore = useAuthStore();
    const response = await gamificationApi.get(`/UserBadge/my-badges?userId=${authStore.userId}`);
    return response.data;
  },
  async getByUserId(userId: string | number) {
    const response = await gamificationApi.get(`/UserBadge/user/${userId}`);
    return response.data;
  },
  async getAllAvailableBadges() {
    const response = await gamificationApi.get('/Badges');
    return response.data;
  },
  async awardBadgeManual(userId: number, badgeId: string) {
    return await gamificationApi.post('/UserBadge/award', {
      userId: userId,
      badgeId: badgeId
    });
  }
};