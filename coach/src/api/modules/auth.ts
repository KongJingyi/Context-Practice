import { request } from "@/api/request";
import { useUserStore } from "@/store/user";

/** 与后端 AuthController#login 对齐：POST /api/auth/login { username } */
export async function loginByUsername(username: string) {
  return request({
    url: "/auth/login",
    method: "POST",
    data: { username },
  }) as Promise<{
    token: string;
    username: string;
    userId: number;
    roles: string[];
  }>;
}

/** 登录并保存完整 session（dev mock 回退），所有端统一入口 */
export async function loginAndStoreToken(username: string) {
  let res: { token: string; username: string; userId: number; roles: string[] };

  try {
    res = await loginByUsername(username);
  } catch {
    if (import.meta.env.DEV) {
      // 后端未启动时 mock 陪练身份登录
      res = {
        token: `mock-coach-token-${Date.now()}`,
        username,
        userId: 2,
        roles: ["COACH"],
      };
    } else {
      throw new Error("登录失败，请检查后端服务是否运行");
    }
  }

  // 与主前端 enterHomeAfterAuth / setAuthSession 对齐
  const userStore = useUserStore();
  userStore.setAuthSession({
    token: res.token,
    user: { username: res.username, userId: res.userId },
    roles: (res.roles as import("@/types/auth").UserRole[]) ?? [],
    appSide: "coach",
  });

  return res;
}

/** 获取当前用户信息。后端：GET /api/v1/user/me */
export async function fetchUserMe() {
  return request({ url: "/v1/user/me", method: "GET" }) as Promise<{
    userId: number;
    username: string;
    nickname: string;
    avatar?: string;
    roles: string[];
    phone?: string;
  }>;
}
