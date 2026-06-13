<template>
  <view class="cs">
    <view class="cs-head">
      <text class="cs-title">灵感广场</text>
      <text class="cs-sub">心得 · 高光 · 面经，一起练出职场表达力</text>
    </view>

    <ExpertTipsScroll v-if="experts.length" :tips="experts" />

    <view class="cs-tabs">
      <view
        v-for="t in tabs"
        :key="t.id"
        class="cs-tab"
        :class="{ 'cs-tab--on': filter === t.id }"
        @tap="switchFilter(t.id)"
      >
        <text>{{ t.label }}</text>
      </view>
    </view>

    <view v-if="loading" class="cs-loading">
      <view class="cs-loading-dot" />
      <view class="cs-loading-dot cs-loading-dot--2" />
      <view class="cs-loading-dot cs-loading-dot--3" />
      <text class="cs-loading-txt">正在加载职场智慧…</text>
    </view>

    <view v-else-if="!posts.length" class="cs-empty">
      <text class="cs-empty-ico">💡</text>
      <text class="cs-empty-txt">还没有动态，完成一次陪练后分享你的第一条心得吧</text>
    </view>

    <view v-else class="cs-masonry">
      <FeedCard
        v-for="p in posts"
        :key="p.id"
        :post="p"
        @comment="openComment"
        @update="onPostUpdate"
      />
    </view>

    <CommentDrawer
      :visible="drawerOpen"
      :post-id="activePostId"
      :count="activeCommentCount"
      @close="drawerOpen = false"
      @commented="onCommented"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import ExpertTipsScroll from "@/components/community/ExpertTipsScroll.vue";
import FeedCard from "@/components/community/FeedCard.vue";
import CommentDrawer from "@/components/community/CommentDrawer.vue";
import { fetchPosts, fetchExpertTips } from "@/api/modules/community.js";
import type { CommunityPost, ExpertTip, FeedFilter } from "@/types/community";

const tabs: { id: FeedFilter; label: string }[] = [
  { id: "hot", label: "热门" },
  { id: "latest", label: "最新" },
  { id: "highlight", label: "高光" },
];

const filter = ref<FeedFilter>("hot");
const loading = ref(true);
const posts = ref<CommunityPost[]>([]);
const experts = ref<ExpertTip[]>([]);

const drawerOpen = ref(false);
const activePostId = ref("");
const activeCommentCount = ref(0);

async function loadFeed() {
  loading.value = true;
  try {
    const [feed, tips] = await Promise.all([
      fetchPosts({ type: filter.value }),
      experts.value.length ? Promise.resolve(experts.value) : fetchExpertTips(),
    ]);
    posts.value = (feed || []).map((p: CommunityPost) => ({
      ...p,
      liked: false,
      collected: false,
    }));
    if (!experts.value.length) experts.value = tips || [];
  } finally {
    loading.value = false;
  }
}

function switchFilter(id: FeedFilter) {
  if (filter.value === id) return;
  filter.value = id;
  loadFeed();
}

function openComment(id: string) {
  const p = posts.value.find((x) => x.id === id);
  activePostId.value = id;
  activeCommentCount.value = p?.stats.comments ?? 0;
  drawerOpen.value = true;
}

function onPostUpdate(updated: CommunityPost) {
  posts.value = posts.value.map((p) => (p.id === updated.id ? updated : p));
}

function onCommented() {
  posts.value = posts.value.map((p) =>
    p.id === activePostId.value
      ? { ...p, stats: { ...p.stats, comments: p.stats.comments + 1 } }
      : p,
  );
  activeCommentCount.value += 1;
}

onMounted(loadFeed);
</script>

<style scoped>
.cs {
  min-height: 100%;
  background: #f0f7ff;
  padding: 24rpx 24rpx calc(120rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.cs-head {
  margin-bottom: 20rpx;
}
.cs-title {
  display: block;
  font-size: 40rpx;
  font-weight: 900;
  color: #0f172a;
}
.cs-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.cs-tabs {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  margin-bottom: 24rpx;
}
.cs-tab {
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.85);
  border: 1rpx solid #e2e8f0;
}
.cs-tab--on {
  background: #2563eb;
  border-color: #2563eb;
}
.cs-tab text {
  font-size: 24rpx;
  font-weight: 700;
  color: #64748b;
}
.cs-tab--on text {
  color: #fff;
}

.cs-masonry {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20rpx;
  width: 100%;
}
@media (min-width: 640px) {
  .cs-masonry {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (min-width: 960px) {
  .cs {
    max-width: 1100px;
    margin: 0 auto;
    padding: 32px 24px calc(80px + env(safe-area-inset-bottom));
  }
  .cs-masonry {
    grid-template-columns: repeat(3, 1fr);
  }
}

.cs-loading {
  padding: 120rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}
.cs-loading-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #2563eb;
  animation: csBounce 1.2s ease-in-out infinite;
}
.cs-loading-dot--2 {
  animation-delay: 0.15s;
}
.cs-loading-dot--3 {
  animation-delay: 0.3s;
}
@keyframes csBounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  40% {
    transform: translateY(-12rpx);
    opacity: 1;
  }
}
.cs-loading-txt {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #64748b;
  font-weight: 600;
}

.cs-empty {
  padding: 100rpx 40rpx;
  text-align: center;
}
.cs-empty-ico {
  font-size: 80rpx;
  display: block;
  animation: csPulse 2s ease-in-out infinite;
}
@keyframes csPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
}
.cs-empty-txt {
  display: block;
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #64748b;
  line-height: 1.6;
}
</style>
