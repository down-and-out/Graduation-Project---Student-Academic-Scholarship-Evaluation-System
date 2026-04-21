/**
 * 系统设置相关 API 接口
 */
import request from '@/utils/request'

// ========================================
// 类型定义
// ========================================

/**
 * 基本设置
 */
export interface BasicSetting {
  /** 系统名称 */
  systemName: string
  /** 系统简称 */
  systemShortName: string
  /** 当前学期 */
  currentSemester: string
  /** 管理员邮箱 */
  adminEmail: string
  /** 联系电话 */
  adminPhone: string
  /** 系统公告 */
  announcement: string
}

/**
 * 权重设置
 */
export interface WeightSetting {
  /** 课程成绩权重 */
  courseWeight: number
  /** 科研成果权重 */
  researchWeight: number
  /** 综合素质权重 */
  comprehensiveWeight: number
}

/**
 * 分数范围
 */
export interface ScoreRange {
  /** 最低分数 */
  min: number
  /** 最高分数 */
  max: number
}

/**
 * 条件规则
 */
export interface Condition {
  /** 条件类型 */
  type: string
  /** 字段名 */
  field: string
  /** 操作符 */
  operator: string
  /** 比较值 */
  value: any
}

/**
 * 奖项规则
 */
export interface AwardRule {
  /** 规则ID */
  id: string
  /** 奖项名称 */
  name: string
  /** 名额比例 */
  ratio: number
  /** 奖励金额 */
  amount: number
  /** 分数范围 */
  scoreRange: ScoreRange
  /** 附加条件 */
  conditions: Condition[]
  /** 优先级 */
  priority: number
}

/**
 * 奖项配置
 */
export interface AwardConfig {
  /** 版本号 */
  /** 配置名称 */
  name: string
  /** 奖项规则列表 */
  rules: AwardRule[]
  /** 分配策略 */
  allocationStrategy: string
}

/**
 * 操作日志
 */
export interface OperationLog {
  id: number
  userId?: number
  username: string
  operationType: string
  operationDesc: string
  requestUri?: string
  requestMethod?: string
  ipAddress: string
  createTime: string
}

/**
 * 日志查询参数
 */
export interface LogQueryParams {
  current: number
  size: number
  operationType?: string[] | string
  username?: string
}

// ========================================
// API 函数
// ========================================

/**
 * 获取单个设置
 * @param key - 设置键（basic/weight/awards）
 * @returns Promise
 */
export function getSetting<T>(key: string): Promise<API.Response<T>> {
  return request({
    url: `/system/setting/${key}`,
    method: 'get'
  })
}

/**
 * 获取当前生效的设置
 * @param key - 设置键
 * @returns Promise
 */
export function getActiveSetting<T>(key: string): Promise<API.Response<T>> {
  return request({
    url: `/system/setting/${key}`,
    method: 'get',
    params: { active: true }
  })
}

/**
 * 更新设置
 * @param key - 设置键
 * @param data - 设置数据对象
 * @returns Promise
 */
export function updateSetting<T>(
  key: string,
  data: T
): Promise<API.Response<void>> {
  return request({
    url: `/system/setting/${key}`,
    method: 'put',
    data
  })
}

/**
 * 获取所有设置
 * @returns Promise
 */
export function getAllSettings(): Promise<API.Response<Record<string, string>>> {
  return request({
    url: '/system/settings',
    method: 'get'
  })
}

/**
 * 分页查询操作日志
 * @param params - 查询参数
 * @returns Promise
 */
export function getOperationLogPage(
  params: LogQueryParams
): Promise<API.Response<API.PageResponse<OperationLog>>> {
  return request({
    url: '/operation-log/page',
    method: 'get',
    params,
    paramsSerializer: {
      serialize: (rawParams: Record<string, unknown>) => {
        const searchParams = new URLSearchParams()

        Object.entries(rawParams).forEach(([key, value]) => {
          if (value === undefined || value === null || value === '') {
            return
          }

          if (Array.isArray(value)) {
            const normalized = value
              .filter(item => item !== undefined && item !== null && item !== '')
              .map(item => String(item))

            if (normalized.length > 0) {
              searchParams.append(key, normalized.join(','))
            }
            return
          }

          searchParams.append(key, String(value))
        })

        return searchParams.toString()
      }
    }
  })
}

// ========================================
// 默认导出
// ========================================

export default {
  getSetting,
  getActiveSetting,
  updateSetting,
  getAllSettings,
  getOperationLogPage
}
