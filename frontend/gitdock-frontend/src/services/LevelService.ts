import axios from 'axios'

// --- INTERFACES ---
export interface LevelRequirement {
    tagId: string
    tagName?: string
    requiredOccurrences: number
}

export interface LevelResponseDto {
    id: string
    name: string
    levelRank: number
    requiredXP: number
    levelTagRequirements: LevelRequirement[]
}

export interface CreateLevelDto {
    name: string
    levelRank: number
    requiredXP: number
    levelTagRequirements: { tagId: string; requiredOccurrences: number }[]
    //  ^^^^^^^^^^^^^^^^^^^^ corrigé : était "requirements", doit matcher le C# DTO
}

// --- CONFIGURATION API ---
const gamificationApi = axios.create({
    baseURL: 'http://localhost:5292/api',
    headers: {
        'Content-Type': 'application/json'
    }
})

// --- SERVICE ---
export const levelService = {
    async getAll(): Promise<LevelResponseDto[]> {
        const response = await gamificationApi.get<LevelResponseDto[]>('/levels')
        return response.data
    },

    async create(level: CreateLevelDto): Promise<LevelResponseDto> {
        const response = await gamificationApi.post<LevelResponseDto>('/levels', level)
        return response.data
    },

    async update(id: string, level: CreateLevelDto): Promise<void> {
        console.log('[LevelService] UPDATE → id =', id, '| payload =', level) // debug temporaire
        if (!id) throw new Error('ID manquant pour la mise à jour du niveau')
        await gamificationApi.put(`/levels/${id}`, level)
    },

    async delete(id: string): Promise<void> {
        await gamificationApi.delete(`/levels/${id}`)
    }
}