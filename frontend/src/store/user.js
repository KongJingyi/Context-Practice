import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getToken, setTokenStorage } from "@/utils/auth/token.js";

const ROLES_KEY = "ctx_user_roles";
const SIDE_KEY = "ctx_preferred_app_side";

/**
 * 用户登录态与 Token（H5 / 微信小程序共用 uni.storage）。
 */
export const useUserStore = defineStore("user", () => {
  const token = ref(getToken());
  const userInfo = ref(null);
  const roles = ref(/** @type {import('@/types/auth').UserRole[]} */ ([]));
  const preferredAppSide = ref(/** @type {import('@/types/auth').AppSide} */ ("user"));

  const isLoggedIn = computed(() => Boolean(token.value));
  const isCoach = computed(() => roles.value.includes("COACH"));
  const isUser = computed(() => roles.value.includes("USER"));

  function setToken(newToken) {
    token.value = newToken || "";
    setTokenStorage(token.value);
  }

  function hydrateFromStorage() {
    token.value = getToken();
    try {
      const raw = uni.getStorageSync(ROLES_KEY);
      if (raw) roles.value = JSON.parse(raw);
    } catch {
      roles.value = [];
    }
    try {
      const side = uni.getStorageSync(SIDE_KEY);
      if (side === "coach" || side === "user" || side === "admin") preferredAppSide.value = side;
    } catch {
      /* ignore */
    }
  }

  function setUserInfo(info) {
    userInfo.value = info;
  }

  /**
   * @param {{ token: string; user?: object; roles?: import('@/types/auth').UserRole[]; appSide?: import('@/types/auth').AppSide }} session
   */
  function setAuthSession(session) {
    setToken(session.token);
    if (session.user) userInfo.value = session.user;
    roles.value = session.roles ?? [];
    if (session.appSide) preferredAppSide.value = session.appSide;
    uni.setStorageSync(ROLES_KEY, JSON.stringify(roles.value));
    uni.setStorageSync(SIDE_KEY, preferredAppSide.value);
  }

  function logout() {
    token.value = "";
    userInfo.value = null;
    roles.value = [];
    preferredAppSide.value = "user";
    setTokenStorage("");
    try {
      uni.removeStorageSync(ROLES_KEY);
      uni.removeStorageSync(SIDE_KEY);
    } catch {
      /* ignore */
    }
  }

  return {
    token,
    userInfo,
    roles,
    preferredAppSide,
    isLoggedIn,
    isCoach,
    isUser,
    setToken,
    hydrateFromStorage,
    setUserInfo,
    setAuthSession,
    logout,
  };
});
