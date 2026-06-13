<template>
  <AdminLayout title="认证审核" subtitle="用户实名与陪练资质认证">
    <el-segmented v-model="status" :options="['pending', 'approved', 'rejected']" @change="load" />
    <el-table v-loading="loading" :data="rows" stripe class="mt">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="kind" label="类型" />
      <el-table-column prop="submitPayload" label="提交内容" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="Number(row.status) === 0">
            <el-button size="small" type="success" @click="review(row, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="review(row, 2)">驳回</el-button>
          </template>
          <el-tag v-else>{{ Number(row.status) === 1 ? "已通过" : "已驳回" }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchVerifications, reviewVerification } from "@admin/api/admin";

const loading = ref(false);
const status = ref("pending");
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchVerifications(status.value);
  } finally {
    loading.value = false;
  }
}

async function review(row: Record<string, unknown>, s: number) {
  let note = "";
  if (s === 2) {
    const { value } = await ElMessageBox.prompt("驳回原因", "认证驳回");
    note = value || "";
  }
  try {
    await reviewVerification(row.id as number, s, note);
    ElMessage.success("已处理");
    await load();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "处理失败，请重试");
  }
}

onMounted(load);
</script>

<style scoped>
.mt { margin-top: 16px; }
</style>
