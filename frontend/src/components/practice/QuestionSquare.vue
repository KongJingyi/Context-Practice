<template>
  <view class="qs">
    <view class="qs-head">
      <view class="qs-head-row">
        <view>
          <text class="qs-title">题库广场</text>
          <text class="qs-sub">6 大场景 · 结构化引导 · 点击即练</text>
        </view>
        <view class="qs-stat">
          <text class="qs-stat-num">{{ totalToday }}</text>
          <text class="qs-stat-lbl">今日练习人次</text>
        </view>
      </view>
    </view>

    <view class="qs-grid">
      <view
        v-for="cat in categories"
        :key="cat.id"
        class="qs-card"
        :class="{ 'qs-card--open': expandedId === cat.id }"
        @tap="onCardTap(cat)"
      >
        <view class="qs-card-main">
          <view class="qs-icon-wrap" :style="iconStyle(cat)">
            <text class="qs-icon">{{ cat.icon }}</text>
          </view>
          <view class="qs-card-text">
            <text class="qs-cat-title">{{ cat.title }}</text>
            <text class="qs-cat-sub">{{ cat.subtitle }}</text>
            <view class="qs-meta">
              <text class="qs-meta-tag">{{ cat.todayCount }}+ 今日练习</text>
              <text class="qs-meta-arrow">{{ expandedId === cat.id ? "收起" : "选题" }}</text>
            </view>
          </view>
        </view>

        <view v-if="expandedId === cat.id" class="qs-questions" @tap.stop>
          <view v-if="loadingId === cat.id" class="qs-loading">
            <text>加载题目中…</text>
          </view>
          <template v-else>
            <view
              v-for="q in displayQuestions"
              :key="q.id"
              class="qs-q-item"
              @tap.stop="emit('pick', q)"
            >
              <text class="qs-q-dot">·</text>
              <text class="qs-q-txt">{{ q.text }}</text>
              <text class="qs-q-go">练这个</text>
            </view>
            <view v-if="!displayQuestions.length" class="qs-loading">
              <text>暂无题目，请稍后再试</text>
            </view>
          </template>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { fetchQuestionCategories, fetchQuestions } from "@/api/modules/practice.js";
import type { QuestionCategory, PracticeQuestion } from "@/types/practice";

const emit = defineEmits<{ (e: "pick", q: PracticeQuestion): void }>();

const categories = ref<QuestionCategory[]>([]);
const expandedId = ref("");
const loadingId = ref("");
const displayQuestions = ref<PracticeQuestion[]>([]);
const questionCache = ref<Record<string, PracticeQuestion[]>>({});

const totalToday = computed(() =>
  categories.value.reduce((sum, c) => sum + c.todayCount, 0),
);

function iconStyle(cat: QuestionCategory) {
  return {
    background: `linear-gradient(145deg, ${cat.gradient[0]}18 0%, ${cat.gradient[1]}28 100%)`,
    borderColor: `${cat.gradient[1]}40`,
  };
}

async function loadCategories() {
  categories.value = await fetchQuestionCategories();
}

async function ensureQuestions(catId: string) {
  if (!questionCache.value[catId]) {
    loadingId.value = catId;
    questionCache.value[catId] = await fetchQuestions(catId);
    loadingId.value = "";
  }
  const pool = questionCache.value[catId] || [];
  displayQuestions.value = [...pool].sort(() => Math.random() - 0.5).slice(0, 3);
}

async function onCardTap(cat: QuestionCategory) {
  if (expandedId.value === cat.id) {
    expandedId.value = "";
    displayQuestions.value = [];
    return;
  }
  expandedId.value = cat.id;
  await ensureQuestions(cat.id);
}

onMounted(loadCategories);
</script>

<style scoped>
.qs {
  padding: 8rpx 0 32rpx;
}

.qs-head {
  margin-bottom: 28rpx;
}
.qs-head-row {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20rpx;
}
.qs-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #0f172a;
}
.qs-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #64748b;
}
.qs-stat {
  flex-shrink: 0;
  padding: 16rpx 24rpx;
  border-radius: 18rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
  text-align: center;
}
.qs-stat-num {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #2563eb;
}
.qs-stat-lbl {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #64748b;
}

.qs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}
@media (min-width: 520px) {
  .qs-grid {
    gap: 28rpx;
  }
}
@media (min-width: 768px) {
  .qs-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}

.qs-card {
  border-radius: 28rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.04);
  overflow: hidden;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}
.qs-card--open {
  border-color: #93c5fd;
  box-shadow: 0 8rpx 28rpx rgba(37, 99, 235, 0.12);
}

.qs-card-main {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 20rpx;
  padding: 32rpx 28rpx;
  min-height: 168rpx;
}

.qs-icon-wrap {
  flex-shrink: 0;
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  border: 1rpx solid;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qs-icon {
  font-size: 48rpx;
}

.qs-card-text {
  flex: 1;
  min-width: 0;
}
.qs-cat-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
}
.qs-cat-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.qs-meta {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
  gap: 10rpx;
}
.qs-meta-tag {
  font-size: 24rpx;
  color: #2563eb;
  font-weight: 600;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: #eff6ff;
}
.qs-meta-arrow {
  font-size: 26rpx;
  color: #94a3b8;
  font-weight: 600;
}
.qs-card--open .qs-meta-arrow {
  color: #2563eb;
}

.qs-questions {
  padding: 0 20rpx 20rpx;
  border-top: 1rpx solid #f1f5f9;
  animation: qsSlide 0.28s ease;
}
.qs-q-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-top: 14rpx;
  padding: 20rpx 20rpx;
  border-radius: 18rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
}
.qs-q-item:active {
  background: #eff6ff;
  border-color: #bfdbfe;
}
.qs-q-dot {
  font-size: 36rpx;
  color: #2563eb;
  line-height: 1;
  flex-shrink: 0;
}
.qs-q-txt {
  flex: 1;
  font-size: 28rpx;
  color: #334155;
  line-height: 1.45;
}
.qs-q-go {
  flex-shrink: 0;
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
}

.qs-loading {
  padding: 20rpx;
  text-align: center;
}
.qs-loading text {
  font-size: 24rpx;
  color: #94a3b8;
}

@keyframes qsSlide {
  from {
    opacity: 0;
    transform: translateY(-8rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
