<template>
  <view class="eb min-h-screen bg-gradient-to-b from-slate-50 via-white to-slate-50 font-inter">
    <view v-if="loading" class="eb-loading mx-auto max-w-6xl px-4 py-20 text-center text-slate-500">
      <text>加载专家信息…</text>
    </view>

    <view v-else-if="!expert" class="eb-state">
      <text class="eb-state-txt">未找到该专家，请返回重试</text>
    </view>

    <view v-else class="eb-wrap">
      <view class="eb-main">
        <view class="eb-headline">
          <text class="eb-name">{{ expert.name }}</text>
          <text class="eb-badge">{{ expert.jobTitle }}</text>
        </view>
        <view class="eb-meta">
          <text class="eb-meta-star">★ {{ expert.rating.toFixed(1) }}</text>
          <text class="eb-meta-item">{{ expert.orderCount }} 单</text>
          <text class="eb-meta-item">{{ expert.sessionMinutes }} 分钟/节</text>
        </view>

        <view class="eb-card eb-intro">
          <view class="eb-section-title">
            <text class="eb-section-icon">📋</text>
            <text class="eb-section-title-txt">专家介绍</text>
          </view>
          <view v-for="(para, idx) in introParagraphs" :key="`intro-${idx}`" class="eb-intro-para">
            <text>{{ para }}</text>
          </view>
        </view>

        <view class="eb-block">
          <text class="eb-block-title">核心擅长领域</text>
          <view class="eb-domain-grid">
            <view v-for="d in expert.domains" :key="d.id" class="eb-domain-card">
              <text class="eb-domain-title">{{ d.title }}</text>
              <text class="eb-domain-desc">{{ d.summary }}</text>
            </view>
          </view>
        </view>

        <view class="eb-block">
          <text class="eb-block-title">学员评价</text>
          <view v-if="reviewsLoading" class="eb-reviews-loading">
            <text>加载评价…</text>
          </view>
          <view v-else-if="!coachReviews.length" class="eb-reviews-empty">
            <text>暂无学员评价，完成陪练后可成为第一位评价者</text>
          </view>
          <view v-else class="eb-reviews">
            <view v-for="r in coachReviews" :key="r.id" class="eb-review">
              <view class="eb-review-avatar">
                <text class="eb-review-avatar-txt">{{ r.avatarLetter }}</text>
              </view>
              <view class="eb-review-body">
                <view class="eb-review-top">
                  <view class="eb-review-left">
                    <text class="eb-review-name">{{ r.displayName }}</text>
                    <text class="eb-review-stars">{{ starText(r.rating) }}</text>
                  </view>
                  <text class="eb-review-date">{{ r.date }}</text>
                </view>
                <text class="eb-review-content">{{ r.content }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="eb-side">
        <view class="eb-side-inner">
          <view class="eb-sidebar">
            <text class="eb-price-label">单节价格</text>
            <view class="eb-price-row">
              <text class="eb-price-num">¥{{ expert.price }}</text>
              <text class="eb-price-unit">/{{ expert.sessionMinutes }} 分钟</text>
            </view>

            <text class="eb-sidebar-label">选择日期</text>
            <scroll-view scroll-x :show-scrollbar="false" class="eb-date-scroll">
              <view class="eb-date-row">
                <view
                  v-for="d in dateOptions"
                  :key="d.iso"
                  class="eb-date-chip"
                  :class="{ 'eb-date-chip--active': selectedDate === d.iso }"
                  @tap="onPickDate(d.iso)"
                >
                  <text class="eb-date-chip-day" :class="{ 'eb-date-chip-day--on': selectedDate === d.iso }">
                    {{ d.dayLabel }}
                  </text>
                  <text class="eb-date-chip-wk" :class="{ 'eb-date-chip-wk--on': selectedDate === d.iso }">
                    {{ d.weekLabel }}
                  </text>
                </view>
              </view>
            </scroll-view>

            <text class="eb-sidebar-label">可用时段</text>
            <view v-if="slotsLoading" class="eb-slots-loading">
              <text>加载时段…</text>
            </view>
            <view v-else class="eb-slots">
              <view
                v-for="s in slots"
                :key="s.id"
                class="eb-slot"
                :class="{
                  'eb-slot--booked': s.booked,
                  'eb-slot--active': !s.booked && selectedSlotId === s.id,
                }"
                @tap="onPickSlot(s)"
              >
                <text class="eb-slot-txt">{{ s.label }}</text>
              </view>
            </view>

            <view
              class="eb-submit"
              :class="{ 'eb-submit--disabled': !canSubmit }"
              @tap="onSubmit"
            >
              <text class="eb-submit-txt">立即预约</text>
            </view>
            <text v-if="!canSubmit" class="eb-submit-hint">请选择日期与可用时段</text>
          </view>
        </view>
      </view>
    </view>

    <CheckoutDrawer
      :visible="checkoutVisible"
      :summary="checkoutSummary"
      @close="checkoutVisible = false"
      @success="onCheckoutSuccess"
      @enter-room="onEnterRoomAfterPay"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from "vue";
import { fetchExpert, fetchExpertSlots } from "@/api/modules/expert.js";
import { fetchCoachReviews } from "@/api/modules/review.js";
import CheckoutDrawer from "@/components/checkout/CheckoutDrawer.vue";
import type { ExpertDetail, ExpertReview, ExpertTimeSlot } from "@/types/booking/expert";
import type { CoachReviewSnippet } from "@/types/review";
import type { CheckoutSummary } from "@/types/checkout";
import { useVerifyStore } from "@/store/verify";
import { useUserStore } from "@/store/user";
import { navigateToTrainingRoom } from "@/utils/training/enterRoom.js";

const verifyStore = useVerifyStore();
const userStore = useUserStore();

const props = defineProps<{
  expertId: string;
  /** 场景名称，用于收银台摘要 */
  sceneTag?: string;
  /** 场景编码，下单时写入 ctx_order.scene_id */
  sceneCode?: string;
}>();

const expert = ref<ExpertDetail | null>(null);
const coachReviews = ref<ExpertReview[]>([]);
const reviewsLoading = ref(false);
const loading = ref(true);
const selectedDate = ref("");
const slots = ref<ExpertTimeSlot[]>([]);
const slotsLoading = ref(false);
const selectedSlotId = ref("");
const checkoutVisible = ref(false);
const checkoutSummary = ref<CheckoutSummary | null>(null);
const lastPaidOrder = ref<{ orderId: string; orderNo: string } | null>(null);

const introParagraphs = computed(() => {
  const t = expert.value?.intro ?? "";
  return t.split(/\n\n+/).filter(Boolean);
});

const dateOptions = computed(() => {
  const out: { iso: string; dayLabel: string; weekLabel: string }[] = [];
  const wk = ["日", "一", "二", "三", "四", "五", "六"];
  const start = new Date();
  start.setHours(0, 0, 0, 0);
  for (let i = 0; i < 14; i += 1) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    const iso = formatIso(d);
    const day = d.getDate();
    out.push({
      iso,
      dayLabel: `${day}日`,
      weekLabel: `周${wk[d.getDay()]}`,
    });
  }
  return out;
});

const canSubmit = computed(
  () => Boolean(selectedDate.value && selectedSlotId.value && expert.value?.id),
);

function formatIso(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function starText(r: number): string {
  const n = Math.round(r);
  return "★".repeat(n) + "☆".repeat(5 - n);
}

function mapCoachReview(rv: CoachReviewSnippet): ExpertReview {
  const name = rv.userName || "学员";
  return {
    id: rv.id,
    displayName: name,
    rating: rv.score,
    date: rv.date,
    content: rv.content,
    avatarLetter: (name[0] || "学").toUpperCase(),
  };
}

async function loadCoachReviews(coachId: string) {
  reviewsLoading.value = true;
  try {
    const rows = await fetchCoachReviews(coachId);
    coachReviews.value = rows.map(mapCoachReview);
  } finally {
    reviewsLoading.value = false;
  }
}

async function loadExpert() {
  if (!props.expertId) {
    expert.value = null;
    coachReviews.value = [];
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const [detail] = await Promise.all([
      fetchExpert(props.expertId),
      loadCoachReviews(props.expertId),
    ]);
    expert.value = detail;
    if (!selectedDate.value && dateOptions.value.length) {
      selectedDate.value = dateOptions.value[0].iso;
    }
  } finally {
    loading.value = false;
  }
}

async function loadSlots() {
  if (!props.expertId || !selectedDate.value) return;
  slotsLoading.value = true;
  selectedSlotId.value = "";
  try {
    slots.value = await fetchExpertSlots(props.expertId, selectedDate.value);
  } finally {
    slotsLoading.value = false;
  }
}

function onPickDate(iso: string) {
  selectedDate.value = iso;
}

function onPickSlot(s: ExpertTimeSlot) {
  if (s.booked) return;
  selectedSlotId.value = selectedSlotId.value === s.id ? "" : s.id;
}

onMounted(() => {
  verifyStore.hydrateFromStorage();
  verifyStore.loadStatus();
});

function formatDateLabel(iso: string) {
  const [y, m, d] = iso.split("-").map(Number);
  return `${y}年${m}月${d}日`;
}

function onSubmit() {
  if (!canSubmit.value || !expert.value) return;
  // 游客未登录：提示并跳转登录页
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: "请先登录", icon: "none", duration: 2000 });
    uni.navigateTo({ url: "/pages/auth/login" });
    return;
  }
  if (!verifyStore.isVerified) {
    uni.showModal({
      title: "需要实名认证",
      content: verifyStore.isPending
        ? "您的认证资料审核中，通过后即可预约专家"
        : "预约专家陪练前请先完成身份认证",
      confirmText: verifyStore.isPending ? "查看进度" : "去认证",
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: "/pages/identity-verify/identity-verify" });
        }
      },
    });
    return;
  }

  const slot = slots.value.find((s) => s.id === selectedSlotId.value);
  checkoutSummary.value = {
    expertId: expert.value.id,
    expertName: expert.value.name,
    expertTitle: expert.value.jobTitle,
    expertAvatarUrl: expert.value.avatarUrl,
    sceneTag: props.sceneTag ?? "专项训练",
    sceneCode: props.sceneCode ?? "INTERVIEW",
    dateIso: selectedDate.value,
    dateLabel: formatDateLabel(selectedDate.value),
    timeRange: slot?.label ?? "",
    slotId: selectedSlotId.value,
    originalAmount: expert.value.price,
    sessionMinutes: expert.value.sessionMinutes,
  };
  checkoutVisible.value = true;
}

function onCheckoutSuccess(payload: { orderId: string; orderNo: string }) {
  checkoutVisible.value = false;
  lastPaidOrder.value = payload;
  uni.showToast({ title: "预约成功", icon: "success", duration: 2000 });
  setTimeout(() => {
    uni.switchTab({ url: "/pages/my-orders/my-orders" });
  }, 600);
}

function onEnterRoomAfterPay() {
  const paid = lastPaidOrder.value;
  const summary = checkoutSummary.value;
  if (!paid?.orderId && !summary) return;
  navigateToTrainingRoom({
    id: paid?.orderId ?? summary?.existingOrderId ?? "",
    sceneTag: summary?.sceneTag ?? "专项训练",
    expertName: summary?.expertName ?? expert.value?.name,
    expertTitle: summary?.expertTitle ?? expert.value?.jobTitle,
    orderNo: paid?.orderNo ?? summary?.existingOrderNo,
  }).catch(() => {});
}

watch(
  () => props.expertId,
  () => {
    selectedDate.value = "";
    selectedSlotId.value = "";
    slots.value = [];
    coachReviews.value = [];
    loadExpert();
  },
  { immediate: true },
);

watch(selectedDate, () => {
  if (props.expertId && selectedDate.value) {
    loadSlots();
  }
});
</script>

<style scoped>
.eb {
  min-height: 100vh;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 40%, #f1f5f9 100%);
  font-family: Inter, "PingFang SC", ui-sans-serif, system-ui, sans-serif;
}

.eb-state {
  padding: 120rpx 32rpx;
  text-align: center;
}
.eb-state-txt {
  font-size: 28rpx;
  color: #64748b;
}

.eb-wrap {
  max-width: 1152px;
  margin: 0 auto;
  padding: 24rpx 28rpx 80rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

/* 大屏 7 : 3 */
@media (min-width: 1024px) {
  .eb-wrap {
    flex-direction: row;
    align-items: flex-start;
    gap: 40px;
    padding: 40px 32px 96px;
  }
  .eb-main {
    flex: 0 0 70%;
    max-width: 70%;
    min-width: 0;
  }
  .eb-side {
    flex: 0 0 30%;
    max-width: 30%;
    min-width: 0;
  }
  .eb-side-inner {
    position: sticky;
    top: 96px;
  }
}

.eb-headline {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.eb-name {
  font-size: 44rpx;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}
@media (min-width: 1024px) {
  .eb-name {
    font-size: 32px;
  }
}
.eb-badge {
  font-size: 22rpx;
  font-weight: 600;
  color: #1d4ed8;
  background: #dbeafe;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
}

.eb-meta {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 40rpx;
}
.eb-meta-star {
  font-size: 26rpx;
  color: #f59e0b;
  font-weight: 600;
}
.eb-meta-item {
  font-size: 26rpx;
  color: #64748b;
}

.eb-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1rpx solid #e2e8f0;
  border-radius: 24rpx;
  padding: 36rpx;
  margin-bottom: 48rpx;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.06);
}
@media (min-width: 1024px) {
  .eb-card {
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 32px;
  }
}

.eb-section-title {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}
.eb-section-icon {
  font-size: 36rpx;
}
.eb-section-title-txt {
  font-size: 34rpx;
  font-weight: 700;
  color: #0f172a;
}

.eb-intro-para {
  margin-bottom: 28rpx;
}
.eb-intro-para:last-child {
  margin-bottom: 0;
}
.eb-intro-para text {
  font-size: 32rpx;
  line-height: 1.85;
  color: #475569;
}
@media (min-width: 1024px) {
  .eb-intro-para text {
    font-size: 17px;
  }
}

.eb-block {
  margin-bottom: 48rpx;
}
.eb-block-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 24rpx;
}

.eb-domain-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24rpx;
}
@media (min-width: 640px) {
  .eb-domain-grid {
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
}

.eb-domain-card {
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.04);
}
.eb-domain-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12rpx;
}
.eb-domain-desc {
  font-size: 26rpx;
  line-height: 1.65;
  color: #64748b;
}

.eb-reviews-loading,
.eb-reviews-empty {
  padding: 32rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #94a3b8;
}

.eb-reviews {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.eb-review {
  display: flex;
  flex-direction: row;
  gap: 24rpx;
  padding: 28rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.04);
}
.eb-review-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: linear-gradient(145deg, #dbeafe, #f1f5f9);
  display: flex;
  align-items: center;
  justify-content: center;
}
.eb-review-avatar-txt {
  font-size: 28rpx;
  font-weight: 700;
  color: #1d4ed8;
}
.eb-review-body {
  flex: 1;
  min-width: 0;
}
.eb-review-top {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}
.eb-review-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}
.eb-review-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}
.eb-review-stars {
  font-size: 22rpx;
  color: #f59e0b;
}
.eb-review-date {
  font-size: 22rpx;
  color: #94a3b8;
}
.eb-review-content {
  font-size: 26rpx;
  line-height: 1.65;
  color: #64748b;
}

.eb-sidebar {
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 12rpx 40rpx rgba(15, 23, 42, 0.08);
}
@media (min-width: 1024px) {
  .eb-sidebar {
    border-radius: 16px;
    padding: 24px;
  }
}

.eb-price-label {
  display: block;
  font-size: 22rpx;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 8rpx;
}
.eb-price-row {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: 36rpx;
}
.eb-price-num {
  font-size: 52rpx;
  font-weight: 800;
  color: #2563eb;
}
@media (min-width: 1024px) {
  .eb-price-num {
    font-size: 36px;
  }
}
.eb-price-unit {
  font-size: 26rpx;
  color: #64748b;
}

.eb-sidebar-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
  margin-bottom: 16rpx;
}

.eb-date-scroll {
  width: 100%;
  margin-bottom: 32rpx;
  white-space: nowrap;
}
.eb-date-row {
  display: inline-flex;
  flex-direction: row;
  gap: 16rpx;
  padding-bottom: 8rpx;
}
.eb-date-chip {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 120rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  background: #f8fafc;
}
.eb-date-chip--active {
  background: #2563eb;
  border-color: #2563eb;
  box-shadow: 0 8rpx 20rpx rgba(37, 99, 235, 0.28);
}
.eb-date-chip-day {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
.eb-date-chip-day--on {
  color: #ffffff;
}
.eb-date-chip-wk {
  font-size: 20rpx;
  color: #64748b;
  margin-top: 4rpx;
}
.eb-date-chip-wk--on {
  color: rgba(255, 255, 255, 0.9);
}

.eb-slots-loading {
  padding: 24rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #94a3b8;
  margin-bottom: 24rpx;
}

.eb-slots {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.eb-slot {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  border: 1rpx solid #e2e8f0;
  background: #ffffff;
}
.eb-slot-txt {
  font-size: 26rpx;
  font-weight: 500;
  color: #334155;
}
.eb-slot--active {
  background: #2563eb;
  border-color: #2563eb;
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.25);
}
.eb-slot--active .eb-slot-txt {
  color: #ffffff;
}
.eb-slot--booked {
  opacity: 0.45;
  background: #f1f5f9;
  border-color: #f1f5f9;
}
.eb-slot--booked .eb-slot-txt {
  color: #94a3b8;
}

.eb-submit {
  width: 100%;
  padding: 28rpx 0;
  border-radius: 16rpx;
  text-align: center;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 12rpx 28rpx rgba(37, 99, 235, 0.32);
}
.eb-submit--disabled {
  background: #cbd5e1;
  box-shadow: none;
  opacity: 0.75;
}
.eb-submit-txt {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
}
.eb-submit-hint {
  display: block;
  text-align: center;
  font-size: 22rpx;
  color: #94a3b8;
  margin-top: 16rpx;
}
</style>
