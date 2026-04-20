<template>
  <view class="page">
    <view class="hero">
      <text class="hero-title">语境智练</text>
      <text class="hero-sub">Web 深度视频模拟 · 小程序预约与复盘</text>
    </view>

    <view class="section-title">场景广场</view>
    <view class="grid">
      <view
        v-for="item in scenarios"
        :key="item.id"
        class="card"
        @click="enterRoom(item)"
      >
        <text class="card-tag">{{ item.tag }}</text>
        <text class="card-title">{{ item.title }}</text>
        <text class="card-desc">{{ item.desc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { setPendingTraining } from "@/utils/trainingBridge.js";

const scenarios = ref([
  {
    id: "job",
    tag: "求职",
    title: "职场求职",
    desc: "自我介绍、项目深挖、薪资沟通",
  },
  {
    id: "mgmt",
    tag: "管理",
    title: "管理汇报",
    desc: "周报月报、资源争取、风险同步",
  },
  {
    id: "client",
    tag: "商务",
    title: "客户谈判",
    desc: "需求澄清、异议处理、收口共识",
  },
  {
    id: "speech",
    tag: "公开",
    title: "公开演讲",
    desc: "开场、结构递进、行动号召",
  },
]);

function enterRoom(item: { id: string; title: string }) {
  setPendingTraining({ scenarioId: item.id, roomTitle: item.title });
  uni.switchTab({ url: "/pages/room/room" });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32rpx 28rpx 48rpx;
  background: linear-gradient(180deg, #eef2ff 0%, #ffffff 45%);
  box-sizing: border-box;
}
.hero {
  margin-bottom: 40rpx;
}
.hero-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #111827;
  display: block;
}
.hero-sub {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #6b7280;
  display: block;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20rpx;
}
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.card {
  width: calc(50% - 10rpx);
  box-sizing: border-box;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 28rpx rgba(15, 23, 42, 0.06);
}
.card-tag {
  font-size: 22rpx;
  color: #4f46e5;
  background: #eef2ff;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  display: inline-block;
}
.card-title {
  margin-top: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  display: block;
}
.card-desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.5;
  display: block;
}
</style>
