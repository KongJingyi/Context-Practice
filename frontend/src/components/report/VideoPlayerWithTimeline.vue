<template>
  <view class="vpt">
    <view class="vpt-player-col">
      <view class="vpt-player-wrap">
        <!-- #ifdef H5 -->
        <video
          ref="videoRef"
          class="vpt-video"
          :src="videoSrc"
          controls
          playsinline
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onMeta"
        />
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <view class="vpt-placeholder">
          <text class="vpt-ph-ico">🎬</text>
          <text class="vpt-ph-txt">录像回放（H5 端可播放）</text>
        </view>
        <!-- #endif -->

        <view class="vpt-lock">
          <text class="vpt-lock-ico">🔒</text>
          <text class="vpt-lock-txt">端到端加密 · 私密云存储，仅本人可见</text>
        </view>
      </view>

      <view class="vpt-bar">
        <view class="vpt-bar-track" @tap="onBarTap">
          <view class="vpt-bar-fill" :style="{ width: `${progressPct}%` }" />
          <view
            v-for="m in markers"
            :key="m.id"
            class="vpt-dot"
            :class="`vpt-dot--${m.type}`"
            :style="{ left: `${(m.seconds / durationSec) * 100}%` }"
          />
        </view>
        <text class="vpt-time">{{ formatSeconds(currentSec) }} / {{ formatSeconds(durationSec) }}</text>
      </view>

      <view class="vpt-actions">
        <view class="vpt-action" @tap="emit('export-highlight')">
          <text>✨ 生成高光片段</text>
        </view>
      </view>
    </view>

    <view class="vpt-list-col">
      <text class="vpt-list-title">逻辑时间轴</text>
      <view
        v-for="m in markers"
        :key="m.id"
        class="vpt-event"
        :class="{ 'vpt-event--active': activeId === m.id }"
        @tap="seekTo(m.seconds, m.id)"
      >
        <text class="vpt-event-time">{{ formatSeconds(m.seconds) }}</text>
        <view class="vpt-event-body">
          <text class="vpt-event-lbl">{{ m.label }}</text>
          <text class="vpt-event-type">{{ typeLabel(m.type) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { formatSeconds } from "@/utils/report/time";
import type { TimelineMarker, FeedbackType } from "@/types/report";

const props = withDefaults(
  defineProps<{
    videoUrl?: string;
    durationSec?: number;
    markers: TimelineMarker[];
    seekSeconds?: number;
  }>(),
  { durationSec: 600, videoUrl: "" },
);

const emit = defineEmits<{ (e: "export-highlight"): void }>();

const videoRef = ref<HTMLVideoElement | null>(null);
const currentSec = ref(0);
const activeId = ref("");

const DEMO_VIDEO =
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";

const videoSrc = computed(() => props.videoUrl || DEMO_VIDEO);
const progressPct = computed(() =>
  props.durationSec > 0 ? Math.min(100, (currentSec.value / props.durationSec) * 100) : 0,
);

function typeLabel(type: FeedbackType) {
  const map: Record<FeedbackType, string> = {
    warning: "待改进点",
    highlight: "表达亮点",
    question: "专家提问",
    turn: "逻辑转折",
  };
  return map[type] || "";
}

function onTimeUpdate() {
  // #ifdef H5
  const v = videoRef.value;
  if (!v) return;
  currentSec.value = v.currentTime;
  const hit = props.markers.find(
    (m) => Math.abs(m.seconds - currentSec.value) < 2,
  );
  if (hit) activeId.value = hit.id;
  // #endif
}

function onMeta() {
  // #ifdef H5
  const v = videoRef.value;
  if (v && v.duration && !Number.isNaN(v.duration)) {
    currentSec.value = v.currentTime;
  }
  // #endif
}

function seekTo(sec: number, id: string) {
  activeId.value = id;
  currentSec.value = sec;
  // #ifdef H5
  const v = videoRef.value;
  if (v) {
    v.currentTime = sec;
    void v.play();
  }
  // #endif
  // #ifndef H5
  uni.showToast({ title: `跳转至 ${formatSeconds(sec)}`, icon: "none" });
  // #endif
}

function onBarTap(e: MouseEvent | TouchEvent) {
  // #ifdef H5
  const target = (e.currentTarget as HTMLElement);
  const rect = target.getBoundingClientRect();
  const clientX = "touches" in e ? e.touches[0]?.clientX : e.clientX;
  if (clientX == null) return;
  const ratio = (clientX - rect.left) / rect.width;
  seekTo(ratio * props.durationSec, "");
  // #endif
}

watch(
  () => props.seekSeconds,
  (sec) => {
    if (sec != null && sec >= 0) seekTo(sec, "");
  },
);

defineExpose({ seekTo });
</script>

<style scoped>
.vpt {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
@media (min-width: 900px) {
  .vpt {
    flex-direction: row;
    align-items: flex-start;
  }
  .vpt-player-col {
    flex: 1.2;
  }
  .vpt-list-col {
    flex: 0.8;
    max-height: 360px;
    overflow-y: auto;
  }
}

.vpt-player-wrap {
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  background: #0f172a;
  aspect-ratio: 16 / 9;
}
.vpt-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.vpt-placeholder {
  width: 100%;
  height: 100%;
  min-height: 320rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #1e293b;
}
.vpt-ph-ico {
  font-size: 64rpx;
}
.vpt-ph-txt {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #94a3b8;
}

.vpt-lock {
  position: absolute;
  left: 12rpx;
  bottom: 12rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
}
.vpt-lock-ico {
  font-size: 20rpx;
}
.vpt-lock-txt {
  font-size: 20rpx;
  color: #e2e8f0;
  font-weight: 600;
}

.vpt-bar {
  margin-top: 16rpx;
}
.vpt-bar-track {
  position: relative;
  height: 8rpx;
  border-radius: 4rpx;
  background: #e2e8f0;
  cursor: pointer;
}
.vpt-bar-fill {
  height: 100%;
  border-radius: 4rpx;
  background: #2563eb;
  transition: width 0.1s linear;
}
.vpt-dot {
  position: absolute;
  top: 50%;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  border: 2rpx solid #fff;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.2);
}
.vpt-dot--warning {
  background: #f59e0b;
}
.vpt-dot--highlight {
  background: #10b981;
}
.vpt-dot--question {
  background: #6366f1;
}
.vpt-dot--turn {
  background: #2563eb;
}
.vpt-time {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
  text-align: right;
}

.vpt-actions {
  margin-top: 16rpx;
}
.vpt-action {
  display: inline-flex;
  padding: 14rpx 24rpx;
  border-radius: 12rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
  cursor: pointer;
}
.vpt-action text {
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
}

.vpt-list-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16rpx;
}
.vpt-event {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  padding: 16rpx;
  margin-bottom: 12rpx;
  border-radius: 12rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
  transition: all 0.25s ease;
  cursor: pointer;
}
.vpt-event--active {
  background: #eff6ff;
  border-color: #2563eb;
  box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.12);
}
.vpt-event-time {
  font-size: 24rpx;
  font-weight: 800;
  color: #2563eb;
  flex-shrink: 0;
}
.vpt-event-lbl {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
}
.vpt-event-type {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #64748b;
}
</style>
