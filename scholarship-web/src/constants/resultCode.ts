/**
 * 响应状态码常量
 */

/**
 * 成功
 */
export const SUCCESS = 200

/**
 * 客户端错误
 */
export const BAD_REQUEST = 400
export const UNAUTHORIZED = 401
export const FORBIDDEN = 403
export const NOT_FOUND = 404
export const METHOD_NOT_ALLOWED = 405
export const CONFLICT = 409
export const UNPROCESSABLE_ENTITY = 422
export const TOO_MANY_REQUESTS = 429

/**
 * 服务端错误
 */
export const INTERNAL_SERVER_ERROR = 500
export const BAD_GATEWAY = 502
export const SERVICE_UNAVAILABLE = 503
export const GATEWAY_TIMEOUT = 504

/**
 * Token 相关错误码
 */
export const TOKEN_EXPIRED = 6101
export const TOKEN_INVALID = 6102
export const TOKEN_BLACKLISTED = 6103

/**
 * 业务错误码
 */
export const VALIDATION_ERROR = 4000
export const DATA_NOT_FOUND = 4040
export const DUPLICATE_DATA = 4090
export const OPERATION_FAILED = 5000

/**
 * 错误消息映射
 */
export const ERROR_MESSAGES: Record<number, string> = {
  [SUCCESS]: '请求成功',
  [BAD_REQUEST]: '请求参数错误',
  [UNAUTHORIZED]: '未授权，请先登录',
  [FORBIDDEN]: '禁止访问',
  [NOT_FOUND]: '请求的资源不存在',
  [METHOD_NOT_ALLOWED]: '请求方法不允许',
  [CONFLICT]: '资源冲突',
  [UNPROCESSABLE_ENTITY]: '请求参数无法被处理',
  [TOO_MANY_REQUESTS]: '请求次数过多',
  [INTERNAL_SERVER_ERROR]: '服务器内部错误',
  [BAD_GATEWAY]: '网关错误',
  [SERVICE_UNAVAILABLE]: '服务不可用',
  [GATEWAY_TIMEOUT]: '网关超时',
  [TOKEN_EXPIRED]: 'Token 已过期',
  [TOKEN_INVALID]: 'Token 无效',
  [TOKEN_BLACKLISTED]: 'Token 已被加入黑名单',
  [VALIDATION_ERROR]: '数据验证失败',
  [DATA_NOT_FOUND]: '数据不存在',
  [DUPLICATE_DATA]: '数据重复',
  [OPERATION_FAILED]: '操作失败'
}

/**
 * 获取错误消息
 * @param code - 错误码
 * @param defaultMessage - 默认消息
 * @returns 错误消息
 */
export function getMessage(code: number, defaultMessage = '请求失败'): string {
  return ERROR_MESSAGES[code] || defaultMessage
}

/**
 * 判断是否为成功响应
 * @param code - 响应码
 * @returns 是否成功
 */
export function isSuccess(code: number): boolean {
  return code === SUCCESS
}

/**
 * 判断是否为 Token 相关错误
 * @param code - 响应码
 * @returns 是否为 Token 错误
 */
export function isTokenError(code: number): boolean {
  return [TOKEN_EXPIRED, TOKEN_INVALID, TOKEN_BLACKLISTED].includes(code)
}

export default {
  SUCCESS,
  BAD_REQUEST,
  UNAUTHORIZED,
  FORBIDDEN,
  NOT_FOUND,
  METHOD_NOT_ALLOWED,
  CONFLICT,
  UNPROCESSABLE_ENTITY,
  TOO_MANY_REQUESTS,
  INTERNAL_SERVER_ERROR,
  BAD_GATEWAY,
  SERVICE_UNAVAILABLE,
  GATEWAY_TIMEOUT,
  TOKEN_EXPIRED,
  TOKEN_INVALID,
  TOKEN_BLACKLISTED,
  VALIDATION_ERROR,
  DATA_NOT_FOUND,
  DUPLICATE_DATA,
  OPERATION_FAILED,
  ERROR_MESSAGES,
  getMessage,
  isSuccess,
  isTokenError
}
