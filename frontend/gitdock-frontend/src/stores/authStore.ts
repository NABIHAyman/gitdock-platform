import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authService } from '@/services/authService'
import type { LoginCredentials, RegisterCredentials } from '@/services/authService'
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

            await router.push('/dashboard/projects') // Utilise await ici
            return { success: true }
        } catch (error: any) {
            const notificationStore = useNotificationStore()
            const detail = error.response?.data?.detail ?? 'Identifiants invalides'
            notificationStore.error(detail)
            return { success: false, error: detail }
        }
    }

    const signup = async (credentials: RegisterCredentials) => {
        try {
            await authService.signup(credentials)
            const notificationStore = useNotificationStore()
            notificationStore.success("Inscription réussie ! Vérifiez vos emails.")
            setTimeout(async () => await router.push('/login'), 2000)
            return { success: true }
        } catch (error: any) {
            const notificationStore = useNotificationStore()
            const message = error.response?.data?.detail ?? "Erreur lors de l'inscription"
            notificationStore.error(message)
            return { success: false, error: message }
        }
    }

    const activateInvitedAccount = async (inviteToken: string, password: string) => {
        try {
            // On appelle la fonction correcte : 'activateAccount'
            await authService.activateAccount({
                token: inviteToken,
                password: password
            })
            const notificationStore = useNotificationStore()
            notificationStore.success("Compte activé avec succès !")
            await router.push('/login')
        } catch (error) {
            const notificationStore = useNotificationStore()
            notificationStore.error("Erreur d'activation")
        }
    }

    const logout = () => {
        token.value = null
        userId.value = null
        localStorage.clear()
        router.push('/login')
    }

    return {
        token, userId, firstName, lastName, role, email, avatarUrl,
        isAuthenticated, login, signup, activateInvitedAccount, logout
    }

})