<template>
  <view class="page">
    <TopToast />
    <UserDashboard />
  </view>
</template>

<script setup lang="ts">
import UserDashboard from "@/components/dashboard/UserDashboard.vue";
import TopToast from "@/components/common/TopToast.vue";
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  box-sizing: border-box;
}
</style>
