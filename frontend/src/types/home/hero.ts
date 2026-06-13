/** 主标题文案（第二行中的高亮词单独配置，便于渐变样式） */
export interface HeroTitleContent {
  line1: string;
  line2Prefix: string;
  line2Highlight: string;
}

/** 单条统计数据（对接 API 时按数组下发） */
export interface HeroStatItem {
  id: string;
  value: number | string;
  label: string;
  icon?: "users" | "sessions" | "coaches";
}

/**
 * Hero 文案与按钮（Props 注入，后期可整体替换为接口返回）
 */
export interface HeroContentProps {
  badge?: string;
  title?: HeroTitleContent;
  subtitle?: string;
  primaryButtonText?: string;
}

/**
 * Hero 统计数据
 * - 推荐：`items` 数组（灵活扩展）
 * - 兼容：`learners` / `sessions` / `coaches` 数字字段
 */
export interface HeroStatsCount {
  items?: HeroStatItem[];
  learners?: number;
  sessions?: number;
  coaches?: number;
}
