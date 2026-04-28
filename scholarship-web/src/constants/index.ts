/**
 * 业务常量定义
 */

/**
 * 用户角色
 */
export const USER_ROLE = {
  STUDENT: 'student',
  TUTOR: 'tutor',
  ADMIN: 'admin'
} as const

/**
 * 用户角色类型
 */
export type UserRole = typeof USER_ROLE[keyof typeof USER_ROLE]

/**
 * 大批量查询 size（下拉选项全量拉取）
 */
export const LARGE_QUERY_SIZE = 1000

/**
 * 用户类型映射（后端返回的数字类型）
 */
export const USER_TYPE_MAP: Record<number, UserRole> = {
  1: USER_ROLE.STUDENT,
  2: USER_ROLE.TUTOR,
  3: USER_ROLE.ADMIN
}

/**
 * 申请状态枚举
 */
export const APPLICATION_STATUS = {
  DRAFT: 0,       // 草稿
  SUBMITTED: 1,   // 已提交
  REVIEWING: 2,   // 审核中
  REVIEWED: 3,    // 评审完成
  COMPLETED: 4,   // 已完成
  PAID: 5,        // 已发放
  REJECTED: 6     // 已拒绝
} as const

/**
 * 申请状态类型
 */
export type ApplicationStatusType = typeof APPLICATION_STATUS[keyof typeof APPLICATION_STATUS]

/**
 * 申请状态文本映射
 */
export const APPLICATION_STATUS_TEXT: Record<number, string> = {
  [APPLICATION_STATUS.DRAFT]: '草稿',
  [APPLICATION_STATUS.SUBMITTED]: '已提交',
  [APPLICATION_STATUS.REVIEWING]: '审核中',
  [APPLICATION_STATUS.REVIEWED]: '评审完成',
  [APPLICATION_STATUS.COMPLETED]: '已完成',
  [APPLICATION_STATUS.PAID]: '已发放',
  [APPLICATION_STATUS.REJECTED]: '已拒绝'
}

/**
 * 申请状态标签类型映射
 */
export const APPLICATION_STATUS_TYPE: Record<number, 'info' | 'warning' | 'success' | 'danger'> = {
  [APPLICATION_STATUS.DRAFT]: 'info',
  [APPLICATION_STATUS.SUBMITTED]: 'warning',
  [APPLICATION_STATUS.REVIEWING]: 'warning',
  [APPLICATION_STATUS.REVIEWED]: 'info',
  [APPLICATION_STATUS.COMPLETED]: 'success',
  [APPLICATION_STATUS.PAID]: 'success',
  [APPLICATION_STATUS.REJECTED]: 'danger'
}

/**
 * 性别常量
 */
export const GENDER = {
  FEMALE: 0 as const,
  MALE: 1 as const
}

/**
 * 性别文本映射
 */
export const GENDER_TEXT: Record<number, string> = {
  [GENDER.FEMALE]: '女',
  [GENDER.MALE]: '男'
}

/**
 * 学历层次常量
 */
export const EDUCATION_LEVEL = {
  MASTER: 1 as const,  // 硕士
  DOCTOR: 2 as const   // 博士
}

/**
 * 学历层次文本映射
 */
export const EDUCATION_LEVEL_TEXT: Record<number, string> = {
  [EDUCATION_LEVEL.MASTER]: '硕士',
  [EDUCATION_LEVEL.DOCTOR]: '博士'
}

/**
 * 用户状态
 */
export const USER_STATUS = {
  DISABLED: 0 as const,
  ENABLED: 1 as const
}

/**
 * 用户状态文本映射
 */
export const USER_STATUS_TEXT: Record<number, string> = {
  [USER_STATUS.DISABLED]: '禁用',
  [USER_STATUS.ENABLED]: '正常'
}

/**
 * 培养方式常量
 */
export const TRAINING_MODE = {
  FULL_TIME: 1 as const,    // 全日制
  PART_TIME: 2 as const     // 非全日制
}

/**
 * 培养方式文本映射
 */
export const TRAINING_MODE_TEXT: Record<number, string> = {
  [TRAINING_MODE.FULL_TIME]: '全日制',
  [TRAINING_MODE.PART_TIME]: '非全日制'
}

/**
 * 学籍状态常量
 */
export const STUDENT_STATUS = {
  SUSPENDED: 0 as const,   // 休学
  ACTIVE: 1 as const,      // 在读
  GRADUATED: 2 as const,   // 毕业
  DROPPED: 3 as const       // 退学
}

/**
 * 学籍状态文本映射
 */
export const STUDENT_STATUS_TEXT: Record<number, string> = {
  [STUDENT_STATUS.SUSPENDED]: '休学',
  [STUDENT_STATUS.ACTIVE]: '在读',
  [STUDENT_STATUS.GRADUATED]: '毕业',
  [STUDENT_STATUS.DROPPED]: '退学'
}

/**
 * 生成年份选项（当前年份前后若干年）
 */
export function generateYearOptions(range: number = 5): number[] {
  const year = new Date().getFullYear()
  return Array.from({ length: range * 2 + 1 }, (_, i) => year - range + i).reverse()
}

export default {
  USER_ROLE,
  USER_TYPE_MAP,
  APPLICATION_STATUS,
  APPLICATION_STATUS_TEXT,
  APPLICATION_STATUS_TYPE,
  GENDER,
  GENDER_TEXT,
  EDUCATION_LEVEL,
  EDUCATION_LEVEL_TEXT,
  USER_STATUS,
  USER_STATUS_TEXT,
  TRAINING_MODE,
  TRAINING_MODE_TEXT,
  STUDENT_STATUS,
  STUDENT_STATUS_TEXT,
  generateYearOptions
}
