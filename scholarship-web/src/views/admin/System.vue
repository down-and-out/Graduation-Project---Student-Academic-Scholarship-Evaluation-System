<template>
  <div class="system-page">
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
    </div>

    <el-tabs v-model="activeTab" class="system-tabs">
      <el-tab-pane label="基本设置" name="basic">
        <el-card shadow="never">
          <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="150px">
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="basicForm.systemName" style="width: 400px" />
            </el-form-item>
            <el-form-item label="系统简称" prop="systemShortName">
              <el-input v-model="basicForm.systemShortName" style="width: 200px" />
            </el-form-item>
            <el-form-item label="当前学期" prop="currentSemester">
              <el-select v-model="basicForm.currentSemester" style="width: 300px">
                <el-option v-for="opt in semesterOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="管理员邮箱" prop="adminEmail">
              <el-input v-model="basicForm.adminEmail" style="width: 300px" />
            </el-form-item>
            <el-form-item label="联系电话" prop="adminPhone">
              <el-input v-model="basicForm.adminPhone" style="width: 200px" />
            </el-form-item>
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
            <el-form-item>
              <el-button type="primary" :loading="savingBasic" @click="handleSaveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="评分权重" name="weight">
        <el-card shadow="never">
          <el-form ref="weightFormRef" :model="weightForm" :rules="weightRules" label-width="200px">
            <el-form-item label="课程成绩权重" prop="courseWeight">
              <el-input-number v-model="weightForm.courseWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <el-form-item label="科研成果权重" prop="researchWeight">
              <el-input-number v-model="weightForm.researchWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <el-form-item label="综合素质权重" prop="comprehensiveWeight">
              <el-input-number v-model="weightForm.comprehensiveWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <el-alert
              title="权重总和必须等于 100%"
              :type="totalWeightAlertType"
              :closable="false"
              style="margin-bottom: 20px"
            />
            <el-form-item>
              <el-button type="primary" :loading="savingWeight" @click="handleSaveWeight">保存权重</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="log">
        <el-card shadow="never">
          <el-form :inline="true" class="search-form">
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
            <el-form-item label="操作人">
              <el-input v-model="logQuery.operator" placeholder="输入用户名" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQueryLog">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="logLoading" :data="logData" border stripe style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="type" label="操作类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getLogTypeText(row.type, row.typeLabel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="操作描述" min-width="200" />
            <el-table-column prop="ip" label="IP 地址" width="140" />
            <el-table-column prop="createTime" label="操作时间" width="180" />
          </el-table>

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
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getEvaluationPage, type EvaluationBatch } from '@/api/evaluation'
import { getOperationLogPage, getSetting, updateSetting } from '@/api/system'
import { LOG_TYPE_LABELS, LOG_TYPE_OPTIONS } from '@/constants/operationLog'
import type { BasicSetting, OperationLog, WeightSetting } from '@/api/system'
import { extractApiData, formatAcademicYearLabel } from '@/utils/helpers'
import { LARGE_QUERY_SIZE } from '@/constants'

type AlertType = 'success' | 'warning' | 'info' | 'error'

interface LogRow {
  operator: string
  type: number | null
  typeLabel: string
  description: string
  ip: string
  createTime: string
}

const CURRENT_YEAR = new Date().getFullYear()
const semesterOptions = ref<Array<{ label: string; value: string }>>([])

const activeTab = ref('basic')
const basicFormRef = ref<FormInstance | null>(null)
const weightFormRef = ref<FormInstance | null>(null)
const logLoading = ref(false)
const savingBasic = ref(false)
const savingWeight = ref(false)

const basicForm = reactive<BasicSetting>({
  systemName: '研究生学业奖学金评定系统',
  systemShortName: '奖学金评定系统',
  currentSemester: `${CURRENT_YEAR}-1`,
  adminEmail: 'admin@example.com',
  adminPhone: '010-12345678',
  announcement: ''
})

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

const weightForm = reactive<WeightSetting>({
  courseWeight: 40,
  researchWeight: 35,
  comprehensiveWeight: 25
})

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

const FULL_YEAR_SEMESTER = 3
const SEMESTER_FORWARD_YEARS = 5

const logData = ref<LogRow[]>([])
const logTotal = ref(0)
const logQuery = reactive({
  current: 1,
  size: 10,
  type: [] as number[],
  operator: ''
})

const totalWeight = computed(() => weightForm.courseWeight + weightForm.researchWeight + weightForm.comprehensiveWeight)
const totalWeightAlertType = computed<AlertType>(() => (totalWeight.value === 100 ? 'success' : 'warning'))

function buildSemesterOption(academicYear: string, semester: number): { label: string; value: string } {
  return {
    label: `${formatAcademicYearLabel(academicYear)}${semester === 1 ? '第一学期' : '第二学期'}`,
    value: `${academicYear}-${semester}`
  }
}

function sortSemesterOptions(options: Array<{ label: string; value: string }>): Array<{ label: string; value: string }> {
  return options.sort((left, right) => {
    const [leftYear, leftSemester] = left.value.split('-')
    const [rightYear, rightSemester] = right.value.split('-')
    const yearDiff = Number.parseInt(rightYear, 10) - Number.parseInt(leftYear, 10)
    if (yearDiff !== 0) return yearDiff
    return Number.parseInt(rightSemester, 10) - Number.parseInt(leftSemester, 10)
  })
}

async function loadSemesterOptions(): Promise<void> {
  try {
    const response = await getEvaluationPage({ current: 1, size: LARGE_QUERY_SIZE })
    const pageData = extractApiData<API.PageResponse<EvaluationBatch>>(response)
    const records = pageData?.records || []
    const optionMap = new Map<string, { label: string; value: string }>()

    let maxAcademicYear = CURRENT_YEAR
    records.forEach(item => {
      if (!item.academicYear || item.semester == null || item.semester === FULL_YEAR_SEMESTER) {
        return
      }
      optionMap.set(`${item.academicYear}-${item.semester}`, buildSemesterOption(item.academicYear, item.semester))
      const numericYear = Number.parseInt(item.academicYear, 10)
      if (!Number.isNaN(numericYear)) {
        maxAcademicYear = Math.max(maxAcademicYear, numericYear)
      }
    })

    for (let year = maxAcademicYear; year <= maxAcademicYear + SEMESTER_FORWARD_YEARS; year += 1) {
      optionMap.set(`${year}-1`, buildSemesterOption(String(year), 1))
      optionMap.set(`${year}-2`, buildSemesterOption(String(year), 2))
    }

    semesterOptions.value = sortSemesterOptions(Array.from(optionMap.values()))
  } catch (error) {
    console.error('加载当前学期选项失败:', error)
    semesterOptions.value = sortSemesterOptions([
      buildSemesterOption(String(CURRENT_YEAR), 1),
      buildSemesterOption(String(CURRENT_YEAR), 2)
    ])
  }
}

function ensureCurrentSemesterOption(): void {
  if (!basicForm.currentSemester) {
    return
  }
  const exists = semesterOptions.value.some(option => option.value === basicForm.currentSemester)
  if (exists) {
    return
  }

  const [academicYear, semesterValue] = basicForm.currentSemester.split('-')
  const semester = Number.parseInt(semesterValue || '', 10)
  const fallbackOption = !academicYear || Number.isNaN(semester)
    ? { label: basicForm.currentSemester, value: basicForm.currentSemester }
    : buildSemesterOption(academicYear, semester)

  semesterOptions.value = sortSemesterOptions([...semesterOptions.value, fallbackOption])
}

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

async function handleSaveWeight(): Promise<void> {
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

function getLogTypeText(type: number | null, typeLabel?: string): string {
  if (typeLabel) {
    return typeLabel
  }
  if (type != null && LOG_TYPE_LABELS[type]) {
    return LOG_TYPE_LABELS[type]
  }
  return type == null ? '-' : String(type)
}

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
    ElMessage.error('查询失败')
    logData.value = []
    logTotal.value = 0
  } finally {
    logLoading.value = false
  }
}

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
    ensureCurrentSemesterOption()
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.error('加载系统设置失败')
  }
}

let refreshTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  void Promise.allSettled([loadSemesterOptions(), loadSettings(), handleQueryLog()])
  // 每 60s 静默刷新系统设置，检测其他管理员的修改
  refreshTimer = setInterval(() => {
    void loadSettings()
  }, 60000)
})

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

.system-tabs .el-tabs__content {
  padding-top: 20px;
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
</style>
