import api from "./api";
import type { User } from "@/types/user";

export default {
  async getUsers(): Promise<User[]> {
    const res = await api.get("/auth/users");
    return res.data;
  }
};