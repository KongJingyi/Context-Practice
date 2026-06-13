import { request } from "./request";

export const fetchDashboard = () => request<Record<string, number>>("/v1/admin/dashboard", { method: "GET" });

export const fetchUsers = (keyword?: string, page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/users", { method: "GET", params: { keyword, page, size: 20 } });

export const updateUserStatus = (userId: number, status: number) =>
  request("/v1/admin/users/" + userId + "/status", { method: "PUT", data: { status } });

export const fetchVerifications = (status = "pending", page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/verifications", { method: "GET", params: { status, page } });

export const reviewVerification = (id: number, status: number, note: string) =>
  request("/v1/admin/verifications/" + id + "/review", { method: "POST", data: { status, note } });

export const fetchComplaints = (status?: string, page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/complaints", { method: "GET", params: { status, page } });

export const handleComplaint = (id: number, status: string, resultNote: string) =>
  request("/v1/admin/complaints/" + id + "/handle", { method: "POST", data: { status, resultNote } });

export const fetchRefunds = (status?: string, page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/refunds", { method: "GET", params: { status, page } });

export const decideRefund = (id: number, status: string) =>
  request("/v1/admin/refunds/" + id + "/decide", { method: "POST", data: { status } });

export const fetchPendingCertificates = (page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/certificates/pending", { method: "GET", params: { page } });

export const reviewCertificate = (id: number, status: number, rejectReason = "") =>
  request("/v1/admin/certificates/" + id + "/review", { method: "POST", data: { status, rejectReason } });

export const fetchAnnouncements = () =>
  request<Record<string, unknown>[]>("/v1/admin/announcements", { method: "GET" });

export const createAnnouncement = (body: Record<string, unknown>) =>
  request("/v1/admin/announcements", { method: "POST", data: body });

export const deleteAnnouncement = (id: number) =>
  request("/v1/admin/announcements/" + id, { method: "DELETE" });

export const fetchConfigs = () => request<Record<string, unknown>[]>("/v1/admin/config", { method: "GET" });

export const upsertConfig = (key: string, value: string, description?: string) =>
  request("/v1/admin/config/" + encodeURIComponent(key), { method: "PUT", data: { value, description } });

export const fetchAuditLogs = (page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/audit-logs", { method: "GET", params: { page, size: 50 } });

export const fetchScenes = () => request<Record<string, unknown>[]>("/v1/admin/scenes", { method: "GET" });

export const updateSceneStatus = (id: number, status: number) =>
  request("/v1/admin/scenes/" + id + "/status", { method: "PUT", data: { status } });

export const fetchOrders = (status?: string, page = 1) =>
  request<Record<string, unknown>[]>("/v1/admin/orders", { method: "GET", params: { status, page } });
