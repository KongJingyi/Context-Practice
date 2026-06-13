<template>
  <view v-if="visible" class="hf" />
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>();
</script>

<style scoped>
.hf {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: #fff;
  pointer-events: none;
  animation: hfFlash 0.45s ease-out forwards;
}
@keyframes hfFlash {
  0% {
    opacity: 0.85;
  }
  100% {
    opacity: 0;
  }
}
</style>
