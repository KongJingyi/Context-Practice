<template>
  <view v-if="displayOrders.length" class="haor">
    <view class="haor-head" :class="{ 'haor-head--pulse': hoverAll || !!hoverRowId }">
      <view class="haor-head-left">
        <text class="haor-dot-icon">●</text>
        <text class="haor-title">{{ orders.length }} 笔订单待处理</text>
      </view>
      <view
        v-if="showViewAll"
        class="haor-all"
        :class="{ 'haor-all--hover': hoverAll }"
        @tap="emit('view-all')"
        @mouseenter="hoverAll = true"
        @mouseleave="hoverAll = false"
      >
        <text>全部</text>
        <text class="haor-all-arrow">›</text>
      </view>
    </view>

    <view class="haor-list">
      <view
        v-for="item in displayOrders"
        :key="item.id"
        class="haor-row"
        :class="{ 'haor-row--hover': hoverRowId === item.id }"
        @tap="emit('open', item)"
        @mouseenter="hoverRowId = item.id"
        @mouseleave="onRowLeave(item.id)"
      >
        <view class="haor-row-main">
          <text class="haor-status" :style="statusStyle(item.status)">
            {{ statusMeta(item.status).label }}
          </text>
          <text class="haor-scene">{{ item.sceneTag }}</text>
          <text v-if="item.status === 'pending_pay'" class="haor-countdown">
            {{ countdownText(item) }}
          </text>
        </view>
        <view class="haor-row-sub">
          <text class="haor-meta">{{ item.expertName }} · {{ timeBrief(item) }}</text>
        </view>
        <view
          class="haor-btn"
          :class="{ 'haor-btn--hover': hoverBtnId === item.id }"
          @tap.stop="emit('action', item)"
          @mouseenter.stop="hoverBtnId = item.id"
          @mouseleave.stop="hoverBtnId = ''"
        >
          <text>{{ actionLabel(item) }}</text>
        </view>
      </view>
    </view>

    <view
      v-if="orders.length > maxDisplay"
      class="haor-more"
      :class="{ 'haor-more--hover': hoverMore }"
      @tap="emit('view-all')"
      @mouseenter="hoverMore = true"
      @mouseleave="hoverMore = false"
    >
      <text>还有 {{ orders.length - maxDisplay }} 笔，点击查看</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from "vue";
import type { MyOrderItem, OrderStatus } from "@/types/orders";
import { ORDER_STATUS_MAP } from "@/types/orders";

const props = withDefaults(
  defineProps<{
    orders: MyOrderItem[];
    showViewAll?: boolean;
    maxDisplay?: number;
  }>(),
  {
    showViewAll: true,
    maxDisplay: 2,
  },
);

const emit = defineEmits<{
  (e: "view-all"): void;
  (e: "open", item: MyOrderItem): void;
  (e: "action", item: MyOrderItem): void;
}>();

const displayOrders = computed(() => props.orders.slice(0, props.maxDisplay));

const hoverRowId = ref("");
const hoverBtnId = ref("");
const hoverAll = ref(false);
const hoverMore = ref(false);

function onRowLeave(id: string) {
  if (hoverRowId.value === id) hoverRowId.value = "";
}

const countdownMap = ref<Record<string, string>>({});
let tickTimer: ReturnType<typeof setInterval> | null = null;

function statusMeta(status: OrderStatus) {
  return ORDER_STATUS_MAP[status];
}

function statusStyle(status: OrderStatus) {
  const m = statusMeta(status);
  return { color: m.color, background: m.bg };
}

function timeBrief(item: MyOrderItem) {
  if (item.isToday) return `今天 ${item.timeRange}`;
  return item.timeRange;
}

function actionLabel(item: MyOrderItem) {
  if (item.status === "pending_pay") return "支付";
  if (item.status === "in_progress" && item.canEnterRoom) return "训练";
  if (item.status === "pending_review") return "复盘";
  if (item.status === "refunding") return "退款";
  return "查看";
}

function updateCountdowns() {
  const map: Record<string, string> = {};
  const now = Date.now();
  for (const o of props.orders) {
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

watch(
  () => props.orders,
  () => startTicker(),
  { immediate: true, deep: true },
);

onUnmounted(() => {
  if (tickTimer) clearInterval(tickTimer);
});
</script>

<style scoped>
.haor {
  padding: 16rpx 18rpx;
  border-radius: 16rpx;
  background: #f8fbff;
  border: 1rpx solid #dbeafe;
  box-sizing: border-box;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}
.haor-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10rpx;
}
.haor-head-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  min-width: 0;
}
.haor-dot-icon {
  font-size: 16rpx;
  color: #2563eb;
  line-height: 1;
  transition: transform 0.25s ease, color 0.25s ease;
}
.haor-title {
  font-size: 22rpx;
  font-weight: 700;
  color: #1e40af;
}
.haor-all {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2rpx;
  flex-shrink: 0;
  border-radius: 8rpx;
  padding: 4rpx 6rpx;
  transition: background 0.2s ease;
}
.haor-all text {
  font-size: 22rpx;
  font-weight: 600;
  color: #2563eb;
  transition: color 0.2s ease;
}
.haor-all-arrow {
  font-size: 24rpx;
  line-height: 1;
  transition: transform 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}
.haor-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.haor-row {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  padding: 12rpx 96rpx 12rpx 12rpx;
  border-radius: 12rpx;
  background: #fff;
  border: 1rpx solid #e8eef5;
  box-sizing: border-box;
  transition:
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease,
    border-color 0.22s ease,
    background 0.22s ease;
}
.haor-row-main {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  min-width: 0;
}
.haor-status {
  flex-shrink: 0;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  font-size: 18rpx;
  font-weight: 700;
}
.haor-scene {
  flex: 1;
  min-width: 0;
  font-size: 24rpx;
  font-weight: 700;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.22s ease;
}
.haor-countdown {
  flex-shrink: 0;
  font-size: 18rpx;
  font-weight: 700;
  color: #b45309;
  font-variant-numeric: tabular-nums;
}
.haor-row-sub {
  min-width: 0;
}
.haor-meta {
  font-size: 20rpx;
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.22s ease;
}
.haor-btn {
  position: absolute;
  top: 50%;
  right: 12rpx;
  transform: translateY(-50%);
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #2563eb;
  transition:
    transform 0.2s cubic-bezier(0.22, 1, 0.36, 1),
    background 0.2s ease,
    box-shadow 0.2s ease;
}
.haor-btn text {
  font-size: 20rpx;
  font-weight: 700;
  color: #fff;
}
.haor-more {
  margin-top: 8rpx;
  text-align: center;
  border-radius: 8rpx;
  padding: 4rpx 0;
  transition: background 0.2s ease;
}
.haor-more text {
  font-size: 20rpx;
  color: #64748b;
  transition: color 0.2s ease;
}

/* #ifdef H5 */
.haor-row {
  cursor: pointer;
}
.haor-all,
.haor-more,
.haor-btn {
  cursor: pointer;
}
.haor-row--hover {
  transform: translateX(6rpx);
  border-color: #93c5fd;
  background: linear-gradient(90deg, #eff6ff 0%, #ffffff 100%);
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.1);
}
.haor-row--hover .haor-scene {
  color: #1d4ed8;
}
.haor-row--hover .haor-meta {
  color: #64748b;
}
.haor-all--hover {
  background: rgba(37, 99, 235, 0.08);
}
.haor-all--hover text {
  color: #1d4ed8;
}
.haor-all--hover .haor-all-arrow {
  transform: translateX(4rpx);
}
.haor-head--pulse .haor-dot-icon {
  transform: scale(1.35);
  color: #1d4ed8;
}
.haor-btn--hover {
  transform: translateY(-50%) scale(1.08);
  background: #1d4ed8;
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.35);
}
.haor-more--hover {
  background: rgba(37, 99, 235, 0.06);
}
.haor-more--hover text {
  color: #2563eb;
}
/* #endif */
</style>
