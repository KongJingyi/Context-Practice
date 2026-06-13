import type { SceneCategoryId, SceneTheme } from "@/types/scene/plaza";

export type CoachLevel = "gold" | "senior" | "rookie";

export type CoachPriceRange = "all" | "0-100" | "100-300" | "300+";

export type CoachSortBy = "rating" | "activity";

/** 擅长标签 id（对接 GET /coaches?tags[]=） */
export type CoachSpecialtyTagId =
  | "pressure"
  | "logic"
  | "star"
  | "group"
  | "speech"
  | "report"
  | "social"
  | "negotiate";

export interface CoachSpecialtyOption {
  id: CoachSpecialtyTagId;
  label: string;
}

export const COACH_SPECIALTY_OPTIONS: CoachSpecialtyOption[] = [
  { id: "pressure", label: "压力面试" },
  { id: "logic", label: "逻辑追问" },
  { id: "star", label: "STAR 法则" },
  { id: "group", label: "群面控场" },
  { id: "speech", label: "公众演讲" },
  { id: "report", label: "汇报答辩" },
  { id: "social", label: "职场社交" },
  { id: "negotiate", label: "谈判沟通" },
];

/** 五维筛选参数（对接 GET /api/coaches） */
export interface CoachFilterParams {
  sceneId: string;
  subSceneName: string;
  levelMin: number;
  levelMax: number;
  tags: CoachSpecialtyTagId[];
  minStars: number;
  todayOnly: boolean;
  priceRange: CoachPriceRange;
  sortBy: CoachSortBy;
}

export function createDefaultFilterParams(
  sceneId = "",
  subSceneName = "",
): CoachFilterParams {
  return {
    sceneId,
    subSceneName,
    levelMin: 1,
    levelMax: 5,
    tags: [],
    minStars: 0,
    todayOnly: false,
    priceRange: "all",
    sortBy: "rating",
  };
}

/** @deprecated 使用 CoachFilterParams */
export interface CoachFilterState extends CoachFilterParams {
  level?: CoachLevel | "all";
}

export interface Coach {
  id: string;
  name: string;
  jobTitle: string;
  avatarUrl?: string;
  online: boolean;
  /** 今日可约（日历状态） */
  availableToday: boolean;
  rating: number;
  orderCount: number;
  skillTags: string[];
  specialtyIds: CoachSpecialtyTagId[];
  price: number;
  sessionMinutes: number;
  /** Lv.1 ~ Lv.5 */
  levelNum: number;
  level: CoachLevel;
  activityScore: number;
  categoryId: SceneCategoryId;
  theme: SceneTheme;
  bio: string;
  highlights: string[];
}

export interface RatingDistributionItem {
  stars: 5 | 4 | 3 | 2 | 1;
  count: number;
}

export interface CoachCertificate {
  id: string;
  title: string;
  imageUrl: string;
}

export interface CoachSuccessStory {
  id: string;
  title: string;
  subtitle: string;
  metric: string;
}

export interface CoachRadarDimension {
  key: string;
  label: string;
  value: number;
}

export interface CoachDetail extends Coach {
  ratingDistribution: RatingDistributionItem[];
  certificates: CoachCertificate[];
  successStories: CoachSuccessStory[];
  radar: CoachRadarDimension[];
}

export interface SmartMatchItem {
  coach: Coach;
  matchPercent: number;
  reason: string;
  highlightTag?: string;
}

export interface SmartMatchResult {
  items: SmartMatchItem[];
}

export interface CoachHallSceneOption {
  id: string;
  label: string;
  categoryId: SceneCategoryId;
  categoryLabel: string;
}

export interface CoachHallHeaderProps {
  categoryLabel: string;
  subSceneName: string;
  guide: string;
}
