<template>
  <view class="page">
    <view class="page__top">
      <HeroSection
        :content="heroContent"
        :stats-count="statsCount"
        :active-orders="activeOrders"
        @book="onBook"
        @view-orders="onViewOrders"
        @open-order="onOpenOrder"
        @order-action="onOrderAction"
      />
    </view>

    <GrowthEngineCanvas :data="growthData" />

    <CheckoutDrawer
      :visible="checkoutVisible"
      :summary="checkoutSummary"
      @close="onCheckoutClose"
      @success="onCheckoutSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import HeroSection from "@/components/home/HeroSection.vue";
import GrowthEngineCanvas from "@/components/home/GrowthEngineCanvas.vue";
import CheckoutDrawer from "@/components/checkout/CheckoutDrawer.vue";
import { fetchHomeOrderReminders, markOrderPaid, toPaidOrderItem } from "@/api/modules/orders.js";
import { GROWTH_ENGINE_DATA } from "@/data/defaults/growthEngine";
import { setHomeOrderFocus } from "@/utils/order/homeOrderBridge";
import { setPendingReview } from "@/utils/review/bridge.js";
import { setPendingTraining } from "@/utils/training/bridge";
import type { CheckoutSummary } from "@/types/checkout";
import type { HeroContentProps, HeroStatsCount } from "@/types/home/hero";
import type { GrowthEngineData } from "@/types/home/growthEngine";
import type { MyOrderItem } from "@/types/orders";

const heroContent = ref<HeroContentProps>({
  badge: "语境智练 · 沟通力提升",
  title: {
    line1: "打破表达困境，",
    line2Prefix: "让每一次开口都",
    line2Highlight: "充满力量",
  },
  subtitle: "沉浸式场景训练，帮你在真实语境中练表达、练气场、练应变。",
  primaryButtonText: "立即预约",
});

const statsCount = ref<HeroStatsCount>({
  items: [
    { id: "learners", value: 12000, label: "累计学员", icon: "users" },
    { id: "sessions", value: 48000, label: "训练场次", icon: "sessions" },
    { id: "coaches", value: 86, label: "专业教练", icon: "coaches" },
  ],
});

const growthData = ref<GrowthEngineData>(GROWTH_ENGINE_DATA);
const activeOrders = ref<MyOrderItem[]>([]);
const checkoutVisible = ref(false);
const checkoutSummary = ref<CheckoutSummary | null>(null);
const payTarget = ref<MyOrderItem | null>(null);

async function loadActiveOrders() {
  try {
    activeOrders.value = await fetchHomeOrderReminders(2);
  } catch {
    activeOrders.value = [];
  }
}

onShow(() => {
  loadActiveOrders();
});

function onBook() {
  uni.switchTab({ url: "/pages/scenes/scenes" });
}

function onViewOrders() {
  uni.switchTab({ url: "/pages/my-orders/my-orders" });
}

function onOpenOrder(item: MyOrderItem) {
  setHomeOrderFocus(item.id);
  uni.switchTab({ url: "/pages/my-orders/my-orders" });
}

function parseDateLabel(label: string): string {
  const m = label.match(/(\d+)年(\d+)月(\d+)日/);
  if (!m) return new Date().toISOString().slice(0, 10);
  return `${m[1]}-${String(Number(m[2])).padStart(2, "0")}-${String(Number(m[3])).padStart(2, "0")}`;
}

function buildCheckoutSummary(item: MyOrderItem): CheckoutSummary {
  return {
    expertId: item.expertId ?? `expert-${item.id}`,
    expertName: item.expertName,
    expertTitle: item.expertTitle,
    expertAvatarUrl: item.expertAvatarUrl,
    sceneTag: item.sceneTag,
    dateIso: item.dateIso ?? parseDateLabel(item.dateLabel),
    dateLabel: item.dateLabel,
    timeRange: item.timeRange,
    slotId: item.slotId ?? `slot-${item.id}`,
    originalAmount: item.amount ?? 0,
    sessionMinutes: item.sessionMinutes ?? 45,
    existingOrderId: item.id,
    existingOrderNo: item.orderNo,
  };
}

function onOrderAction(item: MyOrderItem) {
  if (item.status === "pending_pay") {
    payTarget.value = item;
    checkoutSummary.value = buildCheckoutSummary(item);
    checkoutVisible.value = true;
    return;
  }
  if (item.status === "in_progress" && item.canEnterRoom) {
    setPendingTraining({
      scenarioId: item.id,
      roomTitle: item.sceneTag,
      expertName: item.expertName,
      expertTitle: item.expertTitle,
      orderNo: item.orderNo,
    });
    uni.navigateTo({ url: "/pages/room/room" });
    return;
  }
  if (item.status === "pending_review") {
    setPendingReview({
      orderId: item.id,
      orderNo: item.orderNo,
      expertId: item.expertId || "c_default",
      expertName: item.expertName,
      expertTitle: item.expertTitle,
      sceneTag: item.sceneTag,
    });
    uni.navigateTo({ url: "/pages/post-training-review/post-training-review" });
    return;
  }
  onOpenOrder(item);
}

function onCheckoutClose() {
  checkoutVisible.value = false;
  checkoutSummary.value = null;
  loadActiveOrders();
}

function onCheckoutSuccess(payload: { orderId: string; orderNo: string }) {
  markOrderPaid(payload.orderId);
  activeOrders.value = activeOrders.value.map((o) =>
    o.id === payload.orderId ? (toPaidOrderItem(o) as MyOrderItem) : o,
  );
  checkoutVisible.value = false;
  checkoutSummary.value = null;
  loadActiveOrders();
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f1f5f9;
  box-sizing: border-box;
}
.page__top {
  padding: 24rpx 24rpx 0;
  box-sizing: border-box;
}
</style>
