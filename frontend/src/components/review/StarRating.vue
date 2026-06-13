<template>
  <view class="sr">
    <text v-if="label" class="sr-label">{{ label }}</text>
    <view class="sr-stars">
      <view
        v-for="n in max"
        :key="n"
        class="sr-star"
        :class="{ 'sr-star--on': n <= modelValue, 'sr-star--hover': hoverIndex >= n }"
        @tap="set(n)"
        @mouseenter="hoverIndex = n"
        @mouseleave="hoverIndex = 0"
      >
        <text class="sr-glyph">{{ n <= (hoverIndex || modelValue) ? '★' : '☆' }}</text>
        <view v-if="burst && n === modelValue" class="sr-burst" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    modelValue: number;
    label?: string;
    max?: number;
  }>(),
  { max: 5 },
);

const emit = defineEmits<{ (e: "update:modelValue", v: number): void }>();

const hoverIndex = ref(0);
const burst = ref(false);

function set(n: number) {
  emit("update:modelValue", n);
  burst.value = true;
  setTimeout(() => {
    burst.value = false;
  }, 400);
}

watch(
  () => props.modelValue,
  () => {
    burst.value = true;
    setTimeout(() => {
      burst.value = false;
    }, 300);
  },
);
</script>

<style scoped>
.sr {
  margin-bottom: 24rpx;
}
.sr-label {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #334155;
  margin-bottom: 12rpx;
}
.sr-stars {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
}
.sr-star {
  position: relative;
  cursor: pointer;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.sr-star--hover,
.sr-star--on {
  transform: scale(1.15);
}
.sr-glyph {
  font-size: 44rpx;
  color: #cbd5e1;
  transition: color 0.2s ease, text-shadow 0.2s ease;
}
.sr-star--on .sr-glyph,
.sr-star--hover .sr-glyph {
  color: #f59e0b;
  text-shadow: 0 0 12rpx rgba(245, 158, 11, 0.55);
}
.sr-burst {
  position: absolute;
  inset: -4rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(251, 191, 36, 0.6);
  animation: srPulse 0.4s ease-out;
  pointer-events: none;
}
@keyframes srPulse {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}
</style>
