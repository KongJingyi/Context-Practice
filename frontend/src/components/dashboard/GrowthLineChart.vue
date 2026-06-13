<template>
  <view class="glc-wrap">
    <view class="glc-toolbar">
      <view class="glc-periods">
        <view
          v-for="p in periods"
          :key="p.id"
          class="glc-period"
          :class="{ 'glc-period--on': modelValue === p.id }"
          @tap="onPeriod(p.id)"
        >
          <text>{{ p.label }}</text>
        </view>
      </view>
    </view>
    <!-- #ifdef H5 -->
    <div ref="hostRef" class="glc-host" />
    <!-- #endif -->
    <!-- #ifndef H5 -->
    <view class="glc-fallback">
      <text class="glc-fallback-txt">成长曲线请在 H5 查看（小程序可接入 lime-echart）</text>
    </view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import type { GrowthPeriod } from "@/types/dashboard";

const props = defineProps<{
  xLabels: string[];
  values: number[];
  modelValue?: GrowthPeriod;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", v: GrowthPeriod): void;
}>();

const periods: { id: GrowthPeriod; label: string }[] = [
  { id: "week", label: "周" },
  { id: "month", label: "月" },
  { id: "year", label: "年" },
];

function onPeriod(id: GrowthPeriod) {
  emit("update:modelValue", id);
}

// #ifdef H5
import type { ECharts, EChartsOption } from "echarts";
import * as echarts from "echarts";

const hostRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function buildOption(): EChartsOption {
  return {
    grid: { left: 44, right: 16, top: 28, bottom: 28 },
    xAxis: {
      type: "category",
      data: props.xLabels,
      boundaryGap: false,
      axisLine: { lineStyle: { color: "#e2e8f0" } },
      axisTick: { show: false },
      axisLabel: { color: "#64748b", fontSize: 11 },
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: "#94a3b8", fontSize: 11 },
    },
    tooltip: { trigger: "axis" },
    series: [
      {
        type: "line",
        smooth: true,
        showSymbol: true,
        symbol: "circle",
        symbolSize: 10,
        lineStyle: { width: 3, color: "#3b82f6" },
        itemStyle: {
          color: "#3b82f6",
          borderColor: "#ffffff",
          borderWidth: 2,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(59, 130, 246, 0.38)" },
            { offset: 1, color: "rgba(59, 130, 246, 0.02)" },
          ]),
        },
        data: props.values,
        animation: true,
        animationDuration: 800,
        animationDurationUpdate: 600,
        animationEasing: "cubicOut",
        animationEasingUpdate: "cubicInOut",
      },
    ],
  };
}

function resize() {
  chart?.resize();
}

onMounted(() => {
  const el = hostRef.value;
  if (!el) return;
  chart = echarts.init(el);
  chart.setOption(buildOption());
  window.addEventListener("resize", resize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  chart?.dispose();
  chart = null;
});

watch(
  () => [props.xLabels, props.values] as const,
  () => {
    chart?.setOption(buildOption(), {
      notMerge: false,
      lazyUpdate: false,
    });
  },
  { deep: true },
);
// #endif
</script>

<style scoped>
.glc-wrap {
  width: 100%;
}
.glc-toolbar {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  margin-bottom: 8rpx;
}
.glc-periods {
  display: flex;
  flex-direction: row;
  background: #f1f5f9;
  border-radius: 999rpx;
  padding: 4rpx;
}
.glc-period {
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #64748b;
  transition: all 0.25s ease;
}
.glc-period--on {
  background: #fff;
  color: #2563eb;
  box-shadow: 0 2rpx 8rpx rgba(15, 23, 42, 0.08);
}

/* #ifdef H5 */
.glc-host {
  width: 100%;
  height: 320px;
  min-height: 280px;
}
/* #endif */

/* #ifndef H5 */
.glc-fallback {
  width: 100%;
  height: 320rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 16rpx;
  border: 1rpx dashed #cbd5e1;
}
.glc-fallback-txt {
  font-size: 24rpx;
  color: #94a3b8;
  padding: 0 32rpx;
  text-align: center;
}
/* #endif */
</style>
