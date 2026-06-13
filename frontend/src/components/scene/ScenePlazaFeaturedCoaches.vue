<template>
  <view v-if="coaches.length" class="featured">
    <view class="featured__head">
      <text class="featured__title">平台认证陪练</text>
      <text class="featured__count">共 {{ coaches.length }} 位可预约</text>
    </view>
    <scroll-view class="featured__scroll" scroll-x :show-scrollbar="false">
      <view class="featured__row">
        <view
          v-for="coach in coaches"
          :key="coach.id"
          class="featured__card"
          @tap="emit('book', coach)"
        >
          <view class="featured__avatar">
            <image
              v-if="coach.avatarUrl"
              class="featured__avatar-img"
              :src="coach.avatarUrl"
              mode="aspectFill"
            />
            <text v-else class="featured__avatar-letter">{{ coach.name.slice(0, 1) }}</text>
          </view>
          <text class="featured__name">{{ coach.name }}</text>
          <text class="featured__title">{{ coach.jobTitle }}</text>
          <view class="featured__meta">
            <text class="featured__rating">★ {{ coach.rating.toFixed(1) }}</text>
            <text class="featured__price">¥{{ coach.price }}/{{ coach.sessionMinutes }}分</text>
          </view>
          <view v-if="coach.skillTags?.length" class="featured__tags">
            <text
              v-for="(tag, i) in coach.skillTags.slice(0, 2)"
              :key="`${coach.id}-tag-${i}`"
              class="featured__tag"
            >
              {{ tag }}
            </text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import type { Coach } from "@/types/coach/hall";

defineProps<{
  coaches: Coach[];
}>();

const emit = defineEmits<{
  (e: "book", coach: Coach): void;
}>();
</script>

<style scoped>
.featured {
  margin-bottom: 40rpx;
  padding: 28rpx 0 8rpx;
}
.featured__head {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding: 0 4rpx;
}
.featured__title {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}
.featured__count {
  font-size: 22rpx;
  color: #64748b;
}
.featured__scroll {
  width: 100%;
  white-space: nowrap;
}
.featured__row {
  display: inline-flex;
  flex-direction: row;
  gap: 20rpx;
  padding: 4rpx 4rpx 12rpx;
}
.featured__card {
  flex-shrink: 0;
  width: 280rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
  box-sizing: border-box;
}
.featured__avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 16rpx;
}
.featured__avatar-img {
  width: 100%;
  height: 100%;
}
.featured__avatar-letter {
  font-size: 32rpx;
  font-weight: 800;
  color: #fff;
}
.featured__name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
.featured__title {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.featured__meta {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-top: 14rpx;
}
.featured__rating {
  font-size: 22rpx;
  font-weight: 600;
  color: #d97706;
}
.featured__price {
  font-size: 22rpx;
  font-weight: 600;
  color: #2563eb;
}
.featured__tags {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}
.featured__tag {
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #1d4ed8;
  background: #eff6ff;
}
</style>
