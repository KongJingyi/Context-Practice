const STORAGE_PREFIX = "login_fail_";
const MAX_ATTEMPTS = 5;
const LOCK_MS = 15 * 60 * 1000;

function storageKey(phone) {
  return `${STORAGE_PREFIX}${phone}`;
}

/**
 * @returns {{ failCount: number, locked: boolean, remainSeconds: number }}
 */
export function getLoginLock(phone) {
  if (!phone) {
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  const raw = uni.getStorageSync(storageKey(phone));
  if (!raw) {
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  let record;
  try {
    record = typeof raw === "string" ? JSON.parse(raw) : raw;
  } catch {
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  const now = Date.now();
  if (record.lockedUntil && record.lockedUntil > now) {
    return {
      failCount: record.failCount || MAX_ATTEMPTS,
      locked: true,
      remainSeconds: Math.ceil((record.lockedUntil - now) / 1000),
    };
  }
  if (record.lockedUntil && record.lockedUntil <= now) {
    uni.removeStorageSync(storageKey(phone));
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  return {
    failCount: record.failCount || 0,
    locked: false,
    remainSeconds: 0,
  };
}

export function recordLoginFail(phone) {
  if (!phone) return getLoginLock(phone);
  const current = getLoginLock(phone);
  if (current.locked) return current;

  const failCount = current.failCount + 1;
  const record = {
    failCount,
    lastFailAt: Date.now(),
    lockedUntil: failCount >= MAX_ATTEMPTS ? Date.now() + LOCK_MS : 0,
  };
  uni.setStorageSync(storageKey(phone), JSON.stringify(record));
  return getLoginLock(phone);
}

export function clearLoginFail(phone) {
  if (phone) {
    uni.removeStorageSync(storageKey(phone));
  }
}

export const LOGIN_LIMIT = {
  maxAttempts: MAX_ATTEMPTS,
  lockMinutes: LOCK_MS / 60000,
};
