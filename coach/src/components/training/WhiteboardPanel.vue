<template>
  <div v-if="visible" class="wb">
    <div class="wb-bar">
      <span class="wb-title">白板</span>
      <div v-if="!readonly" class="wb-tools">
        <input v-model="penColor" type="color" class="wb-color" title="笔色" />
        <button type="button" class="wb-btn" @click="emit('clear')">清空</button>
        <button type="button" class="wb-btn wb-btn--ghost" @click="emit('close')">关闭</button>
      </div>
    </div>
    <canvas
      ref="canvasRef"
      class="wb-canvas"
      :class="{ 'wb-canvas--ro': readonly }"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointerleave="onPointerUp"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from "vue";
import type { WhiteboardStroke } from "@/api/modules/videoConference";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    readonly?: boolean;
    strokes: WhiteboardStroke[];
    version: number;
  }>(),
  { readonly: false, version: 0 },
);

const emit = defineEmits<{
  (e: "stroke", stroke: WhiteboardStroke): void;
  (e: "clear"): void;
  (e: "close"): void;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const penColor = ref("#ffffff");
const drawing = ref(false);
const currentPoints = ref<{ x: number; y: number }[]>([]);
let ro: ResizeObserver | null = null;

function normFromEvent(e: PointerEvent) {
  const canvas = canvasRef.value;
  if (!canvas) return { x: 0, y: 0 };
  const rect = canvas.getBoundingClientRect();
  return {
    x: Math.min(1, Math.max(0, (e.clientX - rect.left) / rect.width)),
    y: Math.min(1, Math.max(0, (e.clientY - rect.top) / rect.height)),
  };
}

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
}

function resizeCanvas() {
  const canvas = canvasRef.value;
  if (!canvas?.parentElement) return;
  const { clientWidth, clientHeight } = canvas.parentElement;
  if (clientWidth < 1 || clientHeight < 1) return;
  canvas.width = clientWidth;
  canvas.height = clientHeight;
  redraw();
}

function onPointerDown(e: PointerEvent) {
  if (props.readonly) return;
  drawing.value = true;
  currentPoints.value = [normFromEvent(e)];
  canvasRef.value?.setPointerCapture(e.pointerId);
}

function onPointerMove(e: PointerEvent) {
  if (!drawing.value || props.readonly) return;
  const pt = normFromEvent(e);
  currentPoints.value.push(pt);
  const canvas = canvasRef.value;
  const ctx = canvas?.getContext("2d");
  if (!canvas || !ctx || currentPoints.value.length < 2) return;
  const pts = currentPoints.value;
  const last = pts[pts.length - 2];
  const cur = pts[pts.length - 1];
  ctx.strokeStyle = penColor.value;
  ctx.lineWidth = 3;
  ctx.lineCap = "round";
  ctx.beginPath();
  ctx.moveTo(last.x * canvas.width, last.y * canvas.height);
  ctx.lineTo(cur.x * canvas.width, cur.y * canvas.height);
  ctx.stroke();
}

function onPointerUp() {
  if (!drawing.value || props.readonly) return;
  drawing.value = false;
  if (currentPoints.value.length < 2) {
    currentPoints.value = [];
    return;
  }
  const stroke: WhiteboardStroke = {
    id: `s-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    color: penColor.value,
    width: 3,
    points: [...currentPoints.value],
  };
  currentPoints.value = [];
  emit("stroke", stroke);
}

watch(
  () => [props.strokes, props.version, props.visible] as const,
  () => {
    nextTick(redraw);
  },
  { deep: true },
);

onMounted(() => {
  nextTick(resizeCanvas);
  if (canvasRef.value?.parentElement) {
    ro = new ResizeObserver(resizeCanvas);
    ro.observe(canvasRef.value.parentElement);
  }
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}
.wb-title {
  font-size: 12px;
  font-weight: 700;
  color: #e2e8f0;
}
.wb-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}
.wb-color {
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}
.wb-btn {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: rgba(59, 130, 246, 0.55);
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.wb-btn--ghost {
  background: rgba(255, 255, 255, 0.08);
  color: #94a3b8;
}
.wb-canvas {
  flex: 1;
  width: 100%;
  touch-action: none;
  cursor: crosshair;
}
.wb-canvas--ro {
  cursor: default;
}
</style>
