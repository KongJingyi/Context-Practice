<template>
  <view class="rsr" :class="{ 'rsr--show': show }">
    <view class="rsr-ring" :style="ringStyle">
      <view class="rsr-inner">
        <text class="rsr-num">{{ displayScore }}</text>
        <text class="rsr-unit">分</text>
      </view>
    </view>
    <text class="rsr-label">本场综合评分</text>
    <text v-if="compare" class="rsr-compare">较平台均分 {{ compare }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from "vue";

const props = defineProps<{
  score: number;
  compare?: string;
  animate?: boolean;
}>();

const show = ref(!props.animate);
const displayScore = ref(props.animate ? 0 : props.score);

const ringStyle = computed(() => {
  const pct = Math.min(100, Math.max(0, displayScore.value));
  return {
    background: `conic-gradient(#2563eb ${pct * 3.6}deg, #e2e8f0 ${pct * 3.6}deg)`,
  };
});

onMounted(() => {
  if (!props.animate) return;
  show.value = true;
  const target = props.score;
  const start = Date.now();
  const duration = 1200;
  const tick = () => {
    const t = Math.min(1, (Date.now() - start) / duration);
    const eased = 1 - (1 - t) ** 3;
    displayScore.value = Math.round(target * eased);
    if (t < 1) requestAnimationFrame(tick);
  };
  requestAnimationFrame(tick);
});

watch(
  () => props.score,
  (v) => {
    displayScore.value = v;
  },
);
</script>

<style scoped>
.rsr {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 0;
  opacity: 0;
  transform: scale(0.92);
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.rsr--show {
  opacity: 1;
  transform: scale(1);
}
.rsr-ring {
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(37, 99, 235, 0.2);
  transition: background 0.15s linear;
}
.rsr-inner {
  width: 196rpx;
  height: 196rpx;
  border-radius: 50%;
  background: #fff;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: center;
  gap: 4rpx;
}
.rsr-num {
  font-size: 72rpx;
  font-weight: 900;
  color: #2563eb;
  line-height: 1;
}
.rsr-unit {
  font-size: 28rpx;
  font-weight: 700;
  color: #64748b;
}
.rsr-label {
  margin-top: 20rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}
.rsr-compare {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #059669;
  font-weight: 600;
}

@media (min-width: 768px) {
  .rsr-ring {
    width: 180px;
    height: 180px;
  }
  .rsr-inner {
    width: 148px;
    height: 148px;
  }
  .rsr-num {
    font-size: 52px;
  }
}
</style>
