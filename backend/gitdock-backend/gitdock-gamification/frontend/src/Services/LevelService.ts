import axios from 'axios';

const API_URL = "https://localhost:7261/api/Levels";

// Interface pour les pré-requis (TagId + Nombre d'occurrences)
export interface LevelRequirement {
    tagId: string;
    tagName?: string; // Optionnel, pour l'affichage au front
    requiredOccurrences: number;
}

// Interface pour le Level complet
export interface Level {
    id?: string;
    name: string;
    levelRank: number;
    requiredXP: number;
    requirements: LevelRequirement[];
}

export const levelService = {
    // Récupérer tous les niveaux
    async getAll(): Promise<Level[]> {
        const response = await axios.get(API_URL);
        return response.data;
    },

    // Créer un niveau avec ses pré-requis
    async create(level: Level): Promise<Level> {
        const response = await axios.post(API_URL, level);
        return response.data;
    },

    // Mettre à jour un niveau (PUT)
    async update(id: string, level: Level): Promise<Level> {
        const response = await axios.put(`${API_URL}/${id}`, level);
        return response.data;
    },

    // Supprimer un niveau
    async delete(id: string): Promise<void> {
        await axios.delete(`${API_URL}/${id}`);
    }
};