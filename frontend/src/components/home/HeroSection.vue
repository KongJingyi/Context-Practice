<template>
  <view class="hero">
    <view class="hero__orb hero__orb--tr" aria-hidden="true" />
    <view class="hero__orb hero__orb--bl" aria-hidden="true" />

    <view class="hero__inner">
      <!-- 左侧文案区 -->
      <view class="hero__left">
        <view class="hero__badge">
          <view class="hero__badge-dot" />
          <text class="hero__badge-text">{{ content.badge }}</text>
        </view>

        <view class="hero__title-wrap">
          <!-- #ifdef H5 -->
          <component
            :is="MotionDiv"
            class="hero__title-block"
            :initial="{ opacity: 0, y: 28 }"
            :animate="{ opacity: 1, y: 0 }"
            :transition="lineTransition(0)"
          >
            <h2 class="hero__title-line">{{ content.title.line1 }}</h2>
            <h2 class="hero__title-line">
              <span class="hero__title-plain">{{ content.title.line2Prefix }}</span>
              <span class="hero__title-accent">{{ content.title.line2Highlight }}</span>
            </h2>
          </component>
          <!-- #endif -->
          <!-- #ifndef H5 -->
          <view class="hero__title-block hero__rise" style="animation-delay: 0ms">
            <text class="hero__title-line">{{ content.title.line1 }}</text>
            <text class="hero__title-line">
              <text class="hero__title-plain">{{ content.title.line2Prefix }}</text>
              <text class="hero__title-accent">{{ content.title.line2Highlight }}</text>
            </text>
          </view>
          <!-- #endif -->
        </view>

        <!-- #ifdef H5 -->
        <component
          :is="MotionP"
          class="hero__subtitle"
          :initial="{ opacity: 0, y: 20 }"
          :animate="{ opacity: 1, y: 0 }"
          :transition="lineTransition(1)"
        >
          {{ content.subtitle }}
        </component>
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <text class="hero__subtitle hero__rise" style="animation-delay: 120ms">
          {{ content.subtitle }}
        </text>
        <!-- #endif -->

        <!-- #ifdef H5 -->
        <component
          :is="MotionDiv"
          class="hero__actions"
          :initial="{ opacity: 0, y: 18 }"
          :animate="{ opacity: 1, y: 0 }"
          :transition="lineTransition(2)"
        >
          <button type="button" class="hero__btn hero__btn--primary" @click="emit('book')">
            <text class="hero__btn-text hero__btn-text--white">{{ content.primaryButtonText }}</text>
          </button>
          <HeroActiveOrderReminder
            :orders="activeOrders"
            @view-all="emit('view-orders')"
            @open="emit('open-order', $event)"
            @action="emit('order-action', $event)"
          />
        </component>
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <view class="hero__actions hero__rise" style="animation-delay: 240ms">
          <view class="hero__btn hero__btn--primary" @tap="emit('book')">
            <text class="hero__btn-text hero__btn-text--white">{{ content.primaryButtonText }}</text>
          </view>
          <HeroActiveOrderReminder
            :orders="activeOrders"
            @view-all="emit('view-orders')"
            @open="emit('open-order', $event)"
            @action="emit('order-action', $event)"
          />
        </view>
        <!-- #endif -->

        <view v-if="statItems.length" class="hero__stats hero__rise" :style="mpDelay(3)">
          <view
            v-for="(stat, idx) in statItems"
            :key="stat.id"
            class="hero__stat"
            :class="{ 'hero__stat--border': idx > 0 }"
          >
            <view class="hero__stat-icon-wrap">
              <view class="hero__stat-icon" :class="`hero__stat-icon--${stat.icon || 'users'}`" />
            </view>
            <text class="hero__stat-value">{{ formatStatValue(stat.value) }}</text>
            <text class="hero__stat-label">{{ stat.label }}</text>
          </view>
        </view>
      </view>

      <!-- 右侧 1v1 卡片 -->
      <view class="hero__right hero__rise" :style="mpDelay(4)">
        <view class="hero__card-glow" aria-hidden="true" />
        <view class="hero__card">
          <view class="hero__card-top">
            <view class="hero__live">
              <view class="hero__live-dot" />
              <text class="hero__live-text">1v1 视频通话</text>
            </view>
            <text class="hero__timer">00:42</text>
          </view>

          <view class="hero__screen">
            <view class="hero__screen-bg" />
            <view class="hero__pip">
              <text class="hero__pip-text">你</text>
            </view>
            <view class="hero__coach">
              <view class="hero__coach-avatar">
                <text class="hero__coach-initial">AI</text>
              </view>
              <text class="hero__coach-name">语境教练</text>
              <text class="hero__coach-hint">正在聆听你的表达</text>
            </view>
          </view>

          <view class="hero__wave-box">
            <view
              v-for="i in 16"
              :key="i"
              class="hero__wave-bar"
              :style="{ animationDelay: `${(i - 1) * 0.07}s` }"
            />
          </view>

          <view class="hero__card-actions">
            <view class="hero__action-btn hero__action-btn--mute" />
            <view class="hero__action-btn hero__action-btn--end" />
            <view class="hero__action-btn hero__action-btn--cam" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import HeroActiveOrderReminder from "@/components/home/HeroActiveOrderReminder.vue";
import type { HeroContentProps, HeroStatItem, HeroStatsCount, HeroTitleContent } from "@/types/home/hero";
import type { MyOrderItem } from "@/types/orders";

// #ifdef H5
import { motion } from "motion-v";

const MotionDiv = motion.div;
const MotionP = motion.p;

function lineTransition(index: number) {
  return {
    duration: 0.55,
    delay: index * 0.1,
    ease: [0.22, 1, 0.36, 1] as const,
  };
}
// #endif

const defaultTitle: HeroTitleContent = {
  line1: "打破表达困境，",
  line2Prefix: "让每一次开口都",
  line2Highlight: "充满力量",
};

const defaultContent: Required<HeroContentProps> = {
  badge: "语境智练 · 沟通力提升",
  title: defaultTitle,
  subtitle: "沉浸式场景训练，帮你在真实语境中练表达、练气场、练应变。",
  primaryButtonText: "立即预约",
};

const props = withDefaults(
  defineProps<{
    content?: HeroContentProps;
    statsCount?: HeroStatsCount;
    activeOrders?: MyOrderItem[];
  }>(),
  {
    content: () => ({}),
    statsCount: undefined,
    activeOrders: () => [],
  },
);

const emit = defineEmits<{
  (e: "book"): void;
  (e: "view-orders"): void;
  (e: "open-order", item: MyOrderItem): void;
  (e: "order-action", item: MyOrderItem): void;
}>();

const content = computed(() => ({
  badge: props.content?.badge ?? defaultContent.badge,
  title: { ...defaultTitle, ...props.content?.title },
  subtitle: props.content?.subtitle ?? defaultContent.subtitle,
  primaryButtonText: props.content?.primaryButtonText ?? defaultContent.primaryButtonText,
}));

const statItems = computed<HeroStatItem[]>(() => {
  const s = props.statsCount;
  if (!s) return [];
  if (s.items?.length) return s.items;
  const legacy: HeroStatItem[] = [];
  if (s.learners != null) {
    legacy.push({ id: "learners", value: s.learners, label: "累计学员", icon: "users" });
  }
  if (s.sessions != null) {
    legacy.push({ id: "sessions", value: s.sessions, label: "训练场次", icon: "sessions" });
  }
  if (s.coaches != null) {
    legacy.push({ id: "coaches", value: s.coaches, label: "专业教练", icon: "coaches" });
  }
  return legacy;
});

function formatStatValue(value: number | string) {
  if (typeof value === "string") return value;
  if (value >= 10000) {
    return `${(value / 10000).toFixed(1).replace(/\.0$/, "")}万+`;
  }
  return `${value}+`;
}

function mpDelay(lineIndex: number) {
  // #ifndef H5
  return { animationDelay: `${lineIndex * 100}ms` };
  // #endif
  return {};
}
</script>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 32rpx;
  border-radius: 32rpx;
  background: #ffffff;
  border: 1rpx solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 4rpx 40rpx rgba(15, 23, 42, 0.04);
}

.hero__orb {
  position: absolute;
  width: 520rpx;
  height: 520rpx;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
  animation: orb-breathe 9s ease-in-out infinite;
}
.hero__orb--tr {
  top: -200rpx;
  right: -120rpx;
  background: #eef2ff;
}
.hero__orb--bl {
  bottom: -220rpx;
  left: -140rpx;
  background: #e0f2fe;
  animation-delay: -4.5s;
}

@keyframes orb-breathe {
  0%,
  100% {
    opacity: 0.65;
    transform: scale(1);
  }
  50% {
    opacity: 0.95;
    transform: scale(1.06);
  }
}

.hero__inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 48rpx;
  padding: 48rpx 40rpx 44rpx;
  box-sizing: border-box;
}

/* #ifdef H5 */
@media (min-width: 960px) {
  .hero__inner {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 40px;
    padding: 56px 48px 52px;
  }
}
/* #endif */

.hero__left {
  flex: 1;
  min-width: 0;
}

.hero__badge {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 28rpx;
  margin-bottom: 32rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #f0f7ff 0%, #e8f2ff 100%);
  border: 1rpx solid rgba(147, 197, 253, 0.35);
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.08);
}
.hero__badge-dot {
  width: 12rpx;
  height: 12rpx;
  margin-right: 12rpx;
  border-radius: 50%;
  background: #3b82f6;
}
.hero__badge-text {
  font-family: Inter, ui-sans-serif, system-ui, sans-serif;
  font-size: 24rpx;
  font-weight: 600;
  color: #1d4ed8;
  letter-spacing: 0.02em;
}

.hero__title-wrap {
  margin-bottom: 28rpx;
}
.hero__title-block {
  display: block;
}
.hero__title-line {
  display: block;
  font-family: Inter, ui-sans-serif, system-ui, sans-serif;
  font-size: 52rpx;
  font-weight: 800;
  line-height: 1.22;
  letter-spacing: -0.02em;
  color: #0f172a;
  margin: 0;
  padding: 0;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .hero__title-line {
    font-size: 44px;
    line-height: 1.18;
  }
}
/* #endif */

.hero__title-plain {
  color: #0f172a;
}
.hero__title-accent {
  background: linear-gradient(105deg, #2563eb 0%, #4f46e5 55%, #6366f1 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero__subtitle {
  display: block;
  max-width: 560rpx;
  margin-bottom: 40rpx;
  font-family: Inter, ui-sans-serif, system-ui, sans-serif;
  font-size: 28rpx;
  font-weight: 400;
  line-height: 1.75;
  color: #64748b;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .hero__subtitle {
    font-size: 17px;
    max-width: 420px;
  }
}
/* #endif */

.hero__actions {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-bottom: 28rpx;
}

.hero__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 96rpx;
  padding: 0 48rpx;
  border-radius: 20rpx;
  border: none;
  box-sizing: border-box;
}
.hero__btn--primary {
  background: linear-gradient(180deg, #1e4fc7 0%, #1d4ed8 100%);
  color: #ffffff;
  animation: btn-glow 3s ease-in-out infinite;
}
.hero__btn--primary::after {
  border: none;
}
.hero__btn-text {
  font-size: 30rpx;
  font-weight: 600;
}
.hero__btn-text--white {
  color: #ffffff;
}

@keyframes btn-glow {
  0%,
  100% {
    box-shadow: 0 8rpx 28rpx rgba(29, 78, 216, 0.28);
  }
  50% {
    box-shadow:
      0 12rpx 36rpx rgba(29, 78, 216, 0.38),
      0 0 0 6rpx rgba(59, 130, 246, 0.12);
  }
}

/* 数据条 */
.hero__stats {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  padding: 28rpx 8rpx;
  border-radius: 20rpx;
  background: rgba(248, 250, 252, 0.85);
  border: 1rpx solid #f1f5f9;
}
.hero__stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16rpx;
  box-sizing: border-box;
}
.hero__stat--border {
  border-left: 1rpx solid #e2e8f0;
}
.hero__stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  margin-bottom: 12rpx;
  border-radius: 14rpx;
  background: #eff6ff;
  color: #2563eb;
}
.hero__stat-icon {
  width: 28rpx;
  height: 28rpx;
  position: relative;
  color: #2563eb;
}
.hero__stat-icon--users::before {
  content: "";
  position: absolute;
  width: 12rpx;
  height: 12rpx;
  border: 3rpx solid currentColor;
  border-radius: 50%;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}
.hero__stat-icon--users::after {
  content: "";
  position: absolute;
  width: 22rpx;
  height: 11rpx;
  border: 3rpx solid currentColor;
  border-bottom: none;
  border-radius: 12rpx 12rpx 0 0;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}
.hero__stat-icon--sessions {
  border: 3rpx solid currentColor;
  border-radius: 4rpx;
  box-sizing: border-box;
}
.hero__stat-icon--sessions::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-30%, -50%);
  border-style: solid;
  border-width: 5rpx 0 5rpx 8rpx;
  border-color: transparent transparent transparent currentColor;
}
.hero__stat-icon--coaches::before {
  content: "★";
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  line-height: 1;
  color: currentColor;
}
.hero__stat-value {
  font-family: Inter, ui-sans-serif, system-ui, sans-serif;
  font-size: 40rpx;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
}
.hero__stat-label {
  margin-top: 6rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #94a3b8;
}

/* 右侧卡片 */
.hero__right {
  position: relative;
  display: flex;
  justify-content: center;
  width: 100%;
  flex-shrink: 0;
}
/* #ifdef H5 */
@media (min-width: 960px) {
  .hero__right {
    width: 360px;
  }
}
/* #endif */

.hero__card-glow {
  position: absolute;
  inset: 8% 5%;
  border-radius: 40rpx;
  background: linear-gradient(135deg, rgba(147, 197, 253, 0.35), rgba(199, 210, 254, 0.25));
  filter: blur(24px);
  z-index: 0;
}

.hero__card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 620rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.78);
  border: 1rpx solid rgba(255, 255, 255, 0.95);
  box-shadow:
    0 24rpx 64rpx rgba(15, 23, 42, 0.07),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-sizing: border-box;
}

.hero__card-top {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.hero__live {
  display: flex;
  flex-direction: row;
  align-items: center;
}
.hero__live-dot {
  width: 12rpx;
  height: 12rpx;
  margin-right: 10rpx;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 6rpx rgba(34, 197, 94, 0.2);
  animation: live-pulse 2s ease-in-out infinite;
}
@keyframes live-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.65;
  }
}
.hero__live-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #475569;
}
.hero__timer {
  font-size: 22rpx;
  font-weight: 500;
  color: #94a3b8;
  font-variant-numeric: tabular-nums;
}

.hero__screen {
  position: relative;
  height: 280rpx;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
}
.hero__screen-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #dbeafe 0%, #e0e7ff 45%, #f0f9ff 100%);
}
.hero__pip {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 88rpx;
  height: 112rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.9);
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero__pip-text {
  font-size: 24rpx;
  font-weight: 600;
  color: #64748b;
}
.hero__coach {
  position: absolute;
  left: 50%;
  bottom: 28rpx;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.hero__coach-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffffff, #eff6ff);
  border: 3rpx solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 8rpx 28rpx rgba(37, 99, 235, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}
.hero__coach-initial {
  font-size: 32rpx;
  font-weight: 800;
  color: #2563eb;
}
.hero__coach-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #1e293b;
}
.hero__coach-hint {
  margin-top: 4rpx;
  font-size: 20rpx;
  color: rgba(30, 41, 59, 0.55);
}

.hero__wave-box {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: center;
  gap: 8rpx;
  height: 72rpx;
  padding: 16rpx 24rpx;
  margin-bottom: 24rpx;
  border-radius: 999rpx;
  background: rgba(239, 246, 255, 0.9);
  border: 1rpx solid rgba(191, 219, 254, 0.5);
  box-sizing: border-box;
}
.hero__wave-bar {
  width: 8rpx;
  height: 16rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #3b82f6, #6366f1);
  animation: wave 1.05s ease-in-out infinite;
}
@keyframes wave {
  0%,
  100% {
    height: 16rpx;
    opacity: 0.45;
  }
  50% {
    height: 52rpx;
    opacity: 1;
  }
}

.hero__card-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 28rpx;
}
.hero__action-btn {
  border-radius: 50%;
  background: #f1f5f9;
}
.hero__action-btn--mute,
.hero__action-btn--cam {
  width: 72rpx;
  height: 72rpx;
  opacity: 0.85;
}
.hero__action-btn--end {
  width: 88rpx;
  height: 88rpx;
  background: linear-gradient(180deg, #f87171, #ef4444);
  box-shadow: 0 8rpx 20rpx rgba(239, 68, 68, 0.35);
}

/* #ifndef H5 */
.hero__rise {
  opacity: 0;
  transform: translateY(24rpx);
  animation: hero-rise 0.55s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
@keyframes hero-rise {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
/* #endif */
</style>
