import axios, { type AxiosRequestConfig } from "axios";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "/api",
  timeout: 30000,
  headers: { "Content-Type": "application/json" },
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
        window.location.href = `${import.meta.env.VITE_FRONTEND_BASE || "http://localhost:5173"}/#/pages/auth/login?logout=1&side=admin`;
        return Promise.reject(body);
      }
      if (body.code === 403) {
        return Promise.reject(new Error(body.message || "无权限"));
      }
      if (body.code === 200) return { ...res, data: body.data };
      return Promise.reject(new Error(body.message || "请求失败"));
    }
    return res;
  },
  (err) => Promise.reject(err),
);

type RequestOptions = Omit<AxiosRequestConfig, "url">;

export async function request<T>(url: string, config?: RequestOptions): Promise<T>;
export async function request<T>(config: AxiosRequestConfig): Promise<T>;
export async function request<T>(
  urlOrConfig: string | AxiosRequestConfig,
  config?: RequestOptions,
): Promise<T> {
  const axiosConfig: AxiosRequestConfig =
    typeof urlOrConfig === "string" ? { ...config, url: urlOrConfig } : urlOrConfig;
  const res = await http.request(axiosConfig);
  return res.data as T;
}
