<template>
  <view class="ud">
    <view class="ud-shell">
      <!-- 左侧边栏 -->
      <view class="ud-side">
        <view class="ud-card ud-profile">
          <view class="ud-avatar-wrap">
            <image
              v-if="profile?.avatarUrl"
              class="ud-avatar"
              :src="profile.avatarUrl"
              mode="aspectFill"
            />
            <view v-else class="ud-avatar ud-avatar--ph">
              <text class="ud-avatar-txt">{{ avatarLetter }}</text>
            </view>
            <view class="ud-crown">
              <text class="ud-crown-ico">👑</text>
            </view>
          </view>
          <text class="ud-name">{{ profile?.name ?? "—" }}</text>
          <text class="ud-join">加入语境智练 {{ profile?.joinDays ?? 0 }} 天</text>
          <view class="ud-tags">
            <text class="ud-tag ud-tag--blue">{{ profile?.rankTag ?? "职场精英" }}</text>
            <text class="ud-tag ud-tag--gray">{{ profile?.level ?? "Lv.0" }}</text>
            <text
              v-if="verifyStore.isVerified"
              class="ud-tag ud-tag--ok"
            >已认证</text>
            <text
              v-else-if="verifyStore.isPending"
              class="ud-tag ud-tag--pending"
            >审核中</text>
          </view>
          <view class="ud-stats">
            <view class="ud-stat">
              <text class="ud-stat-num">{{ profile?.finishedCourses ?? 0 }}</text>
              <text class="ud-stat-lbl">已结课</text>
            </view>
            <view class="ud-stat">
              <text class="ud-stat-num">{{ profile?.totalHoursLabel ?? "0h" }}</text>
              <text class="ud-stat-lbl">累计时长</text>
            </view>
          </view>
        </view>

        <view class="ud-card ud-nav">
          <view
            v-for="m in menus"
            :key="m.id"
            class="ud-nav-item"
            :class="{
              'ud-nav-item--on': activeMenu === m.id,
              'ud-nav-item--danger': m.danger,
              'ud-nav-item--hover': hoverMenuId === m.id,
            }"
            @tap="onMenu(m.id)"
            @mouseenter="hoverMenuId = m.id"
            @mouseleave="hoverMenuId = ''"
          >
            <text class="ud-nav-ico">{{ m.icon }}</text>
            <text class="ud-nav-txt">{{ m.label }}</text>
          </view>
        </view>
      </view>

      <!-- 右侧主区 -->
      <view class="ud-main">
        <view v-if="isGuest" class="ud-guest-banner">
          <text class="ud-guest-banner__title">游客模式</text>
          <text class="ud-guest-banner__sub">当前展示示例数据，登录后可同步你的成长曲线与训练记录</text>
          <view class="ud-guest-banner__btn" @tap="goLogin">
            <text>登录 / 注册</text>
          </view>
        </view>

        <view v-if="activeMenu === 'favorites'" class="ud-card ud-panel-card">
          <view v-if="isGuest" class="ud-guest-lock">
            <text class="ud-guest-lock__ico">♡</text>
            <text class="ud-guest-lock__title">收藏场景与专家</text>
            <text class="ud-guest-lock__sub">登录后即可收藏常用场景与心仪专家</text>
            <view class="ud-guest-lock__btn" @tap="goLogin">
              <text>去登录</text>
            </view>
          </view>
          <MyFavorites v-else />
        </view>

        <view v-else-if="activeMenu === 'settings'" class="ud-card ud-panel-card">
          <view v-if="isGuest" class="ud-guest-lock">
            <text class="ud-guest-lock__ico">⚙</text>
            <text class="ud-guest-lock__title">账户设置</text>
            <text class="ud-guest-lock__sub">登录后可修改昵称、头像与安全设置</text>
            <view class="ud-guest-lock__btn" @tap="goLogin">
              <text>去登录</text>
            </view>
          </view>
          <AccountSettings v-else @profile-saved="onProfileSaved" />
        </view>

        <template v-else>
          <DailySelfTestCard />
          <view class="ud-card ud-chart-card">
            <view class="ud-chart-head">
              <view class="ud-chart-title-row">
                <text class="ud-chart-ico">📊</text>
                <text class="ud-chart-title">表达能力成长曲线</text>
              </view>
              <text class="ud-chart-meta">数据截止：{{ growth?.asOfDate ?? "—" }}</text>
            </view>
            <view class="ud-chart-row">
              <GrowthScoreGauge
                v-if="composite"
                class="ud-gauge"
                :score="composite.score"
                :hint="composite.hint"
              />
              <view class="ud-chart-main">
                <GrowthLineChart
                  v-if="growth"
                  v-model="growthPeriod"
                  :x-labels="growth.xLabels"
                  :values="growth.values"
                  @update:model-value="onGrowthPeriodChange"
                />
              </view>
            </view>
          </view>

          <AbilityTagCloud v-if="abilityTags.length" :tags="abilityTags" />

          <view v-if="!isGuest" class="ud-card ud-footprint">
            <MyFootprintTimeline />
          </view>
        </template>

        <view v-if="activeMenu !== 'settings' && activeMenu !== 'favorites'" class="ud-bottom">
          <view class="ud-card ud-reports">
            <view class="ud-sub-head">
              <view class="ud-sub-title-row">
                <text class="ud-sub-ico">📄</text>
                <text class="ud-sub-title">历史反馈报告</text>
              </view>
              <text class="ud-link" @tap="onViewAllReports">查看全部</text>
            </view>
            <view v-for="r in reports" :key="r.id" class="ud-report" @tap="openReport(r)">
              <text class="ud-report-title">{{ r.title }}</text>
              <view class="ud-report-row">
                <text class="ud-report-meta">{{ r.date }} | 导师：{{ r.tutor }}</text>
                <text class="ud-score" :class="scoreClass(r.score)">{{ r.score }}分</text>
              </view>
            </view>
          </view>

          <view class="ud-card ud-medals">
            <view class="ud-sub-head">
              <view class="ud-sub-title-row">
                <text class="ud-sub-ico ud-sub-ico--orange">🏅</text>
                <text class="ud-sub-title">沟通勋章墙</text>
              </view>
            </view>
            <view class="ud-medal-row">
              <view v-for="m in medals" :key="m.id" class="ud-medal">
                <view
                  class="ud-medal-circle"
                  :class="[
                    `ud-medal-circle--${m.tone}`,
                    { 'ud-medal-circle--off': !m.earned, 'ud-medal-circle--glow': m.earned },
                  ]"
                >
                  <text class="ud-medal-glyph">{{ medalGlyph(m) }}</text>
                </view>
                <text class="ud-medal-name" :class="{ 'ud-medal-name--off': !m.earned }">{{ m.name }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import GrowthLineChart from "@/components/dashboard/GrowthLineChart.vue";
import GrowthScoreGauge from "@/components/dashboard/GrowthScoreGauge.vue";
import AbilityTagCloud from "@/components/dashboard/AbilityTagCloud.vue";
import DailySelfTestCard from "@/components/dashboard/DailySelfTestCard.vue";
import MyFootprintTimeline from "@/components/community/MyFootprintTimeline.vue";
import MyFavorites from "@/components/favorites/MyFavorites.vue";
import AccountSettings from "@/components/settings/AccountSettings.vue";
import {
  fetchUserProfile,
  fetchGrowthDataByPeriod,
  fetchRecentReports,
  fetchMedals,
  fetchAbilityTags,
  fetchCompositeScore,
  fetchGuestDashboardBundle,
} from "@/api/modules/dashboard.js";
import type {
  UserDashboardProfile,
  GrowthChartData,
  RecentReportItem,
  MedalItem,
  DashboardMenuId,
  GrowthPeriod,
  AbilityTagItem,
  CompositeScoreData,
} from "@/types/dashboard";
import { useUserStore } from "@/store/user";
import { useVerifyStore } from "@/store/verify";
import type { UserProfileEditable } from "@/types/user/profile";

const userStore = useUserStore();
const verifyStore = useVerifyStore();

const isGuest = computed(() => !userStore.isLoggedIn);

const profile = ref<UserDashboardProfile | null>(null);
const growth = ref<GrowthChartData | null>(null);
const reports = ref<RecentReportItem[]>([]);
const medals = ref<MedalItem[]>([]);
const abilityTags = ref<AbilityTagItem[]>([]);
const composite = ref<CompositeScoreData | null>(null);
const growthPeriod = ref<GrowthPeriod>("month");
const activeMenu = ref<DashboardMenuId>("orders");
const hoverMenuId = ref<DashboardMenuId | "">("");

const menus = computed((): { id: DashboardMenuId; label: string; icon: string; danger?: boolean }[] => {
  const base: { id: DashboardMenuId; label: string; icon: string; danger?: boolean }[] = [
    { id: "orders", label: "我的对练订单", icon: "☰" },
    { id: "verify", label: "身份认证", icon: "🪪" },
    { id: "favorites", label: "收藏场景与专家", icon: "♡" },
    { id: "settings", label: "账户设置", icon: "⚙" },
  ];
  if (isGuest.value) {
    return [...base, { id: "login", label: "登录 / 注册", icon: "→" }];
  }
  return [...base, { id: "logout", label: "退出登录", icon: "⎋", danger: true }];
});

const avatarLetter = computed(() => (profile.value?.name ?? "学").slice(0, 1));

async function loadDashboard() {
  if (isGuest.value) {
    const bundle = await fetchGuestDashboardBundle(growthPeriod.value);
    profile.value = bundle.profile;
    growth.value = bundle.growth;
    reports.value = bundle.reports;
    medals.value = bundle.medals;
    abilityTags.value = bundle.abilityTags;
    composite.value = bundle.composite;
    return;
  }

  verifyStore.hydrateFromStorage();
  await verifyStore.loadStatus();

  const [p, g, r, md, tags, score] = await Promise.all([
    fetchUserProfile(),
    fetchGrowthDataByPeriod(growthPeriod.value),
    fetchRecentReports(),
    fetchMedals(),
    fetchAbilityTags(),
    fetchCompositeScore(),
  ]);
  profile.value = p;
  const nick = (userStore.userInfo as { nickname?: string } | null)?.nickname;
  if (nick) {
    profile.value = { ...p, name: nick };
  }
  growth.value = g;
  reports.value = r;
  medals.value = md;
  abilityTags.value = tags;
  composite.value = score;
}

onMounted(() => {
  loadDashboard();
});

onShow(() => {
  loadDashboard();
});

watch(isGuest, () => {
  loadDashboard();
});

async function onGrowthPeriodChange(period: GrowthPeriod) {
  growthPeriod.value = period;
  if (isGuest.value) {
    const bundle = await fetchGuestDashboardBundle(period);
    growth.value = bundle.growth;
    return;
  }
  growth.value = await fetchGrowthDataByPeriod(period);
}

function goLogin() {
  uni.navigateTo({ url: "/pages/auth/login" });
}

function onProfileSaved(p: UserProfileEditable) {
  if (profile.value) {
    profile.value = {
      ...profile.value,
      name: p.nickname,
      avatarUrl: p.avatarUrl,
    };
  }
}

function scoreClass(score: number): string {
  if (score >= 80) return "ud-score--high";
  if (score < 60) return "ud-score--low";
  return "ud-score--mid";
}

function medalGlyph(m: MedalItem): string {
  if (m.icon === "shield") return "🛡";
  if (m.icon === "check") return "✓";
  return "★";
}

function onMenu(id: DashboardMenuId) {
  activeMenu.value = id;
  if (id === "orders") {
    uni.switchTab({ url: "/pages/my-orders/my-orders" });
    return;
  }
  if (id === "login") {
    goLogin();
    return;
  }
  if (id === "logout") {
    uni.showModal({
      title: "确认退出",
      content: "确定要退出登录吗？",
      confirmText: "退出",
      cancelText: "取消",
      success: (res) => {
        if (res.confirm) {
          userStore.logout();
          uni.reLaunch({ url: "/pages/auth/login" });
        }
      },
    });
    return;
  }
  if (id === "verify") {
    if (isGuest.value) {
      uni.showToast({ title: "请先登录", icon: "none" });
      goLogin();
      return;
    }
    uni.navigateTo({ url: "/pages/identity-verify/identity-verify" });
    return;
  }
  if (id === "favorites" || id === "settings") {
    return;
  }
}

function onViewAllReports() {
  const first = reports.value[0];
  if (first) openReport(first);
  else uni.showToast({ title: "暂无历史报告", icon: "none" });
}

function openReport(r: RecentReportItem) {
  if (isGuest.value) {
    uni.showToast({ title: "登录后查看完整报告", icon: "none" });
    goLogin();
    return;
  }
  uni.navigateTo({ url: `/pages/report-detail/report-detail?orderId=${r.id}` });
}
</script>

<style scoped>
.ud-guest-banner {
  margin-bottom: 20rpx;
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border: 1rpx solid #bfdbfe;
}
.ud-guest-banner__title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1e40af;
}
.ud-guest-banner__sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.5;
}
.ud-guest-banner__btn {
  display: inline-block;
  margin-top: 16rpx;
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
}
.ud-guest-banner__btn text {
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
}
.ud-guest-lock {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  text-align: center;
}
.ud-guest-lock__ico {
  font-size: 56rpx;
  margin-bottom: 20rpx;
}
.ud-guest-lock__title {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}
.ud-guest-lock__sub {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.55;
}
.ud-guest-lock__btn {
  margin-top: 28rpx;
  padding: 18rpx 48rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
}
.ud-guest-lock__btn text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}

.ud {
  min-height: 100vh;
  background: #f8fafc;
  box-sizing: border-box;
  padding: 24rpx 20rpx 40rpx;
}

.ud-shell {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.ud-side {
  width: 100%;
}
.ud-main {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

@media (min-width: 1024px) {
  .ud {
    padding: 32px 24px 48px;
  }
  .ud-shell {
    flex-direction: row;
    align-items: flex-start;
    gap: 28px;
  }
  .ud-side {
    width: 280px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  .ud-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
}

.ud-card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 1rpx 3rpx rgba(15, 23, 42, 0.06);
  border: 1rpx solid #f1f5f9;
  box-sizing: border-box;
}
@media (min-width: 1024px) {
  .ud-card {
    border-radius: 16px;
  }
}

.ud-profile {
  padding: 32rpx 28rpx;
  text-align: center;
}
.ud-avatar-wrap {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 20rpx;
}
.ud-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  box-shadow: 0 0 0 4rpx #ffffff, 0 0 0 6rpx rgba(59, 130, 246, 0.35), 0 12rpx 32rpx rgba(37, 99, 235, 0.2);
}
.ud-avatar--ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #93c5fd, #3b82f6);
}
.ud-avatar-txt {
  font-size: 56rpx;
  font-weight: 800;
  color: #ffffff;
}
.ud-crown {
  position: absolute;
  right: 4rpx;
  bottom: 4rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.35);
}
.ud-crown-ico {
  font-size: 22rpx;
}
.ud-name {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #0f172a;
}
.ud-join {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #94a3b8;
}
.ud-tags {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 12rpx;
  margin-top: 20rpx;
}
.ud-tag {
  font-size: 22rpx;
  font-weight: 600;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
}
.ud-tag--blue {
  color: #1d4ed8;
  background: #eff6ff;
}
.ud-tag--gray {
  color: #64748b;
  background: #f1f5f9;
}
.ud-tag--ok {
  color: #047857;
  background: #ecfdf5;
}
.ud-tag--pending {
  color: #b45309;
  background: #fffbeb;
}

.ud-panel-card {
  overflow: visible;
  padding: 8rpx 4rpx;
}
@media (min-width: 1024px) {
  .ud-panel-card {
    padding: 12px 8px;
  }
}

.ud-chart-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}
@media (min-width: 1024px) {
  .ud-chart-row {
    flex-direction: row;
    align-items: center;
    gap: 0;
  }
  .ud-gauge {
    flex-shrink: 0;
    border-right: 1rpx solid #f1f5f9;
    padding-right: 20px;
    margin-right: 8px;
  }
  .ud-chart-main {
    flex: 1;
    min-width: 0;
    padding-left: 8px;
  }
}
.ud-chart-main {
  width: 100%;
}
.ud-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f1f5f9;
}
.ud-stat-num {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #0f172a;
}
.ud-stat-lbl {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #94a3b8;
}

.ud-nav {
  padding: 12rpx 0;
}
.ud-nav-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  padding: 22rpx 28rpx;
  margin: 0 12rpx 4rpx;
  border-radius: 16rpx;
  transition:
    background 0.22s ease,
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease;
}
.ud-nav-item--on {
  background: #eff6ff;
}
.ud-nav-item--danger .ud-nav-txt,
.ud-nav-item--danger .ud-nav-ico {
  color: #dc2626;
}
.ud-nav-ico {
  font-size: 28rpx;
  color: #64748b;
  width: 40rpx;
  text-align: center;
  transition: color 0.22s ease, transform 0.22s ease;
}
.ud-nav-item--on .ud-nav-ico,
.ud-nav-item--on .ud-nav-txt {
  color: #2563eb;
}
.ud-nav-txt {
  font-size: 28rpx;
  font-weight: 600;
  color: #475569;
  transition: color 0.22s ease;
}

/* #ifdef H5 */
.ud-nav-item {
  cursor: pointer;
}
.ud-nav-item--hover:not(.ud-nav-item--on):not(.ud-nav-item--danger) {
  background: #f0f7ff;
  transform: translateX(6rpx);
  box-shadow: 0 4rpx 16rpx rgba(37, 99, 235, 0.08);
}
.ud-nav-item--hover:not(.ud-nav-item--on):not(.ud-nav-item--danger) .ud-nav-ico,
.ud-nav-item--hover:not(.ud-nav-item--on):not(.ud-nav-item--danger) .ud-nav-txt {
  color: #2563eb;
}
.ud-nav-item--hover:not(.ud-nav-item--on):not(.ud-nav-item--danger) .ud-nav-ico {
  transform: scale(1.08);
}
.ud-nav-item--hover.ud-nav-item--on {
  background: #dbeafe;
  transform: translateX(4rpx);
  box-shadow: 0 6rpx 20rpx rgba(37, 99, 235, 0.12);
}
.ud-nav-item--hover.ud-nav-item--danger {
  background: #fef2f2;
  transform: translateX(4rpx);
  box-shadow: 0 4rpx 14rpx rgba(220, 38, 38, 0.1);
}
.ud-nav-item--hover.ud-nav-item--danger .ud-nav-ico,
.ud-nav-item--hover.ud-nav-item--danger .ud-nav-txt {
  color: #b91c1c;
}
.ud-nav-item--hover.ud-nav-item--danger .ud-nav-ico {
  transform: scale(1.06);
}
/* #endif */

.ud-chart-card {
  padding: 28rpx 24rpx 20rpx;
}
.ud-chart-head {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.ud-chart-title-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
}
.ud-chart-ico {
  font-size: 32rpx;
}
.ud-chart-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}
.ud-chart-meta {
  font-size: 22rpx;
  color: #94a3b8;
}

.ud-bottom {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
@media (min-width: 1024px) {
  .ud-bottom {
    flex-direction: row;
    align-items: stretch;
  }
  .ud-reports {
    flex: 1.1;
    min-width: 0;
  }
  .ud-medals {
    flex: 0.9;
    min-width: 0;
  }
}

.ud-sub-head {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.ud-sub-title-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}
.ud-sub-ico {
  font-size: 28rpx;
}
.ud-sub-ico--orange {
  filter: saturate(1.2);
}
.ud-sub-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}
.ud-link {
  font-size: 24rpx;
  font-weight: 600;
  color: #2563eb;
}

.ud-reports {
  padding: 28rpx 24rpx;
}
.ud-report {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
  cursor: pointer;
}
.ud-report:active {
  opacity: 0.85;
}
.ud-report:last-child {
  border-bottom: none;
}
.ud-report-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10rpx;
}
.ud-report-row {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}
.ud-report-meta {
  font-size: 24rpx;
  color: #64748b;
}
.ud-score {
  font-size: 28rpx;
  font-weight: 800;
}
.ud-score--high {
  color: #059669;
}
.ud-score--mid {
  color: #2563eb;
}
.ud-score--low {
  color: #ea580c;
}

.ud-medals {
  padding: 28rpx 24rpx;
}
.ud-medal-row {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 24rpx;
}
.ud-medal {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 160rpx;
}
.ud-medal-circle {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  transition:
    transform 0.3s ease,
    filter 0.3s ease,
    box-shadow 0.3s ease;
}
.ud-medal-circle--blue {
  background: linear-gradient(145deg, #60a5fa, #2563eb);
}
.ud-medal-circle--orange {
  background: linear-gradient(145deg, #fb923c, #ea580c);
}
.ud-medal-circle--purple {
  background: linear-gradient(145deg, #c084fc, #7c3aed);
}
.ud-medal-circle--off {
  filter: grayscale(1);
  opacity: 0.45;
  box-shadow: none;
}
.ud-medal-circle--glow {
  box-shadow: 0 8rpx 28rpx rgba(59, 130, 246, 0.35);
  animation: udPulse 2.4s ease-in-out infinite;
}
.ud-medal-circle--orange.ud-medal-circle--glow {
  box-shadow: 0 8rpx 28rpx rgba(234, 88, 12, 0.35);
}
.ud-medal-circle--purple.ud-medal-circle--glow {
  box-shadow: 0 8rpx 28rpx rgba(124, 58, 237, 0.3);
}
@keyframes udPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.04);
  }
}
.ud-medal-glyph {
  font-size: 40rpx;
  color: #ffffff;
  font-weight: 800;
}
.ud-medal-name {
  font-size: 22rpx;
  font-weight: 600;
  color: #334155;
  text-align: center;
}
.ud-medal-name--off {
  color: #94a3b8;
}
</style>
