<template>
  <view class="auth-page">
    <view class="auth-bg" />
    <view class="auth-bg auth-bg--2" />

    <view class="nav-back" @tap="goBack">
      <text class="nav-back__icon">‹</text>
      <text class="nav-back__text">返回</text>
    </view>

    <view class="auth-wrap">
      <view class="brand">
        <text class="brand__title">语境智练</text>
        <text class="brand__sub">{{ brandSub }}</text>
      </view>

      <view v-if="showSideSwitcher" class="side-switch">
        <view
          class="side-switch__item"
          :class="{ 'side-switch__item--on': appSide === 'user' }"
          @tap="setAppSide('user')"
        >
          <text class="side-switch__label">学员端</text>
          <text class="side-switch__hint">预约训练 · 成长报告</text>
        </view>
        <view
          class="side-switch__item"
          :class="{ 'side-switch__item--on': appSide === 'coach' }"
          @tap="setAppSide('coach')"
        >
          <text class="side-switch__label">陪练端</text>
          <text class="side-switch__hint">订单 · 训练 · 反馈</text>
        </view>
        <view
          class="side-switch__item side-switch__item--admin"
          :class="{ 'side-switch__item--on': appSide === 'admin' }"
          @tap="setAppSide('admin')"
        >
          <text class="side-switch__label">管理端</text>
          <text class="side-switch__hint">审核 · 配置 · 监控</text>
        </view>
      </view>
      <view v-else class="side-fixed">
        <text>{{ sideFixedLabel }}</text>
      </view>

      <view v-if="lockHint" class="lock-banner">
        <text class="lock-banner__text">{{ lockHint }}</text>
      </view>
      <view v-if="devSmsHint" class="dev-sms-hint">
        <text class="dev-sms-hint__text">{{ devSmsHint }}</text>
      </view>

      <view class="glass-card">
        <view class="tabs">
          <view
            class="tabs__item"
            :class="{ 'tabs__item--active': mode === 'login' }"
            @tap="switchMode('login')"
          >
            登录
          </view>
          <view
            class="tabs__item"
            :class="{ 'tabs__item--active': mode === 'register' }"
            @tap="switchMode('register')"
          >
            注册
          </view>
          <view
            class="tabs__indicator"
            :class="mode === 'register' ? 'tabs__indicator--right' : ''"
          />
        </view>

        <view class="slider-viewport">
          <view
            class="slider-track"
            :class="mode === 'register' ? 'slider-track--register' : ''"
          >
            <!-- 登录 -->
            <view class="panel">
              <view class="field">
                <text class="field__label">手机号</text>
                <input
                  v-model="loginForm.phone"
                  class="field__input"
                  type="number"
                  maxlength="11"
                  placeholder="请输入手机号"
                  placeholder-class="field__placeholder"
                />
              </view>
              <view class="field field--row">
                <view class="field__grow">
                  <text class="field__label">图形验证码</text>
                  <input
                    v-model="loginForm.captcha"
                    class="field__input"
                    maxlength="6"
                    placeholder="请输入图形验证码"
                    placeholder-class="field__placeholder"
                  />
                </view>
                <image
                  class="captcha-img"
                  :class="{ 'captcha-img--loading': captchaLoading }"
                  :src="captchaImage"
                  mode="aspectFit"
                  @tap="refreshCaptcha"
                />
              </view>
              <view class="field field--row">
                <view class="field__grow">
                  <text class="field__label">短信验证码</text>
                  <input
                    v-model="loginForm.smsCode"
                    class="field__input"
                    type="number"
                    maxlength="6"
                    placeholder="请输入短信验证码"
                    placeholder-class="field__placeholder"
                  />
                </view>
                <view
                  class="sms-btn"
                  :class="{ 'sms-btn--disabled': smsDisabled }"
                  @tap="onSendSms('login')"
                >
                  {{ smsLabel }}
                </view>
              </view>
              <RippleButton
                class="submit-btn"
                :loading="submitting"
                @click="onSubmitLogin"
              >
                {{ submitLoginLabel }}
              </RippleButton>
            </view>

            <!-- 注册 -->
            <view class="panel">
              <view v-if="appSide === 'coach' && coachRegisterBlocked" class="coach-register-hint">
                <text class="coach-register-hint__title">陪练账号入驻</text>
                <text class="coach-register-hint__sub">
                  微信小程序仅支持学员注册。陪练入驻与登录请使用电脑浏览器打开陪练工作台。
                </text>
                <view class="coach-register-hint__btn" @tap="setAppSide('user')">
                  <text>切换到学员端注册</text>
                </view>
              </view>
              <template v-else>
              <view v-if="appSide === 'coach'" class="coach-register-banner">
                <text class="coach-register-banner__title">陪练账号入驻</text>
                <text class="coach-register-banner__sub">提交资料后平台审核，通过即可登录陪练工作台</text>
              </view>
              <view class="field">
                <text class="field__label">手机号</text>
                <input
                  v-model="registerForm.phone"
                  class="field__input"
                  type="number"
                  maxlength="11"
                  placeholder="请输入手机号"
                  placeholder-class="field__placeholder"
                />
              </view>
              <view class="field field--row">
                <view class="field__grow">
                  <text class="field__label">图形验证码</text>
                  <input
                    v-model="registerForm.captcha"
                    class="field__input"
                    maxlength="6"
                    placeholder="请输入图形验证码"
                    placeholder-class="field__placeholder"
                  />
                </view>
                <image
                  class="captcha-img"
                  :class="{ 'captcha-img--loading': captchaLoading }"
                  :src="captchaImage"
                  mode="aspectFit"
                  @tap="refreshCaptcha"
                />
              </view>
              <view class="field field--row">
                <view class="field__grow">
                  <text class="field__label">短信验证码</text>
                  <input
                    v-model="registerForm.smsCode"
                    class="field__input"
                    type="number"
                    maxlength="6"
                    placeholder="请输入短信验证码"
                    placeholder-class="field__placeholder"
                  />
                </view>
                <view
                  class="sms-btn"
                  :class="{ 'sms-btn--disabled': smsDisabled }"
                  @tap="onSendSms('register')"
                >
                  {{ smsLabel }}
                </view>
              </view>
              <view class="agree" @tap="registerForm.agreed = !registerForm.agreed">
                <view class="agree__box" :class="{ 'agree__box--on': registerForm.agreed }" />
                <text class="agree__text">{{ registerAgreeText }}</text>
              </view>
              <RippleButton
                class="submit-btn"
                :loading="submitting"
                @click="onSubmitRegister"
              >
                {{ submitRegisterLabel }}
              </RippleButton>
              </template>
            </view>
          </view>
        </view>

        <view v-if="appSide === 'user'" class="divider">
          <view class="divider__line" />
          <text class="divider__text">其他登录方式</text>
          <view class="divider__line" />
        </view>

        <view v-if="appSide === 'user'" class="oauth-row">
          <view class="oauth-item">
            <RippleButton variant="oauth" :block="false" @click="onOAuth('wechat')">
              <template #icon>
                <image class="oauth-icon" src="/static/wechat.png" mode="aspectFit" />
              </template>
              微信登录
            </RippleButton>
          </view>
          <view class="oauth-item">
            <RippleButton variant="oauth" :block="false" @click="onOAuth('qq')">
              <template #icon>
                <image class="oauth-icon" src="/static/qq.png" mode="aspectFit" />
              </template>
              QQ 登录
            </RippleButton>
          </view>
        </view>
      </view>

      <view
        class="guest-entry"
        :class="{ 'guest-entry--hover': guestEntryHover }"
        @tap="onSkipLogin"
        @mouseenter="guestEntryHover = true"
        @mouseleave="guestEntryHover = false"
      >
        <text class="guest-entry__text">跳过登录，先逛逛</text>
        <text class="guest-entry__arrow">→</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import RippleButton from "@/components/common/RippleButton.vue";
import {
  fetchCaptcha,
  fetchLoginLockStatus,
  getOAuthAuthorizeUrl,
  loginByPhone,
  registerByPhone,
  sendSmsCode,
} from "@/api/modules/auth.js";
import { fetchUserMe } from "@/api/modules/videoConference.js";
import { clearLoginFail, getLoginLock, LOGIN_LIMIT, recordLoginFail } from "@/utils/auth/loginLimiter.js";
import { useUserStore } from "@/store/user";
import { getDefaultAppSide, showRoleSwitcher, isCoachRegisterBlocked } from "@/utils/env/appSide";
import { enterAsGuest } from "@/utils/auth/guestEntry";
import {
  navigateAfterAuth,
  roleMatchesSide,
  showRoleMismatchToast,
  mockRolesForSide,
  parseSideFromQuery,
} from "@/utils/auth/roleNav";
import type { AppSide, UserRole } from "@/types/auth";

type AuthMode = "login" | "register";

const userStore = useUserStore();
const showSideSwitcher = showRoleSwitcher();
const coachRegisterBlocked = isCoachRegisterBlocked();
const appSide = ref<AppSide>(getDefaultAppSide());
const mode = ref<AuthMode>("login");
const guestEntryHover = ref(false);
const submitting = ref(false);
const captchaKey = ref("");
const captchaImage = ref("");
const captchaLoading = ref(false);
const smsCountdown = ref(0);
const devSmsHint = ref("");
let smsTimer: ReturnType<typeof setInterval> | null = null;

const loginForm = reactive({
  phone: "",
  captcha: "",
  smsCode: "",
});

const registerForm = reactive({
  phone: "",
  captcha: "",
  smsCode: "",
  agreed: false,
});

const lockState = ref({ failCount: 0, locked: false, remainSeconds: 0 });

const activePhone = computed(() =>
  mode.value === "login" ? loginForm.phone.trim() : registerForm.phone.trim(),
);

const isLocked = computed(() => lockState.value.locked);

const lockHint = computed(() => {
  if (!lockState.value.locked) {
    if (lockState.value.failCount > 0 && mode.value === "login") {
      const left = LOGIN_LIMIT.maxAttempts - lockState.value.failCount;
      return `登录已连续失败 ${lockState.value.failCount} 次，再失败 ${left} 次将锁定 ${LOGIN_LIMIT.lockMinutes} 分钟`;
    }
    return "";
  }
  const min = Math.ceil(lockState.value.remainSeconds / 60);
  const sec = lockState.value.remainSeconds % 60;
  return `登录失败次数过多，请 ${min} 分 ${sec} 秒后再试`;
});

const smsLabel = computed(() =>
  smsCountdown.value > 0 ? `${smsCountdown.value}s` : "获取验证码",
);

const smsDisabled = computed(
  () => smsCountdown.value > 0 || !isValidPhone(activePhone.value),
);

const brandSub = computed(() => {
  if (appSide.value === "admin") return "平台运营 · 审核与配置中心";
  if (appSide.value === "coach") return "陪练工作台 · 订单与训练管理";
  return "沉浸式沟通训练平台";
});

const sideFixedLabel = computed(() => {
  if (appSide.value === "admin") return "管理端登录";
  return appSide.value === "coach" ? "陪练端登录" : "学员端登录";
});

const submitLoginLabel = computed(() => {
  if (appSide.value === "admin") return "进入管理后台";
  return appSide.value === "coach" ? "进入陪练工作台" : "登录";
});

const submitRegisterLabel = computed(() =>
  appSide.value === "coach" ? "提交入驻申请" : "注册",
);

const registerAgreeText = computed(() =>
  appSide.value === "coach"
    ? "我已阅读并同意《陪练服务协议》与《隐私政策》"
    : "我已阅读并同意《用户协议》与《隐私政策》",
);

function setAppSide(side: AppSide) {
  if (appSide.value === side) return;
  appSide.value = side;
  if (side === "coach" && mode.value === "register") {
    mode.value = "login";
  }
}

function isValidPhone(phone: string) {
  return /^1[3-9]\d{9}$/.test(phone);
}

function switchMode(next: AuthMode) {
  if (mode.value === next) return;
  mode.value = next;
  devSmsHint.value = "";
}

function apiErrorMessage(err: unknown, fallback: string) {
  if (err && typeof err === "object" && "message" in err) {
    const msg = String((err as { message?: string }).message || "").trim();
    if (msg) return msg;
  }
  return fallback;
}

function goBack() {
  uni.navigateBack({
    fail: () => uni.switchTab({ url: "/pages/profile/profile" }),
  });
}

function onSkipLogin() {
  enterAsGuest();
}

function syncLockFromLocal(phone: string) {
  lockState.value = getLoginLock(phone);
}

async function syncLockFromServer(phone: string) {
  if (!phone) return;
  try {
    const res = (await fetchLoginLockStatus(phone)) as {
      locked?: boolean;
      remainSeconds?: number;
      failCount?: number;
    };
    if (res && typeof res.locked === "boolean") {
      lockState.value = {
        locked: Boolean(res.locked),
        remainSeconds: res.remainSeconds || 0,
        failCount: res.failCount || 0,
      };
    }
  } catch {
    syncLockFromLocal(phone);
  }
}

watch(
  () => loginForm.phone,
  (phone) => {
    if (mode.value === "login") {
      syncLockFromLocal(phone.trim());
    }
  },
);

watch(activePhone, (phone) => {
  if (phone && mode.value === "login") {
    syncLockFromServer(phone);
  }
});

function applyCaptcha(payload: { captchaKey?: string; captchaImage?: string }) {
  captchaKey.value = payload.captchaKey || "";
  captchaImage.value = payload.captchaImage || "";
  loginForm.captcha = "";
  registerForm.captcha = "";
}

async function refreshCaptcha() {
  if (captchaLoading.value) return;
  captchaLoading.value = true;
  try {
    const res = (await fetchCaptcha()) as {
      captchaKey?: string;
      captchaImage?: string;
    };
    if (!res?.captchaKey || !res?.captchaImage) {
      throw new Error("invalid captcha payload");
    }
    applyCaptcha(res);
  } catch {
    captchaKey.value = "";
    captchaImage.value = "";
    uni.showToast({
      title: "验证码加载失败，请确认后端已启动",
      icon: "none",
      duration: 2500,
    });
  } finally {
    captchaLoading.value = false;
  }
}

function startSmsCountdown(seconds = 60) {
  smsCountdown.value = seconds;
  if (smsTimer) clearInterval(smsTimer);
  smsTimer = setInterval(() => {
    smsCountdown.value -= 1;
    if (smsCountdown.value <= 0 && smsTimer) {
      clearInterval(smsTimer);
      smsTimer = null;
    }
  }, 1000);
}

async function onSendSms(target: AuthMode) {
  const phone = target === "login" ? loginForm.phone.trim() : registerForm.phone.trim();
  if (!isValidPhone(phone)) {
    uni.showToast({ title: "请输入正确手机号", icon: "none" });
    return;
  }
  const captcha =
    target === "login" ? loginForm.captcha.trim() : registerForm.captcha.trim();
  if (!captcha) {
    uni.showToast({ title: "请先填写图形验证码", icon: "none" });
    return;
  }
  if (!captchaKey.value) {
    uni.showToast({ title: "请先刷新图形验证码", icon: "none" });
    await refreshCaptcha();
    return;
  }

  try {
    const res = (await sendSmsCode(
      {
        phone,
        captcha,
        captchaKey: captchaKey.value,
        scene: target === "login" ? "login" : "register",
      },
      { silent: true },
    )) as { expireIn?: number; devCode?: string };
    startSmsCountdown(res?.expireIn && res.expireIn < 120 ? res.expireIn : 60);
    if (res?.devCode) {
      devSmsHint.value = `开发环境验证码：${res.devCode}（正式环境将发送至手机）`;
      uni.showToast({
        title: `验证码：${res.devCode}`,
        icon: "none",
        duration: 5000,
      });
    } else {
      devSmsHint.value = "";
      uni.showToast({ title: "验证码已发送至手机", icon: "success" });
    }
  } catch (err) {
    devSmsHint.value = "";
    uni.showToast({
      title: apiErrorMessage(err, "发送失败，请检查图形验证码或稍后重试"),
      icon: "none",
      duration: 2500,
    });
    refreshCaptcha();
  }
}

function validatePhoneForm(
  form: { phone: string; captcha: string; smsCode: string },
  needAgreement = false,
  agreed = false,
) {
  if (!isValidPhone(form.phone.trim())) {
    uni.showToast({ title: "请输入正确手机号", icon: "none" });
    return false;
  }
  if (!form.captcha.trim()) {
    uni.showToast({ title: "请输入图形验证码", icon: "none" });
    return false;
  }
  if (!captchaKey.value) {
    uni.showToast({ title: "请先刷新图形验证码", icon: "none" });
    return false;
  }
  if (!/^\d{4,6}$/.test(form.smsCode.trim())) {
    uni.showToast({ title: "请输入短信验证码", icon: "none" });
    return false;
  }
  if (needAgreement && !agreed) {
    uni.showToast({ title: "请先同意用户协议", icon: "none" });
    return false;
  }
  return true;
}

async function resolveRolesAfterLogin(): Promise<UserRole[]> {
  try {
    const me = (await fetchUserMe()) as { roles?: UserRole[] };
    if (me?.roles?.length) return me.roles;
  } catch {
    /* mock */
  }
  return mockRolesForSide(appSide.value);
}

async function completeAuthSession(
  token: string,
  fallbackUser?: Record<string, unknown>,
) {
  userStore.setToken(token);
  let roles: UserRole[] = [];
  let user: Record<string, unknown> | undefined = fallbackUser;
  try {
    const me = (await fetchUserMe()) as Record<string, unknown> & { roles?: UserRole[] };
    if (me?.roles?.length) roles = me.roles;
    if (me?.userId != null) user = me;
  } catch {
    roles = mockRolesForSide(appSide.value);
  }
  if (!roles.length) roles = mockRolesForSide(appSide.value);
  if (!roleMatchesSide(appSide.value, roles)) {
    userStore.logout();
    showRoleMismatchToast(appSide.value);
    return;
  }
  userStore.setAuthSession({
    token,
    roles,
    appSide: appSide.value,
    user,
  });
  navigateAfterAuth(appSide.value, roles);
}

async function onSubmitLogin() {
  if (isLocked.value) {
    uni.showToast({ title: lockHint.value || "账号已锁定", icon: "none" });
    return;
  }
  if (!validatePhoneForm(loginForm)) return;

  submitting.value = true;
  const phone = loginForm.phone.trim();
  try {
    const res = (await loginByPhone(
      {
        phone,
        smsCode: loginForm.smsCode.trim(),
      },
      { silent: true },
    )) as { token?: string; user?: Record<string, unknown> };

    if (!res?.token) {
      throw new Error("no token");
    }
    clearLoginFail(phone);
    lockState.value = { failCount: 0, locked: false, remainSeconds: 0 };
    uni.showToast({ title: "登录成功", icon: "success" });
    await completeAuthSession(res.token, res.user);
  } catch (err) {
    lockState.value = recordLoginFail(phone);
    syncLockFromServer(phone);
    refreshCaptcha();
    if (!isLocked.value) {
      uni.showToast({
        title: apiErrorMessage(err, "登录失败"),
        icon: "none",
        duration: 2500,
      });
    }
  } finally {
    submitting.value = false;
  }
}

async function onSubmitRegister() {
  if (appSide.value === "coach" && coachRegisterBlocked) {
    uni.showToast({ title: "请使用电脑端或切换学员端注册", icon: "none" });
    return;
  }
  if (!validatePhoneForm(registerForm, true, registerForm.agreed)) return;

  submitting.value = true;
  const phone = registerForm.phone.trim();
  try {
    const res = (await registerByPhone(
      {
        phone,
        smsCode: registerForm.smsCode.trim(),
        captcha: registerForm.captcha.trim(),
        captchaKey: captchaKey.value,
        appSide: appSide.value,
      },
      { silent: true },
    )) as { token?: string; user?: { nickname?: string } };

    if (!res?.token) {
      throw new Error("no token");
    }
    clearLoginFail(phone);
    uni.showToast({ title: "注册成功", icon: "success" });
    await completeAuthSession(res.token, res.user as Record<string, unknown>);
  } catch (err) {
    uni.showToast({
      title: apiErrorMessage(err, "注册失败"),
      icon: "none",
      duration: 2500,
    });
    refreshCaptcha();
  } finally {
    submitting.value = false;
  }
}

async function onOAuth(provider: "wechat" | "qq") {
  try {
    const res = (await getOAuthAuthorizeUrl(provider)) as { authUrl?: string };
    if (res?.authUrl) {
      // #ifdef H5
      globalThis.location.href = res.authUrl;
      // #endif
      // #ifndef H5
      uni.showToast({ title: "请在 H5 或配置小程序 SDK", icon: "none" });
      // #endif
      return;
    }
    throw new Error("no auth url");
  } catch {
    const label = provider === "wechat" ? "微信" : "QQ";
    uni.showToast({
      title: `${label}登录需后端 OAuth 接口`,
      icon: "none",
      duration: 2500,
    });
  }
}

let lockTimer: ReturnType<typeof setInterval> | null = null;

function startLockTicker() {
  if (lockTimer) clearInterval(lockTimer);
  lockTimer = setInterval(() => {
    const phone = loginForm.phone.trim();
    if (!phone) return;
    const next = getLoginLock(phone);
    lockState.value = next;
    if (!next.locked && lockTimer) {
      clearInterval(lockTimer);
      lockTimer = null;
    }
  }, 1000);
}

watch(
  () => lockState.value.locked,
  (locked) => {
    if (locked) startLockTicker();
  },
);

onMounted(() => {
  const querySide = parseSideFromQuery();
  if (querySide) appSide.value = querySide;
  // #ifdef H5
  if (window.location.href.includes("logout=1")) {
    refreshCaptcha();
    return;
  }
  // #endif
  if (userStore.isLoggedIn) {
    const side = (userStore.preferredAppSide || appSide.value) as AppSide;
    navigateAfterAuth(side, (userStore.roles || []) as UserRole[]);
    return;
  }
  refreshCaptcha();
  if (loginForm.phone.trim()) {
    syncLockFromLocal(loginForm.phone.trim());
  }
});

onUnmounted(() => {
  if (smsTimer) clearInterval(smsTimer);
  if (lockTimer) clearInterval(lockTimer);
});
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(165deg, #e0f2fe 0%, #f0f9ff 42%, #ffffff 100%);
  box-sizing: border-box;
}
.auth-bg {
  position: absolute;
  width: 140%;
  height: 55%;
  left: -20%;
  top: -8%;
  background: radial-gradient(ellipse at 30% 20%, #93c5fd 0%, transparent 62%);
  opacity: 0.55;
  pointer-events: none;
}
.auth-bg--2 {
  top: auto;
  bottom: -12%;
  left: -10%;
  background: radial-gradient(ellipse at 70% 80%, #bfdbfe 0%, transparent 58%);
}
.nav-back {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  margin: 0 0 8rpx 8rpx;
  padding: 12rpx 16rpx;
}
.nav-back__icon {
  font-size: 40rpx;
  color: #3b82f6;
  line-height: 1;
}
.nav-back__text {
  font-size: 28rpx;
  color: #475569;
}
.auth-wrap {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  padding: calc(var(--status-bar-height, 0px) + 48rpx) 32rpx 48rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand {
  margin-bottom: 28rpx;
  text-align: center;
}
.brand__title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #1e40af;
  letter-spacing: 4rpx;
}
.brand__sub {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #64748b;
}

.side-switch {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.side-switch__item {
  flex: 1;
  padding: 20rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.55);
  border: 2rpx solid rgba(148, 163, 184, 0.35);
  transition: all 0.25s ease;
}
.side-switch__item--on {
  background: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.15);
}
.side-switch__item--on:last-child {
  border-color: #475569;
  box-shadow: 0 8rpx 24rpx rgba(51, 65, 85, 0.12);
}
.side-switch__label {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #334155;
}
.side-switch__item--on .side-switch__label {
  color: #1d4ed8;
}
.side-switch__item--on:last-child .side-switch__label {
  color: #334155;
}
.side-switch__item--admin.side-switch__item--on {
  border-color: #2563eb;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.15);
}
.side-switch__item--admin.side-switch__item--on .side-switch__label {
  color: #1d4ed8;
}
.side-switch__hint {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #94a3b8;
}
.side-fixed {
  margin-bottom: 20rpx;
  text-align: center;
}
.side-fixed text {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 600;
}

.coach-register-hint {
  padding: 32rpx 8rpx;
  text-align: center;
}
.coach-register-hint__title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #334155;
}
.coach-register-hint__sub {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.55;
}
.coach-register-hint__btn {
  display: inline-block;
  margin-top: 28rpx;
  padding: 18rpx 36rpx;
  border-radius: 999rpx;
  background: #eff6ff;
  border: 1rpx solid #93c5fd;
}
.coach-register-hint__btn text {
  font-size: 26rpx;
  font-weight: 700;
  color: #2563eb;
}

.coach-register-banner {
  margin-bottom: 20rpx;
  padding: 20rpx 16rpx;
  border-radius: 16rpx;
  background: #f1f5f9;
  border: 1rpx solid #e2e8f0;
}
.coach-register-banner__title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #334155;
}
.coach-register-banner__sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
  line-height: 1.45;
}

.lock-banner {
  margin-bottom: 20rpx;
  padding: 18rpx 22rpx;
  border-radius: 14rpx;
  background: rgba(254, 226, 226, 0.85);
  border: 1rpx solid rgba(252, 165, 165, 0.6);
  backdrop-filter: blur(12px);
}
.lock-banner__text {
  font-size: 24rpx;
  color: #b91c1c;
  line-height: 1.5;
}
.dev-sms-hint {
  margin-bottom: 16rpx;
  padding: 16rpx 20rpx;
  border-radius: 14rpx;
  background: rgba(219, 234, 254, 0.9);
  border: 1rpx solid rgba(147, 197, 253, 0.7);
}
.dev-sms-hint__text {
  font-size: 24rpx;
  color: #1d4ed8;
  line-height: 1.5;
}
.glass-card {
  padding: 28rpx 28rpx 32rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.72);
  border: 1rpx solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 16rpx 48rpx rgba(59, 130, 246, 0.12), 0 4rpx 16rpx rgba(148, 163, 184, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.tabs {
  position: relative;
  display: flex;
  margin-bottom: 28rpx;
  padding: 6rpx;
  border-radius: 16rpx;
  background: rgba(224, 242, 254, 0.85);
}
.tabs__item {
  flex: 1;
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 18rpx 0;
  font-size: 30rpx;
  color: #64748b;
  transition: color 0.3s ease;
}
.tabs__item--active {
  color: #1d4ed8;
  font-weight: 600;
}
.tabs__indicator {
  position: absolute;
  z-index: 1;
  top: 6rpx;
  left: 6rpx;
  width: calc(50% - 6rpx);
  height: calc(100% - 12rpx);
  border-radius: 12rpx;
  background: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.15);
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(0);
}
.tabs__indicator--right {
  transform: translateX(100%);
}
.slider-viewport {
  overflow: hidden;
  width: 100%;
}
.slider-track {
  display: flex;
  width: 200%;
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(0);
}
.slider-track--register {
  transform: translateX(-50%);
}
.panel {
  width: 50%;
  flex-shrink: 0;
  padding: 0 4rpx;
  box-sizing: border-box;
}
.field {
  margin-bottom: 22rpx;
}
.field--row {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
}
.field__grow {
  flex: 1;
  min-width: 0;
}
.field__label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 24rpx;
  color: #475569;
}
.field__input {
  height: 80rpx;
  padding: 0 22rpx;
  border-radius: 14rpx;
  background: #ffffff;
  border: 1rpx solid #bae6fd;
  font-size: 28rpx;
  color: #1e293b;
  box-sizing: border-box;
}
.field__placeholder {
  color: #94a3b8;
  font-size: 26rpx;
}
.captcha-img {
  width: 200rpx;
  height: 80rpx;
  border-radius: 14rpx;
  flex-shrink: 0;
  background: #e0f2fe;
  border: 1rpx solid #bae6fd;
}
.captcha-img--loading {
  opacity: 0.45;
}
.sms-btn {
  flex-shrink: 0;
  min-width: 180rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 14rpx;
  font-size: 24rpx;
  color: #2563eb;
  background: #e0f2fe;
  border: 1rpx solid #93c5fd;
}
.sms-btn--disabled {
  opacity: 0.5;
  pointer-events: none;
}
.submit-btn {
  margin-top: 12rpx;
}
.agree {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.agree__box {
  width: 32rpx;
  height: 32rpx;
  margin-top: 4rpx;
  border-radius: 8rpx;
  border: 2rpx solid #93c5fd;
  flex-shrink: 0;
  box-sizing: border-box;
}
.agree__box--on {
  background: #3b82f6;
  border-color: #3b82f6;
  box-shadow: inset 0 0 0 6rpx #ffffff;
}
.agree__text {
  flex: 1;
  font-size: 22rpx;
  color: #64748b;
  line-height: 1.5;
}
.divider {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 32rpx 0 24rpx;
}
.divider__line {
  flex: 1;
  height: 1rpx;
  background: #e2e8f0;
}
.divider__text {
  font-size: 22rpx;
  color: #94a3b8;
}
.oauth-row {
  display: flex;
  gap: 20rpx;
}
.oauth-item {
  flex: 1;
  min-width: 0;
}
.oauth-icon {
  width: 40rpx;
  height: 40rpx;
  flex-shrink: 0;
}

.guest-entry {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 28rpx;
  padding: 18rpx 24rpx;
  border-radius: 999rpx;
  transition:
    background 0.22s ease,
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}
.guest-entry__text {
  font-size: 26rpx;
  font-weight: 500;
  color: #64748b;
  transition: color 0.22s ease;
}
.guest-entry__arrow {
  font-size: 28rpx;
  color: #94a3b8;
  line-height: 1;
  transition: transform 0.22s ease, color 0.22s ease;
}

/* #ifdef H5 */
.guest-entry {
  cursor: pointer;
}
.guest-entry--hover {
  background: rgba(37, 99, 235, 0.06);
  transform: translateY(-2rpx);
}
.guest-entry--hover .guest-entry__text {
  color: #2563eb;
}
.guest-entry--hover .guest-entry__arrow {
  color: #2563eb;
  transform: translateX(4rpx);
}
/* #endif */

/* H5 响应式：大屏居中卡片 */
/* #ifdef H5 */
@media (min-width: 768px) {
  .auth-wrap {
    max-width: 440px;
    margin: 0 auto;
    padding-top: 48px;
    padding-bottom: 48px;
  }
}
/* #endif */
</style>
