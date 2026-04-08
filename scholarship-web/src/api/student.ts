/**
 * 学生信息相关 API 接口
 */
import request from '@/utils/request'

/**
 * 学生信息
 */
export interface Student {
  id?: number
  studentNo: string
  name: string
  gender: number
  idCard?: string
  phone?: string
  email?: string
  department?: string
  major?: string
  grade?: string
  educationLevel: number
  enrollmentYear: number
  status?: number
  avatar?: string
  tutorId?: number
  tutorName?: string
  direction?: string
  politicalStatus?: string
  nation?: string
  nativePlace?: string
  address?: string
}

/**
 * 分页查询学生信息参数
 */
export interface StudentPageParams extends API.PageParams {
  keyword?: string
  department?: string
  status?: number
  grade?: string
}

/**
 * 分页查询学生信息
 * @param params - 查询参数
 * @returns 分页响应
 */
export function getStudentPage(
  params: StudentPageParams
): Promise<API.Response<API.PageResponse<Student>>> {
  return request({
    url: '/student-info/page',
    method: 'get',
    params
  })
}

/**
 * 获取当前登录学生的信息
 * @returns Promise
 */
export function getMyInfo(): Promise<API.Response<Student>> {
  return request({
    url: '/student-info/my',
    method: 'get'
  })
}

/**
 * 根据 ID 获取学生信息
 * @param id - 学生 ID
 * @returns Promise
 */
export function getStudentById(id: number): Promise<API.Response<Student>> {
  return request({
    url: `/student-info/${id}`,
    method: 'get'
  })
}

/**
 * 新增学生信息
 * @param data - 学生信息
 * @returns Promise
 */
export function addStudent(data: Omit<Student, 'id'>): Promise<API.Response<null>> {
  return request({
    url: '/student-info',
    method: 'post',
    data
  })
}

/**
 * 更新学生信息
 * @param data - 学生信息（必须包含 id）
 * @returns Promise
 */
export function updateStudent(data: Partial<Student> & { id: number }): Promise<API.Response<null>> {
  return request({
    url: '/student-info',
    method: 'put',
    data
  })
}

/**
 * 学生更新自己的信息
 * @param data - 部分学生信息（phone, email, direction）
 * @returns Promise
 */
export function updateMyInfo(data: Partial<Student>): Promise<API.Response<null>> {
  return request({
    url: '/student-info/my',
    method: 'put',
    data
  })
}

/**
 * 删除学生信息
 * @param id - 学生 ID
 * @returns Promise
 */
export function deleteStudent(id: number): Promise<API.Response<null>> {
  return request({
    url: `/student-info/${id}`,
    method: 'delete'
  })
}

export default {
  getStudentPage,
  getMyInfo,
  getStudentById,
  addStudent,
  updateStudent,
  updateMyInfo,
  deleteStudent
}
