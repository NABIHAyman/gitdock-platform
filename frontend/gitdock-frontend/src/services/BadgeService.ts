import api from '@/services/api'

// On définit l'interface pour avoir l'auto-complétion partout
export interface Badge {
    id?: string;
    title: string;
    description: string;
    xp: number;
    icon: string;
    color: string;
    type: string;
}

// Modification dans @/services/BadgeService.ts
export const badgeService = {
    async getAll(): Promise<Badge[]> {
        // Ajout du préfixe /gamification pour passer la Gateway
        const response = await api.get('/gamification/badges')
        return response.data
    },

    async create(badge: Badge): Promise<Badge> {
        const response = await api.post('/gamification/badges', badge)
        return response.data
    },

    async update(id: string, badge: Badge): Promise<Badge> {
        const response = await api.put(`/gamification/badges/${id}`, badge)
        return response.data
    },

    async delete(id: string): Promise<void> {
        await api.delete(`/gamification/badges/${id}`)
    }
};