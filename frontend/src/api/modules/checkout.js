/**
 * 收银台
 * GET  /api/coupons/available
 * POST /api/v1/orders/mock-pay  — 文档 Mock 支付（续付待支付订单）
 * POST /api/order/prepay         — 创建订单并 Mock 支付
 */
import { request } from "@/api/request.js";
import { markOrderPaid, mockPayOrder } from "@/api/modules/orders.js";

/**
 * @param {number} amount 订单原价
 * @returns {Promise<import('@/types/checkout').CouponItem[]>}
 */
export async function fetchAvailableCoupons(amount) {
  try {
    const data = await request({
      url: "/coupons/available",
      method: "GET",
      data: { amount },
    });
    if (Array.isArray(data)) {
      return /** @type {import('@/types/checkout').CouponItem[]} */ (data);
    }
  } catch {
    /* mock */
  }
  return [
    {
      id: "cp-20",
      title: "新人体验券",
      desc: "首单立减 ¥20",
      discountAmount: 20,
      minAmount: 0,
      expireLabel: "7 天内有效",
    },
    {
      id: "cp-50",
      title: "进阶训练券",
      desc: "满 ¥200 减 ¥50",
      discountAmount: 50,
      minAmount: 200,
      expireLabel: "本月有效",
    },
    {
      id: "cp-30",
      title: "周末专享",
      desc: "满 ¥150 减 ¥30",
      discountAmount: 30,
      minAmount: 150,
      expireLabel: "限周六日",
    },
  ].filter((c) => amount >= c.minAmount || c.minAmount === 0);
}

/**
 * @param {import('@/types/checkout').CalculateTotalInput} input
 * @returns {import('@/types/checkout').CalculateTotalResult}
 */
export function calculateTotal(input) {
  const { originalAmount, couponId, coupons } = input;
  let discountAmount = 0;
  if (couponId) {
    const c = coupons.find((x) => x.id === couponId);
    if (c && originalAmount >= c.minAmount) {
      discountAmount = Math.min(c.discountAmount, originalAmount);
    }
  }
  const payableAmount = Math.max(0, Math.round((originalAmount - discountAmount) * 100) / 100);
  return { originalAmount, discountAmount, payableAmount };
}

/**
 * @param {unknown} err
 */
function prepayErrorMessage(err) {
  if (err && typeof err === "object" && "message" in err && typeof err.message === "string") {
    return err.message;
  }
  return "支付失败，请稍后重试";
}

/**
 * @param {import('@/types/checkout').PrepayPayload} payload
 * @returns {Promise<import('@/types/checkout').PrepayResult>}
 */
export async function prepayOrder(payload) {
  const orderId = payload.existingOrderId;
  if (orderId) {
    await mockPayOrder(orderId);
    return {
      orderId,
      orderNo: payload.existingOrderNo ?? `EF${orderId}`,
      prepayId: `mock-pay-${Date.now()}`,
    };
  }

  const data = await request({
    url: "/order/prepay",
    method: "POST",
    data: payload,
  }).catch((err) => {
    throw new Error(prepayErrorMessage(err));
  });

  if (!data || !data.orderId) {
    throw new Error("支付未完成，未获取到订单号");
  }

  markOrderPaid(String(data.orderId));
  return /** @type {import('@/types/checkout').PrepayResult} */ (data);
}
