<template>
  <view class="fav-sk">
    <view v-for="i in count" :key="i" class="fav-sk-card">
      <view class="fav-sk-avatar fav-sk-pulse" />
      <view class="fav-sk-line fav-sk-pulse fav-sk-line--w80" />
      <view class="fav-sk-line fav-sk-pulse fav-sk-line--w50" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 8 });
</script>

<style scoped>
.fav-sk {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}
@media (min-width: 640px) {
  .fav-sk {
    grid-template-columns: repeat(4, 1fr);
  }
}
.fav-sk-card {
  padding: 24rpx;
  border-radius: 16rpx;
  background: #fff;
  border: 1rpx solid #f1f5f9;
}
.fav-sk-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #e2e8f0;
  margin-bottom: 16rpx;
}
.fav-sk-line {
  height: 20rpx;
  border-radius: 6rpx;
  background: #e2e8f0;
  margin-bottom: 10rpx;
}
.fav-sk-line--w80 {
  width: 80%;
}
.fav-sk-line--w50 {
  width: 50%;
}
.fav-sk-pulse {
  animation: favSkPulse 1.4s ease-in-out infinite;
}
@keyframes favSkPulse {
  0%,
  100% {
    opacity: 0.55;
  }
  50% {
    opacity: 1;
  }
}
</style>
