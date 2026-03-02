<template>
  <div class="system-page">
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
    </div>

    <el-tabs v-model="activeTab" class="system-tabs">
      <el-tab-pane label="基本设置" name="basic">
        <el-card shadow="never">
          <el-form :model="basicForm" :rules="basicRules" ref="basicFormRef" label-width="150px">
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
              <el-input v-model="basicForm.announcement" type="textarea" :rows="4" maxlength="500" show-word-limit style="width: 500px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="评分权重" name="weight">
        <el-card shadow="never">
          <el-form :model="weightForm" :rules="weightRules" ref="weightFormRef" label-width="200px">
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
              :type="totalWeight === 100 ? 'success' : 'warning'"
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
          <div class="award-list">
            <div v-for="(award, index) in awards" :key="award.name" class="award-item">
              <el-row :gutter="20" align="middle">
                <el-col :span="6">
                  <el-tag :type="getAwardTagType(index)">{{ award.name }}</el-tag>
                </el-col>
                <el-col :span="6">
                  <span>名额比例：{{ award.ratio }}%</span>
                </el-col>
                <el-col :span="6">
                  <span>金额：¥{{ award.amount }}</span>
                </el-col>
                <el-col :span="6">
                  <el-button size="small" @click="handleEditAward(award, index)">编辑</el-button>
                </el-col>
              </el-row>
            </div>
          </div>
          <el-alert
            v-if="totalAwardRatio !== 100"
            title="名额比例总和不等于 100%，请调整"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px"
          />
          <el-divider />
          <el-button type="primary" @click="handleSaveAwards">保存奖项设置</el-button>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="log">
        <el-card shadow="never">
          <el-form :inline="true" class="search-form">
            <el-form-item label="操作类型">
              <el-select v-model="logQuery.type" placeholder="请选择" clearable>
                <el-option label="全部" value="" />
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
      <el-form :model="awardForm" :rules="awardRules" ref="awardFormRef" label-width="100px">
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
      </el-form>
      <template #footer>
        <el-button @click="handleCloseAwardDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveAward">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 常量定义
const LOG_TYPE_TEXT = {
  login: '登录',
  user: '用户管理',
  evaluation: '评定管理',
  system: '系统设置'
}

const AWARD_TAG_TYPES = ['danger', 'warning', 'success']

// 动态生成学期选项（近 3 学年）
const CURRENT_YEAR = new Date().getFullYear()
const semesterOptions = computed(() => {
  const options = []
  for (let i = 0; i < 3; i++) {
    const year = CURRENT_YEAR - i
    const prevYear = year - 1
    options.push({ label: `${prevYear}-${year}学年第一学期`, value: `${year}-1` })
    options.push({ label: `${prevYear}-${year}学年第二学期`, value: `${year}-2` })
  }
  return options
})

// 状态
const activeTab = ref('basic')
const basicFormRef = ref(null)
const weightFormRef = ref(null)
const awardFormRef = ref(null)
const awardDialogVisible = ref(false)
const currentAwardIndex = ref(-1)
const logLoading = ref(false)

// 基本设置表单
const basicForm = reactive({
  systemName: '研究生学业奖学金评定系统',
  systemShortName: '奖学金评定系统',
  currentSemester: `${CURRENT_YEAR}-1`,
  adminEmail: 'admin@example.com',
  adminPhone: '010-12345678',
  announcement: ''
})

const basicRules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  systemShortName: [{ required: true, message: '请输入系统简称', trigger: 'blur' }],
  currentSemester: [{ required: true, message: '请选择当前学期', trigger: 'change' }],
  adminEmail: [
    { required: true, message: '请输入管理员邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  adminPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^0?[1-9]\d{1,2}-?\d{7,8}$/, message: '电话格式不正确（区号 - 电话号码）', trigger: 'blur' }
  ],
  announcement: [
    { max: 500, message: '公告内容不能超过 500 字', trigger: 'blur' }
  ]
}

// 权重设置
const weightForm = reactive({
  courseWeight: 40,
  researchWeight: 35,
  comprehensiveWeight: 25
})

const weightRules = {
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

const totalWeight = computed(() => {
  return weightForm.courseWeight + weightForm.researchWeight + weightForm.comprehensiveWeight
})

// 奖项设置
const awards = ref([
  { name: '一等奖学金', ratio: 10, amount: 10000 },
  { name: '二等奖学金', ratio: 30, amount: 5000 },
  { name: '三等奖学金', ratio: 50, amount: 2000 }
])

const awardForm = reactive({
  name: '',
  ratio: 0,
  amount: 0
})

const awardRules = {
  ratio: [
    { required: true, message: '请设置名额比例', trigger: 'change' },
    { type: 'number', message: '比例必须为数字', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请设置奖励金额', trigger: 'change' },
    { type: 'number', message: '金额必须为数字', trigger: 'change' }
  ]
}

const totalAwardRatio = computed(() => {
  return awards.value.reduce((sum, award) => sum + award.ratio, 0)
})

// 日志数据
const logData = ref([])
const logTotal = ref(0)
const logQuery = reactive({
  current: 1,
  size: 10,
  type: '',
  operator: ''
})

async function handleSaveBasic() {
  const valid = await basicFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // TODO: 调用 API 保存
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function handleSaveWeight() {
  if (totalWeight.value !== 100) {
    ElMessage.warning('权重总和必须等于 100%')
    return
  }

  const valid = await weightFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // TODO: 调用 API 保存
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

function getAwardTagType(index) {
  return AWARD_TAG_TYPES[index] || ''
}

function handleEditAward(award, index) {
  currentAwardIndex.value = index
  awardForm.name = award.name
  awardForm.ratio = award.ratio
  awardForm.amount = award.amount
  awardDialogVisible.value = true
}

function handleCloseAwardDialog() {
  awardDialogVisible.value = false
  awardFormRef.value?.resetFields()
}

async function handleSaveAward() {
  const valid = await awardFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (currentAwardIndex.value >= 0 && currentAwardIndex.value < awards.value.length) {
    awards.value[currentAwardIndex.value] = { ...awardForm }
    awardDialogVisible.value = false
    ElMessage.success('保存成功')
  }
}

function handleSaveAwards() {
  if (totalAwardRatio.value !== 100) {
    ElMessage.warning('名额比例总和必须等于 100%')
    return
  }
  // TODO: 调用 API 保存
  ElMessage.success('奖项设置保存成功')
}

function getLogTypeText(type) {
  return LOG_TYPE_TEXT[type] || type
}

async function handleQueryLog() {
  logLoading.value = true
  try {
    // TODO: 调用 API 查询
    logData.value = [
      {
        operator: 'admin',
        type: 'login',
        description: '用户登录系统',
        ip: '192.168.1.100',
        createTime: '2024-02-19 10:30:15'
      },
      {
        operator: 'admin',
        type: 'system',
        description: '修改系统设置',
        ip: '192.168.1.100',
        createTime: '2024-02-19 10:35:22'
      }
    ]
    logTotal.value = 2
  } catch (error) {
    ElMessage.error(error.message || '查询失败')
  } finally {
    logLoading.value = false
  }
}

onMounted(() => {
  handleQueryLog()
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
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0;
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
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.award-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.award-item:last-child {
  border-bottom: none;
}
</style>
