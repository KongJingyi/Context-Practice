<template>
  <!-- #ifdef H5 -->
  <div ref="hostRef" class="crc-host" />
  <!-- #endif -->
  <!-- #ifndef H5 -->
  <view class="crc-fallback">
    <view v-for="d in radar" :key="d.key" class="crc-row">
      <text class="crc-label">{{ d.label }}</text>
      <view class="crc-bar-bg">
        <view class="crc-bar-fill" :style="{ width: `${d.value}%` }" />
      </view>
      <text class="crc-val">{{ d.value }}</text>
    </view>
  </view>
  <!-- #endif -->
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, ref } from "vue";
import type { CoachRadarDimension } from "@/types/coach/hall";

const props = defineProps<{
  radar: CoachRadarDimension[];
}>();

// #ifdef H5
import type { ECharts, EChartsOption } from "echarts";
import * as echarts from "echarts";

const hostRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function buildOption(): EChartsOption {
  return {
    radar: {
      indicator: props.radar.map((d) => ({ name: d.label, max: 100 })),
      radius: "62%",
      splitArea: { areaStyle: { color: ["rgba(239,246,255,0.6)", "rgba(255,255,255,0.4)"] } },
      axisName: { color: "#64748b", fontSize: 11 },
    },
    series: [
      {
        type: "radar",
        data: [
          {
            value: props.radar.map((d) => d.value),
            areaStyle: { color: "rgba(37, 99, 235, 0.25)" },
            lineStyle: { color: "#2563eb", width: 2 },
            itemStyle: { color: "#2563eb" },
          },
        ],
        animationDuration: 800,
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
  () => props.radar,
  () => chart?.setOption(buildOption()),
  { deep: true },
);
// #endif
</script>

<style scoped>
/* #ifdef H5 */
.crc-host {
  width: 100%;
  height: 220px;
}
/* #endif */

.crc-fallback {
  width: 100%;
}
.crc-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;
}
.crc-label {
  width: 120rpx;
  font-size: 22rpx;
  color: #64748b;
}
.crc-bar-bg {
  flex: 1;
  height: 12rpx;
  background: #f1f5f9;
  border-radius: 6rpx;
  overflow: hidden;
}
.crc-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #93c5fd, #2563eb);
}
.crc-val {
  width: 48rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
  text-align: right;
}
</style>
