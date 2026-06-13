export interface QuestionCategory {
  id: string;
  title: string;
  subtitle: string;
  icon: string;
  gradient: [string, string];
  todayCount: number;
  span: "wide" | "tall" | "normal";
}

export interface PracticeQuestion {
  id: number;
  text: string;
  category: string;
  categoryId: string;
}

export interface VoiceRadarScores {
  logic: number;
  speed: number;
  fluency: number;
  emotion: number;
  vocabulary: number;
}

export interface VoiceAnalysisResult {
  score: number;
  radar: VoiceRadarScores;
  suggestions: string[];
  metrics: {
    speedWpm: number;
    pauseCount: number;
    clarity: number;
  };
}

export interface TextChange {
  offset: number;
  length: number;
  reason: string;
}

export interface TextOptimizeResult {
  original: string;
  optimized: string;
  changes: TextChange[];
}

export type PracticeLabTab = "square" | "voice" | "text";

export interface PracticeEntryPayload {
  tab: PracticeLabTab;
  questionId?: number;
  questionText?: string;
}
