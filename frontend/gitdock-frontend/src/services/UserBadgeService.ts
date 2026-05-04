import api from './api';

export const userBadgeService = {
    // Récupère les badges de l'utilisateur connecté
    async getMyBadges() {
        const response = await api.get('/UserBadge/my-badges');
        return response.data;
    },

    // Récupère TOUS les badges de la base (pour que le manager choisisse lequel donner)
    // C'est cette méthode qui remplace gamificationService
    async getAllAvailableBadges() {
        const response = await api.get('/Badge'); // Vérifie que ton contrôleur .NET est bien [Route("api/[controller]")]
        return response.data;
    },

    // Attribution manuelle par le Manager
    async awardBadgeManual(userId: number, badgeId: string) {
        return await api.post('/UserBadge/award', {
            userId: userId,
            badgeId: badgeId
        });
    }
};