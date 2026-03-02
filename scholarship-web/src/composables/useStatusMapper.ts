/**
 * 状态映射 Hook
 * 用于管理状态与文本、类型的映射关系
 */

/**
 * 状态映射工具接口
 */
export interface StatusMapper {
  getText: (status: number | string, defaultText?: string) => string
  getType: (status: number | string, defaultType?: string) => string
}

/**
 * 状态映射配置
 */
export interface StatusMapperConfig {
  textMap: Record<number | string, string>
  typeMap: Record<number | string, string>
}

/**
 * 创建状态映射工具
 * @param textMap - 状态文本映射
 * @param typeMap - 状态类型映射
 * @returns 状态映射工具
 */
export function createStatusMapper(
  textMap: Record<number | string, string>,
  typeMap: Record<number | string, string>
): StatusMapper {
  /**
   * 获取状态文本
   * @param status - 状态值
   * @param defaultText - 默认文本
   * @returns 状态文本
   */
  function getText(status: number | string, defaultText = '未知'): string {
    return textMap[status] ?? defaultText
  }

  /**
   * 获取状态类型
   * @param status - 状态值
   * @param defaultType - 默认类型
   * @returns 状态类型
   */
  function getType(status: number | string, defaultType = ''): string {
    return typeMap[status] ?? defaultType
  }

  return {
    getText,
    getType
  }
}

/**
 * 申请状态映射工具
 */
export const applicationStatusMapper = createStatusMapper(
  {
    0: '草稿',
    1: '已提交',
    2: '审核中',
    3: '评审完成',
    4: '已完成',
    5: '已发放',
    6: '已拒绝'
  },
  {
    0: 'info',
    1: 'warning',
    2: 'warning',
    3: 'info',
    4: 'success',
    5: 'success',
    6: 'danger'
  }
)

/**
 * 用户状态映射工具
 */
export const userStatusMapper = createStatusMapper(
  {
    0: '禁用',
    1: '启用'
  },
  {
    0: 'info',
    1: 'success'
  }
)

/**
 * 角色映射工具
 */
export const roleMapper = createStatusMapper(
  {
    student: '研究生',
    tutor: '导师',
    admin: '管理员'
  },
  {
    student: 'success',
    tutor: 'warning',
    admin: 'danger'
  }
)

export default createStatusMapper
