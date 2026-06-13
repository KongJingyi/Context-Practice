<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand__logo">AD</div>
        <div>
          <p class="brand__title">语境智练</p>
          <p class="brand__sub">管理后台</p>
        </div>
      </div>
      <nav class="nav">
        <router-link v-for="item in nav" :key="item.to" :to="item.to" class="admin-sidebar-link" active-class="active">
          {{ item.label }}
        </router-link>
      </nav>
      <button class="logout" type="button" @click="logout">退出登录</button>
    </aside>
    <main class="main page-enter">
      <header v-if="title" class="head">
        <h1>{{ title }}</h1>
        <p v-if="subtitle">{{ subtitle }}</p>
      </header>
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title?: string; subtitle?: string }>();

const nav = [
  { to: "/dashboard", label: "概览" },
  { to: "/users", label: "用户管理" },
  { to: "/verifications", label: "认证审核" },
  { to: "/certificates", label: "证书审核" },
  { to: "/complaints", label: "投诉处理" },
  { to: "/refunds", label: "退款审批" },
  { to: "/orders", label: "订单监控" },
  { to: "/scenes", label: "场景管理" },
  { to: "/question-banks", label: "题库管理" },
  { to: "/community", label: "社区内容" },
  { to: "/announcements", label: "公告管理" },
  { to: "/config", label: "系统配置" },
  { to: "/audit-logs", label: "审计日志" },
];

function logout() {
  localStorage.removeItem("token");
  const base = import.meta.env.VITE_FRONTEND_BASE || "http://localhost:5173";
  window.location.href = `${base}/#/pages/auth/login?logout=1&side=admin`;
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 20px 12px;
  position: fixed;
  inset: 0 auto 0 0;
}
.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 0 8px 20px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 16px;
}
.brand__logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
}
.brand__title {
  margin: 0;
  font-size: 14px;
  font-weight: 800;
}
.brand__sub {
  margin: 2px 0 0;
  font-size: 11px;
  color: #64748b;
}
.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}
.logout {
  margin-top: 12px;
  border: none;
  background: #fef2f2;
  color: #dc2626;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
}
.main {
  flex: 1;
  margin-left: 240px;
  padding: 24px 32px 40px;
}
.head {
  margin-bottom: 20px;
}
.head h1 {
  margin: 0;
  font-size: 22px;
}
.head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}
</style>
