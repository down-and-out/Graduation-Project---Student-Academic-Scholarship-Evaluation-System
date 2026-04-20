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
            v-for="option in semesterFormOptions"
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
            v-for="option in statusOptions"
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
          <el-button
            v-else-if="row.status === BATCH_STATUS.REVIEWING"
            link
            type="warning"
            @click="handleStartPublicity(row)"
          >
            开始公示
          </el-button>
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

    <el-dialog v-model="dialogVisible" title="发起评定" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="评定名称" prop="name">
          <el-input v-model="formData.name" placeholder="如：2024-2025学年第一学期学业奖学金评定" />
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-select v-model="formData.academicYear" placeholder="请选择学年" style="width: 100%">
            <el-option
              v-for="option in academicYearOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="formData.semester" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="option in semesterFormOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请开始" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="申请结束" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
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
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  completeEvaluation,
  createEvaluation,
  deleteEvaluation,
  getEvaluationDetail,
  getEvaluationPage,
  startEvaluationApplication,
  startEvaluationPublicity,
  startEvaluationReview,
  type EvaluationBatch as ApiEvaluationBatch
} from '@/api/evaluation'

type BatchStatus = 1 | 2 | 3 | 4 | 5
type TagType = 'success' | 'warning' | 'info' | 'primary' | 'danger'

interface EvaluationForm {
  name: string
  academicYear: string
  semester: number
  startDate: string
  endDate: string
  remark: string
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

const BATCH_STATUS = {
  NOT_STARTED: 1,
  APPLYING: 2,
  REVIEWING: 3,
  PUBLICITY: 4,
  COMPLETED: 5
} as const

const ACADEMIC_YEAR_OPTIONS: OptionItem<string>[] = [
  { label: '2025-2026学年', value: '2025' },
  { label: '2024-2025学年', value: '2024' },
  { label: '2023-2024学年', value: '2023' },
  { label: '2022-2023学年', value: '2022' }
]

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

const academicYearOptions = computed(() => ACADEMIC_YEAR_OPTIONS)
const semesterFormOptions = computed(() => SEMESTER_OPTIONS)
const statusOptions = computed(() => STATUS_OPTIONS)

const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance | null>(null)
const tableData = ref<ApiEvaluationBatch[]>([])

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10,
  academicYears: [],
  semesters: [],
  statuses: []
})

const formData = reactive<EvaluationForm>({
  name: '',
  academicYear: '',
  semester: 1,
  startDate: '',
  endDate: '',
  remark: ''
})

const formRules: FormRules<EvaluationForm> = {
  name: [{ required: true, message: '请输入评定名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择申请开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择申请结束日期', trigger: 'change' }]
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
  if (!academicYear) {
    return formatSemester(semester)
  }
  const nextYear = Number.parseInt(academicYear, 10) + 1
  return `${academicYear}-${nextYear}学年${formatSemester(semester)}`
}

function resetForm(): void {
  Object.assign(formData, {
    name: '',
    academicYear: '',
    semester: 1,
    startDate: '',
    endDate: '',
    remark: ''
  })
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
    const pageData = extractNestedData<API.PageResponse<ApiEvaluationBatch>>(response)
    tableData.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('查询失败:', error)
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
    const detail = extractNestedData<ApiEvaluationBatch>(response)
    if (!detail) return

    await ElMessageBox.alert(
      [
        `评定名称：${detail.name}`,
        `学年学期：${formatAcademicYearSemester(detail.academicYear, detail.semester)}`,
        `申请开始：${detail.startDate || '-'}`,
        `申请结束：${detail.endDate || '-'}`,
        `获奖人数：${detail.winnerCount ?? 0}`,
        `奖学金总额：${detail.totalAmount ?? 0}`,
        `状态：${getStatusText(detail.status)}`,
        detail.remark ? `备注：${detail.remark}` : ''
      ].filter(Boolean).join('<br/>'),
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
    await ElMessageBox.confirm('确定开始评审吗？开始后批次将进入评审中。', '提示', {
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
  try {
    await ElMessageBox.confirm('确定开始公示吗？开始后批次将进入公示中。', '提示', {
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
    await ElMessageBox.confirm('确定完成该批次评定吗？完成后状态将不可逆。', '提示', {
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

  submitting.value = true
  try {
    await createEvaluation({
      name: formData.name,
      academicYear: formData.academicYear,
      semester: formData.semester,
      startDate: formData.startDate,
      endDate: formData.endDate,
      status: BATCH_STATUS.NOT_STARTED,
      remark: formData.remark
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
  handleQuery()
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
</style>
