export type OrderListTab = "active" | "history";
export type OrderApiStatus = "active" | "history";

export type OrderStatus =
  | "pending_pay" | "in_progress" | "pending_review" | "completed"
  | "cancelled" | "refunding" | "refunded" | "closed";

export interface OrderTimelineEvent { id: string; label: string; time: string; done: boolean; current?: boolean; }
export interface RefundProgressStep { key: string; label: string; done: boolean; current?: boolean; }

export interface MyOrderItem {
  id: string; orderNo: string; sceneTag: string; expertName: string; expertTitle: string;
  expertAvatarUrl?: string; dateLabel: string; timeRange: string; isToday?: boolean;
  status: OrderStatus; payExpireAt?: number; amount?: number; expertId?: string;
  dateIso?: string; slotId?: string; sessionMinutes?: number; canEnterRoom?: boolean;
  enterDeniedReason?: string; roomId?: string; trainingStatus?: string | null;
  sceneId?: number; coachId?: number; scheduledStart?: string; scheduledEnd?: string;
  canRefund?: boolean; timeline?: OrderTimelineEvent[]; refundSteps?: RefundProgressStep[];
}

export interface OrderStatusMeta { label: string; color: string; bg: string; border: string; ribbon?: string; }

export const ORDER_STATUS_MAP: Record<OrderStatus, OrderStatusMeta> = {
  pending_pay:  { label: "待支付", color: "#b45309", bg: "#fffbeb", border: "#fcd34d", ribbon: "待支付" },
  in_progress:  { label: "进行中", color: "#1d4ed8", bg: "#eff6ff", border: "#93c5fd", ribbon: "即将开始" },
  pending_review: { label: "待复盘", color: "#7c3aed", bg: "#f5f3ff", border: "#c4b5fd", ribbon: "待复盘" },
  completed:    { label: "已完成", color: "#047857", bg: "#ecfdf5", border: "#6ee7b7" },
  cancelled:    { label: "已取消", color: "#64748b", bg: "#f1f5f9", border: "#cbd5e1" },
  refunding:    { label: "退款中", color: "#c2410c", bg: "#fff7ed", border: "#fdba74", ribbon: "退款中" },
  refunded:     { label: "已退款", color: "#059669", bg: "#ecfdf5", border: "#6ee7b7" },
  closed:       { label: "已关闭", color: "#475569", bg: "#f8fafc", border: "#e2e8f0" },
};

export const CANCEL_REASONS = [
  "时间冲突，无法参加",
  "暂时不想练了",
  "选错专家/时段",
  "其他原因",
] as const;
