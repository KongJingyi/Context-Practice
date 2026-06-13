<template>
  <view v-if="visible" class="cmm-mask" @tap="onMaskTap">
    <view class="cmm" @tap.stop>
      <view v-if="phase === 'scanning'" class="cmm-scan">
        <text class="cmm-title">AI 智能匹配</text>
        <text class="cmm-sub">正在分析您的训练目标…</text>
        <view class="cmm-radar">
          <view class="cmm-radar-ring cmm-radar-ring--1" />
          <view class="cmm-radar-ring cmm-radar-ring--2" />
          <view class="cmm-radar-ring cmm-radar-ring--3" />
          <view class="cmm-radar-sweep" />
          <view class="cmm-radar-core">
            <text class="cmm-radar-ico">◎</text>
          </view>
        </view>
        <view class="cmm-dots">
          <view class="cmm-dot cmm-dot--a" />
          <view class="cmm-dot cmm-dot--b" />
          <view class="cmm-dot cmm-dot--c" />
        </view>
      </view>

      <view v-else class="cmm-result">
        <text class="cmm-title">为您找到 {{ items.length }} 位最适配专家</text>
        <text class="cmm-sub">基于场景与训练目标智能推荐</text>
        <scroll-view scroll-x class="cmm-scroll" :show-scrollbar="false">
          <view class="cmm-cards">
            <view
              v-for="item in items"
              :key="item.coach.id"
              class="cmm-card"
              @tap="emit('select', item.coach)"
            >
              <view class="cmm-card-glow" />
              <text class="cmm-match-badge">{{ item.matchPercent }}% 匹配</text>
              <view class="cmm-card-avatar">
                <text>{{ item.coach.name.slice(0, 1) }}</text>
              </view>
              <text class="cmm-card-name">{{ item.coach.name }}</text>
              <text class="cmm-card-title">{{ item.coach.jobTitle }}</text>
              <text v-if="item.highlightTag" class="cmm-card-tag">{{ item.highlightTag }}</text>
              <text class="cmm-card-reason">{{ item.reason }}</text>
            </view>
          </view>
        </scroll-view>
        <view class="cmm-close-btn" @tap="emit('close')">
          <text>关闭</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { SmartMatchItem } from "@/types/coach/hall";

defineProps<{
  visible: boolean;
  phase: "scanning" | "result";
  items: SmartMatchItem[];
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "select", coach: import("@/types/coach/hall").Coach): void;
}>();

function onMaskTap() {
  emit("close");
}
</script>

<style scoped>
.cmm-mask {
  position: fixed;
  inset: 0;
  z-index: 1500;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  box-sizing: border-box;
}
.cmm {
  width: 100%;
  max-width: 680rpx;
  padding: 40rpx 32rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 24rpx 64rpx rgba(15, 23, 42, 0.2);
}
.cmm-title {
  display: block;
  text-align: center;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.cmm-sub {
  display: block;
  text-align: center;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #64748b;
}

.cmm-radar {
  position: relative;
  width: 280rpx;
  height: 280rpx;
  margin: 40rpx auto 24rpx;
}
.cmm-radar-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2rpx solid rgba(37, 99, 235, 0.2);
}
.cmm-radar-ring--2 {
  inset: 15%;
}
.cmm-radar-ring--3 {
  inset: 30%;
}
.cmm-radar-sweep {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(
    from 0deg,
    transparent 0deg,
    rgba(37, 99, 235, 0.35) 60deg,
    transparent 120deg
  );
  animation: cmmSweep 2s linear infinite;
}
@keyframes cmmSweep {
  to {
    transform: rotate(360deg);
  }
}
.cmm-radar-core {
  position: absolute;
  inset: 38%;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.4);
}
.cmm-radar-ico {
  font-size: 36rpx;
  color: #fff;
}

.cmm-dots {
  display: flex;
  justify-content: center;
  gap: 12rpx;
}
.cmm-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #93c5fd;
  animation: cmmBounce 1.2s ease-in-out infinite;
}
.cmm-dot--b {
  animation-delay: 0.15s;
}
.cmm-dot--c {
  animation-delay: 0.3s;
}
@keyframes cmmBounce {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-8rpx);
    opacity: 1;
  }
}

.cmm-scroll {
  margin: 28rpx 0 20rpx;
  white-space: nowrap;
}
.cmm-cards {
  display: inline-flex;
  flex-direction: row;
  gap: 20rpx;
  padding: 8rpx 4rpx 16rpx;
}
.cmm-card {
  position: relative;
  width: 280rpx;
  flex-shrink: 0;
  padding: 24rpx 20rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 2rpx solid #fcd34d;
  box-shadow: 0 8rpx 28rpx rgba(234, 179, 8, 0.25);
  overflow: hidden;
}
.cmm-card-glow {
  position: absolute;
  inset: -2rpx;
  border-radius: 20rpx;
  background: linear-gradient(
    135deg,
    rgba(251, 191, 36, 0.35),
    rgba(37, 99, 235, 0.15),
    rgba(251, 191, 36, 0.35)
  );
  animation: cmmGlow 2.5s linear infinite;
  z-index: 0;
  opacity: 0.6;
}
@keyframes cmmGlow {
  0% {
    filter: hue-rotate(0deg);
  }
  100% {
    filter: hue-rotate(30deg);
  }
}
.cmm-match-badge {
  position: relative;
  z-index: 1;
  display: inline-block;
  font-size: 20rpx;
  font-weight: 800;
  color: #b45309;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  margin-bottom: 12rpx;
}
.cmm-card-avatar {
  position: relative;
  z-index: 1;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #dbeafe, #eff6ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 800;
  color: #2563eb;
  margin-bottom: 10rpx;
}
.cmm-card-name {
  position: relative;
  z-index: 1;
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}
.cmm-card-title {
  position: relative;
  z-index: 1;
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin: 4rpx 0 8rpx;
  white-space: normal;
}
.cmm-card-tag {
  position: relative;
  z-index: 1;
  display: block;
  font-size: 20rpx;
  color: #2563eb;
  font-weight: 600;
  margin-bottom: 6rpx;
}
.cmm-card-reason {
  position: relative;
  z-index: 1;
  display: block;
  font-size: 22rpx;
  color: #475569;
  line-height: 1.45;
  white-space: normal;
}
.cmm-close-btn {
  padding: 18rpx;
  border-radius: 14rpx;
  background: #f1f5f9;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #475569;
}
</style>
