<template>
  <view v-if="visible && coaches.length === 2" class="ccp-mask" @tap="emit('close')">
    <view class="ccp" @tap.stop>
      <text class="ccp-title">教练 PK 对比</text>
      <view class="ccp-table">
        <view class="ccp-row ccp-row--head">
          <text class="ccp-cell ccp-cell--label">维度</text>
          <text v-for="c in coaches" :key="c.id" class="ccp-cell ccp-cell--name">{{ c.name }}</text>
        </view>
        <view v-for="row in rows" :key="row.key" class="ccp-row">
          <text class="ccp-cell ccp-cell--label">{{ row.label }}</text>
          <text
            v-for="(c, i) in coaches"
            :key="`${row.key}-${c.id}`"
            class="ccp-cell"
            :class="{ 'ccp-cell--win': row.winnerIndex === i }"
          >
            {{ row.values[i] }}
          </text>
        </view>
      </view>
      <view class="ccp-close" @tap="emit('close')">
        <text>关闭</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Coach } from "@/types/coach/hall";

const props = defineProps<{
  visible: boolean;
  coaches: Coach[];
}>();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const rows = computed(() => {
  const [a, b] = props.coaches;
  if (!a || !b) return [];

  const defs = [
    {
      key: "lv",
      label: "等级",
      va: `Lv.${a.levelNum}`,
      vb: `Lv.${b.levelNum}`,
      scoreA: a.levelNum,
      scoreB: b.levelNum,
    },
    {
      key: "rating",
      label: "评分",
      va: `${a.rating.toFixed(1)} ★`,
      vb: `${b.rating.toFixed(1)} ★`,
      scoreA: a.rating,
      scoreB: b.rating,
    },
    {
      key: "orders",
      label: "完单",
      va: `${a.orderCount} 单`,
      vb: `${b.orderCount} 单`,
      scoreA: a.orderCount,
      scoreB: b.orderCount,
    },
    {
      key: "price",
      label: "价格",
      va: `¥${a.price}`,
      vb: `¥${b.price}`,
      scoreA: -a.price,
      scoreB: -b.price,
    },
    {
      key: "active",
      label: "活跃度",
      va: `${a.activityScore}`,
      vb: `${b.activityScore}`,
      scoreA: a.activityScore,
      scoreB: b.activityScore,
    },
    {
      key: "today",
      label: "今日可约",
      va: a.availableToday ? "是" : "否",
      vb: b.availableToday ? "是" : "否",
      scoreA: a.availableToday ? 1 : 0,
      scoreB: b.availableToday ? 1 : 0,
    },
  ];

  return defs.map((d) => ({
    key: d.key,
    label: d.label,
    values: [d.va, d.vb],
    winnerIndex: d.scoreA === d.scoreB ? -1 : d.scoreA > d.scoreB ? 0 : 1,
  }));
});
</script>

<style scoped>
.ccp-mask {
  position: fixed;
  inset: 0;
  z-index: 1600;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  box-sizing: border-box;
}
.ccp {
  width: 100%;
  max-width: 720rpx;
  max-height: 85vh;
  overflow-y: auto;
  padding: 32rpx 28rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 24rpx 48rpx rgba(15, 23, 42, 0.2);
}
.ccp-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 24rpx;
  text-align: center;
}
.ccp-table {
  border: 1rpx solid #f1f5f9;
  border-radius: 16rpx;
  overflow: hidden;
}
.ccp-row {
  display: flex;
  flex-direction: row;
  border-bottom: 1rpx solid #f1f5f9;
}
.ccp-row:last-child {
  border-bottom: none;
}
.ccp-row--head {
  background: #f8fafc;
}
.ccp-cell {
  flex: 1;
  padding: 18rpx 12rpx;
  font-size: 24rpx;
  color: #334155;
  text-align: center;
}
.ccp-cell--label {
  flex: 0.8;
  text-align: left;
  font-weight: 600;
  color: #64748b;
  background: #fafafa;
}
.ccp-cell--name {
  font-weight: 700;
  color: #0f172a;
}
.ccp-cell--win {
  color: #2563eb;
  font-weight: 800;
  background: #eff6ff;
}
.ccp-close {
  margin-top: 24rpx;
  padding: 18rpx;
  border-radius: 14rpx;
  background: #f1f5f9;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #475569;
}
</style>
