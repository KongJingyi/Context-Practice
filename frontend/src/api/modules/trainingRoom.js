/**
 * 训练房间扩展能力（对齐 api-v1-video-conference.md）
 *
 * P0 核心进房/离开见 @/api/modules/videoConference.js
 * P2 高光/录制见 GET /v1/training/{trainingId}/recording
 */
import { request } from "@/api/request.js";

/**
 * 训练录制回放与高光片段
 * @param {number|string} trainingId
 */
export async function fetchTrainingRecording(trainingId) {
  return request({
    url: `/v1/training/${trainingId}/recording`,
    method: "GET",
  });
}

export async function saveHighlightClip(payload) {
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
  });
}
