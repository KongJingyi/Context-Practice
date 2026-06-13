<template>
  <AdminLayout title="社区内容">
    <div class="toolbar">
      <el-radio-group v-model="filter" @change="load">
        <el-radio-button :value="1">正常</el-radio-button>
        <el-radio-button :value="0">已隐藏</el-radio-button>
        <el-radio-button :value="undefined">全部</el-radio-button>
      </el-radio-group>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="likeCount" label="赞" width="60" />
      <el-table-column prop="commentCount" label="评" width="60" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? "正常" : "隐藏" }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" link @click="toggle(row, 0)">隐藏</el-button>
          <el-button v-else size="small" type="primary" link @click="toggle(row, 1)">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchCommunityPosts, updateCommunityPostStatus } from "@admin/api/admin";

const loading = ref(false);
const filter = ref<number | undefined>(1);
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchCommunityPosts(filter.value);
  } finally {
    loading.value = false;
  }
}

async function toggle(row: Record<string, unknown>, status: number) {
  await updateCommunityPostStatus(row.id as number, status);
  ElMessage.success(status === 1 ? "已恢复" : "已隐藏");
  await load();
}

onMounted(load);
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
