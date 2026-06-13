export type ReviewDimension = "professional" | "attitude" | "quality";

export interface ReviewScores {
  professional: number;
  attitude: number;
  quality: number;
}

export interface ReviewSubmitPayload {
  orderId: string;
  expertId: string;
  scores: ReviewScores;
  tags: string[];
  content: string;
  isAnonymous: boolean;
}

export interface ReviewContext {
  orderId: string;
  orderNo?: string;
  expertId: string;
  expertName: string;
  expertTitle?: string;
  sceneTag?: string;
}

export type ComplaintType =
  | "attitude_issue"
  | "late_no_show"
  | "perfunctory"
  | "privacy_violation"
  | "other";

export interface ComplaintSubmitPayload {
  orderId: string;
  type: ComplaintType;
  description: string;
  attachments: string[];
  isAnonymous: boolean;
}

export interface ComplaintStep {
  title: string;
  time?: string;
  content?: string;
  done: boolean;
  current?: boolean;
}

export interface CoachReviewSnippet {
  id: string;
  userName: string;
  content: string;
  tags: string[];
  score: number;
  date: string;
}
