import request from '@/utils/request'

export interface EvaluationResult {
  id?: number
  batchId: number
  batchName?: string
  studentId: number
  studentName?: string
  studentNo?: string
  department?: string
  major?: string
  courseScore?: number
  researchScore?: number
  competitionScore?: number
  qualityScore?: number
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
  confirmDate?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ResultPageParams extends API.PageParams {
  batchId?: number
  academicYear?: string
  semester?: number
  studentId?: number
  status?: number
  keyword?: string
}

export interface AdjustResultPayload {
  awardLevel: number
  reason: string
}

export function getResultPage(
  params: ResultPageParams
): Promise<API.Response<API.PageResponse<EvaluationResult>>> {
  return request({
    url: '/evaluation-result/page',
    method: 'get',
    params
  })
}

export function getResultDetail(id: number): Promise<API.Response<EvaluationResult>> {
  return request({
    url: `/evaluation-result/${id}`,
    method: 'get'
  })
}

export function adjustResult(id: number, data: AdjustResultPayload): Promise<API.Response<null>> {
  return request({
    url: `/evaluation-result/adjust/${id}`,
    method: 'put',
    data
  })
}

export function getMyResult(batchId?: number): Promise<API.Response<EvaluationResult>> {
  return request({
    url: '/evaluation-result/my-result',
    method: 'get',
    params: { batchId }
  })
}

export async function exportResult(params?: {
  batchId?: number
  academicYear?: string
  semester?: number
}): Promise<Blob> {
  const response = await request({
    url: '/evaluation-result/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
  return response.data as Blob
}

export default {
  getResultPage,
  getResultDetail,
  adjustResult,
  getMyResult,
  exportResult
}
