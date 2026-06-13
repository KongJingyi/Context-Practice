/**
 * 1v1 视频会议 API（对齐 api-v1-video-conference.md）
 */
import { request } from "@/api/request.js";

function apiErrorMessage(err, fallback) {
  if (err && typeof err === "object" && "message" in err && typeof err.message === "string") {
    return err.message;
  }
  return fallback;
}

/**
 * POST /api/v1/training/start
 * @param {{ orderId: number|string; scenarioCode: string }} payload
 * @returns {Promise<import('@/types/videoConference').StartTrainingResult>}
 */
export async function startTraining(payload) {
  const data = await request({
    url: "/v1/training/start",
    method: "POST",
    data: {
      orderId: Number(payload.orderId),
      scenarioCode: payload.scenarioCode || "INTERVIEW",
    },
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "无法开始训练"));
  });
  if (!data?.roomId) {
    throw new Error("开始训练失败，未返回 roomId");
  }
  return normalizeStart(data);
}

/** GET /api/training/start?orderId= — 兼容入口 */
export async function startTrainingGet(orderId) {
  const data = await request({
    url: `/training/start?orderId=${encodeURIComponent(orderId)}`,
    method: "GET",
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "无法开始训练"));
  });
  if (data?.roomId) {
    return {
      roomId: data.roomId,
      trainingId: data.trainingId ?? 100,
      orderId: Number(orderId),
      startedAt: data.startedAt ?? new Date().toISOString().slice(0, 19),
    };
  }
  return startTraining({ orderId, scenarioCode: "INTERVIEW" });
}

/**
 * GET /api/v1/rooms/{roomId}/join-info
 * @param {string} roomId
 * @returns {Promise<import('@/types/videoConference').RoomJoinInfo>}
 */
export async function fetchJoinInfo(roomId) {
  const data = await request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/join-info`,
    method: "GET",
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "获取进房信息失败"));
  });
  if (data?.roomId == null && data?.sdkAppId == null) {
    throw new Error("进房信息不完整");
  }
  return normalizeJoinInfo(data);
}

/** POST /api/v1/rooms/{roomId}/join */
export async function recordRoomJoin(roomId) {
  const data = await request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/join`,
    method: "POST",
    data: {},
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "记录进房失败"));
  });
  return data ?? { joinedAt: new Date().toISOString().slice(0, 19), role: "USER" };
}

/** POST /api/v1/rooms/{roomId}/leave */
export async function leaveRoom(roomId, reason = "USER_HANGUP") {
  try {
    const data = await request({
      url: `/v1/rooms/${encodeURIComponent(roomId)}/leave`,
      method: "POST",
      data: { reason },
    });
    if (data) return data;
  } catch {
    /* optional */
  }
  return { leftAt: new Date().toISOString().slice(0, 19) };
}

/** POST /api/v1/rooms/{roomId}/end（仅 COACH） */
export async function endRoomByCoach(roomId, payload = {}) {
  return request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/end`,
    method: "POST",
    data: payload,
  });
}

/** GET /api/v1/rooms/{roomId} */
export async function fetchRoomStatus(roomId) {
  const data = await request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}`,
    method: "GET",
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "获取房间状态失败"));
  });
  if (data?.roomId) return normalizeRoomStatus(data);
  throw new Error("房间状态无效");
}

/** POST /api/v1/training/end */
export async function endTraining(payload) {
  const data = await request({
    url: "/v1/training/end",
    method: "POST",
    data: {
      roomId: payload.roomId,
      transcript: payload.transcript,
      sceneName: payload.sceneName,
    },
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "结束训练失败"));
  });
  return data ?? { report: "{}" };
}

/** POST /api/v1/training/report */
export async function fetchTrainingReport(trainingRecordId) {
  const data = await request({
    url: "/v1/training/report",
    method: "POST",
    data: { trainingRecordId },
  }).catch((err) => {
    throw new Error(apiErrorMessage(err, "获取报告失败"));
  });
  if (data?.report != null) {
    return { report: parseReportString(data.report) };
  }
  throw new Error("报告不存在");
}

/** GET /api/v1/rooms/{roomId}/state */
export async function fetchRoomState(roomId) {
  try {
    return await request({
      url: `/v1/rooms/${encodeURIComponent(roomId)}/state`,
      method: "GET",
    });
  } catch {
    return {
      pressureMode: {
        enabled: false,
        countdown: { active: false, secondsLeft: 0, totalSeconds: 60 },
        lastInterrupt: null,
        currentQuestion: null,
      },
      whiteboard: { active: false, version: 0, strokes: [] },
      serverTime: new Date().toISOString().slice(0, 19),
    };
  }
}

export async function postPressureCountdown(roomId, body) {
  return request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/pressure/countdown`,
    method: "POST",
    data: body,
  });
}

export async function postPressureInterrupt(roomId, message) {
  return request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/pressure/interrupt`,
    method: "POST",
    data: { message },
  });
}

export async function postPressureQuestion(roomId, body) {
  return request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/pressure/question`,
    method: "POST",
    data: body,
  });
}

export async function postRoomChat(roomId, text) {
  return request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/chat`,
    method: "POST",
    data: { text },
  });
}

export async function fetchRoomChat(roomId, page = 1, size = 50) {
  return request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/chat?page=${page}&size=${size}`,
    method: "GET",
  });
}

export async function loginByUsername(username) {
  const data = await request({
    url: "/auth/login",
    method: "POST",
    data: { username },
  });
  if (data?.token) return data;
  throw new Error("登录失败");
}

export async function fetchUserMe() {
  const data = await request({ url: "/v1/user/me", method: "GET" });
  if (data?.userId != null) return data;
  throw new Error("获取用户信息失败");
}

function normalizeStart(raw) {
  return {
    roomId: raw.roomId,
    trainingId: raw.trainingId,
    orderId: raw.orderId,
    startedAt: raw.startedAt,
  };
}

function normalizeJoinInfo(raw) {
  return {
    roomId: raw.roomId,
    trainingId: raw.trainingId,
    orderId: raw.orderId,
    trainingStatus: raw.trainingStatus ?? "IN_PROGRESS",
    sdkAppId: raw.sdkAppId,
    trtcUserId: raw.trtcUserId,
    userSig: raw.userSig,
    role: raw.role ?? "USER",
    peer: raw.peer,
    sceneName: raw.sceneName ?? "",
    scheduledStart: raw.scheduledStart,
    scheduledEnd: raw.scheduledEnd,
    startedAt: raw.startedAt,
    serverTime: raw.serverTime,
    canEnter: raw.canEnter !== false,
    denyReason: raw.denyReason ?? raw.deny_reason ?? null,
    participants: raw.participants ?? [],
  };
}

function normalizeRoomStatus(raw) {
  return {
    roomId: raw.roomId,
    trainingId: raw.trainingId,
    orderId: raw.orderId,
    trainingStatus: raw.trainingStatus,
    orderStatus: raw.orderStatus,
    startedAt: raw.startedAt,
    endedAt: raw.endedAt ?? null,
    durationSeconds: raw.durationSeconds ?? 0,
    serverTime: raw.serverTime,
    participants: raw.participants ?? [],
  };
}

function parseReportString(report) {
  if (typeof report === "string") {
    try {
      return JSON.parse(report);
    } catch {
      return { raw: report };
    }
  }
  return report;
}
