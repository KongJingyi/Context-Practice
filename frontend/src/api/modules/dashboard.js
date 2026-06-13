/**
 * 个人主页数据（预留对接后端）
 * GET /user/profile、/user/growth、/user/reports/recent
 */
import { request } from "@/api/request.js";

/**
 * @returns {Promise<import('@/types/dashboard').UserDashboardProfile>}
 */
export async function fetchUserProfile() {
  try {
    const data = await request({ url: "/user/profile", method: "GET" });
    if (data && data.name) {
      return /** @type {import('@/types/dashboard').UserDashboardProfile} */ (data);
    }
  } catch {
    /* mock */
  }
  return {
    name: "张泽华",
    joinDays: 385,
    rankTag: "职场精英",
    level: "Lv.8",
    finishedCourses: 12,
    totalHoursLabel: "32h",
    avatarUrl: "",
  };
}

/**
 * @returns {Promise<import('@/types/dashboard').GrowthChartData>}
 */
const MOCK_GROWTH_BY_PERIOD = {
  week: {
    asOfDate: "2026年04月12日",
    xLabels: ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
    values: [72, 75, 78, 80, 82, 85, 88],
  },
  month: {
    asOfDate: "2026年04月12日",
    xLabels: ["2026.01", "2026.02", "2026.03", "2026.04"],
    values: [65, 72, 81, 90],
  },
  year: {
    asOfDate: "2026年04月12日",
    xLabels: ["2023", "2024", "2025", "2026"],
    values: [58, 68, 79, 88],
  },
};

/**
 * @returns {Promise<import('@/types/dashboard').GrowthChartData>}
 */
export async function fetchGrowthData() {
  return fetchGrowthDataByPeriod("month");
}

/**
 * @param {import('@/types/dashboard').GrowthPeriod} period
 * @returns {Promise<import('@/types/dashboard').GrowthChartData>}
 */
export async function fetchGrowthDataByPeriod(period = "month") {
  try {
    const data = await request({
      url: "/user/growth",
      method: "GET",
      data: { period },
    });
    if (data && Array.isArray(data.values)) {
      return /** @type {import('@/types/dashboard').GrowthChartData} */ (data);
    }
  } catch {
    /* mock */
  }
  return MOCK_GROWTH_BY_PERIOD[period] ?? MOCK_GROWTH_BY_PERIOD.month;
}

/**
 * @returns {Promise<import('@/types/dashboard').AbilityTagItem[]>}
 */
export async function fetchAbilityTags() {
  try {
    const data = await request({ url: "/user/ability-tags", method: "GET" });
    if (Array.isArray(data)) {
      return /** @type {import('@/types/dashboard').AbilityTagItem[]} */ (data);
    }
  } catch {
    /* mock */
  }
  return [
    { text: "逻辑清晰", weight: 0.95 },
    { text: "语速适中", weight: 0.82 },
    { text: "结构完整", weight: 0.78 },
    { text: "眼神交流", weight: 0.65 },
    { text: "语速偏快", weight: 0.55 },
    { text: "共情表达", weight: 0.48 },
    { text: "开场有力", weight: 0.42 },
    { text: "收尾仓促", weight: 0.35 },
  ];
}

/**
 * @returns {Promise<import('@/types/dashboard').CompositeScoreData>}
 */
export async function fetchCompositeScore() {
  try {
    const data = await request({ url: "/user/composite-score", method: "GET" });
    if (data && typeof data.score === "number") {
      return /** @type {import('@/types/dashboard').CompositeScoreData} */ (data);
    }
  } catch {
    /* mock */
  }
  return { score: 88, hint: "基于近 30 次陪练反馈" };
}

/**
 * @returns {Promise<import('@/types/dashboard').RecentReportItem[]>}
 */
export async function fetchRecentReports() {
  try {
    const data = await request({ url: "/user/reports/recent", method: "GET" });
    if (Array.isArray(data)) {
      return /** @type {import('@/types/dashboard').RecentReportItem[]} */ (data);
    }
    if (data && Array.isArray(data.list)) {
      return data.list;
    }
  } catch {
    /* mock */
  }
  return [
    {
      id: "r1",
      title: "互联网大厂PM复试模拟",
      date: "2026-04-12",
      tutor: "李子昂",
      score: 88,
    },
    {
      id: "r2",
      title: "年终晋升评委Q&A实操",
      date: "2026-04-02",
      tutor: "周予安",
      score: 82,
    },
  ];
}

/**
 * @returns {Promise<import('@/types/dashboard').MedalItem[]>}
 */
export async function fetchMedals() {
  return [
    { id: "m1", name: "破局先行者", icon: "shield", tone: "blue", earned: true },
    { id: "m2", name: "逻辑大师", icon: "check", tone: "orange", earned: true },
    { id: "m3", name: "谈判专家", icon: "star", tone: "purple", earned: false },
  ];
}

const GUEST_MOCK_REPORTS = [
  {
    id: "guest-r1",
    title: "互联网大厂PM复试模拟",
    date: "2026-04-12",
    tutor: "李子昂",
    score: 88,
  },
  {
    id: "guest-r2",
    title: "年终晋升评委Q&A实操",
    date: "2026-04-02",
    tutor: "周予安",
    score: 82,
  },
];

/**
 * 游客浏览个人中心（不请求需登录接口）
 * @param {import('@/types/dashboard').GrowthPeriod} [period='month']
 */
export async function fetchGuestDashboardBundle(period = "month") {
  return {
    profile: {
      name: "访客",
      joinDays: 0,
      rankTag: "游客浏览",
      level: "—",
      finishedCourses: 0,
      totalHoursLabel: "0h",
      avatarUrl: "",
    },
    growth: MOCK_GROWTH_BY_PERIOD[period] ?? MOCK_GROWTH_BY_PERIOD.month,
    reports: GUEST_MOCK_REPORTS,
    medals: await fetchMedals(),
    abilityTags: [
      { text: "逻辑清晰", weight: 0.82 },
      { text: "语速适中", weight: 0.7 },
      { text: "结构完整", weight: 0.65 },
      { text: "眼神交流", weight: 0.55 },
      { text: "共情表达", weight: 0.45 },
    ],
    composite: { score: 72, hint: "示例数据 · 登录后同步真实得分" },
  };
}
