/**
 * WebRTC 占位：本地媒体 + 信令事件钩子
 * 与学员端 @/utils/training/webrtc 保持一致，便于后续对接 TRTC
 */

export type IceCandidateHandler = (candidate: RTCIceCandidateInit) => void;
export type TrackHandler = (stream: MediaStream, kind: "local" | "remote") => void;

export interface WebRTCSessionHooks {
  onTrack?: TrackHandler;
  onCandidate?: IceCandidateHandler;
}

let localStream: MediaStream | null = null;
const hooks: WebRTCSessionHooks = {};

export function setWebRTCHooks(next: WebRTCSessionHooks) {
  Object.assign(hooks, next);
}

export async function startLocalMedia(constraints: MediaStreamConstraints = {}) {
  if (typeof navigator === "undefined" || !navigator.mediaDevices?.getUserMedia) {
    throw new Error("当前环境不支持 getUserMedia");
  }
  const stream = await navigator.mediaDevices.getUserMedia({
    audio: true,
    video: { facingMode: "user", width: { ideal: 1280 }, height: { ideal: 720 } },
    ...constraints,
  });
  localStream = stream;
  hooks.onTrack?.(stream, "local");
  return stream;
}

export function getLocalStream() {
  return localStream;
}

export function stopLocalMedia() {
  localStream?.getTracks().forEach((t) => t.stop());
  localStream = null;
}

export function setLocalAudioEnabled(enabled: boolean) {
  localStream?.getAudioTracks().forEach((t) => {
    t.enabled = enabled;
  });
}

export function setLocalVideoEnabled(enabled: boolean) {
  localStream?.getVideoTracks().forEach((t) => {
    t.enabled = enabled;
  });
}

export async function attachStreamToVideo(
  el: HTMLVideoElement | null,
  stream: MediaStream | null,
) {
  if (!el) return;
  if (el.srcObject !== stream) {
    el.srcObject = stream;
  }
  if (stream) {
    try {
      await el.play();
    } catch {
      /* ignore autoplay policy */
    }
  }
}
