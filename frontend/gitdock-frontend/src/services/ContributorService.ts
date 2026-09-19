import api from '@/services/api'

/*
const gamificationApi = axios.create({
    baseURL: 'http://localhost:5292/api',
    headers: { 'Content-Type': 'application/json' }
})

// ✅ Même logique que api.ts
gamificationApi.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})
*/

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

export interface ContributorsByProject {
    projectId: number
    projectName: string
    contributors: Contributor[]
}

export interface Badge {
    id: string
    title: string
    description: string
    icon: string
    color: string
    xp: number
    type: string | number
}

// --- SERVICE ---
export const contributorService = {
    async getAll(): Promise<Contributor[]> {
        const response = await api.get<Contributor[]>('/Contributors')
        return response.data
    },

    async getByProject(): Promise<ContributorsByProject[]> {
        const response = await api.get<ContributorsByProject[]>('/Contributors/by-project')
        return response.data
    },

    async getAllBadges(): Promise<Badge[]> {
        const response = await api.get<Badge[]>('/badges')
        return response.data
    },

    async awardBadge(userId: number, badgeId: string): Promise<void> {
        await api.post('/UserBadge/award', { userId, badgeId })
    }
}