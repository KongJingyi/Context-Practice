/**
 * GET  /api/v1/user/favorites?type=experts|scenes
 * DELETE /api/v1/user/favorites/:type/:id
 */
import { request } from "@/api/request.js";
import { showTopToast } from "@/utils/common/topToast";

const MOCK_EXPERTS = [
  { id: "c_101", name: "李子昂", tags: ["面试官", "逻辑犀利"], jobTitle: "前阿里 P9" },
  { id: "c_102", name: "王若溪", tags: ["汇报答辩"], jobTitle: "培训负责人" },
  { id: "c_103", name: "林晚晴", tags: ["压力面试"], jobTitle: "技术总监" },
  { id: "c_104", name: "陈默言", tags: ["群面"], jobTitle: "金牌导师" },
];

const MOCK_SCENES = [
  { id: "s_401", title: "连环追问", icon: "mic", categoryLabel: "压力面试", lastPracticeLabel: "3 天前" },
  { id: "s_302", title: "年终晋升 Q&A", icon: "projector", categoryLabel: "管理汇报", lastPracticeLabel: "1 周前" },
  { id: "s_205", title: "即兴演讲", icon: "spark", categoryLabel: "公众演讲", lastPracticeLabel: "2 周前" },
  { id: "s_108", title: "拒绝加班话术", icon: "conflict", categoryLabel: "冲突处理", lastPracticeLabel: "昨天" },
];

let expertCache = [...MOCK_EXPERTS];
let sceneCache = [...MOCK_SCENES];

/**
 * @param {'experts'|'scenes'} [type]
 */
export async function fetchFavorites(type) {
  try {
    const data = await request({
      url: "/v1/user/favorites",
      method: "GET",
      data: type ? { type } : {},
    });
    if (data?.experts || data?.scenes) {
      if (data.experts) expertCache = data.experts;
      if (data.scenes) sceneCache = data.scenes;
      return {
        experts: expertCache,
        scenes: sceneCache,
      };
    }
  } catch {
    /* mock */
  }
  await delay(600);
  return { experts: [...expertCache], scenes: [...sceneCache] };
}

/**
 * @param {'experts'|'scenes'} type
 * @param {string} id
 */
export async function removeFavorite(type, id) {
  try {
    await request({
      url: `/v1/user/favorites/${type}/${id}`,
      method: "DELETE",
    });
  } catch (err) {
    if (Math.random() > 0.95) {
      showTopToast("取消收藏失败，请稍后重试", "error");
      throw err;
    }
  }
  if (type === "experts") {
    expertCache = expertCache.filter((e) => e.id !== id);
  } else {
    sceneCache = sceneCache.filter((s) => s.id !== id);
  }
  return { ok: true };
}

function delay(ms) {
  return new Promise((r) => setTimeout(r, ms));
}
