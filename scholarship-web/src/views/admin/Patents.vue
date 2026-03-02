<!--
  管理员 - 结果管理页面
  管理员端的专利管理页面，用于管理学生的专利信息。
-->
<template>
  <div class="patent-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="学生姓名">
          <el-input v-model="queryParams.keyword" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" placeholder="请选择" clearable>
            <el-option label="待审核" :value="AUDIT_STATUS.PENDING" />
            <el-option label="审核通过" :value="AUDIT_STATUS.APPROVED" />
            <el-option label="审核驳回" :value="AUDIT_STATUS.REJECTED" />
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
        <el-table-column prop="patentNo" label="专利号" width="150" />
        <el-table-column prop="patentType" label="专利类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.patentType === PATENT_TYPE.INVENT" type="danger">发明专利</el-tag>
            <el-tag v-else-if="row.patentType === PATENT_TYPE.UTILITY" type="success">实用新型</el-tag>
            <el-tag v-else type="warning">外观设计</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantRank" label="申请人排名" width="100" />
        <el-table-column prop="applicationDate" label="申请日期" width="120" />
        <el-table-column prop="patentStatus" label="专利状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.patentStatus === PATENT_STATUS.APPLYING" type="info">申请中</el-tag>
            <el-tag v-else-if="row.patentStatus === PATENT_STATUS.AUTHORIZED" type="success">已授权</el-tag>
            <el-tag v-else type="danger">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="获得分数" width="100" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === AUDIT_STATUS.PENDING" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === AUDIT_STATUS.APPROVED" type="success">通过</el-tag>
            <el-tag v-else type="danger">驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleAudit(row)" v-if="row.auditStatus === AUDIT_STATUS.PENDING">审核</el-button>
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
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="专利名称" prop="patentName">
          <el-input v-model="formData.patentName" placeholder="请输入专利名称" />
        </el-form-item>
        <el-form-item label="专利号" prop="patentNo">
          <el-input v-model="formData.patentNo" placeholder="请输入专利号" />
        </el-form-item>
        <el-form-item label="专利类型" prop="patentType">
          <el-select v-model="formData.patentType" placeholder="请选择专利类型">
            <el-option label="发明专利" :value="PATENT_TYPE.INVENT" />
            <el-option label="实用新型" :value="PATENT_TYPE.UTILITY" />
            <el-option label="外观设计" :value="PATENT_TYPE.DESIGN" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人排名" prop="applicantRank">
          <el-input-number v-model="formData.applicantRank" :min="1" placeholder="请输入排名" />
        </el-form-item>
        <el-form-item label="申请日期" prop="applicationDate">
          <el-date-picker
            v-model="formData.applicationDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="授权日期" prop="authorizationDate">
          <el-date-picker
            v-model="formData.authorizationDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="专利状态" prop="patentStatus">
          <el-select v-model="formData.patentStatus" placeholder="请选择专利状态">
            <el-option label="申请中" :value="PATENT_STATUS.APPLYING" />
            <el-option label="已授权" :value="PATENT_STATUS.AUTHORIZED" />
            <el-option label="已驳回" :value="PATENT_STATUS.REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="证明材料" prop="proofMaterials">
          <el-upload
            action="#"
            :file-list="uploadFileList"
            :limit="3"
            :on-exceed="handleExceed"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
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

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="专利详情" width="600px">
      <el-descriptions :column="2" border v-if="currentPatent">
        <el-descriptions-item label="专利名称">{{ currentPatent.patentName }}</el-descriptions-item>
        <el-descriptions-item label="专利号">{{ currentPatent.patentNo }}</el-descriptions-item>
        <el-descriptions-item label="专利类型">
          <el-tag v-if="currentPatent.patentType === PATENT_TYPE.INVENT" type="danger">发明专利</el-tag>
          <el-tag v-else-if="currentPatent.patentType === PATENT_TYPE.UTILITY" type="success">实用新型</el-tag>
          <el-tag v-else type="warning">外观设计</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人排名">{{ currentPatent.applicantRank }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ currentPatent.applicationDate }}</el-descriptions-item>
        <el-descriptions-item label="授权日期">{{ currentPatent.authorizationDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专利状态">
          <el-tag v-if="currentPatent.patentStatus === PATENT_STATUS.APPLYING" type="info">申请中</el-tag>
          <el-tag v-else-if="currentPatent.patentStatus === PATENT_STATUS.AUTHORIZED" type="success">已授权</el-tag>
          <el-tag v-else type="danger">已驳回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="获得分数">{{ currentPatent.score }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="currentPatent.auditStatus === AUDIT_STATUS.PENDING" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentPatent.auditStatus === AUDIT_STATUS.APPROVED" type="success">通过</el-tag>
          <el-tag v-else type="danger">驳回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentPatent.auditComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPatent.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
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

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPatentPage, addPatent, updatePatent, deletePatent, auditPatent } from '@/api/patent'

// 常量定义
const PATENT_TYPE = {
  INVENT: 1,      // 发明专利
  UTILITY: 2,     // 实用新型
  DESIGN: 3       // 外观设计
}

const PATENT_STATUS = {
  APPLYING: 1,    // 申请中
  AUTHORIZED: 2,  // 已授权
  REJECTED: 3     // 已驳回
}

const AUDIT_STATUS = {
  PENDING: 0,     // 待审核
  APPROVED: 1,    // 审核通过
  REJECTED: 2     // 审核驳回
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  auditStatus: null
})

const dialogVisible = ref(false)
const isEditMode = ref(false)
const dialogTitle = ref('新增专利')
const formRef = ref(null)

const formData = reactive({
  id: null,
  patentName: '',
  patentNo: '',
  patentType: PATENT_TYPE.INVENT,
  applicantRank: 1,
  applicationDate: '',
  authorizationDate: '',
  patentStatus: PATENT_STATUS.APPLYING,
  proofMaterials: '',
  remark: ''
})

const formRules = {
  patentName: [{ required: true, message: '请输入专利名称', trigger: 'blur' }],
  patentNo: [{ required: true, message: '请输入专利号', trigger: 'blur' }],
  patentType: [{ required: true, message: '请选择专利类型', trigger: 'change' }],
  applicantRank: [{ required: true, message: '请输入申请人排名', trigger: 'blur' }]
}

// 文件上传相关
const uploadFileList = ref([])

const handleFileChange = (file, fileList) => {
  uploadFileList.value = fileList
}

const handleFileRemove = (file, fileList) => {
  uploadFileList.value = fileList
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 3 个文件')
}

// 审核相关
const auditDialogVisible = ref(false)
const auditForm = reactive({
  id: null,
  auditStatus: AUDIT_STATUS.APPROVED,
  auditComment: ''
})

const handleAuditDialogClose = () => {
  auditForm.id = null
  auditForm.auditStatus = AUDIT_STATUS.APPROVED
  auditForm.auditComment = ''
}

// 查看详情相关
const viewDialogVisible = ref(false)
const currentPatent = ref(null)

// 查询数据
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getPatentPage(queryParams)
    tableData.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取专利列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.current = 1
  fetchData()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.auditStatus = null
  handleQuery()
}

const handleAdd = () => {
  isEditMode.value = false
  dialogTitle.value = '新增专利'
  dialogVisible.value = true
}

const handleView = (row) => {
  currentPatent.value = row
  viewDialogVisible.value = true
}

const handleAudit = (row) => {
  auditForm.id = row.id
  auditDialogVisible.value = true
}

const handleAuditSubmit = async () => {
  try {
    await auditPatent(auditForm.id, auditForm.auditStatus, auditForm.auditComment)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('审核失败：' + (error.message || '未知错误'))
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该专利吗？', '提示', { type: 'warning' })
    await deletePatent(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error('删除失败：' + (error.message || '未知错误'))
  }
}

const handleSizeChange = () => {
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  uploadFileList.value = []
  formData.id = null
  formData.patentName = ''
  formData.patentNo = ''
  formData.patentType = PATENT_TYPE.INVENT
  formData.applicantRank = 1
  formData.applicationDate = ''
  formData.authorizationDate = ''
  formData.patentStatus = PATENT_STATUS.APPLYING
  formData.proofMaterials = ''
  formData.remark = ''
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    const submitData = {
      patentName: formData.patentName,
      patentNo: formData.patentNo,
      patentType: formData.patentType,
      applicantRank: formData.applicantRank,
      applicationDate: formData.applicationDate,
      authorizationDate: formData.authorizationDate,
      patentStatus: formData.patentStatus,
      proofMaterials: uploadFileList.value.map(f => f.name).join(','),
      remark: formData.remark
    }

    if (isEditMode.value && formData.id) {
      await updatePatent({ id: formData.id, ...submitData })
      ElMessage.success('更新成功')
    } else {
      await addPatent(submitData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败：' + (error.message || '未知错误'))
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
