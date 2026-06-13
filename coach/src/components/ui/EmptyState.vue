<template>
  <div class="coach-card p-16 text-center">
    <div class="w-16 h-16 mx-auto mb-5 rounded-2xl bg-slate-50 flex items-center justify-center">
      <svg class="w-8 h-8 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" :d="iconPath" />
      </svg>
    </div>
    <p class="text-slate-700 font-semibold">{{ title }}</p>
    <p v-if="description" class="text-slate-400 text-sm mt-1.5 max-w-sm mx-auto">{{ description }}</p>
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string;
  description?: string;
  iconPath?: string;
}>(), {
  iconPath: "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2",
});
</script>
