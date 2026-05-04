import axios from 'axios'

const gamificationApi = axios.create({
    baseURL: 'http://localhost:5292/api',
    headers: { 'Content-Type': 'application/json' }
})

// --- INTERFACES ---
export interface ContributorBadge {
    badgeId: string
    title: string
    icon: string
    color: string
    unlockedAt: string
}

export interface Contributor {
    userId: number
    fullName: string
    avatarUrl?: string
    totalExperience: number
    currentLevel: number
    levelName: string
    badges: ContributorBadge[]
}

export interface Badge {
    id: string
    title: string
    description: string
    icon: string
    color: string
    xp: number
}

// --- SERVICE ---
export const contributorService = {
    // Tous les contributeurs avec leur progression
    async getAll(): Promise<Contributor[]> {
        const response = await gamificationApi.get<Contributor[]>('/Contributors')
        return response.data
    },

    // Tous les badges disponibles (pour le manager)
    async getAllBadges(): Promise<Badge[]> {
        const response = await gamificationApi.get<Badge[]>('/Badges')  // ← Badges avec S
        return response.data
    },

    // Attribution manuelle d'un badge
    async awardBadge(userId: number, badgeId: string): Promise<void> {
        await gamificationApi.post('/UserBadge/award', { userId, badgeId })
    }
}