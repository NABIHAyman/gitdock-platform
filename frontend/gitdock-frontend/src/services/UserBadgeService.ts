import api from './api';

export const userBadgeService = {
    // Récupère uniquement les badges gagnés par l'utilisateur
    async getMyBadges() {
        const response = await api.get('/gamification/my-badges');
        return response.data;
    },

    // Récupère tous les badges existants (pour la BadgeGallery)
    async getAllAvailableBadges() {
        const response = await api.get('/gamification/badges');
        return response.data;
    }
};