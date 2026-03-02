/**
 * 全局错误处理工具
 * 用于捕获和处理全局错误
 */

/**
 * 错误信息接口
 */
export interface ErrorInfo {
  error: Error | unknown
  instance: any
  info: string
}

/**
 * 错误上报函数（可扩展为发送到服务器）
 */
export function reportError(errorInfo: ErrorInfo): void {
  const { error, instance, info } = errorInfo

  // 错误信息
  const errorData = {
    message: error instanceof Error ? error.message : String(error),
    stack: error instanceof Error ? error.stack : undefined,
    component: instance?.$options?.name || 'Anonymous',
    info,
    url: window.location.href,
    userAgent: navigator.userAgent,
    timestamp: new Date().toISOString()
  }

  // 输出到控制台
  console.error('=== 全局错误捕获 ===')
  console.error('错误消息:', errorData.message)
  console.error('组件:', errorData.component)
  console.error('阶段:', errorData.info)
  console.error('时间:', errorData.timestamp)
  if (errorData.stack) {
    console.error('堆栈:', errorData.stack)
  }

  // TODO: 发送到错误收集服务器
  // fetch('/api/log-error', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(errorData)
  // })

  // 保存到 localStorage（用于调试）
  try {
    const errors = JSON.parse(localStorage.getItem('app_errors') || '[]')
    errors.unshift(errorData)
    // 只保留最近 50 条错误
    if (errors.length > 50) errors.length = 50
    localStorage.setItem('app_errors', JSON.stringify(errors))
  } catch (e) {
    console.error('保存错误日志失败:', e)
  }
}

/**
 * 清除存储的错误日志
 */
export function clearErrorLogs(): void {
  localStorage.removeItem('app_errors')
}

/**
 * 获取存储的错误日志
 */
export function getErrorLogs(): any[] {
  return JSON.parse(localStorage.getItem('app_errors') || '[]')
}

/**
 * 全局错误处理类
 */
export class ErrorHandler {
  private static instance: ErrorHandler
  private errors: ErrorInfo[] = []
  private maxErrors = 100

  private constructor() {}

  static getInstance(): ErrorHandler {
    if (!ErrorHandler.instance) {
      ErrorHandler.instance = new ErrorHandler()
    }
    return ErrorHandler.instance
  }

  /**
   * 捕获错误
   */
  capture(error: Error | unknown, instance?: any, info?: string): void {
    const errorInfo: ErrorInfo = {
      error,
      instance,
      info: info || 'Unknown'
    }

    this.errors.push(errorInfo)

    // 超出限制时移除最早的错误
    if (this.errors.length > this.maxErrors) {
      this.errors.shift()
    }

    // 上报错误
    reportError(errorInfo)
  }

  /**
   * 获取所有错误
   */
  getErrors(): ErrorInfo[] {
    return [...this.errors]
  }

  /**
   * 清除错误记录
   */
  clear(): void {
    this.errors = []
  }
}

export default {
  reportError,
  clearErrorLogs,
  getErrorLogs,
  ErrorHandler
}
