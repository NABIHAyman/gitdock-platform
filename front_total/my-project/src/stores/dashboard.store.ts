import { defineStore } from "pinia";
import type { User } from "@/types/user";
import type { Task } from "@/types/task";
import type { Project } from "@/types/project";

export const useDashboardStore = defineStore("dashboard", {
  state: () => ({
    users: [] as User[],
    tasks: [] as Task[],
    projects: [] as Project[],
  }),

  getters: {
    totalUsers: (state) => state.users.length,
    totalTasks: (state) => state.tasks.length,
    totalProjects: (state) => state.projects.length,
  },
});