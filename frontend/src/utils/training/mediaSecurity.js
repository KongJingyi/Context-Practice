/** 浏览器仅在 HTTPS 或 localhost 下允许摄像头/麦克风（Secure Context） */
export function canUseCamera() {
  if (typeof window === "undefined") return false;
  if (window.isSecureContext) return true;
  const host = window.location.hostname;
  return host === "localhost" || host === "127.0.0.1";
}

export function cameraBlockedHint() {
  const host = typeof window !== "undefined" ? window.location.host : "";
  return (
    `当前为 HTTP 访问（${host}），浏览器禁止调用摄像头。` +
    "请改用 https:// 地址访问，或在开发机使用 localhost；Edge/Chrome 可临时将本地址加入「不安全来源视为安全」。"
  );
}
