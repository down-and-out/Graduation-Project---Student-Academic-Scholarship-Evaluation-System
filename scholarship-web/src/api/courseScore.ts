import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

export interface CourseScoreImportResult {
  imported: number
  updated: number
  skipped: number
  message: string
}

export interface CourseScore {
  id?: number
  studentId?: number
  studentNo?: string
  studentName?: string
  courseId?: number
  courseName: string
  courseCode?: string
  courseType?: number
  credit?: number
  score?: number
  gpa?: number
  academicYear?: string
  semester?: number
  examDate?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface CourseScorePageParams extends API.PageParams {
  academicYear?: string
  semester?: number
  courseName?: string
}

export function getMyCourseScorePage(
  params: CourseScorePageParams
): Promise<AxiosResponse<API.Response<API.PageResponse<CourseScore>>>> {
  return request({
    url: '/course-score/my/page',
    method: 'get',
    params
  })
}

export function importMyCourseScores(file: File): Promise<AxiosResponse<API.Response<CourseScoreImportResult>>> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/course-score/my/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default {
  getMyCourseScorePage,
  importMyCourseScores
}
