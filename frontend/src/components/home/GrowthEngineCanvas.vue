<template>
  <view ref="sectionRef" class="gec" :class="{ 'gec--visible': isVisible }">
    <view class="gec__inner">
      <view class="gec__header gec-reveal" :style="revealStyle(0)">
        <text class="gec__title">{{ data.sectionTitle }}</text>
        <text class="gec__subtitle">{{ data.sectionSubtitle }}</text>
      </view>

      <view class="gec__body">
        <!-- 左侧流程 -->
        <view class="gec__flow">
          <view class="gec__line-track" aria-hidden="true">
            <view class="gec__line-gradient" />
            <view class="gec__line-dot" />
          </view>

          <view
            v-for="(step, index) in data.steps"
            :key="step.id"
            class="gec-step gec-reveal"
            :class="{
              'gec-step--active': activeStep === index,
              'gec-step--review': step.id === 'review',
            }"
            :style="revealStyle(index + 1)"
            @mouseenter="onStepHover(index)"
            @mouseleave="onStepLeave"
            @tap="onStepTap(index)"
          >
            <view class="gec-step__node">
              <view class="gec-step__dot" />
              <view class="gec-step__icon" :class="`gec-step__icon--${step.icon}`">
                <view
                  v-if="step.icon === 'wave'"
                  class="gec-step__wave-bars"
                >
                  <view
                    v-for="b in 3"
                    :key="b"
                    class="gec-step__wave-bar"
                    :style="{ animationDelay: `${(b - 1) * 0.12}s` }"
                  />
                </view>
              </view>
            </view>
            <view class="gec-step__card">
              <text class="gec-step__index">STEP {{ step.step }}</text>
              <text class="gec-step__title">{{ step.title }}</text>
              <text class="gec-step__desc">{{ step.description }}</text>
            </view>
          </view>
        </view>

        <!-- 右侧看板 -->
        <view class="gec__dashboard gec-reveal" :style="revealStyle(4)">
          <view class="gec-board">
            <view class="gec-board__head">
              <text class="gec-board__label">{{ data.dashboard.title }}</text>
              <view class="gec-board__score-wrap">
                <text class="gec-board__score">{{ displayScore }}</text>
                <text class="gec-board__score-unit">分</text>
              </view>
            </view>

            <view class="gec-board__chart-wrap">
              <!-- 悬浮小卡片 -->
              <view
                v-for="(card, i) in data.dashboard.floatingCards"
                :key="card.id"
                class="gec-float gec-reveal"
                :class="`gec-float--${i + 1}`"
                :style="revealStyle(5 + i)"
              >
                <text class="gec-float__text">{{ card.text }}</text>
              </view>

              <!-- 关键词 -->
              <view
                v-for="(tag, ti) in data.dashboard.keywords"
                :key="`kw-${ti}`"
                class="gec-keyword"
                :class="`gec-keyword--${tag.tone}`"
                :style="keywordStyle(ti)"
              >
                <text class="gec-keyword__text">{{ tag.text }}</text>
              </view>

              <!-- 雷达图 -->
              <view
                class="gec-radar"
                :class="{ 'gec-radar--burst': radarBurst }"
              >
                <svg
                  class="gec-radar__svg"
                  viewBox="0 0 320 320"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <g transform="translate(160,160)">
                    <polygon
                      v-for="level in gridLevels"
                      :key="`grid-${level}`"
                      :points="gridPolygon(level)"
                      class="gec-radar__grid"
                    />
                    <line
                      v-for="(_, i) in data.dashboard.dimensions"
                      :key="`axis-${i}`"
                      x1="0"
                      y1="0"
                      :x2="axisEnd(i).x"
                      :y2="axisEnd(i).y"
                      class="gec-radar__axis"
                    />
                    <polygon
                      :points="dataPolygon"
                      class="gec-radar__area"
                    />
                    <circle
                      v-for="(p, pi) in dataPoints"
                      :key="`pt-${pi}`"
                      :cx="p.x"
                      :cy="p.y"
                      r="4"
                      class="gec-radar__point"
                    />
                  </g>
                </svg>
                <view
                  v-for="(dim, di) in data.dashboard.dimensions"
                  :key="`lbl-${di}`"
                  class="gec-radar__label"
                  :style="labelStyle(di)"
                >
                  <text>{{ dim.label }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { GROWTH_ENGINE_DATA } from "@/data/defaults/growthEngine";
import type { GrowthEngineData } from "@/types/home/growthEngine";

const props = withDefaults(
  defineProps<{
    data?: GrowthEngineData;
  }>(),
  {
    data: () => GROWTH_ENGINE_DATA,
  },
);

const sectionRef = ref<{ $el?: HTMLElement } | HTMLElement | null>(null);
const isVisible = ref(false);
const activeStep = ref(0);
const radarBurst = ref(false);
const displayScore = ref(0);

const RADIUS = 100;
const gridLevels = [0.25, 0.5, 0.75, 1];

let scoreTimer: ReturnType<typeof setInterval> | null = null;
let burstTimer: ReturnType<typeof setTimeout> | null = null;

const dimensionCount = computed(() => props.data.dashboard.dimensions.length);

function angleAt(index: number) {
  return ((Math.PI * 2 * index) / dimensionCount.value) - Math.PI / 2;
}

function polarPoint(index: number, ratio: number) {
  const a = angleAt(index);
  const r = RADIUS * ratio;
  return { x: r * Math.cos(a), y: r * Math.sin(a) };
}

function gridPolygon(level: number) {
  return props.data.dashboard.dimensions
    .map((_, i) => {
      const p = polarPoint(i, level);
      return `${p.x},${p.y}`;
    })
    .join(" ");
}

const dataPoints = computed(() =>
  props.data.dashboard.dimensions.map((d, i) =>
    polarPoint(i, Math.min(d.value / 100, 1)),
  ),
);

const dataPolygon = computed(() =>
  dataPoints.value.map((p) => `${p.x},${p.y}`).join(" "),
);

function axisEnd(index: number) {
  return polarPoint(index, 1);
}

function labelStyle(index: number) {
  const p = polarPoint(index, 1.22);
  const left = 160 + p.x;
  const top = 160 + p.y;
  return {
    left: `${(left / 320) * 100}%`,
    top: `${(top / 320) * 100}%`,
    transform: "translate(-50%, -50%)",
  };
}

const keywordPositions = [
  { left: "8%", top: "18%" },
  { right: "6%", top: "22%" },
  { left: "12%", bottom: "20%" },
];

function keywordStyle(index: number) {
  return keywordPositions[index % keywordPositions.length] || keywordPositions[0];
}

function revealStyle(order: number) {
  return { "--gec-delay": `${order * 0.1}s` };
}

function onStepHover(index: number) {
  activeStep.value = index;
  if (props.data.steps[index]?.id === "review") {
    triggerRadarBurst();
  }
}

function onStepLeave() {
  // 保持最后选中的高亮，不重置
}

function onStepTap(index: number) {
  activeStep.value = index;
  if (props.data.steps[index]?.id === "review") {
    triggerRadarBurst();
  }
}

function triggerRadarBurst() {
  radarBurst.value = false;
  if (burstTimer) clearTimeout(burstTimer);
  requestAnimationFrame(() => {
    radarBurst.value = true;
    burstTimer = setTimeout(() => {
      radarBurst.value = false;
    }, 700);
  });
}

function animateScore(target: number) {
  if (scoreTimer) clearInterval(scoreTimer);
  const duration = 1200;
  const start = Date.now();
  const from = 0;
  scoreTimer = setInterval(() => {
    const t = Math.min((Date.now() - start) / duration, 1);
    const eased = 1 - (1 - t) ** 3;
    displayScore.value = Math.round(from + (target - from) * eased);
    if (t >= 1 && scoreTimer) {
      clearInterval(scoreTimer);
      scoreTimer = null;
    }
  }, 16);
}

function setupScrollReveal() {
  // #ifdef H5
  const raw = sectionRef.value;
  const el = (raw && "$el" in raw ? raw.$el : raw) as HTMLElement | null;
  if (!el || typeof IntersectionObserver === "undefined") {
    isVisible.value = true;
    animateScore(props.data.dashboard.score);
    return;
  }
  const io = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          isVisible.value = true;
          animateScore(props.data.dashboard.score);
          io.disconnect();
        }
      });
    },
    { threshold: 0.15, rootMargin: "0px 0px -40px 0px" },
  );
  io.observe(el);
  // #endif
  // #ifndef H5
  isVisible.value = true;
  animateScore(props.data.dashboard.score);
  // #endif
}

watch(
  () => props.data.dashboard.score,
  (v) => {
    if (isVisible.value) animateScore(v);
  },
);

onMounted(() => {
  setupScrollReveal();
});

onUnmounted(() => {
  if (scoreTimer) clearInterval(scoreTimer);
  if (burstTimer) clearTimeout(burstTimer);
});
</script>

<style scoped>
.gec {
  width: 100%;
  background: #f8fafc;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(40rpx);
  transition:
    opacity 0.7s ease,
    transform 0.7s ease;
}
.gec--visible {
  opacity: 1;
  transform: translateY(0);
}

.gec__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 64rpx 32rpx 80rpx;
  box-sizing: border-box;
}

.gec__header {
  margin-bottom: 48rpx;
  text-align: center;
}
.gec__title {
  display: block;
  font-family: Inter, ui-sans-serif, system-ui, sans-serif;
  font-size: 40rpx;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}
.gec__subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
  color: #94a3b8;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.gec__body {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}
/* #ifdef H5 */
@media (min-width: 900px) {
  .gec__body {
    flex-direction: row;
    align-items: stretch;
    gap: 32px;
  }
  .gec__flow {
    width: 40%;
  }
  .gec__dashboard {
    width: 60%;
  }
}
/* #endif */

/* Scroll reveal */
.gec-reveal {
  opacity: 0;
  transform: translateX(-24rpx);
  transition:
    opacity 0.55s ease var(--gec-delay, 0s),
    transform 0.55s cubic-bezier(0.22, 1, 0.36, 1) var(--gec-delay, 0s);
}
.gec__dashboard.gec-reveal {
  transform: translateX(24rpx) scale(0.96);
}
.gec--visible .gec-reveal {
  opacity: 1;
  transform: translateX(0) scale(1);
}
.gec-float.gec-reveal {
  transform: translateY(16rpx);
}
.gec--visible .gec-float.gec-reveal {
  transform: translateY(0);
}

/* 左侧流程线 */
.gec__flow {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  padding-left: 8rpx;
}
.gec__line-track {
  position: absolute;
  left: 36rpx;
  top: 48rpx;
  bottom: 48rpx;
  width: 4rpx;
  border-radius: 999rpx;
  overflow: hidden;
  z-index: 0;
}
.gec__line-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(59, 130, 246, 0.08) 0%,
    rgba(99, 102, 241, 0.45) 50%,
    rgba(59, 130, 246, 0.08) 100%
  );
}
.gec__line-dot {
  position: absolute;
  left: 50%;
  width: 16rpx;
  height: 16rpx;
  margin-left: -8rpx;
  border-radius: 50%;
  background: #60a5fa;
  box-shadow:
    0 0 12rpx rgba(96, 165, 250, 0.9),
    0 0 24rpx rgba(59, 130, 246, 0.5);
  animation: flow-dot 3.2s ease-in-out infinite;
}
@keyframes flow-dot {
  0% {
    top: 0%;
    opacity: 0.4;
  }
  15% {
    opacity: 1;
  }
  85% {
    opacity: 1;
  }
  100% {
    top: calc(100% - 16rpx);
    opacity: 0.4;
  }
}

/* 步骤卡片 */
.gec-step {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 20rpx;
  cursor: pointer;
  transition: transform 0.25s ease;
}
.gec-step__node {
  position: relative;
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
}
.gec-step__dot {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: #ffffff;
  border: 2rpx solid #e2e8f0;
  transition:
    box-shadow 0.3s ease,
    border-color 0.3s ease;
}
.gec-step--active .gec-step__dot {
  border-color: #3b82f6;
  box-shadow:
    0 0 0 6rpx rgba(59, 130, 246, 0.15),
    0 0 24rpx rgba(59, 130, 246, 0.35);
}
.gec-step__icon {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 32rpx;
  height: 32rpx;
  color: #64748b;
  transition: color 0.25s ease;
}
.gec-step--active .gec-step__icon {
  color: #2563eb;
}

/* mic icon */
.gec-step__icon--mic::before {
  content: "";
  position: absolute;
  width: 14rpx;
  height: 22rpx;
  left: 50%;
  top: 2rpx;
  transform: translateX(-50%);
  border: 3rpx solid currentColor;
  border-radius: 8rpx;
}
.gec-step__icon--mic::after {
  content: "";
  position: absolute;
  width: 20rpx;
  height: 10rpx;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  border: 3rpx solid currentColor;
  border-top: none;
  border-radius: 0 0 12rpx 12rpx;
}

.gec-step__wave-bars {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: 4rpx;
  height: 24rpx;
}
.gec-step__wave-bar {
  width: 5rpx;
  height: 12rpx;
  border-radius: 3rpx;
  background: currentColor;
  animation: wave-bar 0.85s ease-in-out infinite;
}
.gec-step__wave-bar:nth-child(2) {
  height: 20rpx;
}
.gec-step__wave-bar:nth-child(3) {
  height: 14rpx;
}
@keyframes wave-bar {
  0%,
  100% {
    transform: scaleY(0.55);
    opacity: 0.6;
  }
  50% {
    transform: scaleY(1);
    opacity: 1;
  }
}

/* radar icon */
.gec-step__icon--radar {
  border: 3rpx solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
}
.gec-step__icon--radar::after {
  content: "";
  position: absolute;
  width: 50%;
  height: 50%;
  left: 50%;
  top: 50%;
  transform-origin: 0 0;
  border-top: 3rpx solid currentColor;
  animation: radar-spin 2s linear infinite;
}
@keyframes radar-spin {
  to {
    transform: rotate(360deg);
  }
}

.gec-step__card {
  flex: 1;
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.9);
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.04);
  transition:
    box-shadow 0.3s ease,
    border-color 0.3s ease,
    transform 0.25s ease;
}
.gec-step--active .gec-step__card {
  border-color: rgba(59, 130, 246, 0.35);
  box-shadow:
    0 12rpx 40rpx rgba(37, 99, 235, 0.14),
    0 0 0 1rpx rgba(59, 130, 246, 0.08);
  transform: translateX(4rpx);
}
.gec-step__index {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #3b82f6;
  letter-spacing: 0.08em;
}
.gec-step__title {
  display: block;
  margin-top: 6rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}
.gec-step__desc {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.55;
  color: #64748b;
}

/* 右侧看板 */
.gec-board {
  height: 100%;
  min-height: 560rpx;
  padding: 32rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.55);
  border: 1rpx solid rgba(255, 255, 255, 0.85);
  box-shadow:
    0 24rpx 64rpx rgba(15, 23, 42, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-sizing: border-box;
}
.gec-board__head {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.gec-board__label {
  font-size: 28rpx;
  font-weight: 600;
  color: #475569;
}
.gec-board__score-wrap {
  display: flex;
  flex-direction: row;
  align-items: baseline;
}
.gec-board__score {
  font-family: Inter, ui-sans-serif, system-ui, sans-serif;
  font-size: 56rpx;
  font-weight: 800;
  color: #1d4ed8;
  font-variant-numeric: tabular-nums;
}
.gec-board__score-unit {
  margin-left: 4rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #64748b;
}

.gec-board__chart-wrap {
  position: relative;
  min-height: 480rpx;
}

/* 雷达 */
.gec-radar {
  position: relative;
  width: 100%;
  max-width: 520rpx;
  margin: 0 auto;
  aspect-ratio: 1;
  transition: transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.gec-radar--burst {
  animation: radar-burst 0.65s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes radar-burst {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.12);
    filter: drop-shadow(0 0 28rpx rgba(59, 130, 246, 0.45));
  }
  100% {
    transform: scale(1);
  }
}
.gec-radar__svg {
  width: 100%;
  height: 100%;
  display: block;
}
.gec-radar__grid {
  fill: rgba(59, 130, 246, 0.04);
  stroke: rgba(148, 163, 184, 0.25);
  stroke-width: 1;
}
.gec-radar__axis {
  stroke: rgba(148, 163, 184, 0.2);
  stroke-width: 1;
}
.gec-radar__area {
  fill: rgba(59, 130, 246, 0.22);
  stroke: #3b82f6;
  stroke-width: 2;
  transition: all 0.5s ease;
}
.gec-radar--burst .gec-radar__area {
  fill: rgba(59, 130, 246, 0.35);
  stroke-width: 2.5;
}
.gec-radar__point {
  fill: #ffffff;
  stroke: #2563eb;
  stroke-width: 2;
}
.gec-radar__label {
  position: absolute;
  font-size: 20rpx;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
  pointer-events: none;
}

/* 关键词 */
.gec-keyword {
  position: absolute;
  z-index: 2;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  backdrop-filter: blur(8px);
}
.gec-keyword--positive {
  background: rgba(219, 234, 254, 0.85);
  border: 1rpx solid rgba(96, 165, 250, 0.4);
}
.gec-keyword--warning {
  background: rgba(254, 243, 199, 0.9);
  border: 1rpx solid rgba(251, 191, 36, 0.45);
}
.gec-keyword--neutral {
  background: rgba(241, 245, 249, 0.9);
  border: 1rpx solid #e2e8f0;
}
.gec-keyword__text {
  font-size: 22rpx;
  font-weight: 600;
  color: #334155;
}

/* 悬浮卡 */
.gec-float {
  position: absolute;
  z-index: 3;
  max-width: 240rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.72);
  border: 1rpx solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8rpx 28rpx rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(12px);
  animation: float-breathe 4s ease-in-out infinite;
}
.gec-float--1 {
  left: 0;
  top: 8%;
  animation-delay: 0s;
}
.gec-float--2 {
  right: 0;
  top: 42%;
  animation-delay: 1.2s;
}
.gec-float--3 {
  left: 4%;
  bottom: 6%;
  animation-delay: 2.4s;
}
.gec-float__text {
  font-size: 22rpx;
  font-weight: 600;
  color: #334155;
  line-height: 1.4;
}
@keyframes float-breathe {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8rpx);
  }
}
</style>
