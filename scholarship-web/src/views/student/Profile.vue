<!--
  学生个人信息页面
  研究生可以查看和编辑自己的个人信息
-->
<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">个人信息</h2>
    </div>

    <!-- 信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" link @click="isEdit = !isEdit">
            {{ isEdit ? '取消编辑' : '编辑信息' }}
          </el-button>
        </div>
      </template>

      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px" label-position="left">
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
              <el-select v-model="formData.gender" disabled style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="formData.idCard" disabled />
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
              <el-select v-model="formData.educationLevel" disabled style="width: 100%">
                <el-option label="硕士" :value="1" />
                <el-option label="博士" :value="2" />
              </el-select>
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

        <el-form-item label="籍贯">
          <el-input v-model="formData.nativePlace" disabled />
        </el-form-item>

        <el-form-item label="家庭住址">
          <el-input v-model="formData.address" disabled />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" :disabled="!isEdit" />
        </el-form-item>

        <el-form-item label="电子邮箱">
          <el-input v-model="formData.email" :disabled="!isEdit" />
        </el-form-item>

        <el-form-item v-if="isEdit">
          <el-button type="primary" @click="handleSave">保存修改</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getMyInfo, updateMyInfo } from '@/api/student'
import type { Student } from '@/api/student'
import { SUCCESS } from '@/constants/resultCode'

/**
 * 编辑状态
 */
const isEdit = ref(false)

/**
 * 表单引用
 */
const formRef = ref<FormInstance | null>(null)

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 表单数据
 */
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

/**
 * 表单验证规则
 */
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

/**
 * 加载学生信息
 */
async function loadInfo(): Promise<void> {
  loading.value = true
  try {
    const res = await getMyInfo()
    // 检查响应状态码是否为成功（200）
    if (res.code === SUCCESS && res.data) {
      const studentData = res.data
      Object.keys(formData).forEach((key) => {
        if (studentData[key as keyof Student] !== undefined) {
          formData[key as keyof Student] = studentData[key as keyof Student]
        }
      })
    }
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载学生信息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 保存修改
 */
async function handleSave(): Promise<void> {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // 只传递允许学生修改的字段
    await updateMyInfo({
      phone: formData.phone,
      email: formData.email,
      direction: formData.direction
    })
    ElMessage.success('保存成功')
    isEdit.value = false
    await loadInfo()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

/**
 * 取消编辑，恢复原始数据
 */
function handleCancel(): void {
  isEdit.value = false
  loadInfo()
}

// ========== 生命周期 ==========
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
    font-size: 18px;
    font-weight: 500;
    color: #303133;
    margin: 0;
  }
}

.info-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
