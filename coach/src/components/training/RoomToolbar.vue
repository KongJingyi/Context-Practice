<template>
  <div class="rtb">
    <button
      v-for="btn in buttons"
      :key="btn.id"
      type="button"
      class="rtb-btn"
      :class="{
        'rtb-btn--on': btn.active,
        'rtb-btn--danger': btn.danger,
      }"
      @click="emit('action', btn.id)"
    >
      <span class="rtb-ico-wrap">
        <span class="rtb-glyph" :class="`rtb-glyph--${btn.glyph}`" />
      </span>
      <span class="rtb-lbl">{{ btn.label }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  muted: boolean;
  cameraOff: boolean;
}>();

const emit = defineEmits<{
  (e: "action", id: string): void;
}>();

const buttons = computed(() => [
  {
    id: "mute",
    glyph: props.muted ? "mic-off" : "mic",
    label: props.muted ? "已静音" : "静音",
    active: props.muted,
  },
  {
    id: "camera",
    glyph: props.cameraOff ? "camera-off" : "camera",
    label: props.cameraOff ? "开摄像头" : "摄像头",
    active: props.cameraOff,
  },
  {
    id: "highlight",
    glyph: "star",
    label: "高光",
    active: false,
  },
  {
    id: "hangup",
    glyph: "phone",
    label: "结束",
    active: false,
    danger: true,
  },
]);
</script>

<style scoped>
.rtb {
  display: flex;
  flex-direction: row;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px 20px;
  padding: 14px 24px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.78);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
}
.rtb-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 72px;
  padding: 10px 12px;
  border-radius: 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition:
    background 0.22s ease,
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease;
}
.rtb-btn:hover:not(.rtb-btn--danger) {
  background: rgba(59, 130, 246, 0.18);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.2);
}
.rtb-btn--on {
  background: rgba(59, 130, 246, 0.28);
}
.rtb-btn--danger {
  background: rgba(220, 38, 38, 0.38);
  min-width: 80px;
}
.rtb-btn--danger:hover {
  background: rgba(220, 38, 38, 0.55);
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.35);
}
.rtb-ico-wrap {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rtb-glyph {
  position: relative;
  color: #f1f5f9;
  transition: color 0.22s ease, transform 0.22s ease;
}
.rtb-btn--on .rtb-glyph {
  color: #93c5fd;
}
.rtb-btn--danger .rtb-glyph {
  color: #fff;
}
.rtb-glyph--mic {
  width: 14px;
  height: 20px;
}
.rtb-glyph--mic::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 0;
  width: 10px;
  height: 14px;
  margin-left: -5px;
  border: 2px solid currentColor;
  border-radius: 6px;
  box-sizing: border-box;
}
.rtb-glyph--mic::after {
  content: "";
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 14px;
  height: 8px;
  margin-left: -7px;
  border: 2px solid currentColor;
  border-top: none;
  border-radius: 0 0 8px 8px;
  box-sizing: border-box;
}
.rtb-glyph--mic-off {
  width: 18px;
  height: 20px;
  color: #fca5a5;
}
.rtb-glyph--mic-off::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 2px;
  width: 10px;
  height: 12px;
  margin-left: -5px;
  border: 2px solid currentColor;
  border-radius: 6px;
  box-sizing: border-box;
  opacity: 0.85;
}
.rtb-glyph--mic-off::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 18px;
  height: 2px;
  margin-left: -9px;
  margin-top: -1px;
  background: currentColor;
  transform: rotate(-42deg);
  border-radius: 1px;
}
.rtb-glyph--camera {
  width: 22px;
  height: 18px;
}
.rtb-glyph--camera::before {
  content: "";
  position: absolute;
  inset: 4px 0 0 0;
  border: 2px solid currentColor;
  border-radius: 4px;
  box-sizing: border-box;
}
.rtb-glyph--camera::after {
  content: "";
  position: absolute;
  right: -3px;
  top: 7px;
  border-style: solid;
  border-width: 4px 0 4px 7px;
  border-color: transparent transparent transparent currentColor;
}
.rtb-glyph--camera-off {
  width: 22px;
  height: 18px;
  color: #93c5fd;
}
.rtb-glyph--camera-off::before {
  content: "";
  position: absolute;
  inset: 4px 0 0 0;
  border: 2px solid currentColor;
  border-radius: 4px;
  box-sizing: border-box;
  opacity: 0.7;
}
.rtb-glyph--camera-off::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 22px;
  height: 2px;
  margin-left: -11px;
  margin-top: -1px;
  background: #fca5a5;
  transform: rotate(-38deg);
  border-radius: 1px;
}
.rtb-glyph--star {
  width: 20px;
  height: 20px;
  color: #fde68a;
}
.rtb-glyph--star::before {
  content: "✦";
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  line-height: 1;
  color: #fde68a;
  text-shadow: 0 0 12px rgba(251, 191, 36, 0.55);
}
.rtb-glyph--phone {
  width: 20px;
  height: 20px;
  transform: rotate(135deg);
}
.rtb-glyph--phone::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 14px;
  height: 14px;
  margin-left: -7px;
  margin-top: -7px;
  border: 2px solid currentColor;
  border-radius: 4px;
  box-sizing: border-box;
}
.rtb-lbl {
  font-size: 12px;
  color: #e2e8f0;
  font-weight: 600;
}
.rtb-btn:hover .rtb-lbl {
  color: #fff;
}
</style>
