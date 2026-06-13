<template>
  <!-- #ifdef H5 -->
  <div ref="hostRef" class="crb-host" />
  <!-- #endif -->
  <!-- #ifndef H5 -->
  <view class="crb-fallback">
    <view v-for="item in distribution" :key="item.stars" class="crb-row">
      <text class="crb-star">{{ item.stars }}★</text>
      <view class="crb-bar-bg">
        <view class="crb-bar-fill" :style="{ width: barWidth(item) }" />
      </view>
      <text class="crb-pct">{{ barWidth(item) }}</text>
    </view>
  </view>
  <!-- #endif -->
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, watch, ref } from "vue";
import type { RatingDistributionItem } from "@/types/coach/hall";

const props = defineProps<{
  distribution: RatingDistributionItem[];
}>();

const total = computed(() =>
  props.distribution.reduce((s, d) => s + d.count, 0) || 1,
);

function barWidth(item: RatingDistributionItem) {
  return `${Math.round((item.count / total.value) * 100)}%`;
}

// #ifdef H5
import type { ECharts, EChartsOption } from "echarts";
import * as echarts from "echarts";

const hostRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function buildOption(): EChartsOption {
  const sorted = [...props.distribution].sort((a, b) => b.stars - a.stars);
  return {
    grid: { left: 36, right: 8, top: 8, bottom: 8 },
    xAxis: { type: "value", show: false, max: total.value },
    yAxis: {
      type: "category",
      data: sorted.map((d) => `${d.stars}星`),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: "#64748b", fontSize: 11 },
    },
    series: [
      {
        type: "bar",
        data: sorted.map((d) => d.count),
        barWidth: 10,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: "#93c5fd" },
            { offset: 1, color: "#2563eb" },
          ]),
        },
        animationDuration: 600,
      },
    ],
  };
}

onMounted(() => {
  const el = hostRef.value;
  if (!el) return;
  chart = echarts.init(el);
  chart.setOption(buildOption());
});

onBeforeUnmount(() => {
  chart?.dispose();
  chart = null;
});

watch(
  () => props.distribution,
  () => chart?.setOption(buildOption(), { notMerge: false }),
  { deep: true },
);
// #endif
</script>

<style scoped>
/* #ifdef H5 */
.crb-host {
  width: 100%;
  height: 140px;
  min-height: 120px;
}
/* #endif */

.crb-fallback {
  width: 100%;
}
.crb-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 10rpx;
}
.crb-star {
  width: 48rpx;
  font-size: 20rpx;
  color: #64748b;
}
.crb-bar-bg {
  flex: 1;
  height: 12rpx;
  background: #f1f5f9;
  border-radius: 6rpx;
  overflow: hidden;
}
.crb-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #93c5fd, #2563eb);
  border-radius: 6rpx;
  transition: width 0.4s ease;
}
.crb-pct {
  width: 56rpx;
  font-size: 20rpx;
  color: #94a3b8;
  text-align: right;
}
</style>
