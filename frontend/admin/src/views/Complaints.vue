<template>
  <AdminLayout title="投诉处理">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单" width="80" />
      <el-table-column prop="kind" label="类型" width="100" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 'OPEN'" size="small" @click="close(row)">结案</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchComplaints, handleComplaint } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchComplaints("OPEN");
  } finally {
    loading.value = false;
  }
}

async function close(row: Record<string, unknown>) {
  const { value } = await ElMessageBox.prompt("处理说明", "投诉结案");
  await handleComplaint(row.id as number, "CLOSED", value || "");
  ElMessage.success("已结案");
  await load();
}

onMounted(load);
</script>
