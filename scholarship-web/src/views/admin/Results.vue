<template>
  <div class="results-page">
    <div class="page-header">
      <h2 class="page-title">结果管理</h2>
      <el-button type="success" :loading="exporting" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出结果
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="评定批次">
        <el-select v-model="queryParams.batchId" placeholder="请选择" clearable>
          <el-option
            v-for="item in batchOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyword" placeholder="学号/姓名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="公示中" :value="1" />
          <el-option label="已确认" :value="2" />
          <el-option label="有异议" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="获奖总人数" :value="stats.total" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="一等奖学金" :value="stats.firstLevel">
            <template #suffix>人</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="二等奖学金" :value="stats.secondLevel">
            <template #suffix>人</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="三等奖学金" :value="stats.thirdLevel">
            <template #suffix>人</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="batchName" label="评定批次" min-width="180" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="studentName" label="姓名" width="110" />
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column prop="major" label="专业" width="150" />
      <el-table-column label="奖项等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getLevelConfig(row.awardLevel).type">
            {{ getLevelConfig(row.awardLevel).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalScore" label="综合得分" width="100" />
      <el-table-column label="排名" width="90">
        <template #default="{ row }">
          {{ getDisplayRank(row) }}
        </template>
      </el-table-column>
      <el-table-column label="结果状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusConfig(row.resultStatus).type">
            {{ getStatusConfig(row.resultStatus).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
          <el-button link type="primary" @click="handleAdjust(row)">调整</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handleQuery"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="detailDialogVisible" title="评定结果详情" width="820px">
      <template v-if="detailData">
        <el-descriptions :column="2" border class="result-detail">
          <el-descriptions-item label="评定批次">{{ detailData.batchName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detailData.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailData.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ detailData.department || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ detailData.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="奖项等级">
            <el-tag :type="getLevelConfig(detailData.awardLevel).type">
              {{ getLevelConfig(detailData.awardLevel).text }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="综合得分">{{ detailData.totalScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="排名">{{ getDisplayRank(detailData) }}</el-descriptions-item>
          <el-descriptions-item label="结果状态">
            <el-tag :type="getStatusConfig(detailData.resultStatus).type">
              {{ getStatusConfig(detailData.resultStatus).text }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调整备注">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h4>得分明细</h4>
        <el-table :data="buildScoreDetails(detailData)" border size="small">
          <el-table-column prop="item" label="项目" width="160" />
          <el-table-column prop="score" label="得分" width="120" />
        </el-table>
      </template>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustDialogVisible" title="调整奖项等级" width="500px">
      <el-form
        v-if="currentRow"
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
      >
        <el-form-item label="学生姓名">
          <span>{{ currentRow.studentName }}</span>
        </el-form-item>
        <el-form-item label="原等级">
          <el-tag :type="getLevelConfig(currentRow.awardLevel).type">
            {{ getLevelConfig(currentRow.awardLevel).text }}
          </el-tag>
        </el-form-item>
        <el-form-item label="调整后" prop="awardLevel">
          <el-select v-model="adjustForm.awardLevel">
            <el-option label="特等奖学金" :value="1" />
            <el-option label="一等奖学金" :value="2" />
            <el-option label="二等奖学金" :value="3" />
            <el-option label="三等奖学金" :value="4" />
            <el-option label="未获奖" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustSubmitting" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getEvaluationPage, type EvaluationBatch } from '@/api/evaluation'
import {
  adjustResult,
  exportResult,
  getResultDetail,
  getResultPage,
  type EvaluationResult
} from '@/api/result'

type TagType = 'success' | 'warning' | 'danger' | 'info' | 'primary'

interface BatchOption {
  label: string
  value: number
}

interface ScoreDetailRow {
  item: string
  score: number | string
}

interface QueryParams {
  current: number
  size: number
  batchId: number | null
  keyword: string
  status: number | null
}

interface AdjustFormState {
  awardLevel: number | null
  reason: string
}

const LEVEL_CONFIG: Record<number, { text: string; type: TagType }> = {
  1: { text: '特等奖学金', type: 'danger' },
  2: { text: '一等奖学金', type: 'danger' },
  3: { text: '二等奖学金', type: 'warning' },
  4: { text: '三等奖学金', type: 'success' },
  5: { text: '未获奖', type: 'info' }
}

const STATUS_CONFIG: Record<number, { text: string; type: TagType }> = {
  1: { text: '公示中', type: 'warning' },
  2: { text: '已确认', type: 'success' },
  3: { text: '有异议', type: 'danger' }
}

const loading = ref(false)
const exporting = ref(false)
const adjustSubmitting = ref(false)
const tableData = ref<EvaluationResult[]>([])
const total = ref(0)
const batchOptions = ref<BatchOption[]>([])
const detailDialogVisible = ref(false)
const adjustDialogVisible = ref(false)
const currentRow = ref<EvaluationResult | null>(null)
const detailData = ref<EvaluationResult | null>(null)
const adjustFormRef = ref<FormInstance | null>(null)

const stats = reactive({
  total: 0,
  firstLevel: 0,
  secondLevel: 0,
  thirdLevel: 0
})

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10,
  batchId: null,
  keyword: '',
  status: null
})

const adjustForm = reactive<AdjustFormState>({
  awardLevel: null,
  reason: ''
})

const adjustRules: FormRules<AdjustFormState> = {
  awardLevel: [{ required: true, message: '请选择调整后的等级', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

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

function getLevelConfig(level?: number): { text: string; type: TagType } {
  return LEVEL_CONFIG[level || 5] || LEVEL_CONFIG[5]
}

function getStatusConfig(status?: number): { text: string; type: TagType } {
  return STATUS_CONFIG[status || 1] || { text: '未知', type: 'info' }
}

function getDisplayRank(row: EvaluationResult): number | string {
  return row.departmentRank ?? row.majorRank ?? '-'
}

function updateStats(records: EvaluationResult[]): void {
  const awarded = records.filter(item => item.awardLevel && item.awardLevel >= 1 && item.awardLevel <= 4)
  stats.total = awarded.length
  stats.firstLevel = records.filter(item => item.awardLevel === 2).length
  stats.secondLevel = records.filter(item => item.awardLevel === 3).length
  stats.thirdLevel = records.filter(item => item.awardLevel === 4).length
}

function buildScoreDetails(row: EvaluationResult): ScoreDetailRow[] {
  return [
    { item: '课程成绩', score: row.courseScore ?? 0 },
    { item: '科研成果', score: row.researchScore ?? 0 },
    { item: '竞赛获奖', score: row.competitionScore ?? 0 },
    { item: '综合素质', score: row.qualityScore ?? 0 }
  ]
}

function downloadBlob(blob: Blob, fileName: string): void {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

async function loadBatchOptions(): Promise<void> {
  try {
    const response = await getEvaluationPage({ current: 1, size: 1000 })
    const pageData = extractNestedData<API.PageResponse<EvaluationBatch>>(response)
    batchOptions.value = (pageData?.records || [])
      .filter(item => item.id)
      .map(item => ({
        label: item.name,
        value: item.id as number
      }))
  } catch (error) {
    console.error('加载评定批次失败:', error)
    batchOptions.value = []
  }
}

async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const response = await getResultPage({
      current: queryParams.current,
      size: queryParams.size,
      batchId: queryParams.batchId ?? undefined,
      status: queryParams.status ?? undefined,
      keyword: queryParams.keyword || undefined
    })
    const pageData = extractNestedData<API.PageResponse<EvaluationResult>>(response)
    const records = pageData?.records || []
    tableData.value = records
    total.value = pageData?.total || 0
    updateStats(records)
  } catch (error) {
    console.error('查询失败:', error)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleReset(): void {
  queryParams.batchId = null
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.current = 1
  handleQuery()
}

function handleSizeChange(): void {
  queryParams.current = 1
  handleQuery()
}

async function handleView(row: EvaluationResult): Promise<void> {
  if (!row.id) return
  try {
    const response = await getResultDetail(row.id)
    const detail = extractNestedData<EvaluationResult>(response)
    if (!detail) return
    detailData.value = detail
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

function handleAdjust(row: EvaluationResult): void {
  currentRow.value = row
  adjustForm.awardLevel = row.awardLevel ?? null
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

async function handleAdjustSubmit(): Promise<void> {
  const valid = await adjustFormRef.value?.validate().catch(() => false)
  if (!valid || !currentRow.value?.id || adjustForm.awardLevel === null) return

  try {
    await ElMessageBox.confirm(
      `确定要将 ${currentRow.value.studentName || '该学生'} 的奖项等级调整为 ${getLevelConfig(adjustForm.awardLevel).text} 吗？`,
      '调整确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    adjustSubmitting.value = true
    await adjustResult(currentRow.value.id, {
      awardLevel: adjustForm.awardLevel,
      reason: adjustForm.reason.trim()
    })
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('调整失败:', error)
      ElMessage.error('调整失败，请稍后重试')
    }
  } finally {
    adjustSubmitting.value = false
  }
}

async function handleExport(): Promise<void> {
  try {
    exporting.value = true
    const blob = await exportResult(queryParams.batchId ?? undefined)
    const suffix = queryParams.batchId ? `_batch_${queryParams.batchId}` : ''
    downloadBlob(blob, `evaluation_results${suffix}.xlsx`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadBatchOptions(), handleQuery()])
})
</script>

<style scoped lang="scss">
.results-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
    color: #303133;
  }
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stats-row {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.result-detail {
  margin-bottom: 20px;
}

h4 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
</style>
