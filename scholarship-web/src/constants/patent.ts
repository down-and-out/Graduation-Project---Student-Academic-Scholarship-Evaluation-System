export const PATENT_TYPE = {
  INVENT: 1,
  UTILITY: 2,
  DESIGN: 3,
  SOFTWARE_COPYRIGHT: 4
} as const

export const PATENT_STATUS = {
  APPLYING: 1,
  AUTHORIZED: 2,
  REJECTED: 3
} as const

export const AUDIT_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2
} as const

export const PATENT_TYPE_OPTIONS = [
  { label: '发明专利', value: PATENT_TYPE.INVENT },
  { label: '实用新型', value: PATENT_TYPE.UTILITY },
  { label: '外观设计', value: PATENT_TYPE.DESIGN },
  { label: '软件著作权', value: PATENT_TYPE.SOFTWARE_COPYRIGHT }
]

export const PATENT_STATUS_OPTIONS = [
  { label: '申请中', value: PATENT_STATUS.APPLYING },
  { label: '已授权', value: PATENT_STATUS.AUTHORIZED },
  { label: '已失效', value: PATENT_STATUS.REJECTED }
]

export const AUDIT_STATUS_OPTIONS = [
  { label: '待审核', value: AUDIT_STATUS.PENDING },
  { label: '审核通过', value: AUDIT_STATUS.APPROVED },
  { label: '审核驳回', value: AUDIT_STATUS.REJECTED }
]

export const PATENT_TYPE_LABELS: Record<number, string> = {
  [PATENT_TYPE.INVENT]: '发明专利',
  [PATENT_TYPE.UTILITY]: '实用新型',
  [PATENT_TYPE.DESIGN]: '外观设计',
  [PATENT_TYPE.SOFTWARE_COPYRIGHT]: '软件著作权'
}

export const PATENT_TYPE_TAG_TYPES: Record<number, 'danger' | 'success' | 'warning' | 'info'> = {
  [PATENT_TYPE.INVENT]: 'danger',
  [PATENT_TYPE.UTILITY]: 'success',
  [PATENT_TYPE.DESIGN]: 'warning',
  [PATENT_TYPE.SOFTWARE_COPYRIGHT]: 'info'
}

export const PATENT_STATUS_LABELS: Record<number, string> = {
  [PATENT_STATUS.APPLYING]: '申请中',
  [PATENT_STATUS.AUTHORIZED]: '已授权',
  [PATENT_STATUS.REJECTED]: '已失效'
}

export const PATENT_STATUS_TAG_TYPES: Record<number, 'info' | 'success' | 'danger'> = {
  [PATENT_STATUS.APPLYING]: 'info',
  [PATENT_STATUS.AUTHORIZED]: 'success',
  [PATENT_STATUS.REJECTED]: 'danger'
}

export const AUDIT_STATUS_LABELS: Record<number, string> = {
  [AUDIT_STATUS.PENDING]: '待审核',
  [AUDIT_STATUS.APPROVED]: '通过',
  [AUDIT_STATUS.REJECTED]: '驳回'
}

export const AUDIT_STATUS_FILTER_LABELS: Record<number, string> = {
  [AUDIT_STATUS.PENDING]: '待审核',
  [AUDIT_STATUS.APPROVED]: '审核通过',
  [AUDIT_STATUS.REJECTED]: '审核驳回'
}

export const AUDIT_STATUS_TAG_TYPES: Record<number, 'warning' | 'success' | 'danger'> = {
  [AUDIT_STATUS.PENDING]: 'warning',
  [AUDIT_STATUS.APPROVED]: 'success',
  [AUDIT_STATUS.REJECTED]: 'danger'
}
