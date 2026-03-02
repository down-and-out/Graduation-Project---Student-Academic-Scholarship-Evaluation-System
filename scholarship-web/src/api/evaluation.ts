/**
 * 评定管理相关 API 接口
 */
import request from '@/utils/request'

/**
 * 评定批次
 */
export interface EvaluationBatch {
  id?: number
  name: string
  semester: string
  startDate: string
  endDate: string
  applicantCount?: number
  status?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 分页查询评定参数
 */
export interface EvaluationPageParams extends API.PageParams {
  semester?: string
  status?: number
}

/**
 * 创建评定参数
 */
export interface CreateEvaluationData {
  name: string
  semester: string
  startDate: string
  endDate: string
  remark?: string
}

/**
 * 分页查询评定
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getEvaluationPage(
  params: EvaluationPageParams
): Promise<API.Response<API.PageResponse<EvaluationBatch>>> {
  return request({
    url: '/evaluation-batch/page',
    method: 'get',
    params
  })
}

/**
 * 获取评定详情
 * @param id - 评定 ID
 * @returns Promise
 */
export function getEvaluationDetail(id: number): Promise<API.Response<EvaluationBatch>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'get'
  })
}

/**
 * 创建评定
 * @param data - 评定信息
 * @returns Promise
 */
export function createEvaluation(data: CreateEvaluationData): Promise<API.Response<null>> {
  return request({
    url: '/evaluation-batch',
    method: 'post',
    data
  })
}

/**
 * 发布评定
 * @param id - 评定 ID
 * @returns Promise
 */
export function publishEvaluation(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/publish/${id}`,
    method: 'put'
  })
}

/**
 * 结束评定
 * @param id - 评定 ID
 * @returns Promise
 */
export function closeEvaluation(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/close/${id}`,
    method: 'put'
  })
}

/**
 * 删除评定
 * @param id - 评定 ID
 * @returns Promise
 */
export function deleteEvaluation(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'delete'
  })
}

export default {
  getEvaluationPage,
  getEvaluationDetail,
  createEvaluation,
  publishEvaluation,
  closeEvaluation,
  deleteEvaluation
}
