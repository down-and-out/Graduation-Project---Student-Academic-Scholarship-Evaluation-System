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
            奖学金金额：￥{{ result.awardAmount ?? 0 }}
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
        <el-button @click="handleViewDetail(result)">
          查看详情
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
            <el-tag :type="getAwardTagType(row.awardLevel || 0)">
              {{ getAwardTitle(row.awardLevel || 0) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合得分" width="100" />
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="publishDate" label="公示日期" width="180" />
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

    <el-dialog v-model="detailDialogVisible" title="评定结果详情" width="760px">
      <template v-if="detailResult">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评定批次">{{ detailResult.batchName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detailResult.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailResult.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ detailResult.department || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ detailResult.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="获奖等级">
            <el-tag :type="getAwardTagType(detailResult.awardLevel || 0)">
              {{ getAwardTitle(detailResult.awardLevel || 0) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="综合得分">{{ detailResult.totalScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="排名">{{ detailResult.rank ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailResult.status ?? 0)">
              {{ getStatusText(detailResult.status ?? 0) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailResult.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-table :data="buildScoreDetails(detailResult)" border>
          <el-table-column prop="item" label="评分项" width="180" />
          <el-table-column prop="score" label="得分" width="120" />
        </el-table>
      </template>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
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
import { getMyResult, getResultDetail, getResultPage } from '@/api/result'
import type { EvaluationResult } from '@/api/result'

type TagType = 'info' | 'warning' | 'success' | 'danger'

interface StudentResultView extends EvaluationResult {
  rank?: number
  status?: number
  batchName: string
  publishDate?: string
}

interface ScoreDetailRow {
  item: string
  score: number | string
}

const result = ref<StudentResultView | null>(null)
const historyList = ref<StudentResultView[]>([])
const detailResult = ref<StudentResultView | null>(null)
const appealDialogVisible = ref(false)
const detailDialogVisible = ref(false)
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
    1: '特等奖学金',
    2: '一等奖学金',
    3: '二等奖学金',
    4: '三等奖学金',
    5: '未获奖'
  }
  return titles[level] || '未评定'
}

function getAwardClass(level: number): string {
  const classes: Record<number, string> = {
    0: 'award-none',
    1: 'award-first',
    2: 'award-first',
    3: 'award-second',
    4: 'award-third',
    5: 'award-none'
  }
  return classes[level] || 'award-none'
}

function getAwardTagType(level: number): TagType {
  if (level === 1 || level === 2) return 'danger'
  if (level === 3) return 'warning'
  if (level === 4) return 'success'
  return 'info'
}

function getStatusText(status: number): string {
  const texts: Record<number, string> = {
    0: '待公示',
    1: '公示中',
    2: '已确认',
    3: '有异议'
  }
  return texts[status] || '未知'
}

function getStatusType(status: number): TagType {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  return 'info'
}

function normalizeResult(payload: EvaluationResult): StudentResultView {
  return {
    ...payload,
    batchName: payload.batchName || `批次${payload.batchId}`,
    rank: payload.rank ?? payload.departmentRank ?? payload.majorRank,
    status: payload.status ?? payload.resultStatus ?? 0,
    publishDate: payload.publishDate || payload.publicityDate
  }
}

function buildScoreDetails(current: StudentResultView): ScoreDetailRow[] {
  return [
    { item: '课程成绩', score: current.courseScore ?? 0 },
    { item: '科研成果', score: current.researchScore ?? 0 },
    { item: '竞赛获奖', score: current.competitionScore ?? 0 },
    { item: '综合素质', score: current.qualityScore ?? 0 }
  ]
}

function downloadTextFile(content: string, fileName: string): void {
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
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
      appealReason: appealForm.reason,
      appealContent: appealForm.content
    })
    ElMessage.success('异议已提交，请等待处理结果')
    appealDialogVisible.value = false
  } catch (error) {
    console.error('提交异议失败:', error)
    ElMessage.error('提交异议失败')
  }
}

function handleExport(): void {
  if (!result.value) return

  const content = [
    '研究生学业奖学金评定结果证明',
    '',
    `评定批次：${result.value.batchName || '-'}`,
    `姓名：${result.value.studentName || '-'}`,
    `学号：${result.value.studentNo || '-'}`,
    `院系：${result.value.department || '-'}`,
    `专业：${result.value.major || '-'}`,
    `获奖等级：${getAwardTitle(result.value.awardLevel || 0)}`,
    `综合得分：${result.value.totalScore ?? '-'}`,
    `排名：${result.value.rank ?? '-'}`,
    `状态：${getStatusText(result.value.status ?? 0)}`,
    '',
    '说明：本文件由系统根据当前评定结果导出，仅供个人留存与核对。'
  ].join('\r\n')

  const safeBatchName = (result.value.batchName || 'result').replace(/[\\/:*?"<>|]/g, '_')
  downloadTextFile(content, `${safeBatchName}_评定结果证明.txt`)
}

async function handleViewDetail(row: StudentResultView): Promise<void> {
  if (!row.id) return

  try {
    const response = await getResultDetail(row.id)
    const raw = extractNestedData<EvaluationResult>(response)
    if (!raw) return
    detailResult.value = normalizeResult(raw)
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取结果详情失败:', error)
    ElMessage.error('获取结果详情失败')
  }
}

onMounted(() => {
  loadResult()
  loadHistory()
})
</script>

<style scoped>
.result-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header .page-title {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 500;
}

.result-card,
.empty-card,
.history-card {
  margin-bottom: 20px;
}

.award-status {
  display: flex;
  align-items: center;
  gap: 20px;
}

.award-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
}

.award-first {
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
}

.award-second {
  background: linear-gradient(135deg, #e6a23c, #f3d19e);
}

.award-third {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.award-none {
  background: #c0c4cc;
}

.award-title {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.award-amount {
  margin: 0;
  font-size: 18px;
  color: #e6a23c;
  font-weight: 500;
}

.result-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
</style>
