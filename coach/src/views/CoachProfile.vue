<template>
  <CoachLayout page-title="个人主页" page-subtitle="配置头像、简介、擅长场景与服务标签">
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="grid lg:grid-cols-3 gap-6">
        <!-- 左侧资料卡 -->
        <div class="lg:col-span-1 space-y-4">
          <div class="coach-card p-6 text-center">
            <div class="w-20 h-20 mx-auto rounded-2xl bg-gradient-to-br from-teal-500 to-teal-600 flex items-center justify-center text-white text-3xl font-bold shadow-lg shadow-teal-200/50">
              {{ (profile?.nickname || profile?.name || "教").charAt(0) }}
            </div>
            <h2 class="text-xl font-bold text-slate-900 mt-4">{{ profile?.nickname || profile?.name }}</h2>
            <div class="flex items-center justify-center gap-2 mt-2 flex-wrap">
              <span class="coach-badge bg-amber-50 text-amber-700">{{ profile?.level || "陪练员" }}</span>
              <span v-if="profile?.rankTag" class="coach-badge bg-teal-50 text-teal-700">{{ profile.rankTag }}</span>
            </div>
            <p class="text-xs text-slate-400 mt-3">加入平台 {{ profile?.joinDays || "—" }} 天</p>
          </div>

          <div class="coach-card p-5">
            <div class="grid grid-cols-3 gap-2 text-center">
              <div>
                <p class="text-xl font-bold text-teal-600">{{ profile?.finishedCourses || 0 }}</p>
                <p class="text-[10px] text-slate-400 mt-1">完成训练</p>
              </div>
              <div>
                <p class="text-xl font-bold text-blue-600">{{ profile?.totalHoursLabel || "—" }}</p>
                <p class="text-[10px] text-slate-400 mt-1">训练时长</p>
              </div>
              <div>
                <p class="text-xl font-bold text-emerald-600">{{ profile?.goodReviewRate ? `${(profile.goodReviewRate * 100).toFixed(0)}%` : "—" }}</p>
                <p class="text-[10px] text-slate-400 mt-1">好评率</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧编辑与评价 -->
        <div class="lg:col-span-2 space-y-6">
          <div class="coach-card p-6">
            <div class="flex items-center justify-between mb-5">
              <h2 class="font-semibold text-slate-900">主页配置</h2>
              <button
                class="text-sm text-teal-600 hover:text-teal-700 font-medium"
                @click="showEdit = !showEdit"
              >
                {{ showEdit ? "收起" : "编辑资料" }}
              </button>
            </div>

            <div v-if="!showEdit">
              <p class="text-sm text-slate-600 leading-relaxed">{{ editForm.bio || "暂无简介，点击编辑资料完善个人主页" }}</p>
              <div class="flex flex-wrap gap-1.5 mt-4" v-if="displayTags.length">
                <span v-for="tag in displayTags" :key="tag" class="coach-badge bg-teal-50 text-teal-700">{{ tag }}</span>
              </div>
              <div class="mt-4" v-if="editForm.scenes.length">
                <p class="text-xs text-slate-400 mb-2">擅长场景</p>
                <div class="flex flex-wrap gap-2">
                  <span v-for="s in editForm.scenes" :key="s" class="px-3 py-1 bg-slate-100 text-slate-600 rounded-lg text-xs">{{ sceneLabel(s) }}</span>
                </div>
              </div>
            </div>

            <div v-else class="space-y-4">
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1.5 block">昵称</label>
                <el-input v-model="editForm.nickname" maxlength="15" placeholder="展示给学员的昵称" />
              </div>
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1.5 block">个人简介</label>
                <el-input v-model="editForm.bio" type="textarea" :rows="3" maxlength="200" placeholder="介绍你的陪练风格与经验" />
              </div>
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1.5 block">擅长场景</label>
                <el-select v-model="editForm.scenes" multiple placeholder="选择擅长场景" class="w-full">
                  <el-option v-for="s in sceneOptions" :key="s.value" :label="s.label" :value="s.value" />
                </el-select>
              </div>
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1.5 block">服务标签</label>
                <el-select v-model="editForm.tags" multiple allow-create filterable placeholder="如：耐心、专业、高效" class="w-full">
                  <el-option v-for="t in defaultTags" :key="t" :label="t" :value="t" />
                </el-select>
              </div>
              <button
                class="px-6 py-2.5 bg-teal-600 text-white rounded-xl text-sm font-semibold hover:bg-teal-700 transition-colors disabled:opacity-50"
                :disabled="saving"
                @click="handleSave"
              >
                {{ saving ? "保存中…" : "保存资料" }}
              </button>
            </div>
          </div>

          <!-- 学员评价 -->
          <div class="coach-card p-6">
            <h2 class="font-semibold text-slate-900 mb-4">学员评价</h2>
            <EmptyState v-if="!reviews.length" title="暂无评价" description="完成陪练后学员的评价将显示在这里" />
            <div v-else class="space-y-3">
              <div v-for="r in reviews" :key="r.id" class="p-4 bg-slate-50 rounded-xl">
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-full bg-teal-100 flex items-center justify-center text-xs text-teal-700 font-bold">
                      {{ (r.userName || r.user || "?").charAt(0) }}
                    </div>
                    <span class="text-sm font-medium text-slate-700">{{ r.userName || r.user }}</span>
                  </div>
                  <div class="flex gap-0.5">
                    <span v-for="n in 5" :key="n" :class="n <= (r.rating || r.score || 0) ? 'text-amber-400' : 'text-slate-200'">★</span>
                  </div>
                </div>
                <p class="text-sm text-slate-600">{{ r.content }}</p>
                <div class="flex gap-1 mt-2 flex-wrap" v-if="r.tags?.length">
                  <span v-for="t in r.tags" :key="t" class="coach-badge bg-white text-slate-500 border border-slate-200">{{ t }}</span>
                </div>
                <div class="flex gap-2 mt-3">
                  <button class="text-xs text-teal-600 hover:underline" @click="replyReview(r)">回复评价</button>
                  <button class="text-xs text-slate-400 hover:text-red-500" @click="appealReview(r)">申诉</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </CoachLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import CoachLayout from "@/components/layout/CoachLayout.vue";
import LoadingState from "@/components/ui/LoadingState.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import {
  fetchCoachProfile,
  fetchEditableProfile,
  updateCoachProfile,
  fetchCoachReviews,
  type CoachProfile,
  type CoachReview,
} from "@/api/modules/profile";

const loading = ref(true);
const showEdit = ref(false);
const saving = ref(false);
const profile = ref<CoachProfile | null>(null);
const reviews = ref<CoachReview[]>([]);

const sceneOptions = [
  { value: "job", label: "职场求职" },
  { value: "mgmt", label: "管理汇报" },
  { value: "speech", label: "即兴演讲" },
  { value: "pressure", label: "压力训练" },
  { value: "communication", label: "职场沟通" },
  { value: "conflict", label: "冲突表达" },
];

const defaultTags = ["专业", "耐心", "高效", "实战", "逻辑清晰", "压力面"];

const editForm = ref({
  nickname: "",
  bio: "",
  scenes: [] as string[],
  tags: [] as string[],
});

const displayTags = computed(() => editForm.value.tags.length ? editForm.value.tags : (profile.value?.tags || []));

function sceneLabel(key: string) {
  return sceneOptions.find((s) => s.value === key)?.label || key;
}

async function handleSave() {
  saving.value = true;
  try {
    await updateCoachProfile({
      nickname: editForm.value.nickname,
      bio: editForm.value.bio,
      scenes: editForm.value.scenes,
      serviceTags: editForm.value.tags,
    });
    if (profile.value) {
      profile.value.nickname = editForm.value.nickname;
      profile.value.tags = editForm.value.tags;
    }
    ElMessage.success("资料已更新");
    showEdit.value = false;
  } catch {
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
}

function replyReview(r: CoachReview) {
  ElMessageBox.prompt("输入回复内容", `回复 ${r.userName || r.user}`, {
    confirmButtonText: "发送",
    cancelButtonText: "取消",
  }).then(() => ElMessage.success("回复已发送"));
}

function appealReview(r: CoachReview) {
  ElMessageBox.prompt("请说明申诉理由", "恶意评价申诉", {
    confirmButtonText: "提交申诉",
    cancelButtonText: "取消",
  }).then(() => ElMessage.success("申诉已提交，平台将尽快处理"));
}

onMounted(async () => {
  const [p, r] = await Promise.all([fetchCoachProfile(), fetchCoachReviews()]);
  profile.value = p;
  reviews.value = r;
  const editable = await fetchEditableProfile().catch(() => null);
  editForm.value.nickname = editable?.nickname || p?.nickname || p?.name || "";
  editForm.value.bio = (editable as { bio?: string })?.bio || "";
  editForm.value.scenes = (editable as { scenes?: string[] })?.scenes || [];
  editForm.value.tags = p?.tags || [];
  loading.value = false;
});
</script>
