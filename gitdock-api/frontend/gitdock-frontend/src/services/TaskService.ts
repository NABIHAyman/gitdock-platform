import api from '@/services/api'

export interface TaskListItemDTO {
  id: number
  title?: string
  description?: string
  status?: string
  priority?: string
  dueDate?: string | null
  assignedTo?: number | string | null
  assignedBy?: number | string | null
  epic?: number | null
  part?: number | null
  level?: number | null
  projectId?: number | null
}

export interface TaskFormDataDTO {
  epics: Array<{ id: number; title: string }>
  parts: Array<{ id: number; name: string }>
  levels: Array<{ id: number; name: string }>
  users: Array<{ id: number; fullName: string }>
}

export interface TaskCreateUpdatePayload {
  title: string
  description?: string
  status: string
  priority?: string
  dueDate?: string | null
  assignedTo?: number | null
  assignedBy?: number | null
  projectId?: number | null
  epic?: number | null
  part?: number | null
  level?: number | null
}

export interface TaskDashboardDTO {
  [key: string]: unknown
}

/**
 * Chemins relatifs à VITE_API_BASE_URL (ex. http://host:port/api) — pas de préfixe /api en double.
 */
export const TaskService = {
  
  async list(): Promise<TaskListItemDTO[]> {
    const res = await api.get('/tasks');
    const rawData = res.data;
    
    console.log("📦 Payload reçu de /tasks :", rawData);

    if (Array.isArray(rawData)) {
      return rawData;
    }
    
    // Si c'est un objet, on cherche le tableau à l'intérieur (selon les standards Symfony / Spring)
    const extractedArray = rawData?.data || rawData?.['hydra:member'] || rawData?.tasks || rawData?.content;

    if (Array.isArray(extractedArray)) {
       return extractedArray;
    }

    console.error("⚠️ Impossible d'extraire un tableau depuis :", rawData);
    return [];
  },

  async getById(id: number | string): Promise<TaskListItemDTO> {
    const res = await api.get(`/tasks/${id}`)
    return res.data
  },

  async getFormData(): Promise<TaskFormDataDTO> {
    const res = await api.get('/tasks/form-data')
    return res.data
  },

  async getDashboard(): Promise<TaskDashboardDTO> {
    const res = await api.get('/tasks/dashboard')
    return res.data
  },

  async create(payload: TaskCreateUpdatePayload): Promise<TaskListItemDTO> {
    const res = await api.post('/tasks', payload)
    return res.data
  },

  async update(id: number | string, payload: TaskCreateUpdatePayload): Promise<TaskListItemDTO> {
    const res = await api.put(`/tasks/${id}`, payload)
    return res.data
  },

  async softDelete(id: number | string): Promise<void> {
    await api.put(`/tasks/${id}/soft-delete`)
  },
}