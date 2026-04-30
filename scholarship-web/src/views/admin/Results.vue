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
      <el-form-item label="学年">
        <el-select v-model="queryParams.academicYear" placeholder="请选择" clearable>
          <el-option
            v-for="item in academicYearOptions"
            :key="item"
            :label="formatAcademicYearLabel(item)"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择" clearable>
          <el-option
            v-for="item in semesterOptions"
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
          <el-option
            v-for="option in resultStatusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
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
          <el-tag :type="getStatusConfig(getResultStatusValue(row)).type">
            {{ getStatusConfig(getResultStatusValue(row)).text }}
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
            <el-tag :type="getStatusConfig(getResultStatusValue(detailData)).type">
              {{ getStatusConfig(getResultStatusValue(detailData)).text }}
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
import { getEvaluationAcademicYears } from '@/api/evaluation'
import {
  adjustResult,
  exportResult,
  getResultDetail,
  getResultPage,
  type EvaluationResult
} from '@/api/result'
import { extractApiData, formatAcademicYearLabel } from '@/utils/helpers'
import {
  getAwardLevelConfig,
  getResultStatusConfig,
  normalizeResultStatus,
  RESULT_STATUS_OPTIONS,
  type ResultTagType
} from '@/constants/evaluationResult'

defineOptions({ name: 'AdminResults' })

interface SemesterOption {
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
  academicYear: string
  semester: number | null
  keyword: string
  status: number | null
}

interface AdjustFormState {
  awardLevel: number | null
  reason: string
}

const loading = ref(false)
const exporting = ref(false)
const adjustSubmitting = ref(false)
const tableData = ref<EvaluationResult[]>([])
const total = ref(0)
const academicYearOptions = ref<string[]>([])
const detailDialogVisible = ref(false)
const adjustDialogVisible = ref(false)
const currentRow = ref<EvaluationResult | null>(null)
const detailData = ref<EvaluationResult | null>(null)
const adjustFormRef = ref<FormInstance | null>(null)
const resultStatusOptions = RESULT_STATUS_OPTIONS.filter(option => option.value !== 0)
const semesterOptions: SemesterOption[] = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 }
]

const stats = reactive({
  total: 0,
  firstLevel: 0,
  secondLevel: 0,
  thirdLevel: 0
})

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10,
  academicYear: '',
  semester: null,
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

function getLevelConfig(level?: number): { text: string; type: ResultTagType } {
  return getAwardLevelConfig(level)
}

function getStatusConfig(status?: number): { text: string; type: ResultTagType } {
  return getResultStatusConfig(status)
}

function getResultStatusValue(row: EvaluationResult): number {
  return normalizeResultStatus(row.resultStatus, row.status)
}

function getDisplayRank(row: EvaluationResult): number | string {
  if (row.departmentRank != null) return row.departmentRank
  if (row.majorRank != null) return row.majorRank
  return '-'
}

function updateStats(records: EvaluationResult[]): void {
  const nextStats = records.reduce(
    (acc, item) => {
      if (item.awardLevel && item.awardLevel >= 1 && item.awardLevel <= 4) {
        acc.total += 1
      }
      if (item.awardLevel === 2) acc.firstLevel += 1
      if (item.awardLevel === 3) acc.secondLevel += 1
      if (item.awardLevel === 4) acc.thirdLevel += 1
      return acc
    },
    { total: 0, firstLevel: 0, secondLevel: 0, thirdLevel: 0 }
  )

  Object.assign(stats, nextStats)
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

async function loadAcademicYearOptions(): Promise<void> {
  try {
    const response = await getEvaluationAcademicYears()
    const years = extractApiData<string[]>(response) || []
    academicYearOptions.value = [...years].sort((left, right) => right.localeCompare(left))
  } catch (error) {
    console.error('加载评定批次失败:', error)
    academicYearOptions.value = []
  }
}

async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const response = await getResultPage({
      current: queryParams.current,
      size: queryParams.size,
      academicYear: queryParams.academicYear || undefined,
      semester: queryParams.semester ?? undefined,
      status: queryParams.status ?? undefined,
      keyword: queryParams.keyword || undefined
    })
    const pageData = extractApiData<API.PageResponse<EvaluationResult>>(response)
    const records = pageData?.records || []
    tableData.value = records
    total.value = pageData?.total || 0
    updateStats(records)
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleReset(): void {
  queryParams.academicYear = ''
  queryParams.semester = null
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
    const detail = extractApiData<EvaluationResult>(response)
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
    const blob = await exportResult({
      academicYear: queryParams.academicYear || undefined,
      semester: queryParams.semester ?? undefined
    })
    const suffixParts = [
      queryParams.academicYear ? queryParams.academicYear : '',
      queryParams.semester ? `semester_${queryParams.semester}` : ''
    ].filter(Boolean)
    const suffix = suffixParts.length > 0 ? `_${suffixParts.join('_')}` : ''
    downloadBlob(blob, `evaluation_results${suffix}.xlsx`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadAcademicYearOptions(), handleQuery()])
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
