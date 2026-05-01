export const APPLICATION_BATCH_DISPLAY_STATUS = {
  NOT_STARTED: 1,
  APPLYING: 2,
  REVIEWING: 3,
  PUBLICIZING: 4,
  COMPLETED: 5
} as const

export type BatchDisplayStatus =
  typeof APPLICATION_BATCH_DISPLAY_STATUS[keyof typeof APPLICATION_BATCH_DISPLAY_STATUS]

const APPLICATION_BATCH_STATUS_CONFIG: Record<
  BatchDisplayStatus,
  {
    text: string
    type: 'info' | 'success' | 'warning' | 'primary'
    canApply: boolean
  }
> = {
  [APPLICATION_BATCH_DISPLAY_STATUS.NOT_STARTED]: {
    text: '未开始',
    type: 'info',
    canApply: false
  },
  [APPLICATION_BATCH_DISPLAY_STATUS.APPLYING]: {
    text: '申请中',
    type: 'success',
    canApply: true
  },
  [APPLICATION_BATCH_DISPLAY_STATUS.REVIEWING]: {
    text: '评审中',
    type: 'warning',
    canApply: false
  },
  [APPLICATION_BATCH_DISPLAY_STATUS.PUBLICIZING]: {
    text: '公示中',
    type: 'primary',
    canApply: false
  },
  [APPLICATION_BATCH_DISPLAY_STATUS.COMPLETED]: {
    text: '已结束',
    type: 'info',
    canApply: false
  }
}

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

export function normalizeBatchStatus(status?: number | null): BatchDisplayStatus {
  if (status && status in APPLICATION_BATCH_STATUS_CONFIG) {
    return status as BatchDisplayStatus
  }
  return APPLICATION_BATCH_DISPLAY_STATUS.COMPLETED
}

export function getBatchStatusText(status: BatchDisplayStatus): string {
  return APPLICATION_BATCH_STATUS_CONFIG[status].text
}

export function getBatchStatusType(status: BatchDisplayStatus): 'info' | 'success' | 'warning' | 'primary' {
  return APPLICATION_BATCH_STATUS_CONFIG[status].type
}

export function canApplyForBatch(status: BatchDisplayStatus): boolean {
  return APPLICATION_BATCH_STATUS_CONFIG[status].canApply
}
