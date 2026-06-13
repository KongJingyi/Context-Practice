<template>
  <view v-if="visible" class="wb">
    <view class="wb-bar">
      <text class="wb-title">陪练白板</text>
    </view>
    <!-- #ifdef H5 -->
    <canvas ref="canvasRef" class="wb-canvas" />
    <!-- #endif -->
    <!-- #ifndef H5 -->
    <view class="wb-fallback">
      <text class="wb-fallback-text">请在 H5 浏览器查看白板内容</text>
    </view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from "vue";

export interface WhiteboardStroke {
  id: string;
  color: string;
  width: number;
  points: { x: number; y: number }[];
}

const props = defineProps<{
  visible: boolean;
  strokes: WhiteboardStroke[];
  version: number;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
let ro: ResizeObserver | null = null;

function drawStroke(ctx: CanvasRenderingContext2D, stroke: WhiteboardStroke, w: number, h: number) {
  if (!stroke.points?.length) return;
  ctx.strokeStyle = stroke.color || "#fff";
  ctx.lineWidth = stroke.width || 3;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.beginPath();
  stroke.points.forEach((p, i) => {
    const x = p.x * w;
    const y = p.y * h;
    if (i === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  });
  ctx.stroke();
}

function redraw() {
  // #ifdef H5
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  if (!ctx) return;
  const w = canvas.width;
  const h = canvas.height;
  ctx.clearRect(0, 0, w, h);
  ctx.fillStyle = "rgba(15, 23, 42, 0.92)";
  ctx.fillRect(0, 0, w, h);
  props.strokes.forEach((s) => drawStroke(ctx, s, w, h));
  // #endif
}

function resizeCanvas() {
  // #ifdef H5
  const canvas = canvasRef.value;
  if (!canvas?.parentElement) return;
  const { clientWidth, clientHeight } = canvas.parentElement;
  if (clientWidth < 1 || clientHeight < 1) return;
  canvas.width = clientWidth;
  canvas.height = clientHeight;
  redraw();
  // #endif
}

watch(
  () => [props.strokes, props.version, props.visible] as const,
  () => nextTick(redraw),
  { deep: true },
);

onMounted(() => {
  // #ifdef H5
  nextTick(() => {
    resizeCanvas();
    if (canvasRef.value?.parentElement) {
      ro = new ResizeObserver(resizeCanvas);
      ro.observe(canvasRef.value.parentElement);
    }
  });
  // #endif
});

onBeforeUnmount(() => {
  ro?.disconnect();
});
</script>

<style scoped>
.wb {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  background: rgba(15, 23, 42, 0.95);
  border-radius: inherit;
}
.wb-bar {
  padding: 16rpx 24rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.08);
}
.wb-title {
  font-size: 24rpx;
  font-weight: 700;
  color: #e2e8f0;
}
.wb-canvas {
  flex: 1;
  width: 100%;
}
.wb-fallback {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wb-fallback-text {
  font-size: 24rpx;
  color: #64748b;
}
</style>
