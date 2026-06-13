/**
 * 订单 API 字段 ↔ 前端 MyOrderItem 映射（api-v1-video-conference.md §3.6）
 */

/** @param {import('@/types/videoConference').ApiOrderStatus} apiStatus */
export function mapApiOrderStatus(apiStatus) {
  const m = {
    PENDING_PAY: "pending_pay",
    PAID: "in_progress",
    IN_SERVICE: "in_progress",
    COMPLETED: "completed",
    CANCELLED: "cancelled",
    REFUNDING: "refunding",
    REFUNDED: "refunded",
    EXPIRED: "expired",
  };
  return /** @type {import('@/types/orders').OrderStatus} */ (m[apiStatus] || "in_progress");
}

/**
 * @param {import('@/types/videoConference').OrderApiRecord} raw
 * @returns {import('@/types/orders').MyOrderItem}
 */
export function mapOrderRecordToItem(raw) {
  const start = raw.scheduledStart || "";
  const end = raw.scheduledEnd || "";
  const dateLabel = formatDateLabel(start);
  const timeRange = formatTimeRange(start, end);
  const status = mapApiOrderStatus(raw.status);

  let uiStatus = status;
  if (raw.expired || raw.status === "EXPIRED") {
    uiStatus = "expired";
  } else if (raw.sessionEnded || raw.displayPhase === "SESSION_ENDED") {
    uiStatus = "completed";
  } else if (raw.canReview) {
    uiStatus = "pending_review";
  }

  return {
    id: String(raw.orderId),
    orderNo: `EF${raw.orderId}`,
    sceneTag: raw.sceneName || "训练场景",
    expertName: raw.coachName || "专家",
    expertTitle: "",
    expertAvatarUrl: raw.coachAvatar,
    expertId: String(raw.coachId),
    sceneId: raw.sceneId,
    coachId: raw.coachId,
    dateLabel,
    dateIso: start.slice(0, 10),
    timeRange,
    status: uiStatus,
    amount: Number(raw.amount),
    payExpireAt: raw.payExpireAt ?? undefined,
    canEnterRoom: Boolean(raw.canEnterRoom),
    enterDeniedReason: raw.enterDeniedReason ?? undefined,
    roomId: raw.roomId ?? undefined,
    trainingStatus: raw.trainingStatus ?? undefined,
    scheduledStart: start,
    scheduledEnd: end,
    canRefund: Boolean(raw.canRefund),
    hasRated: Boolean(raw.hasRated),
    canReview: Boolean(raw.canReview),
    reportReady: Boolean(raw.reportReady),
    ribbonLabel: raw.ribbonLabel ?? undefined,
    displayPhase: raw.displayPhase ?? undefined,
    canCancel: Boolean(raw.canCancel),
    expired: Boolean(raw.expired),
    sessionEnded: Boolean(raw.sessionEnded),
  };
}

/**
 * @param {unknown} data
 * @returns {import('@/types/orders').MyOrderItem[]}
 */
export function normalizeOrderListResponse(data) {
  if (Array.isArray(data)) {
    return data.map((row) =>
      row.orderId != null ? mapOrderRecordToItem(row) : /** @type {import('@/types/orders').MyOrderItem} */ (row),
    );
  }
  if (data && Array.isArray(data.records)) {
    return data.records.map(mapOrderRecordToItem);
  }
  if (data && Array.isArray(data.list)) {
    return data.list;
  }
  return [];
}

function formatDateLabel(iso) {
  if (!iso) return "—";
  const d = iso.slice(0, 10).split("-");
  if (d.length < 3) return iso;
  return `${d[0]}年${Number(d[1])}月${Number(d[2])}日`;
}

function formatTimeRange(start, end) {
  const sh = start.slice(11, 16);
  const eh = end.slice(11, 16);
  if (sh && eh) return `${sh} - ${eh}`;
  return sh || "—";
}
