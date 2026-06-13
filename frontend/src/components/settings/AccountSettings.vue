<template>
  <view class="as">
    <view class="as-head">
      <text class="as-title">账户设置</text>
      <text class="as-sub">安全后盾 · 隐私保护 · 偏好管理</text>
    </view>

    <view class="as-body">
      <!-- 桌面端左侧导航 -->
      <view class="as-nav as-nav--side">
        <view
          v-for="item in navItems"
          :key="item.id"
          class="as-nav-item"
          :class="{ 'as-nav-item--on': tab === item.id }"
          @tap="tab = item.id"
        >
          <text class="as-nav-ico">{{ item.icon }}</text>
          <text class="as-nav-label">{{ item.label }}</text>
        </view>
      </view>

      <!-- 移动端底部导航 -->
      <view class="as-nav as-nav--bottom">
        <view
          v-for="item in navItems"
          :key="item.id"
          class="as-nav-item as-nav-item--compact"
          :class="{ 'as-nav-item--on': tab === item.id }"
          @tap="tab = item.id"
        >
          <text class="as-nav-ico">{{ item.icon }}</text>
          <text class="as-nav-label">{{ item.shortLabel }}</text>
        </view>
      </view>

      <view class="as-panel">
        <!-- 安全 -->
        <view v-if="tab === 'security'" class="as-section">
          <text class="as-section-title">安全中心</text>
          <text class="as-section-desc">保护您的账号与登录凭证</text>

          <view class="as-row" @tap="phoneModal = true">
            <view class="as-row-text">
              <text class="as-row-title">绑定手机号</text>
              <text class="as-row-desc">{{ security.phoneMasked || '—' }}</text>
            </view>
            <text class="as-row-action">修改</text>
          </view>

          <view class="as-row" @tap="pwdModal = true">
            <view class="as-row-text">
              <text class="as-row-title">登录密码</text>
              <text class="as-row-desc">定期更换密码，提升账号安全</text>
            </view>
            <text class="as-row-action">修改</text>
          </view>
        </view>

        <!-- 隐私 -->
        <view v-else-if="tab === 'privacy'" class="as-section">
          <text class="as-section-title">隐私保护</text>
          <text class="as-section-desc">控制您的数据如何被使用与展示</text>

          <view class="as-toggle-row">
            <view class="as-toggle-text">
              <text class="as-row-title">允许专家查看历史报告</text>
              <text class="as-row-desc">开启后，专家将能查阅您的历史报告以提供精准建议</text>
            </view>
            <view class="as-toggle-right">
              <view
                class="as-switch"
                :class="{ 'as-switch--on': privacy.shareReportWithExpert }"
                @tap="togglePrivacy('shareReportWithExpert')"
              >
                <view class="as-switch-knob" />
              </view>
              <text v-if="savedKey === 'shareReportWithExpert'" class="as-saved">✓</text>
            </view>
          </view>

          <view class="as-toggle-row">
            <view class="as-toggle-text">
              <text class="as-row-title">社区动态匿名化</text>
              <text class="as-row-desc">在实战圈发布内容时隐藏真实昵称与头像</text>
            </view>
            <view class="as-toggle-right">
              <view
                class="as-switch"
                :class="{ 'as-switch--on': privacy.anonymousCommunity }"
                @tap="togglePrivacy('anonymousCommunity')"
              >
                <view class="as-switch-knob" />
              </view>
              <text v-if="savedKey === 'anonymousCommunity'" class="as-saved">✓</text>
            </view>
          </view>
        </view>

        <!-- 通知 -->
        <view v-else-if="tab === 'notifications'" class="as-section">
          <text class="as-section-title">通知偏好</text>
          <text class="as-section-desc">选择您希望接收的消息类型</text>

          <view class="as-toggle-row">
            <view class="as-toggle-text">
              <text class="as-row-title">预约成功短信</text>
              <text class="as-row-desc">专家预约确认后发送短信提醒</text>
            </view>
            <view class="as-toggle-right">
              <view
                class="as-switch"
                :class="{ 'as-switch--on': notify.smsOnBook }"
                @tap="toggleNotify('smsOnBook')"
              >
                <view class="as-switch-knob" />
              </view>
              <text v-if="savedKey === 'smsOnBook'" class="as-saved">✓</text>
            </view>
          </view>

          <view class="as-toggle-row">
            <view class="as-toggle-text">
              <text class="as-row-title">训练开始提醒</text>
              <text class="as-row-desc">对练开始前 15 分钟推送提醒</text>
            </view>
            <view class="as-toggle-right">
              <view
                class="as-switch"
                :class="{ 'as-switch--on': notify.remindBeforeTraining }"
                @tap="toggleNotify('remindBeforeTraining')"
              >
                <view class="as-switch-knob" />
              </view>
              <text v-if="savedKey === 'remindBeforeTraining'" class="as-saved">✓</text>
            </view>
          </view>

          <view class="as-toggle-row">
            <view class="as-toggle-text">
              <text class="as-row-title">专家反馈通知</text>
              <text class="as-row-desc">训练结束后收到专家点评与报告更新</text>
            </view>
            <view class="as-toggle-right">
              <view
                class="as-switch"
                :class="{ 'as-switch--on': notify.expertFeedbackNotify }"
                @tap="toggleNotify('expertFeedbackNotify')"
              >
                <view class="as-switch-knob" />
              </view>
              <text v-if="savedKey === 'expertFeedbackNotify'" class="as-saved">✓</text>
            </view>
          </view>
        </view>

        <!-- 通用 -->
        <view v-else class="as-section as-section--flush">
          <ProfileEditor @saved="emit('profile-saved', $event)" />
        </view>
      </view>
    </view>

    <!-- 修改手机号 -->
    <view v-if="phoneModal" class="as-mask" @tap="phoneModal = false">
      <view class="as-modal" @tap.stop>
        <text class="as-modal-title">修改手机号</text>
        <view class="as-field">
          <text class="as-field-label">新手机号</text>
          <input
            v-model="phoneForm.phone"
            class="as-input"
            type="number"
            maxlength="11"
            placeholder="请输入 11 位手机号"
          />
        </view>
        <view class="as-field as-field--code">
          <text class="as-field-label">验证码</text>
          <input v-model="phoneForm.code" class="as-input as-input--code" type="number" maxlength="6" placeholder="6 位验证码" />
          <view class="as-code-btn" @tap="sendCode"><text>获取验证码</text></view>
        </view>
        <view class="as-modal-actions">
          <view class="as-btn as-btn--ghost" @tap="phoneModal = false"><text>取消</text></view>
          <view class="as-btn" @tap="submitPhone"><text>确认修改</text></view>
        </view>
      </view>
    </view>

    <!-- 修改密码 -->
    <view v-if="pwdModal" class="as-mask" @tap="pwdModal = false">
      <view class="as-modal" @tap.stop>
        <text class="as-modal-title">修改密码</text>
        <view class="as-field">
          <text class="as-field-label">当前密码</text>
          <input v-model="pwdForm.oldPassword" class="as-input" password placeholder="请输入当前密码" />
        </view>
        <view class="as-field">
          <text class="as-field-label">新密码</text>
          <input v-model="pwdForm.newPassword" class="as-input" password placeholder="至少 8 位，含大小写与数字" />
          <view class="as-strength">
            <view class="as-strength-bar">
              <view
                class="as-strength-fill"
                :style="{ width: `${(pwdStrength.score / 4) * 100}%` }"
                :class="`as-strength-fill--${pwdStrength.score}`"
              />
            </view>
            <text class="as-strength-label">{{ pwdStrength.label }}</text>
          </view>
        </view>
        <view class="as-field">
          <text class="as-field-label">确认新密码</text>
          <input v-model="pwdForm.confirm" class="as-input" password placeholder="再次输入新密码" />
        </view>
        <view class="as-modal-actions">
          <view class="as-btn as-btn--ghost" @tap="pwdModal = false"><text>取消</text></view>
          <view class="as-btn" @tap="submitPassword"><text>确认修改</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import ProfileEditor from "@/components/profile/ProfileEditor.vue";
import {
  fetchSecurityInfo,
  updatePassword,
  updatePhone,
  fetchPrivacySettings,
  patchPrivacySettings,
  fetchNotificationSettings,
  patchNotificationSettings,
  passwordStrength,
} from "@/api/modules/userSettings.js";
import { showTopToast } from "@/utils/common/topToast";
import type {
  AccountSettingsTab,
  PrivacySettings,
  NotificationSettings,
} from "@/types/user/settings";
import type { UserProfileEditable } from "@/types/user/profile";

const emit = defineEmits<{
  "profile-saved": [UserProfileEditable];
}>();

const navItems: { id: AccountSettingsTab; label: string; shortLabel: string; icon: string }[] = [
  { id: "security", label: "安全", shortLabel: "安全", icon: "🔒" },
  { id: "privacy", label: "隐私", shortLabel: "隐私", icon: "🛡" },
  { id: "notifications", label: "通知", shortLabel: "通知", icon: "🔔" },
  { id: "general", label: "通用", shortLabel: "通用", icon: "👤" },
];

const tab = ref<AccountSettingsTab>("security");
const security = ref({ phoneMasked: "" });
const privacy = ref<PrivacySettings>({ shareReportWithExpert: true, anonymousCommunity: false });
const notify = ref<NotificationSettings>({
  smsOnBook: true,
  remindBeforeTraining: true,
  expertFeedbackNotify: true,
});
const savedKey = ref("");
let savedTimer: ReturnType<typeof setTimeout> | null = null;

const phoneModal = ref(false);
const pwdModal = ref(false);
const phoneForm = ref({ phone: "", code: "" });
const pwdForm = ref({ oldPassword: "", newPassword: "", confirm: "" });

const pwdStrength = computed(() => passwordStrength(pwdForm.value.newPassword));

onMounted(async () => {
  const [sec, priv, ntf] = await Promise.all([
    fetchSecurityInfo(),
    fetchPrivacySettings(),
    fetchNotificationSettings(),
  ]);
  security.value = sec;
  privacy.value = priv;
  notify.value = ntf;
});

function flashSaved(key: string) {
  savedKey.value = key;
  if (savedTimer) clearTimeout(savedTimer);
  savedTimer = setTimeout(() => {
    savedKey.value = "";
  }, 1800);
}

async function togglePrivacy(key: keyof PrivacySettings) {
  privacy.value = { ...privacy.value, [key]: !privacy.value[key] };
  try {
    await patchPrivacySettings(privacy.value);
    flashSaved(key);
  } catch {
    privacy.value = { ...privacy.value, [key]: !privacy.value[key] };
    showTopToast("保存失败，请稍后重试", "error");
  }
}

async function toggleNotify(key: keyof NotificationSettings) {
  notify.value = { ...notify.value, [key]: !notify.value[key] };
  try {
    await patchNotificationSettings(notify.value);
    flashSaved(key);
  } catch {
    notify.value = { ...notify.value, [key]: !notify.value[key] };
    showTopToast("保存失败，请稍后重试", "error");
  }
}

function sendCode() {
  if (!/^1\d{10}$/.test(phoneForm.value.phone)) {
    showTopToast("请输入正确的手机号", "error");
    return;
  }
  showTopToast("验证码已发送", "success");
}

async function submitPhone() {
  if (!/^1\d{10}$/.test(phoneForm.value.phone) || phoneForm.value.code.length < 4) {
    showTopToast("请填写完整信息", "error");
    return;
  }
  try {
    const res = (await updatePhone({ phone: phoneForm.value.phone, code: phoneForm.value.code })) as {
      phoneMasked?: string;
    };
    security.value.phoneMasked = res.phoneMasked ?? security.value.phoneMasked;
    phoneModal.value = false;
    phoneForm.value = { phone: "", code: "" };
    showTopToast("手机号已更新", "success");
  } catch {
    showTopToast("修改失败，请稍后重试", "error");
  }
}

async function submitPassword() {
  const { oldPassword, newPassword, confirm } = pwdForm.value;
  if (!oldPassword || newPassword.length < 8) {
    showTopToast("请填写符合要求的密码", "error");
    return;
  }
  if (newPassword !== confirm) {
    showTopToast("两次输入的密码不一致", "error");
    return;
  }
  try {
    await updatePassword({ oldPassword, newPassword });
    pwdModal.value = false;
    pwdForm.value = { oldPassword: "", newPassword: "", confirm: "" };
    showTopToast("密码已更新", "success");
  } catch {
    showTopToast("修改失败，请检查当前密码", "error");
  }
}
</script>

<style scoped>
.as {
  padding: 8rpx 0 24rpx;
}
.as-head {
  margin-bottom: 24rpx;
}
.as-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}
.as-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
}

.as-body {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
@media (min-width: 768px) {
  .as-body {
    flex-direction: row;
    align-items: flex-start;
    gap: 24rpx;
  }
}

.as-nav--side {
  display: none;
}
@media (min-width: 768px) {
  .as-nav--side {
    display: flex;
    flex-direction: column;
    width: 200rpx;
    flex-shrink: 0;
    gap: 8rpx;
  }
  .as-nav--bottom {
    display: none;
  }
}

.as-nav--bottom {
  display: flex;
  flex-direction: row;
  position: sticky;
  bottom: 0;
  z-index: 10;
  padding: 8rpx;
  border-radius: 16rpx;
  background: #f8fafc;
  border: 1rpx solid #f1f5f9;
}
.as-nav-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: background 0.2s ease;
}
.as-nav-item--compact {
  flex: 1;
  flex-direction: column;
  gap: 4rpx;
  padding: 12rpx 8rpx;
}
.as-nav-item--on {
  background: #eff6ff;
}
.as-nav-ico {
  font-size: 28rpx;
}
.as-nav-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #64748b;
}
.as-nav-item--on .as-nav-label {
  color: #2563eb;
}
.as-nav-item--compact .as-nav-label {
  font-size: 22rpx;
}

.as-panel {
  flex: 1;
  min-width: 0;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 1rpx solid #f1f5f9;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.04);
}
.as-section--flush {
  padding: 0;
  border: none;
  box-shadow: none;
  background: transparent;
}

.as-section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.as-section-desc {
  display: block;
  margin-top: 8rpx;
  margin-bottom: 28rpx;
  font-size: 24rpx;
  color: #64748b;
}

.as-row,
.as-toggle-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
  gap: 20rpx;
}
.as-row:last-child,
.as-toggle-row:last-child {
  border-bottom: none;
}
.as-row-text,
.as-toggle-text {
  flex: 1;
  min-width: 0;
}
.as-row-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
.as-row-desc {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.5;
}
.as-row-action {
  font-size: 26rpx;
  font-weight: 700;
  color: #2563eb;
  flex-shrink: 0;
}

.as-toggle-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.as-switch {
  width: 88rpx;
  height: 52rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  position: relative;
  transition: background 0.28s cubic-bezier(0.22, 1, 0.36, 1);
  cursor: pointer;
}
.as-switch--on {
  background: #2563eb;
}
.as-switch-knob {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2rpx 6rpx rgba(15, 23, 42, 0.15);
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}
.as-switch--on .as-switch-knob {
  transform: translateX(36rpx);
}

.as-saved {
  font-size: 28rpx;
  color: #10b981;
  font-weight: 800;
  animation: asSaved 0.35s ease;
}
@keyframes asSaved {
  0% { opacity: 0; transform: scale(0.5); }
  50% { opacity: 1; transform: scale(1.2); }
  100% { opacity: 1; transform: scale(1); }
}

.as-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}
.as-modal {
  width: 100%;
  max-width: 560rpx;
  padding: 36rpx 32rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 24rpx 48rpx rgba(15, 23, 42, 0.18);
}
.as-modal-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 28rpx;
}
.as-field {
  margin-bottom: 24rpx;
}
.as-field--code {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: 12rpx;
  flex-wrap: wrap;
}
.as-field-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 10rpx;
}
.as-input {
  width: 100%;
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  border: 1rpx solid #e2e8f0;
  font-size: 28rpx;
  color: #0f172a;
  box-sizing: border-box;
}
.as-input--code {
  flex: 1;
  min-width: 200rpx;
}
.as-code-btn {
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  background: #eff6ff;
  flex-shrink: 0;
}
.as-code-btn text {
  font-size: 24rpx;
  font-weight: 700;
  color: #2563eb;
  white-space: nowrap;
}

.as-strength {
  margin-top: 12rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}
.as-strength-bar {
  flex: 1;
  height: 8rpx;
  border-radius: 999rpx;
  background: #f1f5f9;
  overflow: hidden;
}
.as-strength-fill {
  height: 100%;
  border-radius: 999rpx;
  transition: width 0.3s ease, background 0.3s ease;
}
.as-strength-fill--0 { background: #e2e8f0; }
.as-strength-fill--1 { background: #ef4444; }
.as-strength-fill--2 { background: #f59e0b; }
.as-strength-fill--3 { background: #3b82f6; }
.as-strength-fill--4 { background: #10b981; }
.as-strength-label {
  font-size: 22rpx;
  color: #64748b;
  font-weight: 600;
  min-width: 60rpx;
}

.as-modal-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 12rpx;
}
.as-btn {
  flex: 1;
  padding: 22rpx;
  border-radius: 14rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  text-align: center;
}
.as-btn text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}
.as-btn--ghost {
  background: #f1f5f9;
}
.as-btn--ghost text {
  color: #64748b;
}
</style>
