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