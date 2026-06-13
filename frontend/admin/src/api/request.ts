import axios from "axios";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "/api",
  timeout: 30000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token") || "";
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => {
    const body = res.data;
    if (body && typeof body === "object" && "code" in body) {
      if (body.code === 401) {
        localStorage.removeItem("token");
        window.location.href = `${import.meta.env.VITE_FRONTEND_BASE || "http://localhost:5173"}/#/pages/auth/login?logout=1`;
        return Promise.reject(body);
      }
      if (body.code === 403) {
        return Promise.reject(new Error(body.message || "无权限"));
      }
      if (body.code === 200) return { ...res, data: body.data };
    }
    return res;
  },
  (err) => Promise.reject(err),
);

export async function request<T>(config: Parameters<typeof http.request>[0]): Promise<T> {
  const res = await http.request(config);
  return res.data as T;
}
