<template>
  <ReportDetail v-if="orderId" :order-id="orderId" />
  <view v-else class="empty">
    <text>缺少订单参数</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import ReportDetail from "@/components/report/ReportDetail.vue";

const orderId = ref("");

onLoad((query) => {
  orderId.value = (query?.orderId as string) || (query?.id as string) || "";
});
</script>

<style scoped>
.empty {
  padding: 80rpx;
  text-align: center;
  color: #64748b;
}
</style>
