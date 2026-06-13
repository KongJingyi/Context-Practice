<template>
  <view v-if="visible" class="acm-mask" @tap="emit('close')">
    <view class="acm" @tap.stop>
      <text class="acm-title">裁剪头像</text>
      <text class="acm-sub">拖动图片调整位置，预览为圆形头像</text>

      <view class="acm-preview-wrap">
        <view class="acm-circle">
          <image
            v-if="src"
            class="acm-img"
            :src="src"
            mode="aspectFill"
            :style="imgStyle"
          />
        </view>
      </view>

      <view v-if="src" class="acm-slider-row">
        <text class="acm-slider-label">缩放</text>
        <slider
          class="acm-slider"
          :value="scale"
          :min="100"
          :max="200"
          :step="5"
          activeColor="#2563eb"
          @change="onScaleChange"
        />
      </view>

      <view class="acm-actions">
        <view class="acm-btn acm-btn--ghost" @tap="emit('close')">
          <text>取消</text>
        </view>
        <view class="acm-btn acm-btn--primary" @tap="onConfirm">
          <text>确认使用</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

const props = defineProps<{
  visible: boolean;
  src: string;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "confirm", url: string): void;
}>();

const scale = ref(100);

watch(
  () => props.src,
  () => {
    scale.value = 100;
  },
);

const imgStyle = computed(() => ({
  transform: `scale(${scale.value / 100})`,
}));

function onScaleChange(e: { detail: { value: number } }) {
  scale.value = e.detail.value;
}

function onConfirm() {
  emit("confirm", props.src);
}
</script>

<style scoped>
.acm-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  box-sizing: border-box;
}
.acm {
  width: 100%;
  max-width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 24rpx 48rpx rgba(15, 23, 42, 0.2);
}
.acm-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}
.acm-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}
.acm-preview-wrap {
  display: flex;
  justify-content: center;
  margin: 32rpx 0 24rpx;
}
.acm-circle {
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid #e2e8f0;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.15);
}
.acm-img {
  width: 100%;
  height: 100%;
  transition: transform 0.2s ease;
}
.acm-slider-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.acm-slider-label {
  font-size: 24rpx;
  color: #64748b;
  flex-shrink: 0;
}
.acm-slider {
  flex: 1;
}
.acm-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}
.acm-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 14rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
}
.acm-btn--ghost {
  border: 2rpx solid #e2e8f0;
  color: #475569;
}
.acm-btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
}
</style>
