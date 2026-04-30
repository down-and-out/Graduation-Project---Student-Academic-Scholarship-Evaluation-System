<template>
  <div class="students-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">研究生信息管理</h2>
      <!-- 添加学生按钮已隐藏：学生应通过"用户管理"页面创建，确保有登录账号 -->
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="关键字">
        <el-input v-model="queryParams.keyword" placeholder="学号或姓名" clearable @clear="handleQuery" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="院系">
        <el-select v-model="queryParams.department" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
          <el-option v-for="opt in departmentOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="入学年份">
        <el-input-number
          v-model="queryParams.enrollmentYear"
          :min="2000"
          :max="CURRENT_YEAR + 1"
          :step="1"
          :precision="0"
          controls-position="right"
          placeholder="请输入入学年份"
        />
      </el-form-item>
      <el-form-item label="学籍状态">
        <el-select v-model="queryParams.status" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
          <el-option v-for="opt in STUDENT_STATUS_OPTIONS.filter(item => item.value !== STUDENT_STATUS.DROPPED)" :key="opt.value" :label="opt.label" :value="opt.value" />
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
      empty-text="暂无数据"
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="60">
        <template #default="{ row }">
          {{ GENDER_TEXT_MAP[row.gender] || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column prop="major" label="专业" width="150" />
      <el-table-column prop="enrollmentYear" label="入学年份" width="100" />
      <el-table-column prop="educationLevel" label="学历" width="80">
        <template #default="{ row }">
          {{ EDUCATION_LEVEL_OPTIONS.find(item => item.value === row.educationLevel)?.label || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="学籍状态" width="90">
        <template #default="{ row }">
          <el-tag :type="STUDENT_STATUS_TAG_TYPE_MAP[row.status]">
            {{ STUDENT_STATUS_TEXT_MAP[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" :loading="deletingIds.has(row.id)" @click="handleDelete(row)">删除</el-button>
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
      @current-change="handleQuery"
      @size-change="handleQuery"
    />

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑学生信息"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="formData.studentNo" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" style="width: 100%">
                <el-option v-for="opt in GENDER_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="formData.idCard" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入学年份" prop="enrollmentYear">
              <el-input-number v-model="formData.enrollmentYear" :min="2000" :max="CURRENT_YEAR + 1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历层次" prop="educationLevel">
              <el-select v-model="formData.educationLevel" style="width: 100%">
                <el-option v-for="opt in EDUCATION_LEVEL_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="院系" prop="department">
              <el-input v-model="formData.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="formData.major" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="研究方向" prop="direction">
          <el-input v-model="formData.direction" />
        </el-form-item>
        <el-form-item label="学籍状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio v-for="opt in STUDENT_STATUS_OPTIONS" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentPage, updateStudent, deleteStudent } from '@/api/student'
import { getDepartments } from '@/api/basicData'
import { debounce, isRequestCanceled } from '@/utils/helpers'
import {
  EDUCATION_LEVEL,
  EDUCATION_LEVEL_OPTIONS,
  GENDER,
  GENDER_OPTIONS,
  GENDER_TEXT_MAP,
  STUDENT_STATUS,
  STUDENT_STATUS_OPTIONS,
  STUDENT_STATUS_TAG_TYPE_MAP,
  STUDENT_STATUS_TEXT_MAP
} from '@/constants/user'

defineOptions({ name: 'AdminStudents' })

const CURRENT_YEAR = new Date().getFullYear()

// 院系选项
const departmentOptions = ref([])

// 状态
const loading = ref(false)
const deletingIds = ref(new Set())
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 查询参数（使用 'all' 作为"全部"选项的值，避免 Element Plus 空字符串 bug）
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  department: [],
  enrollmentYear: undefined,
  status: []
})

// 表单数据
const formData = reactive({
  id: null,
  studentNo: '',
  name: '',
  gender: GENDER.MALE,
  idCard: '',
  enrollmentYear: CURRENT_YEAR,
  educationLevel: EDUCATION_LEVEL.MASTER,
  department: '',
  major: '',
  direction: '',
  status: STUDENT_STATUS.ACTIVE
})

// 验证规则
const formRules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{8,12}$/, message: '学号格式不正确（8-12 位数字）', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  enrollmentYear: [
    { required: true, message: '请输入入学年份', trigger: 'blur' },
    { type: 'number', message: '入学年份必须为数字', trigger: 'blur' }
  ],
  educationLevel: [{ required: true, message: '请选择学历层次', trigger: 'change' }],
  department: [{ required: true, message: '请输入院系', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  status: [{ required: true, message: '请选择学籍状态', trigger: 'change' }]
}

// 方法

/**
 * 重置表单数据
 */
function resetFormData() {
  return {
    id: null,
    studentNo: '',
    name: '',
    gender: GENDER.MALE,
    idCard: '',
    enrollmentYear: CURRENT_YEAR,
    educationLevel: EDUCATION_LEVEL.MASTER,
    department: '',
    major: '',
    direction: '',
    status: STUDENT_STATUS.ACTIVE
  }
}

/**
 * 查询数据
 */
async function handleQuery() {
  loading.value = true
  try {
    // 将 'all' 转换为 undefined，不传给后端
    const params = {
      ...queryParams,
      department: queryParams.department.length > 0 ? queryParams.department : undefined,
      enrollmentYear: queryParams.enrollmentYear === undefined ? undefined : String(queryParams.enrollmentYear),
      status: queryParams.status.length > 0 ? queryParams.status : undefined
    }
    const res = await getStudentPage(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) {
    if (isRequestCanceled(error)) return
    ElMessage.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function loadDepartmentOptions() {
  try {
    const res = await getDepartments()
    const data = res.data?.data || res.data || []
    departmentOptions.value = data.map(item => ({ label: item, value: item }))
  } catch (error) {
    console.error('加载院系列表失败:', error)
    departmentOptions.value = []
  }
}

// 防抖查询
const debouncedQuery = debounce(() => {
  handleQuery()
}, 300)

// 监听关键字变化
watch(
  () => queryParams.keyword,
  () => {
    queryParams.current = 1
    debouncedQuery()
  }
)

/**
 * 重置查询
 */
function handleReset() {
  queryParams.keyword = ''
  queryParams.department = []
  queryParams.enrollmentYear = undefined
  queryParams.status = []
  queryParams.current = 1
  handleQuery()
}

/**
 * 添加学生功能已禁用
 * 学生应通过"用户管理"页面创建，确保有登录账号
 */
// function handleAdd() {
//   isEdit.value = false
//   Object.assign(formData, resetFormData())
//   dialogVisible.value = true
// }

/**
 * 编辑学生
 */
function handleEdit(row) {
  isEdit.value = true
  // 只复制存在的属性，避免 undefined 覆盖表单数据
  Object.keys(row).forEach(key => {
    if (formData.hasOwnProperty(key) && row[key] !== undefined) {
      formData[key] = row[key]
    }
  })
  dialogVisible.value = true
}

/**
 * 删除学生
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      '确定要删除该学生信息吗？此操作将同时删除该学生的登录账号，不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    deletingIds.value.add(row.id)
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    deletingIds.value.delete(row.id)
  }
}

/**
 * 提交表单
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 添加功能已禁用，只允许编辑
    if (isEdit.value) {
      await updateStudent(formData)
      ElMessage.success('修改成功')
      dialogVisible.value = false
      handleQuery()
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭对话框
 */
function handleDialogClose() {
  formRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  void Promise.allSettled([loadDepartmentOptions(), handleQuery()])
})

onUnmounted(() => {
  debouncedQuery.cancel()
})
</script>

<style scoped>
.students-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header .page-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0;
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
