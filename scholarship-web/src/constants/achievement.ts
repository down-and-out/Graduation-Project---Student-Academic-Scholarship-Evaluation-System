export type Option = {
  label: string
  value: number
}

export const ACHIEVEMENT_TYPE_OPTIONS = [
  { label: '论文', value: 'paper' },
  { label: '专利', value: 'patent' },
  { label: '项目', value: 'project' },
  { label: '竞赛', value: 'competition' }
] as const

export const ACHIEVEMENT_TYPE_ID_OPTIONS: Option[] = [
  { label: '论文', value: 1 },
  { label: '专利', value: 2 },
  { label: '项目', value: 3 },
  { label: '竞赛', value: 4 }
]

export const ACHIEVEMENT_TYPE_ID_LABELS: Record<number, string> = Object.fromEntries(
  ACHIEVEMENT_TYPE_ID_OPTIONS.map(item => [item.value, item.label])
) as Record<number, string>

export const AUTHOR_RANK = {
  FIRST: 1,
  SECOND: 2,
  CORRESPONDING: 3
} as const

export const AUTHOR_RANK_OPTIONS: Option[] = [
  { label: '第一作者', value: AUTHOR_RANK.FIRST },
  { label: '第二作者', value: AUTHOR_RANK.SECOND },
  { label: '通讯作者', value: AUTHOR_RANK.CORRESPONDING }
]

export const JOURNAL_LEVEL_OPTIONS: Option[] = [
  { label: 'SCI 一区', value: 1 },
  { label: 'SCI 二区', value: 2 },
  { label: 'SCI 三区', value: 3 },
  { label: 'SCI 四区', value: 4 },
  { label: 'EI', value: 5 },
  { label: '核心期刊', value: 6 },
  { label: '普通期刊', value: 7 },
  { label: 'ISTP', value: 8 }
]

export const JOURNAL_LEVEL_LABELS: Record<number, string> = Object.fromEntries(
  JOURNAL_LEVEL_OPTIONS.map(item => [item.value, item.label])
) as Record<number, string>

export const PROJECT_TYPE_OPTIONS: Option[] = [
  { label: '纵向项目', value: 1 },
  { label: '横向项目', value: 2 },
  { label: '校级项目', value: 3 }
]

export const PROJECT_LEVEL_OPTIONS: Option[] = [
  { label: '国家级', value: 1 },
  { label: '省部级', value: 2 },
  { label: '市厅级', value: 3 },
  { label: '校级', value: 4 }
]

export const PROJECT_LEVEL_LABELS: Record<number, string> = Object.fromEntries(
  PROJECT_LEVEL_OPTIONS.map(item => [item.value, item.label])
) as Record<number, string>

export const PROJECT_ROLE_OPTIONS: Option[] = [
  { label: '负责人', value: 1 },
  { label: '核心成员', value: 2 },
  { label: '参与成员', value: 3 }
]

export const PROJECT_STATUS_OPTIONS: Option[] = [
  { label: '立项中', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结题', value: 3 }
]

export const COMPETITION_LEVEL_OPTIONS: Option[] = [
  { label: '国际级', value: 1 },
  { label: '国家级', value: 2 },
  { label: '省级', value: 3 },
  { label: '校级', value: 4 },
  { label: '院级', value: 5 }
]

export const COMPETITION_LEVEL_LABELS: Record<number, string> = Object.fromEntries(
  COMPETITION_LEVEL_OPTIONS.map(item => [item.value, item.label])
) as Record<number, string>

export const COMPETITION_AWARD_LEVEL_OPTIONS: Option[] = [
  { label: '特等奖', value: 1 },
  { label: '一等奖', value: 2 },
  { label: '二等奖', value: 3 },
  { label: '三等奖', value: 4 },
  { label: '优秀奖', value: 5 }
]

export const COMPETITION_AWARD_TYPE_OPTIONS: Option[] = [
  { label: '个人赛', value: 1 },
  { label: '团队赛', value: 2 }
]

export function getAchievementOptionLabel(
  options: readonly Option[],
  value?: number | null,
  fallback = '-'
): string {
  return options.find(item => item.value === value)?.label || fallback
}
