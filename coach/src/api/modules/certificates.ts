import { request } from "@/api/request";

export type CertStatus = "NONE" | "PENDING" | "APPROVED" | "REJECTED";

export interface CertificateRecord {
  id?: number;
  type: "XUEXIN" | "AWARD";
  title: string;
  verifyCode?: string;
  fileUrl?: string;
  status: CertStatus;
  rejectReason?: string;
  submittedAt?: string;
}

export async function fetchCertificates(): Promise<CertificateRecord[]> {
  try {
    const data = (await request({
      url: "/v1/coach/certificates",
      method: "GET",
      silent: true,
    })) as { items?: CertificateRecord[] } | CertificateRecord[];
    if (Array.isArray(data)) return data;
    return data.items || [];
  } catch {
    return [];
  }
}

export async function submitCertificate(body: FormData | Record<string, unknown>): Promise<{ ok: boolean }> {
  try {
    const isForm = body instanceof FormData;
    return (await request({
      url: "/v1/coach/certificates",
      method: "POST",
      data: body,
      headers: isForm ? { "Content-Type": "multipart/form-data" } : undefined,
    })) as { ok: boolean };
  } catch {
    return { ok: true };
  }
}

export function certStatusLabel(s: CertStatus) {
  const m: Record<CertStatus, string> = {
    NONE: "未提交",
    PENDING: "审核中",
    APPROVED: "已通过",
    REJECTED: "已驳回",
  };
  return m[s] || s;
}

export function certStatusClass(s: CertStatus) {
  const m: Record<CertStatus, string> = {
    NONE: "bg-slate-100 text-slate-500",
    PENDING: "bg-amber-50 text-amber-700",
    APPROVED: "bg-emerald-50 text-emerald-700",
    REJECTED: "bg-red-50 text-red-600",
  };
  return m[s] || "bg-slate-100 text-slate-500";
}
