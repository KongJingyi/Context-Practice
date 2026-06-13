<template>
  <text class="nt" :class="{ 'nt--pop': popping }">¥{{ display }}</text>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

const props = defineProps<{
  value: number;
}>();

const display = ref(props.value.toFixed(2));
const popping = ref(false);
let raf = 0;

watch(
  () => props.value,
  (next, prev) => {
    if (next === prev) return;
    const from = prev ?? next;
    const to = next;
    const start = performance.now();
    const duration = 420;
    popping.value = true;

    if (raf) cancelAnimationFrame(raf);

    const step = (now: number) => {
      const t = Math.min(1, (now - start) / duration);
      const eased = 1 - (1 - t) ** 3;
      const cur = from + (to - from) * eased;
      display.value = cur.toFixed(2);
      if (t < 1) {
        raf = requestAnimationFrame(step);
      } else {
        popping.value = false;
        raf = 0;
      }
    };
    raf = requestAnimationFrame(step);
  },
  { immediate: true },
);
</script>

<style scoped>
.nt {
  font-size: 48rpx;
  font-weight: 800;
  color: #0f172a;
  transition: transform 0.2s ease;
}
.nt--pop {
  transform: scale(1.06);
  color: #2563eb;
}
</style>
