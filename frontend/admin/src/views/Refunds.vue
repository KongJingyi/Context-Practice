<template>
  <AdminLayout title="退款审批">
    <div class="toolbar">
      <el-radio-group v-model="filter" @change="load">
        <el-radio-button value="APPLIED">待审批</el-radio-button>
        <el-radio-button value="REFUNDED">已退款</el-radio-button>
        <el-radio-button value="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单" width="80" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 'APPLIED'">
            <el-button size="small" type="success" @click="decide(row, 'APPROVED')">批准退款</el-button>
            <el-button size="small" type="danger" @click="reject(row)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { decideRefund, fetchRefunds } from "@admin/api/admin";

const loading = ref(false);
const filter = ref("APPLIED");
const rows = ref<Record<string, unknown>[]>([]);

function statusLabel(status: unknown) {
  const map: Record<string, string> = {
    APPLIED: "待审批",
    APPROVED: "已批准",
    REFUNDED: "已退款",
    REJECTED: "已拒绝",
  };
  return map[String(status)] || String(status);
}

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchRefunds(filter.value);
  } finally {
    loading.value = false;
  }
}

async function decide(row: Record<string, unknown>, status: string) {
  await ElMessageBox.confirm("确认批准该退款申请？订单将标记为已退款。", "批准退款");
  await decideRefund(row.id as number, status);
  ElMessage.success("退款已处理");
  await load();
}

async function reject(row: Record<string, unknown>) {
  const { value } = await ElMessageBox.prompt("拒绝原因（可选）", "拒绝退款", {
    inputPlaceholder: "填写说明",
  });
  await decideRefund(row.id as number, "REJECTED");
  if (value) ElMessage.info(`已记录：${value}`);
  ElMessage.success("已拒绝");
  await load();
}

onMounted(load);
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
