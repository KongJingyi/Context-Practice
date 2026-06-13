import { request } from "@/api/request";

export interface ScheduleSlot {
  id?: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  enabled?: boolean;
}

export interface SchedulePayload {
  slots: ScheduleSlot[];
}

const DAY_LABELS = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];

export function dayLabel(dow: number) {
  return DAY_LABELS[dow] || `周${dow}`;
}

export async function fetchSchedule(): Promise<ScheduleSlot[]> {
  const data = (await request({
    url: "/v1/coach/schedule",
    method: "GET",
    silent: true,
  })) as { slots?: ScheduleSlot[] } | ScheduleSlot[];
  if (Array.isArray(data)) return data;
  return data.slots || [];
}

export async function saveSchedule(slots: ScheduleSlot[]): Promise<{ ok: boolean }> {
  return (await request({
    url: "/v1/coach/schedule",
    method: "POST",
    data: { slots },
  })) as { ok: boolean };
}
