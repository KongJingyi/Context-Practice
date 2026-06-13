import type { PendingTraining } from "@/types/training/bridge";

const KEY = "ctx_pending_training";

export function setPendingTraining(payload: PendingTraining) {
  uni.setStorageSync(KEY, JSON.stringify(payload));
}

export function consumePendingTraining(): PendingTraining | null {
  try {
    const raw = uni.getStorageSync(KEY);
    if (!raw) return null;
    uni.removeStorageSync(KEY);
    return JSON.parse(raw) as PendingTraining;
  } catch {
    return null;
  }
}

export function peekPendingTraining(): PendingTraining | null {
  try {
    const raw = uni.getStorageSync(KEY);
    if (!raw) return null;
    return JSON.parse(raw) as PendingTraining;
  } catch {
    return null;
  }
}
