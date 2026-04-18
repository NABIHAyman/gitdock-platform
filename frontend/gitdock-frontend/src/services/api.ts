import axios, { type AxiosInstance } from 'axios'

function normalizeBaseURL(raw: unknown): string {
  if (typeof raw !== 'string' || !raw.trim()) {
    if (import.meta.env.DEV) {
      console.warn('[api] VITE_API_BASE_URL is missing; configure it in .env')
    }
    return ''
  }
  return raw.replace(/\/+$/, '')
}

const api: AxiosInstance = axios.create({
  baseURL: normalizeBaseURL(import.meta.env.VITE_API_BASE_URL),
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

api.interceptors.request.use(
  async (config) => {
    try {
      // import dynamique pour éviter le cycle api ↔ authStore ↔ authService ↔ api
      const { useAuthStore } = await import('@/stores/authStore')
      const authStore = useAuthStore()
      const token = authStore.token
      if (token && typeof token === 'string' && token !== 'null' && token !== 'undefined') {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch {
      // Pinia pas encore prêt (tests / import très tôt) : pas de header Bearer
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error)

    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('token')
      }
      console.error('Response data:', error.response.data)
    }
    return Promise.reject(error)
  }
)

export default api
