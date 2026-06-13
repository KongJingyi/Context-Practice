<template>
  <div class="coach-card coach-card-hover p-5 relative overflow-hidden">
    <div class="absolute top-0 left-0 right-0 h-1 rounded-t-2xl" :class="accentClass" />
    <div class="flex items-start justify-between mt-1">
      <div>
        <p class="text-3xl font-bold text-slate-900 tracking-tight">{{ value }}</p>
        <p class="text-xs text-slate-400 mt-1.5 font-medium">{{ label }}</p>
        <p v-if="hint" class="text-[11px] text-slate-400 mt-2">{{ hint }}</p>
      </div>
      <div v-if="icon" class="w-10 h-10 rounded-xl flex items-center justify-center text-lg" :class="iconBg">
        {{ icon }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  value: string | number;
  label: string;
  hint?: string;
  icon?: string;
  tone?: "teal" | "blue" | "green" | "amber" | "slate" | "purple";
}>(), { tone: "teal" });

const accentClass = computed(() => ({
  teal: "bg-gradient-to-r from-teal-500 to-cyan-500",
  blue: "bg-gradient-to-r from-blue-500 to-indigo-500",
  green: "bg-gradient-to-r from-emerald-500 to-green-500",
  amber: "bg-gradient-to-r from-amber-500 to-orange-500",
  slate: "bg-gradient-to-r from-slate-400 to-slate-500",
  purple: "bg-gradient-to-r from-violet-500 to-purple-500",
}[props.tone]));

const iconBg = computed(() => ({
  teal: "bg-teal-50",
  blue: "bg-blue-50",
  green: "bg-emerald-50",
  amber: "bg-amber-50",
  slate: "bg-slate-100",
  purple: "bg-violet-50",
}[props.tone]));
</script>
