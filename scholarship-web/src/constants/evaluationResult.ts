export type ResultTagType = 'success' | 'warning' | 'danger' | 'info' | 'primary'

export interface AwardLevelConfig {
  text: string
  type: ResultTagType
  className: 'award-first' | 'award-second' | 'award-third' | 'award-none'
}

export interface ResultStatusConfig {
  text: string
  type: ResultTagType
}

export const AWARD_LEVEL_MIN = 1
export const AWARD_LEVEL_MAX = 4

const FALLBACK_AWARD_LEVEL = 5
const FALLBACK_RESULT_STATUS = 0

const AWARD_LEVEL_CONFIG: Record<number, AwardLevelConfig> = {
  1: { text: '特等奖学金', type: 'danger', className: 'award-first' },
  2: { text: '一等奖学金', type: 'danger', className: 'award-first' },
  3: { text: '二等奖学金', type: 'warning', className: 'award-second' },
  4: { text: '三等奖学金', type: 'success', className: 'award-third' },
  [FALLBACK_AWARD_LEVEL]: { text: '未获奖', type: 'info', className: 'award-none' }
}

const RESULT_STATUS_CONFIG: Record<number, ResultStatusConfig> = {
  0: { text: '待公示', type: 'info' },
  1: { text: '公示中', type: 'warning' },
  2: { text: '已确认', type: 'success' },
  3: { text: '有异议', type: 'danger' }
}

export const RESULT_STATUS_OPTIONS = Object.entries(RESULT_STATUS_CONFIG).map(([value, config]) => ({
  value: Number(value),
  label: config.text
}))

export function normalizeAwardLevel(level?: number | null): number {
  return level !== undefined && level !== null && level >= AWARD_LEVEL_MIN && level <= AWARD_LEVEL_MAX
    ? level
    : FALLBACK_AWARD_LEVEL
}

export function getAwardLevelConfig(level?: number | null): AwardLevelConfig {
  return AWARD_LEVEL_CONFIG[normalizeAwardLevel(level)]
}

export function normalizeResultStatus(...candidates: Array<number | null | undefined>): number {
  for (const candidate of candidates) {
    if (candidate !== undefined && candidate !== null && candidate >= 0 && candidate <= 3) {
      return candidate
    }
  }
  return FALLBACK_RESULT_STATUS
}

export function getResultStatusConfig(status?: number | null): ResultStatusConfig {
  return RESULT_STATUS_CONFIG[normalizeResultStatus(status)]
}
