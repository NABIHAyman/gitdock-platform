import axios from 'axios';

const api = axios.create({
    // Utilise exactement le nom défini dans ton .env
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;
export const badgeApi = api;