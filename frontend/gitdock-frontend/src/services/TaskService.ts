import axios from 'axios';

const API_URL = 'http://localhost:8080/api/tasks';

export interface TaskCreateUpdatePayload {
  title: string;
  description: string;
  status: string;
  priority: string;
  dueDate: string | null;
  assignedTo: string | null;
  projectId: number;
  epicId: number | null;
  levelId: number | null;
}

export interface ProjectWithCollaborators {
  id: number;
  name: string;
  collaborators: Array<{ id: number; fullName: string }>;
}

export interface TaskFormData {
  projects: ProjectWithCollaborators[];
}

function authHeaders() {
  const token = localStorage.getItem('token')
      ?? localStorage.getItem('jwt')
      ?? localStorage.getItem('authToken');
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };
}

/** Extrait toujours un tableau depuis n'importe quelle réponse API */
function extractArray(data: any): any[] {
  if (Array.isArray(data)) return data;
  if (data && Array.isArray(data.data)) return data.data;
  if (data && Array.isArray(data.tasks)) return data.tasks;
  console.warn('TaskService: réponse inattendue, tableau vide retourné', data);
  return [];
}

export class TaskService {

  static async list(): Promise<any[]> {
    const response = await axios.get(API_URL, authHeaders());
    return extractArray(response.data);
  }

  static async getFormData(projectId?: number): Promise<TaskFormData> {
    const url = projectId
        ? `${API_URL}/form-data?projectId=${projectId}`
        : `${API_URL}/form-data`;
    const response = await axios.get(url, authHeaders());
    return { projects: response.data.projects ?? [] };
  }

  static async create(payload: TaskCreateUpdatePayload) {
    const response = await axios.post(`${API_URL}/create`, payload, authHeaders());
    return response.data;
  }

  static async getById(id: number) {
    const response = await axios.get(`${API_URL}/${id}`, authHeaders());
    return response.data.data ?? response.data;
  }

  /**
   * Update — essaie PUT /api/tasks/{id}
   * Si ton backend utilise une autre route, change ici :
   * ex: `${API_URL}/update/${id}` ou PATCH
   */
  static async update(id: number, payload: TaskCreateUpdatePayload) {
    try {
      const response = await axios.put(`${API_URL}/${id}`, payload, authHeaders());
      return response.data;
    } catch (e: any) {
      if (e?.response?.status === 404 || e?.response?.status === 405) {
        // Fallback PATCH si PUT non supporté
        const response = await axios.patch(`${API_URL}/${id}`, payload, authHeaders());
        return response.data;
      }
      throw e;
    }
  }

  static async softDelete(id: number) {
    const response = await axios.delete(`${API_URL}/${id}`, authHeaders());
    return response.data;
  }

  static async markAsDone(id: number) {
    const response = await axios.patch(`${API_URL}/${id}/done`, {}, authHeaders());
    return response.data;
  }

  static async getByAssignee(userId: number): Promise<any[]> {
    const response = await axios.get(API_URL, {
      ...authHeaders(),
      params: { assigned_to: userId }
    });
    return extractArray(response.data);
  }

}