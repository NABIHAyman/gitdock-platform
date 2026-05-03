import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

// Intercepteur pour le token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// EXPORT PRINCIPAL
export default api;

// AJOUTE CET EXPORT POUR RÉPARER L'ERREUR XPCONFIGSERVICE
export const badgeApi = api;