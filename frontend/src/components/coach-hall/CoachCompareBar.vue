<template>
  <view v-if="coaches.length" class="ccb" :class="{ 'ccb--show': coaches.length }">
    <view class="ccb-inner">
      <view class="ccb-chips">
        <view v-for="c in coaches" :key="c.id" class="ccb-chip">
          <text class="ccb-chip-name">{{ c.name }}</text>
          <text class="ccb-chip-x" @tap.stop="emit('remove', c.id)">×</text>
        </view>
        <text v-if="coaches.length < 2" class="ccb-hint">再选 1 位即可对比</text>
      </view>
      <view
        class="ccb-btn"
        :class="{ 'ccb-btn--on': coaches.length === 2 }"
        @tap="onCompare"
      >
        <text>开始对比</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { Coach } from "@/types/coach/hall";

const props = defineProps<{
  coaches: Coach[];
}>();

const emit = defineEmits<{
  (e: "remove", id: string): void;
  (e: "compare"): void;
}>();

function onCompare() {
  if (props.coaches.length === 2) emit("compare");
}
</script>

<style scoped>
.ccb {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 900;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-top: 1rpx solid #e2e8f0;
  box-shadow: 0 -8rpx 32rpx rgba(15, 23, 42, 0.1);
  transform: translateY(100%);
  animation: ccbSlide 0.35s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
@keyframes ccbSlide {
  to {
    transform: translateY(0);
  }
}
.ccb-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
}
.ccb-chips {
  flex: 1;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}
.ccb-chip {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #eff6ff;
  border: 1rpx solid #93c5fd;
}
.ccb-chip-name {
  font-size: 24rpx;
  font-weight: 600;
  color: #1d4ed8;
}
.ccb-chip-x {
  font-size: 28rpx;
  color: #64748b;
  line-height: 1;
}
.ccb-hint {
  font-size: 22rpx;
  color: #94a3b8;
}
.ccb-btn {
  flex-shrink: 0;
  padding: 16rpx 32rpx;
  border-radius: 14rpx;
  background: #e2e8f0;
  opacity: 0.6;
  pointer-events: none;
}
.ccb-btn--on {
  opacity: 1;
  pointer-events: auto;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.3);
}
.ccb-btn text {
  font-size: 26rpx;
  font-weight: 700;
  color: #64748b;
}
.ccb-btn--on text {
  color: #fff;
}
</style>
