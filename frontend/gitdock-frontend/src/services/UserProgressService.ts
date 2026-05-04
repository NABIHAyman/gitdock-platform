import api from './api';

export const userProgressService = {
    // Pour l'utilisateur connecté
    async getProgress() {
        const response = await api.get('/UserProgress/me'); // Correspond au endpoint .NET
        return response.data;
    },

    // Pour le Manager : Liste globale (Leaderboard)
    async getAllContributors() {
        const response = await api.get('/UserProgress/leaderboard');
        return response.data;
    }
};