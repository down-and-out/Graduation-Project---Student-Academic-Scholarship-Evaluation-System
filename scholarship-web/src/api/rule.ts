/**
 * 评分规则相关 API 接口
 */
import request from '@/utils/request'

/**
 * 评分规则
 */
export interface ScoreRule {
  id?: number
  categoryId: number
  ruleName: string
  ruleType: number
  description?: string
  score: number
  minScore?: number
  maxScore?: number
  required?: boolean
  proofRequired?: boolean
  status?: number
  createTime?: string
  updateTime?: string
}

export interface RuleCategory {
  id: number
  categoryCode: string
  categoryName: string
  description?: string
  sortOrder?: number
  status?: number
}

/**
 * 分页查询评分规则参数
 */
export interface RulePageParams extends API.PageParams {
  ruleType?: number[] | number
  status?: number
}

/**
 * 分页查询评分规则
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getRulePage(
  params: RulePageParams
): Promise<API.Response<API.PageResponse<ScoreRule>>> {
  return request({
    url: '/score-rule/page',
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

/**
 * 获取规则详情
 * @param id - 规则 ID
 * @returns Promise
 */
export function getRuleById(id: number): Promise<API.Response<ScoreRule>> {
  return request({
    url: `/score-rule/${id}`,
    method: 'get'
  })
}

/**
 * 新增规则
 * @param data - 规则信息
 * @returns Promise
 */
export function addRule(data: Omit<ScoreRule, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/score-rule',
    method: 'post',
    data
  })
}

/**
 * 更新规则
 * @param data - 规则信息（必须包含 id）
 * @returns Promise
 */
export function updateRule(data: Partial<ScoreRule> & { id: number }): Promise<API.Response<null>> {
  return request({
    url: '/score-rule',
    method: 'put',
    data
  })
}

/**
 * 删除规则
 * @param id - 规则 ID
 * @returns Promise
 */
export function deleteRule(id: number): Promise<API.Response<null>> {
  return request({
    url: `/score-rule/${id}`,
    method: 'delete'
  })
}

export function getRuleCategoryList(): Promise<API.Response<RuleCategory[]>> {
  return request({
    url: '/rule-category/tree',
    method: 'get'
  })
}

export default {
  getRulePage,
  getRuleById,
  addRule,
  updateRule,
  deleteRule,
  getRuleCategoryList
}
