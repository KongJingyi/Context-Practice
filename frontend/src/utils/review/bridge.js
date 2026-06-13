const KEY = "ctx_pending_review";

/**
 * @param {import('@/types/review').ReviewContext} payload
 */
export function setPendingReview(payload) {
  uni.setStorageSync(KEY, JSON.stringify(payload));
}

/**
 * @returns {import('@/types/review').ReviewContext | null}
 */
export function consumePendingReview() {
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
 * @returns {import('@/types/review').ReviewContext | null}
 */
export function peekPendingReview() {
  try {
    const raw = uni.getStorageSync(KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
