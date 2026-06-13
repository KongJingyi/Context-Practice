import { request } from "@/api/request.js";
import { loginByUsername } from "@/api/modules/videoConference.js";

/**
 * 认证相关接口（与后端约定，当前无后端时由页面做降级/mock）。
 * 文档 P0：POST /api/auth/login { username }
 *
 * 预期响应示例：
 * - captcha: { captchaKey: string, captchaImage: string } // base64 或 url
 * - sms: { expireIn: number }
 * - login/register: { token: string, user?: object }
 * - oauth: { authUrl: string } 或 { token, user }
 * - lockStatus: { locked: boolean, remainSeconds?: number, failCount?: number }
 */

/** GET /v1/auth/captcha */
export function fetchCaptcha() {
  return request({
    url: "/v1/auth/captcha",
    method: "GET",
  });
}

/** POST /v1/auth/sms/send */
export function sendSmsCode(data, opts) {
  return request(
    {
      url: "/v1/auth/sms/send",
      method: "POST",
      data,
    },
    opts,
  );
}

/** POST /v1/auth/login/phone */
export function loginByPhone(data, opts) {
  return request(
    {
      url: "/v1/auth/login/phone",
      method: "POST",
      data,
    },
    opts,
  );
}

/** POST /v1/auth/register/phone */
export function registerByPhone(data, opts) {
  return request(
    {
      url: "/v1/auth/register/phone",
      method: "POST",
      data,
    },
    opts,
  );
}

/** GET /v1/auth/oauth/:provider  H5 可跳转 authUrl；小程序走各自 SDK */
export function getOAuthAuthorizeUrl(provider) {
  return request({
    url: `/v1/auth/oauth/${provider}`,
    method: "GET",
  });
}

/** POST /v1/auth/oauth/:provider/callback */
export function oauthCallback(provider, data) {
  return request({
    url: `/v1/auth/oauth/${provider}/callback`,
    method: "POST",
    data,
  });
}

/** GET /v1/auth/login/lock-status?phone= */
export function fetchLoginLockStatus(phone) {
  return request({
    url: `/v1/auth/login/lock-status?phone=${encodeURIComponent(phone)}`,
    method: "GET",
  });
}

/** POST /api/auth/login — 文档 MVP 登录（见 videoConference.loginByUsername） */
export { loginByUsername };

/**
 * 登录并写入 token（供联调页直接调用）
 * @param {string} username
 */
export async function loginAndStoreToken(username) {
  const res = await loginByUsername(username);
  if (res?.token) {
    uni.setStorageSync("token", res.token);
  }
  return res;
}
