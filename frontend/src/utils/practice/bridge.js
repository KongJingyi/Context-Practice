/** 练习实验室入口参数（Tab 页 switchTab 无法带 query） */
const KEY = "ctx_practice_entry";

/**
 * @param {import('@/types/practice').PracticeEntryPayload} payload
 */
export function setPracticeEntry(payload) {
  uni.setStorageSync(KEY, JSON.stringify(payload));
}

/**
 * @returns {import('@/types/practice').PracticeEntryPayload | null}
 */
export function consumePracticeEntry() {
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
 * @returns {import('@/types/practice').PracticeEntryPayload | null}
 */
export function peekPracticeEntry() {
  try {
    const raw = uni.getStorageSync(KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
