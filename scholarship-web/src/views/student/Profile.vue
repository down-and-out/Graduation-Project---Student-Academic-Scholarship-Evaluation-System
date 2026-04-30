<!--
  学生个人信息页面
  研究生可以查看和编辑自己的个人信息
-->
<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人信息</h2>
    </div>

    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" link @click="isEdit = !isEdit">
            {{ isEdit ? '取消编辑' : '编辑信息' }}
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        label-position="left"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="formData.studentNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="formData.name" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-input :model-value="GENDER_TEXT_MAP[formData.gender] ?? '未知'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input
                :model-value="isEdit ? formData.idCard : maskIdCard(formData.idCard)"
                :disabled="!isEdit"
                @update:model-value="formData.idCard = $event"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入学年份">
              <el-input v-model="formData.enrollmentYear" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历层次">
              <el-input :model-value="EDUCATION_LEVEL_TEXT_MAP[formData.educationLevel] ?? '未知'" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="院系">
              <el-input v-model="formData.department" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input v-model="formData.major" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="导师">
              <el-input v-model="formData.tutorName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="研究方向">
              <el-input v-model="formData.direction" :disabled="!isEdit" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-input v-model="formData.politicalStatus" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="formData.nation" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="籍贯" prop="nativePlace">
          <el-input v-model="formData.nativePlace" :disabled="!isEdit" />
        </el-form-item>

        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="formData.address" :disabled="!isEdit" />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" :disabled="!isEdit" />
        </el-form-item>

        <el-form-item label="电子邮箱">
          <el-input v-model="formData.email" :disabled="!isEdit" />
        </el-form-item>

        <el-form-item v-if="isEdit">
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMyInfo, updateMyInfo } from '@/api/student'
import type { Student } from '@/api/student'
import { EDUCATION_LEVEL_TEXT_MAP, GENDER_TEXT_MAP } from '@/constants/user'
import { extractApiData, isValidIdCard, maskIdCard } from '@/utils/helpers'

const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance | null>(null)
const formData = reactive<Student>({
  id: 0,
  studentNo: '',
  name: '',
  gender: 1,
  idCard: '',
  enrollmentYear: 0,
  educationLevel: 1,
  department: '',
  major: '',
  tutorId: 0,
  tutorName: '',
  direction: '',
  politicalStatus: '',
  nation: '',
  nativePlace: '',
  address: '',
  phone: '',
  email: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email' as const, message: '请输入正确的邮箱地址', trigger: 'blur' }],
  idCard: [
    {
      validator: (_rule, value, callback) => {
        if (!value) { callback(); return }
        if (!isValidIdCard(value as string)) {
          callback(new Error('请输入正确的身份证号'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  nativePlace: [{ required: true, message: '请输入籍贯', trigger: 'blur' }],
  address: [{ required: true, message: '请输入家庭住址', trigger: 'blur' }]
}

async function loadInfo(): Promise<void> {
  try {
    const response = await getMyInfo()
    const profile = extractApiData<Student>(response)
    if (!profile) {
      throw new Error('学生信息为空')
    }

    Object.keys(formData).forEach((key) => {
      const field = key as keyof Student
      if (profile[field] !== undefined) {
        formData[field] = profile[field] as never
      }
    })
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载学生信息失败')
  }
}

async function handleSave(): Promise<void> {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateMyInfo({
      phone: formData.phone,
      email: formData.email,
      direction: formData.direction,
      idCard: formData.idCard,
      nativePlace: formData.nativePlace,
      address: formData.address
    })
    ElMessage.success('保存成功')
    isEdit.value = false
    await loadInfo()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function handleCancel(): void {
  isEdit.value = false
  loadInfo()
}

onMounted(() => {
  loadInfo()
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 20px;
}

.page-header {
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

.info-card {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
