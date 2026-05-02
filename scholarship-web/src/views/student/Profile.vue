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
/**
 * 学生个人信息页面逻辑
 * 功能：查看和编辑个人基本信息、联系方式等
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMyInfo, updateMyInfo } from '@/api/student'
import type { Student } from '@/api/student'
import { EDUCATION_LEVEL_TEXT_MAP, GENDER_TEXT_MAP } from '@/constants/user'
import { extractApiData, isRequestCanceled, isValidIdCard, maskIdCard } from '@/utils/helpers'

// 是否处于编辑模式
const isEdit = ref(false)
// 保存按钮loading状态
const saving = ref(false)
// 表单引用，用于表单验证
const formRef = ref<FormInstance | null>(null)
// 表单数据，存储学生个人信息
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

// 表单验证规则
const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email' as const, message: '请输入正确的邮箱地址', trigger: 'blur' }],
  idCard: [
    {
      validator: (_rule, value, callback) => {
        if (!value) { callback(); return }
        // 验证身份证号格式是否合法
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

/**
 * 加载当前学生的个人信息
 * 从API获取数据并填充到表单
 */
async function loadInfo(): Promise<void> {
  try {
    const response = await getMyInfo()
    const profile = extractApiData<Student>(response)
    if (!profile) {
      throw new Error('学生信息为空')
    }

    // 将获取到的信息合并到表单数据中
    Object.keys(formData).forEach((key) => {
      const field = key as keyof Student
      if (profile[field] !== undefined) {
        formData[field] = profile[field] as never
      }
    })
  } catch (error) {
    console.error('加载学生信息失败:', error)
    if (isRequestCanceled(error)) return
    ElMessage.error('加载学生信息失败')
  }
}

/**
 * 保存修改后的个人信息
 * 验证表单后提交到后端API
 */
async function handleSave(): Promise<void> {
  if (!formRef.value) return

  // 先进行表单验证，验证失败则不提交
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 仅提交可编辑的字段：电话、邮箱、研究方向、身份证号、籍贯、家庭住址
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

/**
 * 取消编辑，恢复原数据
 * 直接重新加载数据，不保留编辑内容
 */
function handleCancel(): void {
  isEdit.value = false
  loadInfo()
}

// 页面加载时自动获取个人信息
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
