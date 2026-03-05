<!--
  管理员 - 用户管理页面
  管理员可以查看、管理系统中的所有用户

  功能：
  1. 用户列表展示与分页
  2. 多条件搜索（关键字、角色、状态）支持防抖
  3. 添加/编辑用户（深拷贝避免数据污染）
  4. 查看详情对话框
  5. 删除用户（带错误处理）
  6. 批量删除用户
  7. 重置密码
  8. 表单验证（根据编辑模式动态调整）
-->
<template>
  <div class="users-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="handleAdd" aria-label="添加用户">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="关键字">
        <el-input v-model="queryParams.keyword" placeholder="用户名或姓名" clearable @clear="handleQuery" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="queryParams.userType" placeholder="请选择" @change="handleQuery">
          <el-option label="全部" value="all" />
          <el-option label="研究生" :value="USER_TYPE.STUDENT" />
          <el-option label="导师" :value="USER_TYPE.TUTOR" />
          <el-option label="管理员" :value="USER_TYPE.ADMIN" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" @change="handleQuery">
          <el-option label="全部" value="all" />
          <el-option label="启用" :value="USER_STATUS.ENABLED" />
          <el-option label="禁用" :value="USER_STATUS.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 批量操作工具栏 -->
    <div v-if="selectedRows.length > 0" class="batch-toolbar">
      <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
      <el-button type="danger" size="small" @click="handleBatchDelete" :loading="batchDeleting">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      ref="selectionRef"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="userType" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="getUserTypeTagType(row.userType)">
            {{ getUserTypeText(row.userType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="getUserStatusTagType(row.status)">
            {{ getUserStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)" aria-label="查看详情">
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button link type="primary" @click="handleEdit(row)" aria-label="编辑用户">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button link type="warning" @click="handleResetPassword(row)" aria-label="重置密码">
            <el-icon><RefreshLeft /></el-icon>
            重置密码
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)" aria-label="删除用户">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" :disabled="isEdit" placeholder="3-20 个字符，字母开头" />
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
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="用户详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="viewData">
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
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Edit, Delete, RefreshLeft } from '@element-plus/icons-vue'
import { isValidPhone, isValidEmail, isValidUsername, deepClone, debounce } from '@/utils/helpers'

// 用户类型映射（后端 userType: 1-研究生 2-导师 3-管理员）
const userTypeMapper = {
  1: { text: '研究生', type: 'success' },
  2: { text: '导师', type: 'warning' },
  3: { text: '管理员', type: 'danger' }
}

// 用户状态映射
const userStatusMap = {
  0: { text: '禁用', type: 'info' },
  1: { text: '启用', type: 'success' }
}

// 角色选项（用于表单下拉框）
const ROLE_OPTIONS = [
  { label: '研究生', value: 1 },
  { label: '导师', value: 2 },
  { label: '管理员', value: 3 }
]

// 用户类型常量（与后端保持一致）
const USER_TYPE = {
  STUDENT: 1,
  TUTOR: 2,
  ADMIN: 3
}

// 用户状态常量
const USER_STATUS = {
  DISABLED: 0,
  ENABLED: 1
}

function getUserTypeText(userType) {
  if (userType === undefined || userType === null || userType === '') return ''
  return userTypeMapper[userType]?.text || userTypeMapper[String(userType)]?.text || '未知'
}

function getUserTypeTagType(userType) {
  if (userType === undefined || userType === null || userType === '') return ''
  return userTypeMapper[userType]?.type || userTypeMapper[String(userType)]?.type || ''
}

function getUserStatusText(status) {
  if (status === undefined || status === null || status === '') return ''
  return userStatusMap[status]?.text || userStatusMap[String(status)]?.text || '未知'
}

function getUserStatusTagType(status) {
  if (status === undefined || status === null || status === '') return ''
  return userStatusMap[status]?.type || userStatusMap[String(status)]?.type || ''
}

import { getUserPage, addUser, updateUser, deleteUser, resetPassword, batchDeleteUsers } from '@/api/user'

// 日期时间格式化
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

// 状态
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 批量操作
const selectedRows = ref([])
const selectionRef = ref(null)

// 查看详情对话框
const viewDialogVisible = ref(false)
const viewData = ref(null)

// 批量删除 loading 状态
const batchDeleting = ref(false)

// 查询参数（使用 'all' 作为"全部"选项的值，避免 Element Plus 空字符串 bug）
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  userType: 'all',
  status: 'all'
})

// 表单数据
const defaultFormData = {
  id: null,
  username: '',
  realName: '',
  userType: 1,  // 使用 userType 字段（1-研究生 2-导师 3-管理员）
  department: '',
  phone: '',
  email: '',
  status: 1
}

const formData = reactive({ ...defaultFormData })

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '添加用户')

// 验证规则
const formRules = computed(() => ({
  username: [
    { required: !isEdit.value, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && !isValidUsername(value)) {
          callback(new Error('用户名只能包含字母、数字和下划线，且必须以字母开头，长度为 3-20 个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择角色', trigger: 'change' }],
  department: [{ required: true, message: '请输入院系', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isValidPhone(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
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
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

// 方法

/**
 * 获取查询参数（处理空值和 'all' 值）
 */
function getQueryParams() {
  return {
    current: queryParams.current,
    size: queryParams.size,
    keyword: queryParams.keyword,
    // 将 'all' 转换为 undefined，不传给后端
    userType: queryParams.userType === 'all' ? undefined : queryParams.userType,
    status: queryParams.status === 'all' ? undefined : queryParams.status
  }
}

/**
 * 加载数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage(getQueryParams())
    // axios 响应格式：res.data 是后端返回的 Result 对象，res.data.data 才是真正的数据
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

/**
 * 查询（用于搜索条件变化时）
 */
function handleQuery() {
  queryParams.current = 1
  loadData()
}

/**
 * 重置查询
 */
function handleReset() {
  queryParams.keyword = ''
  queryParams.userType = 'all'
  queryParams.status = 'all'
  queryParams.current = 1
  loadData()
}

/**
 * 防抖版本的查询
 */
const debouncedQuery = debounce(handleQuery, 300)

/**
 * 关键字变化监听（防抖）
 */
watch(
  () => queryParams.keyword,
  () => {
    queryParams.current = 1
    debouncedQuery()
  }
)

/**
 * 处理分页大小变化
 */
function handleSizeChange(val) {
  queryParams.size = val
  queryParams.current = 1
  loadData()
}

/**
 * 处理页码变化
 */
function handleCurrentChange(val) {
  queryParams.current = val
  loadData()
}

/**
 * 处理表格选择变化
 */
function handleSelectionChange(selection) {
  selectedRows.value = selection.map(item => deepClone(item))
}

/**
 * 查看详情
 */
function handleView(row) {
  viewData.value = deepClone(row)
  viewDialogVisible.value = true
}

/**
 * 添加用户
 */
function handleAdd() {
  isEdit.value = false
  Object.assign(formData, deepClone(defaultFormData))
  dialogVisible.value = true
}

/**
 * 编辑用户
 */
function handleEdit(row) {
  isEdit.value = true
  // 使用 realName 字段
  const editData = deepClone(row)
  editData.realName = row.realName || row.name || ''
  Object.assign(formData, editData)
  dialogVisible.value = true
}

/**
 * 重置密码
 */
function handleResetPassword(row) {
  const displayName = row.realName || row.name || '该用户'
  ElMessageBox.confirm(`确定要重置用户"${displayName}"的密码吗？\n\n重置后密码将恢复为默认密码：123456`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用重置密码 API（需要后端支持）
      await resetPassword(row.id, '123456')
      ElMessage.success('密码已重置为默认密码：123456')
      loadData()
    } catch (error) {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消
  })
}

/**
 * 删除用户
 */
function handleDelete(row) {
  const displayName = row.realName || row.name || '该用户'
  ElMessageBox.confirm(`确定要删除用户"${displayName}"吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用删除 API
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消
  })
}

/**
 * 批量删除用户
 */
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  const names = selectedRows.value.map(item => item.realName || item.name || '').join('、')
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？\n\n${names}\n\n此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    batchDeleting.value = true
    try {
      const ids = selectedRows.value.map(item => item.id)
      // 调用批量删除 API
      await batchDeleteUsers(ids)
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      selectionRef.value?.clearSelection()
      loadData()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败，请稍后重试')
    } finally {
      batchDeleting.value = false
    }
  }).catch(() => {
    // 用户取消
  })
}

/**
 * 提交表单
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 调用添加/编辑 API
    // 构造提交数据（使用后端字段名）
    const submitData = {
      id: formData.id,
      username: formData.username,
      realName: formData.realName,
      userType: formData.userType,
      department: formData.department,
      phone: formData.phone,
      email: formData.email,
      status: formData.status
    }

    if (isEdit.value) {
      await updateUser(submitData)
    } else {
      await addUser(submitData)
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

/**
 * 关闭对话框
 */
function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, defaultFormData)
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
// 布局常量
$layout-padding: 20px;
$layout-gap: 16px;
$layout-margin: 20px;

// 颜色变量
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: $layout-margin;
  padding-bottom: $layout-gap;
  border-bottom: 1px solid $border-color;

  .page-title {
    font-size: 18px;
    font-weight: 500;
    color: $text-color;
    margin: 0;
  }
}

.search-form {
  margin-bottom: $layout-margin;
  padding: $layout-gap;
  background: $bg-color-light;
  border-radius: 4px;
}

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px $layout-gap;
  background: $bg-color-danger;
  border: 1px solid $border-color-danger;
  border-radius: 4px;
  margin-bottom: $layout-gap;

  .batch-info {
    color: $danger-color;
    font-weight: 500;
  }
}

.pagination {
  margin-top: $layout-margin;
  display: flex;
  justify-content: flex-end;
}
</style>
