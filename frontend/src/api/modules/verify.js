/**
 * 身份认证
 * POST /api/auth/verify
 * POST /api/auth/verify/upload
 * GET  /api/auth/status
 */
import { request } from "@/api/request.js";

const VERIFY_KEY = "ctx_auth_verify_status";

function getBaseURL() {
  let base = "";
  // #ifdef H5
  base = import.meta.env.VITE_API_BASE || "/api";
  // #endif
  // #ifndef H5
  base = import.meta.env.VITE_API_BASE || "";
  // #endif
  return base;
}

/**
 * @returns {Promise<import('@/types/auth/verify').AuthVerifyStatusResponse>}
 */
export async function fetchAuthStatus() {
  const data = await request({ url: "/auth/status", method: "GET" });
  if (data && data.status) {
    uni.setStorageSync(VERIFY_KEY, data.status);
    return /** @type {import('@/types/auth/verify').AuthVerifyStatusResponse} */ (data);
  }
  return { status: "unverified" };
}

/**
 * @param {import('@/types/auth/verify').AuthVerifyPayload} payload
 */
export async function submitAuth(payload) {
  const data = await request({
    url: "/auth/verify",
    method: "POST",
    data: payload,
  });
  if (data?.status) {
    uni.setStorageSync(VERIFY_KEY, data.status);
  }
  return data;
}

/**
 * 上传证件图到后端
 * @param {string} filePath 本地临时路径
 * @param {"front"|"back"} [side]
 * @param {(p: number) => void} [onProgress]
 * @returns {Promise<{ url: string }>}
 */
export function uploadFile(filePath, onProgress, side) {
  const token = uni.getStorageSync("token") || "";
  return new Promise((resolve, reject) => {
    const task = uni.uploadFile({
      url: `${getBaseURL()}/auth/verify/upload`,
      filePath,
      name: "file",
      formData: side ? { side } : {},
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        let body = res.data;
        if (typeof body === "string") {
          try {
            body = JSON.parse(body);
          } catch {
            reject(new Error("上传响应解析失败"));
            return;
          }
        }
        if (body && body.code === 200 && body.data?.url) {
          resolve({ url: body.data.url });
          return;
        }
        reject(body || new Error(`上传失败 (${res.statusCode})`));
      },
      fail: (err) => reject(err),
    });
    if (task && typeof task.onProgressUpdate === "function") {
      task.onProgressUpdate((event) => {
        onProgress?.(event.progress);
      });
    }
  });
}
