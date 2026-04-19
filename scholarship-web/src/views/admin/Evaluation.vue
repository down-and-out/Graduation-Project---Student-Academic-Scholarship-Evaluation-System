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
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option v-for="option in semesterOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
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
      <el-table-column prop="name" label="评定名称" min-width="200" />
      <el-table-column prop="startDate" label="开始时间" width="160" />
      <el-table-column prop="endDate" label="结束时间" width="160" />
      <el-table-column prop="winnerCount" label="获奖人数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="info">未开始</el-tag>
          <el-tag v-else-if="row.status === 2" type="primary">申请中</el-tag>
          <el-tag v-else-if="row.status === 3" type="warning">评审中</el-tag>
          <el-tag v-else-if="row.status === 4" type="success">公示中</el-tag>
          <el-tag v-else-if="row.status === 5" type="success">已完成</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" :disabled="row.status !== 1" @click="handlePublish(row)">发布</el-button>
          <el-button link type="warning" :disabled="row.status !== 2 && row.status !== 3" @click="handleClose(row)">结束</el-button>
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
    />

    <el-dialog v-model="dialogVisible" title="发起评定" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="评定名称" prop="name">
          <el-input v-model="formData.name" placeholder="如：2024-2025 学年第一学期学业奖学金评定" />
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-select v-model="formData.academicYear" placeholder="请选择学年" style="width: 100%">
            <el-option v-for="year in academicYearOptions" :key="year.value" :label="year.label" :value="year.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="formData.semester" placeholder="请选择学期" style="width: 100%">
            <el-option v-for="option in semesterFormOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请开始" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="申请结束" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
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
  closeEvaluation,
  createEvaluation,
  deleteEvaluation,
  getEvaluationDetail,
  getEvaluationPage,
  publishEvaluation,
  type EvaluationBatch as ApiEvaluationBatch
} from '@/api/evaluation'
import { formatDate } from '@/utils/helpers'

interface EvaluationForm {
  name: string
  academicYear: string
  semester: number
  startDate: string | Date
  endDate: string | Date
  remark: string
}

interface QueryParams {
  current: number
  size: number
  semester: string
  status: number | undefined
}

interface OptionItem {
  label: string
  value: string | number
}

const ACADEMIC_YEARS = [
  { label: '2025-2026 学年', value: '2025' },
  { label: '2024-2025 学年', value: '2024' },
  { label: '2023-2024 学年', value: '2023' },
  { label: '2022-2023 学年', value: '2022' }
]

const SEMESTER_TYPES = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 },
  { label: '全年', value: 3 }
]

const academicYearOptions = ACADEMIC_YEARS
const semesterFormOptions = SEMESTER_TYPES

const semesterOptions = computed<OptionItem[]>(() => {
  const options: OptionItem[] = []
  for (const year of ACADEMIC_YEARS) {
    for (const semester of SEMESTER_TYPES) {
      options.push({
        label: `${year.label}${semester.label}`,
        value: `${year.value}-${semester.value}`
      })
    }
  }
  return options
})

const statusOptions: Array<{ label: string; value: number }> = [
  { label: '未开始', value: 1 },
  { label: '申请中', value: 2 },
  { label: '评审中', value: 3 },
  { label: '公示中', value: 4 },
  { label: '已完成', value: 5 }
]

const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance | null>(null)
const tableData = ref<ApiEvaluationBatch[]>([])

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10,
  semester: '',
  status: undefined
})

const formData = reactive<EvaluationForm>({
  name: '',
  academicYear: '',
  semester: 0,
  startDate: '',
  endDate: '',
  remark: ''
})

const formRules: FormRules<EvaluationForm> = {
  name: [{ required: true, message: '请输入评定名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
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

function formatSemester(semester: number | null | undefined): string {
  if (semester == null) return '-'
  return SEMESTER_TYPES.find((item) => item.value === semester)?.label || String(semester)
}

function formatAcademicYearSemester(academicYear: string | null | undefined, semester: number | null | undefined): string {
  if (!academicYear && semester == null) return '-'
  const yearText = academicYear ? `${academicYear}-${Number.parseInt(academicYear, 10) + 1} 学年` : ''
  const semesterText = formatSemester(semester)
  return yearText && semesterText !== '-' ? `${yearText}${semesterText}` : (yearText || semesterText)
}

function resetForm(): void {
  Object.assign(formData, {
    name: '',
    academicYear: '',
    semester: 0,
    startDate: '',
    endDate: '',
    remark: ''
  })
}

async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const response = await getEvaluationPage(queryParams)
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
  queryParams.semester = ''
  queryParams.status = undefined
  queryParams.current = 1
  handleQuery()
}

async function handleView(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  const response = await getEvaluationDetail(row.id)
  const detail = extractNestedData<ApiEvaluationBatch>(response)
  if (!detail) return

  await ElMessageBox.alert(
    [
      `评定名称：${detail.name}`,
      `学年学期：${formatAcademicYearSemester(detail.academicYear, detail.semester)}`,
      `申请开始：${detail.startDate}`,
      `申请结束：${detail.endDate}`,
      `获奖人数：${detail.winnerCount ?? 0}`,
      `状态：${statusOptions.find((item) => item.value === detail.status)?.label || '未知'}`,
      detail.remark ? `备注：${detail.remark}` : ''
    ].filter(Boolean).join('<br/>'),
    '评定详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

function handleStart(): void {
  resetForm()
  dialogVisible.value = true
}

async function handlePublish(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定要发布该评定吗？发布后学生即可开始申请。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await publishEvaluation(row.id)
    ElMessage.success('发布成功')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') console.error('发布失败:', error)
  }
}

async function handleClose(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定要结束该评定吗？结束后将停止接收新的申请。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await closeEvaluation(row.id)
    ElMessage.success('已结束评定')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') console.error('结束失败:', error)
  }
}

async function handleDelete(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定要删除该评定吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteEvaluation(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const startDate = formData.startDate instanceof Date
    ? formatDate(formData.startDate, 'YYYY-MM-DD HH:mm:ss')
    : formData.startDate
  const endDate = formData.endDate instanceof Date
    ? formatDate(formData.endDate, 'YYYY-MM-DD HH:mm:ss')
    : formData.endDate

  if (!startDate || !endDate || new Date(endDate) <= new Date(startDate)) {
    ElMessage.error('结束时间必须晚于开始时间')
    return
  }

  submitting.value = true
  try {
    await createEvaluation({
      name: formData.name,
      academicYear: formData.academicYear,
      semester: formData.semester,
      startDate,
      endDate,
      status: 1,
      remark: formData.remark
    })
    ElMessage.success('评定创建成功')
    dialogVisible.value = false
    await handleQuery()
  } catch (error) {
    console.error('提交失败:', error)
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
