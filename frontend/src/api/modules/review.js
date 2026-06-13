/**
 * POST /api/v1/reviews/submit
 */
import { request } from "@/api/request.js";

const POSITIVE_TAGS = ["逻辑犀利", "氛围感强", "非常专业", "耐心细致", "问题精准", "收获很大"];
const IMPROVE_TAGS = ["节奏偏快", "可更具体", "建议更结构化"];

/**
 * @returns {Promise<{ positive: string[]; improve: string[] }>}
 */
export function getReviewTags() {
  return Promise.resolve({ positive: POSITIVE_TAGS, improve: IMPROVE_TAGS });
}

/**
 * @param {import('@/types/review').ReviewSubmitPayload} payload
 */
export async function submitReview(payload) {
  const data = await request({
    url: "/v1/reviews/submit",
    method: "POST",
    data: {
      order_id: payload.orderId,
      expert_id: payload.expertId,
      scores: {
        professional: payload.scores.professional,
        attitude: payload.scores.attitude,
        quality: payload.scores.quality,
      },
      tags: payload.tags,
      content: payload.content,
      is_anonymous: payload.isAnonymous,
    },
  });
  return data ?? { ok: true };
}

/**
 * @param {string} coachId
 * @returns {Promise<import('@/types/review').CoachReviewSnippet[]>}
 */
export async function fetchCoachReviews(coachId) {
  try {
    const data = await request({
      url: `/v1/coaches/${coachId}/reviews`,
      method: "GET",
    });
    if (Array.isArray(data)) return data;
  } catch {
    /* mock */
  }
  return [
    {
      id: "rv1",
      userName: "学员 A**",
      content: "老师的问题非常精准，帮我找到了之前面试总被刷掉的原因。",
      tags: ["逻辑清晰", "非常专业"],
      score: 5,
      date: "2026-04-10",
    },
    {
      id: "rv2",
      userName: "匿名学员",
      content: "氛围很好，不会让人紧张，反馈也很具体。",
      tags: ["氛围感强", "耐心细致"],
      score: 5,
      date: "2026-04-02",
    },
  ];
}
