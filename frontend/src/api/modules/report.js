/**
 * 训练反馈报告 API
 * POST /api/v1/training/report  — 文档主路径（trainingRecordId）
 * GET  /api/v1/reports/:order_id — 扩展/旧约定
 */
import { request } from "@/api/request.js";
import { fetchTrainingReport } from "@/api/modules/videoConference.js";
import { scoreEncouragement } from "@/utils/report/time";

const DIMENSION_LABELS = ["逻辑力", "感染力", "抗压性", "仪态", "深度"];

const REPORT_MAP = {
  "ord-h1": buildReport({
    id: "ord-h1",
    orderNo: "EF20260412001",
    scene: "管理汇报",
    date: "2026-04-12",
    expertName: "王若溪",
    expertTitle: "上市公司培训负责人",
    total: 88,
    session: [85, 82, 78, 90, 86],
    average: [72, 70, 68, 75, 71],
    initial: [62, 58, 55, 65, 60],
    current: [85, 82, 78, 90, 86],
  }),
  r1: null,
  r2: null,
  "ord-h5": buildReport({
    id: "ord-h5",
    orderNo: "EF20260402001",
    scene: "年终晋升评委Q&A",
    date: "2026-04-02",
    expertName: "周予安",
    expertTitle: "前阿里 P9 面试官",
    total: 82,
    session: [78, 80, 75, 84, 79],
    average: [70, 72, 68, 74, 69],
    initial: [60, 62, 58, 66, 61],
    current: [78, 80, 75, 84, 79],
  }),
};

REPORT_MAP.r1 = REPORT_MAP["ord-h1"];
REPORT_MAP.r2 = REPORT_MAP["ord-h5"];

function buildReport(cfg) {
  const improvements = DIMENSION_LABELS.map((label, i) => ({
    label,
    delta: Math.round(((cfg.current[i] - cfg.initial[i]) / Math.max(cfg.initial[i], 1)) * 100),
  })).filter((x) => x.delta > 0);

  const expertFeedback = [
    {
      id: "fb1",
      timestamp: "02:15",
      seconds: 135,
      type: "warning",
      content: "此处论证缺乏数据支撑，听众难以建立信任感",
      suggestion: "建议引用去年的增长率或具体 KPI 完成率，用数字锚定观点",
    },
    {
      id: "fb2",
      timestamp: "05:10",
      seconds: 310,
      type: "question",
      content: "专家压力追问：如果资源减半，你的方案如何调整？",
      suggestion: "先承认约束，再给出优先级排序框架（影响×可行性矩阵）",
    },
    {
      id: "fb3",
      timestamp: "08:12",
      seconds: 492,
      type: "highlight",
      content: "结论先行运用得非常好，开场 15 秒即点明核心观点",
      suggestion: "保持这种开门见山的风格，可在每个段落复用「一句结论 + 两层论据」",
    },
    {
      id: "fb4",
      timestamp: "12:40",
      seconds: 760,
      type: "turn",
      content: "逻辑转折点：从问题描述切换到解决方案时过渡自然",
      suggestion: "可在此加入一句承上启下的过渡语，强化结构感",
    },
  ];

  const timelineMarkers = expertFeedback.map((f) => ({
    id: f.id,
    seconds: f.seconds,
    label: f.type === "question" ? "专家提问" : f.type === "highlight" ? "表达亮点" : f.type === "turn" ? "逻辑转折" : "待改进",
    type: f.type,
  }));

  return {
    orderInfo: {
      id: cfg.id,
      orderNo: cfg.orderNo,
      scene: cfg.scene,
      date: cfg.date,
      expertName: cfg.expertName,
      expertTitle: cfg.expertTitle,
    },
    scores: {
      total: cfg.total,
      dimensions: cfg.session,
      dimensionLabels: DIMENSION_LABELS,
      sessionValues: cfg.session,
      averageValues: cfg.average,
      averageCompare: "+15%",
      initialValues: cfg.initial,
      currentValues: cfg.current,
      improvements,
    },
    expertFeedback,
    timelineMarkers,
    videoUrl: "",
    videoDurationSec: 900,
    milestone: cfg.total >= 85 ? "恭喜！您已成功解锁「逻辑大师」勋章" : "再完成 2 次陪练即可解锁下一枚勋章",
    encouragement: scoreEncouragement(cfg.total),
    growthPath: [
      { id: "g1", date: "2025-11", title: "首次陪练", description: "完成第一节场景模拟课" },
      { id: "g2", date: "2026-01", title: "突破点", description: "第一次通过压力面试模拟", isBreakthrough: true },
      { id: "g3", date: "2026-03", title: "稳定输出", description: "连续 3 次综合得分超过 80" },
      { id: "g4", date: cfg.date.slice(0, 7), title: "本场训练", description: `${cfg.scene} · 综合 ${cfg.total} 分`, isBreakthrough: cfg.total >= 88 },
    ],
  };
}

/**
 * POST /api/v1/training/report
 * @param {number} trainingRecordId
 * @returns {Promise<import('@/types/report').TrainingReportDetail | Record<string, unknown>>}
 */
export async function fetchReportByTrainingId(trainingRecordId) {
  const { report } = await fetchTrainingReport(trainingRecordId);
  if (report && typeof report === "object" && report.orderInfo) {
    return /** @type {import('@/types/report').TrainingReportDetail} */ (report);
  }
  if (report && typeof report === "object") {
    return /** @type {import('@/types/report').TrainingReportDetail} */ (report);
  }
  const mock = REPORT_MAP["ord-h1"];
  return mock;
}

/**
 * @param {string} orderId
 * @returns {Promise<import('@/types/report').TrainingReportDetail>}
 */
export async function fetchReportDetail(orderId) {
  try {
    const data = await request({
      url: `/v1/reports/${encodeURIComponent(orderId)}`,
      method: "GET",
    });
    if (data && data.order_info) {
      return normalizeApiReport(data);
    }
  } catch {
    /* mock */
  }
  const mock = REPORT_MAP[orderId] || REPORT_MAP["ord-h1"];
  return mock;
}

function normalizeApiReport(raw) {
  return {
    orderInfo: {
      id: raw.order_info.id,
      orderNo: raw.order_info.id,
      scene: raw.order_info.scene,
      date: raw.order_info.date,
      expertName: raw.order_info.expert_name || "专家",
      expertTitle: raw.order_info.expert_title || "",
    },
    scores: {
      total: raw.scores.total,
      dimensions: raw.scores.dimensions,
      dimensionLabels: DIMENSION_LABELS,
      sessionValues: raw.scores.dimensions,
      averageValues: raw.scores.average_values || raw.scores.dimensions.map((v) => v - 10),
      averageCompare: raw.scores.average_compare || "+0%",
      initialValues: raw.scores.initial || raw.scores.dimensions.map((v) => v - 15),
      currentValues: raw.scores.dimensions,
      improvements: [],
    },
    expertFeedback: (raw.expert_feedback || []).map((f, i) => ({
      id: `fb-${i}`,
      timestamp: f.timestamp,
      seconds: parseTimestampSafe(f.timestamp),
      type: f.type,
      content: f.content,
      suggestion: f.suggestion,
    })),
    timelineMarkers: [],
    videoUrl: raw.video_url || "",
    videoDurationSec: raw.video_duration_sec || 600,
    milestone: raw.milestone || "",
    encouragement: scoreEncouragement(raw.scores.total),
    growthPath: raw.growth_path || [],
  };
}

function parseTimestampSafe(ts) {
  const parts = String(ts).split(":").map(Number);
  if (parts.length === 2) return parts[0] * 60 + parts[1];
  return 0;
}
