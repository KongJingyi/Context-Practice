<template>
  <AdminLayout title="系统配置">
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="key" label="键" />
      <el-table-column prop="value" label="值" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" link @click="edit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchConfigs, upsertConfig } from "@admin/api/admin";

const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchConfigs();
  } finally {
    loading.value = false;
  }
}

async function edit(row: Record<string, unknown>) {
  const { value } = await ElMessageBox.prompt("配置值", String(row.key), {
    inputValue: String(row.value ?? ""),
  });
  await upsertConfig(String(row.key), value || "", String(row.description ?? ""));
  ElMessage.success("已保存");
  await load();
}

onMounted(load);
</script>
