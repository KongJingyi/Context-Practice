<template>
  <AdminLayout title="审计日志" subtitle="管理员操作留痕">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="actorId" label="操作人" width="90" />
      <el-table-column prop="action" label="动作" width="160" />
      <el-table-column prop="targetType" label="对象类型" width="120" />
      <el-table-column prop="targetId" label="对象ID" width="90" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchAuditLogs } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    rows.value = await fetchAuditLogs();
  } finally {
    loading.value = false;
  }
});
</script>
