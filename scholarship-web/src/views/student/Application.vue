<template>
  <div class="application-page">
    <div class="page-header">
      <h2 class="page-title">奖学金申请</h2>
      <el-button type="primary" :disabled="!canApply" @click="handleApply">
        <el-icon><DocumentAdd /></el-icon>
        {{ hasApplied ? '查看申请' : '提交申请' }}
      </el-button>
    </div>

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
        <el-descriptions-item label="奖金金额">{{ batchInfo.amount }}万元</el-descriptions-item>
        <el-descriptions-item label="说明">{{ batchInfo.description }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

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
          <el-tag :type="getApplicationTagType(myApplication.status)">
            {{ applicationStatusMapper.getText(myApplication.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="综合评分">{{ myApplication.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="导师意见" :span="2">
          {{ myApplication.tutorOpinion || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-else description="暂无申请记录" :image-size="120">
      <el-button type="primary" :disabled="!canApply" @click="handleApply">创建申请</el-button>
    </el-empty>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="申请批次">
          <el-input v-model="batchInfo.name" disabled />
        </el-form-item>

        <el-form-item label="自我评价" prop="selfEvaluation">
          <el-input
            v-model="formData.selfEvaluation"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="请输入自我评价，包括学习情况、科研成果、社会实践等"
            :disabled="isViewMode"
          />
        </el-form-item>

        <el-form-item label="成果备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可补充说明其他事项"
            :disabled="isViewMode"
          />
        </el-form-item>

        <el-form-item v-if="!isViewMode" label="申请声明">
          <el-checkbox v-model="formData.agreed">
            本人承诺所填写信息真实有效，如有虚假愿承担相应责任
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template v-if="!isViewMode" #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
      </template>
      <template v-else #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { DocumentAdd } from '@element-plus/icons-vue'
import { getApplicationPage, submitApplication } from '@/api/application'
import type { Application, SubmitApplicationData } from '@/api/application'
import { getAvailableBatches } from '@/api/batch'
import { applicationStatusMapper } from '@/composables/useStatusMapper'

type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

interface BatchCardInfo {
  id: number | null
  name: string
  applyPeriod: string
  status: 'active' | 'closed'
  statusText: string
  quota: number
  amount: number
  description: string
}

interface ApplicationForm {
  batchId: number | null
  selfEvaluation: string
  remark: string
  agreed: boolean
}

interface BatchLike {
  id?: number
  batchName?: string
  startTime?: string
  endTime?: string
  status?: number
  quota?: number
  amount?: number
  description?: string
}

const dialogVisible = ref(false)
const submitting = ref(false)
const loading = ref(false)
const isViewMode = ref(false)
const formRef = ref<FormInstance | null>(null)
const myApplication = ref<Application | null>(null)

const batchInfo = ref<BatchCardInfo>({
  id: null,
  name: '',
  applyPeriod: '',
  status: 'closed',
  statusText: '',
  quota: 0,
  amount: 0,
  description: ''
})

const formData = reactive<ApplicationForm>({
  batchId: null,
  selfEvaluation: '',
  remark: '',
  agreed: false
})

const formRules: FormRules<ApplicationForm> = {
  selfEvaluation: [
    { required: true, message: '请输入自我评价', trigger: 'blur' },
    { min: 10, max: 1000, message: '自我评价长度应在 10-1000 个字符之间', trigger: 'blur' }
  ]
}

const canApply = computed(() => batchInfo.value.status === 'active')
const hasApplied = computed(() => myApplication.value !== null)
const dialogTitle = computed(() => (isViewMode.value ? '查看申请详情' : hasApplied.value ? '查看申请' : '提交申请'))

function getApplicationStep(status: number): number {
  const stepMap: Record<number, number> = { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 4 }
  return stepMap[status] ?? 0
}

function getApplicationTagType(status: number): TagType {
  return applicationStatusMapper.getType(status) as TagType
}

function extractNestedData<T>(payload: unknown): T | null {
  if (!payload || typeof payload !== 'object') return null
  const raw = payload as Record<string, unknown>
  if (raw.data && typeof raw.data === 'object') {
    const inner = raw.data as Record<string, unknown>
    if ('data' in inner) {
      return inner.data as T
    }
    return raw.data as T
  }
  return raw as T
}

function extractPageData<T>(payload: unknown): API.PageResponse<T> | null {
  const data = extractNestedData<API.PageResponse<T>>(payload)
  if (data?.records) return data
  return null
}

async function loadBatchInfo(): Promise<void> {
  try {
    const response = await getAvailableBatches()
    const batches = extractNestedData<BatchLike[]>(response) || []
    if (!batches.length) return

    const batch = batches[0]
    batchInfo.value = {
      id: batch.id ?? null,
      name: batch.batchName || '',
      applyPeriod: `${batch.startTime || ''} 至 ${batch.endTime || ''}`,
      status: batch.status === 1 ? 'active' : 'closed',
      statusText: batch.status === 1 ? '可申请' : '已结束',
      quota: batch.quota || 0,
      amount: batch.amount || 0,
      description: batch.description || ''
    }
  } catch (error) {
    console.error('加载批次信息失败:', error)
  }
}

async function loadMyApplication(): Promise<void> {
  loading.value = true
  try {
    const response = await getApplicationPage({ current: 1, size: 1 })
    const pageData = extractPageData<Application>(response)
    myApplication.value = pageData?.records?.[0] || null
  } catch (error) {
    console.error('加载申请信息失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleApply(): Promise<void> {
  if (hasApplied.value) {
    try {
      const response = await getApplicationPage({ current: 1, size: 1 })
      const pageData = extractPageData<Application>(response)
      const app = pageData?.records?.[0]
      if (!app) return

      formData.batchId = app.batchId
      formData.selfEvaluation = app.selfEvaluation || ''
      formData.remark = app.remark || ''
      isViewMode.value = true
      dialogVisible.value = true
    } catch {
      ElMessage.error('获取申请详情失败')
    }
    return
  }

  if (!canApply.value) {
    ElMessage.warning('当前不在申请时间内')
    return
  }

  isViewMode.value = false
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  if (!formData.agreed) {
    ElMessage.warning('请先阅读并同意申请声明')
    return
  }

  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const batchId = formData.batchId ?? batchInfo.value.id
  if (batchId == null) {
    ElMessage.error('无法获取申请批次')
    return
  }

  submitting.value = true
  try {
    const payload: SubmitApplicationData = {
      batchId,
      selfEvaluation: formData.selfEvaluation,
      remark: formData.remark || undefined
    }
    await submitApplication(payload)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    await loadMyApplication()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

function handleDialogClose(): void {
  formRef.value?.resetFields()
  formData.batchId = null
  formData.selfEvaluation = ''
  formData.remark = ''
  formData.agreed = false
  isViewMode.value = false
}

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
    margin: 0;
    color: #303133;
    font-size: 18px;
    font-weight: 500;
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
