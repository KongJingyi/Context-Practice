/**
 * POST /api/v1/user/security/update-password
 * PATCH /api/v1/user/settings/privacy
 * PATCH /api/v1/user/settings/notifications
 */
import { request } from "@/api/request.js";

const PRIVACY_KEY = "ctx_user_privacy";
const NOTIFY_KEY = "ctx_user_notify";

const DEFAULT_PRIVACY = {
  shareReportWithExpert: true,
  anonymousCommunity: false,
};

const DEFAULT_NOTIFY = {
  smsOnBook: true,
  remindBeforeTraining: true,
  expertFeedbackNotify: true,
};

export async function fetchSecurityInfo() {
  try {
    const data = await request({ url: "/v1/user/security", method: "GET" });
    if (data?.phone_masked) return { phoneMasked: data.phone_masked };
  } catch {
    /* mock */
  }
  return { phoneMasked: "138****5678" };
}

/**
 * @param {{ oldPassword: string; newPassword: string }} payload
 */
export async function updatePassword(payload) {
  try {
    return await request({
      url: "/v1/user/security/update-password",
      method: "POST",
      data: { old_password: payload.oldPassword, new_password: payload.newPassword },
    });
  } catch {
    /* mock */
  }
  if (!payload.oldPassword || payload.newPassword.length < 8) {
    throw new Error("密码格式不符合要求");
  }
  return { ok: true };
}

/**
 * @param {{ phone: string; code: string }} payload
 */
export async function updatePhone(payload) {
  try {
    return await request({
      url: "/v1/user/security/update-phone",
      method: "POST",
      data: payload,
    });
  } catch {
    /* mock */
  }
  return { ok: true, phoneMasked: `${payload.phone.slice(0, 3)}****${payload.phone.slice(-4)}` };
}

export async function fetchPrivacySettings() {
  try {
    const data = await request({ url: "/v1/user/settings/privacy", method: "GET" });
    if (data) return normalizePrivacy(data);
  } catch {
    /* mock */
  }
  try {
    const raw = uni.getStorageSync(PRIVACY_KEY);
    if (raw) return JSON.parse(raw);
  } catch {
    /* ignore */
  }
  return { ...DEFAULT_PRIVACY };
}

/**
 * @param {import('@/types/user/settings').PrivacySettings} settings
 */
export async function patchPrivacySettings(settings) {
  try {
    await request({
      url: "/v1/user/settings/privacy",
      method: "PATCH",
      data: {
        share_report_with_expert: settings.shareReportWithExpert,
        anonymous_community: settings.anonymousCommunity,
      },
    });
  } catch {
    /* mock */
  }
  uni.setStorageSync(PRIVACY_KEY, JSON.stringify(settings));
  return { ok: true };
}

export async function fetchNotificationSettings() {
  try {
    const data = await request({ url: "/v1/user/settings/notifications", method: "GET" });
    if (data) return normalizeNotify(data);
  } catch {
    /* mock */
  }
  try {
    const raw = uni.getStorageSync(NOTIFY_KEY);
    if (raw) return JSON.parse(raw);
  } catch {
    /* ignore */
  }
  return { ...DEFAULT_NOTIFY };
}

/**
 * @param {import('@/types/user/settings').NotificationSettings} settings
 */
export async function patchNotificationSettings(settings) {
  try {
    await request({
      url: "/v1/user/settings/notifications",
      method: "PATCH",
      data: settings,
    });
  } catch {
    /* mock */
  }
  uni.setStorageSync(NOTIFY_KEY, JSON.stringify(settings));
  return { ok: true };
}

function normalizePrivacy(raw) {
  return {
    shareReportWithExpert: Boolean(raw.share_report_with_expert ?? raw.shareReportWithExpert),
    anonymousCommunity: Boolean(raw.anonymous_community ?? raw.anonymousCommunity),
  };
}

function normalizeNotify(raw) {
  return {
    smsOnBook: Boolean(raw.sms_on_book ?? raw.smsOnBook ?? true),
    remindBeforeTraining: Boolean(raw.remind_before_training ?? raw.remindBeforeTraining ?? true),
    expertFeedbackNotify: Boolean(raw.expert_feedback_notify ?? raw.expertFeedbackNotify ?? true),
  };
}

/** @param {string} pwd */
export function passwordStrength(pwd) {
  if (!pwd) return { score: 0, label: "请输入密码" };
  let score = 0;
  if (pwd.length >= 8) score += 1;
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 1;
  if (/\d/.test(pwd)) score += 1;
  if (/[^A-Za-z0-9]/.test(pwd)) score += 1;
  const labels = ["弱", "较弱", "中等", "强", "很强"];
  return { score, label: labels[score] };
}
