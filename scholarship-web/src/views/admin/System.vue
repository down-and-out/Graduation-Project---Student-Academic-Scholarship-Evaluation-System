<!--
  管理员 - 系统设置页面
  功能：基本设置（系统名称/学期等）、评分权重配置、操作日志查询
-->
<template>
  <div class="system-page">
    <!-- 页面头部：标题 -->
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
    </div>

    <!-- 标签页容器 -->
    <el-tabs v-model="activeTab" class="system-tabs">
      <!-- ==================== 基本设置 Tab ==================== -->
      <el-tab-pane label="基本设置" name="basic">
        <el-card shadow="never">
          <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="150px">
            <!-- 系统名称 -->
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="basicForm.systemName" style="width: 400px" />
            </el-form-item>
            <!-- 系统简称 -->
            <el-form-item label="系统简称" prop="systemShortName">
              <el-input v-model="basicForm.systemShortName" style="width: 200px" />
            </el-form-item>
            <!-- 当前学期（用于默认值填充） -->
            <el-form-item label="当前学期" prop="currentSemester">
              <el-select v-model="basicForm.currentSemester" style="width: 300px">
                <el-option v-for="opt in semesterOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <!-- 管理员邮箱 -->
            <el-form-item label="管理员邮箱" prop="adminEmail">
              <el-input v-model="basicForm.adminEmail" style="width: 300px" />
            </el-form-item>
            <!-- 联系电话 -->
            <el-form-item label="联系电话" prop="adminPhone">
              <el-input v-model="basicForm.adminPhone" style="width: 200px" />
            </el-form-item>
            <!-- 系统公告（支持最多500字） -->
            <el-form-item label="系统公告" prop="announcement">
              <el-input
                v-model="basicForm.announcement"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                style="width: 500px"
              />
            </el-form-item>
            <!-- 保存按钮 -->
            <el-form-item>
              <el-button type="primary" :loading="savingBasic" @click="handleSaveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 评分权重 Tab ==================== -->
      <el-tab-pane label="评分权重" name="weight">
        <el-card shadow="never">
          <el-form ref="weightFormRef" :model="weightForm" :rules="weightRules" label-width="200px">
            <!-- 课程成绩权重 -->
            <el-form-item label="课程成绩权重" prop="courseWeight">
              <el-input-number v-model="weightForm.courseWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <!-- 科研成果权重 -->
            <el-form-item label="科研成果权重" prop="researchWeight">
              <el-input-number v-model="weightForm.researchWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <!-- 综合素质权重 -->
            <el-form-item label="综合素质权重" prop="comprehensiveWeight">
              <el-input-number v-model="weightForm.comprehensiveWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <!-- 权重总和提示（必须等于100%） -->
            <el-alert
              title="权重总和必须等于 100%"
              :type="totalWeightAlertType"
              :closable="false"
              style="margin-bottom: 20px"
            />
            <!-- 保存按钮 -->
            <el-form-item>
              <el-button type="primary" :loading="savingWeight" @click="handleSaveWeight">保存权重</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 操作日志 Tab ==================== -->
      <el-tab-pane label="操作日志" name="log">
        <el-card shadow="never">
          <!-- 搜索表单：操作类型、操作人筛选 -->
          <el-form :inline="true" class="search-form">
            <!-- 操作类型筛选（支持多选） -->
            <el-form-item label="操作类型">
              <el-select v-model="logQuery.type" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
                <el-option
                  v-for="option in LOG_TYPE_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <!-- 操作人搜索 -->
            <el-form-item label="操作人">
              <el-input v-model="logQuery.operator" placeholder="输入用户名" clearable />
            </el-form-item>
            <!-- 查询按钮 -->
            <el-form-item>
              <el-button type="primary" @click="handleQueryLog">查询</el-button>
            </el-form-item>
          </el-form>

          <!-- 操作日志数据表格 -->
          <el-table v-loading="logLoading" :data="logData" border stripe style="width: 100%">
            <!-- 序号列 -->
            <el-table-column type="index" label="序号" width="60" />
            <!-- 操作人列 -->
            <el-table-column prop="operator" label="操作人" width="120" />
            <!-- 操作类型列（显示标签） -->
            <el-table-column prop="type" label="操作类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getLogTypeText(row.type, row.typeLabel) }}</el-tag>
              </template>
            </el-table-column>
            <!-- 操作描述列 -->
            <el-table-column prop="description" label="操作描述" min-width="200" />
            <!-- IP地址列 -->
            <el-table-column prop="ip" label="IP 地址" width="140" />
            <!-- 操作时间列 -->
            <el-table-column prop="createTime" label="操作时间" width="180" />
          </el-table>

          <!-- 分页组件 -->
          <el-pagination
            v-model:current-page="logQuery.current"
            v-model:page-size="logQuery.size"
            :total="logTotal"
            layout="total, prev, pager, next"
            class="pagination"
            @current-change="handleQueryLog"
            @size-change="handleQueryLog"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * 系统设置页面
 * 功能：
 * - 基本设置：系统名称、简称、当前学期、管理员邮箱/电话、系统公告
 * - 评分权重配置：课程成绩、科研成果、综合素质三项权重（总和必须=100%）
 * - 操作日志查询：按操作类型、操作人筛选，查看系统操作记录
 *
 * 注意：
 * - 基本设置每60秒自动刷新（检测其他管理员的修改）
 * - 操作日志需要手动点击"查询"才会刷新（无自动刷新）
 */
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getEvaluationPage, type EvaluationBatch } from '@/api/evaluation'
import { getOperationLogPage, getSetting, updateSetting } from '@/api/system'
import { LOG_TYPE_LABELS, LOG_TYPE_OPTIONS } from '@/constants/operationLog'
import type { BasicSetting, OperationLog, WeightSetting } from '@/api/system'
import { extractApiData, formatAcademicYearLabel, isRequestCanceled } from '@/utils/helpers'
import { LARGE_QUERY_SIZE } from '@/constants'

/** 标签颜色类型 */
type AlertType = 'success' | 'warning' | 'info' | 'error'

/** 操作日志行接口 */
interface LogRow {
  operator: string
  type: number | null
  typeLabel: string
  description: string
  ip: string
  createTime: string
}

/** 当前年份（用于生成默认学期选项） */
const CURRENT_YEAR = new Date().getFullYear()

// ==================== 状态 ====================

/** 当前激活的标签页 */
const activeTab = ref('basic')
/** 基本设置表单引用 */
const basicFormRef = ref<FormInstance | null>(null)
/** 评分权重表单引用 */
const weightFormRef = ref<FormInstance | null>(null)
/** 操作日志表格加载状态 */
const logLoading = ref(false)
/** 基本设置保存中状态 */
const savingBasic = ref(false)
/** 评分权重保存中状态 */
const savingWeight = ref(false)

// ==================== 基本设置表单 ====================

/** 基本设置表单数据（默认值） */
const basicForm = reactive<BasicSetting>({
  systemName: '研究生学业奖学金评定系统',
  systemShortName: '奖学金评定系统',
  currentSemester: `${CURRENT_YEAR}-1`,
  adminEmail: 'admin@example.com',
  adminPhone: '010-12345678',
  announcement: ''
})

/** 基本设置表单验证规则 */
const basicRules: FormRules<BasicSetting> = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  systemShortName: [{ required: true, message: '请输入系统简称', trigger: 'blur' }],
  currentSemester: [{ required: true, message: '请选择当前学期', trigger: 'change' }],
  adminEmail: [
    { required: true, message: '请输入管理员邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  adminPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^(?:0?[1-9]\d{1,2}-?\d{7,8}|1[3-9]\d{9})$/, message: '请输入正确的电话号码', trigger: 'blur' }
  ],
  announcement: [{ max: 500, message: '公告内容不能超过 500 字', trigger: 'blur' }]
}

// ==================== 评分权重表单 ====================

/** 评分权重表单数据（默认值） */
const weightForm = reactive<WeightSetting>({
  courseWeight: 40,
  researchWeight: 35,
  comprehensiveWeight: 25
})

/** 评分权重表单验证规则 */
const weightRules: FormRules<WeightSetting> = {
  courseWeight: [
    { required: true, message: '请设置权重', trigger: 'change' },
    { type: 'number', message: '权重必须为数字', trigger: 'change' }
  ],
  researchWeight: [
    { required: true, message: '请设置权重', trigger: 'change' },
    { type: 'number', message: '权重必须为数字', trigger: 'change' }
  ],
  comprehensiveWeight: [
    { required: true, message: '请设置权重', trigger: 'change' },
    { type: 'number', message: '权重必须为数字', trigger: 'change' }
  ]
}

// ==================== 操作日志 ====================

/** 全年学期枚举值（用于过滤全年批次） */
const FULL_YEAR_SEMESTER = 3
/** 学期选项往前延伸的年份数 */
const SEMESTER_FORWARD_YEARS = 5

/** 操作日志表格数据 */
const logData = ref<LogRow[]>([])
/** 操作日志总数（用于分页） */
const logTotal = ref(0)
/** 操作日志查询参数 */
const logQuery = reactive({
  current: 1,       // 当前页码
  size: 10,         // 每页条数
  type: [] as number[],  // 操作类型列表
  operator: ''      // 操作人（用户名）
})

/** 学期下拉选项（动态从已有批次中提取，并往前延伸5年） */
const semesterOptions = ref<Array<{ label: string; value: string }>>([])

// ==================== 计算属性 ====================

/**
 * 权重总和（课程成绩 + 科研成果 + 综合素质）
 */
const totalWeight = computed(() => weightForm.courseWeight + weightForm.researchWeight + weightForm.comprehensiveWeight)

/**
 * 权重总和提示的类型（等于100%显示成功，否则显示警告）
 */
const totalWeightAlertType = computed<AlertType>(() => (totalWeight.value === 100 ? 'success' : 'warning'))

// ==================== 工具方法 ====================

/**
 * 构建学期选项（如：2024 + 1 → "2024-2025学年 第一学期"）
 */
function buildSemesterOption(academicYear: string, semester: number): { label: string; value: string } {
  return {
    label: `${formatAcademicYearLabel(academicYear)}${semester === 1 ? '第一学期' : '第二学期'}`,
    value: `${academicYear}-${semester}`
  }
}

/**
 * 对学期选项排序（年份降序，同年份第二学期优先）
 */
function sortSemesterOptions(options: Array<{ label: string; value: string }>): Array<{ label: string; value: string }> {
  return options.sort((left, right) => {
    const [leftYear, leftSemester] = left.value.split('-')
    const [rightYear, rightSemester] = right.value.split('-')
    const yearDiff = Number.parseInt(rightYear, 10) - Number.parseInt(leftYear, 10)
    if (yearDiff !== 0) return yearDiff
    return Number.parseInt(rightSemester, 10) - Number.parseInt(leftSemester, 10)
  })
}

/**
 * 加载学期下拉选项
 * 从已有批次中提取学年学期，并往前延伸5年
 */
async function loadSemesterOptions(): Promise<void> {
  try {
    const response = await getEvaluationPage({ current: 1, size: LARGE_QUERY_SIZE })
    const pageData = extractApiData<API.PageResponse<EvaluationBatch>>(response)
    const records = pageData?.records || []
    const optionMap = new Map<string, { label: string; value: string }>()

    let maxAcademicYear = CURRENT_YEAR
    records.forEach(item => {
      // 跳过全年学期或不完整的批次数据
      if (!item.academicYear || item.semester == null || item.semester === FULL_YEAR_SEMESTER) {
        return
      }
      optionMap.set(`${item.academicYear}-${item.semester}`, buildSemesterOption(item.academicYear, item.semester))
      const numericYear = Number.parseInt(item.academicYear, 10)
      if (!Number.isNaN(numericYear)) {
        maxAcademicYear = Math.max(maxAcademicYear, numericYear)
      }
    })

    // 往前延伸5年的学期选项
    for (let year = maxAcademicYear; year <= maxAcademicYear + SEMESTER_FORWARD_YEARS; year += 1) {
      optionMap.set(`${year}-1`, buildSemesterOption(String(year), 1))
      optionMap.set(`${year}-2`, buildSemesterOption(String(year), 2))
    }

    semesterOptions.value = sortSemesterOptions(Array.from(optionMap.values()))
  } catch (error) {
    console.error('加载当前学期选项失败:', error)
    // 失败时提供默认选项（当前年份的第一/第二学期）
    semesterOptions.value = sortSemesterOptions([
      buildSemesterOption(String(CURRENT_YEAR), 1),
      buildSemesterOption(String(CURRENT_YEAR), 2)
    ])
  }
}

/**
 * 确保当前学期选项存在于列表中
 * 如果当前设置值不在选项列表中，则动态添加
 */
function ensureCurrentSemesterOption(): void {
  if (!basicForm.currentSemester) {
    return
  }
  const exists = semesterOptions.value.some(option => option.value === basicForm.currentSemester)
  if (exists) {
    return
  }

  // 动态添加当前学期到选项列表
  const [academicYear, semesterValue] = basicForm.currentSemester.split('-')
  const semester = Number.parseInt(semesterValue || '', 10)
  const fallbackOption = !academicYear || Number.isNaN(semester)
    ? { label: basicForm.currentSemester, value: basicForm.currentSemester }
    : buildSemesterOption(academicYear, semester)

  semesterOptions.value = sortSemesterOptions([...semesterOptions.value, fallbackOption])
}

// ==================== 事件处理 ====================

/**
 * 点击"保存设置"按钮：提交基本设置
 */
async function handleSaveBasic(): Promise<void> {
  const valid = await basicFormRef.value?.validate().catch(() => false)
  if (!valid) return
  savingBasic.value = true
  try {
    await updateSetting<BasicSetting>('basic', basicForm)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存基本设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingBasic.value = false
  }
}

/**
 * 点击"保存权重"按钮：提交评分权重配置
 * 权重总和必须等于100%才能保存
 */
async function handleSaveWeight(): Promise<void> {
  // 校验权重总和
  if (totalWeight.value !== 100) {
    ElMessage.warning('权重总和必须等于 100%')
    return
  }
  const valid = await weightFormRef.value?.validate().catch(() => false)
  if (!valid) return
  savingWeight.value = true
  try {
    await updateSetting<WeightSetting>('weight', weightForm)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存权重设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingWeight.value = false
  }
}

/**
 * 获取操作类型的显示文本（优先使用 typeLabel，其次使用预定义映射）
 */
function getLogTypeText(type: number | null, typeLabel?: string): string {
  if (typeLabel) {
    return typeLabel
  }
  if (type != null && LOG_TYPE_LABELS[type]) {
    return LOG_TYPE_LABELS[type]
  }
  return type == null ? '-' : String(type)
}

/**
 * 查询操作日志列表
 */
async function handleQueryLog(): Promise<void> {
  logLoading.value = true
  try {
    const response = await getOperationLogPage({
      current: logQuery.current,
      size: logQuery.size,
      operationType: logQuery.type.length > 0 ? logQuery.type : undefined,
      username: logQuery.operator || undefined
    })
    const pageData = extractApiData<API.PageResponse<OperationLog>>(response)
    const records = pageData?.records || []
    logData.value = records.map((item) => ({
      operator: item.operatorName,
      type: item.operationType,
      typeLabel: item.operationTypeLabel || '',
      description: item.description,
      ip: item.operatorIp,
      createTime: item.createTime
    }))
    logTotal.value = pageData?.total || 0
  } catch (error) {
    console.error('查询操作日志失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('查询失败')
    logData.value = []
    logTotal.value = 0
  } finally {
    logLoading.value = false
  }
}

/**
 * 加载系统设置（基本设置 + 评分权重）
 */
async function loadSettings(): Promise<void> {
  try {
    const [basicRes, weightRes] = await Promise.all([
      getSetting<BasicSetting>('basic'),
      getSetting<WeightSetting>('weight')
    ])

    const basicData = extractApiData<BasicSetting>(basicRes)
    const weightData = extractApiData<WeightSetting>(weightRes)

    if (basicData) Object.assign(basicForm, basicData)
    if (weightData) Object.assign(weightForm, weightData)
    // 确保当前学期选项在列表中（如果设置值不在已有选项中则动态添加）
    ensureCurrentSemesterOption()
  } catch (error) {
    console.error('加载设置失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('加载系统设置失败')
  }
}

// ==================== 生命周期 ====================

/** 定时器引用（用于定期刷新基本设置） */
let refreshTimer: ReturnType<typeof setInterval> | null = null

/**
 * 组件挂载时：
 * 1. 并发加载学期选项、系统设置、操作日志
 * 2. 启动定时器：每60秒自动刷新系统设置（检测其他管理员的修改）
 */
onMounted(() => {
  void Promise.allSettled([loadSemesterOptions(), loadSettings(), handleQueryLog()])
  // 每 60s 静默刷新系统设置，检测其他管理员的修改
  refreshTimer = setInterval(() => {
    void loadSettings()
  }, 60000)
})

/**
 * 组件卸载时：清理定时器
 */
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.system-page {
  padding: 20px;
}

/* 页面头部 */
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

/* 标签页内容区域 */
.system-tabs .el-tabs__content {
  padding-top: 20px;
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
</style>
