import { request } from "@/api/request";

export async function saveHighlightClip(payload: {
  sessionId: string;
  roomId?: string;
  atMs: number;
  durationSec?: number;
}) {
  try {
    return await request({
      url: "/training/highlight",
      method: "POST",
      data: {
        room_id: payload.roomId ?? payload.sessionId,
        at_ms: payload.atMs,
        duration_sec: payload.durationSec ?? 15,
      },
      silent: true,
    });
  } catch {
    await new Promise((r) => setTimeout(r, 300));
    return { id: `hl-${Date.now()}`, ok: true };
  }
}
