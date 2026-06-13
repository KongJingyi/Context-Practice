<template>
  <view class="hp" @tap.stop>
    <view class="hp-bg" />
  <!-- #ifdef H5 -->
    <video
      class="hp-video"
      :src="src"
      autoplay
      loop
      muted
      playsinline
    />
  <!-- #endif -->
  <!-- #ifndef H5 -->
    <view class="hp-fallback">
      <text class="hp-play">▶</text>
      <text class="hp-lbl">15s 高光</text>
    </view>
  <!-- #endif -->
    <view class="hp-badge">
      <text>15s 高光</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{ src: string }>();
</script>

<style scoped>
.hp {
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  aspect-ratio: 16 / 10;
  margin-bottom: 16rpx;
  cursor: pointer;
}
.hp-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  filter: blur(0);
  opacity: 0.35;
  transform: scale(1.1);
}
.hp-video {
  position: relative;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  z-index: 1;
}
.hp-fallback {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  min-height: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #1e40af, #60a5fa);
}
.hp-play {
  font-size: 48rpx;
  color: #fff;
}
.hp-lbl {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}
.hp-badge {
  position: absolute;
  right: 12rpx;
  top: 12rpx;
  z-index: 2;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(6px);
}
.hp-badge text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 700;
}
</style>
