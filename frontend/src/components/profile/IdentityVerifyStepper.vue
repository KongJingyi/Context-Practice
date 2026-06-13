<template>
  <view class="iv">
    <view class="iv-glass" />
    <view class="iv-inner">
      <view class="iv-stepper">
        <view
          v-for="(s, i) in stepLabels"
          :key="s"
          class="iv-step"
          :class="{ 'iv-step--on': displayStep >= i, 'iv-step--cur': displayStep === i }"
        >
          <view class="iv-step-dot">{{ i + 1 }}</view>
          <text class="iv-step-txt">{{ s }}</text>
        </view>
        <view class="iv-step-line-bg">
          <view class="iv-step-line-fill" :style="{ width: stepLineWidth }" />
        </view>
      </view>

      <!-- 步骤 1：基础信息 -->
      <view v-if="displayStep === 0" class="iv-panel">
        <text class="iv-title">基础信息认证</text>
        <text class="iv-sub">请填写与证件一致的真实信息，提交后进入人工审核</text>

        <view class="iv-field" :class="{ 'iv-field--ok': nameValid, 'iv-field--err': nameTouched && !nameValid }">
          <text class="iv-label">真实姓名</text>
          <input
            v-model="form.realName"
            class="iv-input"
            placeholder="请输入真实姓名"
            maxlength="20"
            @blur="nameTouched = true"
          />
          <text v-if="nameTouched && !nameValid" class="iv-hint iv-hint--err">请输入 2–20 个字符</text>
        </view>

        <view
          class="iv-field"
          :class="{
            'iv-field--ok': idValid,
            'iv-field--err': idTouched && form.idCard && !idValid,
            'iv-field--shake': shakeId,
          }"
        >
          <text class="iv-label">身份证号</text>
          <input
            v-model="form.idCard"
            class="iv-input"
            placeholder="18 位身份证号码"
            maxlength="18"
            @input="onIdInput"
            @blur="onIdBlur"
          />
          <text v-if="idTouched && form.idCard && !idValid" class="iv-hint iv-hint--err">身份证号格式不正确</text>
          <text v-else-if="idValid" class="iv-hint iv-hint--ok">✓ 格式校验通过</text>
        </view>

        <view class="iv-btn iv-btn--primary" :class="{ 'iv-btn--disabled': !canGoUpload }" @tap="goUploadStep">
          <text>下一步：上传证件</text>
        </view>
      </view>

      <!-- 步骤 2：证件上传 -->
      <view v-else-if="displayStep === 1" class="iv-panel">
        <text class="iv-title">上传身份证件</text>
        <text class="iv-sub">支持点击或拖拽上传（H5），请确保四角完整、文字清晰</text>

        <view class="iv-upload-row">
          <IdUploadCard
            label="身份证人像面"
            :file-url="form.idCardFrontUrl"
            :progress="frontProgress"
            :done="frontDone"
            @pick="onPick('front')"
            @drop-file="onDrop('front', $event)"
          />
          <IdUploadCard
            label="身份证国徽面"
            :file-url="form.idCardBackUrl"
            :progress="backProgress"
            :done="backDone"
            @pick="onPick('back')"
            @drop-file="onDrop('back', $event)"
          />
        </view>

        <view class="iv-actions">
          <view class="iv-btn iv-btn--ghost" @tap="displayStep = 0">
            <text>上一步</text>
          </view>
          <view
            class="iv-btn iv-btn--primary"
            :class="{ 'iv-btn--disabled': !canSubmit || submitting }"
            @tap="onSubmit"
          >
            <text>{{ submitting ? "提交中…" : "提交审核" }}</text>
          </view>
        </view>
      </view>

      <!-- 步骤 3：审核中 / 已通过 -->
      <view v-else class="iv-panel iv-panel--center">
        <view v-if="verifyStore.isVerified" class="iv-status">
          <text class="iv-status-ico iv-status-ico--ok">✓</text>
          <text class="iv-status-title">已通过实名认证</text>
          <text class="iv-status-sub">您已获得「已认证」标识，可正常预约陪练</text>
        </view>
        <view v-else class="iv-status">
          <text class="iv-status-ico">🕐</text>
          <text class="iv-status-title">资料已提交，审核中</text>
          <text class="iv-status-sub">预计 1 个工作日内完成审核，请留意通知</text>
        </view>
        <view class="iv-btn iv-btn--primary" @tap="onDone">
          <text>返回个人中心</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from "vue";
import IdUploadCard from "@/components/profile/IdUploadCard.vue";
import { submitAuth, uploadFile } from "@/api/modules/verify.js";
import { validateChineseIdCard } from "@/utils/validate/idCard";
import { useVerifyStore } from "@/store/verify";

const verifyStore = useVerifyStore();

const stepLabels = ["基础信息", "证件上传", "审核结果"];
const displayStep = ref(0);

const form = ref({
  realName: "",
  idCard: "",
  idCardFrontUrl: "",
  idCardBackUrl: "",
});

const nameTouched = ref(false);
const idTouched = ref(false);
const shakeId = ref(false);
const submitting = ref(false);

const frontProgress = ref(0);
const backProgress = ref(0);
const frontDone = ref(false);
const backDone = ref(false);

const nameValid = computed(() => form.value.realName.trim().length >= 2);
const idValid = computed(() => validateChineseIdCard(form.value.idCard));
const canGoUpload = computed(() => nameValid.value && idValid.value);
const canSubmit = computed(() => frontDone.value && backDone.value);

const stepLineWidth = computed(() => `${(displayStep.value / (stepLabels.length - 1)) * 100}%`);

onMounted(async () => {
  await verifyStore.loadStatus();
  if (verifyStore.isPending || verifyStore.isVerified) {
    displayStep.value = 2;
  }
  if (verifyStore.isPending) {
    pollTimer = window.setInterval(async () => {
      await verifyStore.loadStatus();
      if (verifyStore.isVerified || verifyStore.status === "rejected") {
        if (pollTimer) {
          clearInterval(pollTimer);
          pollTimer = undefined;
        }
      }
    }, 10000);
  }
});

let pollTimer: number | undefined;

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

function onIdInput() {
  form.value.idCard = form.value.idCard.replace(/\s/g, "").toUpperCase();
}

function onIdBlur() {
  idTouched.value = true;
  if (form.value.idCard && !idValid.value) {
    shakeId.value = true;
    setTimeout(() => {
      shakeId.value = false;
    }, 500);
  }
}

function goUploadStep() {
  if (!canGoUpload.value) return;
  displayStep.value = 1;
}

async function handleUpload(side: "front" | "back", filePath: string) {
  const isFront = side === "front";
  if (isFront) {
    frontDone.value = false;
    frontProgress.value = 0;
  } else {
    backDone.value = false;
    backProgress.value = 0;
  }
  const { url } = await uploadFile(filePath, (p) => {
    if (isFront) frontProgress.value = p;
    else backProgress.value = p;
  }, side);
  if (isFront) {
    form.value.idCardFrontUrl = url;
    frontDone.value = true;
  } else {
    form.value.idCardBackUrl = url;
    backDone.value = true;
  }
}

function onPick(side: "front" | "back") {
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    success: (res) => {
      const path = res.tempFilePaths?.[0];
      if (path) handleUpload(side, path);
    },
  });
}

function onDrop(side: "front" | "back", path: string) {
  if (path) handleUpload(side, path);
}

async function onSubmit() {
  if (!canSubmit.value || submitting.value) return;
  submitting.value = true;
  try {
    await submitAuth({
      realName: form.value.realName.trim(),
      idCard: form.value.idCard.trim(),
      idCardFrontUrl: form.value.idCardFrontUrl,
      idCardBackUrl: form.value.idCardBackUrl,
    });
    await verifyStore.loadStatus();
    displayStep.value = 2;
    uni.showToast({ title: "提交成功", icon: "success" });
  } finally {
    submitting.value = false;
  }
}

function onDone() {
  uni.navigateBack({
    fail: () => uni.switchTab({ url: "/pages/profile/profile" }),
  });
}
</script>

<style scoped>
.iv {
  position: relative;
  min-height: 100vh;
  background: #f0f7ff;
  box-sizing: border-box;
}
.iv-glass {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(12px);
  pointer-events: none;
}
.iv-inner {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
  padding: 32rpx 28rpx 48rpx;
}
@media (min-width: 768px) {
  .iv-inner {
    padding: 40px 32px 64px;
  }
}

.iv-stepper {
  position: relative;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 40rpx;
  padding: 0 8rpx;
}
.iv-step-line-bg {
  position: absolute;
  left: 12%;
  right: 12%;
  top: 22rpx;
  height: 4rpx;
  background: #e2e8f0;
  z-index: 0;
  border-radius: 4rpx;
  overflow: hidden;
}
.iv-step-line-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  transition: width 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}
.iv-step {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  flex: 1;
}
.iv-step-dot {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #e2e8f0;
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}
.iv-step--on .iv-step-dot {
  background: #dbeafe;
  color: #2563eb;
}
.iv-step--cur .iv-step-dot {
  background: #2563eb;
  color: #fff;
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.35);
}
.iv-step-txt {
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 600;
}
.iv-step--cur .iv-step-txt {
  color: #2563eb;
}

.iv-panel {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 24rpx;
  padding: 36rpx 28rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 8rpx 32rpx rgba(15, 23, 42, 0.06);
}
.iv-panel--center {
  text-align: center;
}
.iv-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}
.iv-sub {
  display: block;
  margin: 12rpx 0 32rpx;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.55;
}

.iv-field {
  margin-bottom: 28rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  border: 2rpx solid #e2e8f0;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}
.iv-field--ok {
  border-color: #93c5fd;
  box-shadow: 0 0 0 3rpx rgba(59, 130, 246, 0.12);
}
.iv-field--err {
  border-color: #fca5a5;
}
.iv-field--shake {
  animation: ivShake 0.45s ease;
}
@keyframes ivShake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-8rpx);
  }
  75% {
    transform: translateX(8rpx);
  }
}
.iv-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #475569;
  margin-bottom: 12rpx;
}
.iv-input {
  width: 100%;
  font-size: 30rpx;
  color: #0f172a;
}
.iv-hint {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
}
.iv-hint--ok {
  color: #059669;
}
.iv-hint--err {
  color: #dc2626;
}

.iv-upload-row {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
@media (min-width: 640px) {
  .iv-upload-row {
    flex-direction: row;
  }
}

.iv-actions {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  margin-top: 32rpx;
}
.iv-actions .iv-btn {
  flex: 1;
}

.iv-btn {
  margin-top: 16rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}
.iv-btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.28);
}
.iv-btn--ghost {
  background: #fff;
  border: 2rpx solid #e2e8f0;
  color: #475569;
}
.iv-btn--disabled {
  opacity: 0.45;
  pointer-events: none;
}

.iv-status-ico {
  font-size: 80rpx;
  display: block;
  margin-bottom: 20rpx;
}
.iv-status-ico--ok {
  color: #059669;
}
.iv-status-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.iv-status-sub {
  display: block;
  margin: 16rpx 0 32rpx;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.6;
}
</style>
