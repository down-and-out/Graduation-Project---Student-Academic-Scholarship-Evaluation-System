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
/**
 * 学生奖学金申请页面
 * 功能：查看当前批次信息、提交奖学金申请、选择关联的科研成果
 */
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

// el-tag类型别名
type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'
// 成果类型枚举：1论文 2专利 3项目 4竞赛
type AchievementType = 1 | 2 | 3 | 4

// 当前批次信息展示用数据结构
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

// 申请表单数据结构
interface ApplicationForm {
  batchId: number | null
  selfEvaluation: string
  remark: string
  agreed: boolean
}

// 成果类型ID选项值类型
type AchievementTypeId = typeof ACHIEVEMENT_TYPE_ID_OPTIONS[number]['value']
// 已选成果映射表，按类型分组存储成果ID列表
type SelectedAchievementMap = Record<AchievementTypeId, number[]>

// 所有成果类型枚举值列表
const ALL_ACHIEVEMENT_TYPES = ACHIEVEMENT_TYPE_ID_OPTIONS.map(item => item.value as AchievementType)

// 弹窗显示状态
const dialogVisible = ref(false)
// 提交按钮loading状态
const submitting = ref(false)
// 页面加载状态
const loading = ref(false)
// 是否为查看模式（查看模式下表单禁用）
const isViewMode = ref(false)
// 表单引用，用于验证
const formRef = ref<FormInstance | null>(null)
// 我的申请记录
const myApplication = ref<Application | null>(null)
// 申请详情数据（用于查看模式）
const applicationDetail = ref<ApplicationDetail | null>(null)
// 可用于申请的成果列表
const availableAchievements = ref<ApplicationAchievementItem[]>([])

// 已选成果ID映射，按成果类型分组（多选checkbox用）
const selectedIds = reactive<SelectedAchievementMap>(
  Object.fromEntries(ACHIEVEMENT_TYPE_ID_OPTIONS.map(o => [o.value, []])) as SelectedAchievementMap
)

// 当前批次信息
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

// 申请表单默认数据
const formData = reactive<ApplicationForm>({
  batchId: null,
  selfEvaluation: '',
  remark: '',
  agreed: false
})

// 表单验证规则：自我评价必填，10-1000字符
const formRules: FormRules<ApplicationForm> = {
  selfEvaluation: [
    { required: true, message: '请输入自我评价', trigger: 'blur' },
    { min: 10, max: 1000, message: '自我评价长度应在 10 到 1000 个字符之间', trigger: 'blur' }
  ]
}

// 根据批次状态判断当前是否可以申请
const canApply = computed(() => canApplyForBatch(batchInfo.value.status))
// 判断是否已提交过申请
const hasApplied = computed(() => myApplication.value !== null)
// 弹窗标题动态计算
const dialogTitle = computed(() => isViewMode.value ? '查看申请详情' : '提交申请')

// 按成果类型分组展示可选成果，用于多选checkbox面板
const achievementGroups = computed(() =>
  ALL_ACHIEVEMENT_TYPES.map(type => ({
    type,
    label: ACHIEVEMENT_TYPE_ID_LABELS[type],
    items: availableAchievements.value.filter(item => item.achievementType === type)
  }))
)

// 已选成果列表计算属性
// 查看模式：直接使用后端返回的已关联成果
// 编辑模式：根据selectedIds构建已选列表
const selectedAchievements = computed<ApplicationAchievementItem[]>(() => {
  if (isViewMode.value) {
    return applicationDetail.value?.achievements || []
  }

  // 将多维ID数组展平为唯一键集合，用于过滤
  const selectedKeySet = new Set(
    ALL_ACHIEVEMENT_TYPES.flatMap(type => selectedIds[type].map(id => `${type}-${id}`))
  )

  return availableAchievements.value.filter(item =>
    selectedKeySet.has(`${item.achievementType}-${item.achievementId}`)
  )
})

// 获取成果类型对应的中文标签
function getTypeLabel(type: number): string {
  return ACHIEVEMENT_TYPE_ID_LABELS[type as AchievementType] || '未知类型'
}

// 根据申请状态获取对应的流程步骤索引（用于el-steps组件）
function getApplicationStep(status: number): number {
  return APPLICATION_STATUS_STEP_MAP[status] ?? 0
}

// 获取申请状态对应的el-tag类型
function getApplicationTagType(status: number): TagType {
  return applicationStatusMapper.getType(status) as TagType
}

/**
 * 规范化批次信息，用于UI展示
 * 合并后端返回的多种字段名和状态值
 */
function normalizeBatchInfo(batch: EvaluationBatch): BatchCardInfo {
  // 申请时间段格式化为 "开始日期 至 结束日期"
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

/**
 * 加载当前可申请的批次信息
 * @returns 批次ID，null表示无可用批次
 */
async function loadBatchInfo(): Promise<number | null> {
  try {
    const response = await getAvailableBatches()
    const batches = extractApiData<EvaluationBatch[]>(response) || []
    if (!batches.length) {
      // 无可用批次时显示空状态
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

    // 默认取第一个可用批次
    batchInfo.value = normalizeBatchInfo(batches[0])
    return batchInfo.value.id
  } catch (error) {
    console.error('加载批次信息失败:', error)
    if (isRequestCanceled(error)) return null
    ElMessage.error('加载批次信息失败')
  }
  return null
}

/**
 * 加载当前学生指定批次的申请记录
 * 用于判断是否已申请以及显示已申请状态
 */
async function loadMyApplication(batchId: number | null): Promise<void> {
  if (batchId == null) {
    myApplication.value = null
    return
  }

  try {
    const response = await getApplicationPage({ current: 1, size: 1, batchId })
    const pageData = extractPageData<Application>(response)
    // 只取最新一条申请记录
    myApplication.value = pageData?.records?.[0] || null
  } catch (error) {
    console.error('加载申请信息失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('加载申请信息失败')
  }
}

/**
 * 加载可用于申请的科研成果列表
 * 仅加载已通过导师审核的成果
 */
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

/**
 * 加载申请详情，用于查看模式展示
 */
async function loadApplicationDetail(applicationId: number): Promise<void> {
  const response = await getApplicationById(applicationId)
  applicationDetail.value = extractApiData<ApplicationDetail>(response)
}

/**
 * 点击申请按钮
 * 已申请过则打开查看弹窗，未申请则打开申请表单弹窗
 */
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

/**
 * 查看已提交的申请详情
 */
async function handleViewApplication(): Promise<void> {
  if (!myApplication.value?.id) return
  try {
    await loadApplicationDetail(myApplication.value.id)
    // 填充表单用于展示
    formData.selfEvaluation = applicationDetail.value?.selfEvaluation || ''
    formData.remark = applicationDetail.value?.remark || ''
    isViewMode.value = true
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取申请详情失败')
  }
}

/**
 * 提交奖学金申请
 * 包含表单验证、成果关联、批次关联等
 */
async function handleSubmit(): Promise<void> {
  // 已申请过则阻止重复提交
  if (hasApplied.value) {
    ElMessage.warning('您已提交过申请')
    return
  }

  // 必须勾选申请声明
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
    // 将各类型成果ID转换为API期望的格式
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

// 重置所有成果类型的选择状态
function resetSelections(): void {
  for (const key of Object.keys(selectedIds)) {
    selectedIds[Number(key) as AchievementTypeId] = []
  }
}

// 弹窗关闭时重置表单和状态
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

/**
 * 页面初始化
 * 并行加载批次信息和我的申请记录
 */
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
