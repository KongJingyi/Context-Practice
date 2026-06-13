<template>
  <view class="plaza">
    <view class="plaza__inner">
      <view class="plaza__header">
        <text class="plaza__title">{{ headerTitle }}</text>
        <text class="plaza__subtitle">{{ headerSubtitle }}</text>
      </view>

      <ScenePlazaFeaturedCoaches
        v-if="featuredCoaches.length"
        :coaches="featuredCoaches"
        @book="onCoachBook"
      />

      <!-- 一级大类药丸 Tab -->
      <scroll-view class="plaza__tabs-scroll" scroll-x :show-scrollbar="false">
        <view class="plaza__tabs">
          <view
            v-for="cat in data.categories"
            :key="cat.id"
            class="plaza__tab"
            :class="{ 'plaza__tab--active': activeCategoryId === cat.id }"
            @tap="switchCategory(cat.id)"
          >
            <text class="plaza__tab-text">{{ cat.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 子类卡片列表（切换动画） -->
      <!-- #ifdef H5 -->
      <AnimatePresence mode="wait">
        <component
          :is="MotionDiv"
          :key="activeCategoryId"
          class="plaza__grid"
          :initial="{ opacity: 0, y: 28 }"
          :animate="{ opacity: 1, y: 0 }"
          :exit="{ opacity: 0, y: -20 }"
          :transition="{ duration: 0.38, ease: [0.22, 1, 0.36, 1] }"
        >
          <SceneItem
            v-for="(card, index) in activeCards"
            :key="card.id"
            :card="card"
            :reveal-delay="index * 70"
            @start="onSceneStart"
          />
        </component>
      </AnimatePresence>
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <view :key="activeCategoryId" class="plaza__grid plaza__grid--animate">
        <SceneItem
          v-for="(card, index) in activeCards"
          :key="card.id"
          :card="card"
          :reveal-delay="index * 70"
          @start="onSceneStart"
        />
      </view>
      <!-- #endif -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import SceneItem from "@/components/scene/SceneItem.vue";
import ScenePlazaFeaturedCoaches from "@/components/scene/ScenePlazaFeaturedCoaches.vue";
import { SCENE_PLAZA_DATA } from "@/data/defaults/scenePlaza";
import { toSceneCard } from "@/utils/scene/scenePlaza.js";
import type { SceneCardData, SceneCategoryId, ScenePlazaData } from "@/types/scene/plaza";
import type { Coach } from "@/types/coach/hall";

// #ifdef H5
import { AnimatePresence, motion } from "motion-v";

const MotionDiv = motion.div;
// #endif

const props = withDefaults(
  defineProps<{
    data?: ScenePlazaData;
    featuredCoaches?: Coach[];
    apiTitle?: string;
    apiSubtitle?: string;
  }>(),
  {
    data: () => SCENE_PLAZA_DATA,
    featuredCoaches: () => [],
    apiTitle: "",
    apiSubtitle: "",
  },
);

const emit = defineEmits<{
  (e: "start", card: SceneCardData): void;
  (e: "book-coach", coach: Coach): void;
}>();

const headerTitle = computed(() => props.apiTitle || props.data.sectionTitle);
const headerSubtitle = computed(() => props.apiSubtitle || props.data.sectionSubtitle);

const activeCategoryId = ref<SceneCategoryId>(
  props.data.categories[0]?.id ?? "job",
);

const activeCategory = computed(() =>
  props.data.categories.find((c) => c.id === activeCategoryId.value),
);

const activeCards = computed<SceneCardData[]>(() => {
  const cat = activeCategory.value;
  if (!cat) return [];
  return cat.scenes.map((sub) => toSceneCard(cat, sub));
});

function switchCategory(id: SceneCategoryId) {
  if (activeCategoryId.value === id) return;
  activeCategoryId.value = id;
}

function onSceneStart(card: SceneCardData) {
  emit("start", card);
}

function onCoachBook(coach: Coach) {
  emit("book-coach", coach);
}
</script>

<style scoped>
.plaza {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #f8fafc 48%, #ffffff 100%);
  box-sizing: border-box;
}

.plaza__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 96rpx 32rpx 80rpx;
  box-sizing: border-box;
}

/* #ifdef H5 */
@media (min-width: 768px) {
  .plaza__inner {
    padding-top: 100px;
    padding-bottom: 96px;
  }
}
/* #endif */

.plaza__header {
  max-width: 720px;
  margin: 0 auto 48rpx;
  text-align: center;
}
.plaza__title {
  display: block;
  font-family: Inter, "PingFang SC", ui-sans-serif, system-ui, sans-serif;
  font-size: 56rpx;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  line-height: 1.15;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .plaza__title {
    font-size: 42px;
  }
}
/* #endif */

.plaza__subtitle {
  display: block;
  margin-top: 24rpx;
  font-family: Inter, "PingFang SC", ui-sans-serif, system-ui, sans-serif;
  font-size: 28rpx;
  line-height: 1.75;
  color: #64748b;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .plaza__subtitle {
    font-size: 17px;
  }
}
/* #endif */

.plaza__tabs-scroll {
  width: 100%;
  margin-bottom: 44rpx;
  white-space: nowrap;
}
.plaza__tabs {
  display: inline-flex;
  flex-direction: row;
  gap: 16rpx;
  padding: 4rpx 8rpx;
  justify-content: center;
  min-width: 100%;
  box-sizing: border-box;
}
.plaza__tab {
  flex-shrink: 0;
  padding: 16rpx 28rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.88);
  border: 1rpx solid #e2e8f0;
  transition:
    background 0.32s ease,
    border-color 0.32s ease,
    box-shadow 0.32s ease,
    transform 0.32s ease;
}
.plaza__tab--active {
  background: #2563eb;
  border-color: #2563eb;
  box-shadow: 0 8rpx 22rpx rgba(37, 99, 235, 0.3);
  transform: translateY(-2rpx);
}
.plaza__tab-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #475569;
  transition: color 0.32s ease;
}
.plaza__tab--active .plaza__tab-text {
  color: #ffffff;
}

.plaza__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
  width: 100%;
}

/* #ifndef H5 */
.plaza__grid--animate {
  animation: grid-fade-in 0.38s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes grid-fade-in {
  from {
    opacity: 0;
    transform: translateY(24rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
/* #endif */

/* #ifdef H5 */
@media (max-width: 900px) {
  .plaza__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 520px) {
  .plaza__grid {
    grid-template-columns: 1fr;
  }
}
/* #endif */
</style>
