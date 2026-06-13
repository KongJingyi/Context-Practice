<template>
  <view class="caf">
    <view class="caf-bar">
      <view class="caf-scene">
        <text class="caf-label">场景</text>
        <picker
          mode="selector"
          :range="sceneLabels"
          :value="scenePickerIndex"
          @change="onSceneChange"
        >
          <view class="caf-scene-pill">
            <text class="caf-scene-txt">{{ filters.subSceneName || "选择场景" }}</text>
            <text class="caf-chev">▾</text>
          </view>
        </picker>
      </view>

      <view class="caf-bar-actions">
        <view class="caf-filter-btn" @tap="panelOpen = !panelOpen">
          <text class="caf-filter-ico">⎚</text>
          <text>筛选</text>
          <view v-if="activeFilterCount" class="caf-badge">
            <text>{{ activeFilterCount }}</text>
          </view>
        </view>
        <view class="caf-ai-btn" @tap="emit('ai-match')">
          <text class="caf-ai-spark">✦</text>
          <text>AI 智能匹配</text>
        </view>
      </view>
    </view>

    <view v-if="panelOpen" class="caf-panel">
      <view class="caf-section">
        <view class="caf-section-head">
          <text class="caf-section-title">等级 Lv.{{ filters.levelMin }} – Lv.{{ filters.levelMax }}</text>
        </view>
        <view class="caf-level-row">
          <text class="caf-level-lbl">最低</text>
          <slider
            class="caf-slider"
            :value="filters.levelMin"
            :min="1"
            :max="5"
            :step="1"
            activeColor="#2563eb"
            block-size="18"
            @change="onLevelMinChange"
          />
        </view>
        <view class="caf-level-row">
          <text class="caf-level-lbl">最高</text>
          <slider
            class="caf-slider"
            :value="filters.levelMax"
            :min="1"
            :max="5"
            :step="1"
            activeColor="#2563eb"
            block-size="18"
            @change="onLevelMaxChange"
          />
        </view>
        <view class="caf-level-dots">
          <text
            v-for="n in 5"
            :key="n"
            class="caf-lv-dot"
            :class="{ 'caf-lv-dot--on': n >= filters.levelMin && n <= filters.levelMax }"
          >
            Lv.{{ n }}
          </text>
        </view>
      </view>

      <view class="caf-section">
        <text class="caf-section-title">最低星级</text>
        <view class="caf-stars-row">
          <view
            v-for="opt in starOptions"
            :key="String(opt.value)"
            class="caf-star-chip"
            :class="{ 'caf-star-chip--on': filters.minStars === opt.value }"
            @tap="setMinStars(opt.value)"
          >
            <text>{{ opt.label }}</text>
          </view>
        </view>
      </view>

      <view class="caf-section">
        <text class="caf-section-title">擅长标签</text>
        <view class="caf-tags">
          <view
            v-for="t in specialtyOptions"
            :key="t.id"
            class="caf-tag"
            :class="{
              'caf-tag--on': filters.tags.includes(t.id),
              'caf-tag--pulse': pulseTagId === t.id,
            }"
            @tap="toggleTag(t.id)"
          >
            <text>{{ t.label }}</text>
          </view>
        </view>
      </view>

      <view class="caf-section caf-section--row">
        <view>
          <text class="caf-section-title">仅看今日可约</text>
          <text class="caf-section-sub">卡片显示绿色可约标识</text>
        </view>
        <switch
          :checked="filters.todayOnly"
          color="#2563eb"
          @change="onTodaySwitch"
        />
      </view>

      <view class="caf-section">
        <text class="caf-section-title">价格区间</text>
        <view class="caf-stars-row">
          <view
            v-for="(label, i) in priceLabels"
            :key="priceValues[i]"
            class="caf-star-chip"
            :class="{ 'caf-star-chip--on': filters.priceRange === priceValues[i] }"
            @tap="setPrice(priceValues[i])"
          >
            <text>{{ label }}</text>
          </view>
        </view>
      </view>

      <view class="caf-panel-foot">
        <view class="caf-reset" @tap="onReset">
          <text>清除</text>
        </view>
        <view class="caf-apply" @tap="onApply">
          <text>应用筛选</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import {
  COACH_SPECIALTY_OPTIONS,
  createDefaultFilterParams,
  type CoachFilterParams,
  type CoachHallSceneOption,
  type CoachPriceRange,
  type CoachSpecialtyTagId,
} from "@/types/coach/hall";

const props = defineProps<{
  filters: CoachFilterParams;
  sceneOptions: CoachHallSceneOption[];
}>();

const emit = defineEmits<{
  (e: "update:filters", value: CoachFilterParams): void;
  (e: "apply"): void;
  (e: "ai-match"): void;
}>();

const panelOpen = ref(false);
const pulseTagId = ref("");

const specialtyOptions = COACH_SPECIALTY_OPTIONS;
const priceLabels = ["全部", "¥0-100", "¥100-300", "¥300+"];
const priceValues: CoachPriceRange[] = ["all", "0-100", "100-300", "300+"];

const starOptions = [
  { value: 0, label: "不限" },
  { value: 4.5, label: "4.5★+" },
  { value: 4.0, label: "4.0★+" },
  { value: 3.5, label: "3.5★+" },
];

const sceneLabels = computed(() => props.sceneOptions.map((o) => o.label));

const scenePickerIndex = computed(() => {
  const i = props.sceneOptions.findIndex((o) => o.id === props.filters.sceneId);
  return i >= 0 ? i : 0;
});

const activeFilterCount = computed(() => {
  let n = 0;
  if (props.filters.levelMin > 1 || props.filters.levelMax < 5) n += 1;
  if (props.filters.minStars > 0) n += 1;
  if (props.filters.tags.length) n += 1;
  if (props.filters.todayOnly) n += 1;
  if (props.filters.priceRange !== "all") n += 1;
  return n;
});

function patch(partial: Partial<CoachFilterParams>) {
  emit("update:filters", { ...props.filters, ...partial });
}

function onSceneChange(e: { detail: { value: string } }) {
  const idx = Number(e.detail.value);
  const opt = props.sceneOptions[idx];
  if (!opt) return;
  patch({ sceneId: opt.id, subSceneName: opt.label });
}

function onLevelMinChange(e: { detail: { value: number } }) {
  let min = e.detail.value;
  let max = props.filters.levelMax;
  if (min > max) max = min;
  patch({ levelMin: min, levelMax: max });
  emit("apply");
}

function onLevelMaxChange(e: { detail: { value: number } }) {
  let max = e.detail.value;
  let min = props.filters.levelMin;
  if (max < min) min = max;
  patch({ levelMin: min, levelMax: max });
  emit("apply");
}

function setMinStars(v: number) {
  patch({ minStars: v });
  emit("apply");
}

function toggleTag(id: CoachSpecialtyTagId) {
  const tags = [...props.filters.tags];
  const i = tags.indexOf(id);
  if (i >= 0) tags.splice(i, 1);
  else tags.push(id);
  pulseTagId.value = id;
  setTimeout(() => {
    pulseTagId.value = "";
  }, 600);
  patch({ tags });
  emit("apply");
}

function onTodaySwitch(e: unknown) {
  const checked = Boolean((e as { detail?: { value?: boolean } })?.detail?.value);
  patch({ todayOnly: checked });
  emit("apply");
}

function setPrice(range: CoachPriceRange) {
  patch({ priceRange: range });
  emit("apply");
}

function onReset() {
  const next = createDefaultFilterParams(props.filters.sceneId, props.filters.subSceneName);
  next.sortBy = props.filters.sortBy;
  emit("update:filters", next);
  emit("apply");
}

function onApply() {
  panelOpen.value = false;
  emit("apply");
}
</script>

<style scoped>
.caf {
  margin-bottom: 24rpx;
}
.caf-bar {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16rpx;
  align-items: flex-end;
  padding: 20rpx 22rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.95);
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.05);
}
.caf-scene {
  flex: 1;
  min-width: 200rpx;
}
.caf-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  font-weight: 600;
  margin-bottom: 8rpx;
}
.caf-scene-pill {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 18rpx;
  border-radius: 12rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
}
.caf-scene-txt {
  font-size: 26rpx;
  font-weight: 600;
  color: #0f172a;
}
.caf-chev {
  color: #94a3b8;
  font-size: 22rpx;
}
.caf-bar-actions {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  flex-shrink: 0;
}
.caf-filter-btn,
.caf-ai-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
  position: relative;
}
.caf-filter-btn {
  background: #f1f5f9;
  color: #334155;
  border: 1rpx solid #e2e8f0;
}
.caf-ai-btn {
  background: linear-gradient(135deg, #6366f1, #2563eb);
  color: #fff;
  box-shadow: 0 6rpx 20rpx rgba(99, 102, 241, 0.35);
}
.caf-filter-ico {
  font-size: 26rpx;
}
.caf-ai-spark {
  font-size: 26rpx;
  animation: cafSpark 1.5s ease-in-out infinite;
}
@keyframes cafSpark {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.15);
  }
}
.caf-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #ef4444;
  display: flex;
  align-items: center;
  justify-content: center;
}
.caf-badge text {
  font-size: 18rpx;
  color: #fff;
  font-weight: 800;
}

.caf-panel {
  margin-top: 16rpx;
  padding: 24rpx 22rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.98);
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 12rpx 32rpx rgba(15, 23, 42, 0.08);
  animation: cafSlide 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes cafSlide {
  from {
    opacity: 0;
    transform: translateY(-12rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.caf-section {
  margin-bottom: 28rpx;
}
.caf-section--row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.caf-section-title {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 12rpx;
}
.caf-section-sub {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 4rpx;
}
.caf-level-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}
.caf-level-lbl {
  font-size: 22rpx;
  color: #64748b;
  width: 56rpx;
  flex-shrink: 0;
}
.caf-slider {
  flex: 1;
}
.caf-level-dots {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-top: 8rpx;
}
.caf-lv-dot {
  font-size: 20rpx;
  color: #cbd5e1;
  font-weight: 600;
}
.caf-lv-dot--on {
  color: #2563eb;
}

.caf-stars-row,
.caf-tags {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
}
.caf-star-chip,
.caf-tag {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: #f1f5f9;
  border: 1rpx solid #e2e8f0;
  font-size: 24rpx;
  color: #475569;
  font-weight: 600;
  transition: all 0.25s ease;
}
.caf-star-chip--on,
.caf-tag--on {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
  box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.4);
}
.caf-tag--on.caf-tag--pulse {
  animation: cafTagPulse 0.6s ease;
}
@keyframes cafTagPulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.45);
  }
  50% {
    box-shadow: 0 0 0 12rpx rgba(37, 99, 235, 0);
  }
}

.caf-panel-foot {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  padding-top: 8rpx;
}
.caf-reset,
.caf-apply {
  flex: 1;
  padding: 18rpx;
  border-radius: 14rpx;
  text-align: center;
  font-size: 26rpx;
  font-weight: 700;
}
.caf-reset {
  border: 1rpx solid #e2e8f0;
  color: #64748b;
}
.caf-apply {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
}
</style>
