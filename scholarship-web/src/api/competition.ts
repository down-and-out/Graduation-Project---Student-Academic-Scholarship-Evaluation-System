import request from '@/utils/request'

export interface CompetitionAward {
  id?: number
  studentId: number
  studentName?: string
  competitionName: string
  competitionLevel?: number
  awardLevel: number
  awardRank?: number
  awardType?: number
  memberRank?: number
  instructor?: string
  issuingUnit?: string
  organizer?: string
  teamMembers?: string
  awardDate?: string
  attachmentUrl?: string
  score?: number
  auditStatus?: number
  auditComment?: string
  auditTime?: string
  createTime?: string
  remark?: string
}

export interface CompetitionPageParams extends API.PageParams {
  studentId?: number
  auditStatus?: number
  keyword?: string
}

export function getCompetitionPage(
  params: CompetitionPageParams
): Promise<API.Response<API.PageResponse<CompetitionAward>>> {
  return request({
    url: '/competition-award/page',
    method: 'get',
    params
  })
}

export function getCompetitionDetail(id: number): Promise<API.Response<CompetitionAward>> {
  return request({
    url: `/competition-award/${id}`,
    method: 'get'
  })
}

export function addCompetition(data: Omit<CompetitionAward, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/competition-award',
    method: 'post',
    data
  })
}

export function updateCompetition(
  data: Partial<CompetitionAward> & { id: number }
): Promise<API.Response<null>> {
  return request({
    url: '/competition-award',
    method: 'put',
    data
  })
}

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
