import api from '@/services/api'

export interface Tag {
  id?: string
  name: string
  color: string
  type: string
}

export const tagService = {
  async getAll(): Promise<Tag[]> {
    const response = await api.get('/tags')
    return response.data
  },
  async create(data: Tag): Promise<Tag> {
    const response = await api.post('/tags', data)
    return response.data
  },
  async update(id: string, data: Tag): Promise<Tag> {
    const response = await api.put(`/tags/${id}`, data)
    return response.data
  },
  async delete(id: string): Promise<void> {
    await api.delete(`/tags/${id}`)
  },
}