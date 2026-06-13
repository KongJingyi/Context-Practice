<template>
  <AdminLayout title="证书审核" subtitle="陪练提交的学信网/比赛证书">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="coachName" label="陪练" />
      <el-table-column prop="kind" label="类型" width="100" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="verifyCode" label="验证码" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="review(row, 1)">通过</el-button>
          <el-button size="small" type="danger" @click="review(row, 2)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchPendingCertificates, reviewCertificate } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchPendingCertificates();
  } finally {
    loading.value = false;
  }
}

async function review(row: Record<string, unknown>, status: number) {
  let reason = "";
  if (status === 2) {
    const { value } = await ElMessageBox.prompt("驳回原因", "证书驳回");
    reason = value || "";
  }
  await reviewCertificate(row.id as number, status, reason);
  ElMessage.success("已处理");
  await load();
}

onMounted(load);
</script>
