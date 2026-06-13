<template>
  <view v-if="visible" class="pso">
    <view class="pso-glow" />
    <view class="pso-card">
      <view class="pso-check-wrap">
        <view class="pso-ring pso-ring--breathe" />
        <view class="pso-check">
          <view class="pso-check-stroke" />
        </view>
      </view>
      <view v-for="n in 12" :key="n" class="pso-confetti" :style="confettiStyle(n)" />

      <text class="pso-title">预约成功！</text>
      <text class="pso-sub">陪练链接已发送至您的站内信</text>
      <text v-if="orderNo" class="pso-order">订单号 {{ orderNo }}</text>

      <view class="pso-actions">
        <view class="pso-btn pso-btn--primary" @tap="emit('enter-room')">
          <text>进入训练间</text>
        </view>
        <view class="pso-btn pso-btn--ghost" @tap="emit('view-orders')">
          <text>查看订单</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean;
  orderNo?: string;
}>();

const emit = defineEmits<{
  (e: "enter-room"): void;
  (e: "view-orders"): void;
}>();

function confettiStyle(n: number) {
  const deg = (n / 12) * 360;
  const colors = ["#3b82f6", "#22c55e", "#f59e0b", "#ec4899", "#8b5cf6"];
  return {
    transform: `rotate(${deg}deg) translateY(-120rpx)`,
    background: colors[n % colors.length],
    animationDelay: `${(n % 5) * 0.08}s`,
  };
}
</script>

<style scoped>
.pso {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(8px);
  padding: 32rpx;
  box-sizing: border-box;
}
.pso-glow {
  position: absolute;
  width: 480rpx;
  height: 480rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(34, 197, 94, 0.25) 0%, transparent 70%);
  animation: psoGlow 2.5s ease-in-out infinite;
}
@keyframes psoGlow {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.08);
    opacity: 1;
  }
}
.pso-card {
  position: relative;
  width: 100%;
  max-width: 560rpx;
  padding: 48rpx 36rpx 40rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 24rpx 64rpx rgba(15, 23, 42, 0.2);
  text-align: center;
  overflow: hidden;
}
.pso-check-wrap {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto 28rpx;
}
.pso-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 4rpx solid rgba(34, 197, 94, 0.25);
}
.pso-ring--breathe {
  animation: psoBreathe 2s ease-in-out infinite;
}
@keyframes psoBreathe {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.12);
    opacity: 1;
  }
}
.pso-check {
  position: absolute;
  inset: 16rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #4ade80, #16a34a);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: psoPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s both;
}
@keyframes psoPop {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}
.pso-check-stroke {
  width: 28rpx;
  height: 48rpx;
  border-right: 6rpx solid #fff;
  border-bottom: 6rpx solid #fff;
  transform: rotate(45deg) translate(-4rpx, -8rpx);
  animation: psoDraw 0.45s ease 0.55s both;
}
@keyframes psoDraw {
  from {
    opacity: 0;
    transform: rotate(45deg) translate(-4rpx, -8rpx) scale(0.3);
  }
  to {
    opacity: 1;
    transform: rotate(45deg) translate(-4rpx, -8rpx) scale(1);
  }
}

.pso-confetti {
  position: absolute;
  left: 50%;
  top: 38%;
  width: 10rpx;
  height: 10rpx;
  border-radius: 2rpx;
  margin-left: -5rpx;
  animation: psoConfetti 0.9s ease-out 0.5s both;
}
@keyframes psoConfetti {
  from {
    opacity: 1;
    transform: rotate(var(--r, 0deg)) translateY(0);
  }
  to {
    opacity: 0;
    transform: rotate(var(--r, 0deg)) translateY(-180rpx) scale(0.5);
  }
}

.pso-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #0f172a;
}
.pso-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.5;
}
.pso-order {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #94a3b8;
}
.pso-actions {
  margin-top: 36rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.pso-btn {
  padding: 22rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
}
.pso-btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.35);
}
.pso-btn--ghost {
  border: 1rpx solid #e2e8f0;
  color: #475569;
  background: #fff;
}
</style>
