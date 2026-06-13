<template>
  <view class="cp">
    <view class="cp-head">
      <text class="cp-badge">陪练工作台</text>
      <text class="cp-title">语境智练 · 陪练端</text>
      <text class="cp-sub">订单管理 · 1v1 训练 · 结构化反馈（PC 优先）</text>
    </view>

    <view class="cp-card">
      <text class="cp-card-title">功能预览</text>
      <view class="cp-item">
        <text class="cp-item-ico">📋</text>
        <view class="cp-item-text">
          <text class="cp-item-name">陪练订单</text>
          <text class="cp-item-desc">查看待服务与进行中订单</text>
        </view>
      </view>
      <view class="cp-item">
        <text class="cp-item-ico">🎥</text>
        <view class="cp-item-text">
          <text class="cp-item-name">训练房间</text>
          <text class="cp-item-desc">TRTC 进房 · 结束训练 · 提交报告</text>
        </view>
      </view>
      <view class="cp-item">
        <text class="cp-item-ico">📝</text>
        <view class="cp-item-text">
          <text class="cp-item-name">结构化反馈</text>
          <text class="cp-item-desc">评分维度 · 亮点 · 改进建议</text>
        </view>
      </view>
      <text class="cp-hint">完整陪练端页面将在此入口扩展，当前为方案 2 登录分流占位。</text>
    </view>

    <view class="cp-actions">
      <view class="cp-btn cp-btn--ghost" @tap="goLogin">
        <text>切换账号</text>
      </view>
      <view class="cp-btn" @tap="onLogout">
        <text>退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from "@/store/user";

const userStore = useUserStore();

function onLogout() {
  uni.showModal({
    title: "确认退出",
    content: "确定要退出登录吗？",
    confirmText: "退出",
    cancelText: "取消",
    success: (res) => {
      if (res.confirm) {
        userStore.logout();
        uni.reLaunch({ url: "/pages/auth/login" });
      }
    },
  });
}

function goLogin() {
  userStore.logout();
  uni.reLaunch({ url: "/pages/auth/login" });
}
</script>

<style scoped>
.cp {
  min-height: 100vh;
  padding: 48rpx 32rpx calc(40rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #f1f5f9 0%, #f8fafc 40%, #ffffff 100%);
  box-sizing: border-box;
}
.cp-head {
  margin-bottom: 32rpx;
}
.cp-badge {
  display: inline-block;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: #334155;
  color: #f8fafc;
  font-size: 22rpx;
  font-weight: 700;
}
.cp-title {
  display: block;
  margin-top: 20rpx;
  font-size: 44rpx;
  font-weight: 800;
  color: #0f172a;
}
.cp-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.5;
}
.cp-card {
  padding: 28rpx;
  border-radius: 24rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.06);
}
.cp-card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 20rpx;
}
.cp-item {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}
.cp-item:last-of-type {
  border-bottom: none;
}
.cp-item-ico {
  font-size: 40rpx;
}
.cp-item-text {
  flex: 1;
}
.cp-item-name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
.cp-item-desc {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748b;
}
.cp-hint {
  display: block;
  margin-top: 20rpx;
  font-size: 22rpx;
  color: #94a3b8;
  line-height: 1.5;
}
.cp-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 32rpx;
}
.cp-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #334155, #475569);
  text-align: center;
}
.cp-btn text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}
.cp-btn--ghost {
  background: #f1f5f9;
  border: 1rpx solid #e2e8f0;
}
.cp-btn--ghost text {
  color: #475569;
}
</style>
