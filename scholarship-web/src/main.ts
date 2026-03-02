/**
 * Vue 应用入口文件
 * 功能：创建 Vue 应用实例，配置全局插件和样式
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

// 中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import '@/styles/index.scss'

// 导入错误处理器
import { ErrorHandler } from '@/utils/errorHandler'

/**
 * 创建 Vue 应用实例
 */
const app = createApp(App)

/**
 * 创建 Pinia 状态管理实例
 */
const pinia = createPinia()

/**
 * 注册全局插件
 */
app.use(pinia)           // 注册状态管理
app.use(router)          // 注册路由
app.use(ElementPlus, {   // 注册 Element Plus 组件库
  locale: zhCn,          // 设置中文语言
})

/**
 * 注册全局错误处理器
 */
app.config.errorHandler = (error, instance, info) => {
  console.error('Vue 全局错误:', error)
  console.error('组件实例:', instance)
  console.error('错误信息:', info)

  // 使用 ErrorHandler 捕获错误
  ErrorHandler.getInstance().capture(error, instance, info)
}

/**
 * 全局未捕获的 Promise 拒绝处理
 */
window.addEventListener('unhandledrejection', (event) => {
  console.error('未捕获的 Promise 拒绝:', event.reason)
  ErrorHandler.getInstance().capture(event.reason, null, 'unhandledrejection')
})

/**
 * 全局错误处理
 */
window.addEventListener('error', (event) => {
  console.error('全局错误:', event.message)
  console.error('错误堆栈:', event.error?.stack)
  ErrorHandler.getInstance().capture(event.error || event.message, null, 'window.onerror')
})

/**
 * 挂载应用
 */
app.mount('#app')
