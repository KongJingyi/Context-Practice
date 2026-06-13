<template>
  <AdminLayout title="公告管理">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">发布公告</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="kind" label="类型" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? "已发布" : "草稿" }}</template>
      </el-table-column>
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑公告' : '发布公告'" width="520px">
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
        <el-button type="primary" @click="save">{{ editingId ? "保存" : "发布" }}</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import {
  createAnnouncement,
  deleteAnnouncement,
  fetchAnnouncements,
  updateAnnouncement,
} from "@admin/api/admin";

const loading = ref(false);
const showDialog = ref(false);
const editingId = ref<number | null>(null);
const rows = ref<Record<string, unknown>[]>([]);
const form = reactive({ title: "", content: "", kind: "NOTICE" });

function resetForm() {
  form.title = "";
  form.content = "";
  form.kind = "NOTICE";
  editingId.value = null;
}

function openCreate() {
  resetForm();
  showDialog.value = true;
}

function openEdit(row: Record<string, unknown>) {
  editingId.value = row.id as number;
  form.title = String(row.title ?? "");
  form.content = String(row.content ?? "");
  form.kind = String(row.kind ?? "NOTICE");
  showDialog.value = true;
}

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchAnnouncements();
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!form.title.trim()) {
    ElMessage.warning("请填写标题");
    return;
  }
  if (editingId.value) {
    await updateAnnouncement(editingId.value, { ...form });
    ElMessage.success("已更新");
  } else {
    await createAnnouncement({ ...form, status: 1 });
    ElMessage.success("已发布");
  }
  showDialog.value = false;
  resetForm();
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
