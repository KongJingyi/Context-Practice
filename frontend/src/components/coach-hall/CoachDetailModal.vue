<template>
  <view v-if="visible && coach" class="modal-mask" @tap="emit('close')">
    <scroll-view scroll-y class="modal-scroll" @tap.stop>
      <view class="modal">
        <view class="modal__head">
          <view class="modal__avatar-wrap">
            <view class="modal__avatar">
              <text class="modal__avatar-letter">{{ coach.name.slice(0, 1) }}</text>
            </view>
            <view v-if="coach.online" class="modal__online" />
            <view v-if="coach.availableToday" class="modal__today-dot" />
          </view>
          <view class="modal__info">
            <view class="modal__name-row">
              <text class="modal__name">{{ coach.name }}</text>
              <text class="modal__lv">Lv.{{ coach.levelNum }}</text>
            </view>
            <text class="modal__title">{{ coach.jobTitle }}</text>
            <text class="modal__rating">★ {{ coach.rating.toFixed(1) }} · {{ coach.orderCount }} 单</text>
          </view>
          <text class="modal__close" @tap="emit('close')">×</text>
        </view>

        <view v-if="detail" class="modal__radar-wrap">
          <text class="modal__section-title">五维能力画像</text>
          <CoachRadarChart :radar="detail.radar" />
        </view>

        <text class="modal__bio">{{ coach.bio }}</text>

        <view class="modal__section">
          <text class="modal__section-title">擅长亮点</text>
          <text v-for="(h, i) in coach.highlights" :key="i" class="modal__bullet">· {{ h }}</text>
        </view>

        <view v-if="detail" class="modal__section">
          <text class="modal__section-title">资质证书</text>
          <scroll-view scroll-x class="modal__cert-scroll" :show-scrollbar="false">
            <view class="modal__cert-row">
              <view
                v-for="cert in detail.certificates"
                :key="cert.id"
                class="modal__cert"
                @tap="previewCert(cert)"
              >
                <view class="modal__cert-glass">
                  <text class="modal__cert-ico">📜</text>
                  <text class="modal__cert-title">{{ cert.title }}</text>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>

        <view v-if="detail" class="modal__section">
          <text class="modal__section-title">成功案例</text>
          <view class="modal__stories">
            <view v-for="s in detail.successStories" :key="s.id" class="modal__story">
              <text class="modal__story-metric">{{ s.metric }}</text>
              <text class="modal__story-title">{{ s.title }}</text>
              <text class="modal__story-sub">{{ s.subtitle }}</text>
            </view>
          </view>
        </view>

        <view v-if="detail" class="modal__reviews">
          <text class="modal__section-title">学员真实评价</text>
          <view class="modal__reviews-body">
            <CoachRatingBar :distribution="detail.ratingDistribution" />
          </view>
          <view v-for="rv in reviewSnippets" :key="rv.id" class="modal__review-item">
            <view class="modal__review-head">
              <text class="modal__review-user">{{ rv.userName }}</text>
              <text class="modal__review-score">★ {{ rv.score }}</text>
            </view>
            <text class="modal__review-txt">「{{ rv.content }}」</text>
            <view class="modal__review-tags">
              <text v-for="tag in rv.tags" :key="tag" class="modal__review-tag">{{ tag }}</text>
            </view>
          </view>
        </view>

        <view class="modal__foot">
          <view class="modal__btn modal__btn--ghost" @tap="emit('close')">
            <text>关闭</text>
          </view>
          <view class="modal__btn modal__btn--primary" @tap="emit('book', coach)">
            <text class="modal__btn-text">立即预约</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import CoachRatingBar from "@/components/coach-hall/charts/CoachRatingBar.vue";
import CoachRadarChart from "@/components/coach-hall/charts/CoachRadarChart.vue";
import { fetchCoachDetail } from "@/api/modules/coach.js";
import { fetchCoachReviews } from "@/api/modules/review.js";
import type { Coach, CoachCertificate, CoachDetail } from "@/types/coach/hall";
import type { CoachReviewSnippet } from "@/types/review";

const props = defineProps<{
  visible: boolean;
  coach: Coach | null;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "book", coach: Coach): void;
}>();

const detail = ref<CoachDetail | null>(null);
const reviewSnippets = ref<CoachReviewSnippet[]>([]);

watch(
  () => [props.visible, props.coach?.id] as const,
  async ([vis, id]) => {
    if (vis && id) {
      const [d, reviews] = await Promise.all([
        fetchCoachDetail(id),
        fetchCoachReviews(id),
      ]);
      detail.value = d;
      reviewSnippets.value = reviews;
    } else {
      detail.value = null;
      reviewSnippets.value = [];
    }
  },
  { immediate: true },
);

function previewCert(cert: CoachCertificate) {
  if (cert.imageUrl) {
    uni.previewImage({ urls: [cert.imageUrl] });
    return;
  }
  uni.showToast({ title: cert.title, icon: "none" });
}
</script>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
}
.modal-scroll {
  height: 100%;
  box-sizing: border-box;
}
.modal {
  width: 100%;
  max-width: 680rpx;
  margin: 48rpx auto;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 24rpx 48rpx rgba(15, 23, 42, 0.18);
  box-sizing: border-box;
}
.modal__head {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  align-items: flex-start;
  margin-bottom: 24rpx;
}
.modal__avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.modal__avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #dbeafe, #eff6ff);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal__avatar-letter {
  font-size: 40rpx;
  font-weight: 700;
  color: #2563eb;
}
.modal__online {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #22c55e;
  border: 3rpx solid #fff;
}
.modal__today-dot {
  position: absolute;
  left: 0;
  top: 0;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #22c55e;
  border: 2rpx solid #fff;
}
.modal__info {
  flex: 1;
  min-width: 0;
}
.modal__name-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
}
.modal__name {
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.modal__lv {
  font-size: 22rpx;
  font-weight: 800;
  color: #2563eb;
  background: #eff6ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.modal__title {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748b;
}
.modal__rating {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #f59e0b;
}
.modal__close {
  font-size: 44rpx;
  color: #94a3b8;
  line-height: 1;
  padding: 0 8rpx;
}

.modal__radar-wrap {
  margin-bottom: 24rpx;
  padding: 16rpx;
  border-radius: 16rpx;
  background: #f8fafc;
  border: 1rpx solid #f1f5f9;
}

.modal__bio {
  font-size: 26rpx;
  line-height: 1.65;
  color: #475569;
}
.modal__section {
  margin-top: 28rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f1f5f9;
}
.modal__section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 16rpx;
}
.modal__bullet {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 6rpx;
}

.modal__cert-scroll {
  white-space: nowrap;
}
.modal__cert-row {
  display: inline-flex;
  flex-direction: row;
  gap: 16rpx;
}
.modal__cert {
  width: 200rpx;
  flex-shrink: 0;
}
.modal__cert-glass {
  padding: 20rpx 16rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.06);
  text-align: center;
}
.modal__cert-ico {
  font-size: 40rpx;
  display: block;
  margin-bottom: 8rpx;
}
.modal__cert-title {
  font-size: 22rpx;
  font-weight: 600;
  color: #334155;
  white-space: normal;
  line-height: 1.4;
}

.modal__stories {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.modal__story {
  padding: 24rpx 20rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 45%, #ffffff 100%);
  border: 1rpx solid rgba(251, 191, 36, 0.35);
  position: relative;
}
.modal__story-metric {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  font-size: 22rpx;
  font-weight: 800;
  color: #b45309;
}
.modal__story-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  padding-right: 100rpx;
}
.modal__story-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #78716c;
  line-height: 1.5;
}

.modal__reviews {
  margin-top: 28rpx;
}
.modal__reviews-body {
  padding: 12rpx;
  border-radius: 12rpx;
  background: #f8fafc;
  margin-bottom: 16rpx;
}
.modal__review-item {
  margin-top: 16rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
}
.modal__review-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 8rpx;
}
.modal__review-user {
  font-size: 24rpx;
  font-weight: 700;
  color: #0f172a;
}
.modal__review-score {
  font-size: 22rpx;
  color: #f59e0b;
  font-weight: 700;
}
.modal__review-txt {
  font-size: 24rpx;
  color: #475569;
  line-height: 1.55;
  font-style: italic;
}
.modal__review-tags {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 10rpx;
}
.modal__review-tag {
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  background: #eff6ff;
  color: #2563eb;
}

.modal__foot {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 32rpx;
  padding-bottom: 24rpx;
}
.modal__btn {
  flex: 1;
  padding: 18rpx;
  border-radius: 12rpx;
  text-align: center;
  font-size: 26rpx;
}
.modal__btn--ghost {
  border: 1rpx solid #e2e8f0;
  color: #475569;
}
.modal__btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}
.modal__btn-text {
  color: #fff;
  font-weight: 600;
}
</style>
