import { request } from "@/api/request";
import type {
  RoomJoinInfo,
  RoomJoinRecord,
  RoomLeaveRecord,
  RoomStatus,
  EndRoomResult,
  EndTrainingResult,
  LeaveReason,
} from "@/types/videoConference";

const MOCK_ROOM_ID = "a1b2c3d4e5f6789012345678901234";

function mockJoinInfo(roomId: string, role: "USER" | "COACH"): RoomJoinInfo {
  const now = new Date().toISOString().replace("T", " ").slice(0, 19);
  return {
    roomId,
    trainingId: 100,
    orderId: 101,
    trainingStatus: "IN_PROGRESS",
    sdkAppId: 1400000000,
    trtcUserId: role === "COACH" ? "c_2" : "u_1",
    userSig: "eJw1jcsOwiAQRf9leGsd...",
    role,
    peer: {
      userId: role === "COACH" ? 1 : 2,
      trtcUserId: role === "COACH" ? "u_1" : "c_2",
      nickname: role === "COACH" ? "张三" : "李教练",
      avatar: "",
    },
    sceneName: "压力面试",
    scheduledStart: "2026-06-01T14:00:00",
    scheduledEnd: "2026-06-01T15:00:00",
    startedAt: now,
    serverTime: now,
    canEnter: true,
    denyReason: null,
    participants: [
      { role: "USER", userId: 1, joined: true, joinedAt: now },
      { role: "COACH", userId: 2, joined: role === "COACH", joinedAt: role === "COACH" ? now : null },
    ],
  };
}

// Get join info (TRTC credentials)
export async function fetchJoinInfo(roomId: string): Promise<RoomJoinInfo> {
  return (await request({
    url: `/v1/rooms/${encodeURIComponent(roomId)}/join-info`,
    method: "GET",
    silent: true,
  })) as RoomJoinInfo;
}

// Record room join
export async function recordRoomJoin(roomId: string): Promise<RoomJoinRecord> {
  try {
    return await request({
      url: `/v1/rooms/${roomId}/join`,
      method: "POST",
      data: {},
      silent: true,
    }) as RoomJoinRecord;
  } catch {
    return { joinedAt: new Date().toISOString(), role: "COACH" };
  }
}

// Leave room
export async function leaveRoom(
  roomId: string,
  reason: LeaveReason = "COACH_HANGUP",
): Promise<RoomLeaveRecord> {
  try {
    return await request({
      url: `/v1/rooms/${roomId}/leave`,
      method: "POST",
      data: { reason },
      silent: true,
    }) as RoomLeaveRecord;
  } catch {
    return { leftAt: new Date().toISOString() };
  }
}

// End training (coach side)
export async function endRoomByCoach(
  roomId: string,
  payload?: { transcript?: string; sceneName?: string },
): Promise<EndRoomResult> {
  try {
    return await request({
      url: `/v1/rooms/${roomId}/end`,
      method: "POST",
      data: payload || {},
    }) as EndRoomResult;
  } catch {
    return {
      trainingId: 100,
      endedAt: new Date().toISOString(),
      durationSeconds: 3600,
      orderStatus: "COMPLETED",
      reportReady: false,
    };
  }
}

// Room status
export async function fetchRoomStatus(roomId: string): Promise<RoomStatus> {
  try {
    return await request({
      url: `/v1/rooms/${roomId}`,
      method: "GET",
      silent: true,
    }) as RoomStatus;
  } catch {
    return {
      roomId,
      trainingId: 100,
      orderId: 101,
      trainingStatus: "IN_PROGRESS",
      orderStatus: "IN_SERVICE",
      startedAt: new Date().toISOString(),
      endedAt: null,
      durationSeconds: 0,
      serverTime: new Date().toISOString(),
      participants: [],
    };
  }
}

// End training and generate report
export async function endTraining(payload: {
  roomId: string;
  transcript?: string;
  sceneName?: string;
}): Promise<EndTrainingResult> {
  return (await request({
    url: "/v1/training/end",
    method: "POST",
    data: payload,
  })) as EndTrainingResult;
}

// Fetch training report
export async function fetchTrainingReport(trainingRecordId: number): Promise<{ report: string }> {
  return (await request({
    url: "/v1/training/report",
    method: "POST",
    data: { trainingRecordId },
  })) as { report: string };
}

// P1 - Pressure mode
export async function postPressureCountdown(
  roomId: string,
  body: { action: "start" | "stop" | "reset"; seconds?: number },
) {
  return request({ url: `/v1/rooms/${roomId}/pressure/countdown`, method: "POST", data: body });
}

export async function postPressureInterrupt(roomId: string, message: string) {
  return request({
    url: `/v1/rooms/${roomId}/pressure/interrupt`,
    method: "POST",
    data: { message },
  });
}

export async function postPressureQuestion(roomId: string, body: { questionId?: number; text: string }) {
  return request({
    url: `/v1/rooms/${roomId}/pressure/question`,
    method: "POST",
    data: body,
  });
}

// P1 - Room state (pressure mode, whiteboard, etc.)
export interface WhiteboardStroke {
  id: string;
  color: string;
  width: number;
  points: { x: number; y: number }[];
}

export interface WhiteboardState {
  active: boolean;
  version: number;
  strokes: WhiteboardStroke[];
}

export interface RoomStateResult {
  pressureMode: {
    enabled: boolean;
    countdown: { active: boolean; secondsLeft: number; totalSeconds: number };
    lastInterrupt: { message: string; at: string } | null;
    currentQuestion: { questionId: number; text: string } | null;
  };
  whiteboard: WhiteboardState;
  serverTime: string;
}

export async function fetchRoomState(roomId: string): Promise<RoomStateResult> {
  try {
    return (await request({
      url: `/v1/rooms/${roomId}/state`,
      method: "GET",
      silent: true,
    })) as RoomStateResult;
  } catch {
    return {
      pressureMode: {
        enabled: false,
        countdown: { active: false, secondsLeft: 0, totalSeconds: 60 },
        lastInterrupt: null,
        currentQuestion: null,
      },
      whiteboard: { active: false, version: 0, strokes: [] },
      serverTime: new Date().toISOString(),
    };
  }
}

export async function postWhiteboardToggle(roomId: string, active: boolean) {
  return request({
    url: `/v1/rooms/${roomId}/whiteboard/toggle`,
    method: "POST",
    data: { active },
  }) as Promise<WhiteboardState>;
}

export async function postWhiteboardStrokes(roomId: string, strokes: WhiteboardStroke[]) {
  return request({
    url: `/v1/rooms/${roomId}/whiteboard/strokes`,
    method: "POST",
    data: { strokes },
  }) as Promise<WhiteboardState>;
}

export async function postWhiteboardClear(roomId: string) {
  return request({
    url: `/v1/rooms/${roomId}/whiteboard/clear`,
    method: "POST",
    data: {},
  }) as Promise<WhiteboardState>;
}

// P1 - Chat
export async function postRoomChat(roomId: string, text: string) {
  return request({ url: `/v1/rooms/${roomId}/chat`, method: "POST", data: { text } });
}

export async function fetchRoomChat(roomId: string, page = 1, size = 50) {
  return request({ url: `/v1/rooms/${roomId}/chat`, method: "GET", params: { page, size } });
}

export { MOCK_ROOM_ID };
