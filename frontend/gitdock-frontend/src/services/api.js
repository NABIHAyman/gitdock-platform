import axios from 'axios'

const api = axios.create({
    // Assure-toi que cette variable est bien dans ton fichier .env
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
})

// Intercepteur pour ajouter le Token JWT à chaque requête
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        // On vérifie que le token est valide avant de l'ajouter
        if (token && token !== 'null' && token !== 'undefined') {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Intercepteur pour gérer les erreurs globales (ex: Token expiré)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', error)

        if (error.response) {
            // Si le token est expiré ou invalide (401)
            if (error.response.status === 401) {
                localStorage.removeItem('token')
                // On pourrait rediriger vers /login ici si nécessaire
            }
            console.error('Response data:', error.response.data)
        }
        return Promise.reject(error)
    }
)

export default api