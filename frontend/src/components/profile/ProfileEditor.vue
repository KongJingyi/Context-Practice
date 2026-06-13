<template>
  <view class="pe">
    <view class="pe-head">
      <text class="pe-title">个人信息</text>
      <view
        class="pe-save"
        :class="{ 'pe-save--on': dirty, 'pe-save--disabled': !dirty || saving }"
        @tap="onSave"
      >
        <text>保存修改</text>
      </view>
    </view>

    <view
      class="pe-avatar-wrap"
      @mouseenter="hoverAvatar = true"
      @mouseleave="hoverAvatar = false"
      @tap="onPickAvatar"
    >
      <image v-if="draft.avatarUrl" class="pe-avatar" :src="draft.avatarUrl" mode="aspectFill" />
      <view v-else class="pe-avatar pe-avatar--ph">
        <text>{{ draft.nickname.slice(0, 1) || "学" }}</text>
      </view>
      <!-- #ifdef H5 -->
      <view v-if="hoverAvatar" class="pe-avatar-mask">
        <text class="pe-mask-txt">更换头像</text>
      </view>
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <view class="pe-avatar-mask pe-avatar-mask--always">
        <text class="pe-mask-txt">更换头像</text>
      </view>
      <!-- #endif -->
    </view>

    <view class="pe-field">
      <text class="pe-label">昵称</text>
      <input
        v-model="draft.nickname"
        class="pe-input"
        type="text"
        maxlength="15"
        placeholder="1-15 个字符"
        placeholder-class="pe-input-placeholder"
        :adjust-position="true"
      />
    </view>

    <view class="pe-field">
      <text class="pe-label">训练目标</text>
      <view class="pe-goals">
        <view
          v-for="g in goalOptions"
          :key="g.id"
          class="pe-goal"
          :class="{ 'pe-goal--on': draft.trainingGoalIds.includes(g.id), 'pe-goal--pop': popId === g.id }"
          @tap="toggleGoal(g.id)"
        >
          <text>{{ g.label }}</text>
        </view>
      </view>
    </view>

    <view v-if="verifyStore.isVerified" class="pe-badge">
      <text class="pe-badge-txt">✓ 已认证</text>
    </view>
    <view v-else-if="verifyStore.isPending" class="pe-badge pe-badge--pending" @tap="goVerify">
      <text class="pe-badge-txt">审核中 · 查看进度</text>
    </view>
    <view v-else class="pe-badge pe-badge--warn" @tap="goVerify">
      <text class="pe-badge-txt">未完成实名认证 · 去认证</text>
    </view>

    <AvatarCropModal
      :visible="cropVisible"
      :src="cropSrc"
      @close="cropVisible = false"
      @confirm="onCropConfirm"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import AvatarCropModal from "@/components/profile/AvatarCropModal.vue";
import { fetchEditableProfile, updateUserInfo } from "@/api/modules/userProfile.js";
import { TRAINING_GOAL_OPTIONS } from "@/types/user/profile";
import type { UserProfileEditable } from "@/types/user/profile";
import { useVerifyStore } from "@/store/verify";
import { useUserStore } from "@/store/user";

const verifyStore = useVerifyStore();
const userStore = useUserStore();

const goalOptions = TRAINING_GOAL_OPTIONS;
const baseline = ref<UserProfileEditable | null>(null);
const draft = reactive<UserProfileEditable>({
  nickname: "",
  avatarUrl: "",
  trainingGoalIds: [],
});

const hoverAvatar = ref(false);
const cropVisible = ref(false);
const cropSrc = ref("");
const saving = ref(false);
const popId = ref("");

const dirty = computed(() => {
  if (!baseline.value) return false;
  return (
    draft.nickname !== baseline.value.nickname ||
    draft.avatarUrl !== baseline.value.avatarUrl ||
    JSON.stringify([...draft.trainingGoalIds].sort()) !==
      JSON.stringify([...baseline.value.trainingGoalIds].sort())
  );
});

onMounted(async () => {
  verifyStore.hydrateFromStorage();
  await verifyStore.loadStatus();
  const p = await fetchEditableProfile();
  baseline.value = JSON.parse(JSON.stringify(p));
  Object.assign(draft, p);
});

function toggleGoal(id: string) {
  const i = draft.trainingGoalIds.indexOf(id);
  if (i >= 0) draft.trainingGoalIds.splice(i, 1);
  else draft.trainingGoalIds.push(id);
  popId.value = id;
  setTimeout(() => {
    popId.value = "";
  }, 280);
}

function onPickAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    success: (res) => {
      const path = res.tempFilePaths?.[0];
      if (path) {
        cropSrc.value = path;
        cropVisible.value = true;
      }
    },
  });
}

function onCropConfirm(url: string) {
  draft.avatarUrl = url;
  cropVisible.value = false;
}

async function onSave() {
  if (!dirty.value || saving.value) return;
  saving.value = true;
  try {
    await updateUserInfo({
      nickname: draft.nickname.trim(),
      avatarUrl: draft.avatarUrl,
      trainingGoalIds: [...draft.trainingGoalIds],
    });
    baseline.value = JSON.parse(JSON.stringify(draft));
    userStore.setUserInfo({
      ...(userStore.userInfo as object),
      nickname: draft.nickname,
    });
    uni.showToast({ title: "已保存", icon: "success" });
    emit("saved", draft);
  } finally {
    saving.value = false;
  }
}

function goVerify() {
  uni.navigateTo({ url: "/pages/identity-verify/identity-verify" });
}

const emit = defineEmits<{
  (e: "saved", profile: UserProfileEditable): void;
}>();
</script>

<style scoped>
.pe {
  padding: 28rpx 24rpx;
  text-align: center;
}
.pe-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.pe-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.pe-save {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  opacity: 0.55;
  transition: all 0.25s ease;
}
.pe-save--on {
  opacity: 1;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 6rpx 16rpx rgba(37, 99, 235, 0.3);
}
.pe-save--on text {
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
}
.pe-save--disabled {
  pointer-events: none;
}
.pe-save text {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 600;
}

.pe-avatar-wrap {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 24rpx;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}
.pe-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: 0 0 0 4rpx #fff, 0 0 0 6rpx rgba(59, 130, 246, 0.3);
}
.pe-avatar--ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #93c5fd, #3b82f6);
  color: #fff;
  font-size: 56rpx;
  font-weight: 800;
}
.pe-avatar-mask {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
}
.pe-avatar-mask--always {
  opacity: 0.85;
}
.pe-mask-txt {
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
}

.pe-field {
  text-align: left;
  margin-bottom: 24rpx;
}
.pe-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 12rpx;
}
.pe-input {
  display: block;
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border-radius: 14rpx;
  border: 1rpx solid #e2e8f0;
  background: #ffffff;
  font-size: 28rpx;
  color: #0f172a;
  box-sizing: border-box;
}
.pe-input-placeholder {
  color: #94a3b8;
  font-size: 26rpx;
}
/* #ifdef H5 */
@media (min-width: 768px) {
  .pe-input {
    height: 44px;
    padding: 0 14px;
    font-size: 15px;
    border-radius: 10px;
  }
}
/* #endif */
.pe-goals {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
}
.pe-goal {
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  background: #f1f5f9;
  border: 1rpx solid #e2e8f0;
  font-size: 24rpx;
  color: #475569;
  transition:
    transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1),
    background 0.2s ease,
    color 0.2s ease;
}
.pe-goal--on {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.25);
}
.pe-goal--pop {
  transform: scale(1.08);
}

.pe-badge {
  margin-top: 16rpx;
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #ecfdf5;
  border: 1rpx solid #6ee7b7;
}
.pe-badge--pending {
  background: #fffbeb;
  border-color: #fcd34d;
}
.pe-badge--warn {
  background: #eff6ff;
  border-color: #93c5fd;
}
.pe-badge-txt {
  font-size: 22rpx;
  font-weight: 600;
  color: #047857;
}
.pe-badge--pending .pe-badge-txt {
  color: #b45309;
}
.pe-badge--warn .pe-badge-txt {
  color: #1d4ed8;
}
</style>
