<template>
  <div class="evaluation-page">
    <div class="page-header">
      <h2 class="page-title">评定管理</h2>
      <el-button type="primary" @click="handleStart">
        <el-icon><Plus /></el-icon>
        发起评定
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="学年">
        <el-select
          v-model="queryParams.academicYears"
          placeholder="请选择"
          multiple
          collapse-tags
          collapse-tags-tooltip
          clearable
        >
          <el-option
            v-for="option in academicYearOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学期">
        <el-select
          v-model="queryParams.semesters"
          placeholder="请选择"
          multiple
          collapse-tags
          collapse-tags-tooltip
          clearable
        >
          <el-option
            v-for="option in SEMESTER_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="queryParams.statuses"
          placeholder="请选择"
          multiple
          collapse-tags
          collapse-tags-tooltip
          clearable
        >
          <el-option
            v-for="option in STATUS_OPTIONS"
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

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column label="学年学期" width="180">
        <template #default="{ row }">
          {{ formatAcademicYearSemester(row.academicYear, row.semester) }}
        </template>
      </el-table-column>
      <el-table-column prop="name" label="评定名称" min-width="220" />
      <el-table-column prop="startDate" label="申请开始" width="120" />
      <el-table-column prop="endDate" label="申请结束" width="120" />
      <el-table-column prop="winnerCount" label="获奖人数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button
            v-if="row.status === BATCH_STATUS.NOT_STARTED"
            link
            type="primary"
            @click="handleStartApplication(row)"
          >
            开始申请
          </el-button>
          <el-button
            v-else-if="row.status === BATCH_STATUS.APPLYING"
            link
            type="warning"
            @click="handleStartReview(row)"
          >
            开始评审
          </el-button>
          <template v-else-if="row.status === BATCH_STATUS.REVIEWING">
            <el-button
              link
              type="danger"
              :loading="isEvaluating(row.id)"
              @click="handleEvaluate(row)"
            >
              {{ getEvaluateButtonText(row.id) }}
            </el-button>
            <el-button
              link
              type="warning"
              :disabled="isEvaluating(row.id)"
              @click="handleStartPublicity(row)"
            >
              开始公示
            </el-button>
          </template>
          <el-button
            v-else-if="row.status === BATCH_STATUS.PUBLICITY"
            link
            type="success"
            @click="handleComplete(row)"
          >
            完成评定
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
      @size-change="handleQuery"
    />

    <el-dialog v-model="dialogVisible" title="发起评定" width="900px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="评定名称" prop="name">
              <el-input v-model="formData.name" placeholder="如：2024-2025学年第一学期奖学金评定" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="学年" prop="academicYear">
              <el-select v-model="formData.academicYear" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="option in formAcademicYearOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="formData.semester" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="option in SEMESTER_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="申请开始" prop="startDate">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请结束" prop="endDate">
              <el-date-picker
                v-model="formData.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="奖项配置">
          <div class="config-block">
            <el-table :data="formData.awardConfigs" border size="small">
              <el-table-column label="奖项等级" width="140">
                <template #default="{ row }">
                  {{ getAwardLevelName(row.awardLevel) }}
                </template>
              </el-table-column>
              <el-table-column label="比例(%)" width="180">
                <template #default="{ row }">
                  <el-input-number v-model="row.ratio" :min="0" :precision="2" :step="1" />
                </template>
              </el-table-column>
              <el-table-column label="金额(元)" width="220">
                <template #default="{ row }">
                  <el-input-number v-model="row.amount" :min="0" :precision="2" :step="100" />
                </template>
              </el-table-column>
            </el-table>
            <div class="config-tip" :class="{ invalid: totalAwardRatio > 100 }">
              当前比例总和：{{ totalAwardRatio }}%
            </div>
          </div>
        </el-form-item>

        <el-form-item label="参与评定规则集">
          <div class="config-block">
            <el-row :gutter="16">
              <el-col v-for="group in ruleGroups" :key="group.type" :span="12">
                <div class="rule-group">
                  <div class="rule-group-title">{{ group.label }}</div>
                  <el-select
                    v-model="formData.selectedRuleIdsByType[group.type]"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    clearable
                    placeholder="请选择规则"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="rule in group.rules"
                      :key="rule.id"
                      :label="buildRuleLabel(rule)"
                      :value="rule.id!"
                    />
                  </el-select>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  completeEvaluation,
  createEvaluation,
  deleteEvaluation,
  getEvaluationAcademicYears,
  getEvaluationDetail,
  getEvaluationPage,
  startEvaluationApplication,
  startEvaluationPublicity,
  startEvaluationReview,
  type BatchAwardConfig as ApiBatchAwardConfig,
  type EvaluationBatch as ApiEvaluationBatch
} from '@/api/evaluation'
import {
  evaluateBatch,
  getEvaluationTask,
  getLatestEvaluationTask,
  type EvaluationTaskResponse
} from '@/api/result'
import { getRuleById, getRulePage, type ScoreRule } from '@/api/rule'
import { RULE_TYPE_LABELS } from '@/constants/rule'
import { EVALUATION_TASK_STATUS } from '@/constants/review'
import { getAwardLevelConfig } from '@/constants/evaluationResult'
import { batchExecute, extractApiData, formatAcademicYearLabel, isRequestCanceled } from '@/utils/helpers'
import { LARGE_QUERY_SIZE } from '@/constants'

defineOptions({ name: 'AdminEvaluation' })

type BatchStatus = 1 | 2 | 3 | 4 | 5
type TagType = 'success' | 'warning' | 'info' | 'primary' | 'danger'

interface EvaluationForm {
  name: string
  academicYear: string
  semester: number
  startDate: string
  endDate: string
  remark: string
  awardConfigs: ApiBatchAwardConfig[]
  selectedRuleIdsByType: Record<number, number[]>
}

interface QueryParams {
  current: number
  size: number
  academicYears: string[]
  semesters: number[]
  statuses: number[]
}

interface OptionItem<T extends string | number> {
  label: string
  value: T
}

interface RuleGroup {
  type: number
  label: string
  rules: ScoreRule[]
}

const BATCH_STATUS = {
  NOT_STARTED: 1,
  APPLYING: 2,
  REVIEWING: 3,
  PUBLICITY: 4,
  COMPLETED: 5
} as const

const SEMESTER_OPTIONS: OptionItem<number>[] = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 },
  { label: '全年', value: 3 }
]

const STATUS_OPTIONS: OptionItem<BatchStatus>[] = [
  { label: '未开始', value: BATCH_STATUS.NOT_STARTED },
  { label: '申请中', value: BATCH_STATUS.APPLYING },
  { label: '评审中', value: BATCH_STATUS.REVIEWING },
  { label: '公示中', value: BATCH_STATUS.PUBLICITY },
  { label: '已完成', value: BATCH_STATUS.COMPLETED }
]

const academicYearOptions = ref<OptionItem<string>[]>([])
const formAcademicYearOptions = computed<OptionItem<string>[]>(() => {
  if (academicYearOptions.value.length === 0) {
    const currentYear = new Date().getFullYear()
    return [
      buildAcademicYearOption(String(currentYear)),
      buildAcademicYearOption(String(currentYear + 1))
    ]
  }

  const nextYearValue = String(
    Math.max(...academicYearOptions.value.map(option => Number.parseInt(option.value, 10)).filter(year => !Number.isNaN(year))) + 1
  )
  const merged = new Map<string, OptionItem<string>>(
    academicYearOptions.value.map(option => [option.value, option])
  )
  if (!merged.has(nextYearValue)) {
    merged.set(nextYearValue, buildAcademicYearOption(nextYearValue))
  }

  return Array.from(merged.values()).sort((left, right) => right.value.localeCompare(left.value))
})
const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance | null>(null)
const tableData = ref<ApiEvaluationBatch[]>([])
const availableRulesByType = ref<Record<number, ScoreRule[]>>({})
const allRules = ref<ScoreRule[]>([])

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10,
  academicYears: [],
  semesters: [],
  statuses: []
})

const EVALUATION_POLL_INTERVAL_MS = 2000
const EVALUATION_POLL_FAILURE_LIMIT = 3
const evaluationTaskMap = reactive<Record<number, EvaluationTaskResponse | null>>({})
const pollTimerMap = new Map<number, ReturnType<typeof setTimeout>>()
const pollLockMap = new Map<number, boolean>()
const pollFailureCountMap = reactive<Record<number, number>>({})

function isEvaluating(batchId?: number): boolean {
  if (!batchId) return false
  const task = evaluationTaskMap[batchId]
  return task !== undefined && task !== null && (task.status === EVALUATION_TASK_STATUS.PENDING || task.status === EVALUATION_TASK_STATUS.RUNNING)
}

function getEvaluateButtonText(batchId?: number): string {
  if (!batchId) return '一键评定'
  const task = evaluationTaskMap[batchId]
  if (!task) return '一键评定'
  if (task.status === EVALUATION_TASK_STATUS.PENDING) return '等待执行...'
  if (task.status === EVALUATION_TASK_STATUS.RUNNING) return '评定中...'
  return '一键评定'
}

function stopPolling(batchId: number): void {
  const timer = pollTimerMap.get(batchId)
  if (timer !== undefined) {
    clearTimeout(timer)
    pollTimerMap.delete(batchId)
  }
  pollLockMap.delete(batchId)
  delete pollFailureCountMap[batchId]
}

function schedulePoll(batchId: number, taskId: number): void {
  const timer = setTimeout(async () => {
    pollTimerMap.delete(batchId)
    await pollTaskStatus(batchId, taskId)
  }, EVALUATION_POLL_INTERVAL_MS)
  pollTimerMap.set(batchId, timer)
}

async function pollTaskStatus(batchId: number, taskId: number): Promise<void> {
  // 请求锁：防止同一 batchId 并发轮询
  if (pollLockMap.get(batchId)) return
  pollLockMap.set(batchId, true)

  try {
    const response = await getEvaluationTask(taskId)
    const task = extractApiData<EvaluationTaskResponse>(response)
    if (!task) {
      pollLockMap.delete(batchId)
      return
    }
    pollFailureCountMap[batchId] = 0
    evaluationTaskMap[batchId] = task

    if (task.status === EVALUATION_TASK_STATUS.SUCCESS) {
      // SUCCESS
      stopPolling(batchId)
      ElMessage.success('评定执行成功，页面即将刷新')
      setTimeout(() => handleQuery(), 500)
      return
    } else if (task.status === EVALUATION_TASK_STATUS.FAILED) {
      // FAILED
      stopPolling(batchId)
      ElMessage.error(task.errorMessage || '评定执行失败，请稍后重试')
      return
    }

    // 仍未结束，释放锁并排定下一次轮询
    pollLockMap.delete(batchId)
    if (task.status === EVALUATION_TASK_STATUS.PENDING || task.status === EVALUATION_TASK_STATUS.RUNNING) {
      schedulePoll(batchId, taskId)
    }
  } catch (error) {
    pollLockMap.delete(batchId)
    const nextFailureCount = (pollFailureCountMap[batchId] || 0) + 1
    pollFailureCountMap[batchId] = nextFailureCount
    if (nextFailureCount >= EVALUATION_POLL_FAILURE_LIMIT) {
      stopPolling(batchId)
      evaluationTaskMap[batchId] = null
      ElMessage.error('评定状态查询连续失败，请刷新页面后重试')
      return
    }
    console.error('轮询任务状态失败:', error)
    // 失败后继续排定下一次轮询
    schedulePoll(batchId, taskId)
  }
}

async function handleEvaluate(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(
      '确定对该批次执行一键评定吗？评定将在后台异步执行，可随时刷新页面查看最新状态。',
      '确认评定',
      {
        confirmButtonText: '确定评定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await evaluateBatch(row.id)
    const task = extractApiData<EvaluationTaskResponse>(response)
    if (!task) return
    evaluationTaskMap[row.id] = task
    ElMessage.success('评定任务已提交，正在后台执行...')
    await pollTaskStatus(row.id, task.taskId)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起评定失败:', error)
      ElMessage.error('发起评定失败，请稍后重试')
    }
  }
}

async function restoreEvaluationTasks(records: ApiEvaluationBatch[]): Promise<void> {
  const reviewingBatches = records.filter(
    item => item.id !== undefined && item.status === BATCH_STATUS.REVIEWING
  )
  if (reviewingBatches.length === 0) return

  await batchExecute(
    reviewingBatches,
    async item => {
      const batchId = item.id as number
      try {
        const response = await getLatestEvaluationTask(batchId)
        const task = extractApiData<EvaluationTaskResponse>(response)
        if (task && (task.status === EVALUATION_TASK_STATUS.PENDING || task.status === EVALUATION_TASK_STATUS.RUNNING)) {
          evaluationTaskMap[batchId] = task
          pollTaskStatus(batchId, task.taskId)
        }
      } catch (error) {
        console.error(`恢复批次 ${batchId} 任务状态失败:`, error)
      }
    },
    4
  )
}

function createDefaultAwardConfigs(): ApiBatchAwardConfig[] {
  return [
    { awardLevel: 1, ratio: 5, amount: 10000 },
    { awardLevel: 2, ratio: 10, amount: 5000 },
    { awardLevel: 3, ratio: 20, amount: 3000 },
    { awardLevel: 4, ratio: 30, amount: 1000 }
  ]
}

function createDefaultSelectedRuleIdsByType(): Record<number, number[]> {
  return Object.keys(RULE_TYPE_LABELS).reduce<Record<number, number[]>>((acc, type) => {
    acc[Number(type)] = []
    return acc
  }, {})
}

const formData = reactive<EvaluationForm>({
  name: '',
  academicYear: '',
  semester: 1,
  startDate: '',
  endDate: '',
  remark: '',
  awardConfigs: createDefaultAwardConfigs(),
  selectedRuleIdsByType: createDefaultSelectedRuleIdsByType()
})

const formRules: FormRules<EvaluationForm> = {
  name: [{ required: true, message: '请输入评定名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择申请开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择申请结束日期', trigger: 'change' }]
}

const totalAwardRatio = computed(() =>
  Number(formData.awardConfigs.reduce((sum, item) => sum + Number(item.ratio || 0), 0).toFixed(2))
)

const ruleGroups = computed<RuleGroup[]>(() =>
  Object.entries(RULE_TYPE_LABELS).map(([type, label]) => ({
    type: Number(type),
    label,
    rules: availableRulesByType.value[Number(type)] || []
  }))
)

const ruleNameMap = computed(() => {
  const entries = allRules.value
    .filter(rule => rule.id !== undefined)
    .map(rule => [rule.id as number, buildRuleLabel(rule)] as const)
  return new Map<number, string>(entries)
})

function getStatusText(status: number | undefined): string {
  return STATUS_OPTIONS.find(item => item.value === status)?.label || '未知'
}

function getStatusTagType(status: number | undefined): TagType {
  switch (status) {
    case BATCH_STATUS.NOT_STARTED:
      return 'info'
    case BATCH_STATUS.APPLYING:
      return 'primary'
    case BATCH_STATUS.REVIEWING:
      return 'warning'
    case BATCH_STATUS.PUBLICITY:
      return 'success'
    case BATCH_STATUS.COMPLETED:
      return 'success'
    default:
      return 'info'
  }
}

function formatSemester(semester: number | null | undefined): string {
  return SEMESTER_OPTIONS.find(item => item.value === semester)?.label || '-'
}

function formatAcademicYearSemester(academicYear: string | null | undefined, semester: number | null | undefined): string {
  if (!academicYear) return formatSemester(semester)
  return `${formatAcademicYearLabel(academicYear)}${formatSemester(semester)}`
}

function buildAcademicYearOption(value: string): OptionItem<string> {
  return {
    label: formatAcademicYearLabel(value),
    value
  }
}

async function loadAcademicYearOptions(): Promise<void> {
  try {
    const response = await getEvaluationAcademicYears()
    const years = extractApiData<string[]>(response) || []
    academicYearOptions.value = [...years]
      .sort((left, right) => right.localeCompare(left))
      .map(buildAcademicYearOption)
  } catch (error) {
    console.error('加载学年选项失败:', error)
    academicYearOptions.value = []
  }
}

function getAwardLevelName(level: number): string {
  return getAwardLevelConfig(level).text
}

function buildRuleLabel(rule: ScoreRule): string {
  const extras = [rule.level, rule.ruleCode].filter(Boolean).join(' / ')
  return extras ? `${rule.ruleName}（${extras}）` : rule.ruleName
}

function flattenSelectedRuleIds(selectedRuleIdsByType: Record<number, number[]>): number[] {
  return Object.values(selectedRuleIdsByType).flat().filter((id, index, arr) => arr.indexOf(id) === index)
}

function resetForm(): void {
  Object.assign(formData, {
    name: '',
    academicYear: '',
    semester: 1,
    startDate: '',
    endDate: '',
    remark: '',
    awardConfigs: createDefaultAwardConfigs(),
    selectedRuleIdsByType: createDefaultSelectedRuleIdsByType()
  })
}

async function loadRules(): Promise<void> {
  const allRuleRes = await getRulePage({ current: 1, size: LARGE_QUERY_SIZE })
  const allRulePage = extractApiData<API.PageResponse<ScoreRule>>(allRuleRes)
  allRules.value = allRulePage?.records || []

  // 从全量规则中按 type 过滤，替代逐个调用 getAvailableRulesByType
  const nextRulesByType: Record<number, ScoreRule[]> = {}
  for (const type of Object.keys(RULE_TYPE_LABELS).map(Number)) {
    nextRulesByType[type] = allRules.value.filter(rule => rule.ruleType === type)
  }
  availableRulesByType.value = nextRulesByType
}

async function ensureRulesLoaded(ruleIds: number[]): Promise<void> {
  const missingRuleIds = ruleIds.filter(id => !ruleNameMap.value.has(id))
  if (missingRuleIds.length === 0) {
    return
  }

  const fetchedRules = await batchExecute(
    missingRuleIds,
    async id => {
      try {
        const response = await getRuleById(id)
        return extractApiData<ScoreRule>(response)
      } catch (error) {
        console.error(`加载规则 ${id} 失败:`, error)
        return null
      }
    },
    4
  )

  const appendedRules = fetchedRules.filter((rule): rule is ScoreRule => Boolean(rule?.id))
  if (appendedRules.length === 0) {
    return
  }

  const mergedRuleMap = new Map<number, ScoreRule>()
  allRules.value.forEach(rule => {
    if (rule.id !== undefined) {
      mergedRuleMap.set(rule.id, rule)
    }
  })
  appendedRules.forEach(rule => {
    mergedRuleMap.set(rule.id as number, rule)
  })
  allRules.value = Array.from(mergedRuleMap.values())
}

async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const response = await getEvaluationPage({
      current: queryParams.current,
      size: queryParams.size,
      academicYears: queryParams.academicYears.length > 0 ? queryParams.academicYears : undefined,
      semesters: queryParams.semesters.length > 0 ? queryParams.semesters : undefined,
      statuses: queryParams.statuses.length > 0 ? queryParams.statuses : undefined
    })
    const pageData = extractApiData<API.PageResponse<ApiEvaluationBatch>>(response)
    tableData.value = pageData?.records || []
    total.value = pageData?.total || 0
    await restoreEvaluationTasks(tableData.value)
  } catch (error) {
    console.error('查询失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

function handleReset(): void {
  queryParams.academicYears = []
  queryParams.semesters = []
  queryParams.statuses = []
  queryParams.current = 1
  handleQuery()
}

async function handleView(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return

  try {
    const response = await getEvaluationDetail(row.id)
    const detail = extractApiData<ApiEvaluationBatch>(response)
    if (!detail) return
    await ensureRulesLoaded(detail.selectedRuleIds || [])

    const awardConfigText = (detail.awardConfigs || [])
      .map(item => `${getAwardLevelName(item.awardLevel)}：${item.ratio}% / ${item.amount}元`)
      .join('<br/>')

    const groupedRuleText = Object.entries(RULE_TYPE_LABELS)
      .map(([type, label]) => {
        const selectedIds = (detail.selectedRuleIds || []).filter(id => {
          const rule = allRules.value.find(item => item.id === id)
          return rule?.ruleType === Number(type)
        })
        const names = selectedIds.map(id => ruleNameMap.value.get(id) || `规则#${id}`)
        return `${label}：${names.length > 0 ? names.join('、') : '未配置'}`
      })
      .join('<br/>')

    await ElMessageBox.alert(
      [
        `评定名称：${detail.name}`,
        `学年学期：${formatAcademicYearSemester(detail.academicYear, detail.semester)}`,
        `申请开始：${detail.startDate || '-'}`,
        `申请结束：${detail.endDate || '-'}`,
        `获奖人数：${detail.winnerCount ?? 0}`,
        `奖学金总额：${detail.totalAmount ?? 0}`,
        `状态：${getStatusText(detail.status)}`,
        `奖项配置：<br/>${awardConfigText || '-'}`,
        `参与评定规则：<br/>${groupedRuleText || '-'}`,
        detail.remark ? `备注：${detail.remark}` : ''
      ].filter(Boolean).join('<br/><br/>'),
      '评定详情',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      }
    )
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

function handleStart(): void {
  resetForm()
  dialogVisible.value = true
}

async function handleStartApplication(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定开始该批次的申请阶段吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await startEvaluationApplication(row.id)
    ElMessage.success('已进入申请阶段')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始申请失败:', error)
      ElMessage.error('开始申请失败')
    }
  }
}

async function handleStartReview(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定开始评审吗？批次将进入评审中。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await startEvaluationReview(row.id)
    ElMessage.success('已进入评审阶段')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始评审失败:', error)
      ElMessage.error('开始评审失败')
    }
  }
}

async function handleStartPublicity(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  if (isEvaluating(row.id)) {
    ElMessage.warning('当前批次仍有评定任务执行中，暂不能开始公示')
    return
  }
  try {
    await ElMessageBox.confirm('确定开始公示吗？批次将进入公示中。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await startEvaluationPublicity(row.id)
    ElMessage.success('已进入公示阶段')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始公示失败:', error)
      ElMessage.error('开始公示失败')
    }
  }
}

async function handleComplete(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定完成该批次评定吗？完成后状态不可逆。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await completeEvaluation(row.id)
    ElMessage.success('评定已完成')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成评定失败:', error)
      ElMessage.error('完成评定失败')
    }
  }
}

async function handleDelete(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定删除该评定批次吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteEvaluation(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!formData.startDate || !formData.endDate || new Date(formData.endDate) <= new Date(formData.startDate)) {
    ElMessage.error('申请结束日期必须晚于申请开始日期')
    return
  }

  if (totalAwardRatio.value > 100) {
    ElMessage.error('奖项比例总和不能超过 100%')
    return
  }

  if (formData.awardConfigs.some(item => Number(item.amount) < 0 || Number(item.ratio) <= 0)) {
    ElMessage.error('奖项比例必须大于 0，金额不能为负数')
    return
  }

  submitting.value = true
  try {
    await createEvaluation({
      name: formData.name,
      academicYear: formData.academicYear,
      semester: formData.semester,
      startDate: formData.startDate,
      endDate: formData.endDate,
      status: BATCH_STATUS.NOT_STARTED,
      remark: formData.remark,
      awardConfigs: formData.awardConfigs.map(item => ({
        awardLevel: item.awardLevel,
        ratio: Number(item.ratio),
        amount: Number(item.amount)
      })),
      selectedRuleIds: flattenSelectedRuleIds(formData.selectedRuleIdsByType)
    })
    ElMessage.success('评定创建成功')
    dialogVisible.value = false
    await handleQuery()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

function handleDialogClose(): void {
  formRef.value?.resetFields()
  resetForm()
}

onMounted(() => {
  void Promise.allSettled([loadRules(), loadAcademicYearOptions(), handleQuery()])
})

onUnmounted(() => {
  pollTimerMap.forEach(timer => clearTimeout(timer))
  pollTimerMap.clear()
  pollLockMap.clear()
})
</script>

<style scoped lang="scss">
.evaluation-page {
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
    color: #303133;
    font-size: 18px;
    font-weight: 500;
  }
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.config-block {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
}

.config-tip {
  margin-top: 12px;
  color: #606266;
  font-size: 13px;

  &.invalid {
    color: #f56c6c;
    font-weight: 600;
  }
}

.rule-group {
  margin-bottom: 16px;
}

.rule-group-title {
  margin-bottom: 8px;
  color: #303133;
  font-weight: 600;
}
</style>
