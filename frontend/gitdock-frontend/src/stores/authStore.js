import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authService } from '@/services/authService'
import { useNotificationStore } from './notificationStore'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
    // Initialisation depuis le localStorage
    const token = ref(localStorage.getItem('token'))
    const userId = ref(localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null)
    const firstName = ref(localStorage.getItem('firstName'))
    const lastName = ref(localStorage.getItem('lastName'))
    const role = ref(localStorage.getItem('role'))

    const isAuthenticated = computed(() => !!token.value)

    const login = async (credentials) => {
        try {
            const response = await authService.login(credentials)

            const receivedToken = response.accessToken
            const uId = response.user.id
            const userFirstName = response.user.firstName
            const userLastName = response.user.lastName
            const mainRole = response.user.role

            // Mise à jour de l'état réactif
            role.value = mainRole
            token.value = receivedToken
            userId.value = uId
            firstName.value = userFirstName
            lastName.value = userLastName

            // Persistance
            localStorage.setItem('token', receivedToken)
            localStorage.setItem('userId', String(uId))
            if (userFirstName) localStorage.setItem('firstName', userFirstName)
            if (userLastName) localStorage.setItem('lastName', userLastName)
            localStorage.setItem('role', mainRole)

            router.push('/projects')
            return { success: true }

        } catch (error) {
            const notificationStore = useNotificationStore()
            const detail = error.response?.data?.detail || 'Une erreur est survenue'

            if (error.response?.status === 401) {
                const message = detail || 'Email ou mot de passe incorrect'
                notificationStore.error(message)
                return { success: false, error: message, needsActivation: false }
            }

            if (error.response?.status === 403) {
                const message = detail || 'Compte non activé'
                notificationStore.error(message)
                return { success: false, error: message, needsActivation: true }
            }

            notificationStore.error(detail)
            return { success: false, error: detail, needsActivation: false }
        }
    }

    const signup = async (credentials) => {
        try {
            await authService.signup(credentials)
            const notificationStore = useNotificationStore()
            notificationStore.success('Inscription réussie ! Un email d\'activation a été envoyé.')

            setTimeout(() => {
                router.push('/login')
            }, 2000)

            return { success: true }
        } catch (error) {
            const notificationStore = useNotificationStore()
            let message = error.response?.data?.detail || error.response?.data?.message || 'Erreur lors de l\'inscription'

            notificationStore.error(message)
            return { success: false, error: message }
        }
    }

    const resendActivationEmail = async (email) => {
        try {
            await authService.resendActivationToken(email)
            const notificationStore = useNotificationStore()
            notificationStore.success('Email d\'activation renvoyé avec succès')
            return { success: true }
        } catch (error) {
            const notificationStore = useNotificationStore()
            const message = error.response?.data?.detail || 'Erreur lors de l\'envoi de l\'email'
            notificationStore.error(message)
            return { success: false, error: message }
        }
    }

    const logout = () => {
        token.value = null
        userId.value = null
        firstName.value = null
        lastName.value = null
        role.value = null

        localStorage.clear() // Plus radical et propre pour un logout
        router.push('/login')
    }

    return {
        token,
        userId,
        firstName,
        lastName,
        role,
        isAuthenticated,
        login,
        signup,
        resendActivationEmail,
        logout,
    }
})