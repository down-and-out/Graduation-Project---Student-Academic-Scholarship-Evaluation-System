import request from '@/utils/request'

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

function buildParamsSerializer() {
  return {
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
}

export function getEvaluationPage(
  params: EvaluationPageParams
): Promise<API.Response<API.PageResponse<EvaluationBatch>>> {
  return request({
    url: '/evaluation-batch/page',
    method: 'get',
    params,
    paramsSerializer: buildParamsSerializer()
  })
}

export function getEvaluationDetail(id: number): Promise<API.Response<EvaluationBatch>> {
  return request({
    url: `/evaluation-batch/${id}`,
    method: 'get'
  })
}

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
