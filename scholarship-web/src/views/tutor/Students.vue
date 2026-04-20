<template>
  <div class="students-page">
    <div class="page-header">
      <h2 class="page-title">指导学生管理</h2>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyword" placeholder="请输入学号或姓名" clearable />
      </el-form-item>
      <el-form-item label="年级">
        <el-input v-model="queryParams.grade" placeholder="请输入入学年份，如 2024" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="grade" label="年级" width="100" />
      <el-table-column prop="major" label="专业" min-width="150" />
      <el-table-column prop="direction" label="研究方向" min-width="150" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewAchievements(row)">成果查看</el-button>
          <el-button link type="primary" @click="handleView(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.current"
      v-model:page-size="queryParams.size"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handleQuery"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="detailDialogVisible" title="学生详细信息" width="700px">
      <el-descriptions :column="2" border class="student-info">
        <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentRow.gender === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ currentRow.grade }}</el-descriptions-item>
        <el-descriptions-item label="专业" :span="2">{{ currentRow.major || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="研究方向" :span="2">{{ currentRow.direction || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.phone || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentRow.email || '暂无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="statistics-section">
        <h4>科研成果统计</h4>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="论文数量" :value="currentRow.paperCount || 0" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="专利数量" :value="currentRow.patentCount || 0" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="项目数量" :value="currentRow.projectCount || 0" />
          </el-col>
        </el-row>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="achievementDialogVisible" title="学生成果统计" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
      </el-descriptions>

      <el-row :gutter="16" class="achievement-cards">
        <el-col :span="8">
          <el-card shadow="never">
            <el-statistic title="论文" :value="currentRow.paperCount || 0" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <el-statistic title="专利" :value="currentRow.patentCount || 0" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <el-statistic title="项目" :value="currentRow.projectCount || 0" />
          </el-card>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="achievementDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getTutorStudentPage } from '@/api/student'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const achievementDialogVisible = ref(false)
const currentRow = ref({})

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  grade: ''
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getTutorStudentPage({
      current: queryParams.current,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      grade: queryParams.grade || undefined
    })
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.grade = ''
  queryParams.current = 1
  handleQuery()
}

function handleSizeChange() {
  queryParams.current = 1
  handleQuery()
}

function handleView(row) {
  currentRow.value = { ...row }
  detailDialogVisible.value = true
}

function handleViewAchievements(row) {
  currentRow.value = { ...row }
  achievementDialogVisible.value = true
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.students-page {
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

.student-info {
  margin-bottom: 20px;
}

.statistics-section h4 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.achievement-cards {
  margin-top: 20px;
}
</style>
