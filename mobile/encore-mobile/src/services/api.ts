import axios from "axios";
import * as SecureStore from "expo-secure-store";

const API_BASE_URL = "http://192.168.1.216:8080";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Automatically attach token to every request
api.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const tokenStorage = {
  save: (token: string) => SecureStore.setItemAsync("token", token),
  get: () => SecureStore.getItemAsync("token"),
  delete: () => SecureStore.deleteItemAsync("token"),
};

export const authApi = {
  register: (name: string, email: string, password: string) =>
    api.post("/api/auth/register", { name, email, password }),

  login: (email: string, password: string) =>
    api.post("/api/auth/login", { email, password }),
};

export default api;
