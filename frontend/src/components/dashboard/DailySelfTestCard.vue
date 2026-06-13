<template>
  <view class="dst" @tap="goLab">
    <view class="dst-glow" />
    <view class="dst-content">
      <view class="dst-left">
        <text class="dst-badge">今日推荐</text>
        <text class="dst-title">开启今日自测</text>
        <text class="dst-sub">3 分钟录音 · AI 雷达图即时反馈</text>
      </view>
      <view class="dst-btn">
        <text>开始练习 →</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { setPracticeEntry } from "@/utils/practice/bridge.js";

function goLab() {
  setPracticeEntry({ tab: "square" });
  uni.navigateTo({ url: "/pages/practice-lab/practice-lab" });
}
</script>

<style scoped>
.dst {
  position: relative;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 50%, #f0f7ff 100%);
  border: 1rpx solid #bfdbfe;
  cursor: pointer;
  transition: transform 0.25s ease;
}
.dst:active {
  transform: scale(0.98);
}
.dst-glow {
  position: absolute;
  top: -40rpx;
  right: -20rpx;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.25) 0%, transparent 70%);
  pointer-events: none;
}
.dst-content {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  gap: 16rpx;
}
.dst-left {
  flex: 1;
}
.dst-badge {
  display: inline-block;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.12);
  font-size: 20rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 10rpx;
}
.dst-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.dst-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}
.dst-btn {
  flex-shrink: 0;
  padding: 16rpx 24rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  box-shadow: 0 6rpx 20rpx rgba(37, 99, 235, 0.3);
}
.dst-btn text {
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

@media (min-width: 1024px) {
  .dst {
    border-radius: 16px;
    margin-bottom: 16px;
  }
  .dst-content {
    padding: 20px 24px;
  }
}
</style>
