/**
 * 学科竞赛获奖相关 API 接口
 */
import request from '@/utils/request'

/**
 * 学科竞赛获奖
 */
export interface CompetitionAward {
  id?: number
  studentId: number
  studentName?: string
  competitionName: string
  awardLevel: string
  awardGrade: number
  organizer?: string
  awardDate?: string
  certificateNo?: string
  score?: number
  status?: number
  createTime?: string
}

/**
 * 分页查询竞赛获奖参数
 */
export interface CompetitionPageParams extends API.PageParams {
  studentId?: number
  status?: number
  keyword?: string
}

/**
 * 分页查询竞赛获奖
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getCompetitionPage(
  params: CompetitionPageParams
): Promise<API.Response<API.PageResponse<CompetitionAward>>> {
  return request({
    url: '/competition-award/page',
    method: 'get',
    params
  })
}

/**
 * 获取竞赛获奖详情
 * @param id - 获奖 ID
 * @returns Promise
 */
export function getCompetitionDetail(id: number): Promise<API.Response<CompetitionAward>> {
  return request({
    url: `/competition-award/${id}`,
    method: 'get'
  })
}

/**
 * 新增竞赛获奖
 * @param data - 获奖信息
 * @returns Promise
 */
export function addCompetition(data: Omit<CompetitionAward, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/competition-award',
    method: 'post',
    data
  })
}

/**
 * 更新竞赛获奖
 * @param data - 获奖信息（必须包含 id）
 * @returns Promise
 */
export function updateCompetition(
  data: Partial<CompetitionAward> & { id: number }
): Promise<API.Response<null>> {
  return request({
    url: '/competition-award',
    method: 'put',
    data
  })
}

/**
 * 删除竞赛获奖
 * @param id - 获奖 ID
 * @returns Promise
 */
export function deleteCompetition(id: number): Promise<API.Response<null>> {
  return request({
    url: `/competition-award/${id}`,
    method: 'delete'
  })
}

export default {
  getCompetitionPage,
  getCompetitionDetail,
  addCompetition,
  updateCompetition,
  deleteCompetition
}
