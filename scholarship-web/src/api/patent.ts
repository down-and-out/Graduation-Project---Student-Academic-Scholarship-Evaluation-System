/**
 * 科研专利相关 API 接口
 */
import request from '@/utils/request'

/**
 * 科研专利
 */
export interface ResearchPatent {
  id?: number
  studentId: number
  studentName?: string
  patentName: string
  patentType: number
  patentNo?: string
  applicationDate?: string
  authorizationDate?: string
  inventors?: string
  status?: number
  auditStatus?: number
  auditComment?: string
  auditTime?: string
  score?: number
  createTime?: string
}

/**
 * 分页查询专利参数
 */
export interface PatentPageParams extends API.PageParams {
  studentId?: number
  patentType?: number
  status?: number
  auditStatus?: number
  keyword?: string
}

/**
 * 分页查询专利
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getPatentPage(
  params: PatentPageParams
): Promise<API.Response<API.PageResponse<ResearchPatent>>> {
  return request({
    url: '/research-patent/page',
    method: 'get',
    params
  })
}

/**
 * 获取专利详情
 * @param id - 专利 ID
 * @returns Promise
 */
export function getPatentDetail(id: number): Promise<API.Response<ResearchPatent>> {
  return request({
    url: `/research-patent/${id}`,
    method: 'get'
  })
}

/**
 * 新增专利
 * @param data - 专利信息
 * @returns Promise
 */
export function addPatent(data: Omit<ResearchPatent, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/research-patent',
    method: 'post',
    data
  })
}

/**
 * 更新专利
 * @param data - 专利信息（必须包含 id）
 * @returns Promise
 */
export function updatePatent(
  data: Partial<ResearchPatent> & { id: number }
): Promise<API.Response<null>> {
  return request({
    url: '/research-patent',
    method: 'put',
    data
  })
}

/**
 * 删除专利
 * @param id - 专利 ID
 * @returns Promise
 */
export function deletePatent(id: number): Promise<API.Response<null>> {
  return request({
    url: `/research-patent/${id}`,
    method: 'delete'
  })
}

/**
 * 审核专利参数
 */
export interface AuditPatentParams {
  auditStatus: number
  auditComment?: string
}

/**
 * 审核专利
 * @param id - 专利 ID
 * @param params - 审核参数
 * @returns Promise
 */
export function auditPatent(
  id: number,
  params: AuditPatentParams
): Promise<API.Response<null>> {
  return request({
    url: `/research-patent/audit/${id}`,
    method: 'put',
    data: params  // 使用 data 传递请求体
  })
}

export default {
  getPatentPage,
  getPatentDetail,
  addPatent,
  updatePatent,
  deletePatent,
  auditPatent
}
