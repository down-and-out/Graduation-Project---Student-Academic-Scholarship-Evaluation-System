<template>
  <div class="result-page">
    <div class="page-header">
      <h2 class="page-title">评定结果查询</h2>
    </div>

    <el-skeleton v-if="loading" :rows="5" animated />

    <el-card v-else-if="result" class="result-card">
      <div class="award-status">
        <div class="award-icon" :class="getAwardClass(result.awardLevel || 0)">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="award-info">
          <h3 class="award-title">{{ getAwardTitle(result.awardLevel || 0) }}</h3>
          <p v-if="showAmount" class="award-amount">
            奖学金金额：￥{{ result.scholarshipAmount ?? result.totalScore }}
          </p>
        </div>
      </div>

      <el-divider />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="评定批次">{{ result.batchName }}</el-descriptions-item>
        <el-descriptions-item label="综合得分">{{ result.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="排名">{{ result.rank ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(result.status ?? 0)">{{ getStatusText(result.status ?? 0) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="result.status === 1" class="result-actions">
        <el-button type="primary" @click="handleAppeal">
          <el-icon><DocumentAdd /></el-icon>
          提出异议
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出证书
        </el-button>
      </div>
    </el-card>

    <el-card v-else class="empty-card">
      <el-empty description="暂无评定结果">
        <el-button type="primary" @click="$router.push('/app/student/application')">去申请奖学金</el-button>
      </el-empty>
    </el-card>

    <el-card v-if="historyList.length > 0" class="history-card">
      <template #header>
        <span>历史评定记录</span>
      </template>
      <el-table :data="historyList" border stripe>
        <el-table-column prop="batchName" label="评定批次" width="200" />
        <el-table-column prop="awardLevel" label="获奖等级" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.awardLevel === 1" type="danger">一等奖</el-tag>
            <el-tag v-else-if="row.awardLevel === 2" type="warning">二等奖</el-tag>
            <el-tag v-else-if="row.awardLevel === 3" type="success">三等奖</el-tag>
            <el-tag v-else type="info">未获奖</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合得分" width="100" />
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="publishDate" label="公示日期" width="120" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="appealDialogVisible" title="提出异议" width="600px">
      <el-form ref="appealFormRef" :model="appealForm" :rules="appealRules" label-width="100px">
        <el-form-item label="异议理由" prop="reason">
          <el-input v-model="appealForm.reason" placeholder="请输入异议理由" />
        </el-form-item>
        <el-form-item label="详细说明" prop="content">
          <el-input
            v-model="appealForm.content"
            type="textarea"
            :rows="5"
            placeholder="请详细说明您的异议内容和相关证据"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAppeal">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { DocumentAdd, Download, Medal } from '@element-plus/icons-vue'
import { submitAppeal } from '@/api/appeal'
import { getMyResult, getResultPage } from '@/api/result'
import type { EvaluationResult } from '@/api/result'

type TagType = 'info' | 'warning' | 'success'

interface StudentResultView extends EvaluationResult {
  scholarshipAmount?: number
  rank?: number
  status?: number
  batchName: string
  publishDate?: string
}

const result = ref<StudentResultView | null>(null)
const historyList = ref<StudentResultView[]>([])
const appealDialogVisible = ref(false)
const appealFormRef = ref<FormInstance | null>(null)
const loading = ref(false)

const appealForm = reactive({
  reason: '',
  content: ''
})

const appealRules: FormRules = {
  reason: [{ required: true, message: '请输入异议理由', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细说明', trigger: 'blur' }]
}

const showAmount = computed(() => {
  const level = result.value?.awardLevel ?? 0
  return level > 0 && level <= 3
})

function extractNestedData<T>(payload: unknown): T | null {
  if (!payload || typeof payload !== 'object') return null
  const raw = payload as Record<string, unknown>
  if (raw.data && typeof raw.data === 'object') {
    const inner = raw.data as Record<string, unknown>
    if ('data' in inner) {
      return inner.data as T
    }
    return raw.data as T
  }
  return raw as T
}

function getAwardTitle(level: number): string {
  const titles: Record<number, string> = {
    0: '未获奖',
    1: '一等奖学金',
    2: '二等奖学金',
    3: '三等奖学金'
  }
  return titles[level] || '未评定'
}

function getAwardClass(level: number): string {
  const classes: Record<number, string> = {
    0: 'award-none',
    1: 'award-first',
    2: 'award-second',
    3: 'award-third'
  }
  return classes[level] || 'award-none'
}

function getStatusText(status: number): string {
  const texts: Record<number, string> = {
    0: '待公示',
    1: '公示中',
    2: '公示完成',
    3: '已完成'
  }
  return texts[status] || '未知'
}

function getStatusType(status: number): TagType {
  if (status === 1) return 'warning'
  if (status === 2 || status === 3) return 'success'
  return 'info'
}

function normalizeResult(payload: EvaluationResult): StudentResultView {
  return {
    ...payload,
    batchName: payload.batchName || `批次${payload.batchId}`,
    scholarshipAmount: payload.scholarshipAmount ?? payload.awardAmount,
    rank: payload.rank ?? payload.departmentRank ?? payload.majorRank,
    status: payload.status ?? payload.resultStatus ?? 0,
    publishDate: payload.publishDate || payload.publicityDate
  }
}

async function loadResult(): Promise<void> {
  try {
    const response = await getMyResult()
    const raw = extractNestedData<EvaluationResult>(response)
    result.value = raw ? normalizeResult(raw) : null
  } catch (error) {
    console.error('加载评定结果失败:', error)
  }
}

async function loadHistory(): Promise<void> {
  loading.value = true
  try {
    const response = await getResultPage({ current: 1, size: 100 })
    const pageData = extractNestedData<API.PageResponse<EvaluationResult>>(response)
    historyList.value = (pageData?.records || []).map(normalizeResult)
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAppeal(): void {
  appealDialogVisible.value = true
}

async function handleSubmitAppeal(): Promise<void> {
  if (!appealFormRef.value) return
  const valid = await appealFormRef.value.validate().catch(() => false)
  if (!valid || !result.value?.id) return

  try {
    await submitAppeal({
      resultId: result.value.id,
      studentId: result.value.studentId,
      reason: appealForm.reason,
      evidence: appealForm.content
    })
    ElMessage.success('异议已提交，请等待处理结果')
    appealDialogVisible.value = false
  } catch (error) {
    console.error('提交异议失败:', error)
    ElMessage.error('提交异议失败')
  }
}

function handleExport(): void {
  ElMessage.info('导出功能开发中')
}

function handleViewDetail(_row: StudentResultView): void {
  ElMessage.info('查看详情功能开发中')
}

onMounted(() => {
  loadResult()
  loadHistory()
})
</script>
