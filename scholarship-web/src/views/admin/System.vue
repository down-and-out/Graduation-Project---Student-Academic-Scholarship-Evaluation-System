<template>
  <div class="system-page">
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
    </div>

    <el-tabs v-model="activeTab" class="system-tabs">
      <el-tab-pane label="基本设置" name="basic">
        <el-card shadow="never">
          <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="150px">
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="basicForm.systemName" style="width: 400px" />
            </el-form-item>
            <el-form-item label="系统简称" prop="systemShortName">
              <el-input v-model="basicForm.systemShortName" style="width: 200px" />
            </el-form-item>
            <el-form-item label="当前学期" prop="currentSemester">
              <el-select v-model="basicForm.currentSemester" style="width: 300px">
                <el-option v-for="opt in semesterOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="管理员邮箱" prop="adminEmail">
              <el-input v-model="basicForm.adminEmail" style="width: 300px" />
            </el-form-item>
            <el-form-item label="联系电话" prop="adminPhone">
              <el-input v-model="basicForm.adminPhone" style="width: 200px" />
            </el-form-item>
            <el-form-item label="系统公告" prop="announcement">
              <el-input
                v-model="basicForm.announcement"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                style="width: 500px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="评分权重" name="weight">
        <el-card shadow="never">
          <el-form ref="weightFormRef" :model="weightForm" :rules="weightRules" label-width="200px">
            <el-form-item label="课程成绩权重" prop="courseWeight">
              <el-input-number v-model="weightForm.courseWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <el-form-item label="科研成果权重" prop="researchWeight">
              <el-input-number v-model="weightForm.researchWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <el-form-item label="综合素质权重" prop="comprehensiveWeight">
              <el-input-number v-model="weightForm.comprehensiveWeight" :min="0" :max="100" :step="5" />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            <el-alert
              title="权重总和必须等于 100%"
              :type="totalWeightAlertType"
              :closable="false"
              style="margin-bottom: 20px"
            />
            <el-form-item>
              <el-button type="primary" @click="handleSaveWeight">保存权重</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="奖项设置" name="award">
        <el-card shadow="never">
          <div v-if="awards.length > 0" class="award-summary">
            <el-card shadow="hover" :body-style="{ padding: '16px' }">
              <div class="summary-header">
                <span class="summary-title">奖项比例分配</span>
                <el-tag :type="awardRatioTagType" size="small">
                  {{ awardRatioText }}
                </el-tag>
              </div>
              <el-progress
                :percentage="Math.min(totalAwardRatio, 100)"
                :status="progressStatus"
                :color="totalAwardRatio < 100 ? '#e6a23c' : undefined"
                :stroke-width="18"
                style="margin: 12px 0"
              />
              <div v-if="totalAwardRatio > 100" class="overflow-hint">已超出 {{ totalAwardRatio - 100 }}%</div>
              <div class="summary-stats">
                <div class="stat-item">
                  <span class="stat-label">当前总比例：</span>
                  <span class="stat-value" :class="awardRatioClass">{{ totalAwardRatio }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">目标比例：</span>
                  <span class="stat-value">100%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">剩余比例：</span>
                  <span class="stat-value" :class="awardRemainClass">{{ 100 - totalAwardRatio }}%</span>
                </div>
              </div>
            </el-card>
          </div>

          <div class="award-list">
            <div v-for="(award, index) in awards" :key="award.id" class="award-item">
              <el-row :gutter="20" align="middle">
                <el-col :span="6">
                  <el-tag :type="getAwardTagType(index)">{{ award.name }}</el-tag>
                </el-col>
                <el-col :span="6">
                  <span>名额比例：{{ award.ratio }}%</span>
                </el-col>
                <el-col :span="6">
                  <span>金额：￥{{ award.amount }}</span>
                </el-col>
                <el-col :span="6">
                  <el-button size="small" @click="handleEditAward(award, index)">编辑</el-button>
                </el-col>
              </el-row>
            </div>
          </div>

          <el-empty v-if="awards.length === 0" description="暂无奖项配置" :image-size="120">
            <template #default>
              <p class="empty-hint">请配置奖项，名额比例总和必须等于 100%</p>
            </template>
          </el-empty>

          <el-alert
            v-if="awards.length > 0 && totalAwardRatio !== 100"
            :title="ratioAlertText"
            :type="totalAwardRatio < 100 ? 'warning' : 'error'"
            :closable="false"
            show-icon
            style="margin: 20px 0"
          />
          <el-divider v-if="awards.length > 0" />
          <el-button v-if="awards.length > 0" type="primary" @click="handleSaveAwards">保存奖项设置</el-button>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="log">
        <el-card shadow="never">
          <el-form :inline="true" class="search-form">
            <el-form-item label="操作类型">
              <el-select v-model="logQuery.type" placeholder="请选择" multiple collapse-tags collapse-tags-tooltip clearable>
                <el-option v-for="(label, value) in LOG_TYPE_TEXT" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作人">
              <el-input v-model="logQuery.operator" placeholder="输入用户名" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQueryLog">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="logLoading" :data="logData" border stripe style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="type" label="操作类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getLogTypeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="操作描述" min-width="200" />
            <el-table-column prop="ip" label="IP 地址" width="140" />
            <el-table-column prop="createTime" label="操作时间" width="180" />
          </el-table>

          <el-pagination
            v-model:current-page="logQuery.current"
            v-model:page-size="logQuery.size"
            :total="logTotal"
            layout="total, prev, pager, next"
            class="pagination"
            @current-change="handleQueryLog"
            @size-change="handleQueryLog"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="awardDialogVisible" title="编辑奖项" width="500px">
      <el-form ref="awardFormRef" :model="awardForm" :rules="awardRules" label-width="100px">
        <el-form-item label="奖项名称" prop="name">
          <el-input v-model="awardForm.name" disabled />
        </el-form-item>
        <el-form-item label="名额比例" prop="ratio">
          <el-input-number v-model="awardForm.ratio" :min="1" :max="100" :step="1" />
          <span style="margin-left: 10px">%</span>
        </el-form-item>
        <el-form-item label="奖励金额" prop="amount">
          <el-input-number v-model="awardForm.amount" :min="0" :step="100" />
          <span style="margin-left: 10px">元</span>
        </el-form-item>
        <el-form-item label="分数范围">
          <el-input-number v-model="awardForm.scoreRange.min" :min="0" :max="100" :precision="2" placeholder="最低分" />
          <span style="margin: 0 10px">-</span>
          <el-input-number v-model="awardForm.scoreRange.max" :min="0" :max="100" :precision="2" placeholder="最高分" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="awardForm.priority" :min="1" :max="10" :step="1" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">数字越小优先级越高</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseAwardDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveAward">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getOperationLogPage, getSetting, updateSetting } from '@/api/system'
import type { AwardConfig, AwardRule, BasicSetting, OperationLog, WeightSetting } from '@/api/system'

type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'
type AlertType = 'success' | 'warning' | 'info' | 'error'
type LogTypeKey = 'login' | 'user' | 'evaluation' | 'system'
type ProgressStatus = '' | 'success' | 'exception'

interface AwardFormState extends AwardRule {}

interface LogRow {
  operator: string
  type: string
  description: string
  ip: string
  createTime: string
}

const LOG_TYPE_TEXT: Record<LogTypeKey, string> = {
  login: '登录',
  user: '用户管理',
  evaluation: '评定管理',
  system: '系统设置'
}

const AWARD_TAG_TYPES: TagType[] = ['danger', 'warning', 'success']
const CURRENT_YEAR = new Date().getFullYear()

const semesterOptions = computed(() => {
  const options: Array<{ label: string; value: string }> = []
  for (let i = 0; i < 3; i++) {
    const year = CURRENT_YEAR - i
    const prevYear = year - 1
    options.push({ label: `${prevYear}-${year}学年第一学期`, value: `${year}-1` })
    options.push({ label: `${prevYear}-${year}学年第二学期`, value: `${year}-2` })
  }
  return options
})

const activeTab = ref('basic')
const basicFormRef = ref<FormInstance | null>(null)
const weightFormRef = ref<FormInstance | null>(null)
const awardFormRef = ref<FormInstance | null>(null)
const awardDialogVisible = ref(false)
const currentAwardIndex = ref(-1)
const logLoading = ref(false)

const basicForm = reactive<BasicSetting>({
  systemName: '研究生学业奖学金评定系统',
  systemShortName: '奖学金评定系统',
  currentSemester: `${CURRENT_YEAR}-1`,
  adminEmail: 'admin@example.com',
  adminPhone: '010-12345678',
  announcement: ''
})

const basicRules: FormRules<BasicSetting> = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  systemShortName: [{ required: true, message: '请输入系统简称', trigger: 'blur' }],
  currentSemester: [{ required: true, message: '请选择当前学期', trigger: 'change' }],
  adminEmail: [
    { required: true, message: '请输入管理员邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  adminPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^0?[1-9]\d{1,2}-?\d{7,8}$/, message: '电话格式不正确', trigger: 'blur' }
  ],
  announcement: [{ max: 500, message: '公告内容不能超过 500 字', trigger: 'blur' }]
}

const weightForm = reactive<WeightSetting>({
  courseWeight: 40,
  researchWeight: 35,
  comprehensiveWeight: 25
})

const weightRules: FormRules<WeightSetting> = {
  courseWeight: [
    { required: true, message: '请设置权重', trigger: 'change' },
    { type: 'number', message: '权重必须为数字', trigger: 'change' }
  ],
  researchWeight: [
    { required: true, message: '请设置权重', trigger: 'change' },
    { type: 'number', message: '权重必须为数字', trigger: 'change' }
  ],
  comprehensiveWeight: [
    { required: true, message: '请设置权重', trigger: 'change' },
    { type: 'number', message: '权重必须为数字', trigger: 'change' }
  ]
}

const awards = ref<AwardRule[]>([])

const awardForm = reactive<AwardFormState>({
  id: '',
  name: '',
  ratio: 0,
  amount: 0,
  scoreRange: { min: 0, max: 100 },
  conditions: [],
  priority: 1
})

const awardRules: FormRules<AwardFormState> = {
  ratio: [
    { required: true, message: '请设置名额比例', trigger: 'change' },
    { type: 'number', message: '比例必须为数字', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请设置奖励金额', trigger: 'change' },
    { type: 'number', message: '金额必须为数字', trigger: 'change' }
  ]
}

const logData = ref<LogRow[]>([])
const logTotal = ref(0)
const logQuery = reactive({
  current: 1,
  size: 10,
  type: [] as string[],
  operator: ''
})

const totalWeight = computed(() => weightForm.courseWeight + weightForm.researchWeight + weightForm.comprehensiveWeight)
const totalAwardRatio = computed(() => awards.value.reduce((sum, award) => sum + award.ratio, 0))

const totalWeightAlertType = computed<AlertType>(() => (totalWeight.value === 100 ? 'success' : 'warning'))
const awardRatioTagType = computed<TagType>(() => {
  if (totalAwardRatio.value === 100) return 'success'
  if (totalAwardRatio.value < 100) return 'warning'
  return 'danger'
})
const awardRatioText = computed(() => {
  if (totalAwardRatio.value === 100) return '已配满'
  if (totalAwardRatio.value < 100) return '未配满'
  return '超出'
})
const progressStatus = computed<ProgressStatus>(() => {
  if (totalAwardRatio.value === 100) return 'success'
  if (totalAwardRatio.value > 100) return 'exception'
  return ''
})
const awardRatioClass = computed(() => ({
  'text-success': totalAwardRatio.value === 100,
  'text-warning': totalAwardRatio.value < 100,
  'text-danger': totalAwardRatio.value > 100
}))
const awardRemainClass = computed(() => ({
  'text-success': 100 - totalAwardRatio.value === 0,
  'text-warning': 100 - totalAwardRatio.value > 0,
  'text-danger': 100 - totalAwardRatio.value < 0
}))
const ratioAlertText = computed(() => (
  totalAwardRatio.value < 100
    ? `名额比例总和为 ${totalAwardRatio.value}%，还差 ${100 - totalAwardRatio.value}% 未分配`
    : `名额比例总和为 ${totalAwardRatio.value}%，已超出 ${totalAwardRatio.value - 100}%`
))

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

async function handleSaveBasic(): Promise<void> {
  const valid = await basicFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await updateSetting<BasicSetting>('basic', basicForm)
  ElMessage.success('保存成功')
}

async function handleSaveWeight(): Promise<void> {
  if (totalWeight.value !== 100) {
    ElMessage.warning('权重总和必须等于 100%')
    return
  }
  const valid = await weightFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await updateSetting<WeightSetting>('weight', weightForm)
  ElMessage.success('保存成功')
}

function getAwardTagType(index: number): TagType {
  return AWARD_TAG_TYPES[index % AWARD_TAG_TYPES.length] || 'info'
}

function handleEditAward(award: AwardRule, index: number): void {
  currentAwardIndex.value = index
  Object.assign(awardForm, {
    id: award.id,
    name: award.name,
    ratio: award.ratio,
    amount: award.amount,
    scoreRange: award.scoreRange || { min: 0, max: 100 },
    conditions: award.conditions || [],
    priority: award.priority || index + 1
  })
  awardDialogVisible.value = true
}

function handleCloseAwardDialog(): void {
  awardDialogVisible.value = false
  awardFormRef.value?.resetFields()
}

async function handleSaveAward(): Promise<void> {
  const valid = await awardFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (currentAwardIndex.value < 0 || currentAwardIndex.value >= awards.value.length) return

  awards.value[currentAwardIndex.value] = {
    ...awards.value[currentAwardIndex.value],
    id: awardForm.id,
    name: awardForm.name,
    ratio: awardForm.ratio,
    amount: awardForm.amount,
    scoreRange: awardForm.scoreRange,
    conditions: awardForm.conditions,
    priority: awardForm.priority
  }
  awardDialogVisible.value = false
  ElMessage.success('保存成功')
}

async function handleSaveAwards(): Promise<void> {
  if (totalAwardRatio.value !== 100) {
    ElMessage.warning(ratioAlertText.value)
    return
  }
  const awardConfig: AwardConfig = {
    name: `${new Date().getFullYear()}年奖项配置`,
    rules: awards.value,
    allocationStrategy: 'scorePriority'
  }
  await updateSetting<AwardConfig>('awards', awardConfig)
  ElMessage.success('奖项设置保存成功')
}

function getLogTypeText(type: string): string {
  return LOG_TYPE_TEXT[type as LogTypeKey] || type
}

async function handleQueryLog(): Promise<void> {
  logLoading.value = true
  try {
    const response = await getOperationLogPage({
      current: logQuery.current,
      size: logQuery.size,
      operationType: logQuery.type.length > 0 ? logQuery.type : undefined,
      username: logQuery.operator || undefined
    })
    const pageData = extractNestedData<API.PageResponse<OperationLog>>(response)
    const records = pageData?.records || []
    logData.value = records.map((item) => ({
      operator: item.username,
      type: item.operationType,
      description: item.operationDesc,
      ip: item.ipAddress,
      createTime: item.createTime
    }))
    logTotal.value = pageData?.total || 0
  } catch (error) {
    console.error('查询操作日志失败:', error)
    ElMessage.error('查询失败')
    logData.value = []
    logTotal.value = 0
  } finally {
    logLoading.value = false
  }
}

async function loadSettings(): Promise<void> {
  try {
    const [basicRes, weightRes, awardsRes] = await Promise.all([
      getSetting<BasicSetting>('basic'),
      getSetting<WeightSetting>('weight'),
      getSetting<AwardConfig>('awards')
    ])

    const basicData = extractNestedData<BasicSetting>(basicRes)
    const weightData = extractNestedData<WeightSetting>(weightRes)
    const awardsData = extractNestedData<AwardConfig>(awardsRes)

    if (basicData) Object.assign(basicForm, basicData)
    if (weightData) Object.assign(weightForm, weightData)
    if (awardsData?.rules) awards.value = awardsData.rules
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.error('加载系统设置失败')
  }
}

onMounted(async () => {
  await loadSettings()
  await handleQueryLog()
})
</script>

<style scoped>
.system-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header .page-title {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 500;
}

.system-tabs .el-tabs__content {
  padding-top: 20px;
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

.award-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.award-item:last-child {
  border-bottom: none;
}

.award-summary {
  margin-bottom: 20px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.summary-title {
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.summary-stats {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.overflow-hint {
  color: #f56c6c;
  font-size: 12px;
  text-align: center;
  margin-top: -8px;
  margin-bottom: 8px;
}

.empty-hint {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}
</style>
