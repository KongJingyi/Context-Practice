<template>
  <view class="page">
    <ScenePlaza
      :featured-coaches="featuredCoaches"
      :api-title="plazaTitle"
      :api-subtitle="plazaSubtitle"
      @start="onSceneStart"
      @book-coach="onCoachBook"
    />
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import ScenePlaza from "@/components/scene/ScenePlaza.vue";
import { fetchScenePlaza } from "@/api/modules/scenePlaza.js";
import type { SceneCardData } from "@/types/scene/plaza";
import type { Coach } from "@/types/coach/hall";

const featuredCoaches = ref<Coach[]>([]);
const plazaTitle = ref("");
const plazaSubtitle = ref("");

onMounted(async () => {
  try {
    const data = await fetchScenePlaza();
    featuredCoaches.value = Array.isArray(data.coaches) ? data.coaches : [];
    plazaTitle.value = data.sectionTitle || "";
    plazaSubtitle.value = data.sectionSubtitle || "";
  } catch {
    /* 保留静态场景卡片，陪练区不展示 */
  }
});

function onSceneStart(card: SceneCardData) {
  uni.navigateTo({
    url: `/pages/coach-hall/coach-hall?sceneId=${card.id}&subSceneName=${card.title}`,
  });
}

function onCoachBook(coach: Coach) {
  const sceneTag = coach.categoryId || "job-tech-deep";
  uni.navigateTo({
    url: `/pages/expert-booking/expert-booking?id=${encodeURIComponent(coach.id)}&sceneTag=${encodeURIComponent(sceneTag)}&sceneCode=INTERVIEW`,
  });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: transparent;
  box-sizing: border-box;
}
</style>
