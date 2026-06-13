<template>
  <CoachLayout page-title="订单详情" :page-subtitle="order ? `#${order.orderId}` : ''">
    <template #header-actions>
      <button class="text-sm text-slate-400 hover:text-teal-600 transition-colors" @click="router.back()">
        返回列表
      </button>
    </template>

    <LoadingState v-if="loading" />

    <template v-else-if="order">
      <div class="grid lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2 space-y-4">
          <!-- 订单概览 -->
          <div class="coach-card p-6">
            <div class="flex items-start justify-between mb-6">
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-teal-500 to-teal-600 flex items-center justify-center text-white text-xl font-bold">
                  {{ order.userName?.charAt(0) || "?" }}
                </div>
                <div>
                  <p class="font-bold text-lg text-slate-900">{{ order.userName }}</p>
                  <p class="text-sm text-slate-500 mt-0.5">{{ order.sceneName }}</p>
                </div>
              </div>
              <span :class="['coach-badge text-sm px-3 py-1', statusBadge(order.status)]">
                {{ statusLabel(order.status) }}
              </span>
            </div>

            <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
              <div class="p-3 bg-slate-50 rounded-xl">
                <p class="text-[10px] text-slate-400 uppercase tracking-wide">开始时间</p>
                <p class="text-sm font-semibold text-slate-900 mt-1">{{ fmt(order.scheduledStart) }}</p>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl">
                <p class="text-[10px] text-slate-400 uppercase tracking-wide">预计时长</p>
                <p class="text-sm font-semibold text-slate-900 mt-1">60 分钟</p>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl">
                <p class="text-[10px] text-slate-400 uppercase tracking-wide">订单号</p>
                <p class="text-sm font-semibold text-slate-900 mt-1">#{{ order.orderId }}</p>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl">
                <p class="text-[10px] text-slate-400 uppercase tracking-wide">训练状态</p>
                <p class="text-sm font-semibold text-slate-900 mt-1">{{ trainingLabel(order.trainingStatus) }}</p>
              </div>
            </div>
          </div>

          <!-- 学员信息与训练目标 -->
          <div class="coach-card p-6">
            <h2 class="font-semibold text-slate-900 mb-4">开课前预览</h2>
            <div class="space-y-4">
              <div class="p-4 bg-teal-50/60 border border-teal-100 rounded-xl">
                <p class="text-xs font-medium text-teal-800 mb-1">训练目标</p>
                <p class="text-sm text-slate-700">{{ trainingGoal }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-xl">
                <p class="text-xs font-medium text-slate-500 mb-1">学员背景</p>
                <p class="text-sm text-slate-700">{{ userBackground }}</p>
              </div>
            </div>
          </div>

          <!-- 提醒 -->
          <div v-if="order.status === 'PAID'" class="coach-card p-4 flex items-center gap-3 border-l-4 border-l-amber-400">
            <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center shrink-0">
              <svg class="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-slate-900">开课前提醒</p>
              <p class="text-xs text-slate-500 mt-0.5">训练将于 {{ fmt(order.scheduledStart) }} 开始，请提前 5 分钟进入房间</p>
            </div>
          </div>
        </div>

        <!-- 操作侧栏 -->
        <div class="space-y-4">
          <div class="coach-card p-6">
            <h2 class="font-semibold text-slate-900 mb-4">快捷操作</h2>
            <div class="space-y-3">
              <button
                v-if="order.roomId"
                class="w-full py-3 bg-teal-600 text-white rounded-xl font-semibold hover:bg-teal-700 transition-colors"
                @click="enterRoom"
              >
                进入训练房间
              </button>
              <button
                v-else-if="order.canEnterRoom"
                disabled
                class="w-full py-3 bg-slate-100 text-slate-400 rounded-xl font-medium cursor-not-allowed"
              >
                等待学员开始
              </button>
              <button
                v-if="order.status === 'COMPLETED'"
                class="w-full py-3 border border-teal-200 text-teal-700 rounded-xl font-medium hover:bg-teal-50 transition-colors"
                @click="router.push(`/report/${order.orderId}`)"
              >
                查看训练报告
              </button>
            </div>
          </div>

          <div v-if="order.trainingStatus === 'IN_PROGRESS'" class="coach-card p-4 flex items-center gap-2">
            <span class="w-2.5 h-2.5 bg-emerald-400 rounded-full animate-pulse" />
            <span class="text-sm text-emerald-700 font-medium">训练进行中</span>
          </div>
        </div>
      </div>
    </template>
  </CoachLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import CoachLayout from "@/components/layout/CoachLayout.vue";
import LoadingState from "@/components/ui/LoadingState.vue";
import { fetchOrderDetail } from "@/api/modules/orders";
import type { OrderApiRecord } from "@/types/videoConference";

const route = useRoute();
const router = useRouter();
const order = ref<OrderApiRecord | null>(null);
const loading = ref(true);

const trainingGoal = ref("提升结构化表达与临场抗压能力，重点练习结论先行与数据支撑");
const userBackground = ref("互联网产品岗，有 3 年工作经验，准备管理岗晋升答辩");

function statusLabel(s: string) {
  const m: Record<string, string> = {
    PENDING_PAY: "待支付", PAID: "待训练", IN_SERVICE: "训练中",
    COMPLETED: "已完成", CANCELLED: "已取消",
  };
  return m[s] || s;
}

function statusBadge(s: string) {
  const m: Record<string, string> = {
    PAID: "bg-blue-50 text-blue-600",
    IN_SERVICE: "bg-emerald-50 text-emerald-700",
    COMPLETED: "bg-slate-100 text-slate-500",
    CANCELLED: "bg-red-50 text-red-500",
  };
  return m[s] || "bg-slate-100 text-slate-500";
}

function trainingLabel(s: string | null) {
  const m: Record<string, string> = {
    IN_PROGRESS: "进行中", ENDED: "已结束", REPORT_READY: "报告已生成",
  };
  return m[s || ""] || s || "待开始";
}

function fmt(iso: string) {
  if (!iso) return "";
  const d = new Date(iso);
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours().toString().padStart(2, "0")}:${d.getMinutes().toString().padStart(2, "0")}`;
}

function enterRoom() {
  if (order.value?.roomId) router.push(`/room/${order.value.roomId}`);
}

onMounted(async () => {
  const id = route.params.id as string;
  if (id) {
    order.value = await fetchOrderDetail(id);
    const ext = order.value as OrderApiRecord & { trainingGoal?: string; userBackground?: string };
    if (ext.trainingGoal) trainingGoal.value = ext.trainingGoal;
    if (ext.userBackground) userBackground.value = ext.userBackground;
  }
  loading.value = false;
});
</script>
