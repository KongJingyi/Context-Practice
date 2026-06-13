import { request } from "@/api/request";
import type { CoachDashboard } from "@/types/videoConference";

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
  return (await request({
    url: "/v1/reviews/submit",
    method: "POST",
    data: payload,
  })) as CoachReportResult;
}

export async function fetchCoachDashboard(): Promise<CoachDashboard> {
  return (await request({
    url: "/v1/coach/dashboard",
    method: "GET",
    silent: true,
  })) as CoachDashboard;
}

export interface RecordingResult {
  recordingUrl: string;
  recordingStatus?: string;
  durationSeconds: number;
  highlights: { startSec: number; endSec: number; label: string; clipUrl?: string }[];
  expiresAt: string;
  expired?: boolean;
}

export async function fetchRecording(trainingId: number): Promise<RecordingResult | null> {
  if (!trainingId) return null;
  return (await request({
    url: `/v1/training/${trainingId}/recording`,
    method: "GET",
    silent: true,
  })) as RecordingResult;
}

export async function fetchReportByOrder(orderId: number | string) {
  return request({
    url: `/v1/reports/${orderId}`,
    method: "GET",
    silent: true,
  });
}

export async function fetchPressureQuestions(): Promise<{ id: string; label: string; text: string }[]> {
  try {
    const rows = (await request({
      url: "/v1/practice/questions",
      method: "GET",
      params: { category: "pressure" },
      silent: true,
    })) as { id: string; text?: string; title?: string }[];
    if (!Array.isArray(rows) || !rows.length) return [];
    return rows.slice(0, 6).map((q) => {
      const text = q.text || q.title || "";
      const label = text.length > 8 ? text.slice(0, 8) + "…" : text;
      return { id: String(q.id), label, text };
    });
  } catch {
    return [];
  }
}
