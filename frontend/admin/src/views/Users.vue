<template>
  <AdminLayout title="用户管理" subtitle="查看账号状态，冻结或解冻用户">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名/手机号" clearable style="max-width: 280px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="mobile" label="手机号" />
      <el-table-column label="角色">
        <template #default="{ row }">{{ (row.roles as string[])?.join(", ") }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? "正常" : "冻结" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="danger" @click="toggle(row, 1)">冻结</el-button>
          <el-button v-else size="small" type="success" @click="toggle(row, 0)">解冻</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchUsers, updateUserStatus } from "@admin/api/admin";

const loading = ref(false);
const keyword = ref("");
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchUsers(keyword.value || undefined);
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

async function toggle(row: Record<string, unknown>, status: number) {
  await updateUserStatus(row.id as number, status);
  ElMessage.success("已更新");
  await load();
}

onMounted(load);
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
