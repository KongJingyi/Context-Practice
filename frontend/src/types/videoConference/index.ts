/** 1v1 视频会议 API（api-v1-video-conference.md） */

export type ApiRole = "USER" | "COACH" | "ADMIN";

export type ApiOrderStatus =
  | "PENDING_PAY"
  | "PAID"
  | "IN_SERVICE"
  | "COMPLETED"
  | "CANCELLED"
  | "REFUNDING"
  | "REFUNDED";

export type ApiTrainingStatus = "IN_PROGRESS" | "ENDED" | "REPORT_READY" | null;

export type LeaveReason =
  | "USER_HANGUP"
  | "COACH_HANGUP"
  | "NETWORK_ERROR"
  | "PAGE_UNLOAD";

/** POST /api/v1/training/start */
export interface StartTrainingPayload {
  orderId: number | string;
  scenarioCode: string;
}

export interface StartTrainingResult {
  roomId: string;
  trainingId: number;
  orderId: number;
  startedAt: string;
}

export interface JoinInfoPeer {
  userId: number;
  trtcUserId: string;
  nickname: string;
  avatar?: string;
}

export interface JoinInfoParticipant {
  role: ApiRole;
  userId: number;
  joined: boolean;
  joinedAt: string | null;
}

/** GET /api/v1/rooms/{roomId}/join-info */
export interface RoomJoinInfo {
  roomId: string;
  trainingId: number;
  orderId: number;
  trainingStatus: ApiTrainingStatus;
  sdkAppId: number;
  trtcUserId: string;
  userSig: string;
  role: ApiRole;
  peer: JoinInfoPeer;
  sceneName: string;
  scheduledStart: string;
  scheduledEnd: string;
  startedAt: string;
  serverTime: string;
  canEnter: boolean;
  denyReason: string | null;
  participants: JoinInfoParticipant[];
}

export interface RoomJoinRecord {
  joinedAt: string;
  role: ApiRole;
}

export interface RoomLeaveRecord {
  leftAt: string;
}

/** GET /api/v1/rooms/{roomId} */
export interface RoomStatus {
  roomId: string;
  trainingId: number;
  orderId: number;
  trainingStatus: ApiTrainingStatus;
  orderStatus: ApiOrderStatus;
  startedAt: string;
  endedAt: string | null;
  durationSeconds: number;
  serverTime: string;
  participants: JoinInfoParticipant[];
}

/** POST /api/v1/rooms/{roomId}/end（陪练端） */
export interface EndRoomPayload {
  transcript?: string;
  sceneName?: string;
}

export interface EndRoomResult {
  trainingId: number;
  endedAt: string;
  durationSeconds: number;
  orderStatus: ApiOrderStatus;
  reportReady: boolean;
}

/** POST /api/v1/training/end */
export interface EndTrainingPayload {
  roomId: string;
  transcript?: string;
  sceneName?: string;
}

export interface EndTrainingResult {
  report: string;
}

/** POST /api/v1/training/report */
export interface FetchTrainingReportPayload {
  trainingRecordId: number;
}

export interface FetchTrainingReportResult {
  report: string;
}

/** POST /api/v1/orders */
export interface CreateOrderApiPayload {
  productId: number;
  coachId: number;
  sceneId: number;
  amount: number;
}

export interface CreateOrderApiResult {
  orderId: number;
}

/** POST /api/v1/orders/mock-pay */
export interface MockPayPayload {
  orderId: number | string;
}

/** GET /api/v1/orders 列表项（API 原始） */
export interface OrderApiRecord {
  orderId: number;
  status: ApiOrderStatus;
  amount: number;
  coachId: number;
  coachName: string;
  coachAvatar?: string;
  sceneId: number;
  sceneName: string;
  scheduledStart: string;
  scheduledEnd: string;
  roomId: string | null;
  trainingStatus: ApiTrainingStatus;
  canEnterRoom: boolean;
  enterDeniedReason?: string | null;
  userId?: number;
  userName?: string;
  userAvatar?: string;
  trainingStartedAt?: string | null;
  hasRated?: boolean;
  canReview?: boolean;
  reportReady?: boolean;
}

export interface OrderListApiResult {
  records: OrderApiRecord[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/** POST /api/auth/login */
export interface LoginApiPayload {
  username: string;
}

export interface LoginApiResult {
  token: string;
  username: string;
  userId: number;
  roles: ApiRole[];
}

/** GET /api/v1/user/me */
export interface UserMeResult {
  userId: number;
  username: string;
  nickname: string;
  avatar?: string;
  roles: ApiRole[];
  phone?: string;
}

/** GET /api/scenes 分页 record */
export interface SceneApiRecord {
  id: number;
  name: string;
  description?: string;
  status: number;
}
