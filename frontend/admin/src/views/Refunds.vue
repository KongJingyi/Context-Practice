<template>
  <AdminLayout title="退款审批">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单" width="80" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 'APPLIED'">
            <el-button size="small" type="success" @click="decide(row, 'APPROVED')">批准</el-button>
            <el-button size="small" type="danger" @click="decide(row, 'REJECTED')">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { decideRefund, fetchRefunds } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchRefunds("APPLIED");
  } finally {
    loading.value = false;
  }
}

async function decide(row: Record<string, unknown>, status: string) {
  await decideRefund(row.id as number, status);
  ElMessage.success("已处理");
  await load();
}

onMounted(load);
</script>
