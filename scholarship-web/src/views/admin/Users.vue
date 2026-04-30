<template>
  <div class="users-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input
          v-model="queryParams.keyword"
          placeholder="用户名或姓名"
          clearable
          @clear="handleQuery"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="queryParams.userType" placeholder="请选择" multiple clearable>
          <el-option v-for="opt in ROLE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="院系">
        <el-select
          v-model="queryParams.department"
          placeholder="请选择"
          multiple
          clearable
          collapse-tags
          collapse-tags-tooltip
        >
          <el-option v-for="opt in departmentOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" multiple clearable>
          <el-option v-for="opt in USER_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div v-if="selectedRows.length > 0" class="batch-toolbar">
      <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
      <el-button type="danger" size="small" :loading="batchDeleting" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>

    <el-table
      ref="selectionRef"
      v-loading="loading"
      :data="tableData"
      border
      stripe
      empty-text="暂无数据"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="120">
        <template #default="{ row }">
          {{ row.realName || row.name }}
        </template>
      </el-table-column>
      <el-table-column prop="userType" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="getUserTypeTagType(row.userType)">
            {{ getUserTypeText(row.userType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" label="院系" width="160" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getUserStatusTagType(row.status)">
            {{ getUserStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button link type="primary" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button link type="warning" @click="handleResetPassword(row)">
            <el-icon><RefreshLeft /></el-icon>
            重置密码
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="formData.username"
                :disabled="isEdit"
                placeholder="3-20 个字符，字母开头"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="userType">
              <el-select v-model="formData.userType" style="width: 100%">
                <el-option v-for="opt in ROLE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="院系" prop="department">
              <el-input v-model="formData.department" placeholder="请输入院系" />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="isStudentUserType(formData.userType)">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学号" prop="studentNo">
                <el-input v-model="formData.studentNo" placeholder="请输入学号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="formData.gender" style="width: 100%" placeholder="请选择性别">
                  <el-option v-for="opt in GENDER_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="入学年份" prop="enrollmentYear">
                <el-select v-model="formData.enrollmentYear" style="width: 100%" placeholder="请选择入学年份">
                  <el-option v-for="year in enrollmentYearOptions" :key="year" :label="String(year)" :value="year" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学历层次" prop="educationLevel">
                <el-select v-model="formData.educationLevel" style="width: 100%" placeholder="请选择学历层次">
                  <el-option
                    v-for="opt in EDUCATION_LEVEL_OPTIONS"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="培养方式" prop="trainingMode">
                <el-select v-model="formData.trainingMode" style="width: 100%" placeholder="请选择培养方式">
                  <el-option
                    v-for="opt in TRAINING_MODE_OPTIONS"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学籍状态" prop="studentStatus">
                <el-select v-model="formData.studentStatus" style="width: 100%" placeholder="请选择学籍状态">
                  <el-option
                    v-for="opt in STUDENT_STATUS_OPTIONS"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="专业" prop="major">
                <el-input v-model="formData.major" placeholder="请输入专业" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="籍贯" prop="nativePlace">
                <el-input v-model="formData.nativePlace" placeholder="请输入籍贯" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="家庭住址" prop="address">
                <el-input v-model="formData.address" placeholder="请输入家庭住址" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="USER_STATUS.ENABLED">启用</el-radio>
            <el-radio :value="USER_STATUS.DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="用户详情" width="700px">
      <el-descriptions v-if="viewData" :column="2" border>
        <el-descriptions-item label="用户 ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewData.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.realName || viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getUserTypeTagType(viewData.userType)">
            {{ getUserTypeText(viewData.userType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="院系">{{ viewData.department }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ viewData.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getUserStatusTagType(viewData.status)">
            {{ getUserStatusText(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(viewData.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, RefreshLeft, View } from '@element-plus/icons-vue'
import { batchDeleteUsers, addUser, deleteUser, getUserPage, resetPassword, updateUser } from '@/api/user'
import { getDepartments } from '@/api/basicData'
import { getStudentPage } from '@/api/student'
import {
  EDUCATION_LEVEL_OPTIONS,
  GENDER_OPTIONS,
  ROLE_OPTIONS,
  STUDENT_STATUS_OPTIONS,
  TRAINING_MODE_OPTIONS,
  USER_STATUS,
  USER_STATUS_MAP,
  USER_STATUS_OPTIONS,
  USER_TYPE,
  USER_TYPE_MAP
} from '@/constants/user'
import { debounce, deepClone, isValidEmail, isValidPhone, isValidUsername } from '@/utils/helpers'
import { LARGE_QUERY_SIZE } from '@/constants'

defineOptions({ name: 'AdminUsers' })

const ENROLLMENT_YEAR_RANGE = 10
const currentYear = new Date().getFullYear()
const enrollmentYearOptions = ref(Array.from({ length: ENROLLMENT_YEAR_RANGE }, (_, index) => currentYear - index))

function isStudentUserType(userType) {
  return Number(userType) === USER_TYPE.STUDENT
}

function getUserTypeText(userType) {
  if (userType === undefined || userType === null || userType === '') return ''
  return USER_TYPE_MAP[userType]?.text || '未知'
}

function getUserTypeTagType(userType) {
  if (userType === undefined || userType === null || userType === '') return ''
  return USER_TYPE_MAP[userType]?.type || ''
}

function getUserStatusText(status) {
  if (status === undefined || status === null || status === '') return ''
  return USER_STATUS_MAP[status]?.text || '未知'
}

function getUserStatusTagType(status) {
  if (status === undefined || status === null || status === '') return ''
  return USER_STATUS_MAP[status]?.type || ''
}

function formatDateTime(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

function validateStudentRequired(value, callback, message) {
  if (!isStudentUserType(formData.userType)) {
    callback()
    return
  }

  if (value === undefined || value === null || value === '') {
    callback(new Error(message))
    return
  }

  if (typeof value === 'string' && !value.trim()) {
    callback(new Error(message))
    return
  }

  callback()
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const departmentOptions = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const batchDeleting = ref(false)
const selectedRows = ref([])
const selectionRef = ref(null)
const formRef = ref(null)
const viewData = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  department: [],
  userType: [],
  status: []
})

const defaultFormData = {
  id: null,
  username: '',
  realName: '',
  userType: USER_TYPE.STUDENT,
  department: '',
  studentNo: '',
  gender: 1,
  idCard: '',
  enrollmentYear: currentYear,
  educationLevel: 1,
  trainingMode: 1,
  nativePlace: '',
  address: '',
  major: '',
  studentStatus: 1,
  phone: '',
  email: '',
  status: USER_STATUS.ENABLED
}

const formData = reactive({ ...defaultFormData })

const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '添加用户'))

const formRules = computed(() => ({
  username: [
    { required: !isEdit.value, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && !isValidUsername(value)) {
          callback(new Error('用户名只能包含字母、数字和下划线，且必须以字母开头，长度为 3-20 位'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择角色', trigger: 'change' }],
  department: [{ required: true, message: '请输入院系', trigger: 'blur' }],
  studentNo: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请输入学号'),
      trigger: 'blur'
    }
  ],
  gender: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请选择性别'),
      trigger: 'change'
    }
  ],
  enrollmentYear: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请选择入学年份'),
      trigger: 'change'
    }
  ],
  educationLevel: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请选择学历层次'),
      trigger: 'change'
    }
  ],
  trainingMode: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请选择培养方式'),
      trigger: 'change'
    }
  ],
  major: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请输入专业'),
      trigger: 'blur'
    }
  ],
  studentStatus: [
    {
      validator: (rule, value, callback) => validateStudentRequired(value, callback, '请选择学籍状态'),
      trigger: 'change'
    }
  ],
  idCard: [
    {
      validator: (rule, value, callback) => {
        if (!isStudentUserType(formData.userType) || !value) {
          callback()
          return
        }

        if (String(value).length !== 18) {
          callback(new Error('身份证号应为 18 位'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isValidPhone(value)) {
          callback(new Error('请输入正确的手机号'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isValidEmail(value)) {
          callback(new Error('请输入正确的邮箱地址'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

function getQueryParams() {
  return {
    current: queryParams.current,
    size: queryParams.size,
    keyword: queryParams.keyword || undefined,
    department: queryParams.department.length > 0 ? queryParams.department : undefined,
    userType: queryParams.userType.length > 0 ? queryParams.userType : undefined,
    status: queryParams.status.length > 0 ? queryParams.status : undefined
  }
}

async function loadDepartmentOptions() {
  try {
    const res = await getDepartments()
    const data = res.data?.data || res.data || []
    departmentOptions.value = data.map(item => ({ label: item, value: item }))
  } catch (error) {
    console.error('加载院系列表失败:', error)
    ElMessage.error('加载院系列表失败')
  }
}

async function loadEnrollmentYearOptions() {
  try {
    const response = await getStudentPage({ current: 1, size: LARGE_QUERY_SIZE })
    const pageData = response.data?.data || response.data || {}
    const years = (pageData.records || [])
      .map(item => Number(item.enrollmentYear))
      .filter(year => !Number.isNaN(year) && year > 0)

    if (years.length === 0) {
      enrollmentYearOptions.value = Array.from({ length: ENROLLMENT_YEAR_RANGE }, (_, index) => currentYear - index)
      return
    }

    const minYear = Math.min(...years)
    const maxYear = Math.max(...years)
    enrollmentYearOptions.value = Array.from(
      { length: maxYear - minYear + 1 },
      (_, index) => maxYear - index
    )
  } catch (error) {
    console.error('加载入学年份选项失败:', error)
    ElMessage.error('加载入学年份选项失败')
    enrollmentYearOptions.value = Array.from({ length: ENROLLMENT_YEAR_RANGE }, (_, index) => currentYear - index)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage(getQueryParams())
    const pageData = res.data?.data || res.data || {}
    tableData.value = pageData.records || []
    total.value = pageData.total || 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.current = 1
  loadData()
}

function handleReset() {
  queryParams.current = 1
  queryParams.keyword = ''
  queryParams.department = []
  queryParams.userType = []
  queryParams.status = []
  loadData()
}

const debouncedQuery = debounce(handleQuery, 300)

watch(
  () => queryParams.keyword,
  () => {
    queryParams.current = 1
    debouncedQuery()
  }
)

watch(
  () => formData.userType,
  userType => {
    if (isStudentUserType(userType)) {
      return
    }

    Object.assign(formData, {
      studentNo: '',
      gender: 1,
      idCard: '',
      enrollmentYear: currentYear,
      educationLevel: 1,
      trainingMode: 1,
      nativePlace: '',
      address: '',
      major: '',
      studentStatus: 1
    })
  }
)

function handleSizeChange(size) {
  queryParams.size = size
  queryParams.current = 1
  loadData()
}

function handleCurrentChange(current) {
  queryParams.current = current
  loadData()
}

function handleSelectionChange(selection) {
  selectedRows.value = selection.map(item => deepClone(item))
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, deepClone(defaultFormData))
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, deepClone(defaultFormData), deepClone(row), {
    realName: row.realName || row.name || ''
  })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = deepClone(row)
  viewDialogVisible.value = true
}

function handleResetPassword(row) {
  const displayName = row.realName || row.name || '该用户'
  ElMessageBox.confirm(
    `确定要重置用户”${displayName}”的密码吗？\n\n重置后密码将恢复为系统默认密码。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      await resetPassword(row.id)
      ElMessage.success('密码重置成功，已恢复为系统默认密码')
      loadData()
    })
    .catch(error => {
      if (error !== 'cancel' && error !== 'close') {
        console.error('重置密码失败:', error)
        ElMessage.error('重置密码失败，请稍后重试')
      }
    })
}

function handleDelete(row) {
  const displayName = row.realName || row.name || '该用户'
  ElMessageBox.confirm(`确定要删除用户“${displayName}”吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(error => {
      if (error !== 'cancel' && error !== 'close') {
        console.error('删除失败:', error)
        ElMessage.error('删除失败，请稍后重试')
      }
    })
}

async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      batchDeleting.value = true
      const ids = selectedRows.value.map(item => item.id)
      await batchDeleteUsers(ids)
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      selectionRef.value?.clearSelection()
      loadData()
    })
    .catch(error => {
      if (error !== 'cancel' && error !== 'close') {
        console.error('批量删除失败:', error)
        ElMessage.error('批量删除失败，请稍后重试')
      }
    })
    .finally(() => {
      batchDeleting.value = false
    })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser({
        id: formData.id,
        username: formData.username,
        realName: formData.realName,
        userType: formData.userType,
        department: formData.department,
        phone: formData.phone,
        email: formData.email,
        status: formData.status
      })
    } else {
      await addUser({
        user: {
          username: formData.username,
          realName: formData.realName,
          userType: formData.userType,
          department: formData.department,
          phone: formData.phone,
          email: formData.email,
          status: formData.status
        },
        major: isStudentUserType(formData.userType) ? formData.major : undefined,
        studentNo: isStudentUserType(formData.userType) ? formData.studentNo : undefined,
        gender: isStudentUserType(formData.userType) ? formData.gender : undefined,
        idCard: isStudentUserType(formData.userType) ? formData.idCard || undefined : undefined,
        enrollmentYear: isStudentUserType(formData.userType) ? formData.enrollmentYear : undefined,
        educationLevel: isStudentUserType(formData.userType) ? formData.educationLevel : undefined,
        trainingMode: isStudentUserType(formData.userType) ? formData.trainingMode : undefined,
        nativePlace: isStudentUserType(formData.userType) ? formData.nativePlace || undefined : undefined,
        address: isStudentUserType(formData.userType) ? formData.address || undefined : undefined,
        status: isStudentUserType(formData.userType) ? formData.studentStatus : undefined
      })
    }

    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, deepClone(defaultFormData))
}

onMounted(() => {
  void Promise.allSettled([loadDepartmentOptions(), loadEnrollmentYearOptions(), loadData()])
})

onUnmounted(() => {
  debouncedQuery.cancel()
})
</script>

<style scoped lang="scss">
$layout-padding: 20px;
$layout-gap: 16px;
$layout-margin: 20px;
$border-color: #e4e7ed;
$text-color: #303133;
$bg-color-light: #f5f7fa;
$bg-color-danger: #fef0f0;
$border-color-danger: #fde2e2;
$danger-color: #f56c6c;

.users-page {
  padding: $layout-padding;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $layout-margin;
  padding-bottom: $layout-gap;
  border-bottom: 1px solid $border-color;
}

.page-title {
  margin: 0;
  color: $text-color;
  font-size: 18px;
  font-weight: 500;
}

.search-form {
  margin-bottom: $layout-margin;
  padding: $layout-gap;
  background: $bg-color-light;
  border-radius: 4px;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $layout-gap;
  padding: 12px $layout-gap;
  background: $bg-color-danger;
  border: 1px solid $border-color-danger;
  border-radius: 4px;
}

.batch-info {
  color: $danger-color;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: $layout-margin;
}
</style>
