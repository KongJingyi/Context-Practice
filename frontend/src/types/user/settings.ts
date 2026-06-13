export type FavoriteTab = "experts" | "scenes";

export interface FavoriteExpert {
  id: string;
  name: string;
  avatar?: string;
  tags: string[];
  jobTitle?: string;
}

export interface FavoriteScene {
  id: string;
  title: string;
  icon: string;
  categoryLabel?: string;
  lastPracticeLabel?: string;
}

export type AccountSettingsTab = "security" | "privacy" | "notifications" | "general";

export interface PrivacySettings {
  shareReportWithExpert: boolean;
  anonymousCommunity: boolean;
}

export interface NotificationSettings {
  smsOnBook: boolean;
  remindBeforeTraining: boolean;
  expertFeedbackNotify: boolean;
}

export interface SecurityInfo {
  phoneMasked: string;
}
