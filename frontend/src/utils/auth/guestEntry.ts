/** 游客入口：仅跳转首页，不写入 token、不改动鉴权状态 */

const GUEST_HOME = "/pages/index/index";

/** 跳过登录，进入首页浏览（Tab 页）。清除残留 token 避免触发 401 跳转。 */
export function enterAsGuest() {
  try {
    uni.removeStorageSync("token");
    uni.removeStorageSync("ctx_user_roles");
    uni.removeStorageSync("ctx_preferred_app_side");
  } catch {
    /* ignore */
  }
  uni.switchTab({
    url: GUEST_HOME,
    fail: () => {
      uni.reLaunch({ url: GUEST_HOME });
    },
  });
}
