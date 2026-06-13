<template>
  <scroll-view class="page-scroll" scroll-y :show-scrollbar="false" :enable-back-to-top="true">
    <CommunitySquare />
  </scroll-view>
</template>

<script setup lang="ts">
import CommunitySquare from "@/components/community/CommunitySquare.vue";
</script>

<style scoped>
.page-scroll {
  height: 100vh;
  width: 100%;
  background: #f0f7ff;
  box-sizing: border-box;
}
</style>
