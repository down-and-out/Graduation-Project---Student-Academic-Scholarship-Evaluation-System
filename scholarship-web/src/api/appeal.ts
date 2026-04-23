import request from '@/utils/request'

export interface ResultAppeal {
  id?: number
  resultId: number
  batchId?: number
  studentId?: number
  studentName?: string
  appealType?: number
  appealTitle?: string
  appealReason: string
  appealContent: string
  attachmentPath?: string
  appealStatus?: number
  handleOpinion?: string
  handlerId?: number
  handlerName?: string
  handleTime?: string
  createTime?: string
}

export interface AppealPageParams extends API.PageParams {
  resultId?: number
  studentId?: number
  appealStatus?: number
}

export function getAppealPage(
  params: AppealPageParams
): Promise<API.Response<API.PageResponse<ResultAppeal>>> {
  return request({
    url: '/result-appeal/page',
    method: 'get',
    params
  })
}

export interface SubmitAppealPayload {
  resultId: number
  appealType?: number
  appealTitle?: string
  appealReason: string
  appealContent: string
  attachmentPath?: string
}

export function submitAppeal(data: SubmitAppealPayload): Promise<API.Response<null>> {
  return request({
    url: '/result-appeal',
    method: 'post',
    data
  })
}

export interface HandleAppealParams {
  handleOpinion: string
}

export function handleAppeal(
  id: number,
  params: HandleAppealParams
): Promise<API.Response<null>> {
  return request({
    url: `/result-appeal/handle/${id}`,
    method: 'put',
    data: params
  })
}

export default {
  getAppealPage,
  submitAppeal,
  handleAppeal
}
