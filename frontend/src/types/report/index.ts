export type FeedbackType = "warning" | "highlight" | "question" | "turn";

export interface ReportOrderInfo {
  id: string;
  orderNo: string;
  scene: string;
  date: string;
  expertName: string;
  expertTitle: string;
}

export interface ReportScores {
  total: number;
  dimensions: number[];
  dimensionLabels: string[];
  sessionValues: number[];
  averageValues: number[];
  averageCompare: string;
  initialValues: number[];
  currentValues: number[];
  improvements: { label: string; delta: number }[];
}

export interface ExpertFeedbackItem {
  id: string;
  timestamp: string;
  seconds: number;
  type: FeedbackType;
  content: string;
  suggestion: string;
}

export interface TimelineMarker {
  id: string;
  seconds: number;
  label: string;
  type: FeedbackType;
}

export interface GrowthMilestone {
  id: string;
  date: string;
  title: string;
  description: string;
  isBreakthrough?: boolean;
}

export interface TrainingReportDetail {
  orderInfo: ReportOrderInfo;
  scores: ReportScores;
  expertFeedback: ExpertFeedbackItem[];
  timelineMarkers: TimelineMarker[];
  videoUrl: string;
  videoDurationSec: number;
  milestone: string;
  encouragement: string;
  growthPath: GrowthMilestone[];
}
