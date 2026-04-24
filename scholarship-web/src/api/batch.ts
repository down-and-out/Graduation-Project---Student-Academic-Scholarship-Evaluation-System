import {
  type EvaluationBatch,
  type EvaluationPageParams,
  createEvaluation,
  deleteEvaluation,
  getAvailableBatches,
  getEvaluationDetail,
  getEvaluationPage,
  startEvaluationApplication
} from '@/api/evaluation'
import request from '@/utils/request'

export interface BatchImportResult {
  successCount: number
  failCount: number
  successNames: string[]
  failures: Array<{
    studentNo: string
    name: string
    reason: string
  }>
}

export interface BatchPageParams extends EvaluationPageParams {
  keyword?: string
}

export function importStudents(file: File): Promise<API.Response<BatchImportResult>> {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/batch/import/students',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadStudentTemplate(): Promise<void> {
  return request({
    url: '/batch/import/students/template',
    method: 'get',
    responseType: 'blob'
  })
}

export { type EvaluationBatch }

export function getBatchPage(params: BatchPageParams): Promise<API.Response<API.PageResponse<EvaluationBatch>>> {
  return getEvaluationPage(params)
}

export function getBatchDetail(id: number): Promise<API.Response<EvaluationBatch>> {
  return getEvaluationDetail(id)
}

export function addBatch(data: Omit<EvaluationBatch, 'id'>): Promise<API.Response<null>> {
  return createEvaluation({
    name: data.name ?? '',
    academicYear: data.academicYear ?? '',
    semester: data.semester ?? 1,
    startDate: data.startDate,
    endDate: data.endDate,
    status: data.status,
    remark: data.remark,
    awardConfigs: data.awardConfigs ?? [],
    selectedRuleIds: data.selectedRuleIds ?? []
  })
}

export function updateBatch(data: Partial<EvaluationBatch> & { id: number }): Promise<API.Response<null>> {
  return request({
    url: '/evaluation-batch',
    method: 'put',
    data
  })
}

export function deleteBatch(id: number): Promise<API.Response<null>> {
  return deleteEvaluation(id)
}

export function startBatch(id: number): Promise<API.Response<null>> {
  return startEvaluationApplication(id)
}

export { getAvailableBatches }

export default {
  importStudents,
  downloadStudentTemplate,
  getBatchPage,
  getBatchDetail,
  addBatch,
  updateBatch,
  deleteBatch,
  startBatch,
  getAvailableBatches
}
