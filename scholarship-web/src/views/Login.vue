<!--
  登录页面组件
  功能：用户输入用户名和密码进行登录

  改进：
  1. 添加 JSDoc 类型注释
  2. 优化错误处理
  3. 移除硬编码的提示文字
-->
<template>
  <div class="login-container">
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 系统标题 -->
      <div class="login-header">
        <h1 class="login-title">研究生学业奖学金评定系统</h1>
        <p class="login-subtitle">Scholarship Evaluation System</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            size="large"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            :disabled="loading"
          />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button
          type="primary"
          size="large"
          class="login-button"
          :loading="loading"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>

      <!-- 提示信息 -->
      <div class="login-tips">
        <p>请使用学校统一账号密码登录</p>
        <p class="login-help">忘记密码请联系管理员重置</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { encryptPassword } from '@/utils/rsa'
import { isValidUsername } from '@/utils/helpers'
import type { LoginParams } from '@/api/auth'

/**
 * 路由实例
 */
const router = useRouter()
const route = useRoute()

/**
 * 用户状态管理
 */
const userStore = useUserStore()

/**
 * 登录表单数据
 */
const loginForm = reactive<LoginParams>({
  username: '',
  password: ''
})

/**
 * 密码长度验证器
 * @param rule - 验证规则
 * @param value - 密码值
 * @param callback - 回调函数
 */
const validatePasswordLength = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (value.length < 10) {
    callback(new Error('密码长度至少为 10 个字符'))
  } else {
    callback()
  }
}

/**
 * 密码复杂度验证器（必须包含字母和数字）
 * @param rule - 验证规则
 * @param value - 密码值
 * @param callback - 回调函数
 */
const validatePasswordComplexity = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (!/(?=.*[0-9])(?=.*[a-zA-Z])/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

/**
 * 密码字符合法性验证器（只能包含字母和数字）
 * @param rule - 验证规则
 * @param value - 密码值
 * @param callback - 回调函数
 */
const validatePasswordChars = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (!/^[a-zA-Z0-9]+$/.test(value)) {
    callback(new Error('密码只能包含字母和数字'))
  } else {
    callback()
  }
}

/**
 * 登录表单验证规则
 */
const loginRules = reactive<FormRules<LoginParams>>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isValidUsername(value)) {
          callback(new Error('用户名只能包含字母、数字和下划线，且必须以字母开头，长度为 3-20 个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePasswordLength, trigger: 'blur' },
    { validator: validatePasswordComplexity, trigger: 'blur' },
    { validator: validatePasswordChars, trigger: 'blur' }
  ]
})

/**
 * 登录中状态
 */
const loading = ref(false)

/**
 * 表单引用
 */
const loginFormRef = ref<FormInstance | null>(null)

/**
 * 处理登录
 */
async function handleLogin(): Promise<void> {
  if (!loginFormRef.value) return

  // 表单验证
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  // 防止重复提交
  if (loading.value) {
    return
  }

  // 开始登录
  loading.value = true

  try {
    // 使用 RSA 加密密码
    const encryptedPassword = encryptPassword(loginForm.password)

    const result = await userStore.login({
      username: loginForm.username,
      password: encryptedPassword
    })

    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/app/dashboard')
    } else {
      ElMessage.error(result.error || '登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error('登录异常:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
}

.login-button {
  width: 100%;
}

.login-tips {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #909399;

  .login-help {
    margin-top: 4px;
    color: #c0c4cc;
  }
}
</style>
