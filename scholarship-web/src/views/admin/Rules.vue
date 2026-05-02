<!--
  管理员 - 评分规则管理页面
  功能：评分规则CRUD、规则类型筛选、启用/禁用状态管理
-->
<template>
  <div class="rules-page">
    <!-- 页面头部：标题 + 添加规则按钮 -->
    <div class="page-header">
      <h2 class="page-title">评分规则管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加规则
      </el-button>
    </div>

    <!-- 搜索表单：按规则类型筛选 -->
    <el-form :inline="true" class="search-form">
      <!-- 规则类型筛选（支持多选） -->
      <el-form-item label="规则类型">
        <el-select v-model="queryParams.ruleType" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
          <el-option v-for="opt in RULE_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <!-- 查询、重置按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 评分规则数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      empty-text="暂无规则数据"
      style="width: 100%"
    >
      <!-- 序号列 -->
      <el-table-column type="index" label="序号" width="60" />
      <!-- 规则编码列（如：PAPER_SCI_1） -->
      <el-table-column prop="ruleCode" label="规则编码" width="150" />
      <!-- 规则名称列 -->
      <el-table-column prop="ruleName" label="规则名称" min-width="200" />
      <!-- 规则类型列（以标签形式展示） -->
      <el-table-column prop="ruleType" label="规则类型" width="100">
        <template #default="{ row }">
          <el-tag :type="RULE_TYPE_TAG_TYPES[row.ruleType] || 'info'">
            {{ RULE_TYPE_LABELS[row.ruleType] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 分值列 -->
      <el-table-column prop="score" label="分值" width="80" />
      <!-- 等级要求列（如：SCI一区） -->
      <el-table-column prop="level" label="等级要求" width="150" />
      <!-- 状态列：启用/禁用 -->
      <el-table-column prop="isAvailable" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isAvailable === AVAILABILITY.ENABLED ? 'success' : 'info'">
            {{ row.isAvailable === AVAILABILITY.ENABLED ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 排序序号列 -->
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <!-- 操作列：编辑、删除 -->
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
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

    <!-- 添加/编辑规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <!-- 规则分类选择（关联真实分类） -->
        <el-form-item label="规则分类" prop="categoryId">
          <el-select v-model="formData.categoryId" style="width: 100%" placeholder="请选择真实分类">
            <el-option
              v-for="category in categoryOptions"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <!-- 规则类型选择：论文/专利/项目/竞赛等 -->
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="formData.ruleType" style="width: 100%">
            <el-option v-for="opt in RULE_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <!-- 规则编码（唯一标识，如：PAPER_SCI_1） -->
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="formData.ruleCode" placeholder="如：PAPER_SCI_1" />
        </el-form-item>
        <!-- 规则名称（如：SCI一区论文） -->
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="如：SCI一区论文" />
        </el-form-item>
        <!-- 等级要求（如：SCI一区） -->
        <el-form-item label="等级要求" prop="level">
          <el-input v-model="formData.level" placeholder="如：SCI一区" />
        </el-form-item>
        <!-- 分值 -->
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="formData.score" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <!-- 最高分（可选，不填则无上限） -->
        <el-form-item label="最高分">
          <el-input-number v-model="formData.maxScore" :min="0" :precision="2" />
          <span class="form-tip">不填则无上限</span>
        </el-form-item>
        <!-- 评分条件（详细描述） -->
        <el-form-item label="评分条件">
          <el-input
            v-model="formData.condition"
            type="textarea"
            :rows="3"
            placeholder="详细描述评分条件"
          />
        </el-form-item>
        <!-- 排序序号（用于规则展示顺序） -->
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <!-- 状态：启用/禁用 -->
        <el-form-item label="状态">
          <el-radio-group v-model="formData.isAvailable">
            <el-radio :label="AVAILABILITY.ENABLED">启用</el-radio>
            <el-radio :label="AVAILABILITY.DISABLED">禁用</el-radio>
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
 * 评分规则管理页面
 * 功能：
 * - 分页查询评分规则列表（支持规则类型筛选）
 * - 添加评分规则
 * - 编辑评分规则
 * - 删除评分规则（删除后清除规则分类缓存）
 * - 启用/禁用规则状态
 *
 * 评分规则分类：论文、专利、项目、竞赛等
 * 每条规则包含：规则编码、规则名称、规则类型、分值、等级要求等
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRulePage, addRule, updateRule, deleteRule, getRuleCategoryList } from '@/api/rule'
import { clearApiCache } from '@/utils/apiCache'
import { RULE_TYPE, RULE_TYPE_LABELS, RULE_TYPE_OPTIONS, RULE_TYPE_TAG_TYPES } from '@/constants/rule'
import { isRequestCanceled, optimisticUpdate } from '@/utils/helpers'

defineOptions({ name: 'AdminRules' })

/** 可用性状态枚举 */
const AVAILABILITY = {
  ENABLED: 1,     // 启用
  DISABLED: 0     // 禁用
}

// ==================== 状态 ====================

/** 表格加载状态 */
const loading = ref(false)
/** 表格数据列表 */
const tableData = ref([])
/** 规则分类列表选项（从后端加载） */
const categoryOptions = ref([])
/** 数据总数（用于分页） */
const total = ref(0)
/** 添加/编辑对话框是否显示 */
const dialogVisible = ref(false)
/** 是否为编辑模式（true=编辑，false=添加） */
const isEdit = ref(false)
/** 表单提交中状态（防止重复提交） */
const submitting = ref(false)
/** 表单引用（用于验证） */
const formRef = ref(null)

/** 对话框标题：动态切换"添加规则"和"编辑规则" */
const dialogTitle = computed(() => isEdit.value ? '编辑规则' : '添加规则')

// ==================== 查询参数 ====================

/** 查询参数（分页 + 筛选条件） */
const queryParams = reactive({
  current: 1,   // 当前页码
  size: 10,     // 每页条数
  ruleType: []  // 规则类型列表
})

// ==================== 表单数据 ====================

/**
 * 获取表单默认值
 * 用于添加模式和对话框重置
 */
const getDefaultFormData = () => ({
  id: null,
  categoryId: null,     // 关联的规则分类ID
  ruleType: RULE_TYPE.PAPER,  // 默认规则类型为论文
  ruleCode: '',         // 规则编码（唯一标识）
  ruleName: '',         // 规则名称
  score: 0,             // 分值
  maxScore: null,        // 最高分（可选）
  level: '',            // 等级要求
  condition: '',         // 评分条件描述
  sortOrder: 0,          // 排序序号
  isAvailable: AVAILABILITY.ENABLED  // 默认启用
})

/** 对话框中使用的表单数据（响应式） */
const formData = reactive(getDefaultFormData())

// ==================== 表单验证规则 ====================

/** 表单验证规则 */
const formRules = {
  categoryId: [{ required: true, message: '请选择真实规则分类', trigger: 'change' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

// ==================== 方法 ====================

/**
 * 加载规则分类列表（用于添加规则时的分类选择）
 */
async function loadCategories() {
  try {
    const res = await getRuleCategoryList()
    categoryOptions.value = res.data?.data || []
  } catch (error) {
    if (isRequestCanceled(error)) return
    ElMessage.error('加载规则分类失败：' + (error.message || '未知错误'))
  }
}

/**
 * 查询评分规则列表
 */
async function handleQuery() {
  loading.value = true
  try {
    // 将空数组转换为 undefined，不传给后端
    const params = {
      ...queryParams,
      ruleType: queryParams.ruleType.length > 0 ? queryParams.ruleType : undefined
    }
    const res = await getRulePage(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) {
    if (isRequestCanceled(error)) return
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 点击重置按钮：清空筛选条件并重新查询
 */
function handleReset() {
  queryParams.ruleType = []
  queryParams.current = 1
  handleQuery()
}

/**
 * 点击添加按钮：清空表单并打开对话框
 */
function handleAdd() {
  isEdit.value = false
  Object.assign(formData, getDefaultFormData())
  dialogVisible.value = true
}

/**
 * 点击编辑按钮：以当前行数据填充表单并打开对话框
 * 使用 JSON.parse(JSON.stringify) 深拷贝避免响应式数据联动
 */
function handleEdit(row) {
  isEdit.value = true
  // 使用深拷贝避免响应式数据联动
  Object.assign(formData, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

/**
 * 点击删除按钮：删除评分规则
 * 删除后清除规则分类缓存，并本地移除该行（避免全量刷新）
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRule(row.id)
    // 清除规则分类缓存
    clearApiCache('rule_categories')
    // 本地移除行，避免不必要的全量刷新
    tableData.value = tableData.value.filter(item => item.id !== row.id)
    total.value = Math.max(0, total.value - 1)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消删除，不显示错误
      return
    }
    ElMessage.error('删除失败：' + (error.message || '未知错误'))
  }
}

/**
 * 点击确定按钮：提交表单（添加或编辑）
 * 编辑成功后使用乐观更新本地数据
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRule(formData)
      // 乐观更新本地行，避免不必要的全量刷新
      optimisticUpdate(tableData.value, { ...formData })
      ElMessage.success('修改成功')
    } else {
      await addRule(formData)
      ElMessage.success('添加成功')
    }
    // 清除规则分类缓存
    clearApiCache('rule_categories')
    dialogVisible.value = false
    // 添加成功后重新查询（确保列表完整）
    if (!isEdit.value) {
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

/**
 * 对话框关闭时的处理：重置表单验证状态并恢复默认值
 */
function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, getDefaultFormData())
}

// ==================== 生命周期 ====================

/** 组件挂载时：并发加载规则分类列表、规则列表 */
onMounted(() => {
  loadCategories()
  handleQuery()
})
</script>

<style scoped lang="scss">
.rules-page {
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
    font-size: 18px;
    font-weight: 500;
    color: #303133;
    margin: 0;
  }
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

/* 最高分输入框后的提示文字 */
.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
