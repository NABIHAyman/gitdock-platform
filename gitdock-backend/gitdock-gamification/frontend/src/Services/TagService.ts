import axios from 'axios'
const API_URL = "https://localhost:7261/api/tags"
export const tagService = {
    getAll: () => axios.get(API_URL),
    create: (data: any) => axios.post(API_URL, data),
    update: (id: string, data: any) => axios.put(`${API_URL}/${id}`, data),
    delete: (id: string) => axios.delete(`${API_URL}/${id}`)
}