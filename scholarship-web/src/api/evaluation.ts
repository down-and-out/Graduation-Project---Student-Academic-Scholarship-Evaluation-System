import request from '@/utils/request'
import { cachedRequest } from '@/utils/apiCache'
import { serializeQueryParams } from '@/utils/helpers'

export interface EvaluationBatch {
  id?: number
  name: string
  batchCode?: string
  academicYear?: string
  semester: number | null
  startDate: string
  endDate: string
  reviewStartDate?: string
  reviewEndDate?: string
  publicityStartDate?: string
  publicityEndDate?: string
  winnerCount?: number
  totalAmount?: number
  status?: number
  description?: string
  remark?: string
  awardConfigs?: BatchAwardConfig[]
  selectedRuleIds?: number[]
  createTime?: string
  updateTime?: string
}

export interface BatchAwardConfig {
  awardLevel: number
  ratio: number
  amount: number
}

export interface EvaluationPageParams extends API.PageParams {
  academicYears?: string[] | string
  semesters?: number[] | number
  statuses?: number[] | number
  semester?: string[] | string
  status?: number[] | number
}

export interface CreateEvaluationData {
  name: string
  academicYear: string
  semester: number
  startDate: string
  endDate: string
  status?: number
  remark?: string
  awardConfigs: BatchAwardConfig[]
  selectedRuleIds: number[]
}

export function getEvaluationPage(
  params: EvaluationPageParams
): Promise<API.Response<API.PageResponse<EvaluationBatch>>> {
  return request({
    url: '/evaluation-batch/page',
    method: 'get',
    params,
    paramsSerializer: { serialize: serializeQueryParams }
  })
}

export function getEvaluationDetail(id: number): Promise<API.Response<EvaluationBatch>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'get'
  })
}

/**
 * 获取评定学年列表（缓存 1 小时）
 */
const _getEvaluationAcademicYears = (): Promise<API.Response<string[]>> => {
  return request({
    url: '/evaluation-batch/meta/years',
    method: 'get'
  })
}
export const getEvaluationAcademicYears = cachedRequest(
  _getEvaluationAcademicYears,
  'evaluation_academic_years',
  60 * 60 * 1000
)

export function createEvaluation(data: CreateEvaluationData): Promise<API.Response<null>> {
  return request({
    url: '/evaluation-batch',
    method: 'post',
    data
  })
}

export function startEvaluationApplication(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/start/${id}`,
    method: 'put'
  })
}

export function startEvaluationReview(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/start-review/${id}`,
    method: 'put'
  })
}

export function startEvaluationPublicity(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/start-publicity/${id}`,
    method: 'put'
  })
}

export function completeEvaluation(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/close/${id}`,
    method: 'put'
  })
}

export function publishEvaluation(id: number): Promise<API.Response<null>> {
  return startEvaluationApplication(id)
}

export function closeEvaluation(id: number): Promise<API.Response<null>> {
  return completeEvaluation(id)
}

export function deleteEvaluation(id: number): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'delete'
  })
}

export function getAvailableBatches(): Promise<API.Response<EvaluationBatch[]>> {
  return request({
    url: '/evaluation-batch/available',
    method: 'get'
  })
}

export default {
  getEvaluationPage,
  getEvaluationDetail,
  getEvaluationAcademicYears,
  createEvaluation,
  startEvaluationApplication,
  startEvaluationReview,
  startEvaluationPublicity,
  completeEvaluation,
  publishEvaluation,
  closeEvaluation,
  deleteEvaluation,
  getAvailableBatches
}
