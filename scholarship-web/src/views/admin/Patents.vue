<template>
  <div class="patent-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入专利名称/专利号/申请人" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" placeholder="请选择" clearable>
            <el-option v-for="opt in AUDIT_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>专利列表</span>
          <el-button type="primary" @click="handleAdd">新增专利</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe empty-text="暂无专利数据">
        <el-table-column prop="patentName" label="专利名称" min-width="200" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="applicant" label="申请人" min-width="140" />
        <el-table-column prop="patentNo" label="专利号" min-width="160" />
        <el-table-column prop="patentType" label="专利类型" width="110">
          <template #default="{ row }">
            <el-tag :type="PATENT_TYPE_TAG_TYPES[row.patentType] || 'warning'">
              {{ PATENT_TYPE_LABELS[row.patentType] || '外观设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inventorRank" label="发明人排名" width="110" />
        <el-table-column prop="applicationDate" label="申请日期" width="120" />
        <el-table-column prop="patentStatus" label="专利状态" width="100">
          <template #default="{ row }">
            <el-tag :type="PATENT_STATUS_TAG_TYPES[row.patentStatus] || 'danger'">
              {{ PATENT_STATUS_LABELS[row.patentStatus] || '已失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="90" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="AUDIT_STATUS_TAG_TYPES[row.auditStatus] || 'danger'">
              {{ AUDIT_STATUS_LABELS[row.auditStatus] || '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.auditStatus === AUDIT_STATUS.PENDING"
              type="primary"
              link
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input-number v-model="formData.studentId" :min="1" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="专利名称" prop="patentName">
          <el-input v-model="formData.patentName" placeholder="请输入专利名称" />
        </el-form-item>
        <el-form-item label="专利号" prop="patentNo">
          <el-input v-model="formData.patentNo" placeholder="请输入专利号" />
        </el-form-item>
        <el-form-item label="专利类型" prop="patentType">
          <el-select v-model="formData.patentType" placeholder="请选择专利类型">
            <el-option v-for="opt in PATENT_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人" prop="applicant">
          <el-input v-model="formData.applicant" placeholder="请输入申请人" />
        </el-form-item>
        <el-form-item label="发明人" prop="inventors">
          <el-input v-model="formData.inventors" placeholder="多个发明人请用逗号分隔" />
        </el-form-item>
        <el-form-item label="发明人排名" prop="inventorRank">
          <el-input-number v-model="formData.inventorRank" :min="1" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="申请人排名" prop="applicantRank">
          <el-input-number v-model="formData.applicantRank" :min="1" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="申请日期" prop="applicationDate">
          <el-date-picker
            v-model="formData.applicationDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="专利状态" prop="patentStatus">
          <el-select v-model="formData.patentStatus" placeholder="请选择专利状态">
            <el-option v-for="opt in PATENT_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="证明材料" prop="attachmentUrl">
          <el-upload
            action="#"
            :file-list="uploadFileList"
            :limit="3"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">上传文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="专利详情" width="700px">
      <el-descriptions v-if="currentPatent" :column="2" border>
        <el-descriptions-item label="专利名称">{{ currentPatent.patentName }}</el-descriptions-item>
        <el-descriptions-item label="学生ID">{{ currentPatent.studentId }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentPatent.applicant || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专利号">{{ currentPatent.patentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发明人">{{ currentPatent.inventors || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发明人排名">{{ currentPatent.inventorRank || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人排名">{{ currentPatent.applicantRank || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ currentPatent.applicationDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专利类型">
          <el-tag :type="getPatentTypeTagType(currentPatent.patentType)">
            {{ getPatentTypeLabel(currentPatent.patentType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="专利状态">
          <el-tag :type="getPatentStatusTagType(currentPatent.patentStatus)">
            {{ getPatentStatusLabel(currentPatent.patentStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusTagType(currentPatent.auditStatus)">
            {{ getAuditStatusLabel(currentPatent.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="得分">{{ currentPatent.score ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentPatent.auditComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">{{ currentPatent.attachmentUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPatent.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" title="审核专利" width="500px" @close="handleAuditDialogClose">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="AUDIT_STATUS.APPROVED">通过</el-radio>
            <el-radio :value="AUDIT_STATUS.REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.auditComment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadFiles } from 'element-plus'
import {
  addPatent,
  auditPatent,
  deletePatent,
  getPatentPage,
  type PatentPageParams,
  type ResearchPatent,
  updatePatent
} from '@/api/patent'
import { isRequestCanceled } from '@/utils/helpers'
import {
  AUDIT_STATUS,
  AUDIT_STATUS_LABELS,
  AUDIT_STATUS_OPTIONS,
  AUDIT_STATUS_TAG_TYPES,
  PATENT_STATUS,
  PATENT_STATUS_LABELS,
  PATENT_STATUS_OPTIONS,
  PATENT_STATUS_TAG_TYPES,
  PATENT_TYPE,
  PATENT_TYPE_LABELS,
  PATENT_TYPE_OPTIONS,
  PATENT_TYPE_TAG_TYPES
} from '@/constants/patent'

type PatentForm = {
  id: number | null
  studentId: number | null
  patentName: string
  patentNo: string
  patentType: number
  applicant: string
  inventors: string
  inventorRank: number | null
  applicantRank: number | null
  applicationDate: string
  patentStatus: number
  attachmentUrl: string
  remark: string
}

const loading = ref(false)
const tableData = ref<ResearchPatent[]>([])
const total = ref(0)
const currentPatent = ref<ResearchPatent | null>(null)
const formRef = ref<FormInstance>()

const queryParams = reactive<PatentPageParams>({
  current: 1,
  size: 10,
  keyword: '',
  auditStatus: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增专利')
const isEditMode = ref(false)
const viewDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const uploadFileList = ref<UploadFile[]>([])

const formData = reactive<PatentForm>({
  id: null,
  studentId: null,
  patentName: '',
  patentNo: '',
  patentType: PATENT_TYPE.INVENT,
  applicant: '',
  inventors: '',
  inventorRank: 1,
  applicantRank: 1,
  applicationDate: '',
  patentStatus: PATENT_STATUS.APPLYING,
  attachmentUrl: '',
  remark: ''
})

const auditForm = reactive({
  id: null as number | null,
  auditStatus: AUDIT_STATUS.APPROVED,
  auditComment: ''
})

const formRules: FormRules<PatentForm> = {
  studentId: [{ required: true, message: '请输入学生ID', trigger: 'blur' }],
  patentName: [{ required: true, message: '请输入专利名称', trigger: 'blur' }],
  patentNo: [{ required: true, message: '请输入专利号', trigger: 'blur' }],
  patentType: [{ required: true, message: '请选择专利类型', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人', trigger: 'blur' }],
  inventors: [{ required: true, message: '请输入发明人', trigger: 'blur' }],
  inventorRank: [{ required: true, message: '请输入发明人排名', trigger: 'blur' }],
  applicantRank: [{ required: true, message: '请输入申请人排名', trigger: 'blur' }]
}

function getPatentTypeLabel(value?: number): string {
  return PATENT_TYPE_LABELS[value ?? PATENT_TYPE.DESIGN] || '外观设计'
}

function getPatentTypeTagType(value?: number): 'danger' | 'success' | 'warning' | 'info' {
  return PATENT_TYPE_TAG_TYPES[value ?? PATENT_TYPE.DESIGN] || 'warning'
}

function getPatentStatusLabel(value?: number): string {
  return PATENT_STATUS_LABELS[value ?? PATENT_STATUS.REJECTED] || '已失效'
}

function getPatentStatusTagType(value?: number): 'info' | 'success' | 'danger' {
  return PATENT_STATUS_TAG_TYPES[value ?? PATENT_STATUS.REJECTED] || 'danger'
}

function getAuditStatusLabel(value?: number): string {
  return AUDIT_STATUS_LABELS[value ?? AUDIT_STATUS.REJECTED] || '驳回'
}

function getAuditStatusTagType(value?: number): 'warning' | 'success' | 'danger' {
  return AUDIT_STATUS_TAG_TYPES[value ?? AUDIT_STATUS.REJECTED] || 'danger'
}

function syncUploadFileList(value?: string) {
  uploadFileList.value = (value || '')
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
    .map((name, index) => ({
      name,
      url: name,
      status: 'success' as const,
      uid: Date.now() + index
    }))
}

function resetFormData() {
  formData.id = null
  formData.studentId = null
  formData.patentName = ''
  formData.patentNo = ''
  formData.patentType = PATENT_TYPE.INVENT
  formData.applicant = ''
  formData.inventors = ''
  formData.inventorRank = 1
  formData.applicantRank = 1
  formData.applicationDate = ''
  formData.patentStatus = PATENT_STATUS.APPLYING
  formData.attachmentUrl = ''
  formData.remark = ''
  uploadFileList.value = []
}

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getPatentPage(queryParams)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (error: any) {
    if (isRequestCanceled(error)) return
    ElMessage.error(`获取专利列表失败：${error?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.current = 1
  fetchData()
}

function handleReset() {
  queryParams.current = 1
  queryParams.keyword = ''
  queryParams.auditStatus = undefined
  fetchData()
}

function handleSizeChange() {
  queryParams.current = 1
  fetchData()
}

function handleCurrentChange() {
  fetchData()
}

function handleAdd() {
  isEditMode.value = false
  dialogTitle.value = '新增专利'
  resetFormData()
  dialogVisible.value = true
}

function handleEdit(row: ResearchPatent) {
  isEditMode.value = true
  dialogTitle.value = '编辑专利'
  formData.id = row.id || null
  formData.studentId = row.studentId ?? null
  formData.patentName = row.patentName || ''
  formData.patentNo = row.patentNo || ''
  formData.patentType = row.patentType || PATENT_TYPE.INVENT
  formData.applicant = row.applicant || ''
  formData.inventors = row.inventors || ''
  formData.inventorRank = row.inventorRank ?? 1
  formData.applicantRank = row.applicantRank ?? 1
  formData.applicationDate = row.applicationDate || ''
  formData.patentStatus = row.patentStatus || PATENT_STATUS.APPLYING
  formData.attachmentUrl = row.attachmentUrl || ''
  formData.remark = row.remark || ''
  syncUploadFileList(row.attachmentUrl)
  dialogVisible.value = true
}

function handleView(row: ResearchPatent) {
  currentPatent.value = row
  viewDialogVisible.value = true
}

function handleAudit(row: ResearchPatent) {
  auditForm.id = row.id || null
  auditForm.auditStatus = AUDIT_STATUS.APPROVED
  auditForm.auditComment = row.auditComment || ''
  auditDialogVisible.value = true
}

async function handleAuditSubmit() {
  if (!auditForm.id) {
    ElMessage.error('缺少专利ID')
    return
  }
  try {
    await auditPatent(auditForm.id, {
      auditStatus: auditForm.auditStatus,
      auditComment: auditForm.auditComment
    })
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(`审核失败：${error?.message || '未知错误'}`)
  }
}

async function handleDelete(row: ResearchPatent) {
  try {
    await ElMessageBox.confirm('确定要删除该专利吗？', '提示', { type: 'warning' })
    await deletePatent(row.id as number)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(`删除失败：${error?.message || '未知错误'}`)
  }
}

function handleDialogClose() {
  formRef.value?.clearValidate()
  resetFormData()
}

function handleAuditDialogClose() {
  auditForm.id = null
  auditForm.auditStatus = AUDIT_STATUS.APPROVED
  auditForm.auditComment = ''
}

function handleFileChange(_file: UploadFile, fileList: UploadFiles) {
  uploadFileList.value = fileList
}

function handleFileRemove(_file: UploadFile, fileList: UploadFiles) {
  uploadFileList.value = fileList
}

function handleExceed() {
  ElMessage.warning('最多只能上传 3 个文件')
}

async function handleSubmit() {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()

    const submitData: Partial<ResearchPatent> = {
      studentId: formData.studentId || undefined,
      patentName: formData.patentName,
      patentNo: formData.patentNo,
      patentType: formData.patentType,
      applicant: formData.applicant,
      inventors: formData.inventors,
      inventorRank: formData.inventorRank || undefined,
      applicantRank: formData.applicantRank || undefined,
      applicationDate: formData.applicationDate || undefined,
      patentStatus: formData.patentStatus,
      attachmentUrl: uploadFileList.value.map(file => file.name).join(','),
      remark: formData.remark
    }

    if (isEditMode.value && formData.id) {
      await updatePatent({ id: formData.id, ...submitData })
      ElMessage.success('更新成功')
    } else {
      await addPatent(submitData as Omit<ResearchPatent, 'id'>)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(`操作失败：${error?.message || '未知错误'}`)
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.patent-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
