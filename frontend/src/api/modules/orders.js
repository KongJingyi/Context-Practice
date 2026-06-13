/**
 * 我的订单（对齐 api-v1-video-conference.md）
 * GET  /api/v1/orders?status=&page=&size=
 * GET  /api/v1/orders/{orderId}
 * POST /api/v1/orders
 * POST /api/v1/orders/mock-pay
 * POST /api/orders/:id/cancel  — 取消（文档未列，保留旧路径兜底）
 * POST /api/orders/:id/refund
 */
import { request } from "@/api/request.js";
import {
  normalizeOrderListResponse,
  mapOrderRecordToItem,
} from "@/api/modules/orderMapper.js";

const PAY_WINDOW_MS = 15 * 60 * 1000;
const MOCK_STATE_KEY = "ctx_order_mock_state";

/**
 * @returns {{ paid: string[]; cancelled: string[]; refunding: string[] }}
 */
function loadMockState() {
  try {
    const raw = uni.getStorageSync(MOCK_STATE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      return {
        paid: parsed.paid ?? [],
        cancelled: parsed.cancelled ?? [],
        refunding: parsed.refunding ?? [],
      };
    }
  } catch {
    /* ignore */
  }
  return { paid: [], cancelled: [], refunding: [] };
}

function saveMockState(state) {
  uni.setStorageSync(MOCK_STATE_KEY, JSON.stringify(state));
}

/** 支付成功后标记（mock 持久化） */
export function markOrderPaid(orderId) {
  const state = loadMockState();
  if (!state.paid.includes(orderId)) state.paid.push(orderId);
  saveMockState(state);
}

function markOrderCancelled(orderId) {
  const state = loadMockState();
  if (!state.cancelled.includes(orderId)) state.cancelled.push(orderId);
  saveMockState(state);
}

function markOrderRefunding(orderId) {
  const state = loadMockState();
  if (!state.refunding.includes(orderId)) state.refunding.push(orderId);
  saveMockState(state);
}

/**
 * @param {import('@/types/orders').MyOrderItem[]} list
 * @param {'active' | 'history'} tab
 */
function applyMockMutations(list, tab) {
  const state = loadMockState();
  const paidSet = new Set(state.paid);
  const cancelledSet = new Set(state.cancelled);
  const refundingSet = new Set(state.refunding);

  const mutated = list.map((item) => {
    let next = { ...item };

    if (paidSet.has(item.id) && next.status === "pending_pay") {
      next = {
        ...next,
        status: "in_progress",
        payExpireAt: undefined,
        canEnterRoom: true,
        canRefund: true,
        timeline: [
          { id: "t1", label: "提交订单", time: "已完成", done: true },
          { id: "t2", label: "支付成功", time: "刚刚", done: true },
          { id: "t3", label: "待训练", time: next.timeRange, done: false, current: true },
          { id: "t4", label: "评价", time: "—", done: false },
        ],
      };
    }

    if (cancelledSet.has(item.id)) {
      next = { ...next, status: "cancelled", payExpireAt: undefined, canEnterRoom: false, canRefund: false };
    }

    if (refundingSet.has(item.id) && next.status === "in_progress") {
      next = {
        ...next,
        status: "refunding",
        canEnterRoom: false,
        canRefund: false,
        refundSteps: [
          { key: "apply", label: "提交申请", done: true },
          { key: "audit", label: "平台审核", done: true, current: true },
          { key: "done", label: "退款到账", done: false },
        ],
      };
    }

    return next;
  });

  if (tab === "active") {
    return mutated.filter((item) => item.status !== "cancelled" && item.status !== "completed");
  }
  return mutated;
}

/**
 * @param {import('@/types/orders').OrderApiStatus} status
 * @returns {Promise<import('@/types/orders').MyOrderItem[]>}
 */
function filterByTab(list, tab) {
  const activeSet = new Set(["pending_pay", "in_progress", "pending_review", "refunding"]);
  const historySet = new Set(["completed", "cancelled", "refunded", "closed"]);
  const set = tab === "active" ? activeSet : historySet;
  const hit = list.filter((item) => set.has(item.status));
  return hit.length ? hit : list;
}

export async function fetchOrders(status) {
  const data = await request({
    url: "/v1/orders?page=1&size=50",
    method: "GET",
  });
  const list = normalizeOrderListResponse(data);
  return filterByTab(list, status);
}

/**
 * 首页未完成订单提醒
 * GET /api/v1/orders/home-reminders?size=3
 * @param {number} [size=3]
 * @returns {Promise<import('@/types/orders').MyOrderItem[]>}
 */
export async function fetchHomeOrderReminders(size = 3) {
  try {
    const data = await request({
      url: `/v1/orders/home-reminders?size=${size}`,
      method: "GET",
    });
    const list = normalizeOrderListResponse(data);
    return filterByTab(list, "active").slice(0, size);
  } catch {
    return [];
  }
}

/**
 * GET /api/v1/orders/{orderId}
 * @param {string|number} orderId
 * @returns {Promise<import('@/types/orders').MyOrderItem>}
 */
export async function fetchOrderDetail(orderId) {
  try {
    const data = await request({
      url: `/v1/orders/${encodeURIComponent(orderId)}`,
      method: "GET",
    });
    if (data?.orderId != null) {
      return mapOrderRecordToItem(data);
    }
  } catch {
    /* mock */
  }
  const all = [...mockActive(), ...mockHistory()];
  const hit = all.find((o) => o.id === String(orderId));
  if (hit) return hit;
  throw new Error("订单不存在");
}

/**
 * POST /api/v1/orders
 * @param {import('@/types/videoConference').CreateOrderApiPayload} payload
 */
export async function createOrderV1(payload) {
  try {
    const data = await request({
      url: "/v1/orders",
      method: "POST",
      data: payload,
    });
    if (data?.orderId != null) return { orderId: String(data.orderId) };
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 400));
  return { orderId: `ord-${Date.now()}` };
}

/**
 * POST /api/v1/orders/mock-pay
 * @param {string|number} orderId
 */
export async function mockPayOrder(orderId) {
  try {
    await request({
      url: "/v1/orders/mock-pay",
      method: "POST",
      data: { orderId: Number(orderId) || orderId },
    });
    markOrderPaid(String(orderId));
    return { ok: true };
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 300));
  markOrderPaid(String(orderId));
  return { ok: true };
}

/**
 * @param {string} orderId
 * @param {string} reason
 */
export async function cancelOrder(orderId, reason) {
  try {
    await request({
      url: `/orders/${orderId}/cancel`,
      method: "POST",
      data: { reason },
    });
    markOrderCancelled(orderId);
    return { ok: true };
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 400));
  markOrderCancelled(orderId);
  return { ok: true };
}

/**
 * @param {string} orderId
 */
export async function applyRefund(orderId) {
  try {
    await request({
      url: `/orders/${orderId}/refund`,
      method: "POST",
    });
    markOrderRefunding(orderId);
    return { ok: true };
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 500));
  markOrderRefunding(orderId);
  return { ok: true };
}

function mockActive() {
  const now = Date.now();
  return [
    {
      id: "ord-p1",
      orderNo: "EF20260520001",
      sceneTag: "互联网大厂模拟面试",
      expertName: "李子昂",
      expertTitle: "前腾讯人力总监",
      expertId: "coach-job-tech-deep-0",
      dateLabel: "2026年5月20日",
      dateIso: "2026-05-20",
      timeRange: "16:00 - 16:45",
      slotId: "slot-pending-1",
      sessionMinutes: 45,
      isToday: true,
      status: "pending_pay",
      payExpireAt: now + PAY_WINDOW_MS,
      amount: 299,
      timeline: [
        { id: "t1", label: "提交订单", time: "14:02", done: true },
        { id: "t2", label: "待支付", time: "进行中", done: false, current: true },
        { id: "t3", label: "待训练", time: "—", done: false },
        { id: "t4", label: "评价", time: "—", done: false },
      ],
    },
    {
      id: "ord-a1",
      orderNo: "EF20260519001",
      sceneTag: "互联网大厂模拟面试",
      expertName: "李子昂",
      expertTitle: "前腾讯人力总监",
      dateLabel: "2026年5月19日",
      timeRange: "16:00 - 16:45",
      isToday: true,
      status: "in_progress",
      amount: 279,
      canEnterRoom: true,
      canRefund: true,
      timeline: [
        { id: "t1", label: "下单", time: "05-18 10:20", done: true },
        { id: "t2", label: "支付成功", time: "05-18 10:21", done: true },
        { id: "t3", label: "待训练", time: "今天 16:00", done: false, current: true },
        { id: "t4", label: "评价", time: "—", done: false },
      ],
    },
    {
      id: "ord-r1",
      orderNo: "EF20260515088",
      sceneTag: "管理汇报",
      expertName: "周予安",
      expertTitle: "10 年 HRBP",
      dateLabel: "2026年5月16日",
      timeRange: "19:30 - 20:15",
      status: "refunding",
      amount: 199,
      refundSteps: [
        { key: "apply", label: "提交申请", done: true },
        { key: "audit", label: "平台审核", done: true, current: true },
        { key: "done", label: "退款到账", done: false },
      ],
      timeline: [
        { id: "t1", label: "下单", time: "05-14 09:00", done: true },
        { id: "t2", label: "支付", time: "05-14 09:01", done: true },
        { id: "t3", label: "申请退款", time: "05-15 11:30", done: true, current: true },
      ],
    },
    {
      id: "ord-v1",
      orderNo: "EF20260510003",
      sceneTag: "年终述职",
      expertName: "陈默言",
      expertTitle: "群面金牌导师",
      dateLabel: "2026年5月10日",
      timeRange: "14:00 - 14:45",
      status: "pending_review",
      amount: 268,
      timeline: [
        { id: "t1", label: "下单", time: "05-08", done: true },
        { id: "t2", label: "支付", time: "05-08", done: true },
        { id: "t3", label: "训练完成", time: "05-10", done: true },
        { id: "t4", label: "待复盘评价", time: "进行中", done: false, current: true },
      ],
    },
  ];
}

function mockHistory() {
  return [
    {
      id: "ord-h1",
      orderNo: "EF20260412001",
      sceneTag: "管理汇报",
      expertName: "王若溪",
      expertTitle: "上市公司培训负责人",
      dateLabel: "2026年4月12日",
      timeRange: "10:00 - 10:45",
      status: "completed",
      amount: 299,
      timeline: [
        { id: "t1", label: "下单", time: "04-10", done: true },
        { id: "t2", label: "支付", time: "04-10", done: true },
        { id: "t3", label: "训练", time: "04-12", done: true },
        { id: "t4", label: "已评价", time: "04-12", done: true },
      ],
    },
    {
      id: "ord-h2",
      orderNo: "EF20260308056",
      sceneTag: "商务谈判",
      expertName: "陈默言",
      expertTitle: "群面金牌导师",
      dateLabel: "2026年3月8日",
      timeRange: "15:00 - 15:50",
      status: "refunded",
      amount: 199,
      refundSteps: [
        { key: "apply", label: "提交申请", done: true },
        { key: "audit", label: "平台审核", done: true },
        { key: "done", label: "退款到账", done: true },
      ],
    },
    {
      id: "ord-h3",
      orderNo: "EF20260220012",
      sceneTag: "压力面试",
      expertName: "林晚晴",
      expertTitle: "前字节技术总监",
      dateLabel: "2026年2月20日",
      timeRange: "20:00 - 20:45",
      status: "cancelled",
      amount: 299,
    },
    {
      id: "ord-h4",
      orderNo: "EF20260115007",
      sceneTag: "公众演讲",
      expertName: "苏景行",
      expertTitle: "高管演讲顾问",
      dateLabel: "2026年1月15日",
      timeRange: "11:00 - 11:45",
      status: "closed",
      amount: 399,
    },
  ];
}

/**
 * 将单条待支付订单转为进行中（供列表乐观更新）
 * @param {import('@/types/orders').MyOrderItem} item
 */
export function toPaidOrderItem(item) {
  return {
    ...item,
    status: /** @type {const} */ ("in_progress"),
    payExpireAt: undefined,
    canEnterRoom: true,
    canRefund: true,
    timeline: [
      { id: "t1", label: "提交订单", time: "已完成", done: true },
      { id: "t2", label: "支付成功", time: "刚刚", done: true },
      { id: "t3", label: "待训练", time: item.timeRange, done: false, current: true },
      { id: "t4", label: "评价", time: "—", done: false },
    ],
  };
}
