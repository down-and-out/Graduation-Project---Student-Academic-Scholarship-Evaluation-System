<!--
  404页面组件
  当访问的页面不存在时显示
-->
<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <!-- 404图标 -->
      <div class="error-code">404</div>

      <!-- 错误信息 -->
      <div class="error-message">
        <h2>页面不存在</h2>
        <p>抱歉，您访问的页面不存在或已被删除</p>
      </div>

      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          返回首页
        </el-button>
        <el-button @click="goBack">
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * 返回首页
 */
function goHome() {
  router.push('/app/dashboard')
}

/**
 * 返回上一页
 */
function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: #f5f7fa;
}

.not-found-content {
  text-align: center;

  .error-code {
    font-size: 120px;
    font-weight: bold;
    color: #409eff;
    line-height: 1;
    margin-bottom: 20px;
  }

  .error-message {
    margin-bottom: 30px;

    h2 {
      font-size: 24px;
      color: #303133;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .error-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}
</style>
