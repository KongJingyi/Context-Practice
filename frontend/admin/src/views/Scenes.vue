<template>
  <AdminLayout title="场景管理">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "上架" : "下架" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" @click="toggle(row, 0)">下架</el-button>
          <el-button v-else size="small" type="primary" @click="toggle(row, 1)">上架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchScenes, updateSceneStatus } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchScenes();
  } finally {
    loading.value = false;
  }
}

async function toggle(row: Record<string, unknown>, status: number) {
  await updateSceneStatus(row.id as number, status);
  ElMessage.success("已更新");
  await load();
}

onMounted(load);
</script>
