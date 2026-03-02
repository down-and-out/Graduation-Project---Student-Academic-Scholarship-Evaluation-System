/**
 * 奖学金申请相关 API 接口
 */
import request from '@/utils/request'

/**
 * 申请记录
 */
export interface Application {
  id?: number
  applicationNo: string
  batchId: number
  batchName?: string
  studentId: number
  studentName?: string
  studentNo?: string
  selfEvaluation?: string
  remark?: string
  status: number
  totalScore?: number
  tutorOpinion?: string
  submitTime?: string
  reviewTime?: string
  completedTime?: string
}

/**
 * 分页查询申请记录参数
 */
export interface ApplicationPageParams extends API.PageParams {
  batchId?: number
  studentId?: number
  status?: number
}

/**
 * 分页查询申请记录
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getApplicationPage(
  params: ApplicationPageParams
): Promise<API.Response<API.PageResponse<Application>>> {
  return request({
    url: '/application/page',
    method: 'get',
    params
  })
}

/**
 * 获取申请详情
 * @param id - 申请 ID
 * @returns Promise
 */
export function getApplicationById(id: number): Promise<API.Response<Application>> {
  return request({
    url: `/application/${id}`,
    method: 'get'
  })
}

/**
 * 提交申请参数
 */
export interface SubmitApplicationData {
  batchId: number
  selfEvaluation: string
  remark?: string
}

/**
 * 提交申请
 * @param data - 申请信息
 * @returns Promise
 */
export function submitApplication(data: SubmitApplicationData): Promise<API.Response<null>> {
  return request({
    url: '/application/submit',
    method: 'post',
    data
  })
}

/**
 * 导师审核申请参数
 */
export interface ReviewApplicationParams {
  opinion: string
  passed: boolean
}

/**
 * 导师审核申请
 * @param id - 申请 ID
 * @param params - 审核参数
 * @returns Promise
 */
export function reviewApplication(
  id: number,
  params: ReviewApplicationParams
): Promise<API.Response<null>> {
  return request({
    url: `/application/review/${id}`,
    method: 'put',
    data: params  // 使用 data 传递请求体
  })
}

export default {
  getApplicationPage,
  getApplicationById,
  submitApplication,
  reviewApplication
}
