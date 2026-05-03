import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authService, type LoginCredentials, type RegisterCredentials } from '@/services/authService'
import { useNotificationStore } from './notificationStore'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem('token'))
    const userId = ref<number | null>(
        localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null,
    )
    const firstName = ref<string | null>(localStorage.getItem('firstName'))
    const lastName = ref<string | null>(localStorage.getItem('lastName'))
    const role = ref<string | null>(localStorage.getItem('role'))
    const email = ref<string | null>(localStorage.getItem('email'))
    const avatarUrl = ref<string | null>(localStorage.getItem('avatarUrl'))

    const isAuthenticated = computed(() => !!token.value)

    const login = async (credentials: LoginCredentials) => {
        try {
            const response = await authService.login(credentials)

            token.value = response.accessToken
            userId.value = response.user.id
            firstName.value = response.user.firstName
            lastName.value = response.user.lastName
            role.value = response.user.role
            email.value = response.user.email
            avatarUrl.value = response.user.avatarUrl ?? null

            localStorage.setItem('token', response.accessToken)
            localStorage.setItem('userId', String(response.user.id))
            if (response.user.firstName) localStorage.setItem('firstName', response.user.firstName)
            if (response.user.lastName) localStorage.setItem('lastName', response.user.lastName)
            localStorage.setItem('role', response.user.role)
            if (response.user.email) localStorage.setItem('email', response.user.email)
            if (response.user.avatarUrl) localStorage.setItem('avatarUrl', response.user.avatarUrl)

            router.push('/projects')
            return { success: true }
        } catch (error: unknown) {
            const notificationStore = useNotificationStore()
            const axiosError = error as { response?: { status?: number; data?: { detail?: string } } }
            const detail = axiosError.response?.data?.detail ?? 'Une erreur est survenue'

            if (axiosError.response?.status === 401) {
                const message = detail || 'Email ou mot de passe incorrect'
                notificationStore.error(message)
                return { success: false, error: message, needsActivation: false }
            }
            if (axiosError.response?.status === 403) {
                const message = detail || 'Compte non activé'
                notificationStore.error(message)
                return { success: false, error: message, needsActivation: true }
            }

            notificationStore.error(detail)
            return { success: false, error: detail, needsActivation: false }
        }
    }

    const signup = async (credentials: RegisterCredentials) => {
        try {
            await authService.signup(credentials)
            const notificationStore = useNotificationStore()
            notificationStore.success("Inscription réussie ! Un email d'activation a été envoyé.")
            setTimeout(() => router.push('/login'), 2000)
            return { success: true }
        } catch (error: unknown) {
            const notificationStore = useNotificationStore()
            const axiosError = error as {
                response?: { data?: { detail?: string; message?: string } }
            }
            const message =
                axiosError.response?.data?.detail ??
                axiosError.response?.data?.message ??
                "Erreur lors de l'inscription"
            notificationStore.error(message)
            return { success: false, error: message }
        }
    }

    const resendActivationEmail = async (userEmail: string) => {
        try {
            await authService.resendActivationToken(userEmail)
            const notificationStore = useNotificationStore()
            notificationStore.success("Email d'activation renvoyé avec succès")
            return { success: true }
        } catch (error: unknown) {
            const notificationStore = useNotificationStore()
            const axiosError = error as { response?: { data?: { detail?: string } } }
            const message = axiosError.response?.data?.detail ?? "Erreur lors de l'envoi de l'email"
            notificationStore.error(message)
            return { success: false, error: message }
        }
    }

    const activateInvitedAccount = async (inviteToken: string, password: string) => {
        await authService.activateInvitedAccount(inviteToken, password)
    }

    const logout = () => {
        token.value = null
        userId.value = null
        firstName.value = null
        lastName.value = null
        role.value = null
        email.value = null
        avatarUrl.value = null
        localStorage.clear()
        router.push('/login')
    }

    return {
        token,
        userId,
        firstName,
        lastName,
        role,
        email,
        avatarUrl,
        isAuthenticated,
        login,
        signup,
        resendActivationEmail,
        activateInvitedAccount,
        logout,
    }
})