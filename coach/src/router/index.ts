import { createRouter, createWebHashHistory } from "vue-router";
import { getToken } from "@/utils/auth/token";
import { useUserStore } from "@/store/user";

const FRONTEND_BASE = import.meta.env.VITE_FRONTEND_BASE || window.location.origin;

const routes = [
  { path: "/", redirect: "/orders" },
  { path: "/orders", name: "Orders", component: () => import("@/views/Orders.vue"), meta: { requiresAuth: true } },
  { path: "/orders/:id", name: "OrderDetail", component: () => import("@/views/OrderDetail.vue"), meta: { requiresAuth: true } },
  { path: "/schedule", name: "Schedule", component: () => import("@/views/Schedule.vue"), meta: { requiresAuth: true } },
  { path: "/history", name: "TrainingHistory", component: () => import("@/views/TrainingHistory.vue"), meta: { requiresAuth: true } },
  { path: "/income", name: "Income", component: () => import("@/views/Income.vue"), meta: { requiresAuth: true } },
  { path: "/dashboard", name: "CoachDashboard", component: () => import("@/views/CoachDashboard.vue"), meta: { requiresAuth: true } },
  { path: "/profile", name: "CoachProfile", component: () => import("@/views/CoachProfile.vue"), meta: { requiresAuth: true } },
  { path: "/certificates", name: "Certificates", component: () => import("@/views/Certificates.vue"), meta: { requiresAuth: true } },
  { path: "/room/:roomId", name: "TrainingRoom", component: () => import("@/views/TrainingRoom.vue"), meta: { requiresAuth: true, fullscreen: true } },
  { path: "/report/:orderId", name: "ReportDetail", component: () => import("@/views/ReportDetail.vue"), meta: { requiresAuth: true, fullscreen: true } },
  { path: "/submit-feedback/:orderId", name: "ReportSubmit", component: () => import("@/views/ReportSubmit.vue"), meta: { requiresAuth: true, fullscreen: true } },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const hashQuery = window.location.hash.split("?")[1] || "";
  const urlToken = new URLSearchParams(hashQuery).get("token");
  if (urlToken) {
    const userStore = useUserStore();
    userStore.setAuthSession({
      token: urlToken,
      roles: ["COACH"],
      appSide: "coach",
    });
    const cleanHash = window.location.hash.split("?")[0];
    window.location.hash = cleanHash;
  }

  if (to.meta.requiresAuth) {
    const token = getToken();
    if (!token) {
      window.location.href = `${FRONTEND_BASE}/#/pages/auth/login?logout=1`;
      return;
    }
  }
  next();
});

export default router;
