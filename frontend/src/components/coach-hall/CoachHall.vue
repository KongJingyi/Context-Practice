<template>
  <view class="coach-hall">
    <view class="coach-hall__inner">
      <CoachHallHeader
        :category-label="header.categoryLabel"
        :sub-scene-name="header.subSceneName"
        :guide="header.guide"
      />

      <CoachAdvancedFilter
        :filters="filterParams"
        :scene-options="sceneOptions"
        @update:filters="onFiltersUpdate"
        @apply="applyFilters"
        @ai-match="startAiMatch"
      />

      <view class="coach-hall__toolbar">
        <text class="coach-hall__count">
          {{ listLoading ? "加载中…" : `共 ${displayCoaches.length} 位陪练` }}
        </text>
        <view class="coach-hall__sort">
          <view
            class="coach-hall__sort-pill"
            :class="{ 'coach-hall__sort-pill--active': filterParams.sortBy === 'rating' }"
            @tap="setSort('rating')"
          >
            <text>好评率</text>
          </view>
          <view
            class="coach-hall__sort-pill"
            :class="{ 'coach-hall__sort-pill--active': filterParams.sortBy === 'activity' }"
            @tap="setSort('activity')"
          >
            <text>活跃度</text>
          </view>
        </view>
      </view>

      <view v-if="listLoading || matchScanning" class="coach-hall__grid">
        <CoachCardSkeleton v-for="n in 6" :key="`sk-${n}`" />
      </view>

      <!-- #ifdef H5 -->
      <TransitionGroup
        v-else-if="displayCoaches.length"
        name="coach-flip"
        tag="div"
        class="coach-hall__grid"
      >
        <CoachCard
          v-for="coach in displayCoaches"
          :key="coach.id"
          :coach="coach"
          :compare-selected="compareIds.includes(coach.id)"
          :highlighted="highlightIds.has(coach.id)"
          @detail="openDetail"
          @book="onBook"
          @toggle-compare="toggleCompare"
        />
      </TransitionGroup>
      <!-- #endif -->

      <!-- #ifndef H5 -->
      <view v-else-if="displayCoaches.length" class="coach-hall__grid">
        <CoachCard
          v-for="coach in displayCoaches"
          :key="coach.id"
          :coach="coach"
          :compare-selected="compareIds.includes(coach.id)"
          :highlighted="highlightIds.has(coach.id)"
          @detail="openDetail"
          @book="onBook"
          @toggle-compare="toggleCompare"
        />
      </view>
      <!-- #endif -->

      <view v-else class="coach-hall__empty">
        <text>暂无符合筛选条件的陪练，请调整筛选条件后重试</text>
      </view>
    </view>

    <CoachDetailModal
      :visible="detailVisible"
      :coach="selectedCoach"
      @close="detailVisible = false"
      @book="onBookFromModal"
    />

    <CoachMatchModal
      :visible="matchVisible"
      :phase="matchPhase"
      :items="matchItems"
      @close="closeMatch"
      @select="onMatchSelect"
    />

    <CoachCompareBar
      :coaches="compareCoaches"
      @remove="removeCompare"
      @compare="compareVisible = true"
    />

    <CoachComparePanel
      :visible="compareVisible"
      :coaches="compareCoaches"
      @close="compareVisible = false"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, TransitionGroup } from "vue";
import CoachHallHeader from "@/components/coach-hall/CoachHallHeader.vue";
import CoachAdvancedFilter from "@/components/coach-hall/CoachAdvancedFilter.vue";
import CoachCard from "@/components/coach-hall/CoachCard.vue";
import CoachCardSkeleton from "@/components/coach-hall/CoachCardSkeleton.vue";
import CoachDetailModal from "@/components/coach-hall/CoachDetailModal.vue";
import CoachMatchModal from "@/components/coach-hall/CoachMatchModal.vue";
import CoachCompareBar from "@/components/coach-hall/CoachCompareBar.vue";
import CoachComparePanel from "@/components/coach-hall/CoachComparePanel.vue";
import { fetchCoaches, applyClientFilters } from "@/api/modules/coach.js";
import { fetchSmartMatch } from "@/api/modules/match.js";
import { getAllSceneOptions, resolveSceneById } from "@/utils/scene/sceneContext";
import { decodeQueryParam } from "@/utils/common/queryString.js";
import {
  createDefaultFilterParams,
  type Coach,
  type CoachFilterParams,
  type CoachSortBy,
  type SmartMatchItem,
} from "@/types/coach/hall";

const props = defineProps<{
  sceneId: string;
  subSceneName?: string;
}>();

const sceneOptions = getAllSceneOptions();

const filterParams = ref<CoachFilterParams>(
  createDefaultFilterParams(props.sceneId, props.subSceneName ?? ""),
);

const allCoaches = ref<Coach[]>([]);
const listLoading = ref(true);
const lastFetchedSceneId = ref("");

const detailVisible = ref(false);
const selectedCoach = ref<Coach | null>(null);

const matchVisible = ref(false);
const matchPhase = ref<"scanning" | "result">("scanning");
const matchItems = ref<SmartMatchItem[]>([]);
const matchScanning = ref(false);
const highlightIds = ref<Set<string>>(new Set());

const compareIds = ref<string[]>([]);
const compareVisible = ref(false);

const compareCoaches = computed(() =>
  compareIds.value
    .map((id) => allCoaches.value.find((c) => c.id === id))
    .filter((c): c is Coach => Boolean(c)),
);

function resolveDisplayTitle(sceneId: string, queryName?: string) {
  const fromPlaza = resolveSceneById(sceneId)?.sub.title;
  if (fromPlaza) return fromPlaza;
  return decodeQueryParam(queryName) || "陪练大厅";
}

function syncNavigationTitle() {
  const title = resolveDisplayTitle(
    filterParams.value.sceneId,
    filterParams.value.subSceneName,
  );
  if (title) {
    uni.setNavigationBarTitle({ title });
  }
}

const header = computed(() => {
  const ctx = resolveSceneById(filterParams.value.sceneId);
  const guide = ctx
    ? `${ctx.sub.description}。建议提前梳理关键论据，陪练将按真实职场节奏进行追问与复盘。`
    : "选择场景后即可查看对应陪练与训练攻略。";
  return {
    categoryLabel: ctx?.category.label ?? "场景训练",
    subSceneName: resolveDisplayTitle(filterParams.value.sceneId, filterParams.value.subSceneName),
    guide,
  };
});

/** 客户端即时过滤，不整页刷新 */
const displayCoaches = computed(() => applyClientFilters(allCoaches.value, filterParams.value));

async function loadCoaches() {
  const id = filterParams.value.sceneId;
  if (!id) return;
  listLoading.value = true;
  try {
    allCoaches.value = await fetchCoaches(id, filterParams.value);
    lastFetchedSceneId.value = id;
  } finally {
    listLoading.value = false;
  }
}

function onFiltersUpdate(next: CoachFilterParams) {
  const sceneChanged = next.sceneId !== filterParams.value.sceneId;
  filterParams.value = next;
  if (sceneChanged) {
    const ctx = resolveSceneById(next.sceneId);
    if (ctx) {
      filterParams.value.subSceneName = ctx.sub.title;
    }
    syncNavigationTitle();
    loadCoaches();
  }
}

function applyFilters() {
  if (filterParams.value.sceneId !== lastFetchedSceneId.value) {
    loadCoaches();
  }
}

function setSort(sortBy: CoachSortBy) {
  filterParams.value.sortBy = sortBy;
}

async function startAiMatch() {
  matchVisible.value = true;
  matchPhase.value = "scanning";
  matchItems.value = [];
  matchScanning.value = true;
  highlightIds.value = new Set();

  try {
    const res = await fetchSmartMatch({
      sceneId: filterParams.value.sceneId,
    });
    matchPhase.value = "result";
    matchItems.value = res.items;
    highlightIds.value = new Set(res.items.map((i) => i.coach.id));
  } finally {
    matchScanning.value = false;
  }
}

function closeMatch() {
  matchVisible.value = false;
}

function onMatchSelect(coach: Coach) {
  matchVisible.value = false;
  openDetail(coach);
}

function toggleCompare(coach: Coach) {
  const ids = [...compareIds.value];
  const i = ids.indexOf(coach.id);
  if (i >= 0) {
    ids.splice(i, 1);
  } else if (ids.length < 2) {
    ids.push(coach.id);
  } else {
    uni.showToast({ title: "最多选择 2 位对比", icon: "none" });
    return;
  }
  compareIds.value = ids;
}

function removeCompare(id: string) {
  compareIds.value = compareIds.value.filter((x) => x !== id);
}

function openDetail(coach: Coach) {
  selectedCoach.value = coach;
  detailVisible.value = true;
}

function onBook(coach: Coach) {
  const scene = filterParams.value.subSceneName || "专项训练";
  const sceneCode = filterParams.value.sceneId || "INTERVIEW";
  uni.navigateTo({
    url: `/pages/expert-booking/expert-booking?id=${encodeURIComponent(coach.id)}&sceneTag=${encodeURIComponent(scene)}&sceneCode=${encodeURIComponent(sceneCode)}`,
  });
}

function onBookFromModal(coach: Coach) {
  detailVisible.value = false;
  onBook(coach);
}

watch(
  () => [props.sceneId, props.subSceneName] as const,
  ([id, name]) => {
    if (!id) return;
    filterParams.value = {
      ...filterParams.value,
      sceneId: id,
      subSceneName: resolveDisplayTitle(id, name),
    };
    syncNavigationTitle();
    loadCoaches();
  },
  { immediate: true },
);
</script>

<style scoped>
.coach-hall {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #f8fafc 45%, #ffffff 100%);
  box-sizing: border-box;
  padding-bottom: 120rpx;
}
.coach-hall__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32rpx 28rpx 80rpx;
  box-sizing: border-box;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .coach-hall__inner {
    padding: 40px 32px 96px;
  }
}
/* #endif */

.coach-hall__toolbar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  flex-wrap: wrap;
  gap: 16rpx;
}
.coach-hall__count {
  font-size: 26rpx;
  color: #64748b;
  font-weight: 500;
}
.coach-hall__sort {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
}
.coach-hall__sort-pill {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.9);
  border: 1rpx solid #e2e8f0;
  transition: all 0.25s ease;
}
.coach-hall__sort-pill--active {
  background: #eff6ff;
  border-color: #93c5fd;
}
.coach-hall__sort-pill text {
  font-size: 24rpx;
  color: #475569;
  font-weight: 600;
}
.coach-hall__sort-pill--active text {
  color: #2563eb;
}

.coach-hall__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
  position: relative;
}
/* #ifdef H5 */
@media (max-width: 1000px) {
  .coach-hall__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 560px) {
  .coach-hall__grid {
    grid-template-columns: 1fr;
  }
}

.coach-flip-move {
  transition: transform 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}
.coach-flip-enter-active,
.coach-flip-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.coach-flip-enter-from,
.coach-flip-leave-to {
  opacity: 0;
  transform: scale(0.94);
}
.coach-flip-leave-active {
  position: absolute;
  width: calc((100% - 48rpx) / 3);
}
@media (max-width: 1000px) {
  .coach-flip-leave-active {
    width: calc((100% - 24rpx) / 2);
  }
}
@media (max-width: 560px) {
  .coach-flip-leave-active {
    width: 100%;
  }
}
/* #endif */

.coach-hall__empty {
  padding: 80rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #94a3b8;
}
</style>
