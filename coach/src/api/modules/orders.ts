import { request } from "@/api/request";
import type { CoachOrderRecord, OrderApiRecord } from "@/types/videoConference";

export async function fetchCoachOrders(status?: string): Promise<CoachOrderRecord[]> {
  const data = (await request({
    url: "/v1/coach/orders",
    method: "GET",
    params: { status, page: 1, size: 20 },
    silent: true,
  })) as { records: CoachOrderRecord[] };
  return data?.records ?? [];
}

export async function fetchOrderDetail(orderId: number | string) {
  return (await request({
    url: `/v1/orders/${orderId}`,
    method: "GET",
    silent: true,
  })) as OrderApiRecord;
}
