import api from "./api";
import type { Task } from "@/types/task";

export default {
  async getAllTasks(): Promise<Task[]> {
    const res = await api.get("/tasks");
    return res.data;
  }
};