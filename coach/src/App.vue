<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { registerTopToast } from "@/utils/toast";
import { ElMessage } from "element-plus";

onMounted(() => {
  registerTopToast((msg: string, type: "error" | "success" | "info") => {
    ElMessage({ message: msg, type });
  });
});
</script>
