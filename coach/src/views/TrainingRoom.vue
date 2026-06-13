<template>
  <div class="tr">
    <header class="tr-head">
      <div class="tr-head-left">
        <h1 class="tr-title">{{ roomTitle }}</h1>
        <p class="tr-sub">{{ scenarioLabel }}</p>
      </div>
      <div class="tr-head-right">
        <div class="tr-conn" :class="`tr-conn--${connectionState}`">
          <span class="tr-conn-dot" />
          <span>{{ connLabel }}</span>
        </div>
        <span class="tr-elapsed">{{ elapsed }}</span>
      </div>
    </header>

    <div class="tr-body">
      <div class="tr-stage">
        <div class="tr-video-wrap">
          <VideoPane
            :label="mainPane.label"
            size="main"
            :connected="mainPane.connected"
            :is-local="mainPane.isLocal"
            :camera-off="mainPane.isLocal && cameraOff"
            :status-text="mainPane.statusText"
            :stream="mainPane.stream"
          />
          <VideoPane
            :label="pipPane.label"
            size="pip"
            :connected="pipPane.connected"
            :is-local="pipPane.isLocal"
            :camera-off="pipPane.isLocal && cameraOff"
            :status-text="pipPane.statusText"
            :stream="pipPane.stream"
            @tap="swapFocus"
          />
        </div>
        <div class="tr-wave-col">
          <VoiceWaveform :active="connectionState === 'connected'" />
        </div>
      </div>

      <CoachToolbox
        :materials="materials"
        :messages="messages"
        :countdown-active="countdownActive"
        :countdown-left="countdownLeft"
        :countdown-percent="countdownPercent"
        @countdown="startCountdown"
        @stop-countdown="stopCountdown"
        @interrupt="doInterrupt"
        @question="doQuestion"
        @chat="sendChat"
      />
    </div>

    <p v-if="highlightCount" class="tr-hl-badge">已捕捉 {{ highlightCount }} 段高光</p>

    <div class="tr-toolbar-wrap">
      <RoomToolbar :muted="muted" :camera-off="cameraOff" @action="onToolbar" />
    </div>

    <HighlightFlash :visible="highlightFlash" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import VideoPane from "@/components/training/VideoPane.vue";
import RoomToolbar from "@/components/training/RoomToolbar.vue";
import VoiceWaveform from "@/components/training/VoiceWaveform.vue";
import HighlightFlash from "@/components/training/HighlightFlash.vue";
import CoachToolbox from "@/components/training/CoachToolbox.vue";
import { saveHighlightClip } from "@/api/modules/trainingRoom";
import {
  startLocalMedia,
  stopLocalMedia,
  setLocalAudioEnabled,
  setLocalVideoEnabled,
  setWebRTCHooks,
} from "@/utils/training/webrtc";
import {
  fetchJoinInfo,
  fetchRoomStatus,
  recordRoomJoin,
  leaveRoom,
  endRoomByCoach,
  postPressureCountdown,
  postPressureInterrupt,
  postPressureQuestion,
  postRoomChat,
} from "@/api/modules/videoConference";
import { fetchMaterials, type MaterialItem } from "@/api/modules/materials";
import type { RoomJoinInfo } from "@/types/videoConference";

type ConnectionState = "idle" | "connecting" | "connected" | "failed";
type FocusPane = "remote" | "local";

const route = useRoute();
const router = useRouter();
const roomId = (route.params.roomId as string) || "";

const joinInfo = ref<RoomJoinInfo | null>(null);
const sessionId = ref(`sess-${Date.now()}`);
const connectionState = ref<ConnectionState>("idle");
const focusPane = ref<FocusPane>("remote");

const localStream = ref<MediaStream | null>(null);
const remoteConnected = ref(false);
const remoteStatusText = ref("等待学员加入…");

const muted = ref(false);
const cameraOff = ref(false);
const highlightFlash = ref(false);
const highlightCount = ref(0);

const elapsed = ref("00:00");
let elapsedTimer: ReturnType<typeof setInterval> | null = null;
let peerPollTimer: ReturnType<typeof setInterval> | null = null;

const countdownActive = ref(false);
const countdownLeft = ref(0);
const countdownTotal = ref(60);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

const messages = ref<{ from: string; text: string }[]>([]);
const materials = ref<MaterialItem[]>([]);

const countdownPercent = computed(() =>
  countdownTotal.value ? (countdownLeft.value / countdownTotal.value) * 100 : 0,
);

const roomTitle = computed(() => joinInfo.value?.sceneName || "训练房间");
const scenarioLabel = computed(() => {
  const peer = joinInfo.value?.peer?.nickname;
  return peer ? `与 ${peer} 的陪练` : "实战陪练";
});

const connLabel = computed(() => {
  if (connectionState.value === "connecting") return "等待学员…";
  if (connectionState.value === "connected") return "已连接";
  if (connectionState.value === "failed") return "连接失败";
  return "准备中";
});

function paneConfig(which: FocusPane) {
  const isLocal = which === "local";
  const connected = isLocal ? Boolean(localStream.value) : remoteConnected.value;
  const peerName = joinInfo.value?.peer?.nickname || "学员";
  return {
    label: isLocal ? "我" : peerName,
    connected,
    isLocal,
    stream: isLocal ? localStream.value : null,
    statusText: isLocal ? "" : remoteStatusText.value,
  };
}

const mainPane = computed(() => paneConfig(focusPane.value));
const pipPane = computed(() => paneConfig(focusPane.value === "remote" ? "local" : "remote"));

function swapFocus() {
  if (connectionState.value !== "connected" && !localStream.value) return;
  focusPane.value = focusPane.value === "remote" ? "local" : "remote";
}

function updateElapsed() {
  const startedAt = joinInfo.value?.startedAt;
  if (!startedAt) return;
  const start = new Date(startedAt).getTime();
  if (Number.isNaN(start)) return;
  const diff = Math.floor((Date.now() - start) / 1000);
  elapsed.value = `${String(Math.floor(diff / 60)).padStart(2, "0")}:${String(diff % 60).padStart(2, "0")}`;
}

function startPeerPolling() {
  if (!roomId) return;
  stopPeerPolling();
  peerPollTimer = setInterval(async () => {
    try {
      const status = await fetchRoomStatus(roomId);
      const student = status.participants?.find((p) => p.role === "USER" && p.joined);
      if (student) {
        remoteConnected.value = true;
        remoteStatusText.value = "";
        connectionState.value = "connected";
        stopPeerPolling();
      }
    } catch {
      /* ignore */
    }
  }, 3000);
}

function stopPeerPolling() {
  if (peerPollTimer) {
    clearInterval(peerPollTimer);
    peerPollTimer = null;
  }
}

async function initMedia() {
  setWebRTCHooks({
    onTrack: (stream, kind) => {
      if (kind === "local") localStream.value = stream;
    },
  });
  try {
    await startLocalMedia();
    connectionState.value = remoteConnected.value ? "connected" : "connecting";
    if (joinInfo.value?.canEnter) {
      await recordRoomJoin(roomId);
    }
  } catch {
    connectionState.value = "failed";
  }
}

async function initSession() {
  if (!roomId) {
    connectionState.value = "failed";
    router.replace("/orders");
    return;
  }
  connectionState.value = "connecting";
  try {
    joinInfo.value = await fetchJoinInfo(roomId);
    if (joinInfo.value.peer?.nickname) {
      remoteStatusText.value = `等待 ${joinInfo.value.peer.nickname} 加入…`;
    }
    if (joinInfo.value.canEnter === false) {
      throw new Error(joinInfo.value.denyReason || "暂不可进入训练房间");
    }
    updateElapsed();
    elapsedTimer = setInterval(updateElapsed, 1000);
    materials.value = await fetchMaterials(roomId);
    startPeerPolling();
    await initMedia();
  } catch (e) {
    connectionState.value = "failed";
    remoteStatusText.value = e instanceof Error ? e.message : "进房失败";
  }
}

function onToolbar(id: string) {
  if (id === "mute") {
    muted.value = !muted.value;
    setLocalAudioEnabled(!muted.value);
  } else if (id === "camera") {
    cameraOff.value = !cameraOff.value;
    setLocalVideoEnabled(!cameraOff.value);
  } else if (id === "highlight") {
    captureHighlight();
  } else if (id === "hangup") {
    endSession();
  }
}

async function captureHighlight() {
  highlightFlash.value = true;
  setTimeout(() => {
    highlightFlash.value = false;
  }, 480);
  await saveHighlightClip({
    sessionId: sessionId.value,
    roomId,
    atMs: Date.now(),
    durationSec: 15,
  });
  highlightCount.value += 1;
}

function endSession() {
  if (!window.confirm("确定结束训练并离开房间吗？")) return;
  void (async () => {
    stopPeerPolling();
    stopLocalMedia();
    stopCountdown();
    try {
      const result = await endRoomByCoach(roomId);
      await leaveRoom(roomId, "COACH_HANGUP");
      const orderId = joinInfo.value?.orderId || result.trainingId;
      router.replace(`/submit-feedback/${orderId}`);
    } catch {
      router.replace("/orders");
    }
  })();
}

function startCountdown(seconds: number) {
  countdownActive.value = true;
  countdownTotal.value = seconds;
  countdownLeft.value = seconds;
  if (countdownTimer) clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    countdownLeft.value--;
    if (countdownLeft.value <= 0) stopCountdown();
  }, 1000);
  postPressureCountdown(roomId, { action: "start", seconds }).catch(() => {});
}

function stopCountdown() {
  countdownActive.value = false;
  if (countdownTimer) clearInterval(countdownTimer);
  postPressureCountdown(roomId, { action: "stop" }).catch(() => {});
}

function doInterrupt(message: string) {
  messages.value.push({ from: "coach", text: `[打断] ${message}` });
  postPressureInterrupt(roomId, message).catch(() => {});
}

function doQuestion(text: string) {
  messages.value.push({ from: "coach", text: `[提问] ${text}` });
  postPressureQuestion(roomId, { text }).catch(() => {});
}

function sendChat(text: string) {
  messages.value.push({ from: "coach", text });
  postRoomChat(roomId, text).catch(() => {});
}

onMounted(() => {
  initSession();
});

onBeforeUnmount(() => {
  stopPeerPolling();
  stopLocalMedia();
  if (elapsedTimer) clearInterval(elapsedTimer);
  if (countdownTimer) clearInterval(countdownTimer);
});
</script>

<style scoped>
.tr {
  min-height: 100vh;
  background: #111827;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}
.tr-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px 10px;
  flex-shrink: 0;
}
.tr-head-left {
  min-width: 0;
}
.tr-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #f9fafb;
}
.tr-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #6b7280;
}
.tr-head-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.tr-conn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 12px;
  color: #9ca3af;
}
.tr-conn-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6b7280;
}
.tr-conn--connecting .tr-conn-dot {
  background: #f59e0b;
  animation: trBlink 1s ease-in-out infinite;
}
.tr-conn--connected .tr-conn-dot {
  background: #22c55e;
}
.tr-conn--connected {
  color: #86efac;
}
.tr-elapsed {
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}
@keyframes trBlink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.35;
  }
}

.tr-body {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.tr-stage {
  flex: 1;
  display: flex;
  padding: 0 16px;
  gap: 12px;
  min-height: 0;
}
.tr-video-wrap {
  flex: 1;
  position: relative;
  min-height: 0;
  border-radius: 16px;
  overflow: hidden;
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.06);
}
.tr-wave-col {
  width: 88px;
  flex-shrink: 0;
  align-self: stretch;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tr-hl-badge {
  margin: 0;
  text-align: center;
  padding: 6px;
  font-size: 13px;
  color: #fcd34d;
  font-weight: 600;
  flex-shrink: 0;
}

.tr-toolbar-wrap {
  padding: 16px 20px 20px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}
</style>
