/**
 * 结果异议相关 API 接口
 */
import request from '@/utils/request'

/**
 * 结果异议
 */
export interface ResultAppeal {
  id?: number
  resultId: number
  studentId: number
  studentName?: string
  reason: string
  evidence?: string
  status?: number
  handleResult?: number
  handleComment?: string
  handleTime?: string
  createTime?: string
}

/**
 * 分页查询异议参数
 */
export interface AppealPageParams extends API.PageParams {
  resultId?: number
  studentId?: number
  status?: number
}

/**
 * 分页查询异议
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getAppealPage(
  params: AppealPageParams
): Promise<API.Response<API.PageResponse<ResultAppeal>>> {
  return request({
    url: '/result-appeal/page',
    method: 'get',
    params
  })
}

/**
 * 提交异议
 * @param data - 异议信息
 * @returns Promise
 */
export function submitAppeal(data: Omit<ResultAppeal, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/result-appeal',
    method: 'post',
    data
  })
}

/**
 * 处理异议参数
 */
export interface HandleAppealParams {
  handleResult: number
  handleComment?: string
}

/**
 * 处理异议
 * @param id - 异议 ID
 * @param params - 处理参数
 * @returns Promise
 */
export function handleAppeal(
  id: number,
  params: HandleAppealParams
): Promise<API.Response<null>> {
  return request({
    url: `/result-appeal/handle/${id}`,
    method: 'put',
    data: params  // 使用 data 传递请求体
  })
}

export default {
  getAppealPage,
  submitAppeal,
  handleAppeal
}
