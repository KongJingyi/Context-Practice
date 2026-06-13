<template>
  <view class="cf">
    <view class="cf-anon">
      <view class="cf-anon-ico">🛡</view>
      <text class="cf-anon-txt">匿名保护 · 您的身份信息将被严格保密</text>
      <switch :checked="anonymous" color="#ef4444" @change="onAnonChange" />
    </view>

    <view class="cf-step">
      <text class="cf-step-num">1</text>
      <text class="cf-step-lbl">选择投诉类型</text>
    </view>
    <view class="cf-types">
      <view
        v-for="t in types"
        :key="t.id"
        class="cf-type"
        :class="{ 'cf-type--on': complaintType === t.id }"
        @tap="complaintType = t.id as ComplaintType"
      >
        <text>{{ t.label }}</text>
      </view>
    </view>

    <view class="cf-step">
      <text class="cf-step-num">2</text>
      <text class="cf-step-lbl">详细描述</text>
    </view>
    <textarea
      v-model="description"
      class="cf-textarea"
      placeholder="请客观描述问题经过，便于平台核实…"
      :maxlength="1000"
    />

    <view class="cf-step">
      <text class="cf-step-num">3</text>
      <text class="cf-step-lbl">上传凭证（选填）</text>
    </view>
    <view class="cf-attach">
      <view class="cf-attach-btn" @tap="pickImage">
        <text class="cf-attach-ico">🖼</text>
        <text>聊天截图</text>
      </view>
      <view class="cf-attach-btn" @tap="linkSessionAudio">
        <text class="cf-attach-ico">🎙</text>
        <text>关联本场录音</text>
      </view>
    </view>
    <view v-if="attachments.length" class="cf-previews">
      <view v-for="(a, i) in attachments" :key="i" class="cf-preview">
        <image v-if="a.type === 'image'" class="cf-preview-img" :src="a.url" mode="aspectFill" />
        <view v-else class="cf-preview-audio">
          <text>🎵 录音片段</text>
        </view>
        <text class="cf-preview-del" @tap="removeAttach(i)">×</text>
      </view>
    </view>

    <view class="cf-submit" :class="{ 'cf-submit--disabled': !canSubmit }" @tap="submit">
      <text>提交投诉</text>
    </view>

    <view class="cf-service" @tap="callService">
      <text class="cf-service-ico">📞</text>
      <text>一键呼叫人工客服</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { COMPLAINT_TYPES, openComplaint } from "@/api/modules/complaint.js";
import { filterSensitiveText } from "@/utils/community/sensitiveFilter";
import type { ComplaintType } from "@/types/review";

const props = defineProps<{
  orderId: string;
}>();

const emit = defineEmits<{ (e: "submitted", id: string): void }>();

const types = COMPLAINT_TYPES;
const complaintType = ref<ComplaintType>("attitude_issue");
const description = ref("");
const anonymous = ref(true);
const attachments = ref<{ type: "image" | "audio"; url: string }[]>([]);

const canSubmit = computed(
  () => complaintType.value && description.value.trim().length >= 10,
);

function onAnonChange(e: unknown) {
  const ev = e as { detail?: { value?: boolean } };
  anonymous.value = !!ev.detail?.value;
}

function pickImage() {
  uni.chooseImage({
    count: 3,
    success: (res) => {
      const paths = res.tempFilePaths;
      const list = Array.isArray(paths) ? paths : paths ? [paths] : [];
      list.forEach((url: string) => {
        attachments.value.push({ type: "image", url });
      });
    },
  });
}

function linkSessionAudio() {
  attachments.value.push({
    type: "audio",
    url: `mock-audio-${props.orderId}`,
  });
  uni.showToast({ title: "已关联本场训练录音", icon: "success" });
}

function removeAttach(i: number) {
  attachments.value = attachments.value.filter((_, idx) => idx !== i);
}

async function submit() {
  if (!canSubmit.value) {
    uni.showToast({ title: "请填写至少 10 字描述", icon: "none" });
    return;
  }
  const check = filterSensitiveText(description.value);
  if (!check.ok) {
    uni.showToast({ title: check.message || "内容不合规", icon: "none" });
    return;
  }
  const res = (await openComplaint({
    orderId: props.orderId,
    type: complaintType.value,
    description: description.value.trim(),
    attachments: attachments.value.map((a) => a.url),
    isAnonymous: anonymous.value,
  })) as { id: string };
  emit("submitted", res.id);
}

function callService() {
  uni.showModal({
    title: "人工客服",
    content: "严重违规（如语言骚扰）可优先拨打 400-xxx-xxxx，平台将 15 分钟内响应。",
    confirmText: "我知道了",
    showCancel: false,
  });
}
</script>

<style scoped>
.cf {
  padding: 24rpx;
}
.cf-anon {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  margin-bottom: 28rpx;
  border-radius: 12rpx;
  background: #fef2f2;
  border: 1rpx solid #fecaca;
}
.cf-anon-ico {
  font-size: 32rpx;
}
.cf-anon-txt {
  flex: 1;
  font-size: 24rpx;
  color: #991b1b;
  font-weight: 600;
  line-height: 1.4;
}
.cf-step {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}
.cf-step-num {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #475569;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
  text-align: center;
  line-height: 36rpx;
}
.cf-step-lbl {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
}
.cf-types {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 28rpx;
}
.cf-type {
  padding: 14rpx 24rpx;
  border-radius: 12rpx;
  background: #f1f5f9;
  border: 2rpx solid transparent;
}
.cf-type text {
  font-size: 26rpx;
  color: #475569;
  font-weight: 600;
}
.cf-type--on {
  background: #fef2f2;
  border-color: #ef4444;
}
.cf-type--on text {
  color: #b91c1c;
}
.cf-textarea {
  width: 100%;
  min-height: 220rpx;
  padding: 20rpx;
  margin-bottom: 28rpx;
  border-radius: 12rpx;
  background: #fff;
  border: 1rpx solid #cbd5e1;
  font-size: 26rpx;
  color: #0f172a;
  box-sizing: border-box;
}
.cf-attach {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.cf-attach-btn {
  flex: 1;
  padding: 24rpx 16rpx;
  border-radius: 12rpx;
  background: #fff;
  border: 1rpx dashed #94a3b8;
  text-align: center;
}
.cf-attach-ico {
  display: block;
  font-size: 36rpx;
  margin-bottom: 8rpx;
}
.cf-attach-btn text:last-child {
  font-size: 24rpx;
  color: #64748b;
}
.cf-previews {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 28rpx;
}
.cf-preview {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  overflow: hidden;
}
.cf-preview-img {
  width: 100%;
  height: 100%;
}
.cf-preview-audio {
  width: 100%;
  height: 100%;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #475569;
}
.cf-preview-del {
  position: absolute;
  right: 4rpx;
  top: 4rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  text-align: center;
  line-height: 36rpx;
  font-size: 28rpx;
}
.cf-submit {
  padding: 22rpx;
  border-radius: 12rpx;
  background: #ef4444;
  text-align: center;
  margin-bottom: 24rpx;
}
.cf-submit--disabled {
  opacity: 0.5;
}
.cf-submit text {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
}
.cf-service {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx;
}
.cf-service-ico {
  font-size: 28rpx;
}
.cf-service text:last-child {
  font-size: 26rpx;
  color: #64748b;
  font-weight: 600;
}
</style>
