export const APPLICATION_BATCH_DISPLAY_STATUS = {
  ACTIVE: 'active',
  CLOSED: 'closed'
} as const

export const APPLICATION_BATCH_STATUS_CONFIG = {
  [APPLICATION_BATCH_DISPLAY_STATUS.ACTIVE]: {
    text: '可申请',
    type: 'success'
  },
  [APPLICATION_BATCH_DISPLAY_STATUS.CLOSED]: {
    text: '已结束',
    type: 'info'
  }
} as const

export const APPLICATION_STEP_CONFIG = [
  { title: '草稿', description: '填写申请信息' },
  { title: '已提交', description: '等待导师审核' },
  { title: '审核中', description: '学院评审中' },
  { title: '评审完成', description: '等待公示' },
  { title: '已完成', description: '查看评定结果' }
] as const

export const APPLICATION_STATUS_STEP_MAP: Record<number, number> = {
  0: 0,
  1: 1,
  2: 2,
  3: 3,
  4: 4,
  5: 4
}

export type BatchDisplayStatus = typeof APPLICATION_BATCH_DISPLAY_STATUS[keyof typeof APPLICATION_BATCH_DISPLAY_STATUS]

export function getBatchStatusText(status: BatchDisplayStatus): string {
  return APPLICATION_BATCH_STATUS_CONFIG[status].text
}

export function getBatchStatusType(status: BatchDisplayStatus): 'success' | 'info' {
  return APPLICATION_BATCH_STATUS_CONFIG[status].type
}
