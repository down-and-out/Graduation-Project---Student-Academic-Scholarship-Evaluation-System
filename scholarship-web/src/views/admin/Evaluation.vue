<!--
  管理员 - 评定管理页面
  管理员可以启动和管理奖学金评定流程
-->
<template>
  <div class="evaluation-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">评定管理</h2>
      <el-button type="primary" @click="handleStart">
        <el-icon><Plus /></el-icon>
        发起评定
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option
            v-for="option in semesterOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
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

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="semester" label="学期" width="180" />
      <el-table-column prop="name" label="评定名称" min-width="200" />
      <el-table-column prop="startDate" label="开始时间" width="160" />
      <el-table-column prop="endDate" label="结束时间" width="160" />
      <el-table-column prop="applicantCount" label="申请人数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="info">未开始</el-tag>
          <el-tag v-else-if="row.status === 1" type="primary">进行中</el-tag>
          <el-tag v-else-if="row.status === 2" type="success">已完成</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button
            link
            type="primary"
            @click="handlePublish(row)"
            :disabled="row.status !== 0"
          >
            发布
          </el-button>
          <el-button
            link
            type="warning"
            @click="handleClose(row)"
            :disabled="row.status !== 1"
          >
            结束
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
    />

    <!-- 发起评定对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发起评定"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="评定名称" prop="name">
          <el-input v-model="formData.name" placeholder="如：2024-2025 学年第一学期学业奖学金评定" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="formData.semester" style="width: 100%">
            <el-option
              v-for="option in semesterOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
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
        <el-button @click="dialogVisible = false" :disabled="submitting">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getEvaluationPage,
  getEvaluationDetail,
  createEvaluation,
  publishEvaluation,
  closeEvaluation,
  deleteEvaluation,
  type EvaluationBatch as ApiEvaluationBatch
} from '@/api/evaluation'
import { formatDate } from '@/utils/helpers'

/**
 * 学年配置 - 可根据需要扩展
 */
const ACADEMIC_YEARS = [
  { label: '2025-2026 学年', value: '2025' },
  { label: '2024-2025 学年', value: '2024' },
  { label: '2023-2024 学年', value: '2023' },
  { label: '2022-2023 学年', value: '2022' }
]

/**
 * 学期选项：1-第一学期，2-第二学期
 */
const SEMESTER_TYPES = [
  { label: '第一学期', value: '1' },
  { label: '第二学期', value: '2' }
]

/**
 * 表单数据接口
 */
interface EvaluationForm {
  name: string
  semester: string
  startDate: string | Date
  endDate: string | Date
  remark: string
}

/**
 * 学期选项
 */
interface SemesterOption {
  label: string
  value: string
}

/**
 * 状态选项
 */
interface StatusOption {
  label: string
  value: number
}

/**
 * 动态生成学期选项列表
 */
const semesterOptions = computed<SemesterOption[]>(() => {
  const options: SemesterOption[] = []
  for (const year of ACADEMIC_YEARS) {
    for (const sem of SEMESTER_TYPES) {
      options.push({
        label: `${year.label}${sem.label}`,
        value: `${year.value}-${sem.value}`
      })
    }
  }
  return options
})

/**
 * 状态选项列表
 */
const statusOptions: StatusOption[] = [
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 }
]

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 提交中状态
 */
const submitting = ref(false)

/**
 * 表格数据
 */
const tableData = ref<ApiEvaluationBatch[]>([])

/**
 * 总记录数
 */
const total = ref(0)

/**
 * 对话框显示状态
 */
const dialogVisible = ref(false)

/**
 * 表单引用
 */
const formRef = ref<FormInstance | null>(null)

/**
 * 查询参数
 */
interface QueryParams {
  current: number
  size: number
  semester: string
  status: number | ''
}

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10,
  semester: '',
  status: ''
})

/**
 * 表单数据
 */
const formData = reactive<EvaluationForm>({
  name: '',
  semester: '',
  startDate: '',
  endDate: '',
  remark: ''
})

/**
 * 表单验证规则
 */
const formRules = reactive<FormRules<EvaluationForm>>({
  name: [{ required: true, message: '请输入评定名称', trigger: 'blur' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
})

/**
 * 查询数据
 */
async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const res = await getEvaluationPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询条件
 */
function handleReset(): void {
  queryParams.semester = ''
  queryParams.status = ''
  queryParams.current = 1
  // 保持 pageSize 不变，只重置页码和筛选条件
  handleQuery()
}

/**
 * 查看评定详情
 * @param row - 行数据
 */
async function handleView(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) {
    ElMessage.error('评定 ID 不存在')
    return
  }

  try {
    const res = await getEvaluationDetail(row.id)
    const detail = res.data

    // 转义 HTML 防止 XSS
    const escapeHtml = (str: string | undefined | null): string => {
      if (!str) return ''
      return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
    }

    const statusMap: Record<number, string> = {
      0: '未开始',
      1: '进行中',
      2: '已完成'
    }

    const content = `
      <div style="padding: 10px; line-height: 1.8;">
        <p><strong>评定名称：</strong>${escapeHtml(detail.name)}</p>
        <p><strong>学期：</strong>${escapeHtml(detail.semester)}</p>
        <p><strong>申请开始：</strong>${detail.startDate}</p>
        <p><strong>申请结束：</strong>${detail.endDate}</p>
        <p><strong>申请人数：</strong>${detail.applicantCount ?? 0}</p>
        <p><strong>状态：</strong>${statusMap[detail.status ?? 0] || '未知'}</p>
        ${detail.remark ? `<p><strong>备注：</strong>${escapeHtml(detail.remark)}</p>` : ''}
      </div>
    `

    await ElMessageBox.alert(content, '评定详情', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    })
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

/**
 * 发起新的评定
 */
function handleStart(): void {
  // 重置表单数据
  Object.assign(formData, {
    name: '',
    semester: '',
    startDate: '',
    endDate: '',
    remark: ''
  })
  dialogVisible.value = true
}

/**
 * 发布评定
 * @param row - 行数据
 */
async function handlePublish(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) {
    ElMessage.error('评定 ID 不存在')
    return
  }

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
    if (error !== 'cancel') {
      console.error('发布失败:', error)
    }
  }
}

/**
 * 结束评定
 * @param row - 行数据
 */
async function handleClose(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) {
    ElMessage.error('评定 ID 不存在')
    return
  }

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
    if (error !== 'cancel') {
      console.error('结束失败:', error)
    }
  }
}

/**
 * 删除评定
 * @param row - 行数据
 */
async function handleDelete(row: ApiEvaluationBatch): Promise<void> {
  if (!row.id) {
    ElMessage.error('评定 ID 不存在')
    return
  }

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
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

/**
 * 提交表单，创建新的评定
 */
async function handleSubmit(): Promise<void> {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 验证结束时间是否晚于开始时间
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
      semester: formData.semester,
      startDate,
      endDate,
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

/**
 * 关闭对话框并重置表单
 */
function handleDialogClose(): void {
  formRef.value?.resetFields()
}

// ========== 生命周期 ==========
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
    font-size: 18px;
    font-weight: 500;
    color: #303133;
    margin: 0;
  }
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
