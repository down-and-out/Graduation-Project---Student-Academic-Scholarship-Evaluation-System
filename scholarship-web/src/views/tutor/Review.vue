<template>
  <div class="review-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">科研成果审核</h2>
        <p class="page-subtitle">按成果类型分别查看并审核名下学生的论文、专利、项目和竞赛记录。</p>
      </div>
    </div>

    <el-tabs v-model="activeType" @tab-change="handleTypeChange">
      <el-tab-pane
        v-for="item in typeOptions"
        :key="item.value"
        :label="item.label"
        :name="item.value"
      />
    </el-tabs>

    <el-form :inline="true" class="search-form">
      <el-form-item label="学生">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入学号、姓名或成果关键词"
          clearable
        />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option
            v-for="item in REVIEW_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe empty-text="暂无数据" style="width: 100%">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="title" label="成果名称" min-width="220" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag :type="row.typeTag">{{ row.typeLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="levelText" label="级别/类型" width="140" />
      <el-table-column prop="score" label="分数" width="100" />
      <el-table-column prop="submitTime" label="提交时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button
            link
            type="primary"
            :disabled="row.status !== REVIEW_DISPLAY_STATUS.PENDING"
            @click="handleReview(row)"
          >
            审核
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
      @current-change="fetchTableData"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="detailDialogVisible" :title="`${activeTypeLabel}详情`" width="760px">
      <el-descriptions :column="2" border class="review-info">
        <el-descriptions-item label="学生姓名">{{ currentRow?.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow?.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成果类型">{{ currentRow?.typeLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成果名称">{{ currentRow?.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="级别/类型">{{ currentRow?.levelText || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分数">{{ currentRow?.score ?? 0 }}</el-descriptions-item>
        <el-descriptions-item :label="currentRow?.metaLabel || '补充信息'" :span="2">
          {{ currentRow?.metaText || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="currentRow?.dateLabel || '日期'">
          {{ currentRow?.dateText || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">
          {{ currentRow?.reviewComment || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="科研成果审核" width="700px">
      <el-descriptions :column="2" border class="review-info">
        <el-descriptions-item label="学生姓名">{{ currentRow?.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow?.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成果类型">{{ currentRow?.typeLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成果名称">{{ currentRow?.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="级别/类型">{{ currentRow?.levelText || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分数">{{ currentRow?.score ?? 0 }}</el-descriptions-item>
        <el-descriptions-item :label="currentRow?.metaLabel || '补充信息'" :span="2">
          {{ currentRow?.metaText || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="currentRow?.dateLabel || '日期'" :span="2">
          {{ currentRow?.dateText || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="REVIEW_DISPLAY_STATUS.APPROVED">通过</el-radio>
            <el-radio :label="REVIEW_DISPLAY_STATUS.REJECTED">不通过</el-radio>
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
        <el-button type="primary" :loading="reviewSubmitting" @click="handleReviewSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getPaperPage,
  reviewPaper,
  type Paper,
  type PaperPageParams
} from '@/api/paper'
import {
  getPatentPage,
  auditPatent,
  type ResearchPatent,
  type PatentPageParams
} from '@/api/patent'
import {
  getProjectPage,
  auditProject,
  type ResearchProject,
  type ProjectPageParams
} from '@/api/project'
import {
  getCompetitionPage,
  auditCompetition,
  type CompetitionAward,
  type CompetitionPageParams
} from '@/api/competition'
import { extractPageData, isRequestCanceled } from '@/utils/helpers'
import {
  ACHIEVEMENT_TYPE_OPTIONS,
  COMPETITION_LEVEL_LABELS,
  JOURNAL_LEVEL_LABELS,
  PROJECT_LEVEL_LABELS
} from '@/constants/achievement'
import { PATENT_TYPE_LABELS } from '@/constants/patent'
import {
  REVIEW_DISPLAY_STATUS,
  REVIEW_STATUS_LABELS,
  REVIEW_STATUS_OPTIONS,
  REVIEW_STATUS_TAG_TYPES,
  normalizeReviewStatus,
  toSubmitReviewStatus,
  type ReviewDisplayStatus
} from '@/constants/review'

defineOptions({ name: 'TutorReview' })

type ReviewType = 'paper' | 'patent' | 'project' | 'competition'

interface ReviewRow {
  id: number
  type: ReviewType
  typeLabel: string
  typeTag: 'primary' | 'success' | 'warning' | 'info'
  studentName?: string
  studentNo?: string
  title: string
  levelText: string
  score: number
  submitTime?: string
  status: number
  reviewComment?: string
  metaLabel: string
  metaText: string
  dateLabel: string
  dateText: string
}

const typeOptions = ACHIEVEMENT_TYPE_OPTIONS as ReadonlyArray<{ label: string; value: ReviewType }>

const loading = ref(false)
const tableData = ref<ReviewRow[]>([])
const total = ref(0)
const activeType = ref<ReviewType>('paper')
const currentRow = ref<ReviewRow | null>(null)
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewingIds = ref(new Set<number>())

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: '' as number | ''
})

const reviewForm = reactive<{
  status: ReviewDisplayStatus
  comment: string
}>({
  status: REVIEW_DISPLAY_STATUS.APPROVED,
  comment: ''
})

const activeTypeLabel = computed(
  () => typeOptions.find(item => item.value === activeType.value)?.label || '成果'
)

function getStatusText(status?: number) {
  return status === undefined ? '-' : (REVIEW_STATUS_LABELS[status] || '-')
}

function getStatusTag(status?: number): 'warning' | 'success' | 'danger' | 'info' {
  return status === undefined ? 'info' : (REVIEW_STATUS_TAG_TYPES[status] || 'info')
}

function normalizePaper(row: Paper): ReviewRow {
  return {
    id: row.id || 0,
    type: 'paper',
    typeLabel: '论文',
    typeTag: 'primary',
    studentName: row.studentName,
    studentNo: row.studentNo,
    title: row.title || row.paperTitle || '-',
    levelText: JOURNAL_LEVEL_LABELS[row.journalLevel || 0] || row.level || '-',
    score: Number(row.score ?? 0),
    submitTime: row.createTime,
    status: row.status ?? REVIEW_DISPLAY_STATUS.PENDING,
    reviewComment: row.reviewComment,
    metaLabel: '作者信息',
    metaText: row.authors || '-',
    dateLabel: '发表日期',
    dateText: row.publicationDate || row.publishDate || row.date || '-'
  }
}

function normalizePatent(row: ResearchPatent): ReviewRow {
  return {
    id: row.id || 0,
    type: 'patent',
    typeLabel: '专利',
    typeTag: 'success',
    studentName: row.studentName,
    studentNo: row.studentNo,
    title: row.patentName || '-',
    levelText: PATENT_TYPE_LABELS[row.patentType] || '-',
    score: Number(row.score ?? 0),
    submitTime: row.createTime,
    status: normalizeReviewStatus(row.auditStatus),
    reviewComment: row.auditComment,
    metaLabel: '专利号',
    metaText: row.patentNo || '-',
    dateLabel: '申请日期',
    dateText: row.applicationDate || '-'
  }
}

function normalizeProject(row: ResearchProject): ReviewRow {
  return {
    id: row.id || 0,
    type: 'project',
    typeLabel: '项目',
    typeTag: 'warning',
    studentName: row.studentName,
    studentNo: row.studentNo,
    title: row.projectName || '-',
    levelText: PROJECT_LEVEL_LABELS[row.projectLevel || 0] || '-',
    score: Number(row.score ?? 0),
    submitTime: row.createTime,
    status: normalizeReviewStatus(row.auditStatus),
    reviewComment: row.auditComment,
    metaLabel: '项目编号',
    metaText: row.projectNo || row.projectSource || '-',
    dateLabel: '项目周期',
    dateText: [row.startDate, row.endDate].filter(Boolean).join(' 至 ') || '-'
  }
}

function normalizeCompetition(row: CompetitionAward): ReviewRow {
  return {
    id: row.id || 0,
    type: 'competition',
    typeLabel: '竞赛',
    typeTag: 'info',
    studentName: row.studentName,
    studentNo: row.studentNo,
    title: row.competitionName || '-',
    levelText: COMPETITION_LEVEL_LABELS[row.competitionLevel || 0] || '-',
    score: Number(row.score ?? 0),
    submitTime: row.createTime,
    status: normalizeReviewStatus(row.auditStatus),
    reviewComment: row.auditComment,
    metaLabel: '指导教师',
    metaText: row.instructor || '-',
    dateLabel: '获奖日期',
    dateText: row.awardDate || '-'
  }
}

async function fetchTableData() {
  loading.value = true
  try {
    if (activeType.value === 'paper') {
      const res = await getPaperPage({
        current: queryParams.current,
        size: queryParams.size,
        keyword: queryParams.keyword || undefined,
        status: queryParams.status === '' ? undefined : queryParams.status
      } as PaperPageParams)
      const pageData = extractPageData<Paper>(res)
      tableData.value = (pageData?.records || []).map(normalizePaper)
      total.value = pageData?.total || 0
      return
    }

    if (activeType.value === 'patent') {
      const res = await getPatentPage({
        current: queryParams.current,
        size: queryParams.size,
        keyword: queryParams.keyword || undefined,
        auditStatus: queryParams.status === '' ? undefined : toSubmitReviewStatus(queryParams.status)
      } as PatentPageParams)
      const pageData = extractPageData<ResearchPatent>(res)
      tableData.value = (pageData?.records || []).map(normalizePatent)
      total.value = pageData?.total || 0
      return
    }

    if (activeType.value === 'project') {
      const res = await getProjectPage({
        current: queryParams.current,
        size: queryParams.size,
        keyword: queryParams.keyword || undefined,
        auditStatus: queryParams.status === '' ? undefined : toSubmitReviewStatus(queryParams.status)
      } as ProjectPageParams)
      const pageData = extractPageData<ResearchProject>(res)
      tableData.value = (pageData?.records || []).map(normalizeProject)
      total.value = pageData?.total || 0
      return
    }

    const res = await getCompetitionPage({
      current: queryParams.current,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      auditStatus: queryParams.status === '' ? undefined : toSubmitReviewStatus(queryParams.status)
    } as CompetitionPageParams)
    const pageData = extractPageData<CompetitionAward>(res)
    tableData.value = (pageData?.records || []).map(normalizeCompetition)
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('查询审核列表失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('查询失败，请稍后重试')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleTypeChange() {
  queryParams.current = 1
  queryParams.status = ''
  fetchTableData()
}

function handleQuery() {
  queryParams.current = 1
  fetchTableData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.current = 1
  fetchTableData()
}

function handleSizeChange() {
  queryParams.current = 1
  fetchTableData()
}

function handleView(row: ReviewRow) {
  currentRow.value = { ...row }
  detailDialogVisible.value = true
}

function handleReview(row: ReviewRow) {
  currentRow.value = { ...row }
  reviewForm.status = REVIEW_DISPLAY_STATUS.APPROVED
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

async function handleReviewSubmit() {
  if (!currentRow.value) return
  if (!reviewForm.comment.trim()) {
    ElMessage.warning('请输入审核意见')
    return
  }

  // 竞态防护：同一记录正在审核中则拦截重复提交
  if (reviewingIds.value.has(currentRow.value.id)) {
    ElMessage.warning('该记录正在审核中，请勿重复操作')
    return
  }
  reviewingIds.value.add(currentRow.value.id)

  reviewSubmitting.value = true
  try {
    const submitStatus = toSubmitReviewStatus(reviewForm.status)

    if (currentRow.value.type === 'paper') {
      await reviewPaper(currentRow.value.id, {
        status: reviewForm.status,
        reviewComment: reviewForm.comment
      })
    } else if (currentRow.value.type === 'patent') {
      await auditPatent(currentRow.value.id, {
        auditStatus: submitStatus,
        auditComment: reviewForm.comment
      })
    } else if (currentRow.value.type === 'project') {
      await auditProject(currentRow.value.id, {
        auditStatus: submitStatus,
        auditComment: reviewForm.comment
      })
    } else {
      await auditCompetition(currentRow.value.id, {
        auditStatus: submitStatus,
        auditComment: reviewForm.comment
      })
    }

    // 本地更新行状态，避免列表刷新前的短暂不一致
    const idx = tableData.value.findIndex(r => r.id === currentRow.value!.id)
    if (idx >= 0) {
      tableData.value[idx].status = reviewForm.status
    }

    ElMessage.success('审核成功')
    reviewDialogVisible.value = false
    await fetchTableData()
  } catch (error) {
    console.error('审核提交失败:', error)
    ElMessage.error('审核提交失败，请稍后重试')
  } finally {
    reviewingIds.value.delete(currentRow.value!.id)
    reviewSubmitting.value = false
  }
}

onMounted(() => {
  fetchTableData()
})
</script>

<style scoped lang="scss">
.review-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;

  .page-title {
    margin: 0 0 6px;
    color: #303133;
    font-size: 22px;
    font-weight: 600;
  }

  .page-subtitle {
    margin: 0;
    color: #909399;
    font-size: 13px;
  }
}

.search-form {
  margin: 8px 0 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
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
