<template>
  <div class="min-h-screen flex bg-slate-50">
    <!-- 侧边栏 -->
    <aside class="w-60 shrink-0 bg-white border-r border-slate-200 flex flex-col fixed inset-y-0 left-0 z-20">
      <div class="px-5 py-6 border-b border-slate-100">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-teal-600 to-teal-500 flex items-center justify-center text-white font-bold text-sm tracking-tight">
            CP
          </div>
          <div>
            <p class="font-bold text-slate-900 text-sm leading-tight">Context 陪练</p>
            <p class="text-[11px] text-slate-400 mt-0.5">专业表达训练平台</p>
          </div>
        </div>
      </div>

      <nav class="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="coach-sidebar-link"
          :class="{ active: isActive(item.to) }"
        >
          <component :is="item.icon" class="nav-icon" />
          <span>{{ item.label }}</span>
          <span
            v-if="item.badge"
            class="ml-auto coach-badge bg-teal-50 text-teal-700"
          >{{ item.badge }}</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-slate-100">
        <div class="flex items-center gap-3 px-2 py-2 mb-2">
          <div class="w-9 h-9 rounded-full bg-teal-100 flex items-center justify-center text-teal-700 font-bold text-sm">
            {{ displayName.charAt(0) }}
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-semibold text-slate-900 truncate">{{ displayName }}</p>
            <p class="text-[11px] text-teal-600 font-medium">{{ levelLabel }}</p>
          </div>
        </div>
        <button
          class="w-full py-2 text-xs text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
          @click="handleLogout"
        >
          退出登录
        </button>
      </div>
    </aside>

    <!-- 主内容 -->
    <div class="flex-1 ml-60 min-h-screen flex flex-col">
      <header v-if="pageTitle" class="sticky top-0 z-10 bg-white/90 backdrop-blur-md border-b border-slate-200">
        <div class="px-8 h-14 flex items-center justify-between">
          <div>
            <h1 class="text-base font-bold text-slate-900">{{ pageTitle }}</h1>
            <p v-if="pageSubtitle" class="text-xs text-slate-400 mt-0.5">{{ pageSubtitle }}</p>
          </div>
          <slot name="header-actions" />
        </div>
      </header>
      <main class="flex-1 px-8 py-6 page-enter">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from "vue";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/user";
import { fetchCoachProfile } from "@/api/modules/profile";

defineProps<{
  pageTitle?: string;
  pageSubtitle?: string;
}>();

const route = useRoute();
const userStore = useUserStore();
const displayName = ref(userStore.userInfo?.username || "陪练员");
const levelLabel = ref("陪练员");

const icon = (paths: string) => ({
  render: () =>
    h("svg", { class: "nav-icon", fill: "none", stroke: "currentColor", viewBox: "0 0 24 24" }, [
      h("path", { "stroke-linecap": "round", "stroke-linejoin": "round", "stroke-width": "1.75", d: paths }),
    ]),
});

const navItems = [
  { to: "/orders", label: "订单工作台", icon: icon("M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2") },
  { to: "/schedule", label: "排班设置", icon: icon("M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z") },
  { to: "/history", label: "训练历史", icon: icon("M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z") },
  { to: "/income", label: "收入明细", icon: icon("M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z") },
  { to: "/dashboard", label: "数据统计", icon: icon("M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z") },
  { to: "/profile", label: "个人主页", icon: icon("M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z") },
  { to: "/certificates", label: "资质认证", icon: icon("M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z") },
];

function isActive(path: string) {
  if (path === "/orders") return route.path === "/orders" || route.path.startsWith("/orders/");
  return route.path.startsWith(path);
}

function handleLogout() {
  try {
    userStore.logout();
  } finally {
    const base = import.meta.env.VITE_FRONTEND_BASE || "http://localhost:5173";
    document.location.href = `${base}/#/pages/auth/login?logout=1`;
  }
}

onMounted(async () => {
  const profile = await fetchCoachProfile().catch(() => null);
  if (profile?.nickname || profile?.name) {
    displayName.value = (profile.nickname || profile.name)!;
  }
  if (profile?.level) levelLabel.value = profile.level;
});
</script>
