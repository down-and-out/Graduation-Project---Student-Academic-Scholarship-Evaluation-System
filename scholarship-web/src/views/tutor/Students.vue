<!--
  导师指导学生管理页面
  导师可以查看和管理指导的学生信息
-->
<template>
  <div class="students-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">指导学生管理</h2>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="关键字">
        <el-input v-model="queryParams.keyword" placeholder="请输入学号或姓名" clearable />
      </el-form-item>
      <el-form-item label="年级">
        <el-select v-model="queryParams.grade" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option label="2021级" value="2021" />
          <el-option label="2022级" value="2022" />
          <el-option label="2023级" value="2023" />
          <el-option label="2024级" value="2024" />
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
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="grade" label="年级" width="100" />
      <el-table-column prop="major" label="专业" min-width="150" />
      <el-table-column prop="researchDirection" label="研究方向" min-width="150" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewAchievements(row)">
            成果查看
          </el-button>
          <el-button link type="primary" @click="handleView(row)">
            详情
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

    <!-- 学生详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="学生详细信息" width="700px">
      <el-descriptions :column="2" border class="student-info">
        <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentRow.gender === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ currentRow.grade }}</el-descriptions-item>
        <el-descriptions-item label="专业" :span="2">{{ currentRow.major }}</el-descriptions-item>
        <el-descriptions-item label="研究方向" :span="2">{{ currentRow.researchDirection }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentRow.email }}</el-descriptions-item>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// ========== 状态 ==========
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentRow = ref({})

// ========== 查询参数 ==========
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  grade: ''
})

// ========== 方法 ==========

/**
 * 查询数据
 */
async function handleQuery() {
  loading.value = true
  try {
    // TODO: 调用实际API接口
    // const res = await getTutorStudents(queryParams)
    // 模拟数据
    setTimeout(() => {
      tableData.value = [
        {
          id: 1,
          studentNo: '202301001',
          name: '张三',
          gender: 1,
          grade: '2023',
          major: '计算机科学与技术',
          researchDirection: '人工智能',
          phone: '13800138001',
          email: 'zhangsan@example.com',
          paperCount: 2,
          patentCount: 1,
          projectCount: 0
        },
        {
          id: 2,
          studentNo: '202301002',
          name: '李四',
          gender: 0,
          grade: '2023',
          major: '计算机科学与技术',
          researchDirection: '机器学习',
          phone: '13800138002',
          email: 'lisi@example.com',
          paperCount: 1,
          patentCount: 0,
          projectCount: 1
        },
        {
          id: 3,
          studentNo: '202201003',
          name: '王五',
          gender: 1,
          grade: '2022',
          major: '软件工程',
          researchDirection: '数据挖掘',
          phone: '13800138003',
          email: 'wangwu@example.com',
          paperCount: 3,
          patentCount: 2,
          projectCount: 1
        }
      ]
      total.value = 3
      loading.value = false
    }, 300)
  } catch (error) {
    console.error('查询失败:', error)
    loading.value = false
  }
}

/**
 * 重置查询
 */
function handleReset() {
  queryParams.keyword = ''
  queryParams.grade = ''
  queryParams.current = 1
  handleQuery()
}

/**
 * 查看详情
 */
function handleView(row) {
  currentRow.value = row
  detailDialogVisible.value = true
}

/**
 * 查看学生成果
 */
function handleViewAchievements(row) {
  ElMessage.info(`查看 ${row.name} 的科研成果`)
  // TODO: 跳转到科研成果页面
}
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

.statistics-section {
  h4 {
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }
}
</style>
