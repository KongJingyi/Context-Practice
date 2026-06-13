import { request } from "@/api/request";

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
  // mock fallback
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
  // old field aliases for view compatibility
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
}

/** 获取可编辑的个人资料 */
export async function fetchEditableProfile(): Promise<EditableProfile> {
  try {
    return (await request({
      url: "/user/profile/edit",
      method: "GET",
      silent: true,
    })) as EditableProfile;
  } catch {
    return { nickname: "李教练" };
  }
}

/** 更新个人资料 */
export async function updateCoachProfile(body: Record<string, unknown>): Promise<{ ok: boolean }> {
  try {
    return (await request({
      url: "/user/update",
      method: "PUT",
      data: body,
    })) as { ok: boolean };
  } catch {
    return { ok: true };
  }
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
    return [
      { id: 1, orderId: 3, sceneName: "压力面试", date: "2026-06-06", totalScore: 85 },
      { id: 2, orderId: 5, sceneName: "管理汇报", date: "2026-06-04", totalScore: 82 },
      { id: 3, orderId: 8, sceneName: "客户谈判", date: "2026-06-01", totalScore: 90 },
    ];
  }
}

/** 获取学员评价列表 */
export async function fetchCoachReviews(coachId?: number): Promise<CoachReview[]> {
  const id = coachId || 2;
  try {
    return (await request({
      url: `/v1/coaches/${id}/reviews`,
      method: "GET",
      silent: true,
    })) as CoachReview[];
  } catch {
    return [
      { id: 1, userName: "张同学", rating: 5, content: "李教练的点评一针见血，帮我快速找到了表达上的盲区", tags: ["专业", "耐心"], createdAt: "2026-06-05" },
      { id: 2, userName: "王同学", rating: 5, content: "压力面试模拟非常逼真，训练后的反馈报告很有用", tags: ["高效", "实战"], createdAt: "2026-06-03" },
      { id: 3, userName: "陈同学", rating: 4, content: "整体体验很好，希望增加更多行业场景", tags: ["用心"], createdAt: "2026-05-28" },
    ];
  }
}
