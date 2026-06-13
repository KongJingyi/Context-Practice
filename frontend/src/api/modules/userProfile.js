/**
 * 用户资料
 * PUT /api/user/update
 */
import { request } from "@/api/request.js";

const PROFILE_CACHE = "ctx_user_profile_edit";

/**
 * @returns {Promise<import('@/types/user/profile').UserProfileEditable>}
 */
export async function fetchEditableProfile() {
  try {
    const data = await request({ url: "/user/profile/edit", method: "GET" });
    if (data && data.nickname != null) {
      return /** @type {import('@/types/user/profile').UserProfileEditable} */ (data);
    }
  } catch {
    /* mock */
  }
  const cached = uni.getStorageSync(PROFILE_CACHE);
  if (cached) {
    try {
      return JSON.parse(cached);
    } catch {
      /* ignore */
    }
  }
  return {
    nickname: "张泽华",
    avatarUrl: "",
    trainingGoalIds: ["interview", "pressure"],
  };
}

/**
 * @param {import('@/types/user/profile').UserProfileEditable} data
 */
export async function updateUserInfo(data) {
  try {
    const res = await request({
      url: "/user/update",
      method: "PUT",
      data,
    });
    if (res) {
      uni.setStorageSync(PROFILE_CACHE, JSON.stringify(data));
      return res;
    }
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 400));
  uni.setStorageSync(PROFILE_CACHE, JSON.stringify(data));
  return { ok: true };
}
