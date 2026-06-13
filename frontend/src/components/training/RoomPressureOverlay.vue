<template>
  <view class="pressure">
    <view v-if="countdownActive" class="pressure-countdown">
      <text class="pressure-countdown-num">{{ countdownLeft }}</text>
      <text class="pressure-countdown-label">秒</text>
    </view>

    <view v-if="interruptMessage" class="pressure-interrupt" :class="{ 'pressure-interrupt--flash': flash }">
      <text class="pressure-interrupt-text">{{ interruptMessage }}</text>
    </view>

    <view v-if="questionText" class="pressure-question">
      <text class="pressure-question-label">压力提问</text>
      <text class="pressure-question-text">{{ questionText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

const props = defineProps<{
  countdownActive: boolean;
  countdownLeft: number;
  interruptMessage: string;
  questionText: string;
}>();

const flash = ref(false);
let lastInterrupt = "";

watch(
  () => props.interruptMessage,
  (msg) => {
    if (msg && msg !== lastInterrupt) {
      lastInterrupt = msg;
      flash.value = true;
      setTimeout(() => {
        flash.value = false;
      }, 1200);
    }
  },
);
</script>

<style scoped>
.pressure {
  position: absolute;
  inset: 0;
  z-index: 15;
  pointer-events: none;
}
.pressure-countdown {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  padding: 12rpx 20rpx;
  background: rgba(239, 68, 68, 0.85);
  border-radius: 16rpx;
}
.pressure-countdown-num {
  font-size: 48rpx;
  font-weight: 800;
  color: #fff;
  font-variant-numeric: tabular-nums;
}
.pressure-countdown-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}
.pressure-interrupt {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 80%;
  padding: 24rpx 32rpx;
  background: rgba(234, 179, 8, 0.92);
  border-radius: 20rpx;
  opacity: 0;
  transition: opacity 0.3s;
}
.pressure-interrupt--flash {
  opacity: 1;
}
.pressure-interrupt-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
}
.pressure-question {
  position: absolute;
  bottom: 24rpx;
  left: 24rpx;
  right: 24rpx;
  padding: 20rpx 24rpx;
  background: rgba(59, 130, 246, 0.88);
  border-radius: 16rpx;
}
.pressure-question-label {
  display: block;
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 8rpx;
}
.pressure-question-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
  line-height: 1.45;
}
</style>
