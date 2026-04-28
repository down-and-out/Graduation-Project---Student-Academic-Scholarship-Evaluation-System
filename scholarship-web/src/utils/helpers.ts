/**
 * 通用工具函数
 */

/**
 * 生成 UUID
 * @returns UUID 字符串
 */
export function generateUUID(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  // 降级方案
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

/**
 * 防抖函数
 * @param fn - 需要防抖的函数
 * @param delay - 延迟时间（毫秒）
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay = 300
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function (this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param fn - 需要节流的函数
 * @param delay - 延迟时间（毫秒）
 * @returns 节流后的函数
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay = 300
): (...args: Parameters<T>) => void {
  let lastTime = 0
  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      fn.apply(this, args)
      lastTime = now
    }
  }
}

/**
 * 格式化日期
 * @param date - 日期
 * @param format - 格式模板
 * @returns 格式化后的日期字符串
 */
export function formatDate(
  date: Date | string | number | null | undefined,
  format = 'YYYY-MM-DD HH:mm:ss'
): string {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化相对时间
 * @param date - 日期
 * @returns 相对时间描述
 */
export function formatRelativeTime(date: Date | string | number | null | undefined): string {
  if (!date) return ''

  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)

  if (years > 0) return `${years}年前`
  if (months > 0) return `${months}个月前`
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  if (seconds > 0) return `${seconds}秒前`
  return '刚刚'
}

/**
 * 验证手机号
 * @param phone - 手机号
 * @returns 是否有效
 */
export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱
 * @param email - 邮箱
 * @returns 是否有效
 */
export function isValidEmail(email: string): boolean {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

/**
 * 验证身份证号
 * @param idCard - 身份证号
 * @returns 是否有效
 */
export function isValidIdCard(idCard: string): boolean {
  return /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(idCard)
}

/**
 * 身份证号脱敏，保留前4位和后4位，中间用 * 替代
 * @returns 如 4301********1234，空值或长度不足返回空字符串
 */
export function maskIdCard(idCard: string | null | undefined): string {
  if (!idCard) return ''
  if (idCard.length < 8) return idCard
  return idCard.slice(0, 4) + '*'.repeat(idCard.length - 8) + idCard.slice(-4)
}

/**
 * 验证密码强度（字母 + 数字，长度至少 10 位）
 * @param password - 密码
 * @returns 是否符合要求
 */
export function isValidPassword(password: string): boolean {
  if (!password || password.length < 10) return false
  const hasLower = /[a-z]/.test(password)
  const hasUpper = /[A-Z]/.test(password)
  const hasDigit = /[0-9]/.test(password)
  const hasSpecial = /[^a-zA-Z0-9]/.test(password)
  // 至少满足三类字符要求（大写字母、小写字母、数字、特殊字符）
  const categoryCount = [hasLower, hasUpper, hasDigit, hasSpecial].filter(Boolean).length
  return categoryCount >= 3
}

/**
 * 验证用户名（字母开头，只能包含字母、数字和下划线）
 * @param username - 用户名
 * @returns 是否符合要求
 */
export function isValidUsername(username: string): boolean {
  return /^[a-zA-Z][a-zA-Z0-9_]{2,19}$/.test(username)
}

/**
 * 格式化文件大小
 * @param bytes - 字节数
 * @returns 格式化后的大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 深拷贝
 * @param obj - 需要拷贝的对象
 * @returns 拷贝后的对象
 */
export function deepClone<T>(obj: T, visited = new WeakMap<object, unknown>()): T {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as any
  if (obj instanceof RegExp) return new RegExp(obj.source, obj.flags) as any

  if (visited.has(obj as object)) {
    return visited.get(obj as object) as T
  }

  if (Array.isArray(obj)) {
    const arr: unknown[] = []
    visited.set(obj as object, arr)
    obj.forEach(item => { arr.push(deepClone(item, visited)) })
    return arr as unknown as T
  }

  const cloned = {} as T
  visited.set(obj as object, cloned)
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      ;(cloned as any)[key] = deepClone((obj as any)[key], visited)
    }
  }
  return cloned
}

/**
 * sleep 函数
 * @param ms - 毫秒数
 * @returns Promise
 */
export function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 从 API 响应中提取实际数据载荷
 * @param payload - API 响应数据
 * @returns 实际数据或 null
 */
export function extractApiData<T>(payload: unknown): T | null {
  if (!payload || typeof payload !== 'object') return null
  const raw = payload as Record<string, unknown>
  if (raw.data && typeof raw.data === 'object') {
    const inner = raw.data as Record<string, unknown>
    if (inner.data !== undefined) {
      return inner.data as T
    }
    return raw.data as T
  }
  return raw as T
}

/**
 * 从 API 响应中提取分页数据
 * @param payload - API 响应数据
 * @returns 分页数据或 null
 */
export function extractPageData<T>(payload: unknown): API.PageResponse<T> | null {
  const pageData = extractApiData<API.PageResponse<T>>(payload)
  if (pageData?.records) {
    return pageData
  }
  return null
}

// ============ 申请审核状态常量 ============

/** 申请审核状态枚举（申请层面的多阶段审核） */
export const APPLICATION_AUDIT_STATUS = {
  PENDING: 0,             // 待审核
  TUTOR_APPROVED: 1,      // 导师通过
  DEPARTMENT_APPROVED: 2, // 院系通过
  REJECTED: 3             // 未通过
} as const

/** 申请审核状态映射 (状态值 => 标签) */
export const APPLICATION_AUDIT_STATUS_LABELS: Record<number, string> = {
  [APPLICATION_AUDIT_STATUS.PENDING]: '待审核',
  [APPLICATION_AUDIT_STATUS.TUTOR_APPROVED]: '导师通过',
  [APPLICATION_AUDIT_STATUS.DEPARTMENT_APPROVED]: '院系通过',
  [APPLICATION_AUDIT_STATUS.REJECTED]: '未通过'
}

/** 申请审核状态 Tag 类型映射 */
export const APPLICATION_AUDIT_STATUS_TYPES: Record<number, 'warning' | 'success' | 'danger' | 'info' | 'primary'> = {
  [APPLICATION_AUDIT_STATUS.PENDING]: 'warning',
  [APPLICATION_AUDIT_STATUS.TUTOR_APPROVED]: 'success',
  [APPLICATION_AUDIT_STATUS.DEPARTMENT_APPROVED]: 'success',
  [APPLICATION_AUDIT_STATUS.REJECTED]: 'danger'
}
