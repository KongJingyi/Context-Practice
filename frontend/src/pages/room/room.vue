<template>
  <view class="page">
    <view class="header">
      <text class="title">{{ title }}</text>
      <text class="meta">训练房间 · {{ scenario }}</text>
    </view>

    <view class="video-shell">
      <!-- #ifdef H5 -->
      <view class="video-area h5">
        <text class="video-hint">H5：沉浸式视频模拟区（接入 WebRTC / 点播流）</text>
      </view>
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <view class="video-area mp">
        <text class="video-hint">小程序：碎片化陪练（live-player / 第三方音视频）</text>
      </view>
      <!-- #endif -->
    </view>

    <view class="panel">
      <view class="panel-head">
        <text class="panel-title">实时表达建议</text>
        <text class="panel-sub">逻辑纠错与临场提示看板</text>
      </view>
      <scroll-view scroll-y class="panel-body">
        <view v-for="(line, i) in tips" :key="i" class="tip-row">
          <text class="dot">·</text>
          <text class="tip-text">{{ line }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { consumePendingTraining } from "@/utils/trainingBridge.js";

const scenario = ref("");
const title = ref("训练房间");

const tips = ref([
  "开场先给结论，再补 1～2 个支撑点，避免平铺细节。",
  "对方打断时，用「好的，我收一下」承接，再拉回你的主线。",
  "数字与期限尽量具体，少用「尽快」「差不多」。",
  "结尾复述共识与下一步 owner / 时间点。",
]);

onShow(() => {
  const p = consumePendingTraining();
  if (p) {
    scenario.value = p.scenarioId;
    title.value = p.roomTitle || "训练房间";
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #0f172a;
  box-sizing: border-box;
}
.header {
  margin-bottom: 20rpx;
}
.title {
  font-size: 34rpx;
  font-weight: 700;
  color: #f8fafc;
  display: block;
}
.meta {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #94a3b8;
  display: block;
}
.video-shell {
  border-radius: 20rpx;
  overflow: hidden;
  background: #1e293b;
}
.video-area {
  width: 100%;
  height: 420rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  box-sizing: border-box;
}
.video-area.h5 {
  background: linear-gradient(145deg, #1e293b, #0f172a);
}
.video-area.mp {
  background: linear-gradient(145deg, #312e81, #1e1b4b);
}
.video-hint {
  color: #e2e8f0;
  font-size: 26rpx;
  text-align: center;
  line-height: 1.55;
}
.panel {
  margin-top: 24rpx;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx 24rpx 12rpx;
  box-sizing: border-box;
}
.panel-head {
  margin-bottom: 12rpx;
}
.panel-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #0f172a;
  display: block;
}
.panel-sub {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #64748b;
  display: block;
}
.panel-body {
  max-height: 360rpx;
}
.tip-row {
  display: flex;
  gap: 8rpx;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}
.dot {
  color: #6366f1;
  font-size: 28rpx;
  line-height: 1.4;
}
.tip-text {
  flex: 1;
  font-size: 26rpx;
  color: #334155;
  line-height: 1.5;
}
</style>
