<template>
  <AdminLayout title="运营概览" subtitle="平台核心指标与待办事项">
    <div v-if="loading" class="loading">加载中…</div>
    <div v-else class="grid">
      <div v-for="item in cards" :key="item.key" class="stat-card">
        <div class="stat-card__value">{{ stats[item.key] ?? 0 }}</div>
        <div class="stat-card__label">{{ item.label }}</div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import AdminLayout from "@admin/components/AdminLayout.vue";
import { fetchDashboard } from "@admin/api/admin";

const loading = ref(true);
const stats = ref<Record<string, number>>({});

const cards = [
  { key: "userCount", label: "注册用户" },
  { key: "orderCount", label: "订单总数" },
  { key: "pendingVerifications", label: "待审认证" },
  { key: "pendingComplaints", label: "待处理投诉" },
  { key: "pendingRefunds", label: "待审退款" },
  { key: "pendingCertificates", label: "待审证书" },
  { key: "sceneCount", label: "训练场景" },
];

onMounted(async () => {
  try {
    stats.value = (await fetchDashboard()) as Record<string, number>;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.loading {
  color: #64748b;
}
</style>
