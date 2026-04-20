import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getToken, setTokenStorage } from "@/utils/auth.js";

/**
 * 用户登录态与 Token（H5 / 微信小程序共用 uni.storage）。
 */
export const useUserStore = defineStore("user", () => {
  const token = ref(getToken());
  const userInfo = ref(null);

  const isLoggedIn = computed(() => Boolean(token.value));

  function setToken(newToken) {
    token.value = newToken || "";
    setTokenStorage(token.value);
  }

  function hydrateFromStorage() {
    token.value = getToken();
  }

  function setUserInfo(info) {
    userInfo.value = info;
  }

  function logout() {
    token.value = "";
    userInfo.value = null;
    setTokenStorage("");
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    hydrateFromStorage,
    setUserInfo,
    logout,
  };
});
