<template>
  <view class="ta">
    <view class="ta-head">
      <text class="ta-title">文稿 AI 优化</text>
      <text class="ta-sub">分屏对比 · 一键职场化改写</text>
    </view>

    <view class="ta-split">
      <view class="ta-pane">
        <text class="ta-pane-lbl">原始稿</text>
        <textarea
          v-model="inputText"
          class="ta-textarea"
          placeholder="粘贴讲稿或录音转文字结果…"
          :maxlength="5000"
        />
      </view>

      <view class="ta-mid">
        <view
          class="ta-btn"
          :class="{ 'ta-btn--loading': loading, 'ta-btn--done': optimizedText && !loading }"
          @tap="runOptimize"
        >
          <view class="ta-btn-shimmer" />
          <text>{{ loading ? 'AI 思考中…' : 'AI 智能优化' }}</text>
        </view>
      </view>

      <view class="ta-pane">
        <text class="ta-pane-lbl">优化建议</text>
        <view class="ta-output">
          <text v-if="!optimizedText && !loading" class="ta-placeholder">优化结果将在此显示</text>
          <text v-else-if="loading" class="ta-loading">正在分析逻辑结构与冗余表达…</text>
          <text v-else class="ta-result">{{ displayedText }}<text v-if="typing" class="ta-cursor">|</text></text>
        </view>
      </view>
    </view>

    <view v-if="changes.length" class="ta-changes">
      <text class="ta-changes-title">改动说明</text>
      <view v-for="(c, i) in changes" :key="i" class="ta-change">
        <text class="ta-change-reason">{{ c.reason }}</text>
      </view>
    </view>

    <view v-if="optimizedText" class="ta-actions">
      <view class="ta-action" @tap="copyOptimized">
        <text>一键复制优化稿</text>
      </view>
      <view class="ta-action ta-action--ghost" @tap="exportPdf">
        <text>导出 PDF</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { optimizeText } from "@/api/modules/practice.js";
import type { TextChange } from "@/types/practice";

const inputText = ref(
  "我觉得那个项目挺好的，因为团队氛围不错，然后我也学到了很多东西。呃，就是那个沟通方面吧，我觉得还可以再加强一下。",
);
const optimizedText = ref("");
const displayedText = ref("");
const changes = ref<TextChange[]>([]);
const loading = ref(false);
const typing = ref(false);

let typeTimer: ReturnType<typeof setInterval> | null = null;

async function runOptimize() {
  const text = inputText.value.trim();
  if (!text) {
    uni.showToast({ title: "请先输入文稿", icon: "none" });
    return;
  }
  loading.value = true;
  optimizedText.value = "";
  displayedText.value = "";
  changes.value = [];

  await new Promise((r) => setTimeout(r, 1200));

  const res = await optimizeText({ text });
  loading.value = false;
  optimizedText.value = res.optimized;
  changes.value = res.changes;
  typewriterReveal(res.optimized);
}

function typewriterReveal(full: string) {
  if (typeTimer) clearInterval(typeTimer);
  displayedText.value = "";
  typing.value = true;
  let i = 0;
  typeTimer = setInterval(() => {
    if (i >= full.length) {
      if (typeTimer) clearInterval(typeTimer);
      typeTimer = null;
      typing.value = false;
      return;
    }
    displayedText.value += full[i];
    i += 1;
  }, 28);
}

function copyOptimized() {
  if (!optimizedText.value) return;
  // #ifdef H5
  navigator.clipboard?.writeText(optimizedText.value).then(() => {
    uni.showToast({ title: "已复制到剪贴板", icon: "success" });
  });
  // #endif
  // #ifndef H5
  uni.setClipboardData({
    data: optimizedText.value,
    success: () => uni.showToast({ title: "已复制", icon: "success" }),
  });
  // #endif
}

function exportPdf() {
  if (!optimizedText.value) return;
  // #ifdef H5
  const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>优化文稿</title>
<style>body{font-family:system-ui;padding:40px;line-height:1.8;color:#0f172a}
h1{font-size:18px;color:#059669} .orig{color:#64748b;margin-bottom:24px;font-size:14px}
.main{font-size:16px;white-space:pre-wrap}</style></head><body>
<h1>语境智练 · AI 优化文稿</h1>
<p class="orig"><strong>原文：</strong>${escapeHtml(inputText.value)}</p>
<p class="main"><strong>优化稿：</strong><br>${escapeHtml(optimizedText.value)}</p>
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

function escapeHtml(s: string) {
  return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
</script>

<style scoped>
.ta {
  padding: 8rpx 0 32rpx;
}
.ta-head {
  margin-bottom: 24rpx;
}
.ta-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}
.ta-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.ta-split {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
@media (min-width: 768px) {
  .ta-split {
    flex-direction: row;
    align-items: stretch;
    gap: 12px;
  }
  .ta-mid {
    width: 120px;
    flex-shrink: 0;
  }
}

.ta-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 320rpx;
}
@media (min-width: 768px) {
  .ta-pane {
    min-height: 360px;
  }
}
.ta-pane-lbl {
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 12rpx;
}
.ta-textarea {
  flex: 1;
  min-height: 280rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  color: #0f172a;
  font-size: 26rpx;
  line-height: 1.6;
  box-sizing: border-box;
}
.ta-output {
  flex: 1;
  min-height: 280rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
  overflow-y: auto;
}
.ta-placeholder,
.ta-loading {
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.6;
}
.ta-loading {
  color: #2563eb;
  animation: taBlink 1s ease-in-out infinite;
}
@keyframes taBlink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
.ta-result {
  font-size: 26rpx;
  color: #0f172a;
  line-height: 1.7;
  white-space: pre-wrap;
  text-decoration: underline;
  text-decoration-color: rgba(37, 99, 235, 0.45);
  text-underline-offset: 4rpx;
}
.ta-cursor {
  color: #2563eb;
  animation: taCursor 0.8s step-end infinite;
}
@keyframes taCursor {
  50% {
    opacity: 0;
  }
}

.ta-mid {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
}
.ta-btn {
  position: relative;
  overflow: hidden;
  padding: 20rpx 32rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6, #60a5fa);
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.3);
  cursor: pointer;
}
.ta-btn text {
  position: relative;
  z-index: 1;
  font-size: 26rpx;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}
.ta-btn--loading {
  opacity: 0.85;
}
.ta-btn-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    transparent 40%,
    rgba(255, 255, 255, 0.35) 50%,
    transparent 60%
  );
  animation: taShimmer 2.2s ease-in-out infinite;
}
@keyframes taShimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.ta-changes {
  margin-top: 24rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
}
.ta-changes-title {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 12rpx;
}
.ta-change {
  margin-bottom: 8rpx;
}
.ta-change-reason {
  font-size: 24rpx;
  color: #64748b;
}

.ta-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 24rpx;
}
.ta-action {
  flex: 1;
  padding: 20rpx;
  text-align: center;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  cursor: pointer;
}
.ta-action--ghost {
  background: #ffffff;
  border: 1rpx solid #dbeafe;
}
.ta-action text {
  font-size: 26rpx;
  font-weight: 700;
  color: #fff;
}
.ta-action--ghost text {
  color: #2563eb;
}
</style>
