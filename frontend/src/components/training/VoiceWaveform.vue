<template>
  <view class="vw">
    <text class="vw-title">表达稳定度</text>
  <!-- #ifdef H5 -->
  <canvas
    :id="canvasId"
    class="vw-canvas"
    :canvas-id="canvasId"
  />
  <!-- #endif -->
  <!-- #ifndef H5 -->
  <view class="vw-bars">
    <view
      v-for="(h, i) in barHeights"
      :key="i"
      class="vw-bar"
      :style="{ height: `${h}%` }"
    />
  </view>
  <!-- #endif -->
    <text class="vw-meta">{{ levelLabel }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from "vue";

const props = defineProps<{
  active: boolean;
}>();

const canvasId = `vw-${Math.random().toString(36).slice(2, 9)}`;
const barHeights = ref<number[]>(Array.from({ length: 24 }, () => 30));
const level = ref(0.6);

const levelLabel = computed(() => {
  if (level.value > 0.75) return "语速平稳";
  if (level.value > 0.45) return "音量适中";
  return "请提高音量";
});

let raf = 0;
let phase = 0;

function tickBars() {
  if (!props.active) return;
  phase += 0.12;
  barHeights.value = barHeights.value.map((_, i) => {
    const v = 0.35 + Math.abs(Math.sin(phase + i * 0.35)) * 0.55;
    level.value = level.value * 0.92 + v * 0.08;
    return Math.round(v * 100);
  });

  // #ifdef H5
  drawCanvas();
  // #endif

  raf = requestAnimationFrame(tickBars);
}

// #ifdef H5
function drawCanvas() {
  const el = document.getElementById(canvasId) as HTMLCanvasElement | null;
  if (!el) return;
  const ctx = el.getContext("2d");
  if (!ctx) return;
  const w = el.width;
  const h = el.height;
  ctx.clearRect(0, 0, w, h);
  const n = 32;
  ctx.strokeStyle = "rgba(96, 165, 250, 0.85)";
  ctx.lineWidth = 2;
  ctx.beginPath();
  for (let i = 0; i < n; i += 1) {
    const x = (i / (n - 1)) * w;
    const y = h / 2 + Math.sin(phase * 2 + i * 0.4) * (h * 0.35) * (0.5 + level.value * 0.5);
    if (i === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  }
  ctx.stroke();
}
// #endif

watch(
  () => props.active,
  (v) => {
    if (raf) cancelAnimationFrame(raf);
    if (v) raf = requestAnimationFrame(tickBars);
  },
);

onMounted(() => {
  // #ifdef H5
  const el = document.getElementById(canvasId) as HTMLCanvasElement | null;
  if (el) {
    el.width = 80;
    el.height = 200;
  }
  // #endif
  if (props.active) raf = requestAnimationFrame(tickBars);
});

onBeforeUnmount(() => {
  if (raf) cancelAnimationFrame(raf);
});
</script>

<style scoped>
.vw {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
  box-sizing: border-box;
}
.vw-title {
  font-size: 20rpx;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 12rpx;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  letter-spacing: 4rpx;
}
.vw-canvas {
  flex: 1;
  width: 80px;
  height: 200px;
}
.vw-bars {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: 4rpx;
  width: 100%;
  min-height: 200rpx;
}
.vw-bar {
  flex: 1;
  min-height: 8rpx;
  background: linear-gradient(180deg, #60a5fa, #2563eb);
  border-radius: 4rpx 4rpx 0 0;
  transition: height 0.12s ease;
}
.vw-meta {
  margin-top: 12rpx;
  font-size: 18rpx;
  color: #94a3b8;
  text-align: center;
}
</style>
