import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

import DashboardTask from "../views/DashboardTask.vue";
import AddTask from "../views/AddTask.vue";
import UpdateTask from "../views/UpdateTask.vue";
import TaskAll from "../views/ViewTask/TaskAll.vue";

const routes: Array<RouteRecordRaw> = [
  { path: "/", redirect: "/dashboardtask" },

  { path: "/dashboardtask", component: DashboardTask },

  { path: "/addtask", component: AddTask },

  {
    path: "/tasks/edit/:id",
    name: "EditTask",
    component: UpdateTask
  },

  // Nouvelle route pour DeleteTask
  
  {
    path: "/TaskAll",
    component: () => import("../views/ViewTask/TaskAll.vue")
  }
];

export const router = createRouter({
  history: createWebHistory(),
  routes
});