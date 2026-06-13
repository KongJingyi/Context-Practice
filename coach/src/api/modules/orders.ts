import { request } from "@/api/request";
import type {
  CoachOrderRecord,
  OrderApiRecord,
} from "@/types/videoConference";

// Mock data for coach orders
const MOCK_COACH_ORDERS: CoachOrderRecord[] = [
  {
    orderId: 101,
    status: "PAID",
    userId: 1,
    userName: "张三",
    sceneName: "压力面试",
    trainingGoal: "提升面试表达能力",
    scheduledStart: "2026-06-01T14:00:00",
    scheduledEnd: "2026-06-01T15:00:00",
    roomId: null,
    trainingStatus: null,
    canEnterRoom: true,
  },
  {
    orderId: 102,
    status: "IN_SERVICE",
    userId: 2,
    userName: "李四",
    sceneName: "管理汇报",
    scheduledStart: "2026-06-01T10:00:00",
    scheduledEnd: "2026-06-01T11:00:00",
    roomId: "a1b2c3d4e5f6789012345678901234",
    trainingStatus: "IN_PROGRESS",
    trainingGoal: "提升汇报逻辑性",
    canEnterRoom: true,
  },
  {
    orderId: 103,
    status: "COMPLETED",
    userId: 3,
    userName: "王五",
    sceneName: "客户谈判",
    scheduledStart: "2026-05-30T15:00:00",
    scheduledEnd: "2026-05-30T16:00:00",
    roomId: "b2c3d4e5f6a7890123456789012345",
    trainingStatus: "REPORT_READY",
    canEnterRoom: false,
  },
];

export async function fetchCoachOrders(status?: string): Promise<CoachOrderRecord[]> {
  try {
    const data = await request({
      url: "/v1/coach/orders",
      method: "GET",
      params: { status, page: 1, size: 20 },
      silent: true,
    }) as { records: CoachOrderRecord[] };
    return data?.records ?? [];
  } catch {
    // Return mock data when backend is not ready
    if (status) {
      return MOCK_COACH_ORDERS.filter((o) => o.status === status);
    }
    return MOCK_COACH_ORDERS;
  }
}

export async function fetchOrderDetail(orderId: number | string) {
  try {
    return await request({
      url: `/v1/orders/${orderId}`,
      method: "GET",
      silent: true,
    }) as OrderApiRecord;
  } catch {
    // Mock detail
    const mock = MOCK_COACH_ORDERS.find(
      (o) => o.orderId === Number(orderId),
    );
    if (!mock) throw new Error("订单不存在");
    return {
      orderId: mock.orderId,
      status: mock.status,
      amount: 99.0,
      coachId: 1,
      coachName: "当前陪练",
      sceneId: 1,
      sceneName: mock.sceneName,
      scheduledStart: mock.scheduledStart,
      scheduledEnd: mock.scheduledEnd,
      roomId: mock.roomId,
      trainingStatus: mock.trainingStatus ?? null,
      canEnterRoom: mock.canEnterRoom,
      userId: mock.userId,
      userName: mock.userName,
      userAvatar: undefined,
    } as OrderApiRecord;
  }
}
