<!--
  评定结果查询页面
  研究生可以查看奖学金评定结果
-->
<template>
  <div class="result-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">评定结果查询</h2>
    </div>

    <!-- 加载状态 -->
    <el-skeleton :rows="5" animated v-if="loading" />

    <!-- 结果卡片 -->
    <el-card v-else-if="result" class="result-card">
      <!-- 获奖状态 -->
      <div class="award-status">
        <div class="award-icon" :class="getAwardClass(result.awardLevel || 0)">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="award-info">
          <h3 class="award-title">{{ getAwardTitle(result.awardLevel || 0) }}</h3>
          <p class="award-amount" v-if="result.awardLevel && result.awardLevel > 0 && result.awardLevel <= 3">
            奖学金金额：¥{{ result.scholarshipAmount || result.totalScore }}
          </p>
        </div>
      </div>

      <!-- 评分详情 -->
      <el-divider />
      <el-descriptions :column="2" border>
        <el-descriptions-item label="评定批次">{{ result.batchName }}</el-descriptions-item>
        <el-descriptions-item label="综合得分">{{ result.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="排名">{{ result.rank }}名</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(result.status)">{{ getStatusText(result.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮 -->
      <div class="result-actions" v-if="result.status === 1">
        <el-button type="primary" @click="handleAppeal">
          <el-icon><DocumentAdd /></el-icon>
          提出异议
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出证书
        </el-button>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-card v-else class="empty-card">
      <el-empty description="暂无评定结果">
        <el-button type="primary" @click="$router.push('/app/student/application')">
          去申请奖学金
        </el-button>
      </el-empty>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="history-card" v-if="historyList.length > 0">
      <template #header>
        <span>历史评定记录</span>
      </template>
      <el-table :data="historyList" border stripe>
        <el-table-column prop="batchName" label="评定批次" width="200" />
        <el-table-column prop="awardLevel" label="获奖等级" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.awardLevel === 1" type="danger">一等奖</el-tag>
            <el-tag v-else-if="row.awardLevel === 2" type="warning">二等奖</el-tag>
            <el-tag v-else-if="row.awardLevel === 3" type="success">三等奖</el-tag>
            <el-tag v-else type="info">未获奖</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合得分" width="100" />
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="publishDate" label="公示日期" width="120" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 异议对话框 -->
    <el-dialog v-model="appealDialogVisible" title="提出异议" width="600px">
      <el-form :model="appealForm" :rules="appealRules" ref="appealFormRef" label-width="100px">
        <el-form-item label="异议理由" prop="reason">
          <el-input v-model="appealForm.reason" placeholder="请输入异议理由" />
        </el-form-item>
        <el-form-item label="详细说明" prop="content">
          <el-input
            v-model="appealForm.content"
            type="textarea"
            :rows="5"
            placeholder="请详细说明您的异议内容和相关证据"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAppeal">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, DocumentAdd, Download } from '@element-plus/icons-vue'
import { getMyResult, getResultPage } from '@/api/result'
import type { EvaluationResult } from '@/api/result'
import { submitAppeal } from '@/api/appeal'
import { SUCCESS } from '@/constants/resultCode'

/**
 * 当前评定结果
 */
const result = ref<EvaluationResult | null>(null)

/**
 * 历史评定记录
 */
const historyList = ref<EvaluationResult[]>([])

/**
 * 异议对话框显示状态
 */
const appealDialogVisible = ref(false)

/**
 * 异议表单引用
 */
const appealFormRef = ref<any>(null)

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 异议表单数据
 */
const appealForm = reactive({
  reason: '',
  content: ''
})

/**
 * 异议表单验证规则
 */
const appealRules = {
  reason: [{ required: true, message: '请输入异议理由', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细说明', trigger: 'blur' }]
}

/**
 * 获取获奖等级标题
 * @param level - 获奖等级
 * @returns 获奖标题
 */
function getAwardTitle(level: number): string {
  const titles: Record<number, string> = {
    0: '未获奖',
    1: '一等奖学金',
    2: '二等奖学金',
    3: '三等奖学金'
  }
  return titles[level] || '未评定'
}

/**
 * 获取获奖样式类名
 * @param level - 获奖等级
 * @returns 样式类名
 */
function getAwardClass(level: number): string {
  const classes: Record<number, string> = {
    1: 'award-first',
    2: 'award-second',
    3: 'award-third',
    0: 'award-none'
  }
  return classes[level] || 'award-none'
}

/**
 * 获取状态文本
 * @param status - 状态值
 * @returns 状态文本
 */
function getStatusText(status: number): string {
  const texts: Record<number, string> = {
    0: '待公示',
    1: '公示中',
    2: '公示完成',
    3: '已完成'
  }
  return texts[status] || '未知'
}

/**
 * 获取状态类型
 * @param status - 状态值
 * @returns Element Plus 标签类型
 */
function getStatusType(status: number): 'info' | 'warning' | 'success' {
  if (status === 1) return 'warning'
  if (status === 2 || status === 3) return 'success'
  return 'info'
}

/**
 * 加载当前评定结果
 */
async function loadResult(): Promise<void> {
  try {
    const res = await getMyResult()
    if (res.code === SUCCESS && res.data) {
      result.value = res.data
    }
  } catch (error) {
    console.error('加载评定结果失败:', error)
  }
}

/**
 * 加载历史评定记录
 */
async function loadHistory(): Promise<void> {
  loading.value = true
  try {
    const res = await getResultPage({ current: 1, size: 100 })
    if (res.code === SUCCESS && res.data?.records) {
      historyList.value = res.data.records
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 打开异议对话框
 */
function handleAppeal(): void {
  appealDialogVisible.value = true
}

/**
 * 提交异议
 */
async function handleSubmitAppeal(): Promise<void> {
  if (!appealFormRef.value) return

  const valid = await appealFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await submitAppeal({
      resultId: result.value?.id || 0,
      studentId: result.value?.studentId || 0,
      reason: appealForm.reason,
      evidence: appealForm.content
    })
    ElMessage.success('异议已提交，请等待处理结果')
    appealDialogVisible.value = false
  } catch (error) {
    console.error('提交异议失败:', error)
    ElMessage.error('提交异议失败')
  }
}

/**
 * 导出证书
 */
function handleExport(): void {
  ElMessage.info('导出功能开发中')
}

/**
 * 查看历史记录详情
 * @param row - 历史记录
 */
function handleViewDetail(row: EvaluationResult): void {
  ElMessage.info('查看详情功能开发中')
}

// ========== 生命周期 ==========
onMounted(() => {
  loadResult()
  loadHistory()
})
</script>
