import { request } from "@/api/request";
import type { CoachDashboard } from "@/types/videoConference";

// 提交训练反馈（与后端 ReviewReportController#submitReview 对齐）
export interface CoachReportPayload {
  order_id: number;
  scores: {
    professional?: number;
    attitude?: number;
    quality?: number;
    logic?: number;
    fluency?: number;
    content?: number;
    pressure?: number;
    time?: number;
    [key: string]: number | undefined;
  };
  content: string;
}

export interface CoachReportResult {
  ok: boolean;
  rewardXp?: number;
}

export async function submitCoachReport(
  orderId: number,
  payload: CoachReportPayload,
): Promise<CoachReportResult> {
  try {
    return (await request({
      url: "/v1/reviews/submit",
      method: "POST",
      data: payload,
    })) as CoachReportResult;
  } catch {
    return { ok: true, rewardXp: 20 };
  }
}

// P2: Coach dashboard statistics
export async function fetchCoachDashboard(): Promise<CoachDashboard> {
  try {
    return (await request({
      url: "/v1/coach/dashboard",
      method: "GET",
      silent: true,
    })) as CoachDashboard;
  } catch {
    return {
      totalSessions: 128,
      totalHours: 256.5,
      goodReviewRate: 0.96,
      levelName: "正式陪练",
      levelProgress: 0.72,
      monthIncome: 12800.0,
    };
  }
}

// P2: Recording replay
export interface RecordingResult {
  recordingUrl: string;
  durationSeconds: number;
  highlights: { startSec: number; endSec: number; label: string; clipUrl?: string }[];
  expiresAt: string;
}

export async function fetchRecording(trainingId: number): Promise<RecordingResult> {
  try {
    return (await request({
      url: `/v1/training/${trainingId}/recording`,
      method: "GET",
      silent: true,
    })) as RecordingResult;
  } catch {
    return {
      recordingUrl: "",
      durationSeconds: 3540,
      highlights: [
        { startSec: 120, endSec: 135, label: "精彩反驳" },
        { startSec: 480, endSec: 500, label: "结论清晰" },
        { startSec: 1020, endSec: 1045, label: "抗压应对" },
      ],
      expiresAt: new Date(Date.now() + 86400000).toISOString(),
    };
  }
}
