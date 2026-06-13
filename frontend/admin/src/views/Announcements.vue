<template>
  <AdminLayout title="公告管理">
    <div class="toolbar">
      <el-button type="primary" @click="showDialog = true">发布公告</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="kind" label="类型" width="100" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="发布公告" width="520px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.kind" style="width: 100%">
            <el-option label="通知" value="NOTICE" />
            <el-option label="培训" value="TRAINING" />
            <el-option label="规范" value="NORM" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="create">发布</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { createAnnouncement, deleteAnnouncement, fetchAnnouncements } from "@admin/api/admin";

const loading = ref(false);
const showDialog = ref(false);
const rows = ref<Record<string, unknown>[]>([]);
const form = reactive({ title: "", content: "", kind: "NOTICE" });

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchAnnouncements();
  } finally {
    loading.value = false;
  }
}

async function create() {
  await createAnnouncement({ ...form, status: 1 });
  showDialog.value = false;
  ElMessage.success("已发布");
  await load();
}

async function remove(row: Record<string, unknown>) {
  await deleteAnnouncement(row.id as number);
  ElMessage.success("已删除");
  await load();
}

onMounted(load);
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
