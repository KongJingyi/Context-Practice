/** 专家详情（GET /api/expert/:id） */
export interface ExpertDomain {
  id: string;
  title: string;
  summary: string;
}

export interface ExpertReview {
  id: string;
  /** 脱敏展示名，如 w***9、张**三 */
  displayName: string;
  rating: number;
  date: string;
  content: string;
  avatarLetter: string;
}

export interface ExpertDetail {
  id: string;
  name: string;
  jobTitle: string;
  avatarUrl?: string;
  rating: number;
  orderCount: number;
  price: number;
  sessionMinutes: number;
  /** 专家介绍长文 */
  intro: string;
  domains: ExpertDomain[];
  reviews: ExpertReview[];
}

/** 时段（GET slots） */
export interface ExpertTimeSlot {
  id: string;
  label: string;
  booked: boolean;
}

export interface CreateOrderPayload {
  expertId: string;
  date: string;
  slotId: string;
}
