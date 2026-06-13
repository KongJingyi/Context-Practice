/** 成长引擎步骤图标类型 */
export type GrowthStepIcon = "mic" | "wave" | "radar";

/** 左侧流程单步（对接 API 时可整表替换） */
export interface GrowthEngineStep {
  id: string;
  step: number;
  title: string;
  icon: GrowthStepIcon;
  description: string;
}

/** 雷达图维度 */
export interface GrowthRadarDimension {
  label: string;
  value: number;
}

/** AI 关键词标签 */
export interface GrowthKeywordTag {
  text: string;
  tone: "positive" | "warning" | "neutral";
}

/** 雷达周围悬浮卡片 */
export interface GrowthFloatingCard {
  id: string;
  text: string;
}

/** 右侧练习成果看板 */
export interface GrowthDashboardData {
  title: string;
  score: number;
  dimensions: GrowthRadarDimension[];
  keywords: GrowthKeywordTag[];
  floatingCards: GrowthFloatingCard[];
}

/** 整块 Growth Engine 数据 */
export interface GrowthEngineData {
  sectionTitle: string;
  sectionSubtitle: string;
  steps: GrowthEngineStep[];
  dashboard: GrowthDashboardData;
}
