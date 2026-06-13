<template>
  <TrainingRoom
    :room-title="title"
    :scenario-id="scenarioId"
    :expert-name="expertName"
    :expert-title="expertTitle"
    :order-no="orderNo"
    :order-id="orderId"
    :room-id="roomId"
  />
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import TrainingRoom from "@/components/training/TrainingRoom.vue";
import { consumePendingTraining } from "@/utils/training/bridge.js";
import type { PendingTraining } from "@/types/training/bridge";

const title = ref("训练房间");
const scenarioId = ref("");
const expertName = ref("语境专家");
const expertTitle = ref("");
const orderNo = ref("");
const orderId = ref("");
const roomId = ref("");

function applyPending(p: PendingTraining) {
  scenarioId.value = p.scenarioCode ?? p.scenarioId ?? "";
  title.value = p.roomTitle || "训练房间";
  expertName.value = p.expertName || expertName.value;
  expertTitle.value = p.expertTitle || "";
  orderNo.value = p.orderNo || "";
  orderId.value = p.orderId != null ? String(p.orderId) : "";
  roomId.value = p.roomId || "";
}

onShow(() => {
  const p = consumePendingTraining() as PendingTraining | null;
  if (p) {
    applyPending(p);
  }
});
</script>

<style scoped>
/* 沉浸式布局由 TrainingRoom 承载 */
</style>
