import api from "./api";
import type { Project } from "@/types/project";

export default {
  async getAllProjects(): Promise<Project[]> {
    const res = await api.get("/projects");
    return res.data;
  }
};