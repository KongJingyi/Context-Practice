<template>
  <view class="sk-card">
    <view class="sk-card__top">
      <view class="sk sk-avatar" />
      <view class="sk-card__meta">
        <view class="sk sk-line sk-line--lg" />
        <view class="sk sk-line sk-line--sm" />
      </view>
      <view class="sk-card__rating">
        <view class="sk sk-line sk-line--md" />
        <view class="sk sk-line sk-line--xs" />
      </view>
    </view>
    <view class="sk-card__tags">
      <view v-for="n in 3" :key="n" class="sk sk-tag" />
    </view>
    <view class="sk-card__foot">
      <view class="sk sk-line sk-line--price" />
      <view class="sk-card__btns">
        <view class="sk sk-btn" />
        <view class="sk sk-btn sk-btn--primary" />
      </view>
    </view>
  </view>
</template>

<style scoped>
.sk-card {
  padding: 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
}
.sk-card__top {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.sk-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.sk-card__meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  justify-content: center;
}
.sk-card__rating {
  width: 120rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: flex-end;
}
.sk-line {
  height: 24rpx;
  border-radius: 8rpx;
  width: 100%;
}
.sk-line--lg {
  width: 70%;
  height: 28rpx;
}
.sk-line--sm {
  width: 90%;
}
.sk-line--md {
  width: 100%;
}
.sk-line--xs {
  width: 60%;
}
.sk-line--price {
  width: 140rpx;
  height: 32rpx;
}
.sk-card__tags {
  display: flex;
  flex-direction: row;
  gap: 10rpx;
  margin-bottom: 22rpx;
}
.sk-tag {
  flex: 1;
  height: 40rpx;
  border-radius: 8rpx;
}
.sk-card__foot {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #f1f5f9;
}
.sk-card__btns {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
}
.sk-btn {
  width: 120rpx;
  height: 56rpx;
  border-radius: 12rpx;
}
.sk-btn--primary {
  width: 140rpx;
}
.sk {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}
@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
