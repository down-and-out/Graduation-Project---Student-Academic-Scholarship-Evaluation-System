export const REVIEW_DISPLAY_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 3
} as const

export const REVIEW_SUBMIT_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2
} as const

export const REVIEW_STATUS_OPTIONS = [
  { label: '待审核', value: REVIEW_DISPLAY_STATUS.PENDING },
  { label: '已通过', value: REVIEW_DISPLAY_STATUS.APPROVED },
  { label: '未通过', value: REVIEW_DISPLAY_STATUS.REJECTED }
]

export const REVIEW_STATUS_LABELS: Record<number, string> = {
  [REVIEW_DISPLAY_STATUS.PENDING]: '待审核',
  [REVIEW_DISPLAY_STATUS.APPROVED]: '已通过',
  [REVIEW_DISPLAY_STATUS.REJECTED]: '未通过'
}

export const REVIEW_STATUS_TAG_TYPES: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
  [REVIEW_DISPLAY_STATUS.PENDING]: 'warning',
  [REVIEW_DISPLAY_STATUS.APPROVED]: 'success',
  [REVIEW_DISPLAY_STATUS.REJECTED]: 'danger'
}

export function normalizeReviewStatus(status?: number | null): number {
  if (status === REVIEW_SUBMIT_STATUS.REJECTED) {
    return REVIEW_DISPLAY_STATUS.REJECTED
  }
  return status ?? REVIEW_DISPLAY_STATUS.PENDING
}

export function toSubmitReviewStatus(status?: number | null): number {
  if (status === REVIEW_DISPLAY_STATUS.REJECTED) {
    return REVIEW_SUBMIT_STATUS.REJECTED
  }
  return status ?? REVIEW_SUBMIT_STATUS.PENDING
}
