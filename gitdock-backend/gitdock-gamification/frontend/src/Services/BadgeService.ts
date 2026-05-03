import axios from 'axios';

const API_URL = "https://localhost:7261/api/badges";

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
        const response = await axios.get(API_URL);
        return response.data;
    },

    // Créer un badge
    async create(badge: Badge): Promise<Badge> {
        const response = await axios.post(API_URL, badge);
        return response.data;
    },

    // Mettre à jour un badge
    async update(id: string, badge: Badge): Promise<Badge> {
        const response = await axios.put(`${API_URL}/${id}`, badge);
        return response.data;
    },

    // Supprimer un badge
    async delete(id: string): Promise<void> {
        await axios.delete(`${API_URL}/${id}`);
    }
};