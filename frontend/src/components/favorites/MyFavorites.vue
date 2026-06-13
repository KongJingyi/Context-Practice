<template>
  <view class="mf">
    <view class="mf-head">
      <text class="mf-title">我的收藏</text>
      <text class="mf-sub">职场智囊团 · 随时预约 · 再次开练</text>
    </view>

    <view class="mf-seg">
      <view
        class="mf-seg-item"
        :class="{ 'mf-seg-item--on': tab === 'experts' }"
        @tap="tab = 'experts'"
      >
        <text>收藏的专家</text>
      </view>
      <view
        class="mf-seg-item"
        :class="{ 'mf-seg-item--on': tab === 'scenes' }"
        @tap="tab = 'scenes'"
      >
        <text>收藏的场景</text>
      </view>
    </view>

    <FavoritesSkeleton v-if="loading" />

    <view v-else-if="currentEmpty" class="mf-empty">
      <text class="mf-empty-ico">{{ tab === 'experts' ? '👤' : '🎯' }}</text>
      <text class="mf-empty-title">
        {{ tab === 'experts' ? '还没有收藏专家' : '还没有收藏场景' }}
      </text>
      <text class="mf-empty-sub">快去发现适合你的专家吧</text>
      <view class="mf-empty-btn" @tap="goDiscover">
        <text>{{ tab === 'experts' ? '去陪练大厅' : '去场景广场' }}</text>
      </view>
    </view>

    <!-- #ifdef H5 -->
    <TransitionGroup v-if="!loading && !currentEmpty" name="mf-list" tag="div" class="mf-grid">
      <template v-if="tab === 'experts'">
        <view
          v-for="ex in experts"
          :key="ex.id"
          class="mf-card mf-card--expert"
          @mouseenter="hoverId = ex.id"
          @mouseleave="hoverId = ''"
        >
          <view class="mf-heart" @tap.stop="unfavorite('experts', ex.id)">
            <text>{{ hoverId === ex.id ? '♡' : '♥' }}</text>
          </view>
          <view v-if="hoverId === ex.id" class="mf-unfav-hint">
            <text>取消收藏</text>
          </view>
          <view class="mf-avatar"><text>{{ ex.name.slice(0, 1) }}</text></view>
          <text class="mf-name">{{ ex.name }}</text>
          <text v-if="ex.tags[0]" class="mf-tag">{{ ex.tags[0] }}</text>
          <view class="mf-book" @tap.stop="bookExpert(ex.id)">
            <text>立即预约</text>
          </view>
        </view>
      </template>
      <template v-else>
        <view v-for="sc in scenes" :key="sc.id" class="mf-card mf-card--scene">
          <view class="mf-heart mf-heart--scene" @tap.stop="unfavorite('scenes', sc.id)">
            <text>♥</text>
          </view>
          <text class="mf-scene-ico">{{ sceneIcon(sc.icon) }}</text>
          <text class="mf-name">{{ sc.title }}</text>
          <text v-if="sc.categoryLabel" class="mf-tag">{{ sc.categoryLabel }}</text>
          <text v-if="sc.lastPracticeLabel" class="mf-practice">最近练习 · {{ sc.lastPracticeLabel }}</text>
          <view class="mf-book mf-book--ghost" @tap.stop="practiceScene(sc.id)">
            <text>再练一次</text>
          </view>
        </view>
      </template>
    </TransitionGroup>
    <!-- #endif -->

    <!-- #ifndef H5 -->
    <view v-if="!loading && !currentEmpty" class="mf-grid">
      <view
        v-for="ex in tab === 'experts' ? experts : []"
        :key="ex.id"
        class="mf-card mf-card--expert"
        :class="{ 'mf-card--leaving': leavingId === ex.id }"
      >
        <view class="mf-heart" @tap.stop="unfavorite('experts', ex.id)"><text>♥</text></view>
        <view class="mf-avatar"><text>{{ ex.name.slice(0, 1) }}</text></view>
        <text class="mf-name">{{ ex.name }}</text>
        <text v-if="ex.tags[0]" class="mf-tag">{{ ex.tags[0] }}</text>
        <view class="mf-book" @tap.stop="bookExpert(ex.id)"><text>立即预约</text></view>
      </view>
      <view
        v-for="sc in tab === 'scenes' ? scenes : []"
        :key="sc.id"
        class="mf-card mf-card--scene"
        :class="{ 'mf-card--leaving': leavingId === sc.id }"
      >
        <view class="mf-heart" @tap.stop="unfavorite('scenes', sc.id)"><text>♥</text></view>
        <text class="mf-scene-ico">{{ sceneIcon(sc.icon) }}</text>
        <text class="mf-name">{{ sc.title }}</text>
        <text v-if="sc.lastPracticeLabel" class="mf-practice">最近练习 · {{ sc.lastPracticeLabel }}</text>
        <view class="mf-book mf-book--ghost" @tap.stop="practiceScene(sc.id)"><text>再练一次</text></view>
      </view>
    </view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, TransitionGroup } from "vue";
import FavoritesSkeleton from "@/components/favorites/FavoritesSkeleton.vue";
import { fetchFavorites, removeFavorite } from "@/api/modules/favorites.js";
import { showTopToast } from "@/utils/common/topToast";
import type { FavoriteExpert, FavoriteScene, FavoriteTab } from "@/types/user/settings";

const tab = ref<FavoriteTab>("experts");
const loading = ref(true);
const experts = ref<FavoriteExpert[]>([]);
const scenes = ref<FavoriteScene[]>([]);
const hoverId = ref("");
const leavingId = ref("");

const currentEmpty = computed(() =>
  tab.value === "experts" ? !experts.value.length : !scenes.value.length,
);

onMounted(async () => {
  loading.value = true;
  const data = await fetchFavorites();
  experts.value = data.experts;
  scenes.value = data.scenes;
  loading.value = false;
});

function sceneIcon(icon: string) {
  const map: Record<string, string> = {
    mic: "🎤",
    projector: "📊",
    spark: "✨",
    conflict: "⚡",
    pressure: "🔥",
    chat: "💬",
  };
  return map[icon] || "🎯";
}

async function unfavorite(type: FavoriteTab, id: string) {
  leavingId.value = id;
  hoverId.value = "";
  try {
    await new Promise((r) => setTimeout(r, 320));
    await removeFavorite(type, id);
    if (type === "experts") {
      experts.value = experts.value.filter((e) => e.id !== id);
    } else {
      scenes.value = scenes.value.filter((s) => s.id !== id);
    }
    showTopToast("已取消收藏", "success");
  } catch {
    /* toast handled */
  } finally {
    leavingId.value = "";
  }
}

function bookExpert(id: string) {
  uni.navigateTo({ url: `/pages/expert-booking/expert-booking?coachId=${id}` });
}

function practiceScene(id: string) {
  uni.navigateTo({ url: `/pages/coach-hall/coach-hall?sceneId=${id}` });
}

function goDiscover() {
  if (tab.value === "experts") {
    uni.navigateTo({ url: "/pages/coach-hall/coach-hall" });
  } else {
    uni.switchTab({ url: "/pages/scenes/scenes" });
  }
}
</script>

<style scoped>
.mf {
  padding: 8rpx 0 24rpx;
}
.mf-head {
  margin-bottom: 24rpx;
}
.mf-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}
.mf-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.mf-seg {
  display: flex;
  flex-direction: row;
  padding: 6rpx;
  margin-bottom: 24rpx;
  border-radius: 16rpx;
  background: #f1f5f9;
}
.mf-seg-item {
  flex: 1;
  padding: 14rpx;
  text-align: center;
  border-radius: 12rpx;
  transition: all 0.25s ease;
}
.mf-seg-item--on {
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(15, 23, 42, 0.06);
}
.mf-seg-item text {
  font-size: 26rpx;
  font-weight: 700;
  color: #64748b;
}
.mf-seg-item--on text {
  color: #2563eb;
}

.mf-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}
@media (min-width: 640px) {
  .mf-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.mf-card {
  position: relative;
  padding: 24rpx 20rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 1rpx solid #f1f5f9;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.3s ease;
}
.mf-card--leaving {
  transform: scale(0.85);
  opacity: 0;
}
.mf-list-leave-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.mf-list-leave-to {
  transform: scale(0.85);
  opacity: 0;
}
.mf-list-move {
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.mf-heart {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
}
.mf-heart text {
  font-size: 32rpx;
  color: #2563eb;
}
.mf-heart--scene text {
  color: #2563eb;
}
.mf-unfav-hint {
  position: absolute;
  top: 12rpx;
  right: 52rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: #fef2f2;
}
.mf-unfav-hint text {
  font-size: 20rpx;
  color: #ef4444;
  font-weight: 600;
}

.mf-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #eff6ff, #dbeafe);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}
.mf-avatar text {
  font-size: 32rpx;
  font-weight: 800;
  color: #2563eb;
}
.mf-scene-ico {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}
.mf-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 8rpx;
}
.mf-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
  margin-bottom: 12rpx;
}
.mf-practice {
  font-size: 20rpx;
  color: #94a3b8;
  margin-bottom: 12rpx;
}
.mf-book {
  width: 100%;
  padding: 12rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  margin-top: auto;
}
.mf-book--ghost {
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
}
.mf-book text {
  font-size: 22rpx;
  font-weight: 700;
  color: #fff;
}
.mf-book--ghost text {
  color: #2563eb;
}

.mf-empty {
  padding: 80rpx 40rpx;
  text-align: center;
}
.mf-empty-ico {
  font-size: 80rpx;
  display: block;
}
.mf-empty-title {
  display: block;
  margin-top: 20rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.mf-empty-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #64748b;
}
.mf-empty-btn {
  display: inline-block;
  margin-top: 28rpx;
  padding: 16rpx 40rpx;
  border-radius: 999rpx;
  background: #2563eb;
}
.mf-empty-btn text {
  font-size: 26rpx;
  font-weight: 700;
  color: #fff;
}
</style>
