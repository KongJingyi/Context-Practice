import type { AppSide } from "@/types/auth";

/** 构建时固定端：user | coach；未设置时 H5 可在登录页切换 */
export function getBuildAppSide(): AppSide | "" {
  const v = import.meta.env.VITE_APP_SIDE;
  if (v === "coach" || v === "user") return v;
  return "";
}

/** 微信小程序仅学员端，不展示陪练切换 */
export function showRoleSwitcher(): boolean {
  if (getBuildAppSide()) return false;
  // #ifdef MP-WEIXIN
  return false;
  // #endif
  return true;
}

export function getDefaultAppSide(): AppSide {
  const build = getBuildAppSide();
  if (build === "coach") return "coach";
  return "user";
}

export function isCoachBuild(): boolean {
  return getBuildAppSide() === "coach";
}

/** 仅微信小程序：陪练不可注册；H5/PC 陪练端可正常入驻 */
export function isCoachRegisterBlocked(): boolean {
  // #ifdef MP-WEIXIN
  return true;
  // #endif
  return false;
}
