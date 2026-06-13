/**
 * AI 智能匹配
 * POST /api/match/smart
 * GET  /api/match/recommend
 */
import { request } from "@/api/request.js";
import { getMockCoachById } from "@/api/modules/coach.js";

/**
 * @param {{ sceneId: string; userId?: string; goalIds?: string[] }} payload
 * @returns {Promise<import('@/types/coach/hall').SmartMatchResult>}
 */
export async function fetchSmartMatch(payload) {
  try {
    const data = await request({
      url: "/match/smart",
      method: "POST",
      data: payload,
    });
    if (data && Array.isArray(data.items)) {
      return /** @type {import('@/types/coach/hall').SmartMatchResult} */ (data);
    }
    if (Array.isArray(data)) {
      return { items: data };
    }
  } catch {
    /* mock */
  }

  await new Promise((r) => setTimeout(r, 2800));

  const ids = [`coach-${payload.sceneId}-0`, `coach-${payload.sceneId}-2`, `coach-${payload.sceneId}-5`];
  const reasons = [
    "该专家近期已帮 50+ 学员通过大厂终面",
    "擅长您选中的：压力面试，追问节奏与真实考场一致",
    "匹配您的训练目标：逻辑补位与抗压表达",
  ];
  const tags = ["匹配度 98%", "擅长：压力面试", "匹配度 94%"];

  const items = ids
    .map((id, i) => {
      const coach = getMockCoachById(id);
      if (!coach) return null;
      return {
        coach,
        matchPercent: 98 - i * 2,
        reason: reasons[i],
        highlightTag: tags[i],
      };
    })
    .filter(Boolean);

  return { items };
}

/**
 * @param {string} sceneId
 * @returns {Promise<import('@/types/coach/hall').SmartMatchResult>}
 */
export async function fetchMatchRecommend(sceneId) {
  try {
    const data = await request({
      url: "/match/recommend",
      method: "GET",
      data: { scene_id: sceneId },
    });
    if (data && Array.isArray(data.items)) {
      return data;
    }
  } catch {
    /* fallback */
  }
  return fetchSmartMatch({ sceneId });
}
