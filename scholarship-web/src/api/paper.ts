/**
 * 科研论文相关 API 接口
 */
import request from '@/utils/request'
import type { ReviewDisplayStatus } from '@/constants/review'

/**
 * 科研论文
 */
export interface Paper {
  id?: number
  studentId?: number
  studentName?: string
  studentNo?: string
  title: string
  paperTitle?: string
  authors: string
  journal?: string
  journalName?: string
  conference?: string
  date?: string
  publishDate?: string
  publicationDate?: string
  doi?: string
  authorRank?: number
  journalLevel?: number
  impactFactor?: number
  level?: string
  score?: number
  status?: number
  reviewComment?: string
  reviewTime?: string
  createTime?: string
}

/**
 * 分页查询论文参数
 */
export interface PaperPageParams extends API.PageParams {
  studentId?: number
  status?: number
  keyword?: string
}

/**
 * 分页查询论文
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getPaperPage(
  params: PaperPageParams
): Promise<API.Response<API.PageResponse<Paper>>> {
  return request({
    url: '/paper/page',
    method: 'get',
    params
  })
}

/**
 * 获取论文详情
 * @param id - 论文 ID
 * @returns Promise
 */
export function getPaperById(id: number): Promise<API.Response<Paper>> {
  return request({
    url: `/paper/${id}`,
    method: 'get'
  })
}

/**
 * 提交论文
 * @param data - 论文信息
 * @returns Promise
 */
export function submitPaper(data: Omit<Paper, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/paper/submit',
    method: 'post',
    data
  })
}

/**
 * 编辑论文
 * @param id - 论文 ID
 * @param data - 论文信息
 * @returns Promise
 */
export function updatePaper(id: number, data: Omit<Paper, 'id'>): Promise<API.Response<null>> {
  return request({
    url: `/paper/${id}`,
    method: 'put',
    data
  })
}

/**
 * 审核论文参数
 */
export interface ReviewPaperParams {
  status: ReviewDisplayStatus
  reviewComment?: string
}

/**
 * 审核论文
 * @param id - 论文 ID
 * @param params - 审核参数
 * @returns Promise
 */
export function reviewPaper(
  id: number,
  params: ReviewPaperParams
): Promise<API.Response<null>> {
  return request({
    url: `/paper/review/${id}`,
    method: 'put',
    data: params  // 使用 data 传递请求体
  })
}

/**
 * 删除论文
 * @param id - 论文 ID
 * @returns Promise
 */
export function deletePaper(id: number): Promise<API.Response<null>> {
  return request({
    url: `/paper/${id}`,
    method: 'delete'
  })
}

/**
 * 统计论文总数
 * @returns 总数
 */
export function countPaper(): Promise<API.Response<number>> {
  return request({
    url: '/paper/count',
    method: 'get'
  })
}

export default {
  getPaperPage,
  getPaperById,
  submitPaper,
  updatePaper,
  reviewPaper,
  deletePaper,
  countPaper
}
