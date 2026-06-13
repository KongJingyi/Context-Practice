<template>
  <view v-if="active" class="lb">
    <view
      v-for="(p, i) in particles"
      :key="i"
      class="lb-p"
      :class="p.kind"
      :style="p.style"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

const props = defineProps<{ active: boolean }>();

const particles = ref<{ kind: string; style: Record<string, string> }[]>([]);

watch(
  () => props.active,
  (v) => {
    if (!v) return;
    const kinds = ["lb-p--heart", "lb-p--dot", "lb-p--heart"];
    particles.value = Array.from({ length: 8 }, (_, i) => {
      const angle = (i / 8) * Math.PI * 2;
      const dist = 24 + Math.random() * 20;
      const dx = Math.cos(angle) * dist;
      const dy = -Math.abs(Math.sin(angle) * dist) - 20;
      return {
        kind: kinds[i % kinds.length],
        style: {
          "--dx": `${dx}px`,
          "--dy": `${dy}px`,
          animationDelay: `${i * 0.03}s`,
        },
      };
    });
    setTimeout(() => {
      particles.value = [];
    }, 700);
  },
);
</script>

<style scoped>
.lb {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: visible;
  z-index: 5;
}
.lb-p {
  position: absolute;
  left: 50%;
  top: 50%;
  animation: lbFly 0.65s ease-out forwards;
  opacity: 0;
}
.lb-p--heart::after {
  content: "♥";
  font-size: 14px;
  color: #2563eb;
}
.lb-p--dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #60a5fa;
  margin: -3px 0 0 -3px;
}
@keyframes lbFly {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(0.6);
  }
  100% {
    opacity: 0;
    transform: translate(calc(-50% + var(--dx)), calc(-50% + var(--dy))) scale(1.1);
  }
}
</style>
