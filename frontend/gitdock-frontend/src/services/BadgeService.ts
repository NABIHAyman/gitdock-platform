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

export const badgeService = {
    // Récupérer tous les badges
    async getAll(): Promise<Badge[]> {
        const response = await api.get('/badges')
        return response.data
    },

    // Créer un badge
    async create(badge: Badge): Promise<Badge> {
        const response = await api.post('/badges', badge)
        return response.data
    },

    // Mettre à jour un badge
    async update(id: string, badge: Badge): Promise<Badge> {
        const response = await api.put(`/badges/${id}`, badge)
        return response.data
    },

    // Supprimer un badge
    async delete(id: string): Promise<void> {
        await api.delete(`/badges/${id}`)
    }
};