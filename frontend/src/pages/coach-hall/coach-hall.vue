<template>
  <CoachHall :scene-id="sceneId" :sub-scene-name="subSceneName" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import CoachHall from "@/components/coach-hall/CoachHall.vue";
import { decodeQueryParam } from "@/utils/common/queryString.js";
import { resolveSceneById } from "@/utils/scene/sceneContext";

const sceneId = ref("");
const subSceneName = ref("");

onLoad((options) => {
  if (options?.sceneId) {
    sceneId.value = decodeQueryParam(options.sceneId);
  }
  if (options?.subSceneName) {
    subSceneName.value = decodeQueryParam(options.subSceneName);
  }
  const ctx = sceneId.value ? resolveSceneById(sceneId.value) : null;
  const title = ctx?.sub.title ?? subSceneName.value;
  if (title) {
    subSceneName.value = title;
    uni.setNavigationBarTitle({ title });
  }
});
</script>

<style scoped>
/* 页面壳层由 CoachHall 承载背景 */
</style>
