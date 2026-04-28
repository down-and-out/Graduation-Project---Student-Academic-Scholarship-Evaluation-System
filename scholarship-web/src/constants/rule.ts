export const RULE_TYPE = {
  PAPER: 1,
  PATENT: 2,
  PROJECT: 3,
  COMPETITION: 4,
  COURSE: 5,
  MORAL: 6
} as const

export const RULE_TYPE_OPTIONS = [
  { label: '论文', value: RULE_TYPE.PAPER },
  { label: '专利', value: RULE_TYPE.PATENT },
  { label: '项目', value: RULE_TYPE.PROJECT },
  { label: '竞赛', value: RULE_TYPE.COMPETITION },
  { label: '课程', value: RULE_TYPE.COURSE },
  { label: '德育', value: RULE_TYPE.MORAL }
]

export const RULE_TYPE_LABELS: Record<number, string> = {
  [RULE_TYPE.PAPER]: '论文',
  [RULE_TYPE.PATENT]: '专利',
  [RULE_TYPE.PROJECT]: '项目',
  [RULE_TYPE.COMPETITION]: '竞赛',
  [RULE_TYPE.COURSE]: '课程',
  [RULE_TYPE.MORAL]: '德育'
}

export const RULE_TYPE_TAG_TYPES: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
  [RULE_TYPE.PAPER]: 'primary',
  [RULE_TYPE.PATENT]: 'success',
  [RULE_TYPE.PROJECT]: 'warning',
  [RULE_TYPE.COMPETITION]: 'info',
  [RULE_TYPE.COURSE]: 'danger',
  [RULE_TYPE.MORAL]: 'warning'
}
