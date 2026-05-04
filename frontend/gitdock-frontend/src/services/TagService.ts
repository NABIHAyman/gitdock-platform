import axios from 'axios';

// Instance dédiée au microservice Gamification (Port 5292)
const tagApi = axios.create({
  baseURL: 'http://localhost:5292/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Intercepteur pour injecter le token JWT
tagApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface Tag {
  id?: string;
  name: string;
  color: string;
  type: string;
}

export const tagService = {
  async getAll(): Promise<Tag[]> {
    const response = await tagApi.get('/tags');
    return response.data;
  },

  async create(data: Tag): Promise<Tag> {
    const response = await tagApi.post('/tags', data);
    return response.data;
  },

  async update(id: string, data: Tag): Promise<Tag> {
    // Correction de l'erreur TS2304 : on utilise directement 'id' et 'data'
    const response = await tagApi.put(`/tags/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await tagApi.delete(`/tags/${id}`);
  }
};