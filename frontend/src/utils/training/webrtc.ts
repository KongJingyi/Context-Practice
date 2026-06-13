/**
 * WebRTC 占位：本地媒体 + 信令事件钩子
 * 后续对接 RTCPeerConnection 时在此扩展
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

/** 调起本地摄像头/麦克风（H5） */
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

/** Mock：模拟远端 onTrack（演示用） */
export function mockRemoteConnected(delayMs = 2800) {
  return new Promise<void>((resolve) => {
    setTimeout(() => {
      if (typeof MediaStream !== "undefined") {
        hooks.onTrack?.(new MediaStream(), "remote");
      }
      resolve();
    }, delayMs);
  });
}

/** Mock：模拟 ICE candidate 回调 */
export function mockEmitCandidate() {
  hooks.onCandidate?.({
    candidate: "candidate:mock 1 udp 2130706431 192.168.0.1 54400 typ host",
    sdpMid: "0",
    sdpMLineIndex: 0,
  });
}

/** 将 MediaStream 绑定到原生 video 元素并尝试播放 */
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
      /* muted 本地流通常可 autoplay；忽略偶发策略拦截 */
    }
  }
}
