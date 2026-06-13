/**
 * 用户从订单进入训练室：start → join-info → record join
 */
import { startTraining, fetchJoinInfo, recordRoomJoin } from "@/api/modules/videoConference.js";
import { setPendingTraining } from "@/utils/training/bridge.js";

/**
 * @param {object} order
 * @param {string|number} order.id
 * @param {string} [order.sceneTag]
 * @param {string} [order.expertName]
 * @param {string} [order.expertTitle]
 * @param {string} [order.orderNo]
 * @param {string|number} [order.sceneId]
 */
export async function enterTrainingRoomFromOrder(order) {
  const orderId = order.id;
  const started = await startTraining({
    orderId: Number(orderId) || orderId,
    scenarioCode: "INTERVIEW",
  });
  const joinInfo = await fetchJoinInfo(started.roomId);
  if (joinInfo.canEnter === false) {
    throw new Error(joinInfo.denyReason || joinInfo.enterDeniedReason || "当前不可进入训练房间");
  }
  await recordRoomJoin(started.roomId);
  setPendingTraining({
    orderId: String(orderId),
    roomId: started.roomId,
    trainingId: started.trainingId,
    scenarioId: String(order.sceneId ?? "INTERVIEW"),
    scenarioCode: "INTERVIEW",
    roomTitle: order.sceneTag || "训练场景",
    expertName: order.expertName,
    expertTitle: order.expertTitle,
    orderNo: order.orderNo,
  });
  return { roomId: started.roomId, trainingId: started.trainingId };
}

/**
 * @param {object} order
 * @param {string} [failToast=进房失败]
 */
export async function navigateToTrainingRoom(order, failToast = "进房失败") {
  uni.showLoading({ title: "进入房间…", mask: true });
  try {
    await enterTrainingRoomFromOrder(order);
    uni.navigateTo({ url: "/pages/room/room" });
  } catch (e) {
    const msg = e instanceof Error ? e.message : failToast;
    uni.showToast({ title: msg, icon: "none", duration: 2800 });
    throw e;
  } finally {
    uni.hideLoading();
  }
}
