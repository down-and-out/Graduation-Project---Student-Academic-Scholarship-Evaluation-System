<template>
  <div class="achievements-page">
    <div class="page-header">
      <h2 class="page-title">绉戠爺鎴愭灉绠＄悊</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        娣诲姞鎴愭灉
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="瀹℃牳鐘舵€?">
        <el-select v-model="queryParams.status" placeholder="璇烽€夋嫨" clearable>
          <el-option label="鍏ㄩ儴" value="" />
          <el-option label="寰呭鏍?" :value="0" />
          <el-option label="宸查€氳繃" :value="1" />
          <el-option label="鏈€氳繃" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">鏌ヨ</el-button>
        <el-button @click="handleReset">閲嶇疆</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
      <el-table-column type="index" label="搴忓彿" width="60" />
      <el-table-column prop="title" label="鎴愭灉鍚嶇О" min-width="200" />
      <el-table-column prop="journalName" label="鏈熷垔鍚嶇О" min-width="180" />
      <el-table-column prop="authorRank" label="浣滆€呮帓鍚?" width="100" />
      <el-table-column prop="status" label="瀹℃牳鐘舵€?" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning">寰呭鏍?</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">宸查€氳繃</el-tag>
          <el-tag v-else-if="row.status === 3" type="danger">鏈€氳繃</el-tag>
          <el-tag v-else type="info">鏈煡</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="鍒涘缓鏃堕棿" width="160" />
      <el-table-column label="鎿嶄綔" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">鏌ョ湅</el-button>
          <el-button link type="primary" :disabled="row.status !== 0" @click="handleEdit(row)">缂栬緫</el-button>
          <el-button link type="danger" :disabled="row.status !== 0" @click="handleDelete(row)">鍒犻櫎</el-button>
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
      @size-change="handleQuery"
      @current-change="handleQuery"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="璁烘枃鏍囬" prop="paperTitle">
          <el-input v-model="formData.paperTitle" placeholder="璇疯緭鍏ヨ鏂囨爣棰?" />
        </el-form-item>
        <el-form-item label="浣滆€呭垪琛?" prop="authors">
          <el-input v-model="formData.authors" placeholder="璇疯緭鍏ヤ綔鑰呭垪琛紝浣跨敤閫楀彿鍒嗛殧" />
        </el-form-item>
        <el-form-item label="浣滆€呮帓鍚?" prop="authorRank">
          <el-select v-model="formData.authorRank" placeholder="璇烽€夋嫨">
            <el-option label="绗竴浣滆€?" :value="1" />
            <el-option label="绗簩浣滆€?" :value="2" />
            <el-option label="閫氳浣滆€?" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="鏈熷垔鍚嶇О" prop="journalName">
          <el-input v-model="formData.journalName" placeholder="璇疯緭鍏ユ湡鍒婂悕绉?" />
        </el-form-item>
        <el-form-item label="鏈熷垔绾у埆" prop="journalLevel">
          <el-select v-model="formData.journalLevel" placeholder="璇烽€夋嫨">
            <el-option label="SCI 涓€鍖?" :value="1" />
            <el-option label="SCI 浜屽尯" :value="2" />
            <el-option label="SCI 涓夊尯" :value="3" />
            <el-option label="SCI 鍥涘尯" :value="4" />
            <el-option label="EI" :value="5" />
            <el-option label="鏍稿績鏈熷垔" :value="6" />
            <el-option label="鏅€氭湡鍒?" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="褰卞搷鍥犲瓙" prop="impactFactor">
          <el-input-number v-model="formData.impactFactor" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="鍙戣〃鏃ユ湡" prop="publicationDate">
          <el-date-picker
            v-model="formData.publicationDate"
            type="date"
            placeholder="閫夋嫨鏃ユ湡"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" @click="handleSubmit">纭畾</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deletePaper, getPaperPage, submitPaper, updatePaper } from '@/api/paper'
import type { Paper, PaperPageParams } from '@/api/paper'

interface PaperForm {
  id: number | null
  paperTitle: string
  authors: string
  authorRank: number
  journalName: string
  journalLevel: number | null
  impactFactor: number | null
  publicationDate: string
}

interface PaperRow extends Paper {
  title: string
  journalName?: string
}

const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance | null>(null)
const tableData = ref<PaperRow[]>([])

const queryParams = reactive<PaperPageParams>({
  current: 1,
  size: 10,
  status: undefined
})

const formData = reactive<PaperForm>({
  id: null,
  paperTitle: '',
  authors: '',
  authorRank: 1,
  journalName: '',
  journalLevel: null,
  impactFactor: null,
  publicationDate: ''
})

const formRules: FormRules<PaperForm> = {
  paperTitle: [{ required: true, message: '璇疯緭鍏ヨ鏂囨爣棰?', trigger: 'blur' }],
  authors: [{ required: true, message: '璇疯緭鍏ヤ綔鑰呭垪琛?', trigger: 'blur' }],
  authorRank: [{ required: true, message: '璇烽€夋嫨浣滆€呮帓鍚?', trigger: 'change' }],
  journalLevel: [{ required: true, message: '璇烽€夋嫨鏈熷垔绾у埆', trigger: 'change' }]
}

const dialogTitle = computed(() => (isEdit.value ? '缂栬緫鎴愭灉' : '娣诲姞鎴愭灉'))

function extractPageData<T>(payload: unknown): API.PageResponse<T> | null {
  if (!payload || typeof payload !== 'object') return null
  const raw = payload as Record<string, unknown>
  if (raw.data && typeof raw.data === 'object') {
    const inner = raw.data as Record<string, unknown>
    if (inner.data && typeof inner.data === 'object') {
      return inner.data as API.PageResponse<T>
    }
    return raw.data as API.PageResponse<T>
  }
  return raw as unknown as API.PageResponse<T>
}

function normalizePaper(row: Paper): PaperRow {
  return {
    ...row,
    title: row.title || row.paperTitle || '',
    journalName: row.journalName || row.journal || '',
    authorRank: row.authorRank ?? 1,
    journalLevel: row.journalLevel ?? undefined,
    impactFactor: row.impactFactor ?? undefined,
    publicationDate: row.publicationDate || row.publishDate || row.date || ''
  }
}

function resetForm(): void {
  Object.assign(formData, {
    id: null,
    paperTitle: '',
    authors: '',
    authorRank: 1,
    journalName: '',
    journalLevel: null,
    impactFactor: null,
    publicationDate: ''
  })
}

async function handleQuery(): Promise<void> {
  loading.value = true
  try {
    const response = await getPaperPage(queryParams)
    const pageData = extractPageData<Paper>(response)
    tableData.value = (pageData?.records || []).map(normalizePaper)
    total.value = pageData?.total || 0
  } catch (error) {
    console.error('鏌ヨ澶辫触:', error)
  } finally {
    loading.value = false
  }
}

function handleReset(): void {
  queryParams.status = undefined
  queryParams.current = 1
  handleQuery()
}

function handleAdd(): void {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleView(_row: PaperRow): void {
  ElMessage.info('鏌ョ湅璇︽儏鍔熻兘寮€鍙戜腑')
}

function handleEdit(row: PaperRow): void {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id ?? null,
    paperTitle: row.title || '',
    authors: row.authors,
    authorRank: row.authorRank ?? 1,
    journalName: row.journalName || '',
    journalLevel: row.journalLevel ?? null,
    impactFactor: row.impactFactor ?? null,
    publicationDate: row.publicationDate || row.publishDate || ''
  })
  dialogVisible.value = true
}

function handleDelete(row: PaperRow): void {
  ElMessageBox.confirm('纭畾瑕佸垹闄よ鎴愭灉鍚楋紵', '鎻愮ず', {
    confirmButtonText: '纭畾',
    cancelButtonText: '鍙栨秷',
    type: 'warning'
  })
    .then(async () => {
      await deletePaper(row.id || 0)
      ElMessage.success('鍒犻櫎鎴愬姛')
      await handleQuery()
    })
    .catch(() => undefined)
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const payload = {
      studentId: 0,
      title: formData.paperTitle,
      paperTitle: formData.paperTitle,
      authors: formData.authors,
      journalName: formData.journalName,
      journal: formData.journalName,
      authorRank: formData.authorRank,
      journalLevel: formData.journalLevel ?? undefined,
      impactFactor: formData.impactFactor ?? undefined,
      publicationDate: formData.publicationDate,
      publishDate: formData.publicationDate
    }

    if (isEdit.value && formData.id) {
      await updatePaper(formData.id, payload)
    } else {
      await submitPaper(payload)
    }

    ElMessage.success(isEdit.value ? '淇敼鎴愬姛' : '娣诲姞鎴愬姛')
    dialogVisible.value = false
    await handleQuery()
  } catch (error) {
    console.error('鎻愪氦澶辫触:', error)
  }
}

function handleDialogClose(): void {
  formRef.value?.resetFields()
  resetForm()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.achievements-page {
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
</style>
