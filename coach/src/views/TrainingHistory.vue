<template>
  <CoachLayout page-title="训练历史" page-subtitle="查看历次训练报告与综合得分">
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="grid grid-cols-3 gap-4 mb-8">
        <StatCard tone="teal" :value="reports.length" label="训练次数" />
        <StatCard tone="green" :value="avgScore" label="平均得分" />
        <StatCard tone="blue" :value="thisMonth" label="本月训练" />
      </div>

      <EmptyState v-if="!reports.length" title="还没有训练记录" description="完成训练后，报告将显示在这里" />

      <div v-else class="space-y-3">
        <div
          v-for="r in reports"
          :key="r.id"
          class="coach-card coach-card-hover p-5"
        >
          <div class="flex items-center justify-between gap-4">
            <div class="flex items-center gap-4">
              <div :class="['w-11 h-11 rounded-xl flex items-center justify-center text-sm font-bold', scoreBg(r.score || r.totalScore)]">
                {{ (r.score || r.totalScore) || "—" }}
              </div>
              <div>
                <p class="font-semibold text-slate-900">{{ r.title || r.sceneName || "训练场景" }}</p>
                <p class="text-xs text-slate-400 mt-0.5">{{ r.date || "未知日期" }}</p>
              </div>
            </div>
            <button
              class="px-4 py-2 text-sm font-medium text-teal-700 border border-teal-200 rounded-xl hover:bg-teal-50 transition-colors"
              @click="viewReport(r.orderId)"
            >
              查看报告
            </button>
          </div>
        </div>
      </div>
    </template>
  </CoachLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import CoachLayout from "@/components/layout/CoachLayout.vue";
import LoadingState from "@/components/ui/LoadingState.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import StatCard from "@/components/ui/StatCard.vue";
import { fetchRecentReports, type ReportSummary } from "@/api/modules/profile";

const router = useRouter();
const loading = ref(true);
const reports = ref<ReportSummary[]>([]);

const avgScore = computed(() => {
  if (!reports.value.length) return "—";
  const sum = reports.value.reduce((a, r) => a + ((r.score || r.totalScore) || 0), 0);
  return String(Math.round(sum / reports.value.length));
});

const thisMonth = computed(() => {
  const now = new Date();
  return reports.value.filter((r) => {
    if (!r.date) return false;
    const d = new Date(r.date);
    return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
  }).length;
});

function scoreBg(s?: number) {
  if (!s) return "bg-slate-100 text-slate-400";
  if (s >= 85) return "bg-emerald-50 text-emerald-600";
  if (s >= 70) return "bg-blue-50 text-blue-600";
  return "bg-amber-50 text-amber-600";
}

function viewReport(orderId?: number) {
  if (orderId) router.push(`/report/${orderId}`);
}

onMounted(async () => {
  reports.value = await fetchRecentReports();
  loading.value = false;
});
</script>
