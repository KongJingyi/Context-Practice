<template>
  <div class="vw">
    <span class="vw-title">表达稳定度</span>
    <canvas :id="canvasId" ref="canvasRef" class="vw-canvas" />
    <span class="vw-meta">{{ levelLabel }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from "vue";

const props = defineProps<{
  active: boolean;
}>();

const canvasId = `vw-${Math.random().toString(36).slice(2, 9)}`;
const canvasRef = ref<HTMLCanvasElement | null>(null);
const level = ref(0.6);

const levelLabel = computed(() => {
  if (level.value > 0.75) return "语速平稳";
  if (level.value > 0.45) return "音量适中";
  return "请提高音量";
});

let raf = 0;
let phase = 0;

function drawCanvas() {
  const el = canvasRef.value;
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

function tick() {
  if (!props.active) return;
  phase += 0.12;
  const v = 0.35 + Math.abs(Math.sin(phase)) * 0.55;
  level.value = level.value * 0.92 + v * 0.08;
  drawCanvas();
  raf = requestAnimationFrame(tick);
}

watch(
  () => props.active,
  (v) => {
    if (raf) cancelAnimationFrame(raf);
    if (v) raf = requestAnimationFrame(tick);
  },
);

onMounted(() => {
  const el = canvasRef.value;
  if (el) {
    el.width = 80;
    el.height = 200;
  }
  if (props.active) raf = requestAnimationFrame(tick);
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
  padding: 12px 6px;
  box-sizing: border-box;
}
.vw-title {
  font-size: 11px;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 10px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  letter-spacing: 3px;
}
.vw-canvas {
  flex: 1;
  width: 80px;
  min-height: 160px;
}
.vw-meta {
  margin-top: 10px;
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
}
</style>
