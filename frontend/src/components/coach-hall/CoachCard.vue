<template>
  <view
    class="coach-card"
    :class="[
      `coach-card--${coach.theme}`,
      {
        'coach-card--hover': isHovered,
        'coach-card--gold': highlighted,
        'coach-card--compare': compareSelected,
      },
    ]"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <view class="coach-card__compare" @tap.stop="emit('toggle-compare', coach)">
      <view class="coach-card__check" :class="{ 'coach-card__check--on': compareSelected }">
        <text v-if="compareSelected">✓</text>
      </view>
      <text class="coach-card__compare-txt">对比</text>
    </view>

    <view class="coach-card__top">
      <view class="coach-card__avatar-wrap">
        <image
          v-if="coach.avatarUrl"
          class="coach-card__avatar"
          :src="coach.avatarUrl"
          mode="aspectFill"
        />
        <view v-else class="coach-card__avatar coach-card__avatar--placeholder">
          <text class="coach-card__avatar-letter">{{ avatarLetter }}</text>
        </view>
        <view v-if="coach.online" class="coach-card__online" />
        <view v-if="coach.availableToday" class="coach-card__today">
          <view class="coach-card__today-dot" />
          <text class="coach-card__today-txt">今日可约</text>
        </view>
      </view>

      <view class="coach-card__identity">
        <view class="coach-card__name-row">
          <text class="coach-card__name">{{ coach.name }}</text>
          <text class="coach-card__lv">Lv.{{ coach.levelNum }}</text>
        </view>
        <text class="coach-card__title">{{ coach.jobTitle }}</text>
      </view>

      <view class="coach-card__stats">
        <view class="coach-card__stars">
          <text class="coach-card__star">★</text>
          <text class="coach-card__rating">{{ coach.rating.toFixed(1) }}</text>
        </view>
        <text class="coach-card__orders">{{ coach.orderCount }} 单</text>
      </view>
    </view>

    <view class="coach-card__tags">
      <text
        v-for="(tag, i) in coach.skillTags.slice(0, 3)"
        :key="`${coach.id}-tag-${i}`"
        class="coach-card__tag"
      >
        {{ tag }}
      </text>
    </view>

    <view class="coach-card__foot">
      <text class="coach-card__price">
        ¥{{ coach.price }}
        <text class="coach-card__price-unit">/{{ coach.sessionMinutes }}分钟</text>
      </text>
      <view class="coach-card__actions">
        <view class="coach-card__btn coach-card__btn--ghost" @tap.stop="emit('detail', coach)">
          <text class="coach-card__btn-text coach-card__btn-text--ghost">查看详情</text>
        </view>
        <view class="coach-card__btn coach-card__btn--primary" @tap.stop="emit('book', coach)">
          <text class="coach-card__btn-text">立即预约</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import type { Coach } from "@/types/coach/hall";

const props = defineProps<{
  coach: Coach;
  compareSelected?: boolean;
  highlighted?: boolean;
}>();

const emit = defineEmits<{
  (e: "detail", coach: Coach): void;
  (e: "book", coach: Coach): void;
  (e: "toggle-compare", coach: Coach): void;
}>();

const isHovered = ref(false);
const avatarLetter = computed(() => props.coach.name.slice(0, 1));
</script>

<style scoped>
.coach-card {
  position: relative;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.05);
  transition:
    transform 0.45s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.32s ease,
    opacity 0.35s ease;
  box-sizing: border-box;
}
.coach-card--hover {
  transform: translateY(-6px);
  box-shadow: 0 16rpx 36rpx rgba(15, 23, 42, 0.1);
}
.coach-card--gold {
  border-color: #fcd34d;
  box-shadow: 0 8rpx 28rpx rgba(234, 179, 8, 0.22);
}
.coach-card--compare {
  border-color: #93c5fd;
  box-shadow: 0 0 0 3rpx rgba(37, 99, 235, 0.15);
}

.coach-card--blue {
  --tag-bg: rgba(37, 99, 235, 0.09);
  --tag-text: #1d4ed8;
  --tag-border: rgba(37, 99, 235, 0.15);
}
.coach-card--indigo {
  --tag-bg: rgba(79, 70, 229, 0.09);
  --tag-text: #4338ca;
  --tag-border: rgba(79, 70, 229, 0.15);
}
.coach-card--orange {
  --tag-bg: rgba(234, 88, 12, 0.1);
  --tag-text: #c2410c;
  --tag-border: rgba(234, 88, 12, 0.15);
}
.coach-card--rose {
  --tag-bg: rgba(225, 29, 72, 0.09);
  --tag-text: #be123c;
  --tag-border: rgba(225, 29, 72, 0.15);
}
.coach-card--emerald {
  --tag-bg: rgba(5, 150, 105, 0.09);
  --tag-text: #047857;
  --tag-border: rgba(5, 150, 105, 0.15);
}
.coach-card--amber {
  --tag-bg: rgba(217, 119, 6, 0.1);
  --tag-text: #b45309;
  --tag-border: rgba(217, 119, 6, 0.15);
}

.coach-card__compare {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  z-index: 2;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
}
.coach-card__check {
  width: 32rpx;
  height: 32rpx;
  border-radius: 8rpx;
  border: 2rpx solid #cbd5e1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #fff;
}
.coach-card__check--on {
  background: #2563eb;
  border-color: #2563eb;
}
.coach-card__compare-txt {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 600;
}

.coach-card__top {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 18rpx;
  padding-right: 80rpx;
}
.coach-card__avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.coach-card__avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #f1f5f9;
}
.coach-card__avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #dbeafe, #eff6ff);
}
.coach-card__avatar-letter {
  font-size: 36rpx;
  font-weight: 700;
  color: #2563eb;
}
.coach-card__online {
  position: absolute;
  right: 2rpx;
  bottom: 2rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #22c55e;
  border: 3rpx solid #ffffff;
  animation: breathe 2s ease-in-out infinite;
}
.coach-card__today {
  position: absolute;
  left: 50%;
  bottom: -8rpx;
  transform: translateX(-50%);
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
  padding: 2rpx 10rpx;
  border-radius: 999rpx;
  background: #ecfdf5;
  border: 1rpx solid #6ee7b7;
  white-space: nowrap;
}
.coach-card__today-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #22c55e;
  flex-shrink: 0;
}
.coach-card__today-txt {
  font-size: 18rpx;
  color: #047857;
  font-weight: 700;
}
@keyframes breathe {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.55);
  }
  50% {
    box-shadow: 0 0 0 8rpx rgba(34, 197, 94, 0);
  }
}

.coach-card__identity {
  flex: 1;
  min-width: 0;
}
.coach-card__name-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}
.coach-card__name {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}
.coach-card__lv {
  font-size: 20rpx;
  font-weight: 800;
  color: #2563eb;
  background: #eff6ff;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
}
.coach-card__title {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #64748b;
  line-height: 1.4;
}
.coach-card__stats {
  text-align: right;
  flex-shrink: 0;
}
.coach-card__stars {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  gap: 4rpx;
}
.coach-card__star {
  font-size: 24rpx;
  color: #f59e0b;
}
.coach-card__rating {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
.coach-card__orders {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #94a3b8;
}

.coach-card__tags {
  display: flex;
  flex-direction: row;
  gap: 10rpx;
  margin-bottom: 20rpx;
}
.coach-card__tag {
  flex: 1;
  text-align: center;
  padding: 8rpx 6rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 500;
  color: var(--tag-text);
  background: var(--tag-bg);
  border: 1rpx solid var(--tag-border);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coach-card__foot {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f1f5f9;
  flex-wrap: wrap;
}
.coach-card__price {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}
.coach-card__price-unit {
  font-size: 22rpx;
  font-weight: 500;
  color: #64748b;
}
.coach-card__actions {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
}
.coach-card__btn {
  padding: 12rpx 20rpx;
  border-radius: 12rpx;
}
.coach-card__btn--ghost {
  border: 1rpx solid #cbd5e1;
  background: #ffffff;
}
.coach-card__btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.25);
}
.coach-card__btn-text {
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
}
.coach-card__btn-text--ghost {
  color: #334155;
}
</style>
