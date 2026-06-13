<template>
  <div class="flex flex-col items-center justify-center py-20 gap-3">
    <div class="w-8 h-8 border-2 border-teal-600 border-t-transparent rounded-full animate-spin" />
    <span class="text-sm text-slate-400">{{ message || "加载中…" }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{ message?: string }>();
</script>
