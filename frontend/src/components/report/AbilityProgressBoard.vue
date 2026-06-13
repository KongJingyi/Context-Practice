<template>
  <view ref="rootRef" class="apb" :class="{ 'apb--animate': animated }">
    <text class="apb-title">能力提升对比</text>
    <text class="apb-sub">本次训练在您成长路径中的位置</text>

    <view v-if="topImprovement" class="apb-hint">
      <text>{{ topImprovement.label }}比初始水平提升了 {{ topImprovement.delta }}%</text>
    </view>

    <!-- #ifdef H5 -->
    <div ref="hostRef" class="apb-chart" />
    <!-- #endif -->
    <!-- #ifndef H5 -->
    <view class="apb-fallback">
      <view v-for="(label, i) in labels" :key="label" class="apb-row">
        <text class="apb-lbl">{{ label }}</text>
        <view class="apb-bars">
          <view class="apb-bar apb-bar--init" :style="{ width: `${initial[i]}%` }" />
          <view class="apb-bar apb-bar--cur" :style="{ width: `${current[i]}%` }" />
        </view>
      </view>
    </view>
    <!-- #endif -->

    <view class="apb-legend">
      <view class="apb-leg"><view class="apb-leg-dot apb-leg-dot--init" /><text>初始水平</text></view>
      <view class="apb-leg"><view class="apb-leg-dot apb-leg-dot--cur" /><text>当前水平</text></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from "vue";

const props = defineProps<{
  labels: string[];
  initial: number[];
  current: number[];
  improvements: { label: string; delta: number }[];
}>();

const animated = ref(false);
const rootRef = ref<HTMLElement | null>(null);

const topImprovement = computed(() => {
  const sorted = [...props.improvements].sort((a, b) => b.delta - a.delta);
  return sorted[0] || null;
});

// #ifdef H5
import type { ECharts, EChartsOption } from "echarts";
import * as echarts from "echarts";

const hostRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;
let observer: IntersectionObserver | null = null;

function buildOption(scale = 1): EChartsOption {
  const cur = props.current.map((v) => Math.round(v * scale));
  return {
    radar: {
      indicator: props.labels.map((name) => ({ name, max: 100 })),
      radius: "62%",
      splitArea: { areaStyle: { color: ["#f8fafc", "#fff"] } },
      axisName: { color: "#64748b", fontSize: 11 },
    },
    series: [
      {
        type: "radar",
        data: [
          {
            value: props.initial,
            areaStyle: { color: "rgba(148, 163, 184, 0.15)" },
            lineStyle: { color: "#cbd5e1", width: 1 },
            itemStyle: { color: "#94a3b8" },
          },
          {
            value: cur,
            areaStyle: { color: "rgba(37, 99, 235, 0.3)" },
            lineStyle: { color: "#2563eb", width: 2 },
            itemStyle: { color: "#2563eb" },
          },
        ],
        animationDuration: 1200,
        animationEasing: "cubicOut",
      },
    ],
  };
}

onMounted(() => {
  const el = hostRef.value;
  if (!el) return;
  chart = echarts.init(el);
  chart.setOption(buildOption(0.3));

  const root = rootRef.value as unknown as Element | null;
  if (root && typeof IntersectionObserver !== "undefined") {
    observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          animated.value = true;
          chart?.setOption(buildOption(1));
          observer?.disconnect();
        }
      },
      { threshold: 0.3 },
    );
    observer.observe(root);
  } else {
    animated.value = true;
    chart.setOption(buildOption(1));
  }
});

onBeforeUnmount(() => {
  observer?.disconnect();
  chart?.dispose();
  chart = null;
});
// #endif

onMounted(() => {
  // #ifndef H5
  animated.value = true;
  // #endif
});
</script>

<style scoped>
.apb {
  padding: 32rpx 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.06);
}
.apb-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}
.apb-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 20rpx;
}
.apb-hint {
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #eff6ff;
  margin-bottom: 16rpx;
}
.apb-hint text {
  font-size: 26rpx;
  font-weight: 700;
  color: #2563eb;
}
.apb-chart {
  width: 100%;
  height: 300px;
}
.apb-fallback {
  padding: 12rpx 0;
}
.apb-row {
  margin-bottom: 16rpx;
}
.apb-lbl {
  font-size: 22rpx;
  color: #64748b;
  margin-bottom: 8rpx;
  display: block;
}
.apb-bars {
  position: relative;
  height: 24rpx;
  background: #f1f5f9;
  border-radius: 6rpx;
}
.apb-bar {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: 6rpx;
  transition: width 1s ease;
}
.apb-bar--init {
  background: #cbd5e1;
  opacity: 0.6;
}
.apb-bar--cur {
  background: linear-gradient(90deg, #93c5fd, #2563eb);
}
.apb-legend {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 32rpx;
  margin-top: 16rpx;
}
.apb-leg {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}
.apb-leg-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}
.apb-leg-dot--init {
  background: #94a3b8;
}
.apb-leg-dot--cur {
  background: #2563eb;
}
</style>
