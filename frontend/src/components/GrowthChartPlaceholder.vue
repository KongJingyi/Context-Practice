<template>
  <view class="chart-placeholder">
    <text class="title">表达能力成长曲线</text>
    <view class="box">
      <text class="hint">图表占位（可接入 uCharts / echarts-for-uniapp）</text>
    </view>
  </view>
</template>

<script setup lang="ts"></script>

<style scoped>
.chart-placeholder {
  width: 100%;
}
.title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a1a;
}
.box {
  margin-top: 24rpx;
  height: 320rpx;
  border: 2rpx dashed #c5d4ff;
  border-radius: 16rpx;
  background: linear-gradient(180deg, #f6f8ff 0%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
}
.hint {
  font-size: 24rpx;
  color: #6b7280;
  text-align: center;
}
</style>
