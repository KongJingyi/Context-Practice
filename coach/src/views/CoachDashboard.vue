<template>
  <CoachLayout page-title="数据统计" page-subtitle="服务时长、接单量与好评率对全员可见">
    <LoadingState v-if="loading" />

    <template v-else-if="dashboard">
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard tone="teal" :value="dashboard.totalSessions" label="总训练场次" />
        <StatCard tone="blue" :value="`${dashboard.totalHours.toFixed(1)}h`" label="总训练时长" />
        <StatCard tone="green" :value="`${(dashboard.goodReviewRate * 100).toFixed(0)}%`" label="好评率" />
        <StatCard tone="amber" :value="`¥${dashboard.monthIncome.toLocaleString()}`" label="本月收入" />
      </div>

      <div class="grid lg:grid-cols-3 gap-6">
        <!-- 等级晋升 -->
        <div class="coach-card p-6 lg:col-span-2">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h2 class="font-semibold text-slate-900">陪练等级</h2>
              <p class="text-xs text-slate-400 mt-1">实习陪练 → 正式陪练 → 金牌陪练</p>
            </div>
            <span class="coach-badge bg-amber-50 text-amber-700 text-sm px-3 py-1">{{ dashboard.levelName }}</span>
          </div>
          <div class="h-3 bg-slate-100 rounded-full overflow-hidden mb-2">
            <div
              class="h-full bg-gradient-to-r from-teal-500 to-teal-400 rounded-full transition-all duration-700"
              :style="{ width: `${dashboard.levelProgress * 100}%` }"
            />
          </div>
          <div class="flex justify-between text-xs text-slate-400">
            <span>当前进度</span>
            <span class="font-semibold text-teal-600">{{ (dashboard.levelProgress * 100).toFixed(0) }}%</span>
          </div>

          <div class="grid grid-cols-3 gap-3 mt-8">
            <div v-for="lvl in levelSteps" :key="lvl.name" :class="['p-4 rounded-xl border text-center', lvl.active ? 'border-teal-200 bg-teal-50' : 'border-slate-100 bg-slate-50']">
              <p :class="['text-sm font-bold', lvl.active ? 'text-teal-700' : 'text-slate-400']">{{ lvl.name }}</p>
              <p class="text-[10px] text-slate-400 mt-1">{{ lvl.desc }}</p>
            </div>
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="coach-card p-6">
          <h2 class="font-semibold text-slate-900 mb-4">成长建议</h2>
          <ul class="space-y-3 text-sm text-slate-600">
            <li class="flex gap-2 p-3 bg-slate-50 rounded-xl">
              <span class="text-teal-600 shrink-0">1</span>
              保持好评率 ≥ 95% 可加速升级
            </li>
            <li class="flex gap-2 p-3 bg-slate-50 rounded-xl">
              <span class="text-teal-600 shrink-0">2</span>
              完成 20 场训练解锁金牌陪练申请
            </li>
            <li class="flex gap-2 p-3 bg-slate-50 rounded-xl">
              <span class="text-teal-600 shrink-0">3</span>
              及时提交课后反馈报告
            </li>
          </ul>
        </div>
      </div>

      <!-- 录制回放 -->
      <div v-if="showRecordingSection" class="coach-card p-6 mt-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="font-semibold text-slate-900">最近训练录制</h2>
            <p v-if="recordingStatusLabel" class="text-xs text-slate-400 mt-1">{{ recordingStatusLabel }}</p>
          </div>
          <a
            v-if="recording?.recordingUrl"
            :href="recording.recordingUrl"
            target="_blank"
            rel="noopener"
            class="text-xs text-teal-600 hover:underline"
          >查看回放</a>
        </div>
        <div v-if="recording?.highlights?.length" class="grid md:grid-cols-3 gap-3">
          <div
            v-for="(h, i) in recording.highlights"
            :key="i"
            class="p-4 bg-slate-50 rounded-xl border border-slate-100"
          >
            <span class="text-xs font-bold text-teal-600">片段 {{ i + 1 }}</span>
            <p class="text-sm font-medium text-slate-800 mt-2">{{ h.label }}</p>
            <p class="text-xs text-slate-400 font-mono mt-1">{{ formatTime(h.startSec) }} – {{ formatTime(h.endSec) }}</p>
            <a v-if="h.clipUrl" :href="h.clipUrl" target="_blank" rel="noopener" class="text-xs text-teal-600 hover:underline mt-1 inline-block">播放片段</a>
          </div>
        </div>
        <p v-else-if="recording?.recordingUrl" class="text-sm text-slate-500">
          本场暂无高光标记，可直接查看完整回放。
        </p>
        <p v-else-if="recording?.recordingStatus === 'PROCESSING'" class="text-sm text-slate-500">
          录制已结束，正在生成回放，通常需 30 秒至 3 分钟。
        </p>
      </div>
    </template>
  </CoachLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import CoachLayout from "@/components/layout/CoachLayout.vue";
import LoadingState from "@/components/ui/LoadingState.vue";
import StatCard from "@/components/ui/StatCard.vue";
import { fetchCoachDashboard, fetchRecording, type RecordingResult } from "@/api/modules/coach";
import type { CoachDashboard } from "@/types/videoConference";

const loading = ref(true);
const dashboard = ref<CoachDashboard | null>(null);
const recording = ref<RecordingResult | null>(null);

const levelSteps = computed(() => {
  const name = dashboard.value?.levelName || "";
  return [
    { name: "实习陪练", desc: "0-10 场", active: name.includes("实习") },
    { name: "正式陪练", desc: "10-50 场", active: name.includes("正式") },
    { name: "金牌陪练", desc: "50+ 场", active: name.includes("金牌") },
  ];
});

const showRecordingSection = computed(() => {
  const r = recording.value;
  if (!r) return false;
  return Boolean(r.recordingUrl)
    || (r.highlights?.length ?? 0) > 0
    || (r.recordingStatus && r.recordingStatus !== "IDLE");
});

const recordingStatusLabel = computed(() => {
  const status = recording.value?.recordingStatus;
  if (!status || status === "READY") return "";
  const labels: Record<string, string> = {
    RECORDING: "录制中",
    PROCESSING: "生成回放中",
    FAILED: "录制失败",
    EXPIRED: "回放已过期",
  };
  return labels[status] ?? status;
});

function formatTime(sec: number): string {
  return `${Math.floor(sec / 60)}:${String(sec % 60).padStart(2, "0")}`;
}

onMounted(async () => {
  dashboard.value = await fetchCoachDashboard();
  const trainingId = dashboard.value?.lastTrainingId;
  if (trainingId) {
    recording.value = await fetchRecording(trainingId);
  }
  loading.value = false;
});
</script>
