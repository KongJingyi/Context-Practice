<template>
  <!-- #ifdef H5 -->
  <div ref="hostRef" class="vrc-host" />
  <!-- #endif -->
  <!-- #ifndef H5 -->
  <view class="vrc-fallback">
    <view v-for="d in dims" :key="d.key" class="vrc-row">
      <text class="vrc-label">{{ d.label }}</text>
      <view class="vrc-bar-bg">
        <view class="vrc-bar-fill" :style="{ width: `${d.value}%` }" />
      </view>
      <text class="vrc-val">{{ d.value }}</text>
    </view>
  </view>
  <!-- #endif -->
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, watch, ref } from "vue";
import type { VoiceRadarScores } from "@/types/practice";

const props = withDefaults(
  defineProps<{
    radar: VoiceRadarScores;
    animate?: boolean;
  }>(),
  { animate: true },
);

const dims = computed(() => [
  { key: "logic", label: "逻辑", value: props.radar.logic },
  { key: "speed", label: "语速", value: props.radar.speed },
  { key: "fluency", label: "流畅", value: props.radar.fluency },
  { key: "emotion", label: "情感", value: props.radar.emotion },
  { key: "vocabulary", label: "词汇", value: props.radar.vocabulary },
]);

// #ifdef H5
import type { ECharts, EChartsOption } from "echarts";
import * as echarts from "echarts";

const hostRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function buildOption(): EChartsOption {
  return {
    radar: {
      indicator: dims.value.map((d) => ({ name: d.label, max: 100 })),
      radius: "68%",
      splitNumber: 4,
      splitArea: {
        areaStyle: {
          color: ["rgba(239,246,255,0.8)", "rgba(255,255,255,0.6)"],
        },
      },
      axisLine: { lineStyle: { color: "rgba(37, 99, 235, 0.2)" } },
      splitLine: { lineStyle: { color: "rgba(37, 99, 235, 0.15)" } },
      axisName: { color: "#64748b", fontSize: 12, fontWeight: 600 },
    },
    series: [
      {
        type: "radar",
        data: [
          {
            value: dims.value.map((d) => d.value),
            areaStyle: { color: "rgba(37, 99, 235, 0.25)" },
            lineStyle: { color: "#2563eb", width: 2 },
            itemStyle: { color: "#2563eb" },
          },
        ],
        animationDuration: props.animate ? 1200 : 0,
        animationEasing: "cubicOut",
      },
    ],
  };
}

onMounted(() => {
  const el = hostRef.value;
  if (!el) return;
  chart = echarts.init(el, undefined, { renderer: "canvas" });
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
.vrc-host {
  width: 100%;
  height: 280px;
}
/* #endif */

.vrc-fallback {
  width: 100%;
  padding: 12rpx 0;
}
.vrc-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;
}
.vrc-label {
  width: 80rpx;
  font-size: 22rpx;
  color: #64748b;
}
.vrc-bar-bg {
  flex: 1;
  height: 12rpx;
  background: #f1f5f9;
  border-radius: 6rpx;
  overflow: hidden;
}
.vrc-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #93c5fd, #2563eb);
}
.vrc-val {
  width: 48rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
  text-align: right;
}
</style>
