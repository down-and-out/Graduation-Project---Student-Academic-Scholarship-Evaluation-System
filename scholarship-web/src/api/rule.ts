/**
 * 评分规则相关 API 接口
 */
import request from '@/utils/request'
import { cachedRequest } from '@/utils/apiCache'
import { serializeQueryParams } from '@/utils/helpers'

/**
 * 评分规则
 */
export interface ScoreRule {
  id?: number
  categoryId: number
  ruleCode?: string
  ruleName: string
  ruleType: number
  level?: string
  condition?: string
  score: number
  maxScore?: number
  isAvailable?: number
  sortOrder?: number
  version?: number
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
    paramsSerializer: { serialize: serializeQueryParams }
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

export function getAvailableRulesByType(ruleType: number): Promise<API.Response<ScoreRule[]>> {
  return request({
    url: '/score-rule/available',
    method: 'get',
    params: { ruleType }
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

const _getRuleCategoryList = (): Promise<API.Response<RuleCategory[]>> => {
  return request({
    url: '/rule-category/tree',
    method: 'get'
  })
}
export const getRuleCategoryList = cachedRequest(_getRuleCategoryList, 'rule_categories', 30 * 60 * 1000)

export default {
  getRulePage,
  getRuleById,
  getAvailableRulesByType,
  addRule,
  updateRule,
  deleteRule,
  getRuleCategoryList
}
