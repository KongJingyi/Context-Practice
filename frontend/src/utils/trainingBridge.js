/** Tab 页 switchTab 无法带 query，用本地缓存传递「当前训练场景」。 */
const KEY = "ctx_pending_training";

/**
 * @param {{ scenarioId: string; roomTitle: string }} payload
 */
export function setPendingTraining(payload) {
  uni.setStorageSync(KEY, JSON.stringify(payload));
}

/**
 * @returns {{ scenarioId: string; roomTitle: string } | null}
 */
export function consumePendingTraining() {
  try {
    const raw = uni.getStorageSync(KEY);
    if (!raw) return null;
    uni.removeStorageSync(KEY);
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

/**
 * @returns {{ scenarioId: string; roomTitle: string } | null}
 */
export function peekPendingTraining() {
  try {
    const raw = uni.getStorageSync(KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
