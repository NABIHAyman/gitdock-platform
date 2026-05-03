import api from './api';

export const userProgressService = {
    // Récupère la progression globale de l'utilisateur connecté
    async getProgress() {
        const response = await api.get('/gamification/progress');
        return response.data;
    },

    // Récupère l'historique des gains d'XP (ex: +10 XP pour un commit)
    async getXpHistory() {
        const response = await api.get('/gamification/xp-history');
        return response.data;
    }
};