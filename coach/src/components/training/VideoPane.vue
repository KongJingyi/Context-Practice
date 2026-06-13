<template>
  <div
    class="vp"
    :class="[
      size === 'main' ? 'vp--main' : 'vp--pip',
      { 'vp--hover': pipHover && size === 'pip' },
    ]"
    @click="size === 'pip' ? emit('tap') : undefined"
    @mouseenter="onPipEnter"
    @mouseleave="onPipLeave"
  >
    <div
      v-show="connected && !(cameraOff && isLocal)"
      ref="videoHostRef"
      class="vp-video-host"
    />

    <div v-if="!connected || (cameraOff && isLocal)" class="vp-placeholder">
      <div class="vp-pulse-wrap" :class="size === 'main' ? 'vp-pulse-wrap--main' : 'vp-pulse-wrap--pip'">
        <div class="vp-pulse vp-pulse--1" />
        <div class="vp-pulse vp-pulse--2" />
        <div class="vp-pulse vp-pulse--3" />
        <div class="vp-avatar" :class="size === 'main' ? 'vp-avatar--main' : 'vp-avatar--pip'">
          <span class="vp-letter" :class="size === 'main' ? 'vp-letter--main' : 'vp-letter--pip'">
            {{ label.slice(0, 1) }}
          </span>
        </div>
      </div>
      <p v-if="!connected" class="vp-status" :class="size === 'main' ? 'vp-status--main' : 'vp-status--pip'">
        {{ statusText }}
      </p>
      <p v-else-if="cameraOff && isLocal" class="vp-status vp-status--main">摄像头已关闭</p>
    </div>

    <div class="vp-badge" :class="size === 'pip' ? 'vp-badge--pip' : ''">
      <span>{{ label }}</span>
    </div>
  </div>
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

const videoHostRef = ref<HTMLElement | null>(null);
let nativeVideo: HTMLVideoElement | null = null;
const pipHover = ref(false);

function onPipEnter() {
  if (props.size === "pip") pipHover.value = true;
}

function onPipLeave() {
  pipHover.value = false;
}

function ensureNativeVideo(): HTMLVideoElement | null {
  const host = videoHostRef.value;
  if (!host) return null;
  if (!nativeVideo || !host.contains(nativeVideo)) {
    nativeVideo = document.createElement("video");
    nativeVideo.className = "vp-video";
    nativeVideo.autoplay = true;
    nativeVideo.playsInline = true;
    nativeVideo.setAttribute("playsinline", "true");
    nativeVideo.controls = false;
    host.appendChild(nativeVideo);
  }
  nativeVideo.muted = props.isLocal !== false;
  return nativeVideo;
}

function bindStream() {
  nextTick(() => {
    if (!props.connected || (props.cameraOff && props.isLocal)) return;
    const el = ensureNativeVideo();
    void attachStreamToVideo(el, props.stream ?? null);
  });
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
  border-radius: 12px;
  overflow: hidden;
  background: #1f2937;
  border: 1px solid rgba(255, 255, 255, 0.08);
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
  right: 16px;
  bottom: 16px;
  width: 200px;
  height: 260px;
  z-index: 8;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.14);
  cursor: pointer;
}
.vp--pip.vp--hover {
  transform: scale(1.04);
  border-color: rgba(147, 197, 253, 0.55);
  box-shadow: 0 20px 48px rgba(37, 99, 235, 0.25);
}
.vp-video-host {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
}
:deep(.vp-video) {
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
.vp-pulse-wrap--main {
  width: 160px;
  height: 160px;
}
.vp-pulse-wrap--pip {
  width: 88px;
  height: 88px;
}
.vp-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid rgba(59, 130, 246, 0.45);
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
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.45);
}
.vp-avatar--main {
  width: 120px;
  height: 120px;
}
.vp-avatar--pip {
  width: 56px;
  height: 56px;
}
.vp-letter {
  font-weight: 800;
  color: #fff;
}
.vp-letter--main {
  font-size: 48px;
}
.vp-letter--pip {
  font-size: 24px;
}
.vp-status {
  margin-top: 16px;
  color: #94a3b8;
  font-weight: 600;
  text-align: center;
  padding: 0 24px;
}
.vp-status--main {
  font-size: 15px;
}
.vp-status--pip {
  font-size: 12px;
  margin-top: 8px;
}
.vp-badge {
  position: absolute;
  left: 12px;
  bottom: 12px;
  z-index: 2;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.72);
  backdrop-filter: blur(8px);
  color: #e2e8f0;
  font-weight: 600;
  font-size: 13px;
}
.vp-badge--pip {
  left: 8px;
  bottom: 8px;
  padding: 3px 10px;
  font-size: 11px;
}
</style>
