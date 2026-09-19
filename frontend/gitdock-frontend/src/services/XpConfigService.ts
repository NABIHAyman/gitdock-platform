import api from '@/services/api' // On utilise l'instance commune

export const xpConfigService = {
  async get() {
    // On passe par la Gateway (port 8080). 
    // La route /api/xpconfig est déjà configurée dans la Gateway vers le port 5292
    const response = await api.get('/xpconfig'); 
    return response.data;
  },

  async update(data: any) {
    const response = await api.put('/xpconfig', data);
    return response.data;
  }
};


/*
// XpConfigService.ts
import axios from 'axios';

// On crée une instance locale ou on utilise l'URL en dur pour tester
export const xpConfigService = {
  async get() {
    // On tape directement sur le port du microservice
    const response = await axios.get('http://localhost:5292/api/XpConfig');
    return response.data;
  },

  async update(data: any) {
    const response = await axios.put('http://localhost:5292/api/XpConfig', data);
    return response.data;
  }
};
*/