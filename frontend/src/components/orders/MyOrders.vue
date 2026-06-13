<template>
  <view class="mo">
    <view class="mo-nav" :style="{ paddingTop: statusBarPx + 'px' }">
      <view class="mo-nav-inner">
        <text class="mo-nav-title">我的订单</text>
        <view class="mo-seg">
          <view class="mo-seg-thumb" :class="{ 'mo-seg-thumb--right': tab === 'history' }" />
          <view class="mo-seg-row">
            <view class="mo-seg-item" @tap="switchTab('active')">
              <text class="mo-seg-txt" :class="{ 'mo-seg-txt--on': tab === 'active' }">进行中</text>
            </view>
            <view class="mo-seg-item" @tap="switchTab('history')">
              <text class="mo-seg-txt" :class="{ 'mo-seg-txt--on': tab === 'history' }">历史订单</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="mo-body">
      <view :key="`${tab}-${listVersion}`" class="mo-list mo-list--refresh">
        <view
          v-for="item in orders"
          :key="item.id"
          class="mo-card"
          :class="{ 'mo-card--flash': flashOrderId === item.id }"
          :style="cardBorderStyle(item.status)"
          @tap="toggleExpand(item.id)"
        >
          <view
            v-if="ribbonText(item)"
            class="mo-ribbon"
            :style="ribbonStyle(item.status)"
          >
            <text class="mo-ribbon-txt">{{ ribbonText(item) }}</text>
          </view>

          <view v-if="item.status === 'pending_pay'" class="mo-countdown">
            <text class="mo-countdown-ico">⏱</text>
            <text class="mo-countdown-txt">请在 {{ countdownText(item) }} 内完成支付</text>
            </view>

          <view class="mo-card-inner">
              <view class="mo-card-left">
              <view class="mo-avatar mo-avatar--square" :class="avatarClass(item)">
                <text class="mo-avatar-letter">{{ item.expertName.slice(0, 1) }}</text>
                </view>
                <view class="mo-info">
                  <view class="mo-meta-row">
                  <text class="mo-status-tag" :style="statusTagStyle(item.status)">
                    {{ item.sessionEnded ? "已结束" : statusMeta(item.status).label }}
                  </text>
                    <text class="mo-cat-tag">{{ item.sceneTag }}</text>
                </view>
                <view
                  class="mo-orderno-wrap"
                  @mouseenter="hoverOrderId = item.id"
                  @mouseleave="hoverOrderId = ''"
                  @tap.stop="copyOrderNo(item.orderNo)"
                >
                    <text class="mo-orderno">订单号: {{ item.orderNo }}</text>
                  <!-- #ifdef H5 -->
                  <view v-if="hoverOrderId === item.id" class="mo-copy-tip">
                    <text>点击复制</text>
                  </view>
                  <!-- #endif -->
                  </view>
                  <view class="mo-expert-line">
                    <text class="mo-expert-name">{{ item.expertName }}</text>
                    <text class="mo-expert-slash"> / </text>
                    <text class="mo-expert-title">{{ item.expertTitle }}</text>
                  </view>
                  <view class="mo-time-row">
                    <text class="mo-date-txt">{{ item.dateLabel }}</text>
                  <text class="mo-time-txt" :class="{ 'mo-time-txt--hl': item.isToday }">
                    {{ item.timeRange }}
                  </text>
                    <text v-if="item.isToday" class="mo-today">（今天）</text>
                </view>
                <text v-if="item.amount" class="mo-amount">¥{{ item.amount }}</text>
              </view>
            </view>

            <view class="mo-card-right" @tap.stop>
              <template v-if="item.status === 'pending_pay'">
                <view class="mo-btn mo-btn-primary" @tap="onPay(item)">
                  <text class="mo-btn-txt">去支付</text>
                </view>
                <text class="mo-link" @tap="openCancel(item)">取消订单</text>
              </template>
              <template v-else-if="item.status === 'in_progress'">
                <view v-if="item.canEnterRoom" class="mo-btn mo-btn-primary" @tap="onEnterRoom(item)">
                  <text class="mo-btn-txt">进入训练房间</text>
                </view>
                <view v-if="item.canRefund" class="mo-btn mo-btn-outline" @tap="openRefundApply(item)">
                  <text class="mo-btn-txt-outline">申请退款</text>
              </view>
                <text v-if="item.canCancel" class="mo-link" @tap="openCancel(item)">取消订单</text>
                <text v-else-if="item.enterDeniedReason" class="mo-hint">{{ item.enterDeniedReason }}</text>
          </template>
              <template v-else-if="item.status === 'expired'">
                <view class="mo-btn mo-btn-gray" @tap="onBookAgain(item)">
                  <text class="mo-btn-txt-gray">再次预约</text>
                </view>
                <text v-if="item.enterDeniedReason" class="mo-hint">{{ item.enterDeniedReason }}</text>
              </template>
              <template v-else-if="item.status === 'pending_review'">
                <view class="mo-btn mo-btn-primary" @tap="onReview(item)">
                  <text class="mo-btn-txt">去复盘评价</text>
                </view>
              </template>
              <template v-else-if="item.status === 'refunding'">
                <view class="mo-btn mo-btn-outline" @tap="openRefundView(item)">
                  <text class="mo-btn-txt-outline">查看退款</text>
                </view>
              </template>
              <template v-else-if="item.status === 'completed'">
                <view v-if="item.canReview" class="mo-btn mo-btn-primary" @tap="onReview(item)">
                  <text class="mo-btn-txt">去复盘评价</text>
                </view>
                <view v-else-if="item.reportReady !== false" class="mo-btn mo-btn-outline" @tap="onViewReport(item)">
                  <text class="mo-btn-txt-outline">查看反馈报告</text>
                </view>
                <view class="mo-btn mo-btn-gray" @tap="onBookAgain(item)">
                  <text class="mo-btn-txt-gray">再次预约</text>
                </view>
              </template>
              <template v-else>
                <view class="mo-btn mo-btn-gray" @tap="onBookAgain(item)">
                  <text class="mo-btn-txt-gray">再次预约</text>
                </view>
              </template>
                </view>
              </view>

          <view v-if="item.refundSteps?.length && item.status === 'refunding'" class="mo-refund-bar">
            <view
              v-for="(s, i) in item.refundSteps"
              :key="s.key"
              class="mo-refund-step"
              :class="{ 'mo-refund-step--done': s.done, 'mo-refund-step--cur': s.current }"
            >
              <view class="mo-refund-dot" />
              <text>{{ s.label }}</text>
              <view v-if="i < item.refundSteps.length - 1" class="mo-refund-line" />
            </view>
          </view>

          <view v-if="expandedId === item.id && item.timeline?.length" class="mo-timeline-wrap">
            <text class="mo-timeline-title">订单生命周期</text>
            <OrderTimeline :events="item.timeline" />
            <text
              v-if="item.status === 'completed' || item.status === 'pending_review'"
              class="mo-complaint-link"
              @tap.stop="openComplaint(item)"
            >投诉与申诉</text>
          </view>
        </view>

        <view v-if="!orders.length" class="mo-empty">
          <text>{{ tab === "active" ? "暂无进行中的订单" : "暂无历史订单" }}</text>
        </view>
      </view>
    </view>

    <OrderCancelModal
      :visible="cancelVisible"
      @close="cancelVisible = false"
      @confirm="onCancelConfirm"
    />
    <OrderRefundModal
      :visible="refundVisible"
      :mode="refundMode"
      :steps="refundSteps"
      @close="refundVisible = false"
      @confirm="onRefundConfirm"
    />

    <CheckoutDrawer
      :visible="checkoutVisible"
      :summary="checkoutSummary"
      @close="onCheckoutClose"
      @success="onCheckoutSuccess"
      @enter-room="onEnterRoomAfterPay"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { fetchOrders, cancelOrder, applyRefund, markOrderPaid, toPaidOrderItem } from "@/api/modules/orders.js";
import { consumeHomeOrderFocus } from "@/utils/order/homeOrderBridge";
import OrderTimeline from "@/components/orders/OrderTimeline.vue";
import OrderCancelModal from "@/components/orders/OrderCancelModal.vue";
import OrderRefundModal from "@/components/orders/OrderRefundModal.vue";
import CheckoutDrawer from "@/components/checkout/CheckoutDrawer.vue";
import type { MyOrderItem, OrderListTab, OrderStatus, RefundProgressStep } from "@/types/orders";
import { ORDER_STATUS_MAP } from "@/types/orders";
import type { CheckoutSummary } from "@/types/checkout";
import { setPendingReview } from "@/utils/review/bridge.js";
import { navigateToTrainingRoom } from "@/utils/training/enterRoom.js";

const tab = ref<OrderListTab>("active");
const orders = ref<MyOrderItem[]>([]);
const statusBarPx = ref(20);
const expandedId = ref("");
const hoverOrderId = ref("");
const cancelVisible = ref(false);
const cancelTarget = ref<MyOrderItem | null>(null);
const refundVisible = ref(false);
const refundMode = ref<"apply" | "view">("apply");
const refundSteps = ref<RefundProgressStep[]>([]);
const refundTarget = ref<MyOrderItem | null>(null);
const checkoutVisible = ref(false);
const checkoutSummary = ref<CheckoutSummary | null>(null);
const payTarget = ref<MyOrderItem | null>(null);
const listVersion = ref(0);
const flashOrderId = ref("");

const countdownMap = ref<Record<string, string>>({});
let tickTimer: ReturnType<typeof setInterval> | null = null;
let refreshTimer: ReturnType<typeof setInterval> | null = null;

try {
  const sys = uni.getSystemInfoSync();
  statusBarPx.value = sys.statusBarHeight || 20;
} catch {
  statusBarPx.value = 20;
}

function statusMeta(status: OrderStatus) {
  return ORDER_STATUS_MAP[status];
}

function ribbonText(item: MyOrderItem) {
  return item.ribbonLabel || statusMeta(item.status).ribbon || "";
}

function statusTagStyle(status: OrderStatus) {
  const m = statusMeta(status);
  return { color: m.color, background: m.bg, border: `1rpx solid ${m.border}` };
}

function cardBorderStyle(status: OrderStatus) {
  const m = statusMeta(status);
  if (status === "in_progress" || status === "pending_pay") {
    return { border: `3rpx solid ${m.border}`, boxShadow: `0 4rpx 20rpx ${m.bg}` };
  }
  if (status === "expired") {
    return { border: `1rpx solid ${m.border}`, opacity: "0.92" };
  }
  return { border: `1rpx solid ${m.border}` };
}

function ribbonStyle(status: OrderStatus) {
  const m = statusMeta(status);
  return { background: `linear-gradient(135deg, ${m.color}, ${m.border})` };
}

function avatarClass(item: MyOrderItem) {
  if (item.status === "in_progress" || item.status === "pending_pay") return "mo-avatar--live";
  if (item.status === "completed" || item.status === "refunded") return "mo-avatar--muted";
  return "";
}

function updateCountdowns() {
  const map: Record<string, string> = {};
  const now = Date.now();
  for (const o of orders.value) {
    if (o.status === "pending_pay" && o.payExpireAt) {
      const diff = o.payExpireAt - now;
      if (diff <= 0) map[o.id] = "00:00";
      else {
        const m = Math.floor(diff / 60000);
        const s = Math.floor((diff % 60000) / 1000);
        map[o.id] = `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
      }
    }
  }
  countdownMap.value = map;
}

function countdownText(item: MyOrderItem) {
  return countdownMap.value[item.id] ?? "15:00";
}

function startTicker() {
  if (tickTimer) clearInterval(tickTimer);
  updateCountdowns();
  tickTimer = setInterval(updateCountdowns, 1000);
}

function startAutoRefresh() {
  if (refreshTimer) clearInterval(refreshTimer);
  refreshTimer = setInterval(() => {
    if (tab.value === "active") load();
  }, 30000);
}

onUnmounted(() => {
  if (tickTimer) clearInterval(tickTimer);
  if (refreshTimer) clearInterval(refreshTimer);
});

async function load() {
  try {
    orders.value = await fetchOrders(tab.value);
    startTicker();
    startAutoRefresh();
  } catch {
    orders.value = [];
  }
}

function switchTab(next: OrderListTab) {
  if (tab.value === next) return;
  tab.value = next;
  expandedId.value = "";
}

watch(tab, () => load());

onShow(() => {
  load();
  const focusId = consumeHomeOrderFocus();
  if (focusId) {
    tab.value = "active";
    flashOrderId.value = focusId;
    expandedId.value = focusId;
    setTimeout(() => {
      flashOrderId.value = "";
    }, 1200);
  }
});

function toggleExpand(id: string) {
  expandedId.value = expandedId.value === id ? "" : id;
}

function copyOrderNo(no: string) {
  uni.setClipboardData({
    data: no,
    success: () => uni.showToast({ title: "已复制订单号", icon: "success" }),
  });
}

function openCancel(item: MyOrderItem) {
  cancelTarget.value = item;
  cancelVisible.value = true;
}

async function onCancelConfirm(reason: string) {
  const item = cancelTarget.value;
  if (!item) return;
  await cancelOrder(item.id, reason);
  cancelVisible.value = false;
  uni.showToast({ title: "订单已取消", icon: "success" });
  load();
}

function openRefundApply(item: MyOrderItem) {
  refundTarget.value = item;
  refundMode.value = "apply";
  refundSteps.value = [
    { key: "apply", label: "提交申请", done: false, current: true },
    { key: "audit", label: "平台审核", done: false },
    { key: "done", label: "退款到账", done: false },
  ];
  refundVisible.value = true;
}

function openRefundView(item: MyOrderItem) {
  refundTarget.value = item;
  refundMode.value = "view";
  refundSteps.value = item.refundSteps ?? [];
  refundVisible.value = true;
}

async function onRefundConfirm() {
  const item = refundTarget.value;
  if (!item) return;
  await applyRefund(item.id);
  refundVisible.value = false;
  uni.showToast({ title: "退款申请已提交", icon: "success" });
  load();
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

function onPay(item: MyOrderItem) {
  if (item.status !== "pending_pay") return;
  payTarget.value = item;
  checkoutSummary.value = buildCheckoutSummary(item);
  checkoutVisible.value = true;
}

function onCheckoutSuccess(payload: { orderId: string; orderNo: string }) {
  markOrderPaid(payload.orderId);
  checkoutVisible.value = false;
  checkoutSummary.value = null;
  flashOrderId.value = payload.orderId;
  load().then(() => {
    setTimeout(() => {
      flashOrderId.value = "";
    }, 900);
  });
}

function onCheckoutClose() {
  checkoutVisible.value = false;
  checkoutSummary.value = null;
  load();
}

function onEnterRoomAfterPay() {
  const item = payTarget.value;
  checkoutVisible.value = false;
  if (item) {
    onEnterRoom({ ...item, status: "in_progress", canEnterRoom: true });
  }
}

function onEnterRoom(order: MyOrderItem) {
  navigateToTrainingRoom({
    id: order.id,
    sceneTag: order.sceneTag,
    expertName: order.expertName,
    expertTitle: order.expertTitle,
    orderNo: order.orderNo,
    sceneId: order.sceneId,
  }).catch(() => {});
}

function onReview(order: MyOrderItem) {
  setPendingReview({
    orderId: order.id,
    orderNo: order.orderNo,
    expertId: order.expertId || "c_default",
    expertName: order.expertName,
    expertTitle: order.expertTitle,
    sceneTag: order.sceneTag,
  });
  uni.navigateTo({ url: "/pages/post-training-review/post-training-review" });
}

function openComplaint(order: MyOrderItem) {
  uni.navigateTo({
    url: `/pages/complaint/complaint?orderId=${order.id}&orderNo=${encodeURIComponent(order.orderNo)}`,
  });
}

function onViewReport(order: MyOrderItem) {
  uni.navigateTo({ url: `/pages/report-detail/report-detail?orderId=${order.id}` });
}

function onBookAgain(_item: MyOrderItem) {
  uni.navigateTo({ url: "/pages/coach-hall/coach-hall?sceneId=job-tech-deep" });
}
</script>

<style scoped>
.mo {
  min-height: 100vh;
  background: #f0f2f5;
  box-sizing: border-box;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.mo-nav {
  background: #ffffff;
  border-bottom: 1rpx solid #e8eaed;
}
.mo-nav-inner {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 28rpx 24rpx;
  min-height: 88rpx;
  gap: 20rpx;
}
.mo-nav-title {
  flex: 1;
  font-size: 40rpx;
  font-weight: 800;
  color: #111827;
}
.mo-seg {
  position: relative;
  flex-shrink: 0;
  padding: 4rpx;
  border-radius: 999rpx;
  background: #e5e7eb;
  min-width: 280rpx;
}
.mo-seg-thumb {
  position: absolute;
  left: 4rpx;
  top: 4rpx;
  bottom: 4rpx;
  width: calc(50% - 8rpx);
  border-radius: 999rpx;
  background: #fff;
  transition: left 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  z-index: 0;
}
.mo-seg-thumb--right {
  left: calc(50%);
}
.mo-seg-row {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
}
.mo-seg-item {
  flex: 1;
  padding: 14rpx;
  text-align: center;
}
.mo-seg-txt {
  font-size: 24rpx;
  font-weight: 600;
  color: #6b7280;
}
.mo-seg-txt--on {
  color: #2563eb;
}

.mo-body {
  padding: 24rpx;
  max-width: 1200px;
  margin: 0 auto;
  box-sizing: border-box;
}
.mo-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.mo-list--refresh {
  animation: moListRefresh 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes moListRefresh {
  from {
    opacity: 0.55;
    transform: translateY(12rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.mo-card--flash {
  animation: moCardFlash 0.85s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes moCardFlash {
  0% {
    transform: scale(1);
    box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.06);
  }
  35% {
    transform: scale(1.015);
    box-shadow: 0 12rpx 36rpx rgba(37, 99, 235, 0.22);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4rpx 24rpx rgba(37, 99, 235, 0.12);
  }
}

.mo-list--enter {
  animation: moFade 0.35s ease;
}
@keyframes moFade {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.mo-card {
  position: relative;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.06);
  overflow: visible;
  box-sizing: border-box;
}
.mo-ribbon {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  z-index: 4;
  padding: 8rpx 20rpx;
  border-radius: 6rpx;
  transform: rotate(6deg);
}
.mo-ribbon-txt {
  font-size: 20rpx;
  font-weight: 800;
  color: #fff;
}

.mo-countdown {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: #fffbeb;
  border-bottom: 1rpx solid #fde68a;
  border-radius: 20rpx 20rpx 0 0;
}
.mo-countdown-ico {
  font-size: 24rpx;
}
.mo-countdown-txt {
  font-size: 24rpx;
  font-weight: 700;
  color: #b45309;
}

.mo-card-inner {
  display: flex;
  flex-direction: row;
  padding: 28rpx 24rpx;
  gap: 20rpx;
  flex-wrap: wrap;
}
.mo-card-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: row;
  gap: 20rpx;
}
.mo-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  flex-shrink: 0;
}
.mo-avatar--live {
  background: linear-gradient(145deg, #eff6ff, #dbeafe);
}
.mo-avatar--muted {
  opacity: 0.7;
}
.mo-avatar-letter {
  font-size: 40rpx;
  font-weight: 800;
  color: #2563eb;
}
.mo-info {
  flex: 1;
  min-width: 0;
  padding-right: 80rpx;
}
.mo-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 10rpx;
}
.mo-status-tag {
  font-size: 20rpx;
  font-weight: 700;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.mo-cat-tag {
  font-size: 22rpx;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.mo-orderno-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 8rpx;
}
.mo-orderno {
  font-size: 22rpx;
  color: #9ca3af;
}
/* #ifdef H5 */
.mo-orderno-wrap:hover .mo-orderno {
  color: #2563eb;
}
.mo-copy-tip {
  position: absolute;
  left: 0;
  top: -36rpx;
  padding: 6rpx 12rpx;
  background: #0f172a;
  border-radius: 8rpx;
  white-space: nowrap;
}
.mo-copy-tip text {
  font-size: 20rpx;
  color: #fff;
}
/* #endif */

.mo-expert-line {
  font-size: 28rpx;
  margin-bottom: 8rpx;
}
.mo-expert-name {
  font-weight: 800;
  color: #111827;
}
.mo-expert-slash,
.mo-expert-title {
  color: #6b7280;
}
.mo-time-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  font-size: 26rpx;
  color: #6b7280;
}
.mo-time-txt--hl,
.mo-today {
  color: #1d4ed8;
  font-weight: 700;
}
.mo-amount {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.mo-card-right {
  flex-shrink: 0;
  width: 220rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  justify-content: center;
}
.mo-btn {
  padding: 16rpx;
  border-radius: 12rpx;
  text-align: center;
  transition: transform 0.25s ease;
}
.mo-btn-primary {
  background: linear-gradient(180deg, #3b82f6, #2563eb);
}
.mo-btn-txt {
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
}
.mo-btn-outline {
  border: 2rpx solid #2563eb;
}
.mo-btn-txt-outline {
  font-size: 24rpx;
  font-weight: 600;
  color: #2563eb;
}
.mo-btn-gray {
  background: #f3f4f6;
}
.mo-btn-txt-gray {
  font-size: 24rpx;
  color: #4b5563;
  font-weight: 600;
}
.mo-link {
  text-align: center;
  font-size: 22rpx;
  color: #2563eb;
  font-weight: 600;
}
.mo-hint {
  display: block;
  text-align: center;
  font-size: 22rpx;
  color: #94a3b8;
  line-height: 1.5;
  padding: 0 8rpx;
}

.mo-refund-bar {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 16rpx 24rpx 20rpx;
  border-top: 1rpx solid #f1f5f9;
}
.mo-refund-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  font-size: 20rpx;
  color: #94a3b8;
}
.mo-refund-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #e2e8f0;
  margin-bottom: 8rpx;
}
.mo-refund-step--done .mo-refund-dot {
  background: #22c55e;
}
.mo-refund-step--cur .mo-refund-dot {
  background: #f59e0b;
}
.mo-refund-step--cur {
  color: #b45309;
  font-weight: 700;
}
.mo-refund-line {
  position: absolute;
  top: 8rpx;
  left: 55%;
  width: 90%;
  height: 2rpx;
  background: #e2e8f0;
}

.mo-timeline-wrap {
  padding: 0 24rpx 24rpx;
  border-top: 1rpx dashed #e2e8f0;
  margin-top: 8rpx;
}
.mo-timeline-title {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #64748b;
  margin: 16rpx 0 8rpx;
}
.mo-complaint-link {
  display: block;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f1f5f9;
  font-size: 22rpx;
  color: #94a3b8;
  text-align: center;
  font-weight: 500;
}

.mo-empty {
  padding: 80rpx 0;
  text-align: center;
  color: #9ca3af;
  font-size: 28rpx;
}

@media (max-width: 720px) {
  .mo-card-inner {
    flex-direction: column;
  }
  .mo-info {
    padding-right: 0;
  }
  .mo-card-right {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }
  .mo-card-right .mo-btn {
    flex: 1;
    min-width: 140rpx;
  }
}
</style>
