import api from './api.js'

export const authService = {
    /**
     * Authentification utilisateur
     */
    async login(credentials) {
        // Note: l'URL doit correspondre au mapping de ton Controller Spring Boot
        const response = await api.post('/auth/authenticate', credentials)
        return response.data
    },

    /**
     * Inscription d'un nouvel utilisateur
     */
    async signup(credentials) {
        const payload = {
            firstName: credentials.firstName,
            lastName: credentials.lastName,
            email: credentials.email,
            username: credentials.email.split('@')[0]
        }

        await api.post('/auth/register', payload)
    },

    /**
     * Renvoyer l'email d'activation
     */
    async resendActivationToken(email) {
        await api.post('/auth/user-activation/resend', null, {
            params: { email }
        })
    },

    /**
     * Vérifier si un token d'activation est encore valide
     */
    async validateActivationToken(token) {
        const response = await api.get('/auth/user-activation/validate', {
            params: { token }
        })
        return response.data
    },

    /**
     * Finaliser l'activation avec le mot de passe
     */
    async activateAccount(token, password, confirmPassword) {
        await api.post('/auth/user-activation/confirm', {
            token,
            password,
            confirmPassword
        })
    },

    /**
     * Demande de réinitialisation de mot de passe
     */
    async requestPasswordReset(email) {
        await api.post('/auth/password-reset/request', null, {
            params: { email }
        })
    },

    /**
     * Confirmer le nouveau mot de passe
     */
    async resetPassword(token, newPassword) {
        await api.post('/auth/password-reset/confirm', {
            token,
            newPassword
        })
    }
}