<template>
  <AdminLayout title="投诉处理">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单" width="80" />
      <el-table-column prop="kind" label="类型" width="100" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="resultNote" label="处理说明" show-overflow-tooltip />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">{{ statusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="isPending(row.status)" size="small" @click="close(row)">结案</el-button>
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

function isPending(status: unknown) {
  return status === "SUBMITTED" || status === "IN_REVIEW" || status === "OPEN";
}

function statusLabel(status: unknown) {
  const map: Record<string, string> = {
    SUBMITTED: "待受理",
    IN_REVIEW: "处理中",
    RESOLVED: "已结案",
    REJECTED: "已驳回",
    OPEN: "待受理",
    CLOSED: "已结案",
  };
  return map[String(status)] || String(status);
}

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchComplaints("pending");
  } finally {
    loading.value = false;
  }
}

async function close(row: Record<string, unknown>) {
  const { value } = await ElMessageBox.prompt("处理说明", "投诉结案");
  await handleComplaint(row.id as number, "RESOLVED", value || "");
  ElMessage.success("已结案");
  await load();
}

onMounted(load);
</script>
