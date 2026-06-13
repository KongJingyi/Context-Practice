import { showTopToast } from "@/utils/common/topToast";

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
 * 是否为文档约定的统一响应 { code, message, data }
 * @param {unknown} body
 */
function isEnvelope(body) {
  return (
    body !== null &&
    typeof body === "object" &&
    "code" in body &&
    typeof /** @type {{ code: unknown }} */ (body).code === "number"
  );
}

function handleUnauthorized() {
  const hadToken = Boolean(uni.getStorageSync("token") || "");
  try {
    uni.removeStorageSync("token");
  } catch {
    /* ignore */
  }
  // 游客模式（无 token）不跳登录页
  if (!hadToken) return;
  showTopToast("登录已失效，请重新登录", "error");
  const pages = getCurrentPages();
  const cur = pages[pages.length - 1];
  const route = cur?.route || "";
  if (!route.includes("auth/login")) {
    uni.navigateTo({ url: "/pages/auth/login" });
  }
}

/**
 * @param {UniApp.RequestOptions} options
 * @param {{ silent?: boolean }} [opts] silent=true 时不自动 Toast 业务错误
 * @returns {Promise<unknown>}
 */
export function request(options, opts = {}) {
  const token = uni.getStorageSync("token") || "";
  const header = {
    "Content-Type": "application/json",
    ...(options.header || {}),
  };
  if (token) {
    header.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url: `${getBaseURL()}${options.url}`,
      header,
      success: (res) => {
        const body = res.data;
        const httpOk = res.statusCode >= 200 && res.statusCode < 300;

        if (res.statusCode === 401 || (isEnvelope(body) && body.code === 401)) {
          handleUnauthorized();
          reject(isEnvelope(body) ? body : { code: 401, message: "Unauthorized" });
          return;
        }

        if (!httpOk) {
          const msg =
            (isEnvelope(body) && body.message) ||
            `请求失败 (${res.statusCode})`;
          if (!opts.silent) showTopToast(msg, "error");
          reject(isEnvelope(body) ? body : res);
          return;
        }

        if (isEnvelope(body)) {
          if (body.code === 200) {
            resolve(body.data);
            return;
          }
          const msg = body.message || "操作失败";
          if (!opts.silent) {
            if (body.code >= 500) {
              showTopToast("系统繁忙，请稍后重试", "error");
            } else {
              showTopToast(msg, "error");
            }
          }
          reject(body);
          return;
        }

        resolve(body);
      },
      fail: (err) => {
        if (!opts.silent) showTopToast("网络异常，请检查连接", "error");
        reject(err);
      },
    });
  });
}
