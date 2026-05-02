<!--
  管理员 - 评定管理页面
  功能：创建评定批次、管理批次状态（全流程：未开始→申请中→评审中→公示中→已完成）、发起一键评定
-->
<template>
  <div class="evaluation-page">
    <!-- 页面头部：标题 + 发起评定按钮 -->
    <div class="page-header">
      <h2 class="page-title">评定管理</h2>
      <el-button type="primary" @click="handleStart">
        <el-icon><Plus /></el-icon>
        发起评定
      </el-button>
    </div>

    <!-- 搜索表单：学年、学期、状态筛选 -->
    <el-form :inline="true" class="search-form">
      <!-- 学年筛选（支持多选） -->
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
      <!-- 学期筛选（支持多选） -->
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
      <!-- 状态筛选（支持多选） -->
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
      <!-- 查询、重置按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 评定批次数据表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <!-- 多选框列 -->
      <el-table-column type="selection" width="55" />
      <!-- 序号列 -->
      <el-table-column type="index" label="序号" width="60" />
      <!-- 学年学期列（格式化显示） -->
      <el-table-column label="学年学期" width="180">
        <template #default="{ row }">
          {{ formatAcademicYearSemester(row.academicYear, row.semester) }}
        </template>
      </el-table-column>
      <!-- 评定名称列 -->
      <el-table-column prop="name" label="评定名称" min-width="220" />
      <!-- 申请开始日期列 -->
      <el-table-column prop="startDate" label="申请开始" width="120" />
      <!-- 申请结束日期列 -->
      <el-table-column prop="endDate" label="申请结束" width="120" />
      <!-- 获奖人数列 -->
      <el-table-column prop="winnerCount" label="获奖人数" width="100" />
      <!-- 状态列：以标签形式展示 -->
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 操作列：根据状态显示不同按钮 -->
      <el-table-column label="操作" min-width="320" fixed="right">
        <template #default="{ row }">
          <!-- 查看详情（所有状态均可查看） -->
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <!-- 未开始 → 点击"开始申请"按钮 -->
          <el-button
            v-if="row.status === BATCH_STATUS.NOT_STARTED"
            link
            type="primary"
            @click="handleStartApplication(row)"
          >
            开始申请
          </el-button>
          <!-- 申请中 → 点击"开始评审"按钮 -->
          <el-button
            v-else-if="row.status === BATCH_STATUS.APPLYING"
            link
            type="warning"
            @click="handleStartReview(row)"
          >
            开始评审
          </el-button>
          <!-- 评审中 → 显示"一键评定"和"开始公示"按钮 -->
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
          <!-- 公示中 → 点击"完成评定"按钮 -->
          <el-button
            v-else-if="row.status === BATCH_STATUS.PUBLICITY"
            link
            type="success"
            @click="handleComplete(row)"
          >
            完成评定
          </el-button>
          <!-- 删除按钮（所有状态均可删除） -->
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handleQuery"
      @size-change="handleQuery"
    />

    <!-- 发起评定对话框：创建新的评定批次 -->
    <el-dialog v-model="dialogVisible" title="发起评定" width="900px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="16">
          <!-- 评定名称 -->
          <el-col :span="12">
            <el-form-item label="评定名称" prop="name">
              <el-input v-model="formData.name" placeholder="如：2024-2025学年第一学期奖学金评定" />
            </el-form-item>
          </el-col>
          <!-- 学年 -->
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
          <!-- 学期 -->
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
          <!-- 申请开始日期 -->
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
          <!-- 申请结束日期 -->
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

        <!-- 奖项配置：各等级奖学金的比例和金额 -->
        <el-form-item label="奖项配置">
          <div class="config-block">
            <el-table :data="formData.awardConfigs" border size="small">
              <!-- 奖项等级（只读显示） -->
              <el-table-column label="奖项等级" width="140">
                <template #default="{ row }">
                  {{ getAwardLevelName(row.awardLevel) }}
                </template>
              </el-table-column>
              <!-- 比例（百分比） -->
              <el-table-column label="比例(%)" width="180">
                <template #default="{ row }">
                  <el-input-number v-model="row.ratio" :min="0" :precision="2" :step="1" />
                </template>
              </el-table-column>
              <!-- 金额（元） -->
              <el-table-column label="金额(元)" width="220">
                <template #default="{ row }">
                  <el-input-number v-model="row.amount" :min="0" :precision="2" :step="100" />
                </template>
              </el-table-column>
            </el-table>
            <!-- 比例总和提示（超过100%时显示红色警示） -->
            <div class="config-tip" :class="{ invalid: totalAwardRatio > 100 }">
              当前比例总和：{{ totalAwardRatio }}%
            </div>
          </div>
        </el-form-item>

        <!-- 参与评定规则集：按规则类型分组选择 -->
        <el-form-item label="参与评定规则集">
          <div class="config-block">
            <el-row :gutter="16">
              <!-- 按规则类型（如论文、专利等）分组显示选择框 -->
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

        <!-- 备注说明 -->
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
/**
 * 评定管理页面
 * 功能：
 * - 分页查询评定批次列表（支持学年、学期、状态筛选）
 * - 创建新的评定批次（配置奖项比例、选择参与规则集）
 * - 批次状态流转管理：未开始 → 申请中 → 评审中 → 公示中 → 已完成
 * - 一键评定：异步执行评分计算，支持实时轮询任务状态
 * - 恢复页面时自动恢复进行中评定任务的状态
 *
 * 批次状态说明：
 * - 1 未开始：批次刚创建，未进入申请阶段
 * - 2 申请中：学生可提交奖学金申请
 * - 3 评审中：导师可审核学生成果，管理员可发起评定计算
 * - 4 公示中：评定结果公示，接受异议
 * - 5 已完成：评定结束，结果最终确认
 */
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

/** 批次状态类型别名 */
type BatchStatus = 1 | 2 | 3 | 4 | 5
/** 标签颜色类型 */
type TagType = 'success' | 'warning' | 'info' | 'primary' | 'danger'

/**
 * 评定表单数据结构（用于创建/编辑批次）
 */
interface EvaluationForm {
  name: string                // 评定名称
  academicYear: string        // 学年（如：2024）
  semester: number            // 学期（1=第一学期，2=第二学期，3=全年）
  startDate: string           // 申请开始日期（yyyy-MM-dd）
  endDate: string             // 申请结束日期（yyyy-MM-dd）
  remark: string              // 备注说明
  awardConfigs: ApiBatchAwardConfig[]  // 奖项配置列表
  selectedRuleIdsByType: Record<number, number[]>  // 按类型分组选中的规则ID
}

/** 查询参数接口 */
interface QueryParams {
  current: number
  size: number
  academicYears: string[]
  semesters: number[]
  statuses: number[]
}

/** 下拉选项通用接口 */
interface OptionItem<T extends string | number> {
  label: string
  value: T
}

/** 规则分组接口（按类型分组） */
interface RuleGroup {
  type: number
  label: string
  rules: ScoreRule[]
}

/** 批次状态枚举 */
const BATCH_STATUS = {
  NOT_STARTED: 1,     // 未开始
  APPLYING: 2,        // 申请中
  REVIEWING: 3,       // 评审中
  PUBLICITY: 4,       // 公示中
  COMPLETED: 5        // 已完成
} as const

/** 学期选项 */
const SEMESTER_OPTIONS: OptionItem<number>[] = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 },
  { label: '全年', value: 3 }
]

/** 批次状态选项 */
const STATUS_OPTIONS: OptionItem<BatchStatus>[] = [
  { label: '未开始', value: BATCH_STATUS.NOT_STARTED },
  { label: '申请中', value: BATCH_STATUS.APPLYING },
  { label: '评审中', value: BATCH_STATUS.REVIEWING },
  { label: '公示中', value: BATCH_STATUS.PUBLICITY },
  { label: '已完成', value: BATCH_STATUS.COMPLETED }
]

// ==================== 状态 ====================

/** 学年下拉选项（动态从后端加载已有学年） */
const academicYearOptions = ref<OptionItem<string>[]>([])

/**
 * 表单中学年下拉选项：
 * - 如果已有学年数据，则基于最大年份扩展
 * - 如果没有数据，则默认提供当前年份和下一年
 */
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

/** 表格加载状态 */
const loading = ref(false)
/** 表单提交中状态（防止重复提交） */
const submitting = ref(false)
/** 数据总数（用于分页） */
const total = ref(0)
/** 添加/编辑对话框是否显示 */
const dialogVisible = ref(false)
/** 表单引用（用于验证） */
const formRef = ref<FormInstance | null>(null)
/** 表格数据列表 */
const tableData = ref<ApiEvaluationBatch[]>([])
/** 按类型分组的可用规则 */
const availableRulesByType = ref<Record<number, ScoreRule[]>>({})
/** 全量规则列表（用于规则标签构建） */
const allRules = ref<ScoreRule[]>([])

// ==================== 查询参数 ====================

/** 查询参数（分页 + 筛选条件） */
const queryParams = reactive<QueryParams>({
  current: 1,       // 当前页码
  size: 10,         // 每页条数
  academicYears: [], // 学年列表
  semesters: [],    // 学期列表
  statuses: []      // 状态列表
})

// ==================== 评定任务轮询状态 ====================

/** 评定任务轮询间隔（毫秒） */
const EVALUATION_POLL_INTERVAL_MS = 2000
/** 连续失败次数上限（超过后停止轮询） */
const EVALUATION_POLL_FAILURE_LIMIT = 3
/** 批次ID → 评定任务响应 的映射（用于UI显示评定状态） */
const evaluationTaskMap = reactive<Record<number, EvaluationTaskResponse | null>>({})
/** 批次ID → setTimeout定时器ID 的映射（用于清理） */
const pollTimerMap = new Map<number, ReturnType<typeof setTimeout>>()
/** 批次ID → 请求锁 的映射（防止同一批次并发轮询） */
const pollLockMap = new Map<number, boolean>()
/** 批次ID → 连续失败次数 的映射 */
const pollFailureCountMap = reactive<Record<number, number>>({})

/**
 * 判断指定批次是否正在评定中
 */
function isEvaluating(batchId?: number): boolean {
  if (!batchId) return false
  const task = evaluationTaskMap[batchId]
  return task !== undefined && task !== null && (task.status === EVALUATION_TASK_STATUS.PENDING || task.status === EVALUATION_TASK_STATUS.RUNNING)
}

/**
 * 获取评定按钮文本（根据任务状态动态变化）
 */
function getEvaluateButtonText(batchId?: number): string {
  if (!batchId) return '一键评定'
  const task = evaluationTaskMap[batchId]
  if (!task) return '一键评定'
  if (task.status === EVALUATION_TASK_STATUS.PENDING) return '等待执行...'
  if (task.status === EVALUATION_TASK_STATUS.RUNNING) return '评定中...'
  return '一键评定'
}

/**
 * 停止轮询指定批次的状态
 * 清理定时器、请求锁、失败计数
 */
function stopPolling(batchId: number): void {
  const timer = pollTimerMap.get(batchId)
  if (timer !== undefined) {
    clearTimeout(timer)
    pollTimerMap.delete(batchId)
  }
  pollLockMap.delete(batchId)
  delete pollFailureCountMap[batchId]
}

/**
 * 排定下一次轮询任务
 */
function schedulePoll(batchId: number, taskId: number): void {
  const timer = setTimeout(async () => {
    pollTimerMap.delete(batchId)
    await pollTaskStatus(batchId, taskId)
  }, EVALUATION_POLL_INTERVAL_MS)
  pollTimerMap.set(batchId, timer)
}

/**
 * 轮询评定任务状态
 * - PENDING/RUNNING：继续轮询
 * - SUCCESS：停止轮询，刷新页面
 * - FAILED：停止轮询，显示错误
 */
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
      // 评定成功，停止轮询，延迟刷新页面
      stopPolling(batchId)
      ElMessage.success('评定执行成功，页面即将刷新')
      setTimeout(() => handleQuery(), 500)
      return
    } else if (task.status === EVALUATION_TASK_STATUS.FAILED) {
      // 评定失败，停止轮询，显示错误消息
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
      // 连续失败超过限制，停止轮询
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

/**
 * 点击"一键评定"按钮：发起异步评定任务
 * 提交后自动开始轮询任务状态
 */
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
    // 记录任务响应（用于UI显示状态）
    evaluationTaskMap[row.id] = task
    ElMessage.success('评定任务已提交，正在后台执行...')
    // 开始轮询任务状态
    await pollTaskStatus(row.id, task.taskId)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起评定失败:', error)
      ElMessage.error('发起评定失败，请稍后重试')
    }
  }
}

/**
 * 恢复页面时，对于"评审中"状态的批次，恢复其评定任务的状态
 * 避免用户刷新页面后丢失进行中的任务状态
 */
async function restoreEvaluationTasks(records: ApiEvaluationBatch[]): Promise<void> {
  // 筛选出"评审中"状态的批次
  const reviewingBatches = records.filter(
    item => item.id !== undefined && item.status === BATCH_STATUS.REVIEWING
  )
  if (reviewingBatches.length === 0) return

  // 并发恢复任务状态（限制并发数为4）
  await batchExecute(
    reviewingBatches,
    async item => {
      const batchId = item.id as number
      try {
        const response = await getLatestEvaluationTask(batchId)
        const task = extractApiData<EvaluationTaskResponse>(response)
        if (task && (task.status === EVALUATION_TASK_STATUS.PENDING || task.status === EVALUATION_TASK_STATUS.RUNNING)) {
          // 任务仍在进行中，恢复状态并开始轮询
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

/**
 * 创建默认奖项配置（特等/一等/二等/三等奖学金）
 */
function createDefaultAwardConfigs(): ApiBatchAwardConfig[] {
  return [
    { awardLevel: 1, ratio: 5, amount: 10000 },
    { awardLevel: 2, ratio: 10, amount: 5000 },
    { awardLevel: 3, ratio: 20, amount: 3000 },
    { awardLevel: 4, ratio: 30, amount: 1000 }
  ]
}

/**
 * 创建按类型分组的规则ID映射（用于表单绑定）
 */
function createDefaultSelectedRuleIdsByType(): Record<number, number[]> {
  return Object.keys(RULE_TYPE_LABELS).reduce<Record<number, number[]>>((acc, type) => {
    acc[Number(type)] = []
    return acc
  }, {})
}

// ==================== 表单数据 ====================

/** 对话框中使用的表单数据（响应式） */
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

/** 表单验证规则 */
const formRules: FormRules<EvaluationForm> = {
  name: [{ required: true, message: '请输入评定名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择申请开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择申请结束日期', trigger: 'change' }]
}

/**
 * 奖项比例总和（用于校验，不能超过100%）
 */
const totalAwardRatio = computed(() =>
  Number(formData.awardConfigs.reduce((sum, item) => sum + Number(item.ratio || 0), 0).toFixed(2))
)

/**
 * 按规则类型分组的规则列表（用于表单中的选择框）
 */
const ruleGroups = computed<RuleGroup[]>(() =>
  Object.entries(RULE_TYPE_LABELS).map(([type, label]) => ({
    type: Number(type),
    label,
    rules: availableRulesByType.value[Number(type)] || []
  }))
)

/**
 * 规则ID → 规则标签文本 的映射（用于详情展示）
 */
const ruleNameMap = computed(() => {
  const entries = allRules.value
    .filter(rule => rule.id !== undefined)
    .map(rule => [rule.id as number, buildRuleLabel(rule)] as const)
  return new Map<number, string>(entries)
})

// ==================== 工具方法 ====================

/**
 * 获取状态文本
 */
function getStatusText(status: number | undefined): string {
  return STATUS_OPTIONS.find(item => item.value === status)?.label || '未知'
}

/**
 * 获取状态的标签颜色类型
 */
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

/**
 * 格式化学期显示文本
 */
function formatSemester(semester: number | null | undefined): string {
  return SEMESTER_OPTIONS.find(item => item.value === semester)?.label || '-'
}

/**
 * 格式化学年学期显示（如：2024-2025学年 第一学期）
 */
function formatAcademicYearSemester(academicYear: string | null | undefined, semester: number | null | undefined): string {
  if (!academicYear) return formatSemester(semester)
  return `${formatAcademicYearLabel(academicYear)}${formatSemester(semester)}`
}

/**
 * 构建学年选项（如：2024 → "2024-2025学年"）
 */
function buildAcademicYearOption(value: string): OptionItem<string> {
  return {
    label: formatAcademicYearLabel(value),
    value
  }
}

/**
 * 加载学年下拉选项（从已有批次中提取）
 */
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

/**
 * 获取奖项等级名称
 */
function getAwardLevelName(level: number): string {
  return getAwardLevelConfig(level).text
}

/**
 * 构建规则标签文本（如："SCI一区论文（SCI一区 / PAPER_SCI_1）"）
 */
function buildRuleLabel(rule: ScoreRule): string {
  const extras = [rule.level, rule.ruleCode].filter(Boolean).join(' / ')
  return extras ? `${rule.ruleName}（${extras}）` : rule.ruleName
}

/**
 * 将按类型分组的规则ID展平为单一列表（用于提交）
 */
function flattenSelectedRuleIds(selectedRuleIdsByType: Record<number, number[]>): number[] {
  return Object.values(selectedRuleIdsByType).flat().filter((id, index, arr) => arr.indexOf(id) === index)
}

/**
 * 重置表单数据为默认值（用于发起新评定）
 */
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

/**
 * 加载所有规则并按类型分组
 */
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

/**
 * 确保指定规则ID的规则已加载到 allRules 中
 * 用于查看详情时，如果选中的规则不在列表中则单独获取
 */
async function ensureRulesLoaded(ruleIds: number[]): Promise<void> {
  const missingRuleIds = ruleIds.filter(id => !ruleNameMap.value.has(id))
  if (missingRuleIds.length === 0) {
    return
  }

  // 并发获取缺失的规则（限制并发数为4）
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

  // 合并到全量规则列表
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

// ==================== 事件处理 ====================

/**
 * 查询评定批次列表
 */
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
    // 恢复进行中评定任务的状态
    await restoreEvaluationTasks(tableData.value)
  } catch (error) {
    console.error('查询失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

/**
 * 点击重置按钮：清空筛选条件并重新查询
 */
function handleReset(): void {
  queryParams.academicYears = []
  queryParams.semesters = []
  queryParams.statuses = []
  queryParams.current = 1
  handleQuery()
}

/**
 * 点击查看详情按钮：以弹窗形式显示批次详细信息
 * 包括：奖项配置、参与规则集等
 */
async function handleView(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return

  try {
    const response = await getEvaluationDetail(row.id)
    const detail = extractApiData<ApiEvaluationBatch>(response)
    if (!detail) return
    // 确保选中的规则已加载（用于显示规则名称）
    await ensureRulesLoaded(detail.selectedRuleIds || [])

    // 格式化奖项配置文本
    const awardConfigText = (detail.awardConfigs || [])
      .map(item => `${getAwardLevelName(item.awardLevel)}：${item.ratio}% / ${item.amount}元`)
      .join('<br/>')

    // 格式化按类型分组的规则文本
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

/**
 * 点击"发起评定"按钮：重置表单并打开对话框
 */
function handleStart(): void {
  resetForm()
  dialogVisible.value = true
}

/**
 * 点击"开始申请"按钮：批次进入申请阶段
 * 学生可以开始提交奖学金申请
 */
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

/**
 * 点击"开始评审"按钮：批次进入评审阶段
 * 导师可以开始审核学生提交的成果
 */
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

/**
 * 点击"开始公示"按钮：批次进入公示阶段
 * 注意：只有当该批次没有进行中的评定任务时才能开始公示
 */
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

/**
 * 点击"完成评定"按钮：批次进入已完成状态
 * 注意：完成后状态不可逆
 */
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

/**
 * 点击"删除"按钮：删除评定批次
 */
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

/**
 * 点击"确定"按钮：提交表单（创建新评定批次）
 * 包含多项校验：日期逻辑、奖项比例总和、奖项必填等
 */
async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 校验：申请结束日期必须晚于申请开始日期
  if (!formData.startDate || !formData.endDate || new Date(formData.endDate) <= new Date(formData.startDate)) {
    ElMessage.error('申请结束日期必须晚于申请开始日期')
    return
  }

  // 校验：奖项比例总和不能超过100%
  if (totalAwardRatio.value > 100) {
    ElMessage.error('奖项比例总和不能超过 100%')
    return
  }

  // 校验：奖项比例必须大于0，金额不能为负数
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

/**
 * 对话框关闭时的处理：重置表单验证状态和表单数据
 */
function handleDialogClose(): void {
  formRef.value?.resetFields()
  resetForm()
}

// ==================== 生命周期 ====================

/** 组件挂载时：并发加载规则、学年选项、批次列表 */
onMounted(() => {
  void Promise.allSettled([loadRules(), loadAcademicYearOptions(), handleQuery()])
})

/** 组件卸载时：清理所有轮询定时器 */
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

/* 页面头部：标题 + 操作按钮水平排列 */
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

/* 搜索表单容器：浅灰背景 + 圆角 */
.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 分页组件：右对齐 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 奖项配置和规则配置的容器 */
.config-block {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
}

/* 奖项比例总和提示 */
.config-tip {
  margin-top: 12px;
  color: #606266;
  font-size: 13px;

  // 比例总和超过100%时，提示文字变红
  &.invalid {
    color: #f56c6c;
    font-weight: 600;
  }
}

/* 规则分组容器 */
.rule-group {
  margin-bottom: 16px;
}

/* 规则分组标题 */
.rule-group-title {
  margin-bottom: 8px;
  color: #303133;
  font-weight: 600;
}
</style>
