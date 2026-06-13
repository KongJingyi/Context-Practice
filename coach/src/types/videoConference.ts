export type ApiRole = "USER" | "COACH" | "ADMIN";

export type ApiOrderStatus =
  | "PENDING_PAY" | "PAID" | "IN_SERVICE" | "COMPLETED"
  | "CANCELLED" | "REFUNDING" | "REFUNDED";

export type ApiTrainingStatus = "IN_PROGRESS" | "ENDED" | "REPORT_READY" | null;

export type LeaveReason = "USER_HANGUP" | "COACH_HANGUP" | "NETWORK_ERROR" | "PAGE_UNLOAD";

export interface StartTrainingPayload { orderId: number | string; scenarioCode: string; }
export interface StartTrainingResult { roomId: string; trainingId: number; orderId: number; startedAt: string; }

export interface JoinInfoPeer { userId: number; trtcUserId: string; nickname: string; avatar?: string; }
export interface JoinInfoParticipant { role: ApiRole; userId: number; joined: boolean; joinedAt: string | null; }

export interface RoomJoinInfo {
  roomId: string; trainingId: number; orderId: number; trainingStatus: ApiTrainingStatus;
  sdkAppId: number; trtcUserId: string; userSig: string; role: ApiRole;
  peer: JoinInfoPeer; sceneName: string; scheduledStart: string; scheduledEnd: string;
  startedAt: string; serverTime: string; canEnter: boolean; denyReason: string | null;
  participants: JoinInfoParticipant[];
}

export interface RoomJoinRecord { joinedAt: string; role: ApiRole; }
export interface RoomLeaveRecord { leftAt: string; }

export interface RoomStatus {
  roomId: string; trainingId: number; orderId: number; trainingStatus: ApiTrainingStatus;
  orderStatus: ApiOrderStatus; startedAt: string; endedAt: string | null;
  durationSeconds: number; serverTime: string; participants: JoinInfoParticipant[];
}

export interface EndRoomPayload { transcript?: string; sceneName?: string; }
export interface EndRoomResult { trainingId: number; endedAt: string; durationSeconds: number; orderStatus: ApiOrderStatus; reportReady: boolean; }
export interface EndTrainingPayload { roomId: string; transcript?: string; sceneName?: string; }
export interface EndTrainingResult { report: string; }
export interface FetchTrainingReportPayload { trainingRecordId: number; }
export interface FetchTrainingReportResult { report: string; }
export interface CreateOrderApiPayload { productId: number; coachId: number; sceneId: number; amount: number; }
export interface CreateOrderApiResult { orderId: number; }
export interface MockPayPayload { orderId: number | string; }

export interface OrderApiRecord {
  orderId: number; status: ApiOrderStatus; amount: number; coachId: number;
  coachName: string; coachAvatar?: string; sceneId: number; sceneName: string;
  scheduledStart: string; scheduledEnd: string; roomId: string | null;
  trainingStatus: ApiTrainingStatus; canEnterRoom: boolean; enterDeniedReason?: string | null;
  userId?: number; userName?: string; userAvatar?: string; trainingStartedAt?: string | null;
  trainingGoal?: string; userBackground?: string; trainingId?: number | null;
  reportReady?: boolean; coachFeedbackPending?: boolean;
}

export interface OrderListApiResult { records: OrderApiRecord[]; total: number; size: number; current: number; pages: number; }
export interface LoginApiPayload { username: string; }
export interface LoginApiResult { token: string; username: string; userId: number; roles: ApiRole[]; }
export interface UserMeResult { userId: number; username: string; nickname: string; avatar?: string; roles: ApiRole[]; phone?: string; }
export interface SceneApiRecord { id: number; name: string; description?: string; status: number; }

// Coach-specific types
export interface CoachOrderRecord {
  orderId: number; status: ApiOrderStatus; userId: number; userName: string;
  userAvatar?: string; sceneName: string; trainingGoal?: string;
  userBackground?: string; trainingId?: number | null;
  scheduledStart: string; scheduledEnd: string; roomId: string | null;
  trainingStatus: ApiTrainingStatus; canEnterRoom: boolean;
  reportReady?: boolean; coachFeedbackPending?: boolean;
}

export interface CoachDashboard {
  totalSessions: number; totalHours: number; goodReviewRate: number;
  levelName: string; levelProgress: number; monthIncome: number;
  lastTrainingId?: number | null;
}
