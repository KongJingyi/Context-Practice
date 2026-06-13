/**
 * POST /api/v1/complaints/open
 * GET  /api/v1/complaints/:id/status
 */
import { request } from "@/api/request.js";

export const COMPLAINT_TYPES = [
  { id: "attitude_issue", label: "态度恶劣" },
  { id: "late_no_show", label: "未准时上线" },
  { id: "perfunctory", label: "敷衍了事" },
  { id: "privacy_violation", label: "隐私违规" },
  { id: "other", label: "其他" },
];

/**
 * @param {import('@/types/review').ComplaintSubmitPayload} payload
 */
export async function openComplaint(payload) {
  try {
    const data = await request({
      url: "/v1/complaints/open",
      method: "POST",
      data: {
        order_id: payload.orderId,
        type: payload.type,
        description: payload.description,
        attachments: payload.attachments,
        is_anonymous: payload.isAnonymous,
      },
    });
    if (data && data.id) return data;
  } catch {
    /* mock */
  }
  const id = `cmp-${Date.now()}`;
  uni.setStorageSync(`ctx_complaint_${id}`, JSON.stringify(buildMockStatus()));
  return { id, status: "pending" };
}

function buildMockStatus() {
  return {
    steps: [
      { title: "投诉已提交", time: "2026-04-12 18:00", done: true },
      { title: "平台受理", time: "2026-04-12 18:30", done: true },
      { title: "调查取证", time: "2026-04-13 10:00", done: true, current: true },
      {
        title: "处理结果",
        content: "平台正在核实，预计 1-3 个工作日内反馈。",
        done: false,
      },
    ],
  };
}

/**
 * @param {string} complaintId
 * @returns {Promise<{ steps: import('@/types/review').ComplaintStep[] }>}
 */
export async function fetchComplaintStatus(complaintId) {
  try {
    const data = await request({
      url: `/v1/complaints/${complaintId}/status`,
      method: "GET",
    });
    if (data && data.steps) return data;
  } catch {
    /* mock */
  }
  try {
    const raw = uni.getStorageSync(`ctx_complaint_${complaintId}`);
    if (raw) return JSON.parse(raw);
  } catch {
    /* ignore */
  }
  return buildMockStatus();
}
