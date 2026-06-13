<template>
  <AdminLayout title="题库管理">
    <div class="toolbar">
      <el-select v-model="sceneFilter" placeholder="筛选场景" clearable style="width: 200px" @change="loadBanks">
        <el-option v-for="s in scenes" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-button type="primary" @click="showBankDialog = true">新建题库</el-button>
    </div>

    <el-table v-loading="bankLoading" :data="banks" stripe @row-click="selectBank">
      <el-table-column prop="name" label="题库" />
      <el-table-column prop="sceneName" label="场景" width="120" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="questionCount" label="题目数" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "上架" : "下架" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" link @click.stop="selectBank(row)">管理题目</el-button>
          <el-button v-if="row.status === 1" size="small" link @click.stop="toggleBank(row, 0)">下架</el-button>
          <el-button v-else size="small" link type="primary" @click.stop="toggleBank(row, 1)">上架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="activeBank" class="questions-panel">
      <div class="panel-head">
        <h3>{{ activeBank.name }} · 题目列表</h3>
        <el-button size="small" type="primary" @click="showQuestionDialog = true">添加题目</el-button>
      </div>
      <el-table v-loading="questionLoading" :data="questions" stripe size="small">
        <el-table-column prop="title" label="题目" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="70" />
        <el-table-column prop="tags" label="标签" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">{{ row.status === 1 ? "上架" : "下架" }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" link type="danger" @click="toggleQuestion(row, 0)">下架</el-button>
            <el-button v-else size="small" link @click="toggleQuestion(row, 1)">上架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showBankDialog" title="新建题库" width="420px">
      <el-form label-width="80px">
        <el-form-item label="场景">
          <el-select v-model="bankForm.sceneId" style="width: 100%">
            <el-option v-for="s in scenes" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="bankForm.name" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="bankForm.category" placeholder="如 pressure" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBankDialog = false">取消</el-button>
        <el-button type="primary" @click="createBank">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showQuestionDialog" title="添加题目" width="480px">
      <el-form label-width="80px">
        <el-form-item label="题目"><el-input v-model="questionForm.title" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="难度"><el-input-number v-model="questionForm.difficulty" :min="1" :max="5" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="questionForm.tags" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQuestionDialog = false">取消</el-button>
        <el-button type="primary" @click="addQuestion">添加</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AdminLayout from "@admin/components/AdminLayout.vue";
import {
  createQuestion,
  createQuestionBank,
  fetchBankQuestions,
  fetchQuestionBanks,
  fetchScenes,
  updateQuestionBankStatus,
  updateQuestionStatus,
} from "@admin/api/admin";

const bankLoading = ref(false);
const questionLoading = ref(false);
const showBankDialog = ref(false);
const showQuestionDialog = ref(false);
const sceneFilter = ref<number | undefined>();
const scenes = ref<Record<string, unknown>[]>([]);
const banks = ref<Record<string, unknown>[]>([]);
const questions = ref<Record<string, unknown>[]>([]);
const activeBank = ref<Record<string, unknown> | null>(null);
const bankForm = reactive({ sceneId: undefined as number | undefined, name: "", category: "" });
const questionForm = reactive({ title: "", difficulty: 3, tags: "" });

async function loadScenes() {
  scenes.value = await fetchScenes();
}

async function loadBanks() {
  bankLoading.value = true;
  try {
    banks.value = await fetchQuestionBanks(sceneFilter.value);
  } finally {
    bankLoading.value = false;
  }
}

async function selectBank(row: Record<string, unknown>) {
  activeBank.value = row;
  questionLoading.value = true;
  try {
    questions.value = await fetchBankQuestions(row.id as number);
  } finally {
    questionLoading.value = false;
  }
}

async function createBank() {
  if (!bankForm.sceneId || !bankForm.name.trim()) {
    ElMessage.warning("请填写场景和名称");
    return;
  }
  await createQuestionBank({ ...bankForm });
  showBankDialog.value = false;
  ElMessage.success("已创建");
  await loadBanks();
}

async function toggleBank(row: Record<string, unknown>, status: number) {
  await updateQuestionBankStatus(row.id as number, status);
  ElMessage.success("已更新");
  await loadBanks();
}

async function addQuestion() {
  if (!activeBank.value || !questionForm.title.trim()) {
    ElMessage.warning("请选择题库并填写题目");
    return;
  }
  await createQuestion(activeBank.value.id as number, { ...questionForm });
  showQuestionDialog.value = false;
  questionForm.title = "";
  ElMessage.success("已添加");
  await selectBank(activeBank.value);
}

async function toggleQuestion(row: Record<string, unknown>, status: number) {
  await updateQuestionStatus(row.id as number, status);
  ElMessage.success("已更新");
  if (activeBank.value) await selectBank(activeBank.value);
}

onMounted(async () => {
  await loadScenes();
  await loadBanks();
});
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.questions-panel { margin-top: 24px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.panel-head h3 { margin: 0; font-size: 15px; }
</style>
