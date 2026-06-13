/**
 * 灵感广场 API
 * GET  /api/v1/posts?type=hot
 * POST /api/v1/posts/:id/like
 * POST /api/v1/posts/:id/comment
 */
import { request } from "@/api/request.js";

const MOCK_POSTS = [
  {
    id: "p_001",
    type: "insight",
    user: { name: "张泽华", medal: "逻辑大师" },
    title: "我是如何克服面试紧张的",
    content:
      "今天在对练中学会了如何处理「面试被打断」，关键在于心态：先停顿 0.5 秒，用一句话确认对方问题，再拉回主线。专家说我第二次明显稳住了。",
    tags: ["#面试技巧", "#心态建设"],
    stats: { likes: 128, comments: 45, collects: 32 },
    publishedAt: "2 小时前",
  },
  {
    id: "p_002",
    type: "highlight",
    user: { name: "林小雨", medal: "表达新星" },
    title: "15s 高光：结论先行的开场",
    content: "截取本次训练最精彩的一段表达，开门见山直接亮观点。",
    tags: ["#高光片段", "#公众演讲"],
    stats: { likes: 256, comments: 18, collects: 89 },
    hasVideo: true,
    videoPreview: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
    publishedAt: "5 小时前",
  },
  {
    id: "p_003",
    type: "interview",
    user: { name: "陈航", medal: "面经达人" },
    title: "字节跳动 · 产品经理二面复盘",
    content: "Q1: 介绍一个失败项目及复盘\nQ2: 如何协调研发与设计的优先级\nQ3: 如果数据与直觉冲突怎么办",
    tags: ["#字节跳动", "#产品经理", "#面经"],
    stats: { likes: 412, comments: 96, collects: 210 },
    company: "字节跳动",
    role: "产品经理",
    publishedAt: "昨天",
  },
  {
    id: "p_004",
    type: "insight",
    user: { name: "王若溪", medal: "职场精英" },
    content: "汇报答辩场景练了 3 次之后，终于能在 2 分钟内讲清 ROI。建议先写「一句话结论」再展开论据。",
    tags: ["#职场汇报"],
    stats: { likes: 89, comments: 12, collects: 44 },
    publishedAt: "2 天前",
  },
  {
    id: "p_005",
    type: "highlight",
    user: { name: "周予安", medal: "谈判专家" },
    title: "谈判场景高光：不卑不亢的拒绝",
    content: "15 秒内完成「理解诉求 → 明确边界 → 给出替代方案」三步走。",
    tags: ["#商务谈判", "#高光片段"],
    stats: { likes: 178, comments: 31, collects: 67 },
    hasVideo: true,
    videoPreview: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
    publishedAt: "3 天前",
  },
  {
    id: "p_006",
    type: "interview",
    user: { name: "苏景行", medal: "逻辑大师" },
    title: "腾讯 · 技术总监终面经验",
    content: "终面更看重「技术决策的逻辑链」而非细节背诵。记得用 STAR，但 S 不要超过 30 秒。",
    tags: ["#腾讯", "#技术管理"],
    stats: { likes: 302, comments: 58, collects: 145 },
    company: "腾讯",
    role: "技术总监",
    publishedAt: "4 天前",
  },
];

const MOCK_COMMENTS = {
  p_001: [
    { id: "c1", userName: "匿名学员", content: "非常受用，感谢分享！", createdAt: "1 小时前" },
    { id: "c2", userName: "李子昂导师", content: "心态稳住了，结构自然会跟上，继续练。", createdAt: "30 分钟前" },
  ],
  p_002: [{ id: "c3", userName: "练习搭子", content: "开场这句可以直接当模板。", createdAt: "2 小时前" }],
};

const MOCK_EXPERTS = [
  { id: "e1", name: "李子昂", title: "前阿里 P9", tip: "被打断时，先复述问题再回答，能争取 3 秒思考时间。" },
  { id: "e2", name: "王若溪", title: "培训负责人", tip: "汇报场景：结论 → 数据 → 风险 → 下一步，四步不会错。" },
  { id: "e3", name: "林晚晴", title: "技术总监", tip: "技术面试中，「我不知道」+ 学习路径 比 硬编 加分。" },
  { id: "e4", name: "陈默言", title: "群面导师", tip: "谈判前先写 BATNA（最佳替代方案），底气来自准备。" },
];

const MOCK_FOOTPRINT = [
  { id: "f1", date: "2026-05-18", title: "分享面试心得", type: "insight", summary: "克服紧张的三步法" },
  { id: "f2", date: "2026-05-10", title: "上传表达高光", type: "highlight", summary: "15s 结论先行片段" },
  { id: "f3", date: "2026-04-28", title: "发布面经复盘", type: "interview", summary: "字节 PM 二面 Q&A" },
  { id: "f4", date: "2026-04-12", title: "第一次录音自测", type: "insight", summary: "开启语境智练旅程" },
];

/**
 * @param {{ type?: string }} params
 */
export async function fetchPosts(params = {}) {
  try {
    const data = await request({
      url: "/v1/posts",
      method: "GET",
      params,
    });
    if (Array.isArray(data) && data.length) return normalizePosts(data);
  } catch {
    /* mock */
  }
  let list = [...MOCK_POSTS];
  if (params.type === "highlight") list = list.filter((p) => p.type === "highlight");
  if (params.type === "latest") list = [...list].reverse();
  return list;
}

function normalizePosts(raw) {
  return raw.map((p) => ({
    id: p.id,
    type: p.type || "insight",
    user: p.user,
    title: p.title,
    content: p.content,
    tags: p.tags || [],
    stats: p.stats,
    hasVideo: p.has_video,
    videoPreview: p.video_preview,
    publishedAt: p.published_at || "刚刚",
    liked: false,
    collected: false,
  }));
}

/**
 * @param {string} postId
 */
export async function fetchPostComments(postId) {
  try {
    const data = await request({ url: `/v1/posts/${postId}/comments`, method: "GET" });
    if (Array.isArray(data)) return data;
  } catch {
    /* mock */
  }
  return MOCK_COMMENTS[postId] || [];
}

/**
 * @param {string} postId
 * @param {{ content: string; parent_id?: string | null }} payload
 */
export async function commentPost(postId, payload) {
  try {
    return await request({
      url: `/v1/posts/${postId}/comment`,
      method: "POST",
      data: payload,
    });
  } catch {
    /* mock */
  }
  return {
    id: `c-${Date.now()}`,
    userName: "我",
    content: payload.content,
    createdAt: "刚刚",
  };
}

/**
 * @param {string} postId
 * @param {boolean} liked
 */
export async function likePost(postId, liked) {
  try {
    return await request({ url: `/v1/posts/${postId}/like`, method: "POST", data: { liked } });
  } catch {
    /* mock */
  }
  return { ok: true, liked };
}

/**
 * @returns {Promise<import('@/types/community').ExpertTip[]>}
 */
export async function fetchExpertTips() {
  try {
    const data = await request({ url: "/v1/community/expert-tips", method: "GET" });
    if (Array.isArray(data)) return data;
  } catch {
    /* mock */
  }
  return MOCK_EXPERTS;
}

/** @returns {Promise<import('@/types/community').FootprintItem[]>} */
export async function fetchMyFootprint() {
  try {
    const data = await request({ url: "/v1/community/my-footprint", method: "GET" });
    if (Array.isArray(data)) return data;
  } catch {
    /* mock */
  }
  return MOCK_FOOTPRINT;
}

export const MENTION_USERS = [
  { id: "u1", name: "李子昂", role: "陪练专家" },
  { id: "u2", name: "王若溪", role: "陪练专家" },
  { id: "u3", name: "练习搭子小明", role: "好友" },
];

export const HOT_TAGS = [
  "#压力面试",
  "#产品经理",
  "#年终汇报",
  "#商务谈判",
  "#公众演讲",
  "#面试技巧",
];
