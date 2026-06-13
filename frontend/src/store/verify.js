import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { fetchAuthStatus } from "@/api/modules/verify.js";

/** @typedef {import('@/types/auth/verify').AuthVerifyStatus} AuthVerifyStatus */

export const useVerifyStore = defineStore("verify", () => {
  /** @type {import('vue').Ref<AuthVerifyStatus>} */
  const status = ref("unverified");
  const loaded = ref(false);

  const isVerified = computed(() => status.value === "verified");
  const isPending = computed(() => status.value === "pending");
  const needVerify = computed(() => status.value === "unverified" || status.value === "rejected");

  /** @param {AuthVerifyStatus} next */
  function setStatus(next) {
    status.value = next;
    uni.setStorageSync("ctx_auth_verify_status", next);
  }

  async function loadStatus() {
    const res = await fetchAuthStatus();
    status.value = res.status;
    loaded.value = true;
    return res;
  }

  function hydrateFromStorage() {
    const cached = uni.getStorageSync("ctx_auth_verify_status");
    if (cached) status.value = cached;
  }

  return {
    status,
    loaded,
    isVerified,
    isPending,
    needVerify,
    setStatus,
    loadStatus,
    hydrateFromStorage,
  };
});
