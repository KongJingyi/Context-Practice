<template>
  <CoachLayout page-title="订单工作台" page-subtitle="管理待训练、进行中和已完成的陪练订单">
    <LoadingState v-if="loading" />

    <template v-else>
      <!-- 统计 -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard
          v-for="(stat, i) in stats"
          :key="stat.key"
          :value="stat.value"
          :label="stat.label"
          :tone="(['teal', 'blue', 'green', 'slate'] as const)[i]"
        />
      </div>

      <!-- 筛选 -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="[
            'px-4 py-2 rounded-xl text-sm font-medium transition-all',
            tab.key === activeTab
              ? 'bg-teal-600 text-white shadow-md shadow-teal-200/60'
              : 'bg-white text-slate-500 border border-slate-200 hover:border-teal-200 hover:text-teal-700',
          ]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span v-if="tabCount(tab.key)" class="ml-1.5 opacity-70">({{ tabCount(tab.key) }})</span>
        </button>
      </div>

      <EmptyState
        v-if="!filteredOrders.length"
        title="暂无订单"
        :description="`当前没有${activeTabLabel}的陪练订单`"
      />

      <div v-else class="space-y-3">
        <div
          v-for="order in filteredOrders"
          :key="order.orderId"
          class="coach-card coach-card-hover p-5 cursor-pointer group"
          @click="goDetail(order.orderId)"
        >
          <div class="flex items-center justify-between gap-4">
            <div class="flex items-center gap-4 min-w-0">
              <div class="relative shrink-0">
                <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-teal-500 to-teal-600 flex items-center justify-center text-white font-bold text-lg">
                  {{ (order.userName || "?").charAt(0) }}
                </div>
                <span
                  v-if="order.status === 'IN_SERVICE'"
                  class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 bg-emerald-400 rounded-full border-2 border-white"
                />
              </div>
              <div class="min-w-0">
                <div class="flex items-center gap-2 flex-wrap">
                  <p class="font-semibold text-slate-900 group-hover:text-teal-700 transition-colors">{{ order.userName }}</p>
                  <span class="text-slate-300">·</span>
                  <span class="text-sm text-slate-500">{{ order.sceneName }}</span>
                </div>
                <p class="text-xs text-slate-400 mt-1 font-mono">
                  {{ formatDate(order.scheduledStart) }}
                  <template v-if="order.scheduledEnd"> — {{ order.scheduledEnd.slice(11, 16) }}</template>
                </p>
              </div>
            </div>
            <div class="flex items-center gap-3 shrink-0">
              <span :class="['coach-badge', statusClass(order.status)]">{{ statusLabel(order.status) }}</span>
              <button
                v-if="order.canEnterRoom && order.roomId"
                class="px-4 py-2 bg-teal-600 text-white text-sm font-medium rounded-xl hover:bg-teal-700 transition-colors"
                @click.stop="enterRoom(order.roomId!)"
              >
                进入房间
              </button>
              <span v-else-if="order.canEnterRoom" class="text-xs text-slate-400 px-3">等待学员开始</span>
            </div>
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
import { fetchCoachOrders } from "@/api/modules/orders";
import type { CoachOrderRecord } from "@/types/videoConference";

const router = useRouter();
const loading = ref(true);
const orders = ref<CoachOrderRecord[]>([]);
const activeTab = ref("");

const tabs = [
  { key: "", label: "全部" },
  { key: "PAID", label: "待训练" },
  { key: "IN_SERVICE", label: "训练中" },
  { key: "COMPLETED", label: "已完成" },
];

const activeTabLabel = computed(() => tabs.find((t) => t.key === activeTab.value)?.label || "全部");

const filteredOrders = computed(() => {
  if (!activeTab.value) return orders.value;
  return orders.value.filter((o) => o.status === activeTab.value);
});

const stats = computed(() => {
  const all = orders.value;
  return [
    { key: "total", label: "全部订单", value: all.length },
    { key: "paid", label: "待训练", value: all.filter((o) => o.status === "PAID").length },
    { key: "active", label: "训练中", value: all.filter((o) => o.status === "IN_SERVICE").length },
    { key: "done", label: "已完成", value: all.filter((o) => o.status === "COMPLETED").length },
  ];
});

function tabCount(key: string) {
  if (!key) return 0;
  return orders.value.filter((o) => o.status === key).length;
}

function statusLabel(s: string) {
  const m: Record<string, string> = {
    PENDING_PAY: "待支付", PAID: "待训练", IN_SERVICE: "训练中",
    COMPLETED: "已完成", CANCELLED: "已取消",
  };
  return m[s] || s;
}

function statusClass(s: string) {
  const m: Record<string, string> = {
    PAID: "bg-blue-50 text-blue-700",
    IN_SERVICE: "bg-emerald-50 text-emerald-700",
    COMPLETED: "bg-slate-100 text-slate-500",
    CANCELLED: "bg-red-50 text-red-500",
  };
  return m[s] || "bg-slate-100 text-slate-500";
}

function formatDate(iso: string) {
  if (!iso) return "";
  const d = new Date(iso);
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function goDetail(orderId: number) { router.push(`/orders/${orderId}`); }
function enterRoom(roomId: string) { router.push(`/room/${roomId}`); }

onMounted(async () => {
  orders.value = await fetchCoachOrders();
  loading.value = false;
});
</script>
