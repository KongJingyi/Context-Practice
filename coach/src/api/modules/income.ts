import { request } from "@/api/request";

export interface IncomeRecord {
  id: number | string;
  orderId: number;
  sceneName: string;
  userName: string;
  paidAmount: number;
  platformFee: number;
  netAmount: number;
  status: "SETTLED" | "PENDING" | "REFUNDED";
  settledAt?: string;
}

export interface IncomeSummary {
  monthTotal: number;
  monthNet: number;
  pendingAmount: number;
  orderCount: number;
}

export async function fetchIncomeSummary(): Promise<IncomeSummary> {
  try {
    return (await request({
      url: "/v1/coach/income/summary",
      method: "GET",
      silent: true,
    })) as IncomeSummary;
  } catch {
    return { monthTotal: 15200, monthNet: 12920, pendingAmount: 840, orderCount: 18 };
  }
}

export async function fetchIncomeRecords(): Promise<IncomeRecord[]> {
  try {
    const data = (await request({
      url: "/v1/coach/income",
      method: "GET",
      silent: true,
    })) as { records?: IncomeRecord[] } | IncomeRecord[];
    if (Array.isArray(data)) return data;
    return data.records || [];
  } catch {
    return [
      { id: 1, orderId: 101, sceneName: "压力面试", userName: "张同学", paidAmount: 168, platformFee: 25.2, netAmount: 142.8, status: "SETTLED", settledAt: "2026-06-08" },
      { id: 2, orderId: 98, sceneName: "管理汇报", userName: "王同学", paidAmount: 228, platformFee: 34.2, netAmount: 193.8, status: "SETTLED", settledAt: "2026-06-06" },
      { id: 3, orderId: 95, sceneName: "即兴演讲", userName: "陈同学", paidAmount: 148, platformFee: 22.2, netAmount: 125.8, status: "PENDING" },
    ];
  }
}
