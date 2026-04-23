<!--
  管理员 - 评分规则管理页面
  管理员可以配置评分规则
-->
<template>
  <div class="rules-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">评分规则管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加规则
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="规则类型">
        <el-select v-model="queryParams.ruleType" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
          <el-option label="论文" :value="RULE_TYPE.PAPER" />
          <el-option label="专利" :value="RULE_TYPE.PATENT" />
          <el-option label="项目" :value="RULE_TYPE.PROJECT" />
          <el-option label="竞赛" :value="RULE_TYPE.COMPETITION" />
          <el-option label="课程" :value="RULE_TYPE.COURSE" />
          <el-option label="德育" :value="RULE_TYPE.MORAL" />
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
      empty-text="暂无规则数据"
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="ruleCode" label="规则编码" width="150" />
      <el-table-column prop="ruleName" label="规则名称" min-width="200" />
      <el-table-column prop="ruleType" label="规则类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.ruleType === RULE_TYPE.PAPER" type="primary">论文</el-tag>
          <el-tag v-else-if="row.ruleType === RULE_TYPE.PATENT" type="success">专利</el-tag>
          <el-tag v-else-if="row.ruleType === RULE_TYPE.PROJECT" type="warning">项目</el-tag>
          <el-tag v-else-if="row.ruleType === RULE_TYPE.COMPETITION" type="info">竞赛</el-tag>
          <el-tag v-else-if="row.ruleType === RULE_TYPE.COURSE" type="danger">课程</el-tag>
          <el-tag v-else-if="row.ruleType === RULE_TYPE.MORAL" type="warning">德育</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分值" width="80" />
      <el-table-column prop="level" label="等级要求" width="150" />
      <el-table-column prop="isAvailable" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isAvailable === AVAILABILITY.ENABLED ? 'success' : 'info'">
            {{ row.isAvailable === AVAILABILITY.ENABLED ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="70" />
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
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
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
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="formData.ruleType" style="width: 100%">
            <el-option label="论文" :value="RULE_TYPE.PAPER" />
            <el-option label="专利" :value="RULE_TYPE.PATENT" />
            <el-option label="项目" :value="RULE_TYPE.PROJECT" />
            <el-option label="竞赛" :value="RULE_TYPE.COMPETITION" />
            <el-option label="课程" :value="RULE_TYPE.COURSE" />
            <el-option label="德育" :value="RULE_TYPE.MORAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="formData.ruleCode" placeholder="如：PAPER_SCI_1" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="如：SCI一区论文" />
        </el-form-item>
        <el-form-item label="等级要求" prop="level">
          <el-input v-model="formData.level" placeholder="如：SCI一区" />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="formData.score" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="最高分">
          <el-input-number v-model="formData.maxScore" :min="0" :precision="2" />
          <span class="form-tip">不填则无上限</span>
        </el-form-item>
        <el-form-item label="评分条件">
          <el-input
            v-model="formData.condition"
            type="textarea"
            :rows="3"
            placeholder="详细描述评分条件"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.isAvailable">
            <el-radio :label="AVAILABILITY.ENABLED">启用</el-radio>
            <el-radio :label="AVAILABILITY.DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRulePage, addRule, updateRule, deleteRule, getRuleCategoryList } from '@/api/rule'

// 常量定义
const RULE_TYPE = {
  PAPER: 1,       // 论文
  PATENT: 2,      // 专利
  PROJECT: 3,     // 项目
  COMPETITION: 4, // 竞赛
  COURSE: 5,      // 课程成绩
  MORAL: 6        // 德育表现
}

const AVAILABILITY = {
  ENABLED: 1,     // 启用
  DISABLED: 0     // 禁用
}

const loading = ref(false)
const tableData = ref([])
const categoryOptions = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const dialogTitle = computed(() => isEdit.value ? '编辑规则' : '添加规则')

const queryParams = reactive({
  current: 1,
  size: 10,
  ruleType: []
})

// 默认表单数据
const getDefaultFormData = () => ({
  id: null,
  categoryId: null,
  ruleType: RULE_TYPE.PAPER,
  ruleCode: '',
  ruleName: '',
  score: 0,
  maxScore: null,
  level: '',
  condition: '',
  sortOrder: 0,
  isAvailable: AVAILABILITY.ENABLED
})

const formData = reactive(getDefaultFormData())

const formRules = {
  categoryId: [{ required: true, message: '请选择真实规则分类', trigger: 'change' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

async function loadCategories() {
  try {
    const res = await getRuleCategoryList()
    categoryOptions.value = res.data?.data || []
  } catch (error) {
    ElMessage.error('加载规则分类失败：' + (error.message || '未知错误'))
  }
}

async function handleQuery() {
  loading.value = true
  try {
    // 将 'all' 转换为 undefined，不传给后端
    const params = {
      ...queryParams,
      ruleType: queryParams.ruleType.length > 0 ? queryParams.ruleType : undefined
    }
    const res = await getRulePage(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) {
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryParams.ruleType = []
  queryParams.current = 1
  handleQuery()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, getDefaultFormData())
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  // 使用深拷贝避免响应式数据联动
  Object.assign(formData, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRule(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消删除，不显示错误
      return
    }
    ElMessage.error('删除失败：' + (error.message || '未知错误'))
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateRule(formData)
      ElMessage.success('修改成功')
    } else {
      await addRule(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, getDefaultFormData())
}

onMounted(() => {
  loadCategories()
  handleQuery()
})
</script>

<style scoped lang="scss">
.rules-page {
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

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
