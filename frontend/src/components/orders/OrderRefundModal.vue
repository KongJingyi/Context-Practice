<template>
  <view v-if="visible" class="orm-mask" @tap="emit('close')">
    <view class="orm" @tap.stop>
      <text class="orm-title">{{ mode === "apply" ? "申请退款" : "退款进度" }}</text>
      <text v-if="mode === 'apply'" class="orm-sub">订单尚未开始训练，可申请全额退款</text>

      <view v-if="steps.length" class="orm-steps">
        <view v-for="(s, i) in steps" :key="s.key" class="orm-step">
          <view
            class="orm-step-dot"
            :class="{ 'orm-step-dot--done': s.done, 'orm-step-dot--cur': s.current }"
          />
          <view v-if="i < steps.length - 1" class="orm-step-line" :class="{ 'orm-step-line--done': s.done }" />
          <text class="orm-step-label" :class="{ 'orm-step-label--cur': s.current }">{{ s.label }}</text>
        </view>
      </view>

      <view class="orm-foot">
        <view v-if="mode === 'apply'" class="orm-btn orm-btn--primary" @tap="emit('confirm')">
          <text>提交申请</text>
        </view>
        <view class="orm-btn orm-btn--ghost" @tap="emit('close')">
          <text>关闭</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { RefundProgressStep } from "@/types/orders";

defineProps<{
  visible: boolean;
  mode: "apply" | "view";
  steps: RefundProgressStep[];
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "confirm"): void;
}>();
</script>

<style scoped>
.orm-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}
.orm {
  width: 100%;
  max-width: 600rpx;
  padding: 32rpx 28rpx;
  border-radius: 24rpx;
  background: #fff;
}
.orm-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.orm-sub {
  display: block;
  margin: 8rpx 0 24rpx;
  font-size: 24rpx;
  color: #64748b;
}
.orm-steps {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 16rpx 0 24rpx;
}
.orm-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.orm-step-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #e2e8f0;
  z-index: 1;
}
.orm-step-dot--done {
  background: #22c55e;
}
.orm-step-dot--cur {
  background: #f59e0b;
  box-shadow: 0 0 0 6rpx rgba(245, 158, 11, 0.25);
}
.orm-step-line {
  position: absolute;
  top: 10rpx;
  left: 50%;
  right: -50%;
  height: 4rpx;
  background: #e2e8f0;
  z-index: 0;
}
.orm-step-line--done {
  background: #86efac;
}
.orm-step:last-child .orm-step-line {
  display: none;
}
.orm-step-label {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #94a3b8;
  text-align: center;
}
.orm-step-label--cur {
  color: #b45309;
  font-weight: 700;
}
.orm-foot {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.orm-btn {
  padding: 18rpx;
  border-radius: 14rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
}
.orm-btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
}
.orm-btn--ghost {
  border: 1rpx solid #e2e8f0;
  color: #64748b;
}
</style>
