/**
 * 评定结果相关 API 接口
 */
import request from '@/utils/request'

/**
 * 评定结果
 */
export interface EvaluationResult {
  id?: number
  batchId: number
  batchName?: string
  studentId: number
  studentName?: string
  studentNo?: string
  department?: string
  major?: string
  departmentRank?: number
  majorRank?: number
  awardLevel?: number
  totalScore: number
  scholarshipAmount?: number
  awardAmount?: number
  rank?: number
  resultStatus?: number
  status?: number
  publicStartTime?: string
  publicEndTime?: string
  grantTime?: string
  publishDate?: string
  publicityDate?: string
  createTime?: string
}

/**
 * 分页查询评定结果参数
 */
export interface ResultPageParams extends API.PageParams {
  batchId?: number
  studentId?: number
  status?: number
  keyword?: string
}

/**
 * 分页查询评定结果
 */
export function getResultPage(
  params: ResultPageParams
): Promise<API.Response<API.PageResponse<EvaluationResult>>> {
  return request({
    url: '/evaluation-result/page',
    method: 'get',
    params
  })
}

/**
 * 获取结果详情
 */
export function getResultDetail(id: number): Promise<API.Response<EvaluationResult>> {
  return request({
    url: `/evaluation-result/${id}`,
    method: 'get'
  })
}

/**
 * 获取我的评定结果
 */
export function getMyResult(batchId?: number): Promise<API.Response<EvaluationResult>> {
  return request({
    url: '/evaluation-result/my-result',
    method: 'get',
    params: { batchId }
  })
}

/**
 * 导出结果
 */
export async function exportResult(batchId?: number): Promise<Blob> {
  const response = await request({
    url: '/evaluation-result/export',
    method: 'get',
    params: { batchId },
    responseType: 'blob'
  })
  return response.data as Blob
}

export default {
  getResultPage,
  getResultDetail,
  getMyResult,
  exportResult
}
