import api from '@/services/api'

export interface LevelRequirement {
    tagId: string
    tagName?: string
    requiredOccurrences: number
}

export interface Level {
    id?: string
    name: string
    levelRank: number
    requiredXP: number
    requirements: LevelRequirement[]
}

export const levelService = {
    async getAll(): Promise<Level[]> {
        const response = await api.get<Level[]>('/levels') // ✅ minuscule — correspond au prédicat Gateway
        return response.data
    },

    async create(level: Level): Promise<Level> {
        const response = await api.post<Level>('/levels', level)
        return response.data
    },

    async update(id: string, level: Level): Promise<Level> {
        const response = await api.put<Level>(`/levels/${id}`, level)
        return response.data
    },

    async delete(id: string): Promise<void> {
        await api.delete(`/levels/${id}`)
    },
}