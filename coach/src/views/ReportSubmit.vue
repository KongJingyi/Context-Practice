<template>
  <div class="min-h-screen bg-slate-50">
    <header class="bg-white border-b border-slate-200 sticky top-0 z-10">
      <div class="max-w-3xl mx-auto px-6 h-14 flex items-center gap-4">
        <button class="text-sm text-slate-400 hover:text-teal-600 transition-colors flex items-center gap-1" @click="router.back()">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
          返回
        </button>
        <span class="text-slate-300">/</span>
        <span class="text-sm font-medium text-slate-700">课后反馈报告</span>
      </div>
    </header>

    <div class="max-w-3xl mx-auto px-6 py-8">
      <div class="coach-card p-6 md:p-8">
        <div class="mb-8">
          <h1 class="text-xl font-bold text-slate-900">提交训练反馈</h1>
          <p class="text-sm text-slate-400 mt-1">订单 #{{ orderId }} · 请从五个维度评分并给出改进建议</p>
        </div>

        <div class="mb-8">
          <p class="text-sm font-semibold text-slate-700 mb-5">五维评分</p>
          <div class="space-y-6">
            <div v-for="dim in dimensions" :key="dim.key" class="flex items-center gap-4">
              <div class="w-28 shrink-0">
                <p class="text-sm font-medium text-slate-700">{{ dim.label }}</p>
                <p class="text-[10px] text-slate-400 mt-0.5">{{ dim.hint }}</p>
              </div>
              <el-slider
                v-model="form.scores[dim.key]"
                :max="5"
                :step="1"
                show-stops
                class="flex-1"
              />
              <span class="w-8 text-lg font-bold text-teal-600 text-right">{{ form.scores[dim.key] }}</span>
            </div>
          </div>
        </div>

        <div class="mb-8">
          <p class="text-sm font-semibold text-slate-700 mb-3">详细点评</p>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="总结学员本次表现，给出针对性改进建议…"
          />
        </div>

        <div class="mb-8">
          <p class="text-sm font-semibold text-slate-700 mb-3">改进建议模板（点击插入）</p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tpl in templates"
              :key="tpl"
              class="px-3 py-1.5 text-xs bg-slate-100 text-slate-600 rounded-lg hover:bg-teal-50 hover:text-teal-700 transition-colors"
              @click="insertTemplate(tpl)"
            >
              {{ tpl }}
            </button>
          </div>
        </div>

        <div class="flex gap-3 pt-6 border-t border-slate-100">
          <button
            class="px-8 py-2.5 bg-teal-600 text-white rounded-xl hover:bg-teal-700 transition-colors text-sm font-semibold disabled:opacity-50"
            :disabled="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? "提交中…" : "提交反馈报告" }}
          </button>
          <button class="px-6 py-2.5 bg-slate-100 text-slate-600 rounded-xl hover:bg-slate-200 transition-colors text-sm font-medium" @click="router.back()">
            稍后提交
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { submitCoachReport } from "@/api/modules/coach";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const orderId = Number(route.params.orderId) || 0;
const submitting = ref(false);

const dimensions = [
  { key: "logic" as const, label: "逻辑清晰度", hint: "结构是否清晰、论证是否完整" },
  { key: "fluency" as const, label: "表达流畅度", hint: "语言是否自然、有无卡顿" },
  { key: "content" as const, label: "内容价值", hint: "观点深度与信息密度" },
  { key: "pressure" as const, label: "临场抗压", hint: "追问下的稳定性" },
  { key: "time" as const, label: "时间把控", hint: "是否在规定时间内完成" },
];

const templates = [
  "建议采用「结论-论据-总结」三段式结构",
  "追问环节可提前准备 2-3 个数据支撑点",
  "注意控制语速，关键结论处适当停顿",
  "开场 30 秒内亮明核心观点",
];

const form = reactive({
  scores: { logic: 4, fluency: 4, content: 4, pressure: 3, time: 4 },
  content: "",
});

function insertTemplate(tpl: string) {
  form.content = form.content ? `${form.content}\n${tpl}` : tpl;
}

async function handleSubmit() {
  if (!form.content.trim()) {
    ElMessage.warning("请填写详细点评");
    return;
  }
  submitting.value = true;
  try {
    await submitCoachReport(orderId, {
      order_id: orderId,
      scores: {
        professional: form.scores.logic,
        attitude: form.scores.fluency,
        quality: form.scores.content,
        pressure: form.scores.pressure,
        time: form.scores.time,
      },
      content: form.content,
    });
    ElMessage.success("反馈报告已提交");
    router.replace("/history");
  } catch {
    ElMessage.error("提交失败");
  } finally {
    submitting.value = false;
  }
}
</script>
