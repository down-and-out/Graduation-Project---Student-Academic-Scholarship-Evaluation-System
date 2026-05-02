<template>
  <div class="application-page">
    <div class="page-header">
      <h2 class="page-title">奖学金申请</h2>
      <el-button type="primary" :disabled="!canApply && !hasApplied" @click="handleApply">
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
          <el-tag :type="getBatchStatusType(batchInfo.status)">
            {{ batchInfo.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总名额">{{ batchInfo.quota }} 人</el-descriptions-item>
        <el-descriptions-item label="奖学金金额">{{ batchInfo.amount }} 万元</el-descriptions-item>
        <el-descriptions-item label="说明">{{ batchInfo.description || '暂无说明' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="myApplication" class="application-card">
      <template #header>
        <div class="card-header">
          <span>我的申请记录</span>
          <el-button type="primary" link @click="handleViewApplication">
            查看详情
          </el-button>
        </div>
      </template>

      <el-steps :active="getApplicationStep(myApplication.status)" align-center>
        <el-step
          v-for="item in APPLICATION_STEP_CONFIG"
          :key="item.title"
          :title="item.title"
          :description="item.description"
        />
      </el-steps>

      <el-descriptions :column="2" border class="application-info">
        <el-descriptions-item label="申请编号">{{ myApplication.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ myApplication.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getApplicationTagType(myApplication.status)">
            {{ applicationStatusMapper.getText(myApplication.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="综合评分">{{ myApplication.totalScore ?? 0 }} 分</el-descriptions-item>
        <el-descriptions-item label="导师意见" :span="2">
          {{ myApplication.tutorOpinion || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-else description="暂无申请记录" :image-size="120">
      <el-button type="primary" :disabled="!canApply" @click="handleApply">提交申请</el-button>
    </el-empty>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="960px" @close="handleDialogClose">
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

        <el-form-item label="补充说明">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可补充说明其他情况"
            :disabled="isViewMode"
          />
        </el-form-item>

        <el-form-item label="已选成果">
          <div class="achievement-panel">
            <template v-if="selectedAchievements.length > 0">
              <div class="achievement-list">
                <div
                  v-for="item in selectedAchievements"
                  :key="`${item.achievementType}-${item.achievementId}`"
                  class="achievement-item"
                >
                  <div class="achievement-main">
                    <div class="achievement-header">
                      <el-tag size="small" effect="plain">{{ getTypeLabel(item.achievementType) }}</el-tag>
                      <span class="achievement-title">{{ item.title || '未命名成果' }}</span>
                    </div>
                    <div class="achievement-meta">
                      <span>{{ item.subtitle || '暂无副标题' }}</span>
                      <span>{{ item.authors || '暂无人员信息' }}</span>
                    </div>
                    <div v-if="item.scoreComment" class="achievement-comment">{{ item.scoreComment }}</div>
                  </div>
                  <div class="achievement-score">分值：{{ item.score ?? 0 }}</div>
                </div>
              </div>
            </template>
            <el-empty v-else :image-size="80" description="暂未关联成果" />
          </div>
        </el-form-item>

        <template v-if="!isViewMode">
          <el-form-item label="选择成果">
            <div class="achievement-groups">
              <section v-for="group in achievementGroups" :key="group.type" class="achievement-group">
                <div class="group-title">{{ group.label }}</div>
                <div class="achievement-panel">
                  <el-checkbox-group v-model="selectedIds[group.type]">
                    <div v-if="group.items.length > 0" class="option-list">
                      <el-checkbox
                        v-for="item in group.items"
                        :key="item.achievementId"
                        :label="item.achievementId"
                        class="option-item"
                      >
                        <div class="option-body">
                          <div class="option-title">{{ item.title || '未命名成果' }}</div>
                          <div class="option-meta">
                            <span>{{ item.subtitle || '暂无副标题' }}</span>
                            <span>{{ item.authors || '暂无人员信息' }}</span>
                          </div>
                          <div class="option-score">
                            <span>分值：{{ item.score ?? 0 }}</span>
                            <span v-if="item.scoreComment">{{ item.scoreComment }}</span>
                          </div>
                        </div>
                      </el-checkbox>
                    </div>
                    <el-empty v-else :image-size="70" :description="`暂无可关联的${group.label}`" />
                  </el-checkbox-group>
                </div>
              </section>
            </div>
          </el-form-item>

          <el-form-item label="申请声明">
            <el-checkbox v-model="formData.agreed">
              本人承诺所填写信息真实有效，如有虚假愿承担相应责任
            </el-checkbox>
          </el-form-item>
        </template>
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
import {
  getApplicationById,
  getApplicationPage,
  getAvailableApplicationAchievements,
  submitApplication
} from '@/api/application'
import { extractApiData, extractPageData, isRequestCanceled } from '@/utils/helpers'
import type {
  Application,
  ApplicationAchievementItem,
  ApplicationDetail,
  SubmitApplicationData
} from '@/api/application'
import { getAvailableBatches, type EvaluationBatch } from '@/api/evaluation'
import { applicationStatusMapper } from '@/composables/useStatusMapper'
import {
  ACHIEVEMENT_TYPE_ID_LABELS,
  ACHIEVEMENT_TYPE_ID_OPTIONS
} from '@/constants/achievement'
import {
  APPLICATION_BATCH_DISPLAY_STATUS,
  APPLICATION_STATUS_STEP_MAP,
  APPLICATION_STEP_CONFIG,
  canApplyForBatch,
  getBatchStatusText,
  getBatchStatusType,
  normalizeBatchStatus,
  type BatchDisplayStatus
} from '@/constants/application'

type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'
type AchievementType = 1 | 2 | 3 | 4

interface BatchCardInfo {
  id: number | null
  name: string
  applyPeriod: string
  status: BatchDisplayStatus
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

type AchievementTypeId = typeof ACHIEVEMENT_TYPE_ID_OPTIONS[number]['value']
type SelectedAchievementMap = Record<AchievementTypeId, number[]>

const ALL_ACHIEVEMENT_TYPES = ACHIEVEMENT_TYPE_ID_OPTIONS.map(item => item.value as AchievementType)

const dialogVisible = ref(false)
const submitting = ref(false)
const loading = ref(false)
const isViewMode = ref(false)
const formRef = ref<FormInstance | null>(null)
const myApplication = ref<Application | null>(null)
const applicationDetail = ref<ApplicationDetail | null>(null)
const availableAchievements = ref<ApplicationAchievementItem[]>([])

const selectedIds = reactive<SelectedAchievementMap>(
  Object.fromEntries(ACHIEVEMENT_TYPE_ID_OPTIONS.map(o => [o.value, []])) as SelectedAchievementMap
)

const batchInfo = ref<BatchCardInfo>({
  id: null,
  name: '',
  applyPeriod: '',
  status: APPLICATION_BATCH_DISPLAY_STATUS.COMPLETED,
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
    { min: 10, max: 1000, message: '自我评价长度应在 10 到 1000 个字符之间', trigger: 'blur' }
  ]
}

const canApply = computed(() => canApplyForBatch(batchInfo.value.status))
const hasApplied = computed(() => myApplication.value !== null)
const dialogTitle = computed(() => isViewMode.value ? '查看申请详情' : '提交申请')

const achievementGroups = computed(() =>
  ALL_ACHIEVEMENT_TYPES.map(type => ({
    type,
    label: ACHIEVEMENT_TYPE_ID_LABELS[type],
    items: availableAchievements.value.filter(item => item.achievementType === type)
  }))
)

const selectedAchievements = computed<ApplicationAchievementItem[]>(() => {
  if (isViewMode.value) {
    return applicationDetail.value?.achievements || []
  }

  const selectedKeySet = new Set(
    ALL_ACHIEVEMENT_TYPES.flatMap(type => selectedIds[type].map(id => `${type}-${id}`))
  )

  return availableAchievements.value.filter(item =>
    selectedKeySet.has(`${item.achievementType}-${item.achievementId}`)
  )
})

function getTypeLabel(type: number): string {
  return ACHIEVEMENT_TYPE_ID_LABELS[type as AchievementType] || '未知类型'
}

function getApplicationStep(status: number): number {
  return APPLICATION_STATUS_STEP_MAP[status] ?? 0
}

function getApplicationTagType(status: number): TagType {
  return applicationStatusMapper.getType(status) as TagType
}

function normalizeBatchInfo(batch: EvaluationBatch): BatchCardInfo {
  const period = batch.startDate && batch.endDate
    ? `${batch.startDate} 至 ${batch.endDate}`
    : batch.startDate || batch.endDate || '-'

  const normalizedStatus = normalizeBatchStatus(batch.status)
  return {
    id: batch.id ?? null,
    name: batch.name || '',
    applyPeriod: period,
    status: normalizedStatus,
    statusText: batch.statusText || getBatchStatusText(normalizedStatus),
    quota: batch.winnerCount ?? 0,
    amount: batch.totalAmount ?? 0,
    description: batch.description || ''
  }
}

async function loadBatchInfo(): Promise<number | null> {
  try {
    const response = await getAvailableBatches()
    const batches = extractApiData<EvaluationBatch[]>(response) || []
    if (!batches.length) {
      batchInfo.value = {
        id: null,
        name: '',
        applyPeriod: '',
        status: APPLICATION_BATCH_DISPLAY_STATUS.COMPLETED,
        statusText: '',
        quota: 0,
        amount: 0,
        description: ''
      }
      return null
    }

    batchInfo.value = normalizeBatchInfo(batches[0])
    return batchInfo.value.id
  } catch (error) {
    console.error('加载批次信息失败:', error)
    if (isRequestCanceled(error)) return null
    ElMessage.error('加载批次信息失败')
  }
  return null
}

async function loadMyApplication(batchId: number | null): Promise<void> {
  if (batchId == null) {
    myApplication.value = null
    return
  }

  try {
    const response = await getApplicationPage({ current: 1, size: 1, batchId })
    const pageData = extractPageData<Application>(response)
    myApplication.value = pageData?.records?.[0] || null
  } catch (error) {
    console.error('加载申请信息失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('加载申请信息失败')
  }
}

async function loadAvailableAchievements(): Promise<void> {
  try {
    const response = await getAvailableApplicationAchievements()
    availableAchievements.value = extractApiData<ApplicationAchievementItem[]>(response) || []
  } catch (error) {
    console.error('加载可选成果失败:', error)
    ElMessage.error('加载可选成果失败')
    availableAchievements.value = []
  }
}

async function loadApplicationDetail(applicationId: number): Promise<void> {
  const response = await getApplicationById(applicationId)
  applicationDetail.value = extractApiData<ApplicationDetail>(response)
}

async function handleApply(): Promise<void> {
  if (hasApplied.value && myApplication.value?.id) {
    await handleViewApplication()
    return
  }

  if (!canApply.value) {
    ElMessage.warning('当前不在申请时间内')
    return
  }
  await loadAvailableAchievements()
  isViewMode.value = false
  dialogVisible.value = true
}

async function handleViewApplication(): Promise<void> {
  if (!myApplication.value?.id) return
  try {
    await loadApplicationDetail(myApplication.value.id)
    formData.selfEvaluation = applicationDetail.value?.selfEvaluation || ''
    formData.remark = applicationDetail.value?.remark || ''
    isViewMode.value = true
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取申请详情失败')
  }
}

async function handleSubmit(): Promise<void> {
  if (hasApplied.value) {
    ElMessage.warning('您已提交过申请')
    return
  }

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
    const achievements = ALL_ACHIEVEMENT_TYPES.flatMap(type =>
      selectedIds[type].map(id => ({
        achievementType: type,
        achievementId: id
      }))
    )

    const payload: SubmitApplicationData = {
      batchId,
      selfEvaluation: formData.selfEvaluation,
      remark: formData.remark || undefined,
      achievements
    }

    await submitApplication(payload)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    await loadMyApplication(batchInfo.value.id)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交申请失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function resetSelections(): void {
  for (const key of Object.keys(selectedIds)) {
    selectedIds[Number(key) as AchievementTypeId] = []
  }
}

function handleDialogClose(): void {
  formRef.value?.resetFields()
  formData.batchId = null
  formData.selfEvaluation = ''
  formData.remark = ''
  formData.agreed = false
  applicationDetail.value = null
  isViewMode.value = false
  resetSelections()
}

async function initializePage(): Promise<void> {
  loading.value = true
  try {
    const batchId = await loadBatchInfo()
    await loadMyApplication(batchId)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void initializePage()
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
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .application-info {
    margin-top: 30px;
  }
}

.achievement-panel {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}

.achievement-groups {
  display: grid;
  gap: 16px;
  width: 100%;
}

.achievement-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-title {
  font-weight: 600;
  color: #303133;
}

.achievement-list,
.option-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.achievement-item,
.option-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.option-item {
  margin-right: 0;
}

.achievement-main,
.option-item :deep(.el-checkbox__label),
.option-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 6px;
  white-space: normal;
}

.achievement-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.achievement-title,
.option-title {
  color: #303133;
  font-weight: 500;
}

.achievement-meta,
.option-meta,
.option-score {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 13px;
  flex-wrap: wrap;
}

.achievement-comment {
  color: #606266;
  font-size: 13px;
}

.achievement-score {
  color: #409eff;
  white-space: nowrap;
}
</style>
