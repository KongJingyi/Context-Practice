/**
 * 训练房间扩展能力（对齐 api-v1-video-conference.md）
 *
 * P0 核心进房/离开见 @/api/modules/videoConference.js
 * P2 高光/录制见 GET /v1/training/{trainingId}/recording（待接）
 */
import { request } from "@/api/request.js";

/**
 * P2 预留 — 高光片段（文档 recording.highlights）
 * @param {{ sessionId: string; roomId?: string; atMs: number; durationSec?: number }} payload
 */
export async function saveHighlightClip(payload) {
  try {
    const data = await request({
      url: "/training/highlight",
      method: "POST",
      data: {
        room_id: payload.roomId ?? payload.sessionId,
        at_ms: payload.atMs,
        duration_sec: payload.durationSec ?? 15,
      },
    });
    if (data) return data;
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 300));
  return { id: `hl-${Date.now()}`, ok: true };
}
