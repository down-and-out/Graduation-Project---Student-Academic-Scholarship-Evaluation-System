<!--
  管理员 - 结果管理页面
  功能：查看评定结果、调整奖项等级、导出结果、统计数据（各级奖学金人数）
-->
<template>
  <div class="results-page">
    <!-- 页面头部：标题 + 导出结果按钮 -->
    <div class="page-header">
      <h2 class="page-title">结果管理</h2>
      <el-button type="success" :loading="exporting" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出结果
      </el-button>
    </div>

    <!-- 搜索表单：学年、学期、关键词、状态筛选 -->
    <el-form :inline="true" class="search-form">
      <!-- 学年筛选 -->
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
      <!-- 学期筛选 -->
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
      <!-- 关键词搜索：学号或姓名 -->
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyword" placeholder="学号/姓名" clearable />
      </el-form-item>
      <!-- 结果状态筛选 -->
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
      <!-- 查询、重置按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计数据卡片：显示各级奖学金人数 -->
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

    <!-- 评定结果数据表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <!-- 序号列 -->
      <el-table-column type="index" label="序号" width="60" />
      <!-- 评定批次列 -->
      <el-table-column prop="batchName" label="评定批次" min-width="180" />
      <!-- 学号列 -->
      <el-table-column prop="studentNo" label="学号" width="130" />
      <!-- 姓名列 -->
      <el-table-column prop="studentName" label="姓名" width="110" />
      <!-- 院系列 -->
      <el-table-column prop="department" label="院系" width="150" />
      <!-- 专业列 -->
      <el-table-column prop="major" label="专业" width="150" />
      <!-- 奖项等级列（以标签形式展示） -->
      <el-table-column label="奖项等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getLevelConfig(row.awardLevel).type">
            {{ getLevelConfig(row.awardLevel).text }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 综合得分列 -->
      <el-table-column prop="totalScore" label="综合得分" width="100" />
      <!-- 排名列（优先显示院系排名，其次专业排名） -->
      <el-table-column label="排名" width="90">
        <template #default="{ row }">
          {{ getDisplayRank(row) }}
        </template>
      </el-table-column>
      <!-- 结果状态列（以标签形式展示） -->
      <el-table-column label="结果状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusConfig(getResultStatusValue(row)).type">
            {{ getStatusConfig(getResultStatusValue(row)).text }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 操作列：查看详情、调整奖项 -->
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
          <el-button link type="primary" @click="handleAdjust(row)">调整</el-button>
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
      @size-change="handleSizeChange"
    />

    <!-- 评定结果详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评定结果详情" width="820px">
      <template v-if="detailData">
        <!-- 基本信息展示 -->
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

        <!-- 得分明细表格 -->
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

    <!-- 调整奖项等级对话框 -->
    <el-dialog v-model="adjustDialogVisible" title="调整奖项等级" width="500px">
      <el-form
        v-if="currentRow"
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
      >
        <!-- 学生姓名（只读显示） -->
        <el-form-item label="学生姓名">
          <span>{{ currentRow.studentName }}</span>
        </el-form-item>
        <!-- 原奖项等级（只读显示） -->
        <el-form-item label="原等级">
          <el-tag :type="getLevelConfig(currentRow.awardLevel).type">
            {{ getLevelConfig(currentRow.awardLevel).text }}
          </el-tag>
        </el-form-item>
        <!-- 调整后奖项等级（可选择） -->
        <el-form-item label="调整后" prop="awardLevel">
          <el-select v-model="adjustForm.awardLevel">
            <el-option label="特等奖学金" :value="1" />
            <el-option label="一等奖学金" :value="2" />
            <el-option label="二等奖学金" :value="3" />
            <el-option label="三等奖学金" :value="4" />
            <el-option label="未获奖" :value="5" />
          </el-select>
        </el-form-item>
        <!-- 调整原因（必填） -->
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
/**
 * 结果管理页面
 * 功能：
 * - 分页查询评定结果列表（支持学年、学期、关键词、状态筛选）
 * - 统计数据卡片：显示各级奖学金获奖人数（基于当前筛选结果的当页数据）
 * - 查看评定结果详情（包括基本信息和得分明细）
 * - 调整奖项等级（用于异议处理）
 * - 导出评定结果为 Excel 文件
 *
 * 注意：统计数据卡片只统计当页数据，不代表全量数据
 */
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
import { extractApiData, formatAcademicYearLabel, isRequestCanceled } from '@/utils/helpers'
import {
  getAwardLevelConfig,
  getResultStatusConfig,
  normalizeResultStatus,
  RESULT_STATUS_OPTIONS,
  type ResultTagType
} from '@/constants/evaluationResult'

defineOptions({ name: 'AdminResults' })

/** 学期选项接口 */
interface SemesterOption {
  label: string
  value: number
}

/** 得分明细行接口 */
interface ScoreDetailRow {
  item: string
  score: number | string
}

/** 查询参数接口 */
interface QueryParams {
  current: number
  size: number
  academicYear: string
  semester: number | null
  keyword: string
  status: number | null
}

/** 调整表单数据结构 */
interface AdjustFormState {
  awardLevel: number | null   // 调整后的奖项等级
  reason: string              // 调整原因
}

// ==================== 状态 ====================

/** 表格加载状态 */
const loading = ref(false)
/** 导出中状态 */
const exporting = ref(false)
/** 调整提交中状态 */
const adjustSubmitting = ref(false)
/** 表格数据列表 */
const tableData = ref<EvaluationResult[]>([])
/** 数据总数（用于分页） */
const total = ref(0)
/** 学年下拉选项 */
const academicYearOptions = ref<string[]>([])
/** 详情对话框是否显示 */
const detailDialogVisible = ref(false)
/** 调整对话框是否显示 */
const adjustDialogVisible = ref(false)
/** 当前操作的行数据（用于调整） */
const currentRow = ref<EvaluationResult | null>(null)
/** 详情数据 */
const detailData = ref<EvaluationResult | null>(null)
/** 调整表单引用 */
const adjustFormRef = ref<FormInstance | null>(null)

/** 结果状态选项（排除"全部"选项） */
const resultStatusOptions = RESULT_STATUS_OPTIONS.filter(option => option.value !== 0)

/** 学期选项列表 */
const semesterOptions: SemesterOption[] = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 }
]

/** 统计数据（基于当页数据） */
const stats = reactive({
  total: 0,         // 获奖总人数
  firstLevel: 0,    // 一等奖学金人数
  secondLevel: 0,   // 二等奖学金人数
  thirdLevel: 0     // 三等奖学金人数
})

// ==================== 查询参数 ====================

/** 查询参数（分页 + 筛选条件） */
const queryParams = reactive<QueryParams>({
  current: 1,       // 当前页码
  size: 10,         // 每页条数
  academicYear: '',  // 学年
  semester: null,   // 学期
  keyword: '',      // 关键词（学号/姓名）
  status: null      // 结果状态
})

/** 调整表单数据 */
const adjustForm = reactive<AdjustFormState>({
  awardLevel: null,
  reason: ''
})

/** 调整表单验证规则 */
const adjustRules: FormRules<AdjustFormState> = {
  awardLevel: [{ required: true, message: '请选择调整后的等级', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

// ==================== 工具方法 ====================

/**
 * 获取奖项等级配置（文本和颜色类型）
 */
function getLevelConfig(level?: number): { text: string; type: ResultTagType } {
  return getAwardLevelConfig(level)
}

/**
 * 获取结果状态配置（文本和颜色类型）
 */
function getStatusConfig(status?: number): { text: string; type: ResultTagType } {
  return getResultStatusConfig(status)
}

/**
 * 获取结果状态值（兼容多种状态字段）
 */
function getResultStatusValue(row: EvaluationResult): number {
  return normalizeResultStatus(row.resultStatus, row.status)
}

/**
 * 获取显示用的排名（优先院系排名，其次专业排名）
 */
function getDisplayRank(row: EvaluationResult): number | string {
  if (row.departmentRank != null) return row.departmentRank
  if (row.majorRank != null) return row.majorRank
  return '-'
}

/**
 * 更新统计数据（基于当页数据）
 * 注意：只统计当页数据，不代表全量数据真实统计
 */
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

/**
 * 构建得分明细表格数据
 */
function buildScoreDetails(row: EvaluationResult): ScoreDetailRow[] {
  return [
    { item: '课程成绩', score: row.courseScore ?? 0 },
    { item: '科研成果', score: row.researchScore ?? 0 },
    { item: '竞赛获奖', score: row.competitionScore ?? 0 },
    { item: '综合素质', score: row.qualityScore ?? 0 }
  ]
}

/**
 * 下载 Blob 文件
 */
function downloadBlob(blob: Blob, fileName: string): void {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

// ==================== 方法 ====================

/**
 * 加载学年下拉选项
 */
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

/**
 * 查询评定结果列表
 */
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
    // 更新统计数据（基于当页）
    updateStats(records)
  } catch (error) {
    console.error('查询失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('查询失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 点击重置按钮：清空筛选条件并重新查询
 */
function handleReset(): void {
  queryParams.academicYear = ''
  queryParams.semester = null
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.current = 1
  handleQuery()
}

/**
 * 每页条数变化时，从第一页重新查询
 */
function handleSizeChange(): void {
  queryParams.current = 1
  handleQuery()
}

/**
 * 点击"查看详情"按钮：获取并显示评定结果详细信息
 */
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

/**
 * 点击"调整"按钮：打开发整对话框，填充当前行数据
 */
function handleAdjust(row: EvaluationResult): void {
  currentRow.value = row
  adjustForm.awardLevel = row.awardLevel ?? null
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

/**
 * 点击"确定"按钮：提交调整表单
 * 需要二次确认，显示调整前后对比
 */
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

/**
 * 点击"导出结果"按钮：导出符合筛选条件的评定结果为 Excel 文件
 */
async function handleExport(): Promise<void> {
  try {
    exporting.value = true
    const blob = await exportResult({
      academicYear: queryParams.academicYear || undefined,
      semester: queryParams.semester ?? undefined
    })
    // 生成文件名后缀（如：_2024-2025_semester_1）
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

// ==================== 生命周期 ====================

/** 组件挂载时：并发加载学年选项、评定结果列表 */
onMounted(async () => {
  await Promise.all([loadAcademicYearOptions(), handleQuery()])
})
</script>

<style scoped lang="scss">
.results-page {
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
    font-size: 18px;
    font-weight: 500;
    color: #303133;
  }
}

/* 搜索表单容器：浅灰背景 + 圆角 */
.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 统计数据卡片行 */
.stats-row {
  margin-bottom: 20px;
}

/* 分页组件：右对齐 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 评定结果详情的描述列表样式 */
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
