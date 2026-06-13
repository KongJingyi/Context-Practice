<template>
  <aside class="toolbox">
    <div class="toolbox-section">
      <h2 class="toolbox-heading">压力工具</h2>

      <div class="toolbox-block">
        <p class="toolbox-label">倒计时</p>
        <div class="toolbox-chips">
          <button type="button" class="toolbox-chip" @click="emit('countdown', 30)">30s</button>
          <button type="button" class="toolbox-chip" @click="emit('countdown', 60)">60s</button>
          <button type="button" class="toolbox-chip" @click="emit('countdown', 120)">120s</button>
          <button
            v-if="countdownActive"
            type="button"
            class="toolbox-chip toolbox-chip--danger"
            @click="emit('stopCountdown')"
          >
            停
          </button>
        </div>
        <div v-if="countdownActive" class="toolbox-countdown">
          <div class="toolbox-countdown-bar">
            <div class="toolbox-countdown-fill" :style="{ width: countdownPercent + '%' }" />
          </div>
          <p class="toolbox-countdown-text">{{ countdownLeft }}s</p>
        </div>
      </div>

      <div class="toolbox-block">
        <p class="toolbox-label">一键打断</p>
        <div class="toolbox-chips">
          <button type="button" class="toolbox-chip" @click="emit('interrupt', '请直接给结论')">给结论</button>
          <button type="button" class="toolbox-chip" @click="emit('interrupt', '时间紧凑，请精简')">精简</button>
          <button type="button" class="toolbox-chip" @click="emit('interrupt', '请回到主线')">回主线</button>
        </div>
      </div>

      <div class="toolbox-block">
        <p class="toolbox-label">压力提问</p>
        <div class="toolbox-chips">
          <button
            v-for="q in pressureQuestions"
            :key="q.id"
            type="button"
            class="toolbox-chip toolbox-chip--warn"
            @click="emit('question', q.text, q.id)"
          >
            {{ q.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="toolbox-section">
      <div class="toolbox-row">
        <h2 class="toolbox-heading">白板</h2>
        <button
          type="button"
          class="toolbox-chip"
          :class="whiteboardActive ? 'toolbox-chip--on' : ''"
          @click="emit('whiteboardToggle')"
        >
          {{ whiteboardActive ? "已开启" : "开启" }}
        </button>
      </div>
    </div>

    <div class="toolbox-section toolbox-section--toggle" @click="materialsOpen = !materialsOpen">
      <div class="toolbox-row">
        <h2 class="toolbox-heading">训练资料</h2>
        <span class="toolbox-toggle">{{ materialsOpen ? "收起 ▲" : "展开 ▼" }}</span>
      </div>
      <div v-if="materialsOpen" class="toolbox-materials">
        <div v-for="m in materials" :key="m.id" class="toolbox-material">
          <span class="toolbox-material-name">{{ m.name }}</span>
          <span class="toolbox-material-size">{{ m.sizeLabel }}</span>
        </div>
        <p v-if="!materials.length" class="toolbox-empty">暂无资料</p>
        <label class="toolbox-upload">
          <input type="file" class="toolbox-upload-input" @change="onFilePick" />
          <span>{{ uploading ? "上传中…" : "+ 上传资料" }}</span>
        </label>
      </div>
    </div>

    <div class="toolbox-section toolbox-section--chat">
      <h2 class="toolbox-heading">消息</h2>
      <div class="toolbox-messages">
        <p v-if="!messages.length" class="toolbox-empty">暂无消息</p>
        <div
          v-for="(msg, i) in messages"
          :key="i"
          class="toolbox-msg"
          :class="msg.from === 'coach' ? 'toolbox-msg--coach' : 'toolbox-msg--user'"
        >
          {{ msg.text }}
        </div>
      </div>
      <div class="toolbox-chat-input">
        <input
          v-model="chatDraft"
          type="text"
          class="toolbox-input"
          placeholder="输入消息…"
          @keyup.enter="send"
        />
        <button type="button" class="toolbox-send" @click="send">发送</button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import type { MaterialItem } from "@/api/modules/materials";
import { fetchPressureQuestions } from "@/api/modules/coach";

const DEFAULT_PRESSURE = [
  { id: "d1", label: "项目延期", text: "如果项目延期你怎么交代？" },
  { id: "d2", label: "方案否决", text: "你的方案被领导否决了怎么办？" },
  { id: "d3", label: "预算砍半", text: "你的预算被砍了50%，怎么调整？" },
];

const pressureQuestions = ref(DEFAULT_PRESSURE);

onMounted(async () => {
  const list = await fetchPressureQuestions();
  if (list.length) pressureQuestions.value = list;
});

defineProps<{
  materials: MaterialItem[];
  messages: { from: string; text: string }[];
  countdownActive: boolean;
  countdownLeft: number;
  countdownPercent: number;
  whiteboardActive: boolean;
  uploading?: boolean;
}>();

const emit = defineEmits<{
  (e: "countdown", seconds: number): void;
  (e: "stopCountdown"): void;
  (e: "interrupt", message: string): void;
  (e: "question", text: string, questionId?: string): void;
  (e: "chat", text: string): void;
  (e: "whiteboardToggle"): void;
  (e: "uploadMaterial", file: File): void;
}>();

const materialsOpen = ref(false);
const chatDraft = ref("");

function send() {
  const text = chatDraft.value.trim();
  if (!text) return;
  emit("chat", text);
  chatDraft.value = "";
}

function onFilePick(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  if (file) emit("uploadMaterial", file);
  input.value = "";
}
</script>

<style scoped>
.toolbox {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: rgba(15, 23, 42, 0.92);
  border-left: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
}
.toolbox-section {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.toolbox-section--toggle {
  cursor: pointer;
}
.toolbox-section--chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  border-bottom: none;
}
.toolbox-heading {
  font-size: 13px;
  font-weight: 700;
  color: #e2e8f0;
  margin: 0 0 12px;
}
.toolbox-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.toolbox-row .toolbox-heading {
  margin: 0;
}
.toolbox-toggle {
  font-size: 11px;
  color: #64748b;
}
.toolbox-block {
  margin-bottom: 14px;
}
.toolbox-block:last-child {
  margin-bottom: 0;
}
.toolbox-label {
  font-size: 11px;
  color: #64748b;
  margin: 0 0 8px;
}
.toolbox-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.toolbox-chip {
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 600;
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.toolbox-chip:hover {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
}
.toolbox-chip--warn:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}
.toolbox-chip--danger {
  color: #fca5a5;
  border-color: rgba(239, 68, 68, 0.3);
}
.toolbox-chip--on {
  background: rgba(59, 130, 246, 0.35);
  color: #93c5fd;
}
.toolbox-upload {
  display: block;
  margin-top: 10px;
  padding: 8px;
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.12);
  border: 1px dashed rgba(59, 130, 246, 0.35);
  border-radius: 8px;
  cursor: pointer;
}
.toolbox-upload-input {
  display: none;
}
.toolbox-countdown {
  margin-top: 8px;
}
.toolbox-countdown-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  overflow: hidden;
}
.toolbox-countdown-fill {
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f97316);
  border-radius: 999px;
  transition: width 1s linear;
}
.toolbox-countdown-text {
  margin: 4px 0 0;
  font-size: 11px;
  color: #94a3b8;
  text-align: right;
}
.toolbox-materials {
  margin-top: 12px;
}
.toolbox-material {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  font-size: 11px;
}
.toolbox-material-name {
  color: #e2e8f0;
}
.toolbox-material-size {
  color: #64748b;
}
.toolbox-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
  min-height: 120px;
}
.toolbox-empty {
  font-size: 11px;
  color: #64748b;
  text-align: center;
  padding: 24px 0;
  margin: 0;
}
.toolbox-msg {
  max-width: 88%;
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.45;
}
.toolbox-msg--coach {
  margin-left: auto;
  background: rgba(59, 130, 246, 0.35);
  color: #e2e8f0;
  border-bottom-right-radius: 4px;
}
.toolbox-msg--user {
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  border-bottom-left-radius: 4px;
}
.toolbox-chat-input {
  display: flex;
  gap: 8px;
}
.toolbox-input {
  flex: 1;
  padding: 8px 12px;
  font-size: 13px;
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  outline: none;
}
.toolbox-input::placeholder {
  color: #64748b;
}
.toolbox-input:focus {
  border-color: rgba(96, 165, 250, 0.5);
}
.toolbox-send {
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(59, 130, 246, 0.6);
  border: none;
  border-radius: 10px;
  cursor: pointer;
}
.toolbox-send:hover {
  background: rgba(59, 130, 246, 0.85);
}
</style>
