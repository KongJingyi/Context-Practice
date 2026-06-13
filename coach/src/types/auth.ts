/** 登录页选择的端（仅 UI / 跳转，不传后端 role 参数） */
export type AppSide = "user" | "coach";

export type UserRole = "USER" | "COACH" | "ADMIN";

export interface AuthUserInfo {
  nickname?: string;
  username?: string;
  userId?: number;
  avatar?: string;
}

export interface LoginSessionPayload {
  token: string;
  roles: UserRole[];
  user?: AuthUserInfo;
  appSide?: AppSide;
}
