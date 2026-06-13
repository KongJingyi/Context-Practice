<template>
  <view class="cp-page">
    <view v-if="!submitted" class="cp-head">
      <text class="cp-title">投诉与申诉</text>
      <text class="cp-sub">订单 {{ orderNo || orderId }}</text>
    </view>

    <ComplaintForm v-if="!submitted && orderId" :order-id="orderId" @submitted="onSubmitted" />

    <view v-else-if="submitted" class="cp-result">
      <text class="cp-result-ico">✓</text>
      <text class="cp-result-title">投诉已受理</text>
      <text class="cp-result-sub">平台将在 1-3 个工作日内反馈处理结果</text>
      <ProcessingTimeline :steps="steps" />
      <view class="cp-back" @tap="goBack">
        <text>返回订单</text>
      </view>
    </view>

    <view class="cp-float" @tap="callService">
      <text>📞</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import ComplaintForm from "@/components/complaint/ComplaintForm.vue";
import ProcessingTimeline from "@/components/complaint/ProcessingTimeline.vue";
import { fetchComplaintStatus } from "@/api/modules/complaint.js";
import type { ComplaintStep } from "@/types/review";

const orderId = ref("");
const orderNo = ref("");
const submitted = ref(false);
const complaintId = ref("");
const steps = ref<ComplaintStep[]>([]);

onLoad((query) => {
  orderId.value = (query?.orderId as string) || "";
  orderNo.value = (query?.orderNo as string) || "";
});

async function onSubmitted(id: string) {
  complaintId.value = id;
  submitted.value = true;
  const data = await fetchComplaintStatus(id);
  steps.value = data.steps;
}

function goBack() {
  uni.navigateBack({
    fail: () => uni.switchTab({ url: "/pages/my-orders/my-orders" }),
  });
}

function callService() {
  uni.showModal({
    title: "人工客服",
    content: "严重违规可优先联系人工客服，15 分钟内响应。",
    showCancel: false,
  });
}
</script>

<style scoped>
.cp-page {
  min-height: 100vh;
  background: #f1f5f9;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
  position: relative;
}
.cp-head {
  padding: 28rpx 24rpx 8rpx;
  background: #334155;
}
.cp-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #fff;
}
.cp-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #94a3b8;
}
.cp-result {
  padding: 48rpx 24rpx;
}
.cp-result-ico {
  display: block;
  text-align: center;
  font-size: 64rpx;
  color: #22c55e;
}
.cp-result-title {
  display: block;
  text-align: center;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
  margin-top: 16rpx;
}
.cp-result-sub {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 32rpx;
}
.cp-back {
  margin-top: 32rpx;
  padding: 20rpx;
  text-align: center;
  border-radius: 12rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
}
.cp-back text {
  font-size: 28rpx;
  font-weight: 700;
  color: #334155;
}
.cp-float {
  position: fixed;
  right: 32rpx;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 8rpx 24rpx rgba(239, 68, 68, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}
.cp-float text {
  font-size: 40rpx;
}
</style>
