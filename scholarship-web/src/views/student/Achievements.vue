<template>
  <div class="achievements-page">
    <div class="page-header">
      <h2 class="page-title">科研成果管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加成果
      </el-button>
    </div>

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

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="title" label="成果名称" min-width="200" />
      <el-table-column prop="journalName" label="期刊名称" min-width="180" />
      <el-table-column prop="authorRank" label="作者排名" width="100" />
      <el-table-column prop="status" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="row.status === 3" type="danger">未通过</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" :disabled="row.status !== 0" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.status !== 0" @click="handleDelete(row)">删除</el-button>
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
      @size-change="handleQuery"
      @current-change="handleQuery"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="论文标题" prop="paperTitle">
          <el-input v-model="formData.paperTitle" placeholder="请输入论文标题" />
        </el-form-item>
        <el-form-item label="作者列表" prop="authors">
          <el-input v-model="formData.authors" placeholder="请输入作者列表，使用逗号分隔" />
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
          <el-input-number v-model="formData.impactFactor" :min="0" :max="100" :precision="2" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deletePaper, getPaperPage, submitPaper } from '@/api/paper'
import type { Paper, PaperPageParams } from '@/api/paper'

interface PaperForm {
  id: number | null
  paperTitle: string
  authors: string
  authorRank: number
  journalName: string
  journalLevel: number | null
  impactFactor: number | null
  publicationDate: string
}

interface PaperRow extends Paper {
  title: string
  journalName?: string
}

const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance | null>(null)
const tableData = ref<PaperRow[]>([])

const queryParams = reactive<PaperPageParams>({
  current: 1,
  size: 10,
  status: undefined
})

const formData = reactive<PaperForm>({
  id: null,
  paperTitle: '',
  authors: '',
  authorRank: 1,
  journalName: '',
  journalLevel: null,
  impactFactor: null,
  publicationDate: ''
})

const formRules: FormRules<PaperForm> = {
  paperTitle: [{ required: true, message: '请输入论文标题', trigger: 'blur' }],
  authors: [{ required: true, message: '请输入作者列表', trigger: 'blur' }],
  authorRank: [{ required: true, message: '请选择作者排名', trigger: 'change' }],
  journalLevel: [{ required: true, message: '请选择期刊级别', trigger: 'change' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑成果' : '添加成果'))

function extractPageData<T>(payload: unknown): API.PageResponse<T> | null {
  if (!payload || typeof payload !== 'object') return null
  const raw = payload as Record<string, unknown>
  if (raw.data && typeof raw.data === 'object') {
    const inner = raw.data as Record<string, unknown>
    if (inner.data && typeof inner.data === 'object') {
      return inner.data as API.PageResponse<T>
    }
    return raw.data as API.PageResponse<T>
  }
  return raw as unknown as API.PageResponse<T>
}

function normalizePaper(row: Paper): PaperRow {
  return {
    ...row,
    title: row.title || row.paperTitle || '',
    journalName: row.journalName || row.journal || '',
    authorRank: row.authorRank ?? 1,
    journalLevel: row.journalLevel ?? undefined,
    impactFactor: row.impactFactor ?? undefined,
    publicationDate: row.publicationDate || row.publishDate || ''
  }
}

function resetForm(): void {
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
}

async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const response = await getPaperPage(queryParams)
    const pageData = extractPageData<Paper>(response)
    tableData.value = (pageData?.records || []).map(normalizePaper)
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

function handleReset(): void {
  queryParams.status = undefined
  queryParams.current = 1
  handleQuery()
}

function handleAdd(): void {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleView(_row: PaperRow): void {
  ElMessage.info('查看详情功能开发中')
}

function handleEdit(row: PaperRow): void {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id ?? null,
    paperTitle: row.title || '',
    authors: row.authors,
    authorRank: row.authorRank ?? 1,
    journalName: row.journalName || '',
    journalLevel: row.journalLevel ?? null,
    impactFactor: row.impactFactor ?? null,
    publicationDate: row.publicationDate || row.publishDate || ''
  })
  dialogVisible.value = true
}

function handleDelete(row: PaperRow): void {
  ElMessageBox.confirm('确定要删除该成果吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deletePaper(row.id || 0)
      ElMessage.success('删除成功')
      await handleQuery()
    })
    .catch(() => undefined)
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await submitPaper({
      studentId: 0,
      title: formData.paperTitle,
      paperTitle: formData.paperTitle,
      authors: formData.authors,
      journalName: formData.journalName,
      journal: formData.journalName,
      authorRank: formData.authorRank,
      journalLevel: formData.journalLevel ?? undefined,
      impactFactor: formData.impactFactor ?? undefined,
      publicationDate: formData.publicationDate,
      publishDate: formData.publicationDate
    })
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    await handleQuery()
  } catch (error) {
    console.error('提交失败:', error)
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
