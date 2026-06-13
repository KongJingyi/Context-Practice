<template>
  <view class="gt">
    <text class="gt-title">成长路径</text>
    <view class="gt-line" />
    <view
      v-for="(m, i) in milestones"
      :key="m.id"
      class="gt-item"
      :class="{ 'gt-item--break': m.isBreakthrough, 'gt-item--show': visible }"
      :style="{ transitionDelay: `${i * 0.1}s` }"
    >
      <view class="gt-dot-wrap">
        <view class="gt-dot" :class="{ 'gt-dot--break': m.isBreakthrough }" />
      </view>
      <view class="gt-body">
        <text class="gt-date">{{ m.date }}</text>
        <text class="gt-name">{{ m.title }}</text>
        <text class="gt-desc">{{ m.description }}</text>
        <text v-if="m.isBreakthrough" class="gt-badge">突破点</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { GrowthMilestone } from "@/types/report";

withDefaults(
  defineProps<{
    milestones: GrowthMilestone[];
    visible?: boolean;
  }>(),
  { visible: true },
);
</script>

<style scoped>
.gt {
  position: relative;
  padding: 32rpx 24rpx 32rpx 48rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
}
.gt-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 24rpx;
}
.gt-line {
  position: absolute;
  left: 58rpx;
  top: 100rpx;
  bottom: 40rpx;
  width: 2rpx;
  background: linear-gradient(180deg, #2563eb, #dbeafe);
}
.gt-item {
  position: relative;
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  margin-bottom: 32rpx;
  opacity: 0;
  transform: translateX(-12rpx);
  transition: all 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}
.gt-item--show {
  opacity: 1;
  transform: translateX(0);
}
.gt-dot-wrap {
  flex-shrink: 0;
  z-index: 1;
}
.gt-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #fff;
  border: 4rpx solid #2563eb;
  margin-top: 8rpx;
}
.gt-dot--break {
  border-color: #f59e0b;
  background: #fffbeb;
  box-shadow: 0 0 0 6rpx rgba(245, 158, 11, 0.2);
}
.gt-body {
  flex: 1;
}
.gt-date {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 600;
}
.gt-name {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  margin-top: 4rpx;
}
.gt-desc {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  margin-top: 6rpx;
  line-height: 1.5;
}
.gt-badge {
  display: inline-block;
  margin-top: 10rpx;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  background: #fffbeb;
  font-size: 20rpx;
  font-weight: 700;
  color: #b45309;
}
</style>
