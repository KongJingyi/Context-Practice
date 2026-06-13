/**
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
  const data = await request({ url: "/v1/user/security", method: "GET" });
  return { phoneMasked: data?.phoneMasked ?? data?.phone_masked ?? "" };
}

/**
 * @param {{ phone: string }} payload
 */
export async function sendChangePhoneCode(payload) {
  return request({
    url: "/v1/user/security/send-phone-code",
    method: "POST",
    data: payload,
  });
}

/**
 * @param {{ phone: string; code: string }} payload
 */
export async function updatePhone(payload) {
  const data = await request({
    url: "/v1/user/security/update-phone",
    method: "POST",
    data: payload,
  });
  return {
    ok: true,
    phoneMasked: data?.phoneMasked ?? data?.phone_masked ?? "",
  };
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

