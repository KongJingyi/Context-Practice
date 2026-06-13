<template>
  <view class="ptr" :class="{ 'ptr--exit': exiting }">
    <view class="ptr-card">
      <view class="ptr-head">
        <view class="ptr-avatar">
          <text>{{ expertName.slice(0, 1) }}</text>
        </view>
        <view class="ptr-head-txt">
          <text class="ptr-title">本场训练复盘评价</text>
          <text class="ptr-sub">{{ expertName }} · {{ expertTitle || "语境专家" }}</text>
        </view>
      </view>

      <StarRating v-model="scores.professional" label="专业度" />
      <StarRating v-model="scores.attitude" label="服务态度" />
      <StarRating v-model="scores.quality" label="反馈质量" />

      <view class="ptr-tags-block">
        <text class="ptr-tags-lbl">正向标签</text>
        <view class="ptr-tags">
          <view
            v-for="t in positiveTags"
            :key="t"
            class="ptr-tag"
            :class="{ 'ptr-tag--on': selectedTags.includes(t) }"
            @tap="toggleTag(t)"
          >
            <text v-if="selectedTags.includes(t)" class="ptr-check">✓</text>
            <text>{{ t }}</text>
          </view>
        </view>
        <text class="ptr-tags-lbl ptr-tags-lbl--sub">待改进（可选）</text>
        <view class="ptr-tags">
          <view
            v-for="t in improveTags"
            :key="t"
            class="ptr-tag ptr-tag--warn"
            :class="{ 'ptr-tag--on': selectedTags.includes(t) }"
            @tap="toggleTag(t)"
          >
            <text v-if="selectedTags.includes(t)" class="ptr-check">✓</text>
            <text>{{ t }}</text>
          </view>
        </view>
      </view>

      <view class="ptr-text-wrap">
        <textarea
          v-model="content"
          class="ptr-textarea"
          placeholder="分享本次陪练的真实感受…"
          :maxlength="500"
        />
        <text class="ptr-count" :class="{ 'ptr-count--ok': content.length >= 20 }">
          {{ content.length }}/500 · 满 20 字可获得经验值奖励
        </text>
      </view>

      <view class="ptr-actions">
        <view class="ptr-skip" @tap="skip">
          <text>稍后评价</text>
        </view>
        <view
          class="ptr-submit"
          :class="{ 'ptr-submit--disabled': !canSubmit || submitting }"
          @tap="submit"
        >
          <text>{{ submitting ? "提交中…" : "提交评价" }}</text>
        </view>
      </view>
    </view>

    <view v-if="flyVisible" class="ptr-fly">
      <view class="ptr-fly-avatar">
        <text>{{ expertName.slice(0, 1) }}</text>
      </view>
      <text class="ptr-fly-txt">您的反馈已送达</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import StarRating from "@/components/review/StarRating.vue";
import { getReviewTags, submitReview } from "@/api/modules/review.js";
import type { ReviewContext, ReviewScores } from "@/types/review";

const props = defineProps<{
  context: ReviewContext;
}>();

const emit = defineEmits<{ (e: "done"): void; (e: "skip"): void }>();

const expertName = computed(() => props.context.expertName);
const expertTitle = computed(() => props.context.expertTitle);

const scores = ref<ReviewScores>({ professional: 5, attitude: 5, quality: 5 });
const selectedTags = ref<string[]>([]);
const content = ref("");
const positiveTags = ref<string[]>([]);
const improveTags = ref<string[]>([]);
const submitting = ref(false);
const exiting = ref(false);
const flyVisible = ref(false);

const canSubmit = computed(
  () =>
    scores.value.professional > 0 &&
    scores.value.attitude > 0 &&
    scores.value.quality > 0 &&
    content.value.trim().length >= 20,
);

onMounted(async () => {
  const tags = await getReviewTags();
  positiveTags.value = tags.positive;
  improveTags.value = tags.improve;
});

function toggleTag(t: string) {
  const i = selectedTags.value.indexOf(t);
  if (i >= 0) selectedTags.value = selectedTags.value.filter((x) => x !== t);
  else selectedTags.value = [...selectedTags.value, t];
}

async function submit() {
  if (!canSubmit.value || submitting.value) return;
  submitting.value = true;
  await submitReview({
    orderId: props.context.orderId,
    expertId: props.context.expertId || "c_default",
    scores: scores.value,
    tags: selectedTags.value,
    content: content.value.trim(),
    isAnonymous: false,
  });
  submitting.value = false;
  flyVisible.value = true;
  exiting.value = true;
  setTimeout(() => {
    uni.showToast({ title: "感谢您的真实反馈", icon: "success", duration: 2200 });
    emit("done");
  }, 1200);
}

function skip() {
  emit("skip");
}
</script>

<style scoped>
.ptr {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #f8fafc 100%);
  padding: 32rpx 24rpx calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  transition: opacity 0.5s ease, transform 0.5s ease;
}
.ptr--exit {
  opacity: 0;
  transform: scale(0.96);
}
.ptr-card {
  max-width: 640px;
  margin: 0 auto;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
  box-shadow: 0 8rpx 32rpx rgba(37, 99, 235, 0.08);
}
.ptr-head {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  margin-bottom: 32rpx;
  align-items: center;
}
.ptr-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #3b82f6, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.25);
}
.ptr-avatar text {
  font-size: 40rpx;
  font-weight: 800;
  color: #fff;
}
.ptr-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.ptr-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748b;
}

.ptr-tags-block {
  margin-bottom: 24rpx;
}
.ptr-tags-lbl {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #475569;
  margin-bottom: 12rpx;
}
.ptr-tags-lbl--sub {
  margin-top: 16rpx;
  color: #94a3b8;
}
.ptr-tags {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
}
.ptr-tag {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
  font-size: 24rpx;
  color: #64748b;
  transition: all 0.25s ease;
  cursor: pointer;
}
.ptr-tag--on {
  background: #eff6ff;
  border-color: #2563eb;
  color: #2563eb;
  font-weight: 700;
  transform: scale(1.02);
}
.ptr-tag--warn.ptr-tag--on {
  background: #fffbeb;
  border-color: #f59e0b;
  color: #b45309;
}
.ptr-check {
  font-size: 20rpx;
  font-weight: 800;
}

.ptr-text-wrap {
  position: relative;
  margin-bottom: 28rpx;
}
.ptr-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  background: #f8fafc;
  font-size: 26rpx;
  color: #0f172a;
  line-height: 1.6;
  box-sizing: border-box;
}
.ptr-count {
  display: block;
  text-align: right;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #94a3b8;
}
.ptr-count--ok {
  color: #2563eb;
  font-weight: 600;
}

.ptr-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}
.ptr-skip {
  flex: 1;
  padding: 20rpx;
  text-align: center;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
}
.ptr-skip text {
  font-size: 26rpx;
  color: #64748b;
  font-weight: 600;
}
.ptr-submit {
  flex: 2;
  padding: 20rpx;
  text-align: center;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  box-shadow: 0 6rpx 20rpx rgba(37, 99, 235, 0.3);
}
.ptr-submit--disabled {
  opacity: 0.45;
}
.ptr-submit text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}

.ptr-fly {
  position: fixed;
  left: 50%;
  top: 40%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: ptrFly 1s ease-out forwards;
  z-index: 100;
  pointer-events: none;
}
.ptr-fly-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 40rpx rgba(37, 99, 235, 0.5);
}
.ptr-fly-avatar text {
  font-size: 48rpx;
  color: #fff;
  font-weight: 800;
}
.ptr-fly-txt {
  margin-top: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #2563eb;
}
@keyframes ptrFly {
  0% {
    opacity: 0;
    transform: translate(-50%, 20%) scale(0.6);
  }
  30% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.05);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -120%) scale(0.4);
  }
}
</style>
