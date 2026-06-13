/**
 * H5 录音占位：MediaRecorder + AudioContext 波形分析
 */

export type WaveformCallback = (levels: number[]) => void;

let mediaStream: MediaStream | null = null;
let audioContext: AudioContext | null = null;
let analyser: AnalyserNode | null = null;
let recorder: MediaRecorder | null = null;
let chunks: BlobPart[] = [];
let rafId = 0;
let onWaveform: WaveformCallback | null = null;
let recordStartedAt = 0;

export function isRecordingSupported() {
  return (
    typeof navigator !== "undefined" &&
    Boolean(navigator.mediaDevices?.getUserMedia) &&
    typeof MediaRecorder !== "undefined"
  );
}

export async function startRecording(onWave: WaveformCallback) {
  if (!isRecordingSupported()) {
    throw new Error("当前环境不支持录音");
  }
  onWaveform = onWave;
  mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
  audioContext = new AudioContext();
  const source = audioContext.createMediaStreamSource(mediaStream);
  analyser = audioContext.createAnalyser();
  analyser.fftSize = 64;
  source.connect(analyser);

  chunks = [];
  recorder = new MediaRecorder(mediaStream);
  recorder.ondataavailable = (e) => {
    if (e.data.size > 0) chunks.push(e.data);
  };
  recorder.start(200);
  recordStartedAt = Date.now();

  const data = new Uint8Array(analyser.frequencyBinCount);
  const tick = () => {
    if (!analyser || !onWaveform) return;
    analyser.getByteFrequencyData(data);
    const n = 32;
    const levels: number[] = [];
    const step = Math.floor(data.length / n);
    for (let i = 0; i < n; i += 1) {
      levels.push(data[i * step] / 255);
    }
    onWaveform(levels);
    rafId = requestAnimationFrame(tick);
  };
  rafId = requestAnimationFrame(tick);
}

export function stopRecording(): Promise<{ blob: Blob; durationSec: number }> {
  return new Promise((resolve, reject) => {
    if (!recorder) {
      reject(new Error("未开始录音"));
      return;
    }
  const startedAt = recordStartedAt || Date.now();
    if (rafId) cancelAnimationFrame(rafId);
    rafId = 0;
    onWaveform = null;

    recorder.onstop = () => {
      const blob = new Blob(chunks, { type: "audio/webm" });
      const durationSec = Math.max(1, Math.round((Date.now() - startedAt) / 1000));
      cleanup();
      resolve({ blob, durationSec });
    };
    recorder.stop();
  });
}

function cleanup() {
  mediaStream?.getTracks().forEach((t) => t.stop());
  mediaStream = null;
  void audioContext?.close();
  audioContext = null;
  analyser = null;
  recorder = null;
  chunks = [];
}

export function abortRecording() {
  if (rafId) cancelAnimationFrame(rafId);
  rafId = 0;
  onWaveform = null;
  if (recorder && recorder.state !== "inactive") {
    try {
      recorder.stop();
    } catch {
      /* ignore */
    }
  }
  cleanup();
}

/** 估算语速（演示用：基于录音时长随机波动） */
export function estimateWpm(durationSec: number, level = 0.5) {
  const base = 130 + level * 40;
  const jitter = (Math.random() - 0.5) * 24;
  const durationBoost = durationSec > 20 ? -8 : 6;
  return Math.round(Math.max(90, Math.min(220, base + jitter + durationBoost)));
}
