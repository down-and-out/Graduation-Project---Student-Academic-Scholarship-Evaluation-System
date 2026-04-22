import request from '@/utils/request'

export interface ApplicationAchievementItem {
  id?: number
  achievementType: number
  achievementId: number
  title?: string
  subtitle?: string
  authors?: string
  score?: number
  scoreComment?: string
}

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

export interface ApplicationDetail extends Application {
  achievements?: ApplicationAchievementItem[]
}

export interface ApplicationPageParams extends API.PageParams {
  batchId?: number
  studentId?: number
  status?: number
}

export interface SubmitApplicationAchievement {
  achievementType: number
  achievementId: number
}

export interface SubmitApplicationData {
  batchId: number
  selfEvaluation: string
  remark?: string
  achievements: SubmitApplicationAchievement[]
}

export interface ReviewApplicationParams {
  opinion: string
  passed: boolean
}

export function getApplicationPage(
  params: ApplicationPageParams
): Promise<API.Response<API.PageResponse<Application>>> {
  return request({
    url: '/application/page',
    method: 'get',
    params
  })
}

export function getApplicationById(id: number): Promise<API.Response<ApplicationDetail>> {
  return request({
    url: `/application/${id}`,
    method: 'get'
  })
}

export function getAvailableApplicationAchievements(): Promise<API.Response<ApplicationAchievementItem[]>> {
  return request({
    url: '/application/available-achievements',
    method: 'get'
  })
}

export function submitApplication(data: SubmitApplicationData): Promise<API.Response<null>> {
  return request({
    url: '/application/submit',
    method: 'post',
    data
  })
}

export function reviewApplication(
  id: number,
  params: ReviewApplicationParams
): Promise<API.Response<null>> {
  return request({
    url: `/application/review/${id}`,
    method: 'put',
    data: params
  })
}

export default {
  getApplicationPage,
  getApplicationById,
  getAvailableApplicationAchievements,
  submitApplication,
  reviewApplication
}
