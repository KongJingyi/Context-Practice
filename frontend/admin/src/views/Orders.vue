<template>
  <AdminLayout title="订单监控">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="学员" width="80" />
      <el-table-column prop="coachId" label="陪练" width="80" />
      <el-table-column prop="amount" label="金额" width="100" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="scheduledStart" label="预约开始" width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchOrders } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    rows.value = await fetchOrders();
  } finally {
    loading.value = false;
  }
});
</script>
