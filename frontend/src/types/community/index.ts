export type PostType = "insight" | "highlight" | "interview";

export interface PostUser {
  name: string;
  avatar?: string;
  medal?: string;
}

export interface PostStats {
  likes: number;
  comments: number;
  collects: number;
}

export interface CommunityPost {
  id: string;
  type: PostType;
  user: PostUser;
  title?: string;
  content: string;
  tags: string[];
  stats: PostStats;
  hasVideo?: boolean;
  videoPreview?: string;
  publishedAt: string;
  /** 面经结构化字段 */
  company?: string;
  role?: string;
  liked?: boolean;
  collected?: boolean;
}

export interface PostComment {
  id: string;
  userName: string;
  content: string;
  createdAt: string;
}

export interface ExpertTip {
  id: string;
  name: string;
  title: string;
  avatar?: string;
  tip: string;
}

export interface FootprintItem {
  id: string;
  date: string;
  title: string;
  type: PostType;
  summary: string;
}

export type FeedFilter = "hot" | "latest" | "highlight";
