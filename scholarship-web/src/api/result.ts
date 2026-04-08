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
  department?: string        // 院系
  major?: string             // 专业
  departmentRank?: number    // 院系排名
  majorRank?: number         // 专业排名
  awardLevel?: number        // 获奖等级: 1-特等, 2-一等, 3-二等, 4-三等, 5-未获奖
  totalScore: number
  resultStatus?: number      // 结果状态: 1-公示中, 2-已确定, 3-有异议
  status?: number            // 兼容字段
  publicStartTime?: string
  publicEndTime?: string
  grantTime?: string
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
 * @param params - 查询参数
 * @returns 分页响应
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
 * @param id - 结果 ID
 * @returns Promise
 */
export function getResultDetail(id: number): Promise<API.Response<EvaluationResult>> {
  return request({
    url: `/evaluation-result/${id}`,
    method: 'get'
  })
}

/**
 * 获取我的评定结果
 * @param batchId - 批次 ID
 * @returns Promise
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
 * @param batchId - 批次 ID
 * @returns Blob (Excel 文件)
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
