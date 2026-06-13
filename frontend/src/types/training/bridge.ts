/** 进入训练房前暂存（页面 bridge，联调时由 videoConference API 填充） */

export interface PendingTraining {
  /** 场景编码，对应 POST /v1/training/start 的 scenarioCode */
  scenarioId: string;
  roomTitle: string;
  expertName?: string;
  expertTitle?: string;
  orderNo?: string;
  /** 订单 ID，联调 start 接口用 */
  orderId?: string | number;
  /** start / join-info 返回 */
  roomId?: string;
  trainingId?: number;
  scenarioCode?: string;
}
