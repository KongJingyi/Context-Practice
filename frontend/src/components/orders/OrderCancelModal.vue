<template>
  <view v-if="visible" class="ocm-mask" @tap="emit('close')">
    <view class="ocm" @tap.stop>
      <text class="ocm-title">取消订单</text>
      <text class="ocm-sub">请选择取消原因，便于我们改进服务</text>
      <view class="ocm-list">
        <view
          v-for="r in reasons"
          :key="r"
          class="ocm-item"
          :class="{ 'ocm-item--on': selected === r }"
          @tap="selected = r"
        >
          <text>{{ r }}</text>
        </view>
      </view>
      <view class="ocm-foot">
        <view class="ocm-btn ocm-btn--ghost" @tap="emit('close')">
          <text>再想想</text>
        </view>
        <view
          class="ocm-btn ocm-btn--danger"
          :class="{ 'ocm-btn--disabled': !selected || submitting }"
          @tap="onConfirm"
        >
          <text>{{ submitting ? "提交中…" : "确认取消" }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { CANCEL_REASONS } from "@/types/orders";

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "confirm", reason: string): void;
}>();

const reasons = CANCEL_REASONS;
const selected = ref("");
const submitting = ref(false);

watch(
  () => props.visible,
  (v) => {
    if (v) {
      selected.value = "";
      submitting.value = false;
    }
  },
);

function onConfirm() {
  if (!selected.value || submitting.value) return;
  submitting.value = true;
  emit("confirm", selected.value);
}
</script>

<style scoped>
.ocm-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}
.ocm {
  width: 100%;
  max-width: 600rpx;
  padding: 32rpx 28rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 24rpx 48rpx rgba(15, 23, 42, 0.18);
}
.ocm-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.ocm-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 20rpx;
}
.ocm-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.ocm-item {
  padding: 18rpx 20rpx;
  border-radius: 12rpx;
  border: 2rpx solid #e2e8f0;
  font-size: 26rpx;
  color: #334155;
}
.ocm-item--on {
  border-color: #2563eb;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}
.ocm-foot {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 28rpx;
}
.ocm-btn {
  flex: 1;
  padding: 18rpx;
  border-radius: 14rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
}
.ocm-btn--ghost {
  border: 1rpx solid #e2e8f0;
  color: #64748b;
}
.ocm-btn--danger {
  background: #dc2626;
  color: #fff;
}
.ocm-btn--disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
