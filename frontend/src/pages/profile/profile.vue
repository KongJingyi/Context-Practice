<template>
  <view class="page">
    <view class="head">
      <text class="name">{{ displayName }}</text>
      <text class="status">{{ userStore.isLoggedIn ? "已登录" : "未登录（演示）" }}</text>
    </view>

    <view class="actions">
      <button v-if="!userStore.isLoggedIn" class="btn primary" type="button" @click="mockLogin">
        模拟登录
      </button>
      <button v-else class="btn ghost" type="button" @click="onLogout">退出登录</button>
    </view>

    <GrowthChartPlaceholder class="chart" />

    <view class="hint-block">
      <text class="hint-title">小程序端</text>
      <text class="hint-body">预约、复盘摘要可集中在此页，与 Web 深度模拟形成互补。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { StoreGeneric } from "pinia";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/store/user";
import GrowthChartPlaceholder from "@/components/GrowthChartPlaceholder.vue";

const userStore = useUserStore() as StoreGeneric;
const { userInfo } = storeToRefs(userStore);

const displayName = computed(
  () => (userInfo.value as { nickname?: string } | null)?.nickname || "语境智练学员",
);

function mockLogin() {
  userStore.setToken("demo-token-" + Date.now());
  userStore.setUserInfo({ nickname: "演示用户" });
}

function onLogout() {
  userStore.logout();
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32rpx 28rpx 48rpx;
  background: #f8fafc;
  box-sizing: border-box;
}
.head {
  padding: 8rpx 0 24rpx;
}
.name {
  font-size: 40rpx;
  font-weight: 700;
  color: #0f172a;
  display: block;
}
.status {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #64748b;
  display: block;
}
.actions {
  display: flex;
  gap: 20rpx;
  margin-bottom: 32rpx;
}
.btn {
  flex: 1;
  font-size: 28rpx;
}
.btn.primary {
  background: #4f46e5;
  color: #ffffff;
  border: none;
}
.btn.ghost {
  background: #ffffff;
  color: #475569;
  border: 2rpx solid #e2e8f0;
}
.chart {
  margin-bottom: 32rpx;
}
.hint-block {
  padding: 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
}
.hint-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  display: block;
}
.hint-body {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.55;
  display: block;
}
</style>
