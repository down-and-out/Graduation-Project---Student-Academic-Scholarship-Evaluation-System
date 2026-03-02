<!--
  管理员 - 结果管理页面
  管理员可以查看和管理奖学金评定结果
-->
<template>
  <div class="results-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">结果管理</h2>
      <el-button type="success" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出结果
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option v-for="item in SEMESTER_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="奖项等级">
        <el-select v-model="queryParams.level" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option label="一等奖学金" value="1" />
          <el-option label="二等奖学金" value="2" />
          <el-option label="三等奖学金" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="院系">
        <el-select v-model="queryParams.department" placeholder="请选择" clearable>
          <el-option label="全部" value="" />
          <el-option v-for="item in DEPARTMENT_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="获奖总人数" :value="stats.total" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="一等奖学金" :value="stats.firstLevel">
            <template #suffix>人</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="二等奖学金" :value="stats.secondLevel">
            <template #suffix>人</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="三等奖学金" :value="stats.thirdLevel">
            <template #suffix>人</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

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
      <el-table-column prop="semester" label="学期" width="180" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="department" label="院系" width="150" />
      <el-table-column prop="major" label="专业" width="150" />
      <el-table-column prop="level" label="奖项等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getLevelConfig(row.level).type">{{ getLevelConfig(row.level).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="综合得分" width="100" />
      <el-table-column prop="rank" label="排名" width="80" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
          <el-button link type="primary" @click="handleAdjust(row)">调整</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评定结果详情" width="800px">
      <el-descriptions :column="2" border class="result-detail" v-if="currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ currentRow.department }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentRow.major }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ currentRow.semester }}</el-descriptions-item>
        <el-descriptions-item label="奖项等级">
          <el-tag :type="getLevelConfig(currentRow.level).type">{{ getLevelConfig(currentRow.level).text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="综合得分">{{ currentRow.score }}</el-descriptions-item>
        <el-descriptions-item label="排名">{{ currentRow.rank }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4>得分明细</h4>
      <el-table :data="currentRow?.details || []" border size="small">
        <el-table-column prop="item" label="项目" width="150" />
        <el-table-column prop="score" label="得分" width="100" />
        <el-table-column prop="description" label="说明" />
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 调整对话框 -->
    <el-dialog v-model="adjustDialogVisible" title="调整奖项等级" width="500px">
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef" label-width="100px" v-if="currentRow">
        <el-form-item label="学生姓名">
          <span>{{ currentRow.name }}</span>
        </el-form-item>
        <el-form-item label="原等级">
          <el-tag :type="getLevelConfig(currentRow.level).type">{{ getLevelConfig(currentRow.level).text }}</el-tag>
        </el-form-item>
        <el-form-item label="调整后" prop="level">
          <el-select v-model="adjustForm.level">
            <el-option label="一等奖学金" :value="1" />
            <el-option label="二等奖学金" :value="2" />
            <el-option label="三等奖学金" :value="3" />
            <el-option label="未获奖" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

// ========== 常量配置 ==========
const SEMESTER_OPTIONS = [
  { label: '2024-2025 学年第一学期', value: '2024-1' },
  { label: '2024-2025 学年第二学期', value: '2024-2' }
]

const DEPARTMENT_OPTIONS = [
  { label: '计算机学院', value: '计算机学院' },
  { label: '软件学院', value: '软件学院' },
  { label: '信息学院', value: '信息学院' }
]

const LEVEL_CONFIG = {
  1: { text: '一等奖学金', type: 'danger' },
  2: { text: '二等奖学金', type: 'warning' },
  3: { text: '三等奖学金', type: 'success' },
  0: { text: '未获奖', type: 'info' }
}

// ========== 状态 ==========
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const adjustDialogVisible = ref(false)
const currentRow = ref(null)
const adjustFormRef = ref(null)

// ========== 统计数据 ==========
const stats = reactive({
  total: 0,
  firstLevel: 0,
  secondLevel: 0,
  thirdLevel: 0
})

// ========== 查询参数 ==========
const queryParams = reactive({
  current: 1,
  size: 10,
  semester: '',
  level: '',
  department: ''
})

// ========== 调整表单 ==========
const adjustForm = reactive({
  level: '',
  reason: ''
})

const adjustRules = {
  level: [{ required: true, message: '请选择调整后的等级', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

// ========== 方法 ==========

/**
 * 获取等级标签配置
 */
function getLevelConfig(level) {
  return LEVEL_CONFIG[level] || LEVEL_CONFIG[0]
}

/**
 * 查询数据
 */
async function handleQuery() {
  loading.value = true
  try {
    // TODO: 调用实际API接口
    setTimeout(() => {
      tableData.value = [
        {
          id: 1,
          semester: '2024-2025学年第一学期',
          studentNo: '202301001',
          name: '张三',
          department: '计算机学院',
          major: '计算机科学与技术',
          level: 1,
          score: 95.5,
          rank: 1,
          details: [
            { item: '课程成绩', score: 40, description: '加权平均分90分' },
            { item: '科研成果', score: 35.5, description: '发表论文2篇，专利1项' },
            { item: '综合素质', score: 20, description: '担任班长，参与多项活动' }
          ]
        },
        {
          id: 2,
          semester: '2024-2025学年第一学期',
          studentNo: '202301002',
          name: '李四',
          department: '计算机学院',
          major: '软件工程',
          level: 2,
          score: 88.0,
          rank: 2,
          details: [
            { item: '课程成绩', score: 38, description: '加权平均分88分' },
            { item: '科研成果', score: 30, description: '发表论文1篇' },
            { item: '综合素质', score: 20, description: '参与多项活动' }
          ]
        },
        {
          id: 3,
          semester: '2024-2025学年第一学期',
          studentNo: '202301003',
          name: '王五',
          department: '软件学院',
          major: '软件工程',
          level: 3,
          score: 82.5,
          rank: 3,
          details: [
            { item: '课程成绩', score: 35, description: '加权平均分85分' },
            { item: '科研成果', score: 27.5, description: '参与项目1项' },
            { item: '综合素质', score: 20, description: '表现良好' }
          ]
        }
      ]
      total.value = 3
      stats.total = 156
      stats.firstLevel = 20
      stats.secondLevel = 50
      stats.thirdLevel = 86
      loading.value = false
    }, 300)
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败，请稍后重试')
    loading.value = false
  }
}

/**
 * 重置查询
 */
function handleReset() {
  queryParams.semester = ''
  queryParams.level = ''
  queryParams.department = ''
  queryParams.current = 1
  handleQuery()
}

/**
 * 导出结果
 */
function handleExport() {
  ElMessage.info('导出功能开发中')
}

/**
 * 查看详情
 */
function handleView(row) {
  currentRow.value = row
  detailDialogVisible.value = true
}

/**
 * 调整等级
 */
function handleAdjust(row) {
  currentRow.value = row
  adjustForm.level = ''
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

/**
 * 提交调整
 */
async function handleAdjustSubmit() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // 二次确认
    await ElMessageBox.confirm(
      `确定要将 ${currentRow.value?.name} 的奖项等级调整为 ${getLevelConfig(adjustForm.level).text} 吗？`,
      '调整确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用调整 API
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('调整失败:', error)
      ElMessage.error('调整失败，请稍后重试')
    }
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.results-page {
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

.stats-row {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.result-detail {
  margin-bottom: 20px;
}

h4 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
</style>
