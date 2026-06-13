<template>
  <div class="min-h-screen bg-slate-50">
    <header class="bg-white border-b border-slate-200 sticky top-0 z-10">
      <div class="max-w-3xl mx-auto px-6 h-14 flex items-center gap-4">
        <button class="text-sm text-slate-400 hover:text-teal-600 transition-colors flex items-center gap-1" @click="router.back()">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
          返回
        </button>
        <span class="text-slate-300">/</span>
        <span class="text-sm font-medium text-slate-700">训练报告</span>
      </div>
    </header>

    <div class="max-w-3xl mx-auto px-6 py-8">
      <LoadingState v-if="loading" />
      <EmptyState v-else-if="!report" title="暂无报告" description="训练报告尚未生成或无权查看" />

      <template v-else>
        <div class="coach-card p-6 mb-4">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-xl font-bold text-slate-900">{{ report.order_info.scene }}</h1>
              <p class="text-sm text-slate-400 mt-1">{{ report.order_info.date }} · {{ report.order_info.expert_name }}</p>
            </div>
            <div class="text-center">
              <p class="text-4xl font-bold text-teal-600">{{ report.scores.total }}</p>
              <p class="text-xs text-slate-400">综合评分</p>
            </div>
          </div>
        </div>

        <div class="coach-card p-6 mb-4">
          <h2 class="font-semibold text-slate-900 mb-5">五维评分</h2>
          <div class="space-y-4">
            <div v-for="(score, i) in report.scores.dimensions" :key="i">
              <div class="flex justify-between text-sm mb-1.5">
                <span class="text-slate-600">{{ dimLabels[i] }}</span>
                <span class="font-bold text-slate-900">{{ score }}<span class="text-xs text-slate-400 font-normal">/100</span></span>
              </div>
              <div class="h-2 bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full rounded-full transition-all duration-700 bg-teal-500" :style="{ width: score + '%', opacity: score / 100 }" />
              </div>
            </div>
          </div>
        </div>

        <div class="coach-card p-6 mb-4" v-if="report.expert_feedback?.length">
          <h2 class="font-semibold text-slate-900 mb-4">专家反馈</h2>
          <div class="space-y-3">
            <div
              v-for="(fb, i) in report.expert_feedback"
              :key="i"
              :class="['p-4 rounded-xl border', fb.type === 'highlight' ? 'bg-emerald-50 border-emerald-100' : 'bg-amber-50 border-amber-100']"
            >
              <div class="flex items-center gap-2 mb-1">
                <span :class="['text-xs font-bold', fb.type === 'highlight' ? 'text-emerald-600' : 'text-amber-600']">
                  {{ fb.type === 'highlight' ? '亮点' : '待改善' }}
                </span>
                <span class="text-xs text-slate-400 font-mono">{{ fb.timestamp }}</span>
              </div>
              <p class="text-sm text-slate-700">{{ fb.content }}</p>
              <p v-if="fb.suggestion" class="text-xs text-slate-500 mt-1">{{ fb.suggestion }}</p>
            </div>
          </div>
        </div>

        <div class="coach-card p-6 text-center" v-if="report.milestone">
          <p class="text-sm text-slate-600">{{ report.milestone }}</p>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import LoadingState from "@/components/ui/LoadingState.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import { fetchReportByOrder } from "@/api/modules/coach";

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const report = ref<Record<string, any> | null>(null);

const dimLabels = ["逻辑清晰度", "表达流畅度", "内容价值", "临场抗压", "时间把控"];

onMounted(async () => {
  const orderId = route.params.orderId as string;
  try {
    report.value = (await fetchReportByOrder(orderId)) as Record<string, any>;
  } catch {
    report.value = null;
  }
  loading.value = false;
});
</script>
