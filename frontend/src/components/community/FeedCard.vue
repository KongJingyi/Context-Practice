<template>
  <view
    class="fc"
    :class="[`fc--${post.type}`, { 'fc--hover': hovering }]"
    @mouseenter="hovering = true"
    @mouseleave="hovering = false"
  >
    <view v-if="hovering && !post.collected" class="fc-collect" @tap.stop="toggleCollect">
      <text>☆ 收藏</text>
    </view>
    <view v-if="hovering && post.collected" class="fc-collect fc-collect--on" @tap.stop="toggleCollect">
      <text>★ 已收藏</text>
    </view>

    <view class="fc-head">
      <view class="fc-avatar"><text>{{ post.user.name.slice(0, 1) }}</text></view>
      <view class="fc-meta">
        <text class="fc-name">{{ post.user.name }}</text>
        <view class="fc-sub">
          <text v-if="post.user.medal" class="fc-medal">{{ post.user.medal }}</text>
          <text class="fc-time">{{ post.publishedAt }}</text>
        </view>
      </view>
    </view>

    <view v-if="post.type === 'interview'" class="fc-interview-tag">
      <text>{{ post.company }} · {{ post.role }}</text>
    </view>

    <text v-if="post.title" class="fc-title">{{ post.title }}</text>
    <text class="fc-content">{{ post.content }}</text>

    <HighlightPlayer
      v-if="post.type === 'highlight' && post.videoPreview"
      :src="post.videoPreview"
    />

    <view v-if="post.tags.length" class="fc-tags">
      <text v-for="tag in post.tags" :key="tag" class="fc-tag">{{ tag }}</text>
    </view>

    <view class="fc-foot">
      <view class="fc-action" @tap.stop="toggleLike">
        <view class="fc-like-wrap">
          <LikeBurst :active="burst" />
          <text class="fc-ico" :class="{ 'fc-ico--on': post.liked }">{{ post.liked ? "♥" : "♡" }}</text>
        </view>
        <text class="fc-stat" :class="{ 'fc-stat--pop': likePop }">{{ post.stats.likes }}</text>
      </view>
      <view class="fc-action" @tap.stop="emit('comment', post.id)">
        <text class="fc-ico">💬</text>
        <text class="fc-stat">{{ post.stats.comments }}</text>
      </view>
      <view class="fc-action" @tap.stop="toggleCollect">
        <text class="fc-ico" :class="{ 'fc-ico--on': post.collected }">{{ post.collected ? "★" : "☆" }}</text>
        <text class="fc-stat">{{ post.stats.collects }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import LikeBurst from "@/components/community/LikeBurst.vue";
import HighlightPlayer from "@/components/community/HighlightPlayer.vue";
import { likePost } from "@/api/modules/community.js";
import type { CommunityPost } from "@/types/community";

const props = defineProps<{ post: CommunityPost }>();
const emit = defineEmits<{ (e: "comment", id: string): void; (e: "update", post: CommunityPost): void }>();

const hovering = ref(false);
const burst = ref(false);
const likePop = ref(false);

async function toggleLike() {
  const liked = !props.post.liked;
  if (liked) {
    burst.value = true;
    setTimeout(() => {
      burst.value = false;
    }, 700);
  }
  likePop.value = true;
  setTimeout(() => {
    likePop.value = false;
  }, 420);
  await likePost(props.post.id, liked);
  emit("update", {
    ...props.post,
    liked,
    stats: {
      ...props.post.stats,
      likes: props.post.stats.likes + (liked ? 1 : -1),
    },
  });
}

function toggleCollect() {
  const collected = !props.post.collected;
  emit("update", {
    ...props.post,
    collected,
    stats: {
      ...props.post.stats,
      collects: props.post.stats.collects + (collected ? 1 : -1),
    },
  });
  uni.showToast({ title: collected ? "已收藏" : "已取消收藏", icon: "none" });
}
</script>

<style scoped>
.fc {
  position: relative;
  width: 100%;
  margin-bottom: 0;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.04);
  transition:
    transform 0.3s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.3s ease;
  box-sizing: border-box;
}
.fc--hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.1);
}
.fc--insight {
  border-top: 4rpx solid #93c5fd;
}
.fc--highlight {
  border-top: 4rpx solid #2563eb;
}
.fc--interview {
  border-top: 4rpx solid #6366f1;
}

.fc-collect {
  position: absolute;
  right: 20rpx;
  top: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #eff6ff;
  border: 1rpx solid #dbeafe;
  animation: fcFadeIn 0.25s ease;
  z-index: 2;
}
.fc-collect--on {
  background: #fffbeb;
  border-color: #fde68a;
}
.fc-collect text {
  font-size: 22rpx;
  font-weight: 700;
  color: #2563eb;
}
.fc-collect--on text {
  color: #b45309;
}
@keyframes fcFadeIn {
  from {
    opacity: 0;
    transform: translateY(4rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fc-head {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.fc-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #eff6ff, #dbeafe);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.fc-avatar text {
  font-size: 28rpx;
  font-weight: 800;
  color: #2563eb;
}
.fc-name {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}
.fc-sub {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-top: 4rpx;
}
.fc-medal {
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}
.fc-time {
  font-size: 22rpx;
  color: #94a3b8;
}

.fc-interview-tag {
  display: inline-block;
  margin-bottom: 12rpx;
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  background: #eef2ff;
}
.fc-interview-tag text {
  font-size: 22rpx;
  font-weight: 700;
  color: #4f46e5;
}

.fc-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 10rpx;
  line-height: 1.4;
}
.fc-content {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  font-size: 26rpx;
  color: #475569;
  line-height: 1.6;
  white-space: pre-wrap;
}

.fc-tags {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}
.fc-tag {
  font-size: 22rpx;
  color: #2563eb;
  background: #eff6ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.fc-foot {
  display: flex;
  flex-direction: row;
  gap: 32rpx;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f1f5f9;
}
.fc-action {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
}
.fc-like-wrap {
  position: relative;
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fc-ico {
  font-size: 28rpx;
  color: #94a3b8;
  transition: color 0.2s ease, transform 0.2s ease;
}
.fc-ico--on {
  color: #2563eb;
  transform: scale(1.15);
}
.fc-stat {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 600;
  transition: transform 0.2s ease;
}
.fc-stat--pop {
  animation: fcStatPop 0.42s cubic-bezier(0.22, 1, 0.36, 1);
  color: #2563eb;
}
@keyframes fcStatPop {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.35);
  }
  100% {
    transform: scale(1);
  }
}
</style>
