<template>
  <view class="efl">
    <text class="efl-title">专家深度点评</text>
    <view
      v-for="(item, i) in items"
      :key="item.id"
      class="efl-item"
      :class="[`efl-item--${item.type}`, { 'efl-item--show': visible }]"
      :style="{ transitionDelay: `${i * 0.12}s` }"
    >
      <view class="efl-head">
        <text class="efl-type">{{ typeLabel(item.type) }}</text>
        <view class="efl-ts" @tap="emit('jump', item.seconds)">
          <text class="efl-ts-ico">▶</text>
          <text class="efl-ts-txt">{{ item.timestamp }}</text>
        </view>
      </view>
      <text class="efl-content">「{{ item.content }}」</text>
      <view class="efl-suggest">
        <text class="efl-suggest-lbl">建议</text>
        <text class="efl-suggest-txt">{{ item.suggestion }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { ExpertFeedbackItem, FeedbackType } from "@/types/report";

defineProps<{
  items: ExpertFeedbackItem[];
  visible?: boolean;
}>();

const emit = defineEmits<{ (e: "jump", seconds: number): void }>();

function typeLabel(type: FeedbackType) {
  const map: Record<FeedbackType, string> = {
    warning: "待改进",
    highlight: "表达亮点",
    question: "专家追问",
    turn: "逻辑转折",
  };
  return map[type] || "点评";
}
</script>

<style scoped>
.efl-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 20rpx;
}
.efl-item {
  margin-bottom: 20rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  border-left: 6rpx solid #2563eb;
  background: #f8fafc;
  opacity: 0;
  transform: translateY(16rpx);
  transition: all 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}
.efl-item--show {
  opacity: 1;
  transform: translateY(0);
}
.efl-item--warning {
  border-left-color: #f59e0b;
  background: #fffbeb;
}
.efl-item--highlight {
  border-left-color: #059669;
  background: #ecfdf5;
}
.efl-item--question {
  border-left-color: #6366f1;
  background: #eef2ff;
}
.efl-item--turn {
  border-left-color: #2563eb;
}
.efl-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.efl-type {
  font-size: 22rpx;
  font-weight: 700;
  color: #64748b;
}
.efl-ts {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.1);
  cursor: pointer;
}
.efl-ts-ico {
  font-size: 18rpx;
  color: #2563eb;
}
.efl-ts-txt {
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
}
.efl-content {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.55;
  font-style: italic;
}
.efl-suggest {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx dashed #e2e8f0;
}
.efl-suggest-lbl {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 6rpx;
}
.efl-suggest-txt {
  font-size: 24rpx;
  color: #475569;
  line-height: 1.55;
}
</style>
