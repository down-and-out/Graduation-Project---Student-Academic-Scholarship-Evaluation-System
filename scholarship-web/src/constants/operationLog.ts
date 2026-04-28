export const OPERATION_LOG_TYPE = {
  LOGIN: 1,
  USER_MANAGEMENT: 2,
  EVALUATION_MANAGEMENT: 3,
  SYSTEM_SETTING: 4
} as const

export type OperationLogTypeValue = typeof OPERATION_LOG_TYPE[keyof typeof OPERATION_LOG_TYPE]

export const LOG_TYPE_OPTIONS: Array<{ value: OperationLogTypeValue; label: string }> = [
  { value: OPERATION_LOG_TYPE.LOGIN, label: '登录' },
  { value: OPERATION_LOG_TYPE.USER_MANAGEMENT, label: '用户管理' },
  { value: OPERATION_LOG_TYPE.EVALUATION_MANAGEMENT, label: '评定管理' },
  { value: OPERATION_LOG_TYPE.SYSTEM_SETTING, label: '系统设置' }
]

export const LOG_TYPE_LABELS: Record<number, string> = {
  [OPERATION_LOG_TYPE.LOGIN]: '登录',
  [OPERATION_LOG_TYPE.USER_MANAGEMENT]: '用户管理',
  [OPERATION_LOG_TYPE.EVALUATION_MANAGEMENT]: '评定管理',
  [OPERATION_LOG_TYPE.SYSTEM_SETTING]: '系统设置'
}
