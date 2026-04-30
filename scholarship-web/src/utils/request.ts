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
  _dedupKey?: string
}

// ========== GET 请求去重 ==========

/** 去重 TTL（毫秒） */
const DEDUP_TTL = 5000

/** Deferred 辅助：允许外部 resolve/reject 的 Promise */
interface Deferred<T> {
  promise: Promise<T>
  resolve: (value: T) => void
  reject: (reason: unknown) => void
}

function createDeferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void
  let reject!: (reason: unknown) => void
  const promise = new Promise<T>((res, rej) => {
    resolve = res
    reject = rej
  })
  return { promise, resolve, reject }
}

/** 去重 Map：key → { deferred, timestamp } */
interface PendingEntry {
  deferred: Deferred<AxiosResponse>
  timestamp: number
}
const pendingMap = new Map<string, PendingEntry>()

/**
 * 参数序列化（与项目各处 paramsSerializer 逻辑一致）
 * - 按 key 字母排序
 * - 过滤 undefined / null / 空字符串
 * - 数组值用逗号 join
 */
function serializeParams(params: Record<string, unknown>): string {
  const searchParams = new URLSearchParams()
  const sortedKeys = Object.keys(params).sort()
  for (const key of sortedKeys) {
    const value = params[key]
    if (value === undefined || value === null || value === '') continue
    if (Array.isArray(value)) {
      const normalized = value
        .filter(item => item !== undefined && item !== null && item !== '')
        .map(item => String(item))
      if (normalized.length > 0) {
        searchParams.append(key, normalized.join(','))
      }
    } else {
      searchParams.append(key, String(value))
    }
  }
  return searchParams.toString()
}

/**
 * 生成去重 key：GET:${url}:${稳定序列化的params}
 */
function buildDedupKey(config: InternalAxiosRequestConfig): string {
  const url = config.url || ''
  const paramsStr = config.params ? serializeParams(config.params as Record<string, unknown>) : ''
  return `GET:${url}:${paramsStr}`
}

/**
 * 判断请求是否应参与 GET 去重
 */
function isDedupEligible(config: InternalAxiosRequestConfig): boolean {
  if (config.method?.toLowerCase() !== 'get') return false
  if (config.responseType === 'blob' || config.responseType === 'arraybuffer') return false
  if (config.headers?.['X-No-Dedup']) return false
  return true
}

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',           // API 基础路径
  timeout: 15000,            // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// ========== 路由级 AbortController（导航切换时取消在途请求） ==========

/** 按路由路径分组的 AbortController */
const routeAbortControllers = new Map<string, AbortController>()

/** 当前活跃的路由路径 */
let currentRoutePath: string | null = null

/**
 * 更新当前路由路径（由 router.beforeEach 调用）
 */
export function setCurrentRoute(path: string): void {
  currentRoutePath = path
}

/**
 * 取消指定路由的所有在途请求
 */
export function abortStaleRequests(routePath: string): void {
  const controller = routeAbortControllers.get(routePath)
  if (controller) {
    controller.abort()
    routeAbortControllers.delete(routePath)
  }
}

/**
 * 获取或创建当前路由的 AbortController
 */
function getRouteAbortSignal(routePath: string): AbortSignal | undefined {
  let controller = routeAbortControllers.get(routePath)
  if (!controller || controller.signal.aborted) {
    controller = new AbortController()
    routeAbortControllers.set(routePath, controller)
  }
  return controller.signal
}

// 登出状态标记
const isLoggingOut = { value: false }

/**
 * 跳转登录页（优先 SPA 路由跳转，避免整页刷新）
 */
function redirectToLogin(): void {
  import('@/router')
    .then(module => {
      module.default.push('/login').catch(() => {
        window.location.href = '/login'
      })
    })
    .catch(() => {
      window.location.href = '/login'
    })
}

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
        redirectToLogin()
      })
  } catch (error) {
    console.error('登出处理异常:', error)
    isLoggingOut.value = false
    redirectToLogin()
  }
}

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  (config: CustomRequestConfig) => {
    // ===== GET 请求去重（优先执行，减少不必要处理） =====
    if (isDedupEligible(config)) {
      // 清理过期条目
      const now = Date.now()
      for (const [k, v] of pendingMap) {
        if (now - v.timestamp > DEDUP_TTL) {
          pendingMap.delete(k)
        }
      }

      const key = buildDedupKey(config)
      const existing = pendingMap.get(key)

      if (existing) {
        // 命中：共享同一个 deferred Promise，跳过实际 HTTP 请求
        config.adapter = () => existing.deferred.promise
        return config
      }

      // 未命中：创建 Deferred，真实响应到达时由拦截器 resolve
      const deferred = createDeferred<AxiosResponse>()
      pendingMap.set(key, { deferred, timestamp: Date.now() })
      // 将 key 写入 config，以便响应拦截器定位
      config._dedupKey = key
    }

    // ===== 路由级 AbortController：导航离开时取消在途请求 =====
    if (currentRoutePath) {
      const signal = getRouteAbortSignal(currentRoutePath)
      if (signal && !signal.aborted) {
        // 合并已有 signal（如果有的话）
        if (config.signal) {
          const combined = new AbortController()
          ;(config.signal as AbortSignal).addEventListener('abort', () => combined.abort())
          signal.addEventListener('abort', () => combined.abort())
          config.signal = combined.signal
        } else {
          config.signal = signal
        }
      }
    }

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

    // 处理 GET 去重：resolve deferred，让共享的请求收到响应
    const dedupKey = (response.config as CustomRequestConfig)._dedupKey
    if (dedupKey) {
      const pending = pendingMap.get(dedupKey)
      if (pending) {
        pending.deferred.resolve(response)
        pendingMap.delete(dedupKey)
      }
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
    // 请求被取消（路由切换或 AbortController），静默处理
    if (error.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    }

    console.error('响应错误:', error)

    // 处理 GET 去重：reject deferred，让共享的请求也收到错误
    const errorDedupKey = (error.config as CustomRequestConfig)?._dedupKey
    if (errorDedupKey) {
      const pending = pendingMap.get(errorDedupKey)
      if (pending) {
        pending.deferred.reject(error)
        pendingMap.delete(errorDedupKey)
      }
    }

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
        case resultCode.CONFLICT:
          ElMessage.warning(message || '数据已被他人修改，请刷新后重试')
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
