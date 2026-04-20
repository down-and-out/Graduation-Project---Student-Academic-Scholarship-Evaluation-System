/**
 * Axios 请求封装模块
 * 统一处理 HTTP 请求，包括请求拦截、响应拦截、错误处理
 */
import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { tokenStore } from '@/utils/secureStorage'
import * as resultCode from '@/constants/resultCode'
import { generateUUID } from '@/utils/helpers'

// Token 相关错误码数组，用于统一处理
const TOKEN_ERROR_CODES = [
  resultCode.UNAUTHORIZED,
  resultCode.TOKEN_EXPIRED,
  resultCode.TOKEN_INVALID,
  resultCode.TOKEN_BLACKLISTED
]

/**
 * 扩展 AxiosRequestConfig 类型，添加自定义属性
 */
interface CustomRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',           // API 基础路径
  timeout: 15000,            // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 登出状态标记
const isLoggingOut = { value: false }

/**
 * 处理登出和跳转登录页
 * 使用标记防止重复登出
 */
function handleLogoutAndRedirect(): void {
  if (isLoggingOut.value) return

  isLoggingOut.value = true

  try {
    const userStore = useUserStore()
    userStore.clearUserState()

    // 异步登出，不阻塞
    userStore.logout()
      .catch(() => { /* 忽略登出错误 */ })
      .finally(() => {
        isLoggingOut.value = false
        window.location.href = '/login'
      })
  } catch (error) {
    console.error('登出处理异常:', error)
    isLoggingOut.value = false
    window.location.href = '/login'
  }
}

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  (config: CustomRequestConfig) => {
    // 从 secureStorage 获取 token（内存存储）
    const token = tokenStore.get()

    // 如果 token 存在，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 防止重复提交：为特定请求添加请求 ID
    if (config.method === 'post' || config.method === 'put') {
      // 优先使用 crypto API，降级使用 helpers 中的实现
      if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        config.headers['X-Request-ID'] = crypto.randomUUID()
      } else {
        config.headers['X-Request-ID'] = generateUUID()
      }
    }

    return config
  },
  (error) => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  (response: AxiosResponse<API.Response>) => {
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }

    const res = response.data

    // 如果响应码不是 200，视为错误
    if (res.code !== resultCode.SUCCESS) {
      // 获取错误消息
      const message = res.message || resultCode.getMessage(res.code) || '请求失败'

      // Token 过期或无效：跳转登录页
      if (TOKEN_ERROR_CODES.includes(res.code)) {
        handleLogoutAndRedirect()
      } else {
        // 其他错误显示提示消息
        ElMessage.error(message)
      }

      return Promise.reject(new Error(message))
    }

    // 返回响应数据（保持 AxiosResponse 格式）
    return response
  },
  (error) => {
    console.error('响应错误:', error)

    // 处理网络错误
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || resultCode.getMessage(status) || '请求失败'

      switch (status) {
        case resultCode.BAD_REQUEST:
          ElMessage.error(message || '请求参数错误')
          break
        case resultCode.UNAUTHORIZED:
          ElMessage.error('未授权，请先登录')
          handleLogoutAndRedirect()
          break
        case resultCode.FORBIDDEN:
          ElMessage.error('禁止访问')
          break
        case resultCode.NOT_FOUND:
          ElMessage.error('请求的资源不存在')
          break
        case resultCode.TOO_MANY_REQUESTS:
          ElMessage.error('请求次数过多，请稍后重试')
          break
        case resultCode.INTERNAL_SERVER_ERROR:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(message)
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default request
