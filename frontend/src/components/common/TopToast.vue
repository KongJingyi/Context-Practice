<template>
  <view v-if="visible" class="tt" :class="`tt--${type}`">
    <text>{{ message }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { registerTopToast } from "@/utils/common/topToast";

const visible = ref(false);
const message = ref("");
const type = ref<"error" | "success" | "info">("error");
let timer: ReturnType<typeof setTimeout> | null = null;

function show(msg: string, t: "error" | "success" | "info" = "error") {
  message.value = msg;
  type.value = t;
  visible.value = true;
  if (timer) clearTimeout(timer);
  timer = setTimeout(() => {
    visible.value = false;
  }, 3000);
}

onMounted(() => {
  registerTopToast(show);
});

onBeforeUnmount(() => {
  registerTopToast(() => {});
  if (timer) clearTimeout(timer);
});
</script>

<style scoped>
.tt {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  padding: 16rpx 32rpx;
  border-radius: 999rpx;
  max-width: 90%;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.12);
  animation: ttIn 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.tt--error {
  background: #fef2f2;
  border: 1rpx solid #fecaca;
}
.tt--error text {
  color: #b91c1c;
}
.tt--success {
  background: #ecfdf5;
  border: 1rpx solid #a7f3d0;
}
.tt--success text {
  color: #047857;
}
.tt--info {
  background: #eff6ff;
  border: 1rpx solid #bfdbfe;
}
.tt--info text {
  color: #1d4ed8;
}
.tt text {
  font-size: 26rpx;
  font-weight: 600;
}
@keyframes ttIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-12rpx);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>
