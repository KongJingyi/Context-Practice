<template>
  <scroll-view class="ets" scroll-x :show-scrollbar="false">
    <view class="ets-row">
      <view
        v-for="e in tips"
        :key="e.id"
        class="ets-card"
        :class="{ 'ets-card--hover': hoverId === e.id }"
        @mouseenter="hoverId = e.id"
        @mouseleave="hoverId = ''"
      >
        <view class="ets-avatar">
          <text>{{ e.name.slice(0, 1) }}</text>
        </view>
        <view class="ets-body">
          <view class="ets-meta">
            <text class="ets-name">{{ e.name }}</text>
            <text class="ets-title">{{ e.title }}</text>
          </view>
          <text class="ets-tip">{{ e.tip }}</text>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { ExpertTip } from "@/types/community";

defineProps<{ tips: ExpertTip[] }>();

const hoverId = ref("");
</script>

<style scoped>
.ets {
  width: 100%;
  margin-bottom: 24rpx;
}
.ets-row {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  gap: 16rpx;
  padding: 4rpx 24rpx 16rpx;
  box-sizing: border-box;
}
.ets-card {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 20rpx;
  flex: 0 0 480rpx;
  width: 480rpx;
  max-width: 78vw;
  min-height: 168rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.05);
  box-sizing: border-box;
  transition:
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease,
    border-color 0.22s ease;
}
.ets-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: linear-gradient(145deg, #eff6ff, #dbeafe);
  border: 3rpx solid #fbbf24;
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}
.ets-avatar text {
  font-size: 32rpx;
  font-weight: 800;
  color: #2563eb;
  line-height: 1;
}
.ets-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.ets-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.ets-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
}
.ets-title {
  font-size: 22rpx;
  color: #b45309;
  font-weight: 600;
  line-height: 1.3;
}
.ets-tip {
  font-size: 24rpx;
  color: #475569;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

/* #ifdef H5 */
.ets-card {
  cursor: default;
}
.ets-card--hover {
  transform: translateY(-4rpx);
  border-color: #bfdbfe;
  box-shadow: 0 10rpx 28rpx rgba(37, 99, 235, 0.12);
}
/* #endif */

/* #ifdef H5 */
@media (min-width: 768px) {
  .ets-row {
    padding: 4rpx 8rpx 16rpx;
  }
  .ets-card {
    flex: 0 0 360px;
    width: 360px;
    max-width: none;
  }
}
/* #endif */
</style>
