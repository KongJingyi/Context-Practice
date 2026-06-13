<template>
  <view
    class="ripple-btn"
    :class="[
      `ripple-btn--${variant}`,
      { 'ripple-btn--block': block, 'ripple-btn--disabled': disabled || loading },
    ]"
    @tap="onTap"
  >
    <view v-if="loading" class="ripple-btn__spinner" />
    <view class="ripple-btn__content">
      <slot name="icon" />
      <text class="ripple-btn__text"><slot /></text>
    </view>
    <view
      v-for="wave in ripples"
      :key="wave.id"
      class="ripple-btn__wave"
      :style="{
        left: wave.x + 'px',
        top: wave.y + 'px',
        width: wave.size + 'px',
        height: wave.size + 'px',
      }"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";

const props = withDefaults(
  defineProps<{
    variant?: "primary" | "ghost" | "oauth";
    block?: boolean;
    disabled?: boolean;
    loading?: boolean;
  }>(),
  {
    variant: "primary",
    block: true,
    disabled: false,
    loading: false,
  },
);

const emit = defineEmits<{
  (e: "click"): void;
}>();

type Ripple = { id: number; x: number; y: number; size: number };

const ripples = ref<Ripple[]>([]);
let rippleSeed = 0;

function onTap(e: { detail?: { x?: number; y?: number }; touches?: { x: number; y: number }[] }) {
  if (props.disabled || props.loading) return;

  const touch = e.touches?.[0];
  const x = touch?.x ?? e.detail?.x ?? 0;
  const y = touch?.y ?? e.detail?.y ?? 0;
  const id = ++rippleSeed;
  const size = 120;
  ripples.value.push({ id, x: x - size / 2, y: y - size / 2, size });
  setTimeout(() => {
    ripples.value = ripples.value.filter((r) => r.id !== id);
  }, 600);

  emit("click");
}
</script>

<style scoped>
.ripple-btn {
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  padding: 0 32rpx;
  border-radius: 16rpx;
  box-sizing: border-box;
}
.ripple-btn--block {
  width: 100%;
}
.ripple-btn--primary {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 55%, #2563eb 100%);
  box-shadow: 0 10rpx 28rpx rgba(59, 130, 246, 0.28);
}
.ripple-btn--ghost {
  background: #ffffff;
  border: 1rpx solid #bae6fd;
}
.ripple-btn--oauth {
  width: 100%;
  min-height: 80rpx;
  background: #ffffff;
  border: 1rpx solid #bae6fd;
}
.ripple-btn--disabled {
  opacity: 0.55;
  pointer-events: none;
}
.ripple-btn__content {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.ripple-btn__text {
  font-size: 30rpx;
  font-weight: 600;
  color: #f8fafc;
  letter-spacing: 1rpx;
}
.ripple-btn--ghost .ripple-btn__text {
  font-weight: 500;
  font-size: 26rpx;
  color: #475569;
}
.ripple-btn--oauth .ripple-btn__text {
  font-weight: 500;
  font-size: 26rpx;
  color: #2563eb;
}
.ripple-btn__spinner {
  width: 28rpx;
  height: 28rpx;
  margin-right: 12rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.ripple-btn__wave {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  transform: scale(0);
  animation: ripple 0.6s ease-out forwards;
  pointer-events: none;
  z-index: 1;
}
@keyframes ripple {
  to {
    transform: scale(2.8);
    opacity: 0;
  }
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
