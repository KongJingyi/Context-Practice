import { createRouter, createWebHashHistory } from "vue-router";

const FRONTEND_BASE = import.meta.env.VITE_FRONTEND_BASE || "http://localhost:5173";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/dashboard", component: () => import("@admin/views/Dashboard.vue"), meta: { title: "概览" } },
  { path: "/users", component: () => import("@admin/views/Users.vue"), meta: { title: "用户管理" } },
  { path: "/verifications", component: () => import("@admin/views/Verifications.vue"), meta: { title: "认证审核" } },
  { path: "/complaints", component: () => import("@admin/views/Complaints.vue"), meta: { title: "投诉处理" } },
  { path: "/refunds", component: () => import("@admin/views/Refunds.vue"), meta: { title: "退款审批" } },
  { path: "/certificates", component: () => import("@admin/views/Certificates.vue"), meta: { title: "证书审核" } },
  { path: "/announcements", component: () => import("@admin/views/Announcements.vue"), meta: { title: "公告管理" } },
  { path: "/config", component: () => import("@admin/views/Config.vue"), meta: { title: "系统配置" } },
  { path: "/scenes", component: () => import("@admin/views/Scenes.vue"), meta: { title: "场景管理" } },
  { path: "/question-banks", component: () => import("@admin/views/QuestionBanks.vue"), meta: { title: "题库管理" } },
  { path: "/community", component: () => import("@admin/views/Community.vue"), meta: { title: "社区内容" } },
  { path: "/orders", component: () => import("@admin/views/Orders.vue"), meta: { title: "订单监控" } },
  { path: "/audit-logs", component: () => import("@admin/views/AuditLogs.vue"), meta: { title: "审计日志" } },
];

const router = createRouter({ history: createWebHashHistory(), routes });

router.beforeEach((to, _from, next) => {
  const hashQuery = window.location.hash.split("?")[1] || "";
  const urlToken = new URLSearchParams(hashQuery).get("token");
  if (urlToken) {
    localStorage.setItem("token", urlToken);
    localStorage.setItem("ctx_preferred_app_side", "admin");
    const cleanHash = window.location.hash.split("?")[0];
    window.location.hash = cleanHash;
  }
  if (!localStorage.getItem("token")) {
    window.location.href = `${FRONTEND_BASE}/#/pages/auth/login?logout=1&side=admin`;
    return;
  }
  document.title = `管理后台 · ${(to.meta.title as string) || "语境智练"}`;
  next();
});

export default router;
