<template>
  <PostTrainingReview v-if="context" :context="context" @done="onDone" @skip="onSkip" />
  <view v-else class="empty">
    <text>暂无评价信息</text>
    <view class="empty-btn" @tap="goOrders">
      <text>返回订单</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import PostTrainingReview from "@/components/review/PostTrainingReview.vue";
import { consumePendingReview, peekPendingReview } from "@/utils/review/bridge.js";
import type { ReviewContext } from "@/types/review";

const context = ref<ReviewContext | null>(null);

onLoad((query) => {
  const pending = consumePendingReview() || peekPendingReview();
  if (pending) {
    context.value = pending;
    return;
  }
  if (query?.orderId) {
    context.value = {
      orderId: query.orderId as string,
      orderNo: (query.orderNo as string) || "",
      expertId: (query.expertId as string) || "c_default",
      expertName: (query.expertName as string) || "语境专家",
      expertTitle: (query.expertTitle as string) || "",
    };
  }
});

onMounted(() => {
  if (!context.value) {
    const pending = peekPendingReview();
    if (pending) context.value = pending;
  }
});

function onDone() {
  uni.switchTab({ url: "/pages/my-orders/my-orders" });
}

function onSkip() {
  uni.navigateBack({
    fail: () => uni.switchTab({ url: "/pages/my-orders/my-orders" }),
  });
}

function goOrders() {
  uni.switchTab({ url: "/pages/my-orders/my-orders" });
}
</script>

<style scoped>
.empty {
  padding: 120rpx 40rpx;
  text-align: center;
  color: #64748b;
}
.empty-btn {
  margin-top: 32rpx;
  padding: 16rpx 32rpx;
  display: inline-block;
  border-radius: 12rpx;
  background: #2563eb;
}
.empty-btn text {
  color: #fff;
  font-weight: 700;
}
</style>
