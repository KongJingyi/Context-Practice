<template>
  <view class="mft">
    <view class="mft-head">
      <text class="mft-title">我的足迹</text>
      <text class="mft-link" @tap="goSquare">去灵感广场 →</text>
    </view>
    <view class="mft-line" />
    <view
      v-for="(item, i) in items"
      :key="item.id"
      class="mft-item"
      :style="{ animationDelay: `${i * 0.08}s` }"
    >
      <view class="mft-dot" :class="`mft-dot--${item.type}`" />
      <view class="mft-body">
        <text class="mft-date">{{ item.date }}</text>
        <text class="mft-name">{{ item.title }}</text>
        <text class="mft-sum">{{ item.summary }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { fetchMyFootprint } from "@/api/modules/community.js";
import type { FootprintItem } from "@/types/community";

const items = ref<FootprintItem[]>([]);

onMounted(async () => {
  items.value = await fetchMyFootprint();
});

function goSquare() {
  uni.switchTab({ url: "/pages/insight-square/insight-square" });
}
</script>

<style scoped>
.mft {
  position: relative;
  padding: 28rpx 24rpx 28rpx 48rpx;
}
.mft-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-left: 8rpx;
}
.mft-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.mft-link {
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
}
.mft-line {
  position: absolute;
  left: 58rpx;
  top: 100rpx;
  bottom: 32rpx;
  width: 2rpx;
  background: linear-gradient(180deg, #2563eb, #dbeafe);
}
.mft-item {
  position: relative;
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  margin-bottom: 28rpx;
  animation: mftIn 0.5s ease both;
}
@keyframes mftIn {
  from {
    opacity: 0;
    transform: translateX(-8rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
.mft-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  margin-top: 8rpx;
  flex-shrink: 0;
  z-index: 1;
  background: #fff;
  border: 4rpx solid #2563eb;
}
.mft-dot--highlight {
  border-color: #6366f1;
}
.mft-dot--interview {
  border-color: #059669;
}
.mft-date {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 600;
}
.mft-name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
  margin-top: 4rpx;
}
.mft-sum {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  margin-top: 4rpx;
}
</style>
