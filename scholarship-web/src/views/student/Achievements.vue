<!--
  科研成果管理页面
  研究生可以查看、添加、编辑自己的科研成果
-->
<template>
  <div class="achievements-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">科研成果管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加成果
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="审核状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="未通过" :value="3" />
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
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="title" label="成果名称" min-width="200" />
      <el-table-column prop="level" label="级别" width="120" />
      <el-table-column prop="score" label="分值" width="80" />
      <el-table-column prop="status" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="row.status === 3" type="danger">未通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button
            link
            type="primary"
            @click="handleEdit(row)"
            :disabled="row.status !== 0"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row)"
            :disabled="row.status !== 0"
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
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleQuery"
      @current-change="handleQuery"
      class="pagination"
    />

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="论文标题" prop="paperTitle">
          <el-input v-model="formData.paperTitle" placeholder="请输入论文标题" />
        </el-form-item>
        <el-form-item label="作者列表" prop="authors">
          <el-input v-model="formData.authors" placeholder="请输入作者列表，用逗号分隔" />
        </el-form-item>
        <el-form-item label="作者排名" prop="authorRank">
          <el-select v-model="formData.authorRank" placeholder="请选择">
            <el-option label="第一作者" :value="1" />
            <el-option label="第二作者" :value="2" />
            <el-option label="通讯作者" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="期刊名称" prop="journalName">
          <el-input v-model="formData.journalName" placeholder="请输入期刊名称" />
        </el-form-item>
        <el-form-item label="期刊级别" prop="journalLevel">
          <el-select v-model="formData.journalLevel" placeholder="请选择">
            <el-option label="SCI 一区" :value="1" />
            <el-option label="SCI 二区" :value="2" />
            <el-option label="SCI 三区" :value="3" />
            <el-option label="SCI 四区" :value="4" />
            <el-option label="EI" :value="5" />
            <el-option label="核心期刊" :value="6" />
            <el-option label="普通期刊" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响因子" prop="impactFactor">
          <el-input-number v-model="formData.impactFactor" :min="0" :max="100" :precision="2" placeholder="请输入影响因子" />
        </el-form-item>
        <el-form-item label="发表日期" prop="publicationDate">
          <el-date-picker
            v-model="formData.publicationDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { Ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getPaperPage, submitPaper, deletePaper } from '@/api/paper'
import type { Paper, PaperPageParams } from '@/api/paper'

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 表格数据
 */
const tableData = ref<Paper[]>([])

/**
 * 总记录数
 */
const total = ref(0)

/**
 * 对话框显示状态
 */
const dialogVisible = ref(false)

/**
 * 对话框标题
 */
const dialogTitle = computed(() => isEdit.value ? '编辑成果' : '添加成果')

/**
 * 编辑模式标志
 */
const isEdit = ref(false)

/**
 * 表单引用
 */
const formRef = ref<FormInstance | null>(null)

/**
 * 查询参数
 */
const queryParams = reactive<PaperPageParams>({
  current: 1,
  size: 10,
  status: undefined
})

/**
 * 表单数据
 */
const formData = reactive({
  id: null,
  paperTitle: '',
  authors: '',
  authorRank: 1,
  journalName: '',
  journalLevel: null,
  impactFactor: null,
  publicationDate: ''
})

/**
 * 表单验证规则
 */
const formRules = {
  paperTitle: [{ required: true, message: '请输入论文标题', trigger: 'blur' }],
  authors: [{ required: true, message: '请输入作者列表', trigger: 'blur' }],
  authorRank: [{ required: true, message: '请选择作者排名', trigger: 'change' }],
  journalLevel: [{ required: true, message: '请选择期刊级别', trigger: 'change' }]
}

/**
 * 查询数据
 */
async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const res = await getPaperPage(queryParams as PaperPageParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询条件
 */
function handleReset(): void {
  queryParams.status = undefined
  queryParams.current = 1
  handleQuery()
}

/**
 * 添加成果
 */
function handleAdd(): void {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    paperTitle: '',
    authors: '',
    authorRank: 1,
    journalName: '',
    journalLevel: null,
    impactFactor: null,
    publicationDate: ''
  })
  dialogVisible.value = true
}

/**
 * 查看详情
 * @param row - 行数据
 */
function handleView(row: Paper): void {
  ElMessage.info('查看详情功能开发中')
}

/**
 * 编辑成果
 * @param row - 行数据
 */
function handleEdit(row: Paper): void {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    paperTitle: row.title || row.paperTitle,
    authors: row.authors,
    authorRank: row.authorRank,
    journalName: row.journalName,
    journalLevel: row.journalLevel,
    impactFactor: row.impactFactor,
    publicationDate: row.publishDate || row.publicationDate
  })
  dialogVisible.value = true
}

/**
 * 删除成果
 * @param row - 行数据
 */
function handleDelete(row: Paper): void {
  ElMessageBox.confirm('确定要删除该成果吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deletePaper(row.id || 0)
        ElMessage.success('删除成功')
        handleQuery()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 用户取消
    })
}

/**
 * 提交表单
 */
async function handleSubmit(): Promise<void> {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await submitPaper(formData as any)
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('提交失败:', error)
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
.achievements-page {
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
