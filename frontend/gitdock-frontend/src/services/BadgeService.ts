import axios from 'axios';

const gamificationApi = axios.create({
  baseURL: 'http://localhost:5292/api',
  headers: { 'Content-Type': 'application/json' }
});

gamificationApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export interface Badge {
  id?: string;
  title: string;
  description: string;
  xp: number;
  icon: string;
  color: string;
  type: string;
}

export const badgeService = {
  async getAll(): Promise<Badge[]> {
    const response = await gamificationApi.get('/Badges');
    return response.data;
  },
  async create(badge: Badge): Promise<Badge> {
    const response = await gamificationApi.post('/Badges', badge);
    return response.data;
  },
  async update(id: string, badge: Badge): Promise<Badge> {
    const response = await gamificationApi.put(`/Badges/${id}`, badge);
    return response.data;
  },
  async delete(id: string): Promise<void> {
    await gamificationApi.delete(`/Badges/${id}`);
  }
};