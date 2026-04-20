import request from '@/utils/request'

export interface User {
  id?: number
  username: string
  password?: string
  realName?: string
  name?: string
  userType?: number
  role?: string
  department?: string
  major?: string
  phone?: string
  email?: string
  avatar?: string
  status?: number
  createTime?: string
  updateTime?: string
  remark?: string
}

export interface UserCreateRequest {
  user: Partial<User>
  major?: string
  studentNo?: string
  gender?: number
  idCard?: string
  enrollmentYear?: number
  educationLevel?: number
  trainingMode?: number
  nativePlace?: string
  address?: string
  status?: number
}

export interface UserPageParams extends API.PageParams {
  keyword?: string
  userType?: number[] | number
  status?: number[] | number
}

export function getUserPage(params: UserPageParams): Promise<API.Response<API.PageResponse<User>>> {
  return request({
    url: '/sys/user/page',
    method: 'get',
    params,
    paramsSerializer: {
      serialize: (rawParams: Record<string, unknown>) => {
        const searchParams = new URLSearchParams()

        Object.entries(rawParams).forEach(([key, value]) => {
          if (value === undefined || value === null || value === '') {
            return
          }

          if (Array.isArray(value)) {
            const normalized = value
              .filter(item => item !== undefined && item !== null && item !== '')
              .map(item => String(item))

            if (normalized.length > 0) {
              searchParams.append(key, normalized.join(','))
            }
            return
          }

          searchParams.append(key, String(value))
        })

        return searchParams.toString()
      }
    }
  })
}

export function getUserById(id: number): Promise<API.Response<User>> {
  return request({
    url: `/sys/user/${id}`,
    method: 'get'
  })
}

export function addUser(data: UserCreateRequest): Promise<API.Response<null>> {
  return request({
    url: '/sys/user',
    method: 'post',
    data
  })
}

export function updateUser(data: Partial<User>): Promise<API.Response<null>> {
  return request({
    url: '/sys/user',
    method: 'put',
    data
  })
}

export function deleteUser(id: number): Promise<API.Response<null>> {
  return request({
    url: `/sys/user/${id}`,
    method: 'delete'
  })
}

export function batchDeleteUsers(ids: number[]): Promise<API.Response<null>> {
  return request({
    url: '/sys/user/batch',
    method: 'delete',
    data: ids
  })
}

export function resetPassword(id: number, password: string): Promise<API.Response<null>> {
  return request({
    url: `/sys/user/reset-password/${id}`,
    method: 'put',
    params: { password }
  })
}

export default {
  getUserPage,
  getUserById,
  addUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  resetPassword
}
