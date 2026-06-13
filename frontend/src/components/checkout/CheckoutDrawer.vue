<template>
  <view v-if="visible" class="cd-mask" @tap="onMaskTap">
    <view
      class="cd-drawer"
      :class="{ 'cd-drawer--open': drawerOpen }"
      @tap.stop
    >
      <view class="cd-head">
        <text class="cd-title">确认订单</text>
        <text class="cd-close" @tap="close">×</text>
      </view>

      <scroll-view scroll-y class="cd-body">
        <!-- 订单摘要 -->
        <view v-if="summary" class="cd-summary">
          <view class="cd-summary-avatar">
            <text>{{ summary.expertName.slice(0, 1) }}</text>
          </view>
          <view class="cd-summary-info">
            <text class="cd-summary-name">{{ summary.expertName }}</text>
            <text class="cd-summary-sub">{{ summary.expertTitle }}</text>
            <text class="cd-summary-scene">{{ summary.sceneTag }}</text>
            <text class="cd-summary-time">
              {{ summary.dateLabel }} · {{ summary.timeRange }}
            </text>
            <text v-if="summary.existingOrderNo" class="cd-summary-order">
              订单号 {{ summary.existingOrderNo }}
            </text>
          </view>
        </view>

        <!-- 优惠券 -->
        <view class="cd-section">
          <view class="cd-coupon-head" @tap="couponExpanded = !couponExpanded">
            <text class="cd-section-label">优惠券 / 体验券</text>
            <view class="cd-coupon-pick">
              <text v-if="selectedCoupon" class="cd-coupon-pick-on">
                -¥{{ selectedCoupon.discountAmount }}
              </text>
              <text v-else class="cd-coupon-pick-off">{{ coupons.length }} 张可用</text>
              <text class="cd-chev">{{ couponExpanded ? "▴" : "▾" }}</text>
            </view>
          </view>
          <view v-if="couponExpanded" class="cd-coupon-list">
            <view
              class="cd-coupon-card"
              :class="{ 'cd-coupon-card--on': !selectedCouponId }"
              @tap="selectCoupon('')"
            >
              <text class="cd-coupon-title">不使用优惠券</text>
            </view>
            <view
              v-for="c in coupons"
              :key="c.id"
              class="cd-coupon-card"
              :class="{ 'cd-coupon-card--on': selectedCouponId === c.id }"
              @tap="selectCoupon(c.id)"
            >
              <view class="cd-coupon-left">
                <text class="cd-coupon-amt">¥{{ c.discountAmount }}</text>
                <text class="cd-coupon-title">{{ c.title }}</text>
                <text class="cd-coupon-desc">{{ c.desc }} · {{ c.expireLabel }}</text>
              </view>
              <view v-if="selectedCouponId === c.id" class="cd-coupon-check">✓</view>
            </view>
          </view>
        </view>

        <!-- 金额 -->
        <view class="cd-price-box">
          <view class="cd-price-row">
            <text>课程费用</text>
            <text>¥{{ totals.originalAmount.toFixed(2) }}</text>
          </view>
          <view v-if="totals.discountAmount > 0" class="cd-price-row cd-price-row--disc">
            <text>优惠抵扣</text>
            <text>-¥{{ totals.discountAmount.toFixed(2) }}</text>
          </view>
          <view class="cd-price-total">
            <text>应付</text>
            <NumberTicker :value="totals.payableAmount" />
          </view>
        </view>

        <!-- 支付方式 -->
        <view class="cd-section">
          <text class="cd-section-label">支付方式</text>
          <view class="cd-pay-row">
            <view
              v-for="m in paymentMethods"
              :key="m.id"
              class="cd-pay-item"
              :class="{ 'cd-pay-item--on': paymentMethod === m.id }"
              @tap="paymentMethod = m.id"
            >
              <view class="cd-pay-icon">
                <text>{{ m.icon }}</text>
              </view>
              <text class="cd-pay-label">{{ m.label }}</text>
            </view>
          </view>
        </view>

        <!-- 支付失败 -->
        <view v-if="payError" class="cd-fail">
          <text class="cd-fail-title">支付未完成</text>
          <text class="cd-fail-msg">{{ payError }}</text>
          <view class="cd-fail-actions">
            <view class="cd-fail-btn" @tap="retryPay">
              <text>重新支付</text>
            </view>
            <view class="cd-fail-btn cd-fail-btn--ghost" @tap="contactSupport">
              <text>联系客服</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <view class="cd-foot">
        <view
          class="cd-pay-btn"
          :class="{ 'cd-pay-btn--loading': paying, 'cd-pay-btn--disabled': paying || payDisabled }"
          @tap="onConfirmPay"
        >
          <view v-if="paying" class="cd-pay-loading">
            <view class="cd-pay-ring" />
            <text>支付中…</text>
          </view>
          <text v-else>确认支付 ¥{{ totals.payableAmount.toFixed(2) }}</text>
        </view>
      </view>
    </view>

    <PaymentSuccessOverlay
      :visible="successVisible"
      :order-no="successOrderNo"
      @enter-room="onEnterRoom"
      @view-orders="onViewOrders"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import NumberTicker from "@/components/checkout/NumberTicker.vue";
import PaymentSuccessOverlay from "@/components/checkout/PaymentSuccessOverlay.vue";
import {
  fetchAvailableCoupons,
  calculateTotal,
  prepayOrder,
} from "@/api/modules/checkout.js";
import { PAYMENT_METHODS } from "@/types/checkout";
import type {
  CheckoutSummary,
  CouponItem,
  PaymentMethodId,
} from "@/types/checkout";
import { debounce } from "@/utils/common/debounce";

const props = defineProps<{
  visible: boolean;
  summary: CheckoutSummary | null;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "success", payload: { orderId: string; orderNo: string }): void;
  (e: "enter-room"): void;
}>();

const drawerOpen = ref(false);
const coupons = ref<CouponItem[]>([]);
const selectedCouponId = ref("");
const couponExpanded = ref(false);
const paymentMethod = ref<PaymentMethodId>("wechat");
const paying = ref(false);
const payDisabled = ref(false);
const payError = ref("");
const successVisible = ref(false);
const successOrderNo = ref("");

const paymentMethods = PAYMENT_METHODS;

const selectedCoupon = computed(() =>
  coupons.value.find((c) => c.id === selectedCouponId.value),
);

const totals = computed(() =>
  calculateTotal({
    originalAmount: props.summary?.originalAmount ?? 0,
    couponId: selectedCouponId.value || undefined,
    coupons: coupons.value,
  }),
);

watch(
  () => props.visible,
  async (v) => {
    if (v) {
      payError.value = "";
      successVisible.value = false;
      paying.value = false;
      payDisabled.value = false;
      selectedCouponId.value = "";
      couponExpanded.value = false;
      setTimeout(() => {
        drawerOpen.value = true;
      }, 16);
      if (props.summary) {
        coupons.value = await fetchAvailableCoupons(props.summary.originalAmount);
      }
    } else {
      drawerOpen.value = false;
    }
  },
);

function selectCoupon(id: string) {
  selectedCouponId.value = id;
}

function close() {
  if (paying.value) return;
  drawerOpen.value = false;
  setTimeout(() => emit("close"), 280);
}

function onMaskTap() {
  if (!paying.value && !successVisible.value) close();
}

const doPay = debounce(async () => {
  if (!props.summary || paying.value || payDisabled.value) return;
  paying.value = true;
  payError.value = "";
  try {
    const res = await prepayOrder({
      expertId: props.summary.expertId,
      date: props.summary.dateIso,
      slotId: props.summary.slotId,
      couponId: selectedCouponId.value || undefined,
      paymentMethod: paymentMethod.value,
      amount: totals.value.payableAmount,
      originalAmount: props.summary.originalAmount,
      sceneCode: props.summary.sceneCode,
      existingOrderId: props.summary.existingOrderId,
      existingOrderNo: props.summary.existingOrderNo,
    });
    successOrderNo.value = res.orderNo;
    successVisible.value = true;
    payDisabled.value = true;
    emit("success", { orderId: res.orderId, orderNo: res.orderNo });
  } catch (e) {
    payError.value = e instanceof Error ? e.message : "支付失败，请重试";
  } finally {
    paying.value = false;
  }
}, 500);

function onConfirmPay() {
  doPay();
}

function retryPay() {
  payError.value = "";
  payDisabled.value = false;
  onConfirmPay();
}

function contactSupport() {
  uni.showToast({ title: "客服：400-000-0000（mock）", icon: "none", duration: 2500 });
}

function onEnterRoom() {
  successVisible.value = false;
  close();
  emit("enter-room");
}

function onViewOrders() {
  successVisible.value = false;
  close();
  uni.switchTab({ url: "/pages/my-orders/my-orders" });
}
</script>

<style scoped>
.cd-mask {
  position: fixed;
  inset: 0;
  z-index: 2500;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
}
.cd-drawer {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 92%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(24px);
  box-shadow: -12rpx 0 48rpx rgba(15, 23, 42, 0.12);
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.38s cubic-bezier(0.22, 1, 0.36, 1);
  box-sizing: border-box;
}
.cd-drawer--open {
  transform: translateX(0);
}

.cd-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 28rpx 16rpx;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.8);
}
.cd-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.cd-close {
  font-size: 48rpx;
  color: #94a3b8;
  line-height: 1;
  padding: 0 8rpx;
}

.cd-body {
  flex: 1;
  padding: 20rpx 28rpx;
  box-sizing: border-box;
}

.cd-summary {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: rgba(248, 250, 252, 0.9);
  border: 1rpx solid #e2e8f0;
  margin-bottom: 24rpx;
}
.cd-summary-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  background: linear-gradient(145deg, #dbeafe, #eff6ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 800;
  color: #2563eb;
  flex-shrink: 0;
}
.cd-summary-info {
  flex: 1;
  min-width: 0;
}
.cd-summary-name {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.cd-summary-sub {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin-top: 4rpx;
}
.cd-summary-scene {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #334155;
  margin-top: 10rpx;
}
.cd-summary-time {
  display: block;
  font-size: 24rpx;
  color: #2563eb;
  font-weight: 700;
  margin-top: 6rpx;
}
.cd-summary-order {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 6rpx;
}

.cd-section {
  margin-bottom: 24rpx;
}
.cd-section-label {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #334155;
  margin-bottom: 14rpx;
}
.cd-coupon-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}
.cd-coupon-pick {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}
.cd-coupon-pick-on {
  font-size: 26rpx;
  font-weight: 800;
  color: #dc2626;
}
.cd-coupon-pick-off {
  font-size: 24rpx;
  color: #64748b;
}
.cd-chev {
  color: #94a3b8;
  font-size: 22rpx;
}
.cd-coupon-list {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.cd-coupon-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 20rpx;
  border-radius: 14rpx;
  border: 2rpx solid #e2e8f0;
  background: #fff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.cd-coupon-card--on {
  border-color: #2563eb;
  box-shadow: 0 0 0 3rpx rgba(37, 99, 235, 0.12);
}
.cd-coupon-left {
  flex: 1;
}
.cd-coupon-amt {
  font-size: 32rpx;
  font-weight: 800;
  color: #dc2626;
}
.cd-coupon-title {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
  margin-top: 4rpx;
}
.cd-coupon-desc {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 4rpx;
}
.cd-coupon-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 800;
}

.cd-price-box {
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
  margin-bottom: 24rpx;
}
.cd-price-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 10rpx;
}
.cd-price-row--disc text:last-child {
  color: #dc2626;
  font-weight: 700;
}
.cd-price-total {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  margin-top: 8rpx;
  border-top: 1rpx dashed #e2e8f0;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.cd-pay-row {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}
.cd-pay-item {
  flex: 1;
  padding: 20rpx 12rpx;
  border-radius: 16rpx;
  border: 2rpx solid #e2e8f0;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  transition: all 0.25s ease;
}
.cd-pay-item--on {
  border-color: #2563eb;
  box-shadow:
    0 0 0 1rpx rgba(37, 99, 235, 0.2),
    0 0 20rpx rgba(37, 99, 235, 0.25);
  animation: cdPayGlow 1.8s ease-in-out infinite;
}
@keyframes cdPayGlow {
  0%,
  100% {
    box-shadow: 0 0 0 1rpx rgba(37, 99, 235, 0.2), 0 0 12rpx rgba(37, 99, 235, 0.15);
  }
  50% {
    box-shadow: 0 0 0 2rpx rgba(37, 99, 235, 0.35), 0 0 24rpx rgba(37, 99, 235, 0.3);
  }
}
.cd-pay-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
  color: #334155;
}
.cd-pay-item--on .cd-pay-icon {
  background: #eff6ff;
  color: #2563eb;
}
.cd-pay-label {
  font-size: 22rpx;
  font-weight: 600;
  color: #475569;
}

.cd-fail {
  margin-top: 16rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #fef2f2, #fff1f2);
  border: 1rpx solid #fecaca;
}
.cd-fail-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #b91c1c;
}
.cd-fail-msg {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #dc2626;
}
.cd-fail-actions {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  margin-top: 16rpx;
}
.cd-fail-btn {
  flex: 1;
  padding: 14rpx;
  border-radius: 12rpx;
  background: #dc2626;
  text-align: center;
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
}
.cd-fail-btn--ghost {
  background: #fff;
  border: 1rpx solid #fecaca;
  color: #b91c1c;
}

.cd-foot {
  padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #e2e8f0;
  background: rgba(255, 255, 255, 0.95);
}
.cd-pay-btn {
  padding: 24rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  text-align: center;
  font-size: 30rpx;
  font-weight: 800;
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.35);
}
.cd-pay-btn--disabled {
  opacity: 0.65;
  pointer-events: none;
}
.cd-pay-btn--loading {
  pointer-events: none;
}
.cd-pay-loading {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}
.cd-pay-ring {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  animation: cdSpin 0.8s linear infinite;
}
@keyframes cdSpin {
  to {
    transform: rotate(360deg);
  }
}
</style>
