<template>
  <CoachLayout page-title="排班设置" page-subtitle="设置每周可接单时段，自动同步至学员预约系统">
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="grid lg:grid-cols-3 gap-6">
        <!-- 左侧说明 -->
        <div class="coach-card p-6 lg:col-span-1 h-fit">
          <h2 class="font-semibold text-slate-900 mb-3">排班规则</h2>
          <ul class="space-y-3 text-sm text-slate-500">
            <li class="flex gap-2">
              <span class="text-teal-600 font-bold shrink-0">·</span>
              最小时间单位 30 分钟，每段至少 1 小时
            </li>
            <li class="flex gap-2">
              <span class="text-teal-600 font-bold shrink-0">·</span>
              支持同一天设置多个不连续时段
            </li>
            <li class="flex gap-2">
              <span class="text-teal-600 font-bold shrink-0">·</span>
              保存后立即同步至预约日历
            </li>
          </ul>
          <div class="mt-6 p-4 rounded-xl bg-teal-50 border border-teal-100">
            <p class="text-xs text-teal-800 font-medium">本周可接单</p>
            <p class="text-2xl font-bold text-teal-700 mt-1">{{ enabledCount }} 个时段</p>
          </div>
        </div>

        <!-- 排班编辑 -->
        <div class="lg:col-span-2 space-y-4">
          <div
            v-for="day in weekDays"
            :key="day.dow"
            class="coach-card p-5"
          >
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-3">
                <span class="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center text-sm font-bold text-slate-700">
                  {{ day.short }}
                </span>
                <span class="font-semibold text-slate-900">{{ day.label }}</span>
              </div>
              <button
                class="text-xs text-teal-600 hover:text-teal-700 font-medium px-3 py-1.5 rounded-lg hover:bg-teal-50 transition-colors"
                @click="addSlot(day.dow)"
              >
                + 添加时段
              </button>
            </div>

            <div v-if="slotsForDay(day.dow).length" class="space-y-2">
              <div
                v-for="(slot, idx) in slotsForDay(day.dow)"
                :key="`${day.dow}-${idx}`"
                class="flex items-center gap-3 p-3 bg-slate-50 rounded-xl"
              >
                <el-time-select
                  v-model="slot.startTime"
                  start="08:00"
                  step="00:30"
                  end="22:00"
                  placeholder="开始"
                  size="small"
                  class="!w-28"
                />
                <span class="text-slate-300">—</span>
                <el-time-select
                  v-model="slot.endTime"
                  start="08:00"
                  step="00:30"
                  end="23:00"
                  placeholder="结束"
                  size="small"
                  class="!w-28"
                />
                <el-switch v-model="slot.enabled" size="small" />
                <button
                  class="ml-auto text-slate-400 hover:text-red-500 p-1 rounded transition-colors"
                  @click="removeSlot(day.dow, idx)"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
            <p v-else class="text-sm text-slate-400 py-2">当日休息，点击上方添加时段</p>
          </div>

          <div class="flex gap-3 pt-2">
            <button
              class="px-6 py-2.5 bg-teal-600 text-white rounded-xl text-sm font-semibold hover:bg-teal-700 transition-colors disabled:opacity-50"
              :disabled="saving"
              @click="handleSave"
            >
              {{ saving ? "保存中…" : "保存排班" }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </CoachLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import CoachLayout from "@/components/layout/CoachLayout.vue";
import LoadingState from "@/components/ui/LoadingState.vue";
import { fetchSchedule, saveSchedule, type ScheduleSlot } from "@/api/modules/schedule";

const loading = ref(true);
const saving = ref(false);
const slots = ref<ScheduleSlot[]>([]);

const weekDays = [
  { dow: 1, label: "周一", short: "一" },
  { dow: 2, label: "周二", short: "二" },
  { dow: 3, label: "周三", short: "三" },
  { dow: 4, label: "周四", short: "四" },
  { dow: 5, label: "周五", short: "五" },
  { dow: 6, label: "周六", short: "六" },
  { dow: 0, label: "周日", short: "日" },
];

const enabledCount = computed(() => slots.value.filter((s) => s.enabled !== false).length);

function slotsForDay(dow: number) {
  return slots.value.filter((s) => s.dayOfWeek === dow);
}

function addSlot(dow: number) {
  slots.value.push({ dayOfWeek: dow, startTime: "09:00", endTime: "12:00", enabled: true });
}

function removeSlot(dow: number, idx: number) {
  const daySlots = slots.value.filter((s) => s.dayOfWeek === dow);
  const target = daySlots[idx];
  slots.value = slots.value.filter((s) => s !== target);
}

async function handleSave() {
  saving.value = true;
  try {
    await saveSchedule(slots.value);
    ElMessage.success("排班已保存并同步");
  } catch {
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  slots.value = await fetchSchedule();
  loading.value = false;
});
</script>
