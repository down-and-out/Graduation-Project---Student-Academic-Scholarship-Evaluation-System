<!--
  管理员 - 研究生信息管理页面
  功能：查询、编辑、删除研究生信息（添加功能已禁用，学生应通过"用户管理"页面创建）
-->
<template>
  <div class="students-page">
    <!-- 页面头部：标题 -->
    <!-- 注意：添加学生按钮已注释禁用，学生应通过"用户管理"页面创建，确保有登录账号 -->
    <div class="page-header">
      <h2 class="page-title">研究生信息管理</h2>
    </div>

    <!-- 搜索表单：关键字、院系、入学年份、学籍状态筛选 -->
    <el-form :inline="true" class="search-form">
      <!-- 关键字搜索：学号或姓名 -->
      <el-form-item label="关键字">
        <el-input v-model="queryParams.keyword" placeholder="学号或姓名" clearable @clear="handleQuery" @keyup.enter="handleQuery" />
      </el-form-item>
      <!-- 院系筛选（支持多选） -->
      <el-form-item label="院系">
        <el-select v-model="queryParams.department" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
          <el-option v-for="opt in departmentOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <!-- 入学年份筛选 -->
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
      <!-- 学籍状态筛选（排除"辍学"状态，不显示在筛选列表中） -->
      <el-form-item label="学籍状态">
        <el-select v-model="queryParams.status" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
          <el-option v-for="opt in STUDENT_STATUS_OPTIONS.filter(item => item.value !== STUDENT_STATUS.DROPPED)" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <!-- 查询、重置按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 研究生信息数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      empty-text="暂无数据"
      style="width: 100%"
    >
      <!-- 多选框列（当前未用于批量操作） -->
      <el-table-column type="selection" width="55" />
      <!-- 序号列 -->
      <el-table-column type="index" label="序号" width="60" />
      <!-- 学号列 -->
      <el-table-column prop="studentNo" label="学号" width="130" />
      <!-- 姓名列 -->
      <el-table-column prop="name" label="姓名" width="100" />
      <!-- 性别列（显示文本而非数值） -->
      <el-table-column prop="gender" label="性别" width="60">
        <template #default="{ row }">
          {{ GENDER_TEXT_MAP[row.gender] || '-' }}
        </template>
      </el-table-column>
      <!-- 院系列 -->
      <el-table-column prop="department" label="院系" width="150" />
      <!-- 专业列 -->
      <el-table-column prop="major" label="专业" width="150" />
      <!-- 入学年份列 -->
      <el-table-column prop="enrollmentYear" label="入学年份" width="100" />
      <!-- 学历列（显示标签文本） -->
      <el-table-column prop="educationLevel" label="学历" width="80">
        <template #default="{ row }">
          {{ EDUCATION_LEVEL_OPTIONS.find(item => item.value === row.educationLevel)?.label || '-' }}
        </template>
      </el-table-column>
      <!-- 学籍状态列（以标签形式展示） -->
      <el-table-column prop="status" label="学籍状态" width="90">
        <template #default="{ row }">
          <el-tag :type="STUDENT_STATUS_TAG_TYPE_MAP[row.status]">
            {{ STUDENT_STATUS_TEXT_MAP[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 操作列：编辑、删除 -->
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" :loading="deletingIds.has(row.id)" @click="handleDelete(row)">删除</el-button>
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

    <!-- 编辑学生信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑学生信息"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <!-- 学号（编辑时禁用） -->
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="formData.studentNo" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <!-- 姓名 -->
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- 性别 -->
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" style="width: 100%">
                <el-option v-for="opt in GENDER_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 身份证号 -->
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="formData.idCard" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- 入学年份 -->
          <el-col :span="12">
            <el-form-item label="入学年份" prop="enrollmentYear">
              <el-input-number v-model="formData.enrollmentYear" :min="2000" :max="CURRENT_YEAR + 1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <!-- 学历层次 -->
          <el-col :span="12">
            <el-form-item label="学历层次" prop="educationLevel">
              <el-select v-model="formData.educationLevel" style="width: 100%">
                <el-option v-for="opt in EDUCATION_LEVEL_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- 院系 -->
          <el-col :span="12">
            <el-form-item label="院系" prop="department">
              <el-input v-model="formData.department" />
            </el-form-item>
          </el-col>
          <!-- 专业 -->
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="formData.major" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 研究方向 -->
        <el-form-item label="研究方向" prop="direction">
          <el-input v-model="formData.direction" />
        </el-form-item>
        <!-- 学籍状态 -->
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
/**
 * 研究生信息管理页面
 * 功能：
 * - 分页查询研究生信息（支持关键字、院系、入学年份、学籍状态筛选）
 * - 编辑研究生信息（添加功能已禁用）
 * - 删除研究生信息（同时删除对应的登录账号）
 *
 * 注意：添加学生功能已禁用，学生应通过"用户管理"页面创建，确保有登录账号
 */
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

/** 当前年份（用于入学年份默认值和最大值限制） */
const CURRENT_YEAR = new Date().getFullYear()

// ==================== 状态 ====================

/** 院系列表选项（从后端加载） */
const departmentOptions = ref([])

/** 表格加载状态 */
const loading = ref(false)
/** 当前正在删除的学生ID集合（用于显示删除按钮的加载状态） */
const deletingIds = ref(new Set())
/** 表格数据列表 */
const tableData = ref([])
/** 数据总数（用于分页） */
const total = ref(0)
/** 编辑对话框是否显示 */
const dialogVisible = ref(false)
/** 是否为编辑模式 */
const isEdit = ref(false)
/** 表单提交中状态（防止重复提交） */
const submitting = ref(false)
/** 表单引用（用于验证） */
const formRef = ref(null)

// ==================== 查询参数 ====================

/**
 * 查询参数（分页 + 筛选条件）
 * 注意：使用 'all' 作为"全部"选项的值，避免 Element Plus 空字符串 bug
 */
const queryParams = reactive({
  current: 1,           // 当前页码
  size: 10,             // 每页条数
  keyword: '',          // 关键字（学号或姓名）
  department: [],      // 院系列表
  enrollmentYear: undefined,  // 入学年份
  status: []            // 学籍状态列表
})

// ==================== 表单数据 ====================

/** 表单数据（用于编辑学生信息） */
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

// ==================== 表单验证规则 ====================

/** 表单验证规则 */
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

// ==================== 方法 ====================

/**
 * 重置表单数据为默认值
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
 * 查询研究生信息列表
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

/**
 * 加载院系列表选项（用于筛选下拉框）
 */
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

/** 防抖查询（300ms），避免输入时频繁请求 */
const debouncedQuery = debounce(() => {
  handleQuery()
}, 300)

/**
 * 监听关键字变化，自动触发查询（防抖）
 * 注意：页码重置为1
 */
watch(
  () => queryParams.keyword,
  () => {
    queryParams.current = 1
    debouncedQuery()
  }
)

/**
 * 点击重置按钮：清空所有筛选条件并重新查询
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
 * 点击编辑按钮：以当前行数据填充表单并打开对话框
 * 使用 Object.keys 过滤，避免 undefined 覆盖表单数据
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
 * 点击删除按钮：删除学生信息
 * 注意：此操作将同时删除该学生的登录账号，不可恢复
 * 需要二次确认
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
 * 点击确定按钮：提交表单（仅支持编辑）
 * 添加功能已禁用
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
 * 对话框关闭时的处理：重置表单验证状态
 */
function handleDialogClose() {
  formRef.value?.resetFields()
}

// ==================== 生命周期 ====================

/** 组件挂载时：并发加载院系选项、研究生信息列表 */
onMounted(() => {
  void Promise.allSettled([loadDepartmentOptions(), handleQuery()])
})

/** 组件卸载时：取消未完成的防抖查询 */
onUnmounted(() => {
  debouncedQuery.cancel()
})
</script>

<style scoped>
.students-page {
  padding: 20px;
}

/* 页面头部：标题 */
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

/* 搜索表单容器：浅灰背景 + 圆角 */
.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 分页组件：右对齐 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
