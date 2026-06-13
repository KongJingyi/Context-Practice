/** 订单 */

export type OrderListTab = "active" | "history";

export type OrderApiStatus = "active" | "history";

export type OrderStatus =
  | "pending_pay"
  | "in_progress"
  | "pending_review"
  | "completed"
  | "cancelled"
  | "refunding"
  | "refunded"
  | "expired"
  | "closed";

export interface OrderTimelineEvent {
  id: string;
  label: string;
  time: string;
  done: boolean;
  current?: boolean;
}

export interface RefundProgressStep {
  key: string;
  label: string;
  done: boolean;
  current?: boolean;
}

export interface MyOrderItem {
  id: string;
  orderNo: string;
  sceneTag: string;
  expertName: string;
  expertTitle: string;
  expertAvatarUrl?: string;
  dateLabel: string;
  timeRange: string;
  isToday?: boolean;
  status: OrderStatus;
  /** 待支付截止 Unix 毫秒 */
  payExpireAt?: number;
  amount?: number;
  expertId?: string;
  dateIso?: string;
  slotId?: string;
  sessionMinutes?: number;
  /** 是否可进入训练间 */
  canEnterRoom?: boolean;
  /** canEnterRoom=false 时后端原因文案 */
  enterDeniedReason?: string;
  /** TRTC 房间号（训练开始后由后端下发） */
  roomId?: string;
  /** API: IN_PROGRESS | ENDED | REPORT_READY */
  trainingStatus?: string | null;
  sceneId?: number;
  coachId?: number;
  scheduledStart?: string;
  scheduledEnd?: string;
  /** 是否可申请退款（未开始） */
  canRefund?: boolean;
  /** 是否已提交训练评价 */
  hasRated?: boolean;
  /** 是否可进入评价页 */
  canReview?: boolean;
  /** 训练报告是否已生成 */
  reportReady?: boolean;
  /** 右上角角标（后端按时间计算） */
  ribbonLabel?: string;
  /** UPCOMING | ENTERABLE | IN_TRAINING | EXPIRED 等 */
  displayPhase?: string;
  /** 是否可取消 */
  canCancel?: boolean;
  /** 是否已失效（预约结束未进房） */
  expired?: boolean;
  /** 训练时段已结束（超时归档） */
  sessionEnded?: boolean;
  timeline?: OrderTimelineEvent[];
  refundSteps?: RefundProgressStep[];
}

export interface OrderStatusMeta {
  label: string;
  color: string;
  bg: string;
  border: string;
  ribbon?: string;
}

export const ORDER_STATUS_MAP: Record<OrderStatus, OrderStatusMeta> = {
  pending_pay: {
    label: "待支付",
    color: "#b45309",
    bg: "#fffbeb",
    border: "#fcd34d",
    ribbon: "待支付",
  },
  in_progress: {
    label: "进行中",
    color: "#1d4ed8",
    bg: "#eff6ff",
    border: "#93c5fd",
  },
  expired: {
    label: "已失效",
    color: "#64748b",
    bg: "#f8fafc",
    border: "#cbd5e1",
    ribbon: "已失效",
  },
  pending_review: {
    label: "待复盘",
    color: "#7c3aed",
    bg: "#f5f3ff",
    border: "#c4b5fd",
    ribbon: "待复盘",
  },
  completed: {
    label: "已完成",
    color: "#047857",
    bg: "#ecfdf5",
    border: "#6ee7b7",
  },
  cancelled: {
    label: "已取消",
    color: "#64748b",
    bg: "#f1f5f9",
    border: "#cbd5e1",
  },
  refunding: {
    label: "退款中",
    color: "#c2410c",
    bg: "#fff7ed",
    border: "#fdba74",
    ribbon: "退款中",
  },
  refunded: {
    label: "已退款",
    color: "#059669",
    bg: "#ecfdf5",
    border: "#6ee7b7",
  },
  closed: {
    label: "已关闭",
    color: "#475569",
    bg: "#f8fafc",
    border: "#e2e8f0",
  },
};

export const CANCEL_REASONS = [
  "时间冲突，无法参加",
  "暂时不想练了",
  "选错专家/时段",
  "其他原因",
] as const;
