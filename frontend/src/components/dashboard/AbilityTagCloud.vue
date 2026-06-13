<template>
  <view class="atc">
    <view class="atc-head">
      <text class="atc-ico">☁</text>
      <text class="atc-title">能力标签云</text>
      <text class="atc-sub">来自陪练反馈的高频评价</text>
    </view>
    <view class="atc-pool">
      <text
        v-for="t in tags"
        :key="t.text"
        class="atc-tag"
        :style="tagStyle(t.weight)"
      >
        {{ t.text }}
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { AbilityTagItem } from "@/types/dashboard";

defineProps<{
  tags: AbilityTagItem[];
}>();

function tagStyle(weight: number) {
  const min = 22;
  const max = 34;
  const size = min + (max - min) * weight;
  const alpha = 0.45 + weight * 0.55;
  const r = Math.round(37 + (1 - weight) * 40);
  const g = Math.round(99 + (1 - weight) * 60);
  const b = Math.round(235 - (1 - weight) * 30);
  return {
    fontSize: `${size}rpx`,
    color: `rgba(${r}, ${g}, ${b}, ${alpha})`,
    background: `rgba(59, 130, 246, ${0.06 + weight * 0.14})`,
  };
}
</script>

<style scoped>
.atc {
  padding: 28rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  border: 1rpx solid #f1f5f9;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.06);
  transition: box-shadow 0.25s ease;
}
.atc:hover {
  box-shadow: 0 4rpx 12rpx rgba(15, 23, 42, 0.08);
}
.atc-head {
  margin-bottom: 20rpx;
}
.atc-ico {
  font-size: 28rpx;
  margin-right: 8rpx;
}
.atc-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}
.atc-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #94a3b8;
}
.atc-pool {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 14rpx 16rpx;
  align-items: center;
  justify-content: center;
  min-height: 120rpx;
}
.atc-tag {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  font-weight: 600;
  line-height: 1.3;
  transition: transform 0.2s ease;
}
</style>
