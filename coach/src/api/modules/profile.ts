import { request } from "@/api/request";
import { fetchUserMe } from "@/api/modules/auth";

/** 教练个人资料（后端 GET /api/user/profile 实际返回） */
export interface CoachProfile {
  name?: string;
  nickname?: string;
  joinDays?: number;
  rankTag?: string;
  level?: string;
  finishedCourses?: number;
  totalHoursLabel?: string;
  avatarUrl?: string | null;
  tags?: string[];
  goodReviewRate?: number;
}

/** 可编辑的资料字段（后端 GET /api/user/profile/edit 实际返回） */
export interface EditableProfile {
  nickname: string;
  avatarUrl?: string | null;
  trainingGoalIds?: string[];
}

/** 训练报告摘要（后端 GET /api/user/reports/recent 实际返回） */
export interface ReportSummary {
  id: string | number;
  orderId?: number;
  title?: string;
  tutor?: string;
  date?: string;
  score?: number;
  sceneName?: string;
  totalScore?: number;
}

/** 学员评价（后端 GET /api/v1/coaches/{id}/reviews 实际返回） */
export interface CoachReview {
  id?: number;
  user?: string;
  userName?: string;
  score?: number;
  rating?: number;
  content?: string;
  tags?: string[];
  createdAt?: string;
}

/** 获取教练个人主页 */
export async function fetchCoachProfile(): Promise<CoachProfile> {
  try {
    return (await request({
      url: "/user/profile",
      method: "GET",
      silent: true,
    })) as CoachProfile;
  } catch {
    if (import.meta.env.DEV) {
      return {
        name: "李教练",
        nickname: "李教练",
        joinDays: 15,
        rankTag: "进阶表达者",
        level: "Lv.8",
        finishedCourses: 128,
        totalHoursLabel: "256h",
        tags: ["压力面试", "商业汇报", "逻辑表达"],
        goodReviewRate: 0.96,
      };
    }
    throw new Error("无法加载个人资料");
  }
}

/** 获取可编辑的个人资料 */
export async function fetchEditableProfile(): Promise<EditableProfile> {
  return (await request({
    url: "/user/profile/edit",
    method: "GET",
    silent: true,
  })) as EditableProfile;
}

/** 更新个人资料 */
export async function updateCoachProfile(body: Record<string, unknown>): Promise<{ ok: boolean }> {
  return (await request({
    url: "/user/update",
    method: "PUT",
    data: body,
  })) as { ok: boolean };
}

/** 获取训练历史报告列表 */
export async function fetchRecentReports(): Promise<ReportSummary[]> {
  try {
    return (await request({
      url: "/user/reports/recent",
      method: "GET",
      silent: true,
    })) as ReportSummary[];
  } catch {
    if (import.meta.env.DEV) {
      return [
        { id: 1, orderId: 3, sceneName: "压力面试", date: "2026-06-06", totalScore: 85 },
        { id: 2, orderId: 5, sceneName: "管理汇报", date: "2026-06-04", totalScore: 82 },
      ];
    }
    return [];
  }
}

/** 获取学员评价列表（当前登录陪练） */
export async function fetchCoachReviews(coachId?: number): Promise<CoachReview[]> {
  let id = coachId;
  if (!id) {
    const me = await fetchUserMe();
    id = me.userId;
  }
  return (await request({
    url: `/v1/coaches/${id}/reviews`,
    method: "GET",
    silent: true,
  })) as CoachReview[];
}

/** 回复学员评价 POST /api/v1/coach/ratings/{id}/reply */
export async function replyCoachRating(ratingId: number, reply: string): Promise<{ ok: boolean }> {
  return (await request({
    url: `/v1/coach/ratings/${ratingId}/reply`,
    method: "POST",
    data: { reply },
  })) as { ok: boolean };
}

/** 申诉恶意评价 POST /api/v1/coach/ratings/{id}/appeal */
export async function appealCoachRating(ratingId: number, reason: string): Promise<{ ok: boolean }> {
  return (await request({
    url: `/v1/coach/ratings/${ratingId}/appeal`,
    method: "POST",
    data: { reason },
  })) as { ok: boolean };
}
