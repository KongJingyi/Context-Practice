<template>
  <CoachLayout page-title="收入明细" page-subtitle="每笔订单的支付金额、平台抽成与实际到账一目了然">
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard tone="teal" icon="¥" :value="`¥${summary.monthTotal.toLocaleString()}`" label="本月流水" hint="学员支付总额" />
        <StatCard tone="green" icon="+" :value="`¥${summary.monthNet.toLocaleString()}`" label="本月到账" hint="扣除平台抽成后" />
        <StatCard tone="amber" icon="…" :value="`¥${summary.pendingAmount.toLocaleString()}`" label="待结算" />
        <StatCard tone="blue" icon="#" :value="summary.orderCount" label="本月订单" />
      </div>

      <div class="coach-card overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h2 class="font-semibold text-slate-900">收入记录</h2>
          <span class="text-xs text-slate-400">平台抽成比例 15%</span>
        </div>

        <EmptyState v-if="!records.length" title="暂无收入记录" description="完成陪练订单后，收入将在此展示" />

        <div v-else class="divide-y divide-slate-50">
          <div
            v-for="r in records"
            :key="r.id"
            class="px-6 py-4 flex items-center gap-4 hover:bg-slate-50/80 transition-colors"
          >
            <div class="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center text-teal-600 font-bold text-sm shrink-0">
              {{ r.userName.charAt(0) }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <p class="font-medium text-slate-900 truncate">{{ r.sceneName }}</p>
                <span :class="['coach-badge', statusClass(r.status)]">{{ statusLabel(r.status) }}</span>
              </div>
              <p class="text-xs text-slate-400 mt-0.5">
                订单 #{{ r.orderId }} · {{ r.userName }}
                <template v-if="r.settledAt"> · {{ r.settledAt }}</template>
              </p>
            </div>
            <div class="text-right shrink-0">
              <p class="text-lg font-bold text-emerald-600">+¥{{ r.netAmount.toFixed(2) }}</p>
              <p class="text-[11px] text-slate-400 mt-0.5">
                支付 ¥{{ r.paidAmount }} · 抽成 ¥{{ r.platformFee.toFixed(2) }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </CoachLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import CoachLayout from "@/components/layout/CoachLayout.vue";
import LoadingState from "@/components/ui/LoadingState.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import StatCard from "@/components/ui/StatCard.vue";
import { fetchIncomeRecords, fetchIncomeSummary, type IncomeRecord } from "@/api/modules/income";

const loading = ref(true);
const summary = ref({ monthTotal: 0, monthNet: 0, pendingAmount: 0, orderCount: 0 });
const records = ref<IncomeRecord[]>([]);

function statusLabel(s: string) {
  return { SETTLED: "已到账", PENDING: "待结算", REFUNDED: "已退款" }[s] || s;
}

function statusClass(s: string) {
  return {
    SETTLED: "bg-emerald-50 text-emerald-700",
    PENDING: "bg-amber-50 text-amber-700",
    REFUNDED: "bg-red-50 text-red-500",
  }[s] || "bg-slate-100 text-slate-500";
}

onMounted(async () => {
  [summary.value, records.value] = await Promise.all([
    fetchIncomeSummary(),
    fetchIncomeRecords(),
  ]);
  loading.value = false;
});
</script>
