<template>
  <div class="course-score-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">课程成绩</h2>
        <p class="page-subtitle">支持上传主修成绩模板，系统按学期、课程名称、课程代码、学分、有效成绩和课程性质导入。</p>
      </div>
      <div class="header-actions">
        <el-upload
          :auto-upload="false"
          :show-file-list="true"
          :limit="1"
          accept=".xls,.xlsx"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
        >
          <template #trigger>
            <el-button>选择 Excel</el-button>
          </template>
        </el-upload>
        <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="handleImport">
          导入成绩
        </el-button>
      </div>
    </div>

    <el-alert
      title="请使用包含 学期、课程名称、课程代码、学分、有效成绩、课程性质 的主修成绩表。合格、不合格、通过类课程会导入记录并显示原始成绩文本，但不参与均分计算。"
      type="info"
      :closable="false"
      class="import-tip"
    />

    <el-form :inline="true" class="search-form">
      <el-form-item label="学年">
        <el-select v-model="queryParams.academicYear" placeholder="全部" clearable style="width: 160px">
          <el-option
            v-for="item in academicYearOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="全部" clearable style="width: 160px">
          <el-option
            v-for="item in COURSE_SCORE_SEMESTER_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="课程名">
        <el-input v-model="queryParams.courseName" placeholder="请输入课程名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="courseCode" label="课程代码" width="130" />
      <el-table-column prop="academicYear" label="学年" width="120" />
      <el-table-column prop="semester" label="学期" width="120">
        <template #default="{ row }">
          {{ formatSemester(row.semester) }}
        </template>
      </el-table-column>
      <el-table-column prop="credit" label="学分" width="100" />
      <el-table-column prop="courseType" label="课程性质" width="110">
        <template #default="{ row }">
          {{ formatCourseType(row.courseType) }}
        </template>
      </el-table-column>
      <el-table-column prop="score" label="成绩" width="100">
        <template #default="{ row }">
          {{ formatScoreDisplay(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="gpa" label="绩点" width="100">
        <template #default="{ row }">
          {{ row.gpa ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { UploadFile, UploadFiles } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getMyCourseScorePage, getMyCourseScoreYears, importMyCourseScores } from '@/api/courseScore'
import type { CourseScore, CourseScoreImportResult, CourseScorePageParams } from '@/api/courseScore'
import { extractApiData, extractPageData, isRequestCanceled } from '@/utils/helpers'
import {
  COURSE_SCORE_SEMESTER_LABELS,
  COURSE_SCORE_SEMESTER_OPTIONS,
  COURSE_TYPE_LABELS
} from '@/constants/courseScore'

defineOptions({ name: 'StudentCourseScores' })

const loading = ref(false)
const uploading = ref(false)
const total = ref(0)
const tableData = ref<CourseScore[]>([])
const selectedFile = ref<File | null>(null)
const academicYearOptions = ref<Array<{ label: string; value: string }>>([])

const queryParams = reactive<CourseScorePageParams>({
  current: 1,
  size: 10,
  academicYear: '',
  semester: undefined,
  courseName: ''
})

function buildAcademicYearOptions(years: string[]): Array<{ label: string; value: string }> {
  const values = Array.from(
    new Set(years.filter((value): value is string => Boolean(value)))
  ).sort((a, b) => b.localeCompare(a, 'zh-CN'))

  return values.map(value => ({ label: value, value }))
}

async function fetchAcademicYearOptions(): Promise<void> {
  try {
    const response = await getMyCourseScoreYears()
    academicYearOptions.value = buildAcademicYearOptions(extractApiData<string[]>(response) || [])
  } catch (error) {
    console.error('加载课程成绩学年选项失败:', error)
    academicYearOptions.value = []
  }
}

function formatSemester(value?: number): string {
  return value === undefined ? '-' : (COURSE_SCORE_SEMESTER_LABELS[value] || '-')
}

function formatCourseType(value?: number): string {
  return value === undefined ? '-' : (COURSE_TYPE_LABELS[value] || '-')
}

function formatScoreDisplay(row: CourseScore): string {
  if (row.scoreText) return row.scoreText
  if (row.score !== undefined && row.score !== null) return String(row.score)
  return '-'
}

async function fetchTableData(): Promise<void> {
  loading.value = true
  try {
    const response = await getMyCourseScorePage({
      ...queryParams,
      academicYear: queryParams.academicYear || undefined,
      courseName: queryParams.courseName || undefined
    })
    const pageData = extractPageData<CourseScore>(response)
    tableData.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('加载课程成绩失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('加载课程成绩失败')
  } finally {
    loading.value = false
  }
}

function handleReset(): void {
  queryParams.current = 1
  queryParams.academicYear = ''
  queryParams.semester = undefined
  queryParams.courseName = ''
  void fetchTableData()
}

function handleQuery(page?: number | Event): void {
  queryParams.current = typeof page === 'number' ? page : 1
  void fetchTableData()
}

function handleSizeChange(): void {
  queryParams.current = 1
  void fetchTableData()
}

function handleFileChange(uploadFile: UploadFile, uploadFiles: UploadFiles): void {
  const latestFile = uploadFile.raw || uploadFiles.at(-1)?.raw || null
  selectedFile.value = latestFile ?? null
}

function handleFileRemove(): void {
  selectedFile.value = null
}

async function handleImport(): Promise<void> {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择 Excel 文件')
    return
  }

  uploading.value = true
  try {
    const response = await importMyCourseScores(selectedFile.value)
    const importResult = extractApiData<CourseScoreImportResult>(response)
    ElMessage.success(importResult?.message || '成绩导入成功')
    selectedFile.value = null
    queryParams.current = 1
    await Promise.all([fetchAcademicYearOptions(), fetchTableData()])
  } catch (error) {
    console.error('导入成绩失败:', error)
    ElMessage.error('导入成绩失败')
  } finally {
    uploading.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([fetchAcademicYearOptions(), fetchTableData()])
})
</script>

<style scoped lang="scss">
.course-score-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 500;
}

.page-subtitle {
  margin: 8px 0 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.import-tip {
  margin-bottom: 20px;
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
