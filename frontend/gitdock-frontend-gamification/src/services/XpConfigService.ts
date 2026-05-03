import api from './api';

// Résout TS2305 & TS2352 : On ajoute tous les champs nécessaires
export interface XpConfigDTO {
  id?: number;
  level: number;
  xpRequired: number;
  commitXp: number;   // Ajouté pour correspondre à ton objet
  prXp: number;       // Ajouté
  bugFixXp: number;   // Ajouté
  description?: string;
}

export const xpConfigService = {
  async getAll() {
    const response = await api.get('/xp-configs');
    return response.data;
  },

  // Résout TS2554 : On s'assure que 'id' est bien attendu
  async get(id: number) {
    const response = await api.get(`/xp-configs/${id}`);
    return response.data;
  },

  // Résout TS2554 : On attend l'id ET les données
  async update(id: number, data: XpConfigDTO) {
    const response = await api.put(`/xp-configs/${id}`, data);
    return response.data;
  }
};