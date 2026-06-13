/** 个人主页 / 用户看板 */

export interface UserDashboardProfile {
  name: string;
  joinDays: number;
  rankTag: string;
  level: string;
  finishedCourses: number;
  totalHoursLabel: string;
  avatarUrl?: string;
}

export type GrowthPeriod = "week" | "month" | "year";

export interface GrowthChartData {
  asOfDate: string;
  xLabels: string[];
  values: number[];
}

export interface GrowthChartByPeriod {
  week: GrowthChartData;
  month: GrowthChartData;
  year: GrowthChartData;
}

export interface AbilityTagItem {
  text: string;
  /** 0–1，越大字号与色深越高 */
  weight: number;
}

export interface CompositeScoreData {
  score: number;
  hint?: string;
}

export interface RecentReportItem {
  id: string;
  title: string;
  date: string;
  tutor: string;
  score: number;
}

export interface MedalItem {
  id: string;
  name: string;
  icon: "shield" | "check" | "star";
  tone: "blue" | "orange" | "purple";
  earned: boolean;
}

export type DashboardMenuId =
  | "orders"
  | "favorites"
  | "verify"
  | "settings"
  | "logout"
  | "login";
