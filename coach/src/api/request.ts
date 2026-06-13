import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import { showTopToast } from "@/utils/toast";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "/api",
  timeout: 30000,
  headers: { "Content-Type": "application/json" },
});

// Request interceptor: attach token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token") || "";
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle unified envelope { code, message, data }
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const body = response.data;

    if (isEnvelope(body)) {
      if (body.code === 401) {
        handleUnauthorized();
        return Promise.reject(body);
      }
      if (body.code === 200) {
        return { ...response, data: body.data } as AxiosResponse;
      }
      return Promise.reject(body);
    }

    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      handleUnauthorized();
      return Promise.reject({ code: 401, message: "Unauthorized" });
    }
    showTopToast("网络异常，请检查连接", "error");
    return Promise.reject(error);
  },
);

function isEnvelope(body: unknown): body is { code: number; message?: string; data?: unknown } {
  return (
    body !== null &&
    typeof body === "object" &&
    "code" in body &&
    typeof (body as Record<string, unknown>).code === "number"
  );
}

let unauthorizedHandling = false;

function handleUnauthorized() {
  if (unauthorizedHandling) return;
  unauthorizedHandling = true;
  localStorage.removeItem("token");
  showTopToast("登录已失效，请重新登录", "error");
  const frontendBase = import.meta.env.VITE_FRONTEND_BASE || window.location.origin;
  // 带上 logout=1，清除学员端 localStorage 中的旧 token，避免 5173 ↔ 5175 跳转死循环
  window.location.href = `${frontendBase}/#/pages/auth/login?logout=1`;
}

export interface RequestOptions extends AxiosRequestConfig {
  silent?: boolean;
}

export async function request(options: RequestOptions): Promise<unknown> {
  const { silent, ...axiosOptions } = options;
  try {
    const res = await http(axiosOptions);
    return res.data;
  } catch (err: unknown) {
    const body = err as { code?: number; message?: string };
    const httpOk = false;

    if (body.code === 401) {
      // 401 已在 response 拦截器中统一处理
      throw body;
    }

    if (!silent) {
      const msg =
        body.message ||
        (body.code && body.code >= 500 ? "系统繁忙，请稍后重试" : "请求失败");
      showTopToast(msg, "error");
    }
    throw err;
  }
}
