<template>
  <view class="ptl">
    <view
      v-for="(step, i) in steps"
      :key="i"
      class="ptl-item"
      :class="{
        'ptl-item--done': step.done,
        'ptl-item--cur': step.current,
        'ptl-item--pending': !step.done && !step.current,
      }"
    >
      <view class="ptl-rail">
        <view class="ptl-dot" />
        <view v-if="i < steps.length - 1" class="ptl-line" />
      </view>
      <view class="ptl-body">
        <text class="ptl-title">{{ step.title }}</text>
        <text v-if="step.time" class="ptl-time">{{ step.time }}</text>
        <view v-if="step.content" class="ptl-feedback">
          <text>{{ step.content }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { ComplaintStep } from "@/types/review";

defineProps<{ steps: ComplaintStep[] }>();
</script>

<style scoped>
.ptl {
  padding: 16rpx 0;
}
.ptl-item {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  min-height: 100rpx;
}
.ptl-rail {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32rpx;
  flex-shrink: 0;
}
.ptl-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #e2e8f0;
  border: 4rpx solid #fff;
  box-shadow: 0 0 0 2rpx #e2e8f0;
  flex-shrink: 0;
}
.ptl-item--done .ptl-dot {
  background: #22c55e;
  box-shadow: 0 0 0 2rpx #bbf7d0;
}
.ptl-item--cur .ptl-dot {
  background: #f59e0b;
  box-shadow: 0 0 0 2rpx #fde68a;
  animation: ptlPulse 1.5s ease-in-out infinite;
}
@keyframes ptlPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.15);
  }
}
.ptl-line {
  flex: 1;
  width: 2rpx;
  min-height: 60rpx;
  background: #e2e8f0;
  margin: 4rpx 0;
}
.ptl-item--done .ptl-line {
  background: linear-gradient(180deg, #22c55e, #e2e8f0);
}
.ptl-body {
  flex: 1;
  padding-bottom: 24rpx;
}
.ptl-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
.ptl-item--pending .ptl-title {
  color: #94a3b8;
}
.ptl-time {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #64748b;
}
.ptl-feedback {
  margin-top: 12rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
}
.ptl-feedback text {
  font-size: 24rpx;
  color: #475569;
  line-height: 1.55;
}
</style>
