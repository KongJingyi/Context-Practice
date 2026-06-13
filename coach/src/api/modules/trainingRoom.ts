import { request } from "@/api/request";

export async function saveHighlightClip(payload: {
  sessionId: string;
  roomId?: string;
  atMs?: number;
  durationSec?: number;
  startSec?: number;
}) {
  const duration = payload.durationSec ?? 15;
  const startSec = payload.startSec ?? 0;
  return request({
    url: "/training/highlight",
    method: "POST",
    data: {
      roomId: payload.roomId ?? payload.sessionId,
      startSec,
      endSec: startSec + duration,
      type: "HIGHLIGHT",
      label: "训练亮点",
    },
    silent: true,
  });
}
