/** 大类 ID */
export type SceneCategoryId =
  | "job"
  | "mgmt"
  | "speech"
  | "pressure"
  | "communication"
  | "conflict";

/** 大类主题色（占位渐变） */
export type SceneTheme =
  | "blue"
  | "indigo"
  | "orange"
  | "rose"
  | "emerald"
  | "amber";

/** 大类默认图标 */
export type SceneIconType =
  | "mic"
  | "projector"
  | "spark"
  | "pressure"
  | "chat"
  | "conflict";

/** 子场景（对接 API 时挂在大类下） */
export interface SceneSubScene {
  id: string;
  /** 子类卡片标题 */
  title: string;
  /** 核心训练点描述 */
  description: string;
  /** 2-3 个训练 Feature Tags */
  featureTags: string[];
  durationMinutes: number;
  learners: number;
  imageUrl?: string;
}

/** 大类（一级 Tab + 子场景列表） */
export interface SceneCategory {
  id: SceneCategoryId;
  /** Tab 药丸文案 */
  label: string;
  theme: SceneTheme;
  icon: SceneIconType;
  scenes: SceneSubScene[];
}

/** 卡片展示模型（大类 + 子场景合并，供 SceneItem 使用） */
export interface SceneCardData {
  id: string;
  categoryId: SceneCategoryId;
  /** 左上角 Sub-tag，如「职场求职」 */
  categoryLabel: string;
  theme: SceneTheme;
  icon: SceneIconType;
  title: string;
  description: string;
  featureTags: string[];
  durationMinutes: number;
  learners: number;
  imageUrl?: string;
}

/** 场景广场整块数据（嵌套结构，便于后端下发） */
export interface ScenePlazaData {
  sectionTitle: string;
  sectionSubtitle: string;
  categories: SceneCategory[];
}

/** GET /api/scene-plaza 返回的陪练与场景摘要 */
export interface ScenePlazaApiData {
  sectionTitle: string;
  sectionSubtitle: string;
  coachCount: number;
  coaches: import("@/types/coach/hall").Coach[];
  scenes: SceneApiRecord[];
}

export interface SceneApiRecord {
  id: number;
  code: string;
  name: string;
  description?: string;
}
