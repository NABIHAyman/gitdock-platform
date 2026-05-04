import api from '@/services/api'

// --- INTERFACES ---
export interface RegisterCredentials {
    firstName: string
    lastName: string
    email: string
    username?: string
}

export interface LoginCredentials {
    email: string
    password: string
}

// --- SERVICE ---
export const authService = {

    async signup(userData: RegisterCredentials) {
        const response = await api.post('/auth/register', userData)
        return response.data
    },

    async login(credentials: LoginCredentials) {
        const response = await api.post('/auth/authenticate', credentials)
        return response.data
    },

    /**
     * Finalise l'activation (Mise à jour pour correspondre au @PostMapping("/confirm") du Java)
     * URL complète : /api/auth/user-activation/confirm
     */
    async activateAccount(data: { token: string; password: string; confirmPassword?: string }) {
        const response = await api.post('/auth/user-activation/accept-invitation', data)
        return response.data
    },

    /**
     * Valide le token (Mise à jour pour correspondre au @GetMapping("/validate") du Java)
     * URL complète : /api/auth/user-activation/validate
     */
    async validateActivationToken(token: string) {
        const response = await api.get('/auth/user-activation/validate', { params: { token } })
        return response.data
    },

    /**
     * Renvoie l'email (Déjà correct avec ton Java @PostMapping("/resend"))
     * URL complète : /api/auth/user-activation/resend
     */
    async resendActivation(email: string) {
        const response = await api.post('/auth/user-activation/resend', null, {
            params: { email }
        })
        return response.data
    },

    /**
     * Demande de réinitialisation (Résout l'erreur TS2339)
     */
    async requestPasswordReset(email: string) {
        const response = await api.post('/auth/request-password-reset', { email })
        return response.data
    },

    /**
     * Réinitialisation finale
     */
    async resetPassword(token: string, password: string) {
        const response = await api.post('/auth/reset-password', { token, password })
        return response.data
    },

    async acceptInvitation(data: { token: string; password: string; confirmPassword: string }) {
        const response = await api.post('/auth/user-activation/accept-invitation', data)
        return response.data
    },
}