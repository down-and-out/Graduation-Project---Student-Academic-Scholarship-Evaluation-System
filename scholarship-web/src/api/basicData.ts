/**
 * 基础数据 API
 * 提供院系、专业等基础数据的查询
 */
import request from '@/utils/request'

/**
 * 获取院系列表
 */
export function getDepartments(): Promise<API.Response<string[]>> {
  return request({
    url: '/api/basic-data/departments',
    method: 'get'
  })
}

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
