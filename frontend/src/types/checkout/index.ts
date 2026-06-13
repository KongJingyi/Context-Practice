/** 收银台 / 支付 */

export type PaymentMethodId = "wechat" | "alipay" | "balance";

export interface PaymentMethodOption {
  id: PaymentMethodId;
  label: string;
  icon: string;
}

export const PAYMENT_METHODS: PaymentMethodOption[] = [
  { id: "wechat", label: "微信支付", icon: "微" },
  { id: "alipay", label: "支付宝", icon: "支" },
  { id: "balance", label: "平台余额", icon: "余" },
];

export interface CouponItem {
  id: string;
  title: string;
  desc: string;
  /** 抵扣金额（元） */
  discountAmount: number;
  /** 满减门槛，0 表示无门槛 */
  minAmount: number;
  expireLabel: string;
}

export interface CheckoutSummary {
  expertId: string;
  expertName: string;
  expertTitle: string;
  expertAvatarUrl?: string;
  sceneTag: string;
  /** 场景编码，写入订单 scene_id */
  sceneCode?: string;
  dateIso: string;
  dateLabel: string;
  timeRange: string;
  slotId: string;
  originalAmount: number;
  sessionMinutes: number;
  /** 待支付订单续付 */
  existingOrderId?: string;
  existingOrderNo?: string;
}

export interface PrepayPayload {
  expertId: string;
  date: string;
  slotId: string;
  couponId?: string;
  paymentMethod: PaymentMethodId;
  amount: number;
  originalAmount?: number;
  sceneCode?: string;
  existingOrderId?: string;
  existingOrderNo?: string;
}

export interface PrepayResult {
  orderId: string;
  orderNo: string;
  prepayId?: string;
}

export interface CalculateTotalInput {
  originalAmount: number;
  couponId?: string;
  coupons: CouponItem[];
}

export interface CalculateTotalResult {
  originalAmount: number;
  discountAmount: number;
  payableAmount: number;
}
