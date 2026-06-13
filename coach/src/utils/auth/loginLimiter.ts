const STORAGE_PREFIX = "login_fail_";
const MAX_ATTEMPTS = 5;
const LOCK_MS = 15 * 60 * 1000;

interface LoginLockRecord {
  failCount: number;
  lastFailAt?: number;
  lockedUntil: number;
}

function storageKey(phone: string): string {
  return `${STORAGE_PREFIX}${phone}`;
}

export function getLoginLock(phone: string) {
  if (!phone) {
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  const raw = localStorage.getItem(storageKey(phone));
  if (!raw) {
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  let record: LoginLockRecord;
  try {
    record = JSON.parse(raw);
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
    localStorage.removeItem(storageKey(phone));
    return { failCount: 0, locked: false, remainSeconds: 0 };
  }
  return { failCount: record.failCount || 0, locked: false, remainSeconds: 0 };
}

export function recordLoginFail(phone: string) {
  if (!phone) return getLoginLock(phone);
  const current = getLoginLock(phone);
  if (current.locked) return current;

  const failCount = current.failCount + 1;
  const record: LoginLockRecord = {
    failCount,
    lockedUntil: failCount >= MAX_ATTEMPTS ? Date.now() + LOCK_MS : 0,
  };
  localStorage.setItem(storageKey(phone), JSON.stringify(record));
  return getLoginLock(phone);
}

export function clearLoginFail(phone: string) {
  if (phone) {
    localStorage.removeItem(storageKey(phone));
  }
}

export const LOGIN_LIMIT = { maxAttempts: MAX_ATTEMPTS, lockMinutes: LOCK_MS / 60000 };
