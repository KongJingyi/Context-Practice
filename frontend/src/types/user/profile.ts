export interface UserProfileEditable {
  nickname: string;
  avatarUrl: string;
  /** 训练目标标签 id 列表 */
  trainingGoalIds: string[];
}

export interface TrainingGoalOption {
  id: string;
  label: string;
}

export const TRAINING_GOAL_OPTIONS: TrainingGoalOption[] = [
  { id: "interview", label: "面试突击" },
  { id: "daily", label: "日常积累" },
  { id: "pressure", label: "抗压训练" },
  { id: "report", label: "汇报答辩" },
  { id: "speech", label: "公众演讲" },
  { id: "social", label: "职场社交" },
];
