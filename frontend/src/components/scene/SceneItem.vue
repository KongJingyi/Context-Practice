<template>
  <view
    class="scene-card"
    :class="[`scene-card--${card.theme}`, { 'scene-card--hover': isHovered }]"
    :style="{ '--reveal-delay': `${revealDelay ?? 0}ms` }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
    @tap="emit('start', card)"
  >
    <view class="scene-card__visual">
      <image
        v-if="card.imageUrl"
        class="scene-card__image"
        :src="card.imageUrl"
        mode="aspectFill"
      />
      <view v-else class="scene-card__placeholder">
        <view class="scene-card__gradient" />
        <view class="scene-card__geo" aria-hidden="true">
          <view class="scene-card__geo-line" />
          <view class="scene-card__geo-ring" />
        </view>
        <view
          class="scene-card__icon"
          :class="[`scene-card__icon--${card.icon}`, { 'scene-card__icon--bounce': isHovered }]"
        />
      </view>
    </view>

    <view class="scene-card__body">
      <view class="scene-card__meta">
        <text class="scene-card__tag">{{ card.categoryLabel }}</text>
        <view class="scene-card__learners">
          <view class="scene-card__learners-icon" />
          <text class="scene-card__learners-text">{{ learnersLabel }} 人练过</text>
        </view>
      </view>

      <text class="scene-card__title">{{ card.title }}</text>

      <view v-if="card.featureTags.length" class="scene-card__features">
        <text
          v-for="(tag, i) in card.featureTags"
          :key="`${card.id}-ft-${i}`"
          class="scene-card__feature-tag"
        >
          {{ tag }}
        </text>
      </view>

      <text class="scene-card__desc">{{ card.description }}</text>

      <view class="scene-card__footer">
        <text class="scene-card__duration">{{ card.durationMinutes }} 分钟 / 课时</text>
        <view
          class="scene-card__arrow"
          :class="{ 'scene-card__arrow--shift': isHovered, 'scene-card__arrow--scale': isHovered }"
        >
          <text class="scene-card__arrow-icon">→</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import type { SceneCardData } from "@/types/scene/plaza";

const props = defineProps<{
  card: SceneCardData;
  revealDelay?: number;
}>();

const emit = defineEmits<{
  (e: "start", card: SceneCardData): void;
}>();

const isHovered = ref(false);

const learnersLabel = computed(() => {
  const n = props.card.learners;
  if (n >= 10000) {
    return `${(n / 10000).toFixed(1).replace(/\.0$/, "")}w+`;
  }
  if (n >= 1000) {
    return `${(n / 1000).toFixed(1).replace(/\.0$/, "")}k+`;
  }
  return `${n}+`;
});
</script>

<style scoped>
.scene-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 24rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 4rpx 14rpx rgba(15, 23, 42, 0.05);
  opacity: 0;
  transform: translateY(18rpx);
  animation: card-in 0.5s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  animation-delay: var(--reveal-delay, 0ms);
  transition:
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.35s ease;
  cursor: pointer;
  box-sizing: border-box;
}
.scene-card--hover {
  transform: translateY(-8px);
  box-shadow: 0 20rpx 40rpx rgba(15, 23, 42, 0.12);
  animation: none;
  opacity: 1;
}
@keyframes card-in {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.scene-card--blue {
  --theme-from: #dbeafe;
  --theme-to: #eff6ff;
  --theme-accent: #2563eb;
  --theme-tag-bg: #eff6ff;
  --theme-tag-text: #1d4ed8;
  --feature-bg: rgba(37, 99, 235, 0.08);
  --feature-text: #1e40af;
}
.scene-card--indigo {
  --theme-from: #e0e7ff;
  --theme-to: #eef2ff;
  --theme-accent: #4f46e5;
  --theme-tag-bg: #eef2ff;
  --theme-tag-text: #4338ca;
  --feature-bg: rgba(79, 70, 229, 0.08);
  --feature-text: #3730a3;
}
.scene-card--orange {
  --theme-from: #ffedd5;
  --theme-to: #fff7ed;
  --theme-accent: #ea580c;
  --theme-tag-bg: #fff7ed;
  --theme-tag-text: #c2410c;
  --feature-bg: rgba(234, 88, 12, 0.1);
  --feature-text: #9a3412;
}
.scene-card--rose {
  --theme-from: #ffe4e6;
  --theme-to: #fff1f2;
  --theme-accent: #e11d48;
  --theme-tag-bg: #fff1f2;
  --theme-tag-text: #be123c;
  --feature-bg: rgba(225, 29, 72, 0.08);
  --feature-text: #9f1239;
}
.scene-card--emerald {
  --theme-from: #d1fae5;
  --theme-to: #ecfdf5;
  --theme-accent: #059669;
  --theme-tag-bg: #ecfdf5;
  --theme-tag-text: #047857;
  --feature-bg: rgba(5, 150, 105, 0.08);
  --feature-text: #065f46;
}
.scene-card--amber {
  --theme-from: #fef3c7;
  --theme-to: #fffbeb;
  --theme-accent: #d97706;
  --theme-tag-bg: #fffbeb;
  --theme-tag-text: #b45309;
  --feature-bg: rgba(217, 119, 6, 0.1);
  --feature-text: #92400e;
}

.scene-card__visual {
  position: relative;
  height: 0;
  padding-bottom: 52%;
  overflow: hidden;
  border-radius: 24rpx 24rpx 0 0;
}
.scene-card__image,
.scene-card__placeholder {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.scene-card__placeholder {
  overflow: hidden;
}
.scene-card__gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, var(--theme-from) 0%, var(--theme-to) 72%);
  transition: transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
  transform-origin: center;
}
.scene-card--hover .scene-card__gradient {
  transform: scale(1.08);
}
.scene-card__geo-line {
  position: absolute;
  width: 120%;
  height: 2rpx;
  top: 38%;
  left: -10%;
  background: linear-gradient(90deg, transparent, var(--theme-accent), transparent);
  opacity: 0.12;
  transform: rotate(-5deg);
}
.scene-card__geo-ring {
  position: absolute;
  width: 130rpx;
  height: 130rpx;
  border: 2rpx solid var(--theme-accent);
  border-radius: 50%;
  opacity: 0.1;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.scene-card__icon {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 76rpx;
  height: 76rpx;
  color: var(--theme-accent);
  opacity: 0.32;
  transition: transform 0.35s ease;
}
.scene-card__icon--bounce {
  transform: translate(-50%, calc(-50% - 6rpx));
  opacity: 0.44;
}

.scene-card__icon--mic::before {
  content: "";
  position: absolute;
  width: 26rpx;
  height: 42rpx;
  left: 50%;
  top: 6rpx;
  transform: translateX(-50%);
  border: 4rpx solid currentColor;
  border-radius: 13rpx;
}
.scene-card__icon--mic::after {
  content: "";
  position: absolute;
  width: 42rpx;
  height: 18rpx;
  left: 50%;
  bottom: 4rpx;
  transform: translateX(-50%);
  border: 4rpx solid currentColor;
  border-top: none;
  border-radius: 0 0 18rpx 18rpx;
}
.scene-card__icon--projector {
  border: 4rpx solid currentColor;
  border-radius: 6rpx;
  height: 34rpx;
  top: 20rpx;
}
.scene-card__icon--spark::before {
  content: "✦";
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
}
.scene-card__icon--pressure::before {
  content: "";
  position: absolute;
  width: 100%;
  height: 6rpx;
  top: 50%;
  left: 0;
  background: currentColor;
  box-shadow: 0 -14rpx 0 currentColor, 0 14rpx 0 currentColor;
}
.scene-card__icon--chat {
  border: 4rpx solid currentColor;
  border-radius: 10rpx;
  height: 38rpx;
}
.scene-card__icon--conflict::before,
.scene-card__icon--conflict::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 26rpx;
  border-bottom: 4rpx solid currentColor;
  border-radius: 50%;
}
.scene-card__icon--conflict::before {
  top: 16rpx;
  transform: rotate(-8deg);
}
.scene-card__icon--conflict::after {
  bottom: 16rpx;
  transform: rotate(8deg);
}

.scene-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 22rpx 22rpx 20rpx;
  box-sizing: border-box;
}
.scene-card__meta {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 14rpx;
}
.scene-card__tag {
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 600;
  color: var(--theme-tag-text);
  background: var(--theme-tag-bg);
}
.scene-card__learners {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
}
.scene-card__learners-icon {
  width: 22rpx;
  height: 22rpx;
  border: 2rpx solid #94a3b8;
  border-radius: 50%;
  position: relative;
}
.scene-card__learners-icon::after {
  content: "";
  position: absolute;
  width: 12rpx;
  height: 7rpx;
  left: 50%;
  bottom: -5rpx;
  transform: translateX(-50%);
  border: 2rpx solid #94a3b8;
  border-top: none;
  border-radius: 6rpx 6rpx 0 0;
}
.scene-card__learners-text {
  font-size: 20rpx;
  color: #94a3b8;
}
.scene-card__title {
  display: block;
  font-family: Inter, "PingFang SC", ui-sans-serif, system-ui, sans-serif;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  line-height: 1.35;
}
.scene-card__features {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}
.scene-card__feature-tag {
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 500;
  color: var(--feature-text);
  background: var(--feature-bg);
  border: 1rpx solid rgba(148, 163, 184, 0.2);
}
.scene-card__desc {
  display: block;
  margin-top: 12rpx;
  flex: 1;
  font-size: 22rpx;
  line-height: 1.55;
  color: #64748b;
}
.scene-card__footer {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-top: 18rpx;
  padding-top: 14rpx;
  border-top: 1rpx solid #f1f5f9;
}
.scene-card__duration {
  font-size: 22rpx;
  font-weight: 500;
  color: #64748b;
}
.scene-card__arrow {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    transform 0.32s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.32s ease;
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.22);
}
.scene-card__arrow--shift {
  transform: translateX(8px);
}
.scene-card__arrow--scale {
  box-shadow: 0 8rpx 22rpx rgba(37, 99, 235, 0.38);
}
.scene-card__arrow-icon {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}
</style>
