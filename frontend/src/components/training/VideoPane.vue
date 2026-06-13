<template>
  <view
    class="vp"
    :class="[
      size === 'main' ? 'vp--main' : 'vp--pip',
      { 'vp--connecting': !connected, 'vp--off': cameraOff && isLocal, 'vp--hover': pipHover },
    ]"
    @tap="emit('tap')"
    @mouseenter="onPipEnter"
    @mouseleave="onPipLeave"
  >
    <!-- #ifdef H5 -->
    <view
      v-show="connected && !(cameraOff && isLocal)"
      ref="videoHostRef"
      class="vp-video-host"
    />
    <!-- #endif -->

    <view v-if="!connected || (cameraOff && isLocal)" class="vp-placeholder">
      <view class="vp-pulse-wrap">
        <view class="vp-pulse vp-pulse--1" />
        <view class="vp-pulse vp-pulse--2" />
        <view class="vp-pulse vp-pulse--3" />
        <view class="vp-avatar">
          <text class="vp-letter">{{ label.slice(0, 1) }}</text>
        </view>
      </view>
      <text v-if="!connected" class="vp-status">{{ statusText }}</text>
      <text v-else-if="cameraOff && isLocal" class="vp-status">摄像头已关闭</text>
    </view>

    <view class="vp-badge">
      <text>{{ label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount, nextTick } from "vue";
import { attachStreamToVideo } from "@/utils/training/webrtc";

const props = defineProps<{
  label: string;
  size: "main" | "pip";
  connected: boolean;
  isLocal?: boolean;
  cameraOff?: boolean;
  statusText?: string;
  stream?: MediaStream | null;
}>();

const emit = defineEmits<{ (e: "tap"): void }>();

const videoHostRef = ref<unknown>(null);
let nativeVideo: HTMLVideoElement | null = null;
const pipHover = ref(false);

function resolveHostEl(): HTMLElement | null {
  const raw = videoHostRef.value;
  if (!raw) return null;
  if (raw instanceof HTMLElement) return raw;
  const maybe = raw as { $el?: unknown };
  if (maybe.$el instanceof HTMLElement) return maybe.$el;
  return null;
}

function onPipEnter() {
  if (props.size === "pip") pipHover.value = true;
}

function onPipLeave() {
  pipHover.value = false;
}

function ensureNativeVideo(): HTMLVideoElement | null {
  const host = resolveHostEl();
  if (!host) return null;
  if (!nativeVideo || !host.contains(nativeVideo)) {
    nativeVideo = document.createElement("video");
    nativeVideo.className = "vp-video";
    nativeVideo.autoplay = true;
    nativeVideo.playsInline = true;
    nativeVideo.setAttribute("playsinline", "true");
    nativeVideo.setAttribute("webkit-playsinline", "true");
    nativeVideo.controls = false;
    host.appendChild(nativeVideo);
  }
  nativeVideo.muted = props.isLocal !== false;
  return nativeVideo;
}

function bindStream() {
  // #ifdef H5
  nextTick(() => {
    if (!props.connected || (props.cameraOff && props.isLocal)) return;
    const el = ensureNativeVideo();
    void attachStreamToVideo(el, props.stream ?? null);
  });
  // #endif
}

watch(() => props.stream, bindStream, { immediate: true });

watch(
  () => [props.connected, props.cameraOff, props.isLocal] as const,
  () => bindStream(),
);

onBeforeUnmount(() => {
  if (nativeVideo) {
    nativeVideo.srcObject = null;
    nativeVideo.remove();
    nativeVideo = null;
  }
});
</script>

<style scoped>
.vp {
  position: relative;
  border-radius: 20rpx;
  overflow: hidden;
  background: #1f2937;
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  box-sizing: border-box;
  transition:
    transform 0.3s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.3s ease,
    border-color 0.3s ease;
}
.vp--main {
  width: 100%;
  height: 100%;
  min-height: 100%;
  border-radius: 0;
  border: none;
}
.vp--pip {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  width: 260rpx;
  height: 340rpx;
  z-index: 8;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.5);
  border: 2rpx solid rgba(255, 255, 255, 0.14);
}
.vp-video-host {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
}
.vp-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  background: #111827;
}
.vp-placeholder {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 50% 38%, #1e3a5f 0%, #0f172a 72%);
}
.vp-pulse-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.vp--main .vp-pulse-wrap {
  width: 220rpx;
  height: 220rpx;
}
.vp--pip .vp-pulse-wrap {
  width: 120rpx;
  height: 120rpx;
}
.vp-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2rpx solid rgba(59, 130, 246, 0.45);
  animation: vpPulse 2.4s ease-out infinite;
}
.vp-pulse--2 {
  animation-delay: 0.6s;
}
.vp-pulse--3 {
  animation-delay: 1.2s;
}
@keyframes vpPulse {
  0% {
    transform: scale(0.55);
    opacity: 0.9;
  }
  100% {
    transform: scale(1.35);
    opacity: 0;
  }
}
.vp-avatar {
  border-radius: 50%;
  background: linear-gradient(145deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  box-shadow: 0 8rpx 28rpx rgba(37, 99, 235, 0.45);
}
.vp--main .vp-avatar {
  width: 168rpx;
  height: 168rpx;
}
.vp--pip .vp-avatar {
  width: 88rpx;
  height: 88rpx;
}
.vp-letter {
  font-weight: 800;
  color: #fff;
}
.vp--main .vp-letter {
  font-size: 72rpx;
}
.vp--pip .vp-letter {
  font-size: 40rpx;
}
.vp-status {
  margin-top: 24rpx;
  color: #94a3b8;
  font-weight: 600;
  text-align: center;
  padding: 0 32rpx;
}
.vp--main .vp-status {
  font-size: 28rpx;
}
.vp--pip .vp-status {
  font-size: 20rpx;
  margin-top: 12rpx;
}
.vp-badge {
  position: absolute;
  left: 16rpx;
  bottom: 16rpx;
  z-index: 2;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.72);
  backdrop-filter: blur(8px);
}
.vp--pip .vp-badge {
  left: 12rpx;
  bottom: 12rpx;
  padding: 4rpx 12rpx;
}
.vp-badge text {
  color: #e2e8f0;
  font-weight: 600;
}
.vp--main .vp-badge text {
  font-size: 24rpx;
}
.vp--pip .vp-badge text {
  font-size: 20rpx;
}

/* #ifdef H5 */
.vp--pip {
  cursor: pointer;
}
.vp--pip.vp--hover {
  transform: scale(1.04);
  border-color: rgba(147, 197, 253, 0.55);
  box-shadow: 0 20rpx 48rpx rgba(37, 99, 235, 0.25);
}
/* #endif */

/* #ifdef H5 */
@media (min-width: 768px) {
  .vp--pip {
    width: 200px;
    height: 260px;
    right: 20px;
    bottom: 20px;
  }
  .vp--main .vp-avatar {
    width: 120px;
    height: 120px;
  }
  .vp--main .vp-letter {
    font-size: 48px;
  }
  .vp--main .vp-pulse-wrap {
    width: 160px;
    height: 160px;
  }
}
/* #endif */
</style>
