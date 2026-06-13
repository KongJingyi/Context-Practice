import type { AppSide, UserRole } from "@/types/auth";

const COACH_PORTAL = "/pages/auth/coach-portal";
const USER_HOME = "/pages/index/index";

export function roleLabel(side: AppSide): string {
  if (side === "coach") return "陪练端";
  if (side === "admin") return "管理端";
  return "学员端";
}

export function roleMatchesSide(side: AppSide, roles: UserRole[]): boolean {
  if (!roles.length) return true;
  if (side === "admin") return roles.includes("ADMIN");
  if (side === "coach") return roles.includes("COACH") || roles.includes("ADMIN");
  return roles.includes("USER") || roles.includes("ADMIN");
}

export function navigateAfterAuth(side: AppSide, roles: UserRole[]) {
  const token = uni.getStorageSync("token") || "";
  if (side === "admin") {
    const adminBase = import.meta.env.VITE_ADMIN_BASE || "http://localhost:5176";
    // #ifdef H5
    window.location.href = `${adminBase}/#/dashboard?token=${encodeURIComponent(token)}`;
    return;
    // #endif
    uni.showToast({ title: "管理后台请在 PC 浏览器打开", icon: "none" });
    return;
  }
  if (side === "coach") {
    const coachBase = import.meta.env.VITE_COACH_BASE || "http://localhost:5175";
    // #ifdef H5
    window.location.href = `${coachBase}/#/orders?token=${encodeURIComponent(token)}`;
    return;
    // #endif
    uni.reLaunch({ url: COACH_PORTAL });
    return;
  }
  uni.switchTab({ url: USER_HOME });
}

export function showRoleMismatchToast(side: AppSide) {
  const msg =
    side === "admin"
      ? "当前账号不是管理员，请使用管理员账号登录"
      : side === "coach"
        ? "当前账号不是陪练身份，请使用学员端登录或联系管理员"
        : "当前账号为陪练身份，请切换到陪练端登录";
  uni.showToast({ title: msg, icon: "none", duration: 2800 });
}

export function mockRolesForSide(side: AppSide): UserRole[] {
  if (side === "admin") return ["ADMIN"];
  return side === "coach" ? ["COACH"] : ["USER"];
}

/** 从 URL 读取登录端偏好 ?side=admin|coach|user */
export function parseSideFromQuery(): AppSide | null {
  // #ifdef H5
  try {
    const hash = window.location.hash || "";
    const q = hash.includes("?") ? hash.split("?")[1] : window.location.search.slice(1);
    const side = new URLSearchParams(q).get("side");
    if (side === "admin" || side === "coach" || side === "user") return side;
  } catch {
    /* ignore */
  }
  // #endif
  return null;
}
