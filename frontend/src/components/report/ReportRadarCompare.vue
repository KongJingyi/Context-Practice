<template>
  <!-- #ifdef H5 -->
  <div ref="hostRef" class="rrc-host" />
  <!-- #endif -->
  <!-- #ifndef H5 -->
  <view class="rrc-fallback">
    <view v-for="(label, i) in labels" :key="label" class="rrc-row">
      <text class="rrc-label">{{ label }}</text>
      <view class="rrc-bar-bg">
        <view class="rrc-bar-fill" :style="{ width: `${session[i]}%` }" />
      </view>
      <text class="rrc-val">{{ session[i] }}</text>
    </view>
  </view>
  <!-- #endif -->
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, ref } from "vue";

const props = defineProps<{
  labels: string[];
  session: number[];
  average: number[];
}>();

// #ifdef H5
import type { ECharts, EChartsOption } from "echarts";
import * as echarts from "echarts";

const hostRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function buildOption(): EChartsOption {
  return {
    legend: {
      data: ["本场得分", "用户均分"],
      bottom: 0,
      textStyle: { color: "#64748b", fontSize: 11 },
    },
    radar: {
      indicator: props.labels.map((name) => ({ name, max: 100 })),
      radius: "58%",
      splitArea: {
        areaStyle: { color: ["rgba(239,246,255,0.8)", "rgba(255,255,255,0.5)"] },
      },
      axisName: { color: "#64748b", fontSize: 11 },
    },
    series: [
      {
        type: "radar",
        data: [
          {
            name: "用户均分",
            value: props.average,
            areaStyle: { color: "rgba(148, 163, 184, 0.2)" },
            lineStyle: { color: "#94a3b8", width: 1, type: "dashed" },
            itemStyle: { color: "#94a3b8" },
          },
          {
            name: "本场得分",
            value: props.session,
            areaStyle: { color: "rgba(37, 99, 235, 0.28)" },
            lineStyle: { color: "#2563eb", width: 2 },
            itemStyle: { color: "#2563eb" },
          },
        ],
        animationDuration: 1000,
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
  () => [props.session, props.average, props.labels],
  () => chart?.setOption(buildOption()),
  { deep: true },
);
// #endif
</script>

<style scoped>
/* #ifdef H5 */
.rrc-host {
  width: 100%;
  height: 300px;
}
/* #endif */

.rrc-fallback {
  width: 100%;
  padding: 12rpx 0;
}
.rrc-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;
}
.rrc-label {
  width: 100rpx;
  font-size: 22rpx;
  color: #64748b;
}
.rrc-bar-bg {
  flex: 1;
  height: 12rpx;
  background: #f1f5f9;
  border-radius: 6rpx;
  overflow: hidden;
}
.rrc-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #93c5fd, #2563eb);
}
.rrc-val {
  width: 48rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
  text-align: right;
}
</style>
