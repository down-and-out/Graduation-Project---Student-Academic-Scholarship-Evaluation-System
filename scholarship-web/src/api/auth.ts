/**
 * 认证相关 API 接口
 */
import request from '@/utils/request'

/**
 * 登录请求参数
 */
export interface LoginParams {
  username: string
  password: string
}

/**
 * 登录响应数据
 */
export interface LoginData {
  token: string
  userId: number
  username: string
  realName: string
  userType: number
  avatar?: string
}

/**
 * 用户信息
 */
export interface UserInfo {
  userId: number
  username: string
  realName: string
  userType: number
  avatar?: string
  email?: string
  phone?: string
  gender?: number
}

/**
 * 用户登录
 * @param data - 登录信息
 * @returns 返回包含 token 和用户信息的响应
 */
export function login(data: LoginParams): Promise<API.Response<LoginData>> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns Promise
 */
export function logout(): Promise<API.Response<null>> {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 * @returns Promise
 */
export function getCurrentUser(): Promise<API.Response<UserInfo>> {
  return request({
    url: '/auth/current-user',
    method: 'get'
  })
}

export default {
  login,
  logout,
  getCurrentUser
}
