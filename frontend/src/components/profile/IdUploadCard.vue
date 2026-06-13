<template>
  <view
    class="iuc"
    :class="{ 'iuc--done': done, 'iuc--drag': isDragOver }"
    @tap="emit('pick')"
  >
    <!-- #ifdef H5 -->
    <view
      class="iuc-drop"
      @dragover.prevent="onDragOver"
      @dragleave.prevent="onDragLeave"
      @drop.prevent="onDrop"
    />
    <!-- #endif -->

    <image v-if="fileUrl && done" class="iuc-preview" :src="fileUrl" mode="aspectFill" />

    <view v-if="!done" class="iuc-placeholder">
      <text v-if="progress < 100" class="iuc-upload-ico">↑</text>
      <text v-else class="iuc-check">✓</text>
      <text class="iuc-label">{{ label }}</text>
      <text class="iuc-tip">点击上传 · H5 可拖拽</text>
    </view>

    <view v-if="progress > 0 && progress < 100" class="iuc-progress">
      <view class="iuc-ring">
        <view class="iuc-ring-fill" :style="{ '--p': progress }" />
        <text class="iuc-pct">{{ progress }}%</text>
      </view>
    </view>

    <view v-if="done" class="iuc-done-badge">
      <text class="iuc-done-ico">✓</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";

defineProps<{
  label: string;
  fileUrl: string;
  progress: number;
  done: boolean;
}>();

const emit = defineEmits<{
  (e: "pick"): void;
  (e: "drop-file", path: string): void;
}>();

const isDragOver = ref(false);

function onDragOver() {
  isDragOver.value = true;
}

function onDragLeave() {
  isDragOver.value = false;
}

function onDrop(e: DragEvent) {
  isDragOver.value = false;
  const file = e.dataTransfer?.files?.[0];
  if (!file || !file.type.startsWith("image/")) return;
  const url = URL.createObjectURL(file);
  emit("drop-file", url);
}
</script>

<style scoped>
.iuc {
  position: relative;
  flex: 1;
  min-height: 280rpx;
  border-radius: 20rpx;
  border: 2rpx dashed #93c5fd;
  background: linear-gradient(145deg, #eff6ff 0%, #f8fafc 100%);
  overflow: hidden;
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease,
    transform 0.25s ease;
  box-sizing: border-box;
}
.iuc--drag {
  border-color: #2563eb;
  box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.15);
  transform: scale(1.01);
}
.iuc--done {
  border-style: solid;
  border-color: #86efac;
  background: #f0fdf4;
}
.iuc-drop {
  position: absolute;
  inset: 0;
  z-index: 2;
}
.iuc-preview {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0.35;
}
.iuc-placeholder {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280rpx;
  padding: 24rpx;
}
.iuc-upload-ico {
  font-size: 56rpx;
  color: #3b82f6;
  font-weight: 700;
}
.iuc-check {
  font-size: 56rpx;
  color: #22c55e;
  font-weight: 800;
}
.iuc-label {
  margin-top: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #1e40af;
}
.iuc-tip {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}

.iuc-progress {
  position: absolute;
  inset: 0;
  z-index: 3;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
}
.iuc-ring {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.iuc-ring-fill {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 8rpx solid #e2e8f0;
  border-top-color: #2563eb;
  transform: rotate(calc(var(--p, 0) * 3.6deg));
  transition: transform 0.15s linear;
}
.iuc-pct {
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
  z-index: 1;
}

.iuc-done-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  z-index: 4;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #22c55e;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(34, 197, 94, 0.4);
}
.iuc-done-ico {
  color: #fff;
  font-size: 28rpx;
  font-weight: 800;
}
</style>
