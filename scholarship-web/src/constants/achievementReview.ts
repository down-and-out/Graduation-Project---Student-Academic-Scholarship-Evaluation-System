import type { Option } from '@/constants/achievement'

export const GENERIC_AUDIT_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2
} as const

export const GENERIC_AUDIT_OPTIONS: Option[] = [
  { label: '待审核', value: GENERIC_AUDIT_STATUS.PENDING },
  { label: '审核通过', value: GENERIC_AUDIT_STATUS.APPROVED },
  { label: '审核驳回', value: GENERIC_AUDIT_STATUS.REJECTED }
]

export const GENERIC_AUDIT_LABELS: Record<number, string> = {
  [GENERIC_AUDIT_STATUS.PENDING]: '待审核',
  [GENERIC_AUDIT_STATUS.APPROVED]: '审核通过',
  [GENERIC_AUDIT_STATUS.REJECTED]: '审核驳回'
}

export const GENERIC_AUDIT_TYPES: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
  [GENERIC_AUDIT_STATUS.PENDING]: 'warning',
  [GENERIC_AUDIT_STATUS.APPROVED]: 'success',
  [GENERIC_AUDIT_STATUS.REJECTED]: 'danger'
}
