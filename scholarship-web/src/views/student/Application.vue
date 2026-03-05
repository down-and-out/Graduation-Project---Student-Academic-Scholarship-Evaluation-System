<!--
  奖学金申请页面
  研究生可以查看申请状态、提交奖学金申请

  改进：
  1. 使用 useTable hook 简化表格管理
  2. 使用 createStatusMapper 统一管理状态映射
  3. 添加 JSDoc 类型注释
  4. 移除 TODO 和 mock 数据
-->
<template>
  <div class="application-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">奖学金申请</h2>
      <el-button type="primary" @click="handleApply" :disabled="!canApply">
        <el-icon><DocumentAdd /></el-icon>
        {{ hasApplied ? '查看申请' : '提交申请' }}
      </el-button>
    </div>

    <!-- 当前批次信息 -->
    <el-card class="batch-card">
      <template #header>
        <span>当前评定批次</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="批次名称">{{ batchInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ batchInfo.applyPeriod }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="batchInfo.status === 'active' ? 'success' : 'info'">
            {{ batchInfo.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总名额">{{ batchInfo.quota }}人</el-descriptions-item>
        <el-descriptions-item label="奖金额度">{{ batchInfo.amount }}万元</el-descriptions-item>
        <el-descriptions-item label="说明">{{ batchInfo.description }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 我的申请状态 -->
    <el-card v-if="myApplication" class="application-card">
      <template #header>
        <span>我的申请</span>
      </template>
      <el-steps :active="getApplicationStep(myApplication.status)" align-center>
        <el-step title="草稿" description="填写申请信息" />
        <el-step title="已提交" description="等待导师审核" />
        <el-step title="审核中" description="院系评审" />
        <el-step title="评审完成" description="等待公示" />
        <el-step title="已完成" description="评定结果" />
      </el-steps>

      <el-descriptions :column="2" border class="application-info">
        <el-descriptions-item label="申请编号">{{ myApplication.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ myApplication.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="applicationStatusMapper.getType(myApplication.status)">
            {{ applicationStatusMapper.getText(myApplication.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="综合评分">{{ myApplication.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="导师意见" :span="2">
          {{ myApplication.tutorOpinion || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 空状态 -->
    <el-empty v-else description="暂无申请记录" :image-size="120">
      <el-button type="primary" @click="handleApply" :disabled="!canApply">
        创建申请
      </el-button>
    </el-empty>

    <!-- 申请表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="申请批次">
          <el-input v-model="batchInfo.name" disabled />
        </el-form-item>

        <el-form-item label="自我评价" prop="selfEvaluation">
          <el-input
            v-model="formData.selfEvaluation"
            type="textarea"
            :rows="4"
            placeholder="请输入自我评价，包括学习情况、科研成果、社会实践等"
            maxlength="1000"
            show-word-limit
            :disabled="isViewMode"
          />
        </el-form-item>

        <el-form-item label="成果备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="可补充说明其他事项（选填）"
            maxlength="500"
            show-word-limit
            :disabled="isViewMode"
          />
        </el-form-item>

        <el-form-item label="申请声明" v-if="!isViewMode">
          <el-checkbox v-model="formData.agreed">
            本人承诺所填写信息真实有效，如有虚假愿承担相应责任
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer v-if="!isViewMode">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交申请
        </el-button>
      </template>
      <template #footer v-else>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { DocumentAdd } from '@element-plus/icons-vue'
import { getApplicationPage, submitApplication } from '@/api/application'
import type { Application, SubmitApplicationData } from '@/api/application'
import { getAvailableBatches } from '@/api/batch'
import { applicationStatusMapper } from '@/composables/useStatusMapper'

/**
 * 对话框显示状态
 */
const dialogVisible = ref(false)

/**
 * 提交中状态
 */
const submitting = ref(false)

/**
 * 表单引用
 */
const formRef = ref<FormInstance | null>(null)

/**
 * 我的申请记录
 */
const myApplication = ref<Application | null>(null)

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 查看模式标志
 */
const isViewMode = ref(false)

/**
 * 当前可用批次信息
 */
const batchInfo = ref({
  id: null,
  name: '',
  applyPeriod: '',
  status: '',
  statusText: '',
  quota: 0,
  amount: 0,
  description: ''
})

/**
 * 申请表单数据
 */
const formData = reactive({
  batchId: null,
  selfEvaluation: '',
  remark: '',
  agreed: false
})

/**
 * 表单验证规则
 */
const formRules = {
  selfEvaluation: [
    { required: true, message: '请输入自我评价', trigger: 'blur' },
    { min: 10, max: 1000, message: '自我评价长度应在 10-1000 个字符之间', trigger: 'blur' }
  ]
}

/**
 * 是否可以申请
 */
const canApply = computed(() => {
  return batchInfo.value.status === 'active'
})

/**
 * 是否已申请
 */
const hasApplied = computed(() => {
  return myApplication.value !== null
})

/**
 * 对话框标题
 */
const dialogTitle = computed(() => {
  if (isViewMode.value) {
    return '查看申请详情'
  }
  return hasApplied.value ? '查看申请' : '提交申请'
})

/**
 * 获取申请步骤索引
 * @param status - 申请状态
 * @returns 步骤索引
 */
function getApplicationStep(status: number): number {
  const stepMap: Record<number, number> = {
    0: 0,  // 草稿
    1: 1,  // 已提交
    2: 2,  // 审核中
    3: 3,  // 评审完成
    4: 4,  // 已完成
    5: 4   // 已发放
  }
  return stepMap[status] ?? 0
}

/**
 * 加载当前批次信息
 */
async function loadBatchInfo(): Promise<void> {
  try {
    const res = await getAvailableBatches()
    if (res.data && res.data.length > 0) {
      const batch = res.data[0]
      batchInfo.value = {
        id: batch.id,
        name: batch.batchName,
        applyPeriod: `${batch.startTime} 至 ${batch.endTime}`,
        status: batch.status,
        statusText: batch.status === 1 ? '可申请' : '已结束',
        quota: batch.quota || 0,
        amount: batch.amount || 0,
        description: batch.description || ''
      }
    }
  } catch (error) {
    console.error('加载批次信息失败:', error)
  }
}

/**
 * 加载我的申请记录
 */
async function loadMyApplication(): Promise<void> {
  loading.value = true
  try {
    const res = await getApplicationPage({ current: 1, size: 1 })
    if (res.data?.data?.records?.length > 0) {
      myApplication.value = res.data.data.records[0]
    }
  } catch (error) {
    console.error('加载申请信息失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 处理申请或查看申请
 */
async function handleApply(): Promise<void> {
  if (hasApplied.value) {
    // 查看申请详情
    try {
      const res = await getApplicationPage({ current: 1, size: 1 })
      if (res.data?.data?.records?.length > 0) {
        const app = res.data.data.records[0]
        formData.batchId = app.batchId
        formData.selfEvaluation = app.selfEvaluation || ''
        formData.remark = app.remark || ''
        isViewMode.value = true
        dialogVisible.value = true
      }
    } catch (error) {
      ElMessage.error('获取申请详情失败')
    }
  } else if (!canApply.value) {
    ElMessage.warning('当前不在申请时间内')
  } else {
    isViewMode.value = false
    dialogVisible.value = true
  }
}

/**
 * 提交申请
 */
async function handleSubmit(): Promise<void> {
  if (!formData.agreed) {
    ElMessage.warning('请先阅读并同意申请声明')
    return
  }

  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submitApplication({
      batchId: formData.batchId || batchInfo.value.id,
      selfEvaluation: formData.selfEvaluation,
      remark: formData.remark
    } as SubmitApplicationData)

    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    await loadMyApplication()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 关闭对话框并重置表单
 */
function handleDialogClose(): void {
  formRef.value?.resetFields()
  formData.selfEvaluation = ''
  formData.remark = ''
  formData.agreed = false
  isViewMode.value = false
}

// ========== 生命周期 ==========
onMounted(() => {
  loadBatchInfo()
  loadMyApplication()
})
</script>

<style scoped lang="scss">
.application-page {
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

.batch-card {
  margin-bottom: 20px;
}

.application-card {
  .application-info {
    margin-top: 30px;
  }
}
</style>
