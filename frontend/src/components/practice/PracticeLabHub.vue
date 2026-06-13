<template>
  <view class="plh">
    <view class="plh-hero">
      <view class="plh-hero-glow" />
      <text class="plh-hero-title">练习实验室</text>
      <text class="plh-hero-sub">先自测 · 再对练 · 低价高频磨表达</text>
    </view>

    <view class="plh-tabs">
      <view
        v-for="t in tabs"
        :key="t.id"
        class="plh-tab"
        :class="{ 'plh-tab--on': activeTab === t.id }"
        @tap="activeTab = t.id"
      >
        <text class="plh-tab-ico">{{ t.icon }}</text>
        <text class="plh-tab-txt">{{ t.label }}</text>
      </view>
    </view>

    <view class="plh-body">
      <QuestionSquare v-if="activeTab === 'square'" @pick="onPickQuestion" />
      <VoicePractice v-else-if="activeTab === 'voice'" :question-text="currentQuestion" />
      <TextAI v-else />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import QuestionSquare from "@/components/practice/QuestionSquare.vue";
import VoicePractice from "@/components/practice/VoicePractice.vue";
import TextAI from "@/components/practice/TextAI.vue";
import { consumePracticeEntry } from "@/utils/practice/bridge.js";
import type { PracticeLabTab, PracticeQuestion } from "@/types/practice";

const tabs: { id: PracticeLabTab; label: string; icon: string }[] = [
  { id: "square", label: "题库广场", icon: "📚" },
  { id: "voice", label: "录音自测", icon: "🎙" },
  { id: "text", label: "文稿优化", icon: "✨" },
];

const activeTab = ref<PracticeLabTab>("square");
const currentQuestion = ref("");

function applyEntry() {
  const entry = consumePracticeEntry();
  if (!entry) return;
  if (entry.tab) activeTab.value = entry.tab;
  if (entry.questionText) currentQuestion.value = entry.questionText;
}

function onPickQuestion(q: PracticeQuestion) {
  currentQuestion.value = q.text;
  activeTab.value = "voice";
}

onMounted(applyEntry);
onShow(applyEntry);
</script>

<style scoped>
.plh {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #f8fafc 48%, #ffffff 100%);
  padding: 24rpx 24rpx calc(32rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.plh-hero {
  position: relative;
  padding: 32rpx 8rpx 28rpx;
  overflow: hidden;
}
.plh-hero-glow {
  position: absolute;
  top: -60rpx;
  right: -40rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.18) 0%, transparent 70%);
  pointer-events: none;
}
.plh-hero-title {
  display: block;
  font-size: 44rpx;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: 2rpx;
}
.plh-hero-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #64748b;
}

.plh-tabs {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding: 8rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.06);
}
.plh-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 16rpx 8rpx;
  border-radius: 14rpx;
  transition: background 0.25s ease;
  cursor: pointer;
}
.plh-tab--on {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4rpx 16rpx rgba(37, 99, 235, 0.25);
}
.plh-tab-ico {
  font-size: 32rpx;
}
.plh-tab-txt {
  font-size: 22rpx;
  font-weight: 700;
  color: #64748b;
}
.plh-tab--on .plh-tab-txt {
  color: #ffffff;
}

.plh-body {
  min-height: 400rpx;
}

@media (min-width: 768px) {
  .plh {
    max-width: 960px;
    margin: 0 auto;
    padding: 32px 24px 48px;
  }
}
</style>
