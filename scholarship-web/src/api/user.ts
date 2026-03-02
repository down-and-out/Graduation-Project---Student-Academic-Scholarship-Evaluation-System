/**
 * 系统用户管理 API
 */
import request from '@/utils/request'

/**
 * 用户信息
 */
export interface User {
  id?: number
  username: string
  password?: string
  realName?: string
  name?: string  // 用于兼容前端
  userType?: number
  role?: string  // 用于显示
  department?: string
  phone?: string
  email?: string
  avatar?: string
  status?: number
  createTime?: string
  updateTime?: string
  remark?: string
}

/**
 * 分页查询参数
 */
export interface UserPageParams extends API.PageParams {
  keyword?: string
  userType?: number
  status?: number
}

/**
 * 分页查询用户
 */
export function getUserPage(params: UserPageParams): Promise<API.Response<API.PageResponse<User>>> {
  return request({
    url: '/sys/user/page',
    method: 'get',
    params
  })
}

/**
 * 根据 ID 查询用户
 */
export function getUserById(id: number): Promise<API.Response<User>> {
  return request({
    url: `/sys/user/${id}`,
    method: 'get'
  })
}

/**
 * 新增用户
 */
export function addUser(data: Partial<User>): Promise<API.Response<null>> {
  return request({
    url: '/sys/user',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(data: Partial<User>): Promise<API.Response<null>> {
  return request({
    url: '/sys/user',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id: number): Promise<API.Response<null>> {
  return request({
    url: `/sys/user/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除用户
 */
export function batchDeleteUsers(ids: number[]): Promise<API.Response<null>> {
  return request({
    url: '/sys/user/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 重置密码
 */
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
