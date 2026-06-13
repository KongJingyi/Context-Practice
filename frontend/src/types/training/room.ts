/** 训练房间 */

export type ConnectionState = "idle" | "connecting" | "connected" | "failed";

export type FocusPane = "local" | "remote";

export interface LogicHint {
  id: string;
  text: string;
  tone?: "info" | "warn" | "success";
}

export interface WhiteboardDot {
  id: string;
  x: number;
  y: number;
  /** 0–1 相对坐标 */
}

export interface HighlightMarker {
  id: string;
  atMs: number;
  label: string;
}

export interface TrainingRoomContext {
  scenarioId: string;
  roomTitle: string;
  expertName?: string;
}
