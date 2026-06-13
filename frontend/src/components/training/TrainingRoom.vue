<template>
  <view class="tr">
    <view class="tr-head">
      <view class="tr-head-left">
        <text class="tr-title">{{ roomTitle }}</text>
        <text class="tr-sub">{{ scenarioLabel }}</text>
      </view>
      <view class="tr-conn" :class="`tr-conn--${connectionState}`">
        <view class="tr-conn-dot" />
        <text>{{ connLabel }}</text>
      </view>
    </view>

    <view class="tr-stage">
      <view class="tr-video-wrap">
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
        <RoomPressureOverlay
          :countdown-active="pressureCountdownActive"
          :countdown-left="pressureCountdownLeft"
          :interrupt-message="pressureInterrupt"
          :question-text="pressureQuestion"
        />
        <WhiteboardOverlay
          :visible="whiteboardActive"
          :strokes="whiteboardStrokes"
          :version="whiteboardVersion"
        />
      </view>

      <view class="tr-wave-col">
        <VoiceWaveform :active="connectionState === 'connected'" />
      </view>
    </view>

    <view v-if="highlightCount" class="tr-hl-badge">
      <text>已捕捉 {{ highlightCount }} 段高光</text>
    </view>

    <view class="tr-toolbar-wrap">
      <RoomToolbar
        :muted="muted"
        :camera-off="cameraOff"
        @action="onToolbar"
      />
    </view>

    <HighlightFlash :visible="highlightFlash" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import VideoPane from "@/components/training/VideoPane.vue";
import RoomToolbar from "@/components/training/RoomToolbar.vue";
import VoiceWaveform from "@/components/training/VoiceWaveform.vue";
import HighlightFlash from "@/components/training/HighlightFlash.vue";
import RoomPressureOverlay from "@/components/training/RoomPressureOverlay.vue";
import WhiteboardOverlay from "@/components/training/WhiteboardOverlay.vue";
import { saveHighlightClip } from "@/api/modules/trainingRoom.js";
import {
  startLocalMedia,
  stopLocalMedia,
  setLocalAudioEnabled,
  setLocalVideoEnabled,
} from "@/utils/training/webrtc";
import {
  canUseTrtc,
  trtcFallbackHint,
  startTrtcSession,
  stopTrtcSession,
  setTrtcAudioEnabled,
  setTrtcVideoEnabled,
} from "@/utils/training/trtcSession.js";
import { canUseCamera, cameraBlockedHint } from "@/utils/training/mediaSecurity.js";
import { fetchJoinInfo, fetchRoomStatus, leaveRoom, endTraining, recordRoomJoin, fetchRoomState } from "@/api/modules/videoConference.js";
import { setPendingReview } from "@/utils/review/bridge.js";
import type { ConnectionState, FocusPane } from "@/types/training/room";

const props = withDefaults(
  defineProps<{
    roomTitle?: string;
    scenarioId?: string;
    expertName?: string;
    expertTitle?: string;
    orderNo?: string;
    orderId?: string;
    roomId?: string;
  }>(),
  {
    roomTitle: "训练房间",
    scenarioId: "",
    expertName: "语境专家",
    expertTitle: "",
    orderNo: "",
    orderId: "",
    roomId: "",
  },
);

const sessionId = ref(`sess-${Date.now()}`);
const sessionStartedAt = ref("");
const connectionState = ref<ConnectionState>("idle");
const focusPane = ref<FocusPane>("remote");

const localStream = ref<MediaStream | null>(null);
const remoteStream = ref<MediaStream | null>(null);
const remoteConnected = ref(false);
const usingTrtc = ref(false);

const muted = ref(false);
const cameraOff = ref(false);

const highlightFlash = ref(false);
const highlightCount = ref(0);

const remoteStatusText = ref("等待专家加入…");

let peerPollTimer: ReturnType<typeof setInterval> | null = null;
let statePollTimer: ReturnType<typeof setInterval> | null = null;

const pressureCountdownActive = ref(false);
const pressureCountdownLeft = ref(0);
const pressureInterrupt = ref("");
const pressureQuestion = ref("");
const whiteboardActive = ref(false);
const whiteboardStrokes = ref<{ id: string; color: string; width: number; points: { x: number; y: number }[] }[]>([]);
const whiteboardVersion = ref(0);

const scenarioLabel = computed(() => props.scenarioId || "实战陪练");
const connLabel = computed(() => {
  if (!usingTrtc.value && localStream.value) return "仅本地预览";
  if (connectionState.value === "connecting") return "正在呼叫专家…";
  if (connectionState.value === "connected") return "已连接";
  if (connectionState.value === "failed") return "连接失败";
  return "准备中";
});

function paneConfig(which: FocusPane) {
  const isLocal = which === "local";
  const connected = isLocal ? Boolean(localStream.value) : remoteConnected.value;
  return {
    label: isLocal ? "我" : props.expertName,
    connected,
    isLocal,
    stream: isLocal ? localStream.value : remoteStream.value,
    statusText: isLocal ? "" : remoteStatusText.value,
  };
}

const mainPane = computed(() => paneConfig(focusPane.value));
const pipPane = computed(() => paneConfig(focusPane.value === "remote" ? "local" : "remote"));

function swapFocus() {
  if (connectionState.value !== "connected" && !localStream.value) return;
  focusPane.value = focusPane.value === "remote" ? "local" : "remote";
}

async function initSession() {
  if (!props.roomId) {
    connectionState.value = "failed";
    uni.showToast({ title: "缺少房间信息，请从订单进入", icon: "none" });
    return;
  }
  connectionState.value = "connecting";
  try {
    const joinInfo = await fetchJoinInfo(props.roomId);
    sessionStartedAt.value = joinInfo.startedAt || "";
    if (joinInfo.peer?.nickname) {
      remoteStatusText.value = `等待 ${joinInfo.peer.nickname} 加入…`;
    }
    if (joinInfo.canEnter === false) {
      throw new Error(joinInfo.denyReason || "暂不可进入训练房间");
    }
    await initMedia(joinInfo);
    startStatePolling();
  } catch (e) {
    connectionState.value = "failed";
    uni.showToast({
      title: e instanceof Error ? e.message : "进房失败",
      icon: "none",
    });
  }
}

function startPeerPolling() {
  if (!props.roomId) return;
  stopPeerPolling();
  peerPollTimer = setInterval(async () => {
    try {
      const status = await fetchRoomStatus(props.roomId!);
      const coach = status.participants?.find((p) => p.role === "COACH" && p.joined);
      if (coach && !usingTrtc.value) {
        remoteStatusText.value =
          "陪练已进房，但 TRTC 未连接，无法看到/听到对方。请检查浏览器控制台 [TRTC] 报错。";
      }
    } catch {
      /* ignore poll errors */
    }
  }, 3000);
}

function stopPeerPolling() {
  if (peerPollTimer) {
    clearInterval(peerPollTimer);
    peerPollTimer = null;
  }
}

function startStatePolling() {
  if (!props.roomId) return;
  stopStatePolling();
  void pollRoomState();
  statePollTimer = setInterval(pollRoomState, 2000);
}

function stopStatePolling() {
  if (statePollTimer) {
    clearInterval(statePollTimer);
    statePollTimer = null;
  }
}

async function pollRoomState() {
  if (!props.roomId) return;
  try {
    const state = await fetchRoomState(props.roomId);
    const pm = state.pressureMode;
    if (pm?.countdown?.active) {
      pressureCountdownActive.value = true;
      pressureCountdownLeft.value = pm.countdown.secondsLeft ?? 0;
    } else {
      pressureCountdownActive.value = false;
    }
    if (pm?.lastInterrupt?.message) {
      pressureInterrupt.value = pm.lastInterrupt.message;
    }
    if (pm?.currentQuestion?.text) {
      pressureQuestion.value = pm.currentQuestion.text;
    }
    if (state.whiteboard) {
      whiteboardActive.value = state.whiteboard.active;
      if (state.whiteboard.version !== whiteboardVersion.value) {
        whiteboardVersion.value = state.whiteboard.version;
        whiteboardStrokes.value = state.whiteboard.strokes || [];
      }
    }
  } catch {
    /* ignore */
  }
}

async function initMediaFallback() {
  try {
    // #ifdef H5
    const stream = await startLocalMedia();
    localStream.value = stream;
    connectionState.value = usingTrtc.value && remoteConnected.value ? "connected" : "connecting";
    // #endif
    // #ifndef H5
    connectionState.value = "connecting";
    uni.showToast({ title: "请在 H5 浏览器中使用摄像头", icon: "none" });
    // #endif
  } catch (e) {
    connectionState.value = "failed";
    const msg =
      e instanceof Error ? e.message : "无法打开摄像头，请检查浏览器权限";
    uni.showToast({ title: msg, icon: "none", duration: 4000 });
  }
}

async function initTrtcMedia(joinInfo: {
  sdkAppId: number;
  trtcUserId: string;
  userSig: string;
}) {
  // #ifdef H5
  usingTrtc.value = true;
  await startTrtcSession(
    {
      sdkAppId: joinInfo.sdkAppId,
      userId: joinInfo.trtcUserId,
      userSig: joinInfo.userSig,
      roomId: props.roomId || joinInfo.roomId,
    },
    {
      onLocalStream: (stream) => {
        localStream.value = stream;
      },
      onRemoteStream: (stream) => {
        remoteStream.value = stream;
        remoteConnected.value = true;
        remoteStatusText.value = "";
        connectionState.value = "connected";
        stopPeerPolling();
      },
      onRemoteUserJoined: () => {
        remoteConnected.value = true;
        remoteStatusText.value = "";
        connectionState.value = "connected";
        stopPeerPolling();
      },
      onError: (err) => {
        console.warn("[TRTC]", err.message);
        remoteStatusText.value = `TRTC 错误：${err.message}`;
      },
    },
  );
  connectionState.value = remoteConnected.value ? "connected" : "connecting";
  await recordRoomJoin(props.roomId!);
  // #endif
}

async function initMedia(joinInfo: {
  sdkAppId: number;
  trtcUserId: string;
  userSig: string;
}) {
  // #ifdef H5
  if (!canUseCamera()) {
    connectionState.value = "failed";
    uni.showToast({ title: cameraBlockedHint(), icon: "none", duration: 5000 });
    return;
  }
  if (canUseTrtc(joinInfo.userSig)) {
    try {
      await initTrtcMedia(joinInfo);
      return;
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      console.warn("[TRTC] fallback to local media:", e);
      usingTrtc.value = false;
      await stopTrtcSession();
      remoteStatusText.value = `${trtcFallbackHint(joinInfo.userSig)}（${msg}）`;
      uni.showToast({ title: trtcFallbackHint(joinInfo.userSig), icon: "none", duration: 5000 });
    }
  } else {
    remoteStatusText.value = trtcFallbackHint(joinInfo.userSig);
    uni.showToast({ title: remoteStatusText.value, icon: "none", duration: 5000 });
    console.warn("[TRTC]", remoteStatusText.value);
  }
  startPeerPolling();
  await initMediaFallback();
  // #endif
  // #ifndef H5
  await initMediaFallback();
  // #endif
}

function onToolbar(id: string) {
  if (id === "mute") {
    muted.value = !muted.value;
    if (usingTrtc.value) setTrtcAudioEnabled(!muted.value);
    else setLocalAudioEnabled(!muted.value);
  } else if (id === "camera") {
    cameraOff.value = !cameraOff.value;
    if (usingTrtc.value) setTrtcVideoEnabled(!cameraOff.value);
    else setLocalVideoEnabled(!cameraOff.value);
  } else if (id === "highlight") {
    captureHighlight();
  } else if (id === "hangup") {
    endSession();
  }
}

function getElapsedSec() {
  if (!sessionStartedAt.value) return 0;
  const start = new Date(sessionStartedAt.value).getTime();
  if (Number.isNaN(start)) return 0;
  return Math.floor((Date.now() - start) / 1000);
}

async function captureHighlight() {
  highlightFlash.value = true;
  setTimeout(() => {
    highlightFlash.value = false;
  }, 480);
  await saveHighlightClip({
    sessionId: sessionId.value,
    roomId: props.roomId,
    startSec: getElapsedSec(),
    durationSec: 15,
  });
  highlightCount.value += 1;
  uni.showToast({ title: "已记录一段精彩表达（15s）", icon: "success" });
}

function endSession() {
  uni.showModal({
    title: "结束训练",
    content: "确定退出训练房间吗？",
    success: async (res) => {
      if (!res.confirm) return;
      stopPeerPolling();
      if (usingTrtc.value) await stopTrtcSession();
      else stopLocalMedia();
      if (props.roomId) {
        try {
          await leaveRoom(props.roomId);
          await endTraining({
            roomId: props.roomId,
            sceneName: props.roomTitle,
            transcript: "",
          });
        } catch {
          /* 仍跳转复盘页 */
        }
      }
      setPendingReview({
        orderId: props.orderId || props.scenarioId || `sess-${sessionId.value}`,
        orderNo: props.orderNo,
        expertId: "c_default",
        expertName: props.expertName,
        expertTitle: props.expertTitle,
        sceneTag: props.roomTitle,
      });
      uni.redirectTo({
        url: "/pages/post-training-review/post-training-review",
        fail: () => uni.switchTab({ url: "/pages/my-orders/my-orders" }),
      });
    },
  });
}

onMounted(() => {
  initSession();
});

onBeforeUnmount(() => {
  stopPeerPolling();
  stopStatePolling();
  if (usingTrtc.value) void stopTrtcSession();
  else stopLocalMedia();
});
</script>

<style scoped>
.tr {
  min-height: 100vh;
  background: #111827;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.tr-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx 12rpx;
  flex-shrink: 0;
}
.tr-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #f9fafb;
}
.tr-sub {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #6b7280;
}
.tr-conn {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.06);
  font-size: 22rpx;
  color: #9ca3af;
}
.tr-conn-dot {
  width: 12rpx;
  height: 12rpx;
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
@keyframes trBlink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.35;
  }
}

.tr-stage {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: stretch;
  padding: 0 20rpx;
  gap: 14rpx;
  min-height: 0;
}
.tr-video-wrap {
  flex: 1;
  position: relative;
  min-height: 56vh;
  height: 100%;
  border-radius: 24rpx;
  overflow: hidden;
  background: #0f172a;
  border: 1rpx solid rgba(255, 255, 255, 0.06);
}
.tr-wave-col {
  width: 80rpx;
  flex-shrink: 0;
  align-self: stretch;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.06);
}

.tr-hl-badge {
  text-align: center;
  padding: 8rpx;
  font-size: 22rpx;
  color: #fcd34d;
  font-weight: 600;
  flex-shrink: 0;
}

.tr-toolbar-wrap {
  padding: 24rpx 24rpx 20rpx;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

/* #ifdef H5 */
@media (min-width: 768px) {
  .tr-video-wrap {
    min-height: calc(100vh - 220px);
  }
  .tr-wave-col {
    width: 88px;
  }
}
/* #endif */
</style>
