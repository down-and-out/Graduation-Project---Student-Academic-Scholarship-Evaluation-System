<!--
  研究生学业奖学金评定系统 - 根组件
  这是应用的根组件，所有其他组件都是它的子组件
-->
<template>
  <!-- 错误边界 - 捕获子组件渲染期间的错误 -->
  <ErrorBoundary v-slot="{ retry }">
    <!-- 路由视图 - 根据当前路由渲染对应的页面组件 -->
    <router-view />
  </ErrorBoundary>
</template>

<script setup lang="ts">
import { onErrorCaptured } from 'vue'
import { ElMessage } from 'element-plus'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import { reportError } from '@/utils/errorHandler'

// 应用级别的全局错误捕获
onErrorCaptured((error, instance, info) => {
  console.error('App.vue 捕获到错误:', error)
  console.error('组件实例:', instance)
  console.error('错误信息:', info)

  // 上报错误
  reportError({ error, instance, info })

  // 显示错误提示
  ElMessage.error('组件加载失败，请刷新页面重试')

  // 阻止错误继续向上传播
  return false
})
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji',
    'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}
</style>
