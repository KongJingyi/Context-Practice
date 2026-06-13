<template>
  <view class="otl">
    <view v-for="(ev, i) in events" :key="ev.id" class="otl-item">
      <view class="otl-rail">
        <view
          class="otl-dot"
          :class="{
            'otl-dot--done': ev.done,
            'otl-dot--cur': ev.current,
          }"
        />
        <view v-if="i < events.length - 1" class="otl-line" :class="{ 'otl-line--done': ev.done }" />
      </view>
      <view class="otl-body">
        <text class="otl-label" :class="{ 'otl-label--cur': ev.current }">{{ ev.label }}</text>
        <text class="otl-time">{{ ev.time }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { OrderTimelineEvent } from "@/types/orders";

defineProps<{
  events: OrderTimelineEvent[];
}>();
</script>

<style scoped>
.otl {
  padding: 16rpx 0 8rpx 8rpx;
}
.otl-item {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  min-height: 64rpx;
}
.otl-rail {
  width: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}
.otl-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #e2e8f0;
  border: 3rpx solid #fff;
  box-shadow: 0 0 0 2rpx #e2e8f0;
  flex-shrink: 0;
}
.otl-dot--done {
  background: #2563eb;
  box-shadow: 0 0 0 2rpx #93c5fd;
}
.otl-dot--cur {
  background: #fff;
  box-shadow: 0 0 0 3rpx #2563eb;
  animation: otlPulse 1.5s ease-in-out infinite;
}
@keyframes otlPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.15);
  }
}
.otl-line {
  flex: 1;
  width: 2rpx;
  min-height: 24rpx;
  background: #e2e8f0;
  margin: 4rpx 0;
}
.otl-line--done {
  background: #93c5fd;
}
.otl-body {
  flex: 1;
  padding-bottom: 16rpx;
}
.otl-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #64748b;
}
.otl-label--cur {
  color: #2563eb;
  font-weight: 800;
}
.otl-time {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 4rpx;
}
</style>
