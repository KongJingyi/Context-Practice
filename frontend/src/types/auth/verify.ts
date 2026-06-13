/** 身份认证状态 */
export type AuthVerifyStatus = "unverified" | "pending" | "verified" | "rejected";

export interface AuthVerifyPayload {
  realName: string;
  idCard: string;
  idCardFrontUrl: string;
  idCardBackUrl: string;
}

export interface AuthVerifyStatusResponse {
  status: AuthVerifyStatus;
  rejectReason?: string;
}
