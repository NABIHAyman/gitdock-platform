import api from '@/services/api'

export interface XpConfigDTO {
  commitXp: number
  prXp: number
  bugFixXp: number
}

export const xpConfigService = {
  async get(): Promise<XpConfigDTO> {
    const response = await api.get('/xpconfig')
    return response.data
  },

  async update(payload: XpConfigDTO): Promise<XpConfigDTO> {
    const response = await api.put('/xpconfig', payload)
    return response.data
  },
}

