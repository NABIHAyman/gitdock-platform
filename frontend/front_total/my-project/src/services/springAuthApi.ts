import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8081/api/auth",
  headers: {
    "Content-Type": "application/json",
  },
});

// LOGIN
export const loginUser = async (email: string, password: string) => {
  const response = await api.post("/authenticate", {
    email,
    password,
  });
  return response.data;
};

// REGISTER
export const registerUser = async (data: any) => {
  const response = await api.post("/register", data);
  return response.data;
};

// RESET PASSWORD CONFIRM ✅ (AJOUTÉ)
export const confirmResetPassword = async (token: string, newPassword: string) => {
  const response = await api.post("/reset-password/confirm", {
    token,
    newPassword,
  });
  return response.data;
};

export default api;