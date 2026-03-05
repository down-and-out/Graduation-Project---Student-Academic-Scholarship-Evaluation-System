<!--
  导师科研成果审核页面
  导师可以审核指导学生的科研成果
-->
<template>
  <div class="review-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">科研成果审核</h2>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="学生姓名">
        <el-input v-model="queryParams.keyword" placeholder="请输入学号或姓名" clearable />
      </el-form-item>
      <el-form-item label="成果类型">
        <el-select v-model="queryParams.type" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option label="论文" value="paper" />
          <el-option label="专利" value="patent" />
          <el-option label="项目" value="project" />
        </el-select>
      </el-form-item>
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
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="title" label="成果名称" min-width="200" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.type === 'paper'" type="primary">论文</el-tag>
          <el-tag v-else-if="row.type === 'patent'" type="success">专利</el-tag>
          <el-tag v-else-if="row.type === 'project'" type="warning">项目</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="级别" width="120" />
      <el-table-column prop="score" label="申请分值" width="90" />
      <el-table-column prop="submitTime" label="提交时间" width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="row.status === 3" type="danger">未通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button
            link
            type="primary"
            @click="handleReview(row)"
            :disabled="row.status !== 0"
          >
            审核
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
    />

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="科研成果审核" width="700px">
      <el-descriptions :column="2" border class="review-info">
        <el-descriptions-item label="学生姓名">{{ currentRow.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="成果类型">{{ getTypeText(currentRow.type) }}</el-descriptions-item>
        <el-descriptions-item label="成果名称">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="级别">{{ currentRow.level }}</el-descriptions-item>
        <el-descriptions-item label="申请分值">{{ currentRow.score }}分</el-descriptions-item>
        <el-descriptions-item label="作者/发明人" :span="2">{{ currentRow.authors }}</el-descriptions-item>
        <el-descriptions-item label="发表日期" :span="2">{{ currentRow.date }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="3">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReviewSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaperPage, reviewPaper } from '@/api/paper'

// ========== 状态 ==========
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const reviewDialogVisible = ref(false)
const currentRow = ref({})

// ========== 查询参数 ==========
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  type: '',
  status: undefined
})

// ========== 审核表单 ==========
const reviewForm = reactive({
  status: 1,
  comment: ''
})

// ========== 方法 ==========

/**
 * 获取类型文本
 */
function getTypeText(type) {
  const types = { paper: '论文', patent: '专利', project: '项目' }
  return types[type] || type
}

/**
 * 查询数据
 */
async function handleQuery() {
  loading.value = true
  try {
    const res = await getPaperPage(queryParams)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询
 */
function handleReset() {
  queryParams.keyword = ''
  queryParams.type = ''
  queryParams.status = undefined
  queryParams.current = 1
  handleQuery()
}

/**
 * 查看详情
 */
function handleView(row) {
  ElMessage.info('查看详情功能开发中')
}

/**
 * 审核按钮
 */
function handleReview(row) {
  currentRow.value = row
  reviewForm.status = 1
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

/**
 * 提交审核
 */
async function handleReviewSubmit() {
  if (!reviewForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }

  try {
    await reviewPaper(currentRow.value.id, {
      status: reviewForm.status,
      reviewComment: reviewForm.comment
    })
    ElMessage.success('审核成功')
    reviewDialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.review-page {
  padding: 20px;
}

.page-header {
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

.review-info {
  margin-bottom: 20px;
}
</style>
