<template>
  <view class="gsg">
    <view class="gsg-ring-wrap">
      <view class="gsg-ring" :style="ringStyle">
        <view class="gsg-inner">
          <view class="gsg-score-row">
            <text class="gsg-num">{{ score }}</text>
            <text class="gsg-unit">分</text>
          </view>
        </view>
      </view>
    </view>
    <text class="gsg-label">当前综合评分</text>
    <text class="gsg-hint">{{ hint }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    score: number;
    hint?: string;
  }>(),
  {
    hint: "基于近 30 次陪练反馈",
  },
);

const ringStyle = computed(() => {
  const pct = Math.min(100, Math.max(0, props.score));
  return {
    background: `conic-gradient(#2563eb ${pct * 3.6}deg, #e8eef5 ${pct * 3.6}deg)`,
  };
});
</script>

<style scoped>
.gsg {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 24rpx 20rpx;
  width: 100%;
  box-sizing: border-box;
}
.gsg-ring-wrap {
  flex-shrink: 0;
}
.gsg-ring {
  width: 168rpx;
  height: 168rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(37, 99, 235, 0.12);
  transition: background 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.gsg-inner {
  width: 136rpx;
  height: 136rpx;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gsg-score-row {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: center;
  gap: 4rpx;
}
.gsg-num {
  font-size: 52rpx;
  font-weight: 800;
  color: #2563eb;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.gsg-unit {
  font-size: 24rpx;
  font-weight: 700;
  color: #64748b;
  line-height: 1;
  padding-bottom: 8rpx;
}
.gsg-label {
  margin-top: 18rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.3;
}
.gsg-hint {
  margin-top: 10rpx;
  max-width: 280rpx;
  padding: 0 8rpx;
  font-size: 22rpx;
  color: #64748b;
  text-align: center;
  line-height: 1.5;
  word-break: break-all;
}

/* #ifdef H5 */
@media (min-width: 1024px) {
  .gsg {
    width: auto;
    min-width: 200px;
    padding: 12px 20px 16px;
  }
  .gsg-ring {
    width: 148px;
    height: 148px;
  }
  .gsg-inner {
    width: 120px;
    height: 120px;
  }
  .gsg-num {
    font-size: 44px;
  }
  .gsg-unit {
    font-size: 14px;
    padding-bottom: 6px;
  }
  .gsg-hint {
    max-width: 180px;
    font-size: 12px;
  }
}
/* #endif */
</style>
