<template>
  <ExpertBooking :expert-id="expertId" :scene-tag="sceneTag" :scene-code="sceneCode" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import ExpertBooking from "@/components/booking/ExpertBooking.vue";
import { decodeQueryParam } from "@/utils/common/queryString.js";

const expertId = ref("");
const sceneTag = ref("专项训练");
const sceneCode = ref("INTERVIEW");

onLoad((options) => {
  const raw = options?.id ?? options?.expertId ?? "";
  expertId.value = decodeQueryParam(String(raw));
  if (options?.sceneTag) {
    sceneTag.value = decodeQueryParam(String(options.sceneTag));
  }
  if (options?.sceneCode) {
    sceneCode.value = decodeQueryParam(String(options.sceneCode));
  }
  if (expertId.value) {
    uni.setNavigationBarTitle({ title: "专家预约" });
  }
});
</script>

<style scoped>
/* 布局由 ExpertBooking 内 Tailwind 完成 */
</style>
