/**
 * 评定批次相关 API 接口
 */
import request from '@/utils/request'

/**
 * 评定批次
 */
export interface EvaluationBatch {
  id?: number
  batchName: string
  batchNo?: string
  startTime: string
  endTime: string
  quota?: number
  amount?: number
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/**
 * 分页查询评定批次参数
 */
export interface BatchPageParams extends API.PageParams {
  keyword?: string
  status?: number
}

/**
 * 分页查询评定批次
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getBatchPage(
  params: BatchPageParams
): Promise<API.Response<API.PageResponse<EvaluationBatch>>> {
  return request({
    url: '/evaluation-batch/page',
    method: 'get',
    params
  })
}

/**
 * 获取批次详情
 * @param id - 批次 ID
 * @returns Promise
 */
export function getBatchDetail(id: number): Promise<API.Response<EvaluationBatch>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'get'
  })
}

/**
 * 新增评定批次
 * @param data - 批次信息
 * @returns Promise
 */
export function addBatch(data: Omit<EvaluationBatch, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/evaluation-batch',
    method: 'post',
    data
  })
}

/**
 * 更新评定批次
 * @param data - 批次信息（必须包含 id）
 * @returns Promise
 */
export function updateBatch(data: Partial<EvaluationBatch> & { id: number }): Promise<API.Response<null>> {
  return request({
    url: '/evaluation-batch',
    method: 'put',
    data
  })
}

/**
 * 删除评定批次
 * @param id - 批次 ID
 * @returns Promise
 */
export function deleteBatch(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'delete'
  })
}

/**
 * 开启批次
 * @param id - 批次 ID
 * @returns Promise
 */
export function startBatch(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/start/${id}`,
    method: 'put'
  })
}

/**
 * 获取当前可申请的批次
 * @returns Promise
 */
export function getAvailableBatches(): Promise<API.Response<EvaluationBatch[]>> {
  return request({
    url: '/evaluation-batch/available',
    method: 'get'
  })
}

export default {
  getBatchPage,
  getBatchDetail,
  addBatch,
  updateBatch,
  deleteBatch,
  startBatch,
  getAvailableBatches
}
