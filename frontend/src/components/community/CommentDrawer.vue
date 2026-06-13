<template>
  <view v-if="visible" class="cd-mask" @tap="emit('close')">
    <view class="cd" @tap.stop>
      <view class="cd-handle" />
      <view class="cd-head">
        <text class="cd-title">评论 {{ count }}</text>
        <text class="cd-close" @tap="emit('close')">✕</text>
      </view>

      <scroll-view class="cd-list" scroll-y>
        <view v-for="c in comments" :key="c.id" class="cd-item">
          <view class="cd-avatar"><text>{{ c.userName.slice(0, 1) }}</text></view>
          <view class="cd-body">
            <text class="cd-user">{{ c.userName }}</text>
            <text class="cd-text">{{ c.content }}</text>
            <text class="cd-time">{{ c.createdAt }}</text>
          </view>
        </view>
        <view v-if="!comments.length" class="cd-empty">
          <text>还没有评论，来抢沙发吧</text>
        </view>
      </scroll-view>

      <view class="cd-input-wrap" :class="{ 'cd-input-wrap--focus': focused }">
        <textarea
          v-model="draft"
          class="cd-input"
          placeholder="写下你的想法… 输入 @ 提及专家，# 添加话题"
          :maxlength="500"
          @focus="focused = true"
          @blur="onBlur"
          @input="onInput"
        />
        <view v-if="mentionOpen" class="cd-pop">
          <view
            v-for="u in mentionUsers"
            :key="u.id"
            class="cd-pop-item"
            @tap="insertMention(u.name)"
          >
            <text class="cd-pop-name">{{ u.name }}</text>
            <text class="cd-pop-role">{{ u.role }}</text>
          </view>
        </view>
        <view v-if="tagOpen" class="cd-pop">
          <view
            v-for="tag in hotTags"
            :key="tag"
            class="cd-pop-item"
            @tap="insertTag(tag)"
          >
            <text class="cd-pop-name">{{ tag }}</text>
          </view>
        </view>
        <view class="cd-send" @tap="submit">
          <text>发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { fetchPostComments, commentPost, MENTION_USERS, HOT_TAGS } from "@/api/modules/community.js";
import { filterSensitiveText } from "@/utils/community/sensitiveFilter";
import type { PostComment } from "@/types/community";

const props = defineProps<{
  visible: boolean;
  postId: string;
  count: number;
}>();

const emit = defineEmits<{ (e: "close"): void; (e: "commented"): void }>();

const comments = ref<PostComment[]>([]);
const draft = ref("");
const focused = ref(false);
const mentionOpen = ref(false);
const tagOpen = ref(false);
const mentionUsers = MENTION_USERS;
const hotTags = HOT_TAGS;

watch(
  () => [props.visible, props.postId] as const,
  async ([vis, id]) => {
    if (vis && id) {
      comments.value = await fetchPostComments(id);
      draft.value = "";
      mentionOpen.value = false;
      tagOpen.value = false;
    }
  },
);

function onInput() {
  const v = draft.value;
  const last = v.slice(-1);
  mentionOpen.value = last === "@";
  tagOpen.value = last === "#";
}

function onBlur() {
  setTimeout(() => {
    focused.value = false;
    mentionOpen.value = false;
    tagOpen.value = false;
  }, 200);
}

function insertMention(name: string) {
  draft.value = draft.value.replace(/@$/, `@${name} `);
  mentionOpen.value = false;
}

function insertTag(tag: string) {
  draft.value = draft.value.replace(/#$/, `${tag} `);
  tagOpen.value = false;
}

async function submit() {
  const check = filterSensitiveText(draft.value);
  if (!check.ok) {
    uni.showToast({ title: check.message || "发送失败", icon: "none" });
    return;
  }
  const c = await commentPost(props.postId, { content: draft.value.trim(), parent_id: null });
  comments.value = [c as PostComment, ...comments.value];
  draft.value = "";
  emit("commented");
  uni.showToast({ title: "评论已发布", icon: "success" });
}
</script>

<style scoped>
.cd-mask {
  position: fixed;
  inset: 0;
  z-index: 900;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  animation: cdFadeIn 0.25s ease;
}
@keyframes cdFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
.cd {
  height: 50vh;
  min-height: 360rpx;
  border-radius: 24rpx 24rpx 0 0;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.6);
  display: flex;
  flex-direction: column;
  animation: cdSlideUp 0.35s cubic-bezier(0.22, 1, 0.36, 1);
  padding-bottom: env(safe-area-inset-bottom);
}
@keyframes cdSlideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}
.cd-handle {
  width: 64rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: #cbd5e1;
  margin: 12rpx auto 8rpx;
}
.cd-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 28rpx 16rpx;
}
.cd-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.cd-close {
  font-size: 32rpx;
  color: #94a3b8;
  padding: 8rpx;
}
.cd-list {
  flex: 1;
  padding: 0 28rpx;
  box-sizing: border-box;
}
.cd-item {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.cd-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cd-avatar text {
  font-size: 26rpx;
  font-weight: 700;
  color: #2563eb;
}
.cd-user {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #0f172a;
}
.cd-text {
  display: block;
  font-size: 26rpx;
  color: #334155;
  line-height: 1.5;
  margin-top: 4rpx;
}
.cd-time {
  display: block;
  font-size: 20rpx;
  color: #94a3b8;
  margin-top: 6rpx;
}
.cd-empty {
  padding: 48rpx;
  text-align: center;
  color: #94a3b8;
  font-size: 26rpx;
}

.cd-input-wrap {
  position: relative;
  padding: 16rpx 28rpx 24rpx;
  border-top: 1rpx solid rgba(226, 232, 240, 0.8);
}
.cd-input-wrap--focus .cd-input {
  border-color: #2563eb;
  box-shadow: 0 0 0 4rpx rgba(37, 99, 235, 0.12);
  animation: cdBreath 2s ease-in-out infinite;
}
@keyframes cdBreath {
  0%,
  100% {
    box-shadow: 0 0 0 4rpx rgba(37, 99, 235, 0.12);
  }
  50% {
    box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.18);
  }
}
.cd-input {
  width: 100%;
  min-height: 80rpx;
  max-height: 160rpx;
  padding: 16rpx 120rpx 16rpx 20rpx;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  background: rgba(255, 255, 255, 0.9);
  font-size: 26rpx;
  color: #0f172a;
  box-sizing: border-box;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.cd-send {
  position: absolute;
  right: 40rpx;
  bottom: 36rpx;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #2563eb;
}
.cd-send text {
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
}
.cd-pop {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 100%;
  margin-bottom: 8rpx;
  max-height: 240rpx;
  overflow-y: auto;
  border-radius: 16rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.1);
  z-index: 2;
}
.cd-pop-item {
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid #f1f5f9;
}
.cd-pop-name {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
}
.cd-pop-role {
  font-size: 22rpx;
  color: #64748b;
}
</style>
