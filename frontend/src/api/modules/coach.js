/**
 * 陪练员 API
 * GET /api/coaches?level_min=&level_max=&tags[]=&stars=&today_only=
 */
import { request } from "@/api/request.js";
import { resolveSceneById } from "@/utils/scene/sceneContext";

const MOCK_NAMES = [
  "林晚晴",
  "周予安",
  "陈默言",
  "苏景行",
  "顾清和",
  "沈知夏",
  "陆星河",
  "唐意远",
  "宋知微",
  "韩修然",
];

const MOCK_TITLES = [
  "前字节技术总监",
  "10 年 HRBP / 面试官",
  "麦肯锡沟通教练",
  "上市公司培训负责人",
  "群面金牌导师",
  "高管演讲顾问",
];

const MOCK_TAGS = [
  ["连环追问", "项目拆解", "STAR 法则"],
  ["压力面", "八大问", "价值观引导"],
  ["群面控场", "观点提炼", "节奏把控"],
  ["复盘答辩", "数据叙事", "结论先行"],
  ["预算争取", "向上管理", "利益对齐"],
  ["风险汇报", "坏消息表达", "方案闭环"],
  ["即兴演讲", "3 分钟结构", "观点收敛"],
  ["电梯演讲", "闪击表达", "卖点提炼"],
  ["抗压训练", "逻辑补位", "即时回击"],
];

const SPECIALTY_POOL = [
  ["pressure", "logic", "star"],
  ["pressure", "star"],
  ["group", "logic"],
  ["report", "logic"],
  ["negotiate", "social"],
  ["report", "pressure"],
  ["speech", "social"],
  ["speech", "logic"],
  ["pressure", "logic"],
];

const LEVEL_POOL = ["gold", "senior", "rookie"];
const LEVEL_NUM_MAP = { gold: 5, senior: 3, rookie: 2 };
const PRICE_BUCKETS = [89, 128, 199, 268, 399, 499, 699];

/**
 * @param {string} sceneId
 * @param {import('@/types/coach/hall').CoachFilterParams} [filterParams]
 * @returns {Promise<import('@/types/coach/hall').Coach[]>}
 */
export async function fetchCoaches(sceneId, filterParams) {
  try {
    const fp = filterParams || {};
    const data = await request({
      url: "/coaches",
      method: "GET",
      data: {
        scene_id: sceneId,
        level_min: fp.levelMin,
        level_max: fp.levelMax,
        stars: fp.minStars || undefined,
        tags: fp.tags,
        today_only: fp.todayOnly || undefined,
      },
    });
    if (Array.isArray(data)) {
      return /** @type {import('@/types/coach/hall').Coach[]} */ (data);
    }
  } catch {
    /* mock */
  }

  return new Promise((resolve) => {
    const delay = 500 + Math.floor(Math.random() * 300);
    setTimeout(() => {
      resolve(generateMockCoaches(sceneId));
    }, delay);
  });
}

/**
 * @param {import('@/types/coach/hall').Coach[]} list
 * @param {import('@/types/coach/hall').CoachFilterParams} fp
 */
export function applyClientFilters(list, fp) {
  let out = [...list];

  out = out.filter((c) => c.levelNum >= fp.levelMin && c.levelNum <= fp.levelMax);

  if (fp.minStars > 0) {
    out = out.filter((c) => c.rating >= fp.minStars);
  }

  if (fp.tags.length) {
    out = out.filter((c) => fp.tags.some((t) => c.specialtyIds.includes(t)));
  }

  if (fp.todayOnly) {
    out = out.filter((c) => c.availableToday);
  }

  if (fp.priceRange === "0-100") {
    out = out.filter((c) => c.price <= 100);
  } else if (fp.priceRange === "100-300") {
    out = out.filter((c) => c.price > 100 && c.price <= 300);
  } else if (fp.priceRange === "300+") {
    out = out.filter((c) => c.price > 300);
  }

  if (fp.sortBy === "activity") {
    out.sort((a, b) => b.activityScore - a.activityScore);
  } else {
    out.sort((a, b) => b.rating - a.rating || b.orderCount - a.orderCount);
  }

  return out;
}

/**
 * @param {string} sceneId
 * @returns {import('@/types/coach/hall').Coach[]}
 */
function generateMockCoaches(sceneId) {
  const ctx = resolveSceneById(sceneId);
  const categoryId = ctx?.category.id ?? "pressure";
  const theme = ctx?.category.theme ?? "rose";
  const seed = hashCode(sceneId);

  return Array.from({ length: 9 }, (_, i) => {
    const idx = (seed + i) % MOCK_NAMES.length;
    const level = LEVEL_POOL[(seed + i * 2) % LEVEL_POOL.length];
    const levelNum = Math.min(5, Math.max(1, LEVEL_NUM_MAP[level] + ((seed + i) % 2)));
    const price = PRICE_BUCKETS[(seed + i * 3) % PRICE_BUCKETS.length];
    const rating = 4.5 + ((seed + i) % 6) * 0.1;
    const orderCount = 120 + ((seed + i * 11) % 880);
    const activityScore = 60 + ((seed + i * 7) % 40);
    const specialtyIds = SPECIALTY_POOL[(seed + i) % SPECIALTY_POOL.length];

    return {
      id: `coach-${sceneId}-${i}`,
      name: MOCK_NAMES[idx],
      jobTitle: MOCK_TITLES[(seed + i) % MOCK_TITLES.length],
      online: (seed + i) % 3 !== 0,
      availableToday: (seed + i) % 4 !== 1,
      rating: Math.round(rating * 10) / 10,
      orderCount,
      skillTags: MOCK_TAGS[(seed + i) % MOCK_TAGS.length],
      specialtyIds,
      price,
      sessionMinutes: ctx?.sub.durationMinutes ?? 45,
      levelNum,
      level,
      activityScore,
      categoryId,
      theme,
      bio: `专注「${ctx?.sub.title ?? "沟通训练"}」实战陪练，累计辅导 ${orderCount}+ 场，擅长在高压对话中帮助学员稳住节奏、补齐逻辑链。`,
      highlights: [
        "大厂 / 名企实战背景",
        "1v1 定制化追问脚本",
        "课后结构化复盘报告",
      ],
    };
  });
}

/**
 * @param {string} coachId
 * @returns {Promise<import('@/types/coach/hall').CoachDetail | null>}
 */
export async function fetchCoachDetail(coachId) {
  try {
    const data = await request({
      url: `/coaches/${coachId}`,
      method: "GET",
    });
    if (data && data.id) {
      return /** @type {import('@/types/coach/hall').CoachDetail} */ (data);
    }
  } catch {
    /* mock */
  }

  const base = getMockCoachById(coachId);
  if (!base) return null;
  return enrichCoachDetail(base);
}

/**
 * @param {import('@/types/coach/hall').Coach} coach
 * @returns {import('@/types/coach/hall').CoachDetail}
 */
export function enrichCoachDetail(coach) {
  const seed = hashCode(coach.id);
  const total = 80 + (seed % 120);

  return {
    ...coach,
    ratingDistribution: [
      { stars: 5, count: Math.round(total * 0.62) },
      { stars: 4, count: Math.round(total * 0.22) },
      { stars: 3, count: Math.round(total * 0.1) },
      { stars: 2, count: Math.round(total * 0.04) },
      { stars: 1, count: Math.round(total * 0.02) },
    ],
    certificates: [
      { id: "c1", title: "国家级心理咨询师", imageUrl: "" },
      { id: "c2", title: "ICF 专业教练认证", imageUrl: "" },
      { id: "c3", title: "大厂面试官资格证", imageUrl: "" },
    ],
    successStories: [
      {
        id: "s1",
        title: "学员斩获字节 Offer",
        subtitle: "3 次陪练后终面通过率提升",
        metric: "Offer +1",
      },
      {
        id: "s2",
        title: "晋升答辩满分通过",
        subtitle: "结构化叙事 + 数据闭环",
        metric: "评分 9.6",
      },
      {
        id: "s3",
        title: "高压群面逆袭",
        subtitle: "从沉默到控场发言",
        metric: "能力 +40%",
      },
    ],
    radar: [
      { key: "pro", label: "专业度", value: 88 + (seed % 10) },
      { key: "aff", label: "亲和力", value: 82 + (seed % 12) },
      { key: "react", label: "反应速度", value: 90 + (seed % 8) },
      { key: "logic", label: "逻辑力", value: 92 + (seed % 6) },
      { key: "fb", label: "反馈深度", value: 86 + (seed % 11) },
    ],
  };
}

function hashCode(str) {
  let h = 0;
  for (let i = 0; i < str.length; i += 1) {
    h = (h << 5) - h + str.charCodeAt(i);
    h |= 0;
  }
  return Math.abs(h);
}

/**
 * @param {string} coachId
 * @returns {import('@/types/coach/hall').Coach | null}
 */
export function getMockCoachById(coachId) {
  const m = String(coachId).match(/^coach-(.+)-(\d+)$/);
  if (!m) return null;
  const sceneId = m[1];
  const index = Number(m[2]);
  const list = generateMockCoaches(sceneId);
  return list[index] ?? null;
}
