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
      <el-form-item label="评定批次">
        <el-select v-model="queryParams.batchId" placeholder="请选择" clearable>
          <el-option label="全部" :value="null" />
          <el-option v-for="item in batchOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyword" placeholder="学号/姓名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="全部" :value="null" />
          <el-option label="公示中" :value="1" />
          <el-option label="已确定" :value="2" />
          <el-option label="有异议" :value="3" />
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
            <el-option label="特等奖学金" :value="1" />
            <el-option label="一等奖学金" :value="2" />
            <el-option label="二等奖学金" :value="3" />
            <el-option label="三等奖学金" :value="4" />
            <el-option label="未获奖" :value="5" />
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
import { getResultPage } from '@/api/result'

// ========== 常量配置 ==========
// 批次选项（后续可从API获取）
const batchOptions = ref([
  { label: '2024-2025学年第一学期', value: 1 },
  { label: '2024-2025学年第二学期', value: 2 }
])

// 后端奖项等级：1-特等, 2-一等, 3-二等, 4-三等, 5-未获奖
const LEVEL_CONFIG = {
  1: { text: '特等奖学金', type: 'danger' },
  2: { text: '一等奖学金', type: 'danger' },
  3: { text: '二等奖学金', type: 'warning' },
  4: { text: '三等奖学金', type: 'success' },
  5: { text: '未获奖', type: 'info' }
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
  batchId: null,
  keyword: '',
  status: null
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
  return LEVEL_CONFIG[level] || LEVEL_CONFIG[5]
}

/**
 * 查询数据
 */
async function handleQuery() {
  loading.value = true
  try {
    const res = await getResultPage({
      current: queryParams.current,
      size: queryParams.size,
      batchId: queryParams.batchId || undefined,
      status: queryParams.status || undefined,
      keyword: queryParams.keyword || undefined
    })

    if (res.code === 200 && res.data) {
      // 映射后端数据到前端表格格式
      tableData.value = res.data.records.map(item => ({
        id: item.id,
        semester: item.batchName || `批次${item.batchId}`,
        studentNo: item.studentNo || '',
        name: item.studentName || '',
        department: item.department || '-',
        major: item.major || '-',
        level: item.awardLevel || 5,
        score: item.totalScore || 0,
        rank: item.departmentRank || item.majorRank || 0,
        resultStatus: item.resultStatus,
        details: []
      }))
      total.value = res.data.total || 0

      // 从返回数据计算统计
      updateStats(res.data.records)
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败: ' + (error.message || '未知错误'))
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 从返回数据计算统计
 */
function updateStats(records) {
  // 统计获奖人数（awardLevel 1-4 为获奖，5 为未获奖）
  const awarded = records.filter(r => r.awardLevel && r.awardLevel >= 1 && r.awardLevel <= 4)
  stats.total = awarded.length
  // awardLevel: 1-特等, 2-一等, 3-二等, 4-三等
  stats.firstLevel = records.filter(r => r.awardLevel === 2).length  // 一等奖学金
  stats.secondLevel = records.filter(r => r.awardLevel === 3).length // 二等奖学金
  stats.thirdLevel = records.filter(r => r.awardLevel === 4).length  // 三等奖学金
}

/**
 * 重置查询
 */
function handleReset() {
  queryParams.batchId = null
  queryParams.keyword = ''
  queryParams.status = null
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
