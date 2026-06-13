<template>
  <view class="rd">
    <view class="rd-toolbar">
      <view class="rd-export" @tap="exportPdf">
        <text>导出 PDF</text>
      </view>
    </view>

    <view v-if="loading" class="rd-loading">
      <text>正在加载报告…</text>
    </view>

    <view v-else-if="report" id="report-print-root" class="rd-body">
      <view class="rd-header">
        <text class="rd-scene">{{ report.orderInfo.scene }}</text>
        <text class="rd-meta">
          {{ report.orderInfo.date }} · {{ report.orderInfo.expertName }} · {{ report.orderInfo.orderNo }}
        </text>
      </view>

      <view class="rd-encourage" :class="{ 'rd-encourage--show': revealed }">
        <text>{{ report.encouragement }}</text>
      </view>

      <ReportScoreRing
        :score="report.scores.total"
        :compare="report.scores.averageCompare"
        :animate="true"
      />

      <view class="rd-split" :class="{ 'rd-split--show': revealed }">
        <view class="rd-card rd-radar">
          <text class="rd-card-title">五维能力雷达</text>
          <ReportRadarCompare
            :labels="report.scores.dimensionLabels"
            :session="report.scores.sessionValues"
            :average="report.scores.averageValues"
          />
        </view>
        <view class="rd-card rd-feedback">
          <ExpertFeedbackList
            :items="report.expertFeedback"
            :visible="revealed"
            @jump="onJumpToVideo"
          />
        </view>
      </view>

      <view class="rd-section" :class="{ 'rd-section--show': revealed }">
        <text class="rd-section-title">交互式录像回放</text>
        <VideoPlayerWithTimeline
          ref="playerRef"
          :video-url="report.videoUrl"
          :duration-sec="report.videoDurationSec"
          :markers="report.timelineMarkers"
          :seek-seconds="seekSeconds"
          @export-highlight="onExportHighlight"
        />
      </view>

      <view v-if="report.milestone" class="rd-milestone" :class="{ 'rd-milestone--show': revealed }">
        <text>🏅 {{ report.milestone }}</text>
      </view>

      <view class="rd-section" :class="{ 'rd-section--show': revealed }">
        <AbilityProgressBoard
          :labels="report.scores.dimensionLabels"
          :initial="report.scores.initialValues"
          :current="report.scores.currentValues"
          :improvements="report.scores.improvements"
        />
      </view>

      <view class="rd-section" :class="{ 'rd-section--show': revealed }">
        <GrowthTimeline :milestones="report.growthPath" :visible="revealed" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import ReportScoreRing from "@/components/report/ReportScoreRing.vue";
import ReportRadarCompare from "@/components/report/ReportRadarCompare.vue";
import ExpertFeedbackList from "@/components/report/ExpertFeedbackList.vue";
import VideoPlayerWithTimeline from "@/components/report/VideoPlayerWithTimeline.vue";
import AbilityProgressBoard from "@/components/report/AbilityProgressBoard.vue";
import GrowthTimeline from "@/components/report/GrowthTimeline.vue";
import { fetchReportDetail } from "@/api/modules/report.js";
import type { TrainingReportDetail } from "@/types/report";

const props = defineProps<{
  orderId: string;
}>();

const loading = ref(true);
const report = ref<TrainingReportDetail | null>(null);
const revealed = ref(false);
const seekSeconds = ref<number | undefined>(undefined);
const playerRef = ref<{ seekTo: (sec: number, id: string) => void } | null>(null);

onMounted(async () => {
  loading.value = true;
  report.value = await fetchReportDetail(props.orderId);
  loading.value = false;
  setTimeout(() => {
    revealed.value = true;
  }, 400);
});

function onJumpToVideo(seconds: number) {
  seekSeconds.value = seconds;
  playerRef.value?.seekTo(seconds, "");
  setTimeout(() => {
    seekSeconds.value = undefined;
  }, 100);
}

function onExportHighlight() {
  uni.showToast({ title: "高光片段已标记，稍后可在相册查看", icon: "success" });
}

function exportPdf() {
  // #ifdef H5
  const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>训练反馈报告</title>
<style>body{font-family:system-ui;padding:40px;color:#0f172a;line-height:1.7}
h1{color:#2563eb;font-size:22px}.meta{color:#64748b;font-size:14px;margin-bottom:24px}
.score{font-size:48px;font-weight:900;color:#2563eb}</style></head><body>
<h1>语境智练 · 结构化反馈报告</h1>
<p class="meta">${report.value?.orderInfo.scene ?? ""} · ${report.value?.orderInfo.date ?? ""}</p>
<p class="score">${report.value?.scores.total ?? "—"} 分</p>
<p>${report.value?.encouragement ?? ""}</p>
<p style="margin-top:24px;color:#64748b;font-size:12px">由语境智练自动生成 · 仅供个人复盘使用</p>
</body></html>`;
  const w = window.open("", "_blank");
  if (w) {
    w.document.write(html);
    w.document.close();
    w.print();
  }
  // #endif
  uni.showToast({ title: "请在打印对话框选择「另存为 PDF」", icon: "none", duration: 2500 });
}
</script>

<style scoped>
.rd {
  min-height: 100vh;
  background: #f8fafc;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
}
.rd-toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 16rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #e2e8f0;
}
.rd-export {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
  cursor: pointer;
}
.rd-export text {
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
}

.rd-loading {
  padding: 120rpx;
  text-align: center;
  color: #64748b;
  font-size: 28rpx;
}

.rd-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 24rpx;
  box-sizing: border-box;
}
.rd-header {
  text-align: center;
  margin-bottom: 16rpx;
}
.rd-scene {
  display: block;
  font-size: 36rpx;
  font-weight: 900;
  color: #0f172a;
}
.rd-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.rd-encourage {
  text-align: center;
  padding: 20rpx 24rpx;
  margin-bottom: 8rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  opacity: 0;
  transform: translateY(12rpx);
  transition: all 0.5s ease 0.2s;
}
.rd-encourage--show {
  opacity: 1;
  transform: translateY(0);
}
.rd-encourage text {
  font-size: 26rpx;
  font-weight: 600;
  color: #1d4ed8;
  line-height: 1.5;
}

.rd-split {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin: 24rpx 0;
  opacity: 0;
  transform: translateY(20rpx);
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1) 0.35s;
}
.rd-split--show {
  opacity: 1;
  transform: translateY(0);
}
@media (min-width: 900px) {
  .rd-split {
    flex-direction: row;
    align-items: flex-start;
  }
  .rd-radar {
    flex: 1;
  }
  .rd-feedback {
    flex: 1;
  }
}

.rd-card {
  padding: 24rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.04);
}
.rd-card-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 12rpx;
}

.rd-section {
  margin: 24rpx 0;
  opacity: 0;
  transform: translateY(20rpx);
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1) 0.5s;
}
.rd-section--show {
  opacity: 1;
  transform: translateY(0);
}
.rd-section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16rpx;
}

.rd-milestone {
  text-align: center;
  padding: 20rpx;
  margin: 24rpx 0;
  border-radius: 16rpx;
  background: #fffbeb;
  border: 1rpx solid #fde68a;
  opacity: 0;
  transition: opacity 0.5s ease 0.6s;
}
.rd-milestone--show {
  opacity: 1;
}
.rd-milestone text {
  font-size: 26rpx;
  font-weight: 700;
  color: #b45309;
}
</style>
