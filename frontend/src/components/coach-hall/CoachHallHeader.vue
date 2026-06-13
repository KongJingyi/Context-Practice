<template>
  <view class="hall-header">
    <view class="hall-header__crumb">
      <text class="hall-header__crumb-link" @tap="goHome">首页</text>
      <text class="hall-header__crumb-sep">›</text>
      <text class="hall-header__crumb-mid">{{ categoryLabel }}</text>
      <text class="hall-header__crumb-sep">›</text>
      <text class="hall-header__crumb-current">{{ subSceneName }}</text>
    </view>
    <text class="hall-header__title">{{ subSceneName }}</text>
    <view class="hall-header__guide">
      <text class="hall-header__guide-label">场景攻略</text>
      <text class="hall-header__guide-text">{{ guide }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  categoryLabel: string;
  subSceneName: string;
  guide: string;
}>();

function goHome() {
  uni.switchTab({ url: "/pages/index/index" });
}
</script>

<style scoped>
.hall-header {
  margin-bottom: 32rpx;
}
.hall-header__crumb {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 20rpx;
}
.hall-header__crumb-link {
  font-size: 24rpx;
  color: #2563eb;
  font-weight: 500;
}
.hall-header__crumb-sep {
  font-size: 22rpx;
  color: #cbd5e1;
}
.hall-header__crumb-mid {
  font-size: 24rpx;
  color: #64748b;
}
.hall-header__crumb-current {
  font-size: 24rpx;
  color: #0f172a;
  font-weight: 600;
}
.hall-header__title {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  line-height: 1.2;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .hall-header__title {
    font-size: 36px;
  }
}
/* #endif */
.hall-header__guide {
  margin-top: 20rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.75);
  border: 1rpx solid #e2e8f0;
}
.hall-header__guide-label {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 8rpx;
}
.hall-header__guide-text {
  font-size: 26rpx;
  line-height: 1.65;
  color: #475569;
}
</style>
