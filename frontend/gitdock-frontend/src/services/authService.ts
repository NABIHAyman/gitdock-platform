import api from '@/services/api'

export interface LoginCredentials {
    email: string
    password: string
}

export interface RegisterCredentials {
    firstName: string
    lastName: string
    email: string
}

export interface AuthUser {
    id: number
    firstName: string
    lastName: string
    email: string
    role: string
    avatarUrl?: string
}

export interface AuthResponse {
    accessToken: string
    user: AuthUser
}

export const authService = {
    async login(credentials: LoginCredentials): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/authenticate', credentials)
        return response.data
    },

    async signup(credentials: RegisterCredentials): Promise<void> {
        const payload = {
            firstName: credentials.firstName,
            lastName: credentials.lastName,
            email: credentials.email,
            username: credentials.email.split('@')[0],
        }
        await api.post('/auth/register', payload)
    },

    async resendActivationToken(email: string): Promise<void> {
        await api.post('/auth/user-activation/resend', null, { params: { email } })
    },

    async validateActivationToken(token: string): Promise<boolean> {
        const response = await api.get<boolean>('/auth/user-activation/validate', {
            params: { token },
        })
        return response.data
    },

    async activateAccount(token: string, password: string, confirmPassword: string): Promise<void> {
        await api.post('/auth/user-activation/confirm', { token, password, confirmPassword })
    },

    async requestPasswordReset(email: string): Promise<void> {
        await api.post('/auth/password-reset/request', null, { params: { email } })
    },

    async resetPassword(token: string, newPassword: string): Promise<void> {
        await api.post('/auth/password-reset/confirm', { token, newPassword })
    },

    async activateInvitedAccount(token: string, password: string): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/user-activation/accept-invitation', {
            token,
            password,
        })
        return response.data
    },
}