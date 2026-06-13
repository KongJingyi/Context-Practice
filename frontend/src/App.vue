<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import { useUserStore } from "@/store/user";
import { useVerifyStore } from "@/store/verify";
import { navigateAfterAuth } from "@/utils/auth/roleNav";
import type { AppSide, UserRole } from "@/types/auth";

onLaunch(() => {
  const userStore = useUserStore();
  const verifyStore = useVerifyStore();
  userStore.hydrateFromStorage();
  verifyStore.hydrateFromStorage();

  // 从 coach 退出时携带 logout=1，清除会话避免自动跳回陪练端
  // #ifdef H5
  if (window.location.href.includes("logout=1")) {
    userStore.logout();
    return;
  }
  // #endif

  if (userStore.isLoggedIn) {
    const side = (userStore.preferredAppSide || "user") as AppSide;
    const roles = (userStore.roles || []) as UserRole[];
    if (side === "user") {
      verifyStore.loadStatus();
    }
    navigateAfterAuth(side, roles);
  }
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});
</script>

<style>
/* #ifdef H5 */
@import "@/styles/tailwind.css";
/* #endif */

/* #ifndef H5 */
page {
  background-color: #f8fafc;
  color: #0f172a;
  font-size: 28rpx;
}
view,
text {
  box-sizing: border-box;
}
/* #endif */
</style>
