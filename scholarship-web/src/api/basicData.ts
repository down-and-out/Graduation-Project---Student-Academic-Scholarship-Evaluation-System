/**
 * 基础数据 API
 * 提供院系、专业等基础数据的查询
 */
import request from '@/utils/request'
import { cachedRequest } from '@/utils/apiCache'

/**
 * 获取院系列表（缓存 1 小时）
 */
const _getDepartments = (): Promise<API.Response<string[]>> => {
  return request({
    url: '/api/basic-data/departments',
    method: 'get'
  })
}
export const getDepartments = cachedRequest(_getDepartments, 'departments', 60 * 60 * 1000)

/**
 * 获取专业列表
 * @param department 院系（可选）
 */
export function getMajors(department?: string): Promise<API.Response<string[]>> {
  return request({
    url: '/api/basic-data/majors',
    method: 'get',
    params: { department }
  })
}

export default {
  getDepartments,
  getMajors
}
