/**
 * 系统通知相关 API 接口
 */
import type { AxiosResponse } from 'axios'
import request from '@/utils/request'

/**
 * 系统通知
 */
export interface Notification {
  id: number
  title: string
  content: string
  notificationType: number
  receiverId?: number
  roleId?: number
  version?: number
  receiverType?: number
  businessId?: number
  isRead?: number
  readTime?: string
  senderId?: number
  senderName?: string
  deleted?: number
  createTime: string
}

/**
 * 获取最新通知列表
 * @param limit - 数量限制，默认 5
 * @returns 通知列表响应
 */
export function getLatestNotifications(
  limit: number = 5
): Promise<AxiosResponse<API.Response<Notification[]>>> {
  return request({
    url: '/sys/notification/latest',
    method: 'get',
    params: { limit }
  })
}

export default {
  getLatestNotifications
}
