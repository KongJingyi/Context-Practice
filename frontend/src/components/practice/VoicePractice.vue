<template>
  <view class="vp">
    <view v-if="questionText" class="vp-q">
      <text class="vp-q-lbl">当前题目</text>
      <text class="vp-q-txt">{{ questionText }}</text>
    </view>

    <!-- 录音阶段 -->
    <view v-if="phase === 'idle' || phase === 'recording'" class="vp-studio">
      <view v-if="phase === 'recording'" class="vp-wpm">
        <text class="vp-wpm-num">{{ wpm }}</text>
        <text class="vp-wpm-lbl">字/分钟（估算）</text>
      </view>

      <view class="vp-wave-wrap">
        <!-- #ifdef H5 -->
        <canvas :id="canvasId" class="vp-canvas" />
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <view class="vp-bars">
          <view
            v-for="(h, i) in barLevels"
            :key="i"
            class="vp-bar"
            :style="{ height: `${Math.max(8, h * 100)}%` }"
          />
        </view>
        <!-- #endif -->
      </view>

      <view
        class="vp-rec-btn"
        :class="{ 'vp-rec-btn--on': phase === 'recording' }"
        @tap="toggleRecord"
      >
        <view class="vp-rec-inner" />
      </view>
      <text class="vp-rec-hint">{{ phase === 'recording' ? '点击结束录音' : '点击开始录音自测' }}</text>
    </view>

    <!-- AI 分析中 -->
    <view v-else-if="phase === 'analyzing'" class="vp-analyze">
      <text class="vp-analyze-txt">AI 正在深度解码表达逻辑…</text>
      <view class="vp-progress">
        <view class="vp-progress-fill" :style="{ width: `${analyzeProgress}%` }" />
      </view>
      <text class="vp-analyze-sub">{{ analyzeProgress }}%</text>
    </view>

    <!-- 报告 -->
    <view v-else-if="phase === 'report' && result" class="vp-report">
      <view class="vp-score-wrap" :class="{ 'vp-score-wrap--show': reportVisible }">
        <view class="vp-ring">
          <view class="vp-ring-glow" :class="{ 'vp-ring-glow--gold': result.score >= 85 }" />
          <text class="vp-ring-num">{{ result.score }}</text>
          <text class="vp-ring-lbl">综合得分</text>
          <view v-if="result.score >= 85" class="vp-particles">
            <view v-for="i in 8" :key="i" class="vp-particle" :class="`vp-particle--${i}`" />
          </view>
        </view>
      </view>

      <view class="vp-radar-wrap" :class="{ 'vp-radar-wrap--show': reportVisible }">
        <VoiceRadarChart :radar="result.radar" />
      </view>

      <view class="vp-metrics" :class="{ 'vp-metrics--show': reportVisible }">
        <view class="vp-metric">
          <text class="vp-metric-val">{{ result.metrics.speedWpm }}</text>
          <text class="vp-metric-lbl">语速 (字/分)</text>
        </view>
        <view class="vp-metric">
          <text class="vp-metric-val">{{ result.metrics.pauseCount }}</text>
          <text class="vp-metric-lbl">停顿次数</text>
        </view>
        <view class="vp-metric">
          <text class="vp-metric-val">{{ result.metrics.clarity }}</text>
          <text class="vp-metric-lbl">清晰度</text>
        </view>
      </view>

      <view class="vp-suggest" :class="{ 'vp-suggest--show': reportVisible }">
        <text class="vp-suggest-title">AI 建议</text>
        <text v-for="(s, i) in result.suggestions" :key="i" class="vp-suggest-item">· {{ s }}</text>
      </view>

      <view class="vp-retry" @tap="resetPractice">
        <text>再练一题</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from "vue";
import VoiceRadarChart from "@/components/practice/VoiceRadarChart.vue";
import { analyzeVoice } from "@/api/modules/practice.js";
import type { VoiceAnalysisResult } from "@/types/practice";
import {
  startRecording,
  stopRecording,
  abortRecording,
  isRecordingSupported,
  estimateWpm,
} from "@/utils/practice/recorder";

const props = defineProps<{
  questionText?: string;
}>();

type Phase = "idle" | "recording" | "analyzing" | "report";

const phase = ref<Phase>("idle");
const wpm = ref(0);
const barLevels = ref<number[]>(Array.from({ length: 32 }, () => 0.1));
const result = ref<VoiceAnalysisResult | null>(null);
const analyzeProgress = ref(0);
const reportVisible = ref(false);

const canvasId = `vp-wave-${Math.random().toString(36).slice(2, 9)}`;
let wpmTimer: ReturnType<typeof setInterval> | null = null;
let analyzeTimer: ReturnType<typeof setInterval> | null = null;
let avgLevel = 0.5;

// #ifdef H5
function drawWaveform(levels: number[]) {
  const el = document.getElementById(canvasId) as HTMLCanvasElement | null;
  if (!el) return;
  const ctx = el.getContext("2d");
  if (!ctx) return;
  const w = el.width;
  const h = el.height;
  ctx.clearRect(0, 0, w, h);

  const grad = ctx.createLinearGradient(0, 0, w, 0);
  grad.addColorStop(0, "#2563eb");
  grad.addColorStop(0.5, "#3b82f6");
  grad.addColorStop(1, "#60a5fa");
  ctx.fillStyle = grad;

  const barW = w / levels.length;
  levels.forEach((lv, i) => {
    const bh = Math.max(4, lv * h * 0.85);
    const x = i * barW + barW * 0.15;
    const y = (h - bh) / 2;
    ctx.beginPath();
    ctx.roundRect(x, y, barW * 0.7, bh, 3);
    ctx.fill();
  });

  ctx.strokeStyle = "rgba(59, 130, 246, 0.45)";
  ctx.lineWidth = 2;
  ctx.beginPath();
  levels.forEach((lv, i) => {
    const x = (i / (levels.length - 1)) * w;
    const y = h / 2 - lv * h * 0.35;
    if (i === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  });
  ctx.stroke();
}
// #endif

function onWaveform(levels: number[]) {
  barLevels.value = levels;
  const avg = levels.reduce((a, b) => a + b, 0) / levels.length;
  avgLevel = avgLevel * 0.85 + avg * 0.15;
  // #ifdef H5
  drawWaveform(levels);
  // #endif
}

async function toggleRecord() {
  if (phase.value === "idle") {
    // #ifdef H5
    if (!isRecordingSupported()) {
      uni.showToast({ title: "当前环境不支持录音", icon: "none" });
      return;
    }
    try {
      await startRecording(onWaveform);
      phase.value = "recording";
      wpmTimer = setInterval(() => {
        wpm.value = estimateWpm(10, avgLevel);
      }, 800);
    } catch {
      uni.showToast({ title: "请允许麦克风权限", icon: "none" });
    }
    // #endif
    // #ifndef H5
    phase.value = "recording";
    wpmTimer = setInterval(() => {
      wpm.value = estimateWpm(10, 0.5);
      barLevels.value = barLevels.value.map(() => 0.2 + Math.random() * 0.6);
    }, 120);
    // #endif
  } else if (phase.value === "recording") {
    await finishRecording();
  }
}

async function finishRecording() {
  if (wpmTimer) {
    clearInterval(wpmTimer);
    wpmTimer = null;
  }

  let durationSec = 8;
  // #ifdef H5
  try {
    const { durationSec: d } = await stopRecording();
    durationSec = d;
  } catch {
    abortRecording();
  }
  // #endif
  // #ifndef H5
  phase.value = "analyzing";
  // #endif

  phase.value = "analyzing";
  analyzeProgress.value = 0;
  reportVisible.value = false;

  analyzeTimer = setInterval(() => {
    analyzeProgress.value = Math.min(100, analyzeProgress.value + 4 + Math.floor(Math.random() * 6));
    if (analyzeProgress.value >= 100 && analyzeTimer) {
      clearInterval(analyzeTimer);
      analyzeTimer = null;
    }
  }, 120);

  const analysis = await analyzeVoice({ durationSec, blobSize: durationSec * 1024 });
  await new Promise((r) => setTimeout(r, 1800));

  if (analyzeTimer) {
    clearInterval(analyzeTimer);
    analyzeTimer = null;
  }
  analyzeProgress.value = 100;
  result.value = analysis;
  phase.value = "report";
  setTimeout(() => {
    reportVisible.value = true;
  }, 80);
}

function resetPractice() {
  phase.value = "idle";
  result.value = null;
  reportVisible.value = false;
  wpm.value = 0;
  barLevels.value = Array.from({ length: 32 }, () => 0.1);
  abortRecording();
}

watch(
  () => props.questionText,
  () => {
    if (phase.value === "report") resetPractice();
  },
);

onMounted(() => {
  // #ifdef H5
  const el = document.getElementById(canvasId) as HTMLCanvasElement | null;
  if (el) {
    el.width = el.clientWidth || 360;
    el.height = el.clientHeight || 200;
  }
  // #endif
});

onBeforeUnmount(() => {
  if (wpmTimer) clearInterval(wpmTimer);
  if (analyzeTimer) clearInterval(analyzeTimer);
  abortRecording();
});
</script>

<style scoped>
.vp {
  padding: 8rpx 0 32rpx;
}
.vp-q {
  margin-bottom: 24rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
}
.vp-q-lbl {
  display: block;
  font-size: 20rpx;
  color: #2563eb;
  font-weight: 700;
  margin-bottom: 8rpx;
}
.vp-q-txt {
  font-size: 28rpx;
  color: #0f172a;
  line-height: 1.5;
  font-weight: 600;
}

.vp-studio {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
}
.vp-wpm {
  text-align: center;
  margin-bottom: 16rpx;
  animation: vpPulse 1.2s ease-in-out infinite;
}
.vp-wpm-num {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #2563eb;
}
.vp-wpm-lbl {
  font-size: 22rpx;
  color: #64748b;
}
@keyframes vpPulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.vp-wave-wrap {
  width: 100%;
  height: 280rpx;
  border-radius: 24rpx;
  background: radial-gradient(ellipse at 50% 50%, rgba(59, 130, 246, 0.1) 0%, #ffffff 70%);
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  overflow: hidden;
}
.vp-canvas {
  width: 100%;
  height: 100%;
  display: block;
}
.vp-bars {
  width: 90%;
  height: 80%;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
}
.vp-bar {
  flex: 1;
  min-height: 8rpx;
  background: linear-gradient(180deg, #60a5fa, #2563eb);
  border-radius: 4rpx;
  transition: height 0.08s ease;
}

.vp-rec-btn {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(37, 99, 235, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.vp-rec-btn:active {
  transform: scale(0.95);
}
.vp-rec-btn--on {
  border-color: #ef4444;
  animation: vpRecPulse 1.5s ease-in-out infinite;
}
.vp-rec-inner {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #ef4444;
  transition: border-radius 0.2s ease;
}
.vp-rec-btn--on .vp-rec-inner {
  width: 40rpx;
  height: 40rpx;
  border-radius: 8rpx;
}
@keyframes vpRecPulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
  }
  50% {
    box-shadow: 0 0 0 16rpx rgba(239, 68, 68, 0);
  }
}
.vp-rec-hint {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #94a3b8;
}

.vp-analyze {
  padding: 80rpx 24rpx;
  text-align: center;
}
.vp-analyze-txt {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 32rpx;
}
.vp-progress {
  height: 8rpx;
  border-radius: 4rpx;
  background: #e2e8f0;
  overflow: hidden;
  margin-bottom: 12rpx;
}
.vp-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  border-radius: 4rpx;
  transition: width 0.15s ease;
}
.vp-analyze-sub {
  font-size: 22rpx;
  color: #64748b;
}

.vp-report {
  padding-bottom: 24rpx;
}
.vp-score-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 24rpx;
  opacity: 0;
  transform: translateY(20rpx);
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.vp-score-wrap--show {
  opacity: 1;
  transform: translateY(0);
}
.vp-ring {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: conic-gradient(#2563eb 0%, #3b82f6 50%, #dbeafe 50%);
  box-shadow: 0 0 40rpx rgba(37, 99, 235, 0.25);
}
.vp-ring-glow {
  position: absolute;
  inset: -8rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(59, 130, 246, 0.35);
  animation: vpRingBreath 2s ease-in-out infinite;
}
.vp-ring-glow--gold {
  border-color: rgba(251, 191, 36, 0.6);
  box-shadow: 0 0 24rpx rgba(251, 191, 36, 0.35);
}
@keyframes vpRingBreath {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
}
.vp-ring-num {
  font-size: 56rpx;
  font-weight: 900;
  color: #fff;
  z-index: 1;
}
.vp-ring-lbl {
  font-size: 20rpx;
  color: #dbeafe;
  z-index: 1;
}
.vp-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.vp-particle {
  position: absolute;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #fbbf24;
  animation: vpParticle 1.8s ease-out infinite;
}
.vp-particle--1 {
  top: 10%;
  left: 50%;
  animation-delay: 0s;
}
.vp-particle--2 {
  top: 30%;
  right: 10%;
  animation-delay: 0.2s;
}
.vp-particle--3 {
  bottom: 20%;
  right: 15%;
  animation-delay: 0.4s;
}
.vp-particle--4 {
  bottom: 10%;
  left: 50%;
  animation-delay: 0.6s;
}
.vp-particle--5 {
  bottom: 25%;
  left: 10%;
  animation-delay: 0.8s;
}
.vp-particle--6 {
  top: 35%;
  left: 8%;
  animation-delay: 1s;
}
.vp-particle--7 {
  top: 15%;
  right: 25%;
  animation-delay: 0.3s;
}
.vp-particle--8 {
  bottom: 40%;
  right: 5%;
  animation-delay: 0.5s;
}
@keyframes vpParticle {
  0% {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate3d(var(--dx, 20rpx), var(--dy, -40rpx), 0) scale(0);
  }
}

.vp-radar-wrap {
  opacity: 0;
  transform: translateY(24rpx);
  transition: all 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.15s;
}
.vp-radar-wrap--show {
  opacity: 1;
  transform: translateY(0);
}
.vp-metrics {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin: 24rpx 0;
  opacity: 0;
  transform: translateY(24rpx);
  transition: all 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.3s;
}
.vp-metrics--show {
  opacity: 1;
  transform: translateY(0);
}
.vp-metric {
  flex: 1;
  padding: 20rpx 12rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.04);
  text-align: center;
}
.vp-metric-val {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #2563eb;
}
.vp-metric-lbl {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #64748b;
}

.vp-suggest {
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
  opacity: 0;
  transform: translateY(24rpx);
  transition: all 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.45s;
}
.vp-suggest--show {
  opacity: 1;
  transform: translateY(0);
}
.vp-suggest-title {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 12rpx;
}
.vp-suggest-item {
  display: block;
  font-size: 24rpx;
  color: #475569;
  line-height: 1.6;
  margin-bottom: 6rpx;
}

.vp-retry {
  margin-top: 24rpx;
  padding: 20rpx;
  text-align: center;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  cursor: pointer;
}
.vp-retry text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}
</style>
