import type { Option } from '@/constants/achievement'

export const COURSE_SCORE_SEMESTER_OPTIONS: Option[] = [
  { label: '第一学期', value: 1 },
  { label: '第二学期', value: 2 },
  { label: '夏季学期', value: 3 }
]

export const COURSE_SCORE_SEMESTER_LABELS: Record<number, string> = Object.fromEntries(
  COURSE_SCORE_SEMESTER_OPTIONS.map(item => [item.value, item.label])
) as Record<number, string>

export const COURSE_TYPE_OPTIONS: Option[] = [
  { label: '必修', value: 1 },
  { label: '选修', value: 2 },
  { label: '任选', value: 3 }
]

export const COURSE_TYPE_LABELS: Record<number, string> = Object.fromEntries(
  COURSE_TYPE_OPTIONS.map(item => [item.value, item.label])
) as Record<number, string>
