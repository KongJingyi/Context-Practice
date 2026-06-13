<template>
  <view class="rtb">
    <view
      v-for="btn in buttons"
      :key="btn.id"
      class="rtb-btn"
      :class="{
        'rtb-btn--on': btn.active,
        'rtb-btn--danger': btn.danger,
        'rtb-btn--hover': hoverId === btn.id,
      }"
      @tap="emit('action', btn.id)"
      @mouseenter="hoverId = btn.id"
      @mouseleave="hoverId = ''"
    >
      <view class="rtb-ico-wrap">
        <view class="rtb-glyph" :class="`rtb-glyph--${btn.glyph}`" />
      </view>
      <text class="rtb-lbl">{{ btn.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const props = defineProps<{
  muted: boolean;
  cameraOff: boolean;
}>();

const emit = defineEmits<{
  (e: "action", id: string): void;
}>();

const hoverId = ref("");

const buttons = computed(() => [
  {
    id: "mute",
    glyph: props.muted ? "mic-off" : "mic",
    label: props.muted ? "已静音" : "静音",
    active: props.muted,
  },
  {
    id: "camera",
    glyph: props.cameraOff ? "camera-off" : "camera",
    label: props.cameraOff ? "开摄像头" : "摄像头",
    active: props.cameraOff,
  },
  {
    id: "highlight",
    glyph: "star",
    label: "高光",
    active: false,
  },
  {
    id: "hangup",
    glyph: "phone",
    label: "结束",
    active: false,
    danger: true,
  },
]);
</script>

<style scoped>
.rtb {
  display: flex;
  flex-direction: row;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16rpx 24rpx;
  padding: 20rpx 28rpx;
  border-radius: 999rpx;
  background: rgba(17, 24, 39, 0.78);
  backdrop-filter: blur(16px);
  border: 1rpx solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.4);
}
.rtb-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  min-width: 108rpx;
  padding: 16rpx 14rpx;
  border-radius: 18rpx;
  transition:
    background 0.22s ease,
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease;
}
.rtb-btn--on {
  background: rgba(59, 130, 246, 0.28);
}
.rtb-btn--danger {
  background: rgba(220, 38, 38, 0.38);
  min-width: 120rpx;
}
.rtb-ico-wrap {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rtb-glyph {
  position: relative;
  color: #f1f5f9;
  transition: color 0.22s ease, transform 0.22s ease;
}
.rtb-btn--on .rtb-glyph {
  color: #93c5fd;
}
.rtb-btn--danger .rtb-glyph {
  color: #fff;
}

/* 麦克风 */
.rtb-glyph--mic {
  width: 22rpx;
  height: 32rpx;
}
.rtb-glyph--mic::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 0;
  width: 14rpx;
  height: 22rpx;
  margin-left: -7rpx;
  border: 3rpx solid currentColor;
  border-radius: 8rpx;
  box-sizing: border-box;
}
.rtb-glyph--mic::after {
  content: "";
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 22rpx;
  height: 12rpx;
  margin-left: -11rpx;
  border: 3rpx solid currentColor;
  border-top: none;
  border-radius: 0 0 12rpx 12rpx;
  box-sizing: border-box;
}

/* 已静音 */
.rtb-glyph--mic-off {
  width: 28rpx;
  height: 32rpx;
  color: #fca5a5;
}
.rtb-glyph--mic-off::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 2rpx;
  width: 14rpx;
  height: 20rpx;
  margin-left: -7rpx;
  border: 3rpx solid currentColor;
  border-radius: 8rpx;
  box-sizing: border-box;
  opacity: 0.85;
}
.rtb-glyph--mic-off::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 28rpx;
  height: 3rpx;
  margin-left: -14rpx;
  margin-top: -2rpx;
  background: currentColor;
  transform: rotate(-42deg);
  border-radius: 2rpx;
}

/* 摄像头 */
.rtb-glyph--camera {
  width: 36rpx;
  height: 28rpx;
}
.rtb-glyph--camera::before {
  content: "";
  position: absolute;
  inset: 6rpx 0 0 0;
  border: 3rpx solid currentColor;
  border-radius: 6rpx;
  box-sizing: border-box;
}
.rtb-glyph--camera::after {
  content: "";
  position: absolute;
  right: -4rpx;
  top: 10rpx;
  border-style: solid;
  border-width: 6rpx 0 6rpx 10rpx;
  border-color: transparent transparent transparent currentColor;
}

/* 摄像头关闭 */
.rtb-glyph--camera-off {
  width: 36rpx;
  height: 28rpx;
  color: #93c5fd;
}
.rtb-glyph--camera-off::before {
  content: "";
  position: absolute;
  inset: 6rpx 0 0 0;
  border: 3rpx solid currentColor;
  border-radius: 6rpx;
  box-sizing: border-box;
  opacity: 0.7;
}
.rtb-glyph--camera-off::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 36rpx;
  height: 3rpx;
  margin-left: -18rpx;
  margin-top: -2rpx;
  background: #fca5a5;
  transform: rotate(-38deg);
  border-radius: 2rpx;
}

/* 高光星 */
.rtb-glyph--star {
  width: 32rpx;
  height: 32rpx;
  color: #fde68a;
}
.rtb-glyph--star::before {
  content: "✦";
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  line-height: 1;
  color: #fde68a;
  text-shadow: 0 0 12rpx rgba(251, 191, 36, 0.55);
}

/* 挂断 */
.rtb-glyph--phone {
  width: 32rpx;
  height: 32rpx;
  transform: rotate(135deg);
}
.rtb-glyph--phone::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 22rpx;
  height: 22rpx;
  margin-left: -11rpx;
  margin-top: -11rpx;
  border: 3rpx solid currentColor;
  border-radius: 6rpx;
  box-sizing: border-box;
}

.rtb-lbl {
  font-size: 22rpx;
  color: #e2e8f0;
  font-weight: 600;
  transition: color 0.22s ease;
}

/* #ifdef H5 */
.rtb-btn {
  cursor: pointer;
}
.rtb-btn--hover:not(.rtb-btn--danger) {
  background: rgba(59, 130, 246, 0.18);
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 20rpx rgba(37, 99, 235, 0.2);
}
.rtb-btn--hover:not(.rtb-btn--danger) .rtb-glyph {
  transform: scale(1.1);
  color: #fff;
}
.rtb-btn--hover:not(.rtb-btn--danger) .rtb-glyph--star::before {
  color: #fef08a;
}
.rtb-btn--hover:not(.rtb-btn--danger) .rtb-lbl {
  color: #fff;
}
.rtb-btn--hover.rtb-btn--danger {
  background: rgba(220, 38, 38, 0.55);
  transform: translateY(-4rpx) scale(1.04);
  box-shadow: 0 8rpx 24rpx rgba(220, 38, 38, 0.35);
}
.rtb-btn--hover.rtb-btn--danger .rtb-glyph {
  transform: rotate(135deg) scale(1.08);
}
/* #endif */
</style>
