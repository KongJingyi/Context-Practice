/**
 * 练习实验室 API（预留对接后端）
 * GET  /api/v1/practice/questions/categories
 * GET  /api/v1/practice/questions?category=xxx
 * POST /api/v1/practice/analyze-voice
 * POST /api/v1/practice/optimize-text
 */
import { request } from "@/api/request.js";

const MOCK_CATEGORIES = [
  {
    id: "interview",
    title: "面试求职",
    subtitle: "自我介绍 · 离职原因 · 薪资谈判",
    icon: "🎯",
    gradient: ["#064e3b", "#10b981"],
    todayCount: 238,
    span: "wide",
  },
  {
    id: "report",
    title: "职场汇报",
    subtitle: "周报 · 项目复盘 · 向上管理",
    icon: "📊",
    gradient: ["#1e3a8a", "#3b82f6"],
    todayCount: 186,
    span: "normal",
  },
  {
    id: "negotiation",
    title: "商务谈判",
    subtitle: "议价 · 合同条款 · 资源争取",
    icon: "🤝",
    gradient: ["#312e81", "#6366f1"],
    todayCount: 142,
    span: "tall",
  },
  {
    id: "speech",
    title: "公众演讲",
    subtitle: "开场白 · 观点阐述 · 收尾",
    icon: "🎤",
    gradient: ["#134e4a", "#14b8a6"],
    todayCount: 167,
    span: "normal",
  },
  {
    id: "social",
    title: "日常社交",
    subtitle: "破冰 · 寒暄 · 饭局应对",
    icon: "💬",
    gradient: ["#0c4a6e", "#0ea5e9"],
    todayCount: 203,
    span: "normal",
  },
  {
    id: "objection",
    title: "异议处理",
    subtitle: "拒绝加班 · 应对质疑 · 冲突化解",
    icon: "⚡",
    gradient: ["#4c1d95", "#8b5cf6"],
    todayCount: 195,
    span: "wide",
  },
];

const MOCK_QUESTIONS = {
  interview: [
    { id: 101, text: "请做一段 3 分钟的自我介绍", category: "面试求职", categoryId: "interview" },
    { id: 102, text: "为什么要离开上一家公司？", category: "面试求职", categoryId: "interview" },
    { id: 103, text: "你的核心竞争力是什么？", category: "面试求职", categoryId: "interview" },
    { id: 104, text: "如何处理与上级意见不合？", category: "面试求职", categoryId: "interview" },
  ],
  report: [
    { id: 201, text: "向领导汇报本季度项目进展（2 分钟）", category: "职场汇报", categoryId: "report" },
    { id: 202, text: "复盘一次失败项目，重点讲改进措施", category: "职场汇报", categoryId: "report" },
    { id: 203, text: "申请增加团队编制，如何陈述理由？", category: "职场汇报", categoryId: "report" },
  ],
  negotiation: [
    { id: 301, text: "客户要求降价 15%，你如何回应？", category: "商务谈判", categoryId: "negotiation" },
    { id: 302, text: "争取跨部门资源支持的话术", category: "商务谈判", categoryId: "negotiation" },
    { id: 303, text: "合同延期交付，如何与客户协商？", category: "商务谈判", categoryId: "negotiation" },
  ],
  speech: [
    { id: 401, text: "部门年会 5 分钟开场致辞", category: "公众演讲", categoryId: "speech" },
    { id: 402, text: "阐述一个创新方案的价值与可行性", category: "公众演讲", categoryId: "speech" },
    { id: 403, text: "总结发言：如何留下深刻印象", category: "公众演讲", categoryId: "speech" },
  ],
  social: [
    { id: 501, text: "行业活动上向陌生人自我介绍", category: "日常社交", categoryId: "social" },
    { id: 502, text: "商务饭局中如何自然接话", category: "日常社交", categoryId: "social" },
    { id: 503, text: "电梯里遇到大老板，说点什么？", category: "日常社交", categoryId: "social" },
  ],
  objection: [
    { id: 601, text: "如何礼貌拒绝周末加班", category: "异议处理", categoryId: "objection" },
    { id: 602, text: "客户质疑方案专业性，如何回应", category: "异议处理", categoryId: "objection" },
    { id: 603, text: "同事甩锅时如何清晰划界", category: "异议处理", categoryId: "objection" },
  ],
};

const MOCK_ANALYSIS_POOL = [
  {
    score: 88,
    radar: { logic: 85, speed: 92, fluency: 80, emotion: 88, vocabulary: 86 },
    suggestions: ["语速稍微偏快，建议每句结尾留 0.5 秒停顿", "结论先行做得不错，继续保持"],
    metrics: { speedWpm: 168, pauseCount: 4, clarity: 87 },
  },
  {
    score: 76,
    radar: { logic: 72, speed: 78, fluency: 70, emotion: 82, vocabulary: 74 },
    suggestions: ["语气词「那个、嗯」出现 6 次，建议替换为短暂停顿", "第二段逻辑跳跃，可加过渡句"],
    metrics: { speedWpm: 142, pauseCount: 9, clarity: 73 },
  },
  {
    score: 91,
    radar: { logic: 90, speed: 88, fluency: 92, emotion: 90, vocabulary: 89 },
    suggestions: ["表达流畅度优秀", "建议在关键数据处加重语气"],
    metrics: { speedWpm: 155, pauseCount: 3, clarity: 92 },
  },
];

function normalizeCategories(raw) {
  return raw.map((c) => ({
    id: c.id,
    title: c.title,
    subtitle: c.subtitle,
    icon: c.icon,
    gradient: Array.isArray(c.gradient) ? c.gradient : ["#1e3a8a", "#3b82f6"],
    todayCount: Number(c.todayCount ?? c.today_count ?? 0) || 0,
    span: c.span || "normal",
  }));
}

/**
 * @returns {Promise<import('@/types/practice').QuestionCategory[]>}
 */
export async function fetchQuestionCategories() {
  try {
    const data = await request({ url: "/v1/practice/questions/categories", method: "GET" });
    if (Array.isArray(data) && data.length) return normalizeCategories(data);
  } catch {
    /* mock */
  }
  return MOCK_CATEGORIES;
}

/**
 * @param {string} categoryId
 * @returns {Promise<import('@/types/practice').PracticeQuestion[]>}
 */
export async function fetchQuestions(categoryId) {
  try {
    const data = await request({
      url: `/v1/practice/questions?category=${encodeURIComponent(categoryId)}`,
      method: "GET",
    });
    if (Array.isArray(data) && data.length) return data;
  } catch {
    /* mock */
  }
  return MOCK_QUESTIONS[categoryId] || [];
}

/**
 * @param {{ durationSec: number; blobSize?: number }} payload
 * @returns {Promise<import('@/types/practice').VoiceAnalysisResult>}
 */
export async function analyzeVoice(payload) {
  try {
    const data = await request({
      url: "/v1/practice/analyze-voice",
      method: "POST",
      data: payload,
    });
    if (data && typeof data.score === "number") return data;
  } catch {
    /* mock */
  }
  const base = MOCK_ANALYSIS_POOL[Math.floor(Math.random() * MOCK_ANALYSIS_POOL.length)];
  const durationFactor = Math.min(1, (payload.durationSec || 30) / 45);
  const score = Math.round(base.score * (0.85 + durationFactor * 0.15));
  return {
    ...base,
    score,
    metrics: {
      ...base.metrics,
      speedWpm: Math.round(base.metrics.speedWpm + (Math.random() - 0.5) * 20),
      pauseCount: Math.max(1, base.metrics.pauseCount + Math.floor(Math.random() * 3) - 1),
    },
  };
}

/**
 * @param {{ text: string }} payload
 * @returns {Promise<import('@/types/practice').TextOptimizeResult>}
 */
export async function optimizeText(payload) {
  try {
    const data = await request({
      url: "/v1/practice/optimize-text",
      method: "POST",
      data: payload,
    });
    if (data && data.optimized) return data;
  } catch {
    /* mock */
  }
  const original = payload.text.trim();
  const optimized = original
    .replace(/我觉得/g, "我认为")
    .replace(/那个/g, "")
    .replace(/挺好的/g, "具有显著价值")
    .replace(/因为/g, "核心原因在于")
    .replace(/^/, "【结论先行】");
  return {
    original,
    optimized: optimized || "请先输入需要优化的文稿内容。",
    changes: [
      { offset: 0, length: 6, reason: "增加结论先行结构" },
      { offset: 0, length: 3, reason: "口语化改为职场化" },
    ],
  };
}
