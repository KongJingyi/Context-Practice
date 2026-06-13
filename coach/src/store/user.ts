import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getToken, setTokenStorage } from "@/utils/auth/token";
import type { AppSide, UserRole, AuthUserInfo } from "@/types/auth";

const ROLES_KEY = "ctx_user_roles";
const SIDE_KEY = "ctx_preferred_app_side";

/**
 * 用户登录态与 Token（PC Web，localStorage）。
 * 与主前端 ContextPractice-frontend 的 store/user.js 保持结构一致。
 */
export const useUserStore = defineStore("user", () => {
  const token = ref(getToken());
  const userInfo = ref<AuthUserInfo | null>(null);
  const roles = ref<UserRole[]>([]);
  const preferredAppSide = ref<AppSide>("coach");

  const isLoggedIn = computed(() => Boolean(token.value));
  const isCoach = computed(() => roles.value.includes("COACH"));
  const isUser = computed(() => roles.value.includes("USER"));

  function setToken(newToken: string) {
    token.value = newToken || "";
    setTokenStorage(token.value);
  }

  function hydrateFromStorage() {
    token.value = getToken();
    try {
      const raw = localStorage.getItem(ROLES_KEY);
      if (raw) roles.value = JSON.parse(raw);
    } catch {
      roles.value = [];
    }
    try {
      const side = localStorage.getItem(SIDE_KEY);
      if (side === "coach" || side === "user") preferredAppSide.value = side;
    } catch {
      /* ignore */
    }
  }

  function setUserInfo(info: AuthUserInfo) {
    userInfo.value = info;
  }

  /**
   * 登录成功后保存完整 session，与主前端 login.vue 的 enterHomeAfterAuth 对齐。
   */
  function setAuthSession(session: {
    token: string;
    user?: AuthUserInfo;
    roles?: UserRole[];
    appSide?: AppSide;
  }) {
    setToken(session.token);
    if (session.user) userInfo.value = session.user;
    roles.value = session.roles ?? [];
    if (session.appSide) preferredAppSide.value = session.appSide;
    localStorage.setItem(ROLES_KEY, JSON.stringify(roles.value));
    localStorage.setItem(SIDE_KEY, preferredAppSide.value);
  }

  function logout() {
    token.value = "";
    userInfo.value = null;
    roles.value = [];
    preferredAppSide.value = "coach";
    setTokenStorage("");
    try {
      localStorage.removeItem(ROLES_KEY);
      localStorage.removeItem(SIDE_KEY);
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
