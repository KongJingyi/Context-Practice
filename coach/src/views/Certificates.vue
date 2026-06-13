<template>
  <CoachLayout page-title="资质认证" page-subtitle="提交学信网学籍与比赛证书，通过审核后即可正式接单">
    <LoadingState v-if="loading" />

    <template v-else>
      <div class="grid lg:grid-cols-2 gap-6 mb-8">
        <!-- 学信网认证 -->
        <div class="coach-card p-6">
          <div class="flex items-start justify-between mb-5">
            <div>
              <h2 class="font-semibold text-slate-900">学信网学籍认证</h2>
              <p class="text-sm text-slate-400 mt-1">上传学信网截图并提供在线验证码</p>
            </div>
            <span v-if="xuexinCert" :class="['coach-badge', certStatusClass(xuexinCert.status)]">
              {{ certStatusLabel(xuexinCert.status) }}
            </span>
          </div>

          <div class="space-y-4">
            <div>
              <label class="text-xs font-medium text-slate-500 mb-1.5 block">在线验证码</label>
              <el-input v-model="xuexinForm.verifyCode" placeholder="12 位学信网验证码" maxlength="12" />
            </div>
            <div>
              <label class="text-xs font-medium text-slate-500 mb-1.5 block">学籍截图</label>
              <el-upload
                drag
                :auto-upload="false"
                :limit="1"
                accept="image/*"
                :on-change="(f: any) => xuexinForm.file = f.raw"
              >
                <div class="py-4">
                  <p class="text-sm text-slate-600">拖拽或点击上传截图</p>
                  <p class="text-xs text-slate-400 mt-1">支持 JPG / PNG，最大 5MB</p>
                </div>
              </el-upload>
            </div>
            <button
              class="w-full py-2.5 bg-teal-600 text-white rounded-xl text-sm font-semibold hover:bg-teal-700 transition-colors disabled:opacity-50"
              :disabled="submittingXuexin"
              @click="submitXuexin"
            >
              {{ submittingXuexin ? "提交中…" : "提交学籍认证" }}
            </button>
          </div>
        </div>

        <!-- 比赛证书 -->
        <div class="coach-card p-6">
          <div class="flex items-start justify-between mb-5">
            <div>
              <h2 class="font-semibold text-slate-900">比赛获奖证书</h2>
              <p class="text-sm text-slate-400 mt-1">上传相关比赛获奖证书以审核资质</p>
            </div>
          </div>

          <div class="space-y-4">
            <div>
              <label class="text-xs font-medium text-slate-500 mb-1.5 block">证书名称</label>
              <el-input v-model="awardForm.title" placeholder="如：全国大学生辩论赛一等奖" />
            </div>
            <div>
              <label class="text-xs font-medium text-slate-500 mb-1.5 block">证书图片</label>
              <el-upload
                drag
                :auto-upload="false"
                :limit="1"
                accept="image/*"
                :on-change="(f: any) => awardForm.file = f.raw"
              >
                <div class="py-4">
                  <p class="text-sm text-slate-600">拖拽或点击上传证书</p>
                </div>
              </el-upload>
            </div>
            <button
              class="w-full py-2.5 border border-teal-600 text-teal-700 rounded-xl text-sm font-semibold hover:bg-teal-50 transition-colors disabled:opacity-50"
              :disabled="submittingAward"
              @click="submitAward"
            >
              {{ submittingAward ? "提交中…" : "提交比赛证书" }}
            </button>
          </div>
        </div>
      </div>

      <!-- 已提交记录 -->
      <div class="coach-card overflow-hidden" v-if="records.length">
        <div class="px-6 py-4 border-b border-slate-100">
          <h2 class="font-semibold text-slate-900">提交记录</h2>
        </div>
        <div class="divide-y divide-slate-50">
          <div v-for="r in records" :key="r.id" class="px-6 py-4 flex items-center justify-between">
            <div>
              <p class="font-medium text-slate-900">{{ r.title }}</p>
              <p class="text-xs text-slate-400 mt-0.5">{{ r.type === "XUEXIN" ? "学信网认证" : "比赛证书" }} · {{ r.submittedAt || "刚刚" }}</p>
              <p v-if="r.rejectReason" class="text-xs text-red-500 mt-1">驳回原因：{{ r.rejectReason }}</p>
            </div>
            <span :class="['coach-badge', certStatusClass(r.status)]">{{ certStatusLabel(r.status) }}</span>
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
import {
  fetchCertificates,
  submitCertificate,
  certStatusLabel,
  certStatusClass,
  type CertificateRecord,
} from "@/api/modules/certificates";

const loading = ref(true);
const records = ref<CertificateRecord[]>([]);
const submittingXuexin = ref(false);
const submittingAward = ref(false);

const xuexinForm = ref<{ verifyCode: string; file: File | null }>({ verifyCode: "", file: null });
const awardForm = ref<{ title: string; file: File | null }>({ title: "", file: null });

const xuexinCert = computed(() => records.value.find((r) => r.type === "XUEXIN"));

async function submitXuexin() {
  if (!xuexinForm.value.verifyCode.trim()) {
    ElMessage.warning("请填写学信网验证码");
    return;
  }
  submittingXuexin.value = true;
  try {
    const fd = new FormData();
    fd.append("type", "XUEXIN");
    fd.append("verifyCode", xuexinForm.value.verifyCode);
    if (xuexinForm.value.file) fd.append("file", xuexinForm.value.file);
    await submitCertificate(fd);
    ElMessage.success("学籍认证已提交，等待审核");
    records.value = await fetchCertificates();
  } catch {
    ElMessage.error("提交失败");
  } finally {
    submittingXuexin.value = false;
  }
}

async function submitAward() {
  if (!awardForm.value.title.trim()) {
    ElMessage.warning("请填写证书名称");
    return;
  }
  submittingAward.value = true;
  try {
    const fd = new FormData();
    fd.append("type", "AWARD");
    fd.append("title", awardForm.value.title);
    if (awardForm.value.file) fd.append("file", awardForm.value.file);
    await submitCertificate(fd);
    ElMessage.success("比赛证书已提交");
    records.value = await fetchCertificates();
  } catch {
    ElMessage.error("提交失败");
  } finally {
    submittingAward.value = false;
  }
}

onMounted(async () => {
  records.value = await fetchCertificates();
  loading.value = false;
});
</script>
