export const USER_TYPE = {
  STUDENT: 1,
  TUTOR: 2,
  ADMIN: 3
} as const

export const USER_STATUS = {
  DISABLED: 0,
  ENABLED: 1
} as const

export const GENDER = {
  FEMALE: 0,
  MALE: 1
} as const

export const EDUCATION_LEVEL = {
  MASTER: 1,
  PHD: 2
} as const

export const TRAINING_MODE = {
  FULL_TIME: 1,
  PART_TIME: 2
} as const

export const STUDENT_STATUS = {
  SUSPENDED: 0,
  ACTIVE: 1,
  GRADUATED: 2,
  DROPPED: 3
} as const

export const ROLE_OPTIONS = [
  { label: '研究生', value: USER_TYPE.STUDENT },
  { label: '导师', value: USER_TYPE.TUTOR },
  { label: '管理员', value: USER_TYPE.ADMIN }
]

export const USER_STATUS_OPTIONS = [
  { label: '禁用', value: USER_STATUS.DISABLED },
  { label: '启用', value: USER_STATUS.ENABLED }
]

export const GENDER_OPTIONS = [
  { label: '女', value: GENDER.FEMALE },
  { label: '男', value: GENDER.MALE }
]

export const EDUCATION_LEVEL_OPTIONS = [
  { label: '硕士', value: EDUCATION_LEVEL.MASTER },
  { label: '博士', value: EDUCATION_LEVEL.PHD }
]

export const TRAINING_MODE_OPTIONS = [
  { label: '全日制', value: TRAINING_MODE.FULL_TIME },
  { label: '非全日制', value: TRAINING_MODE.PART_TIME }
]

export const STUDENT_STATUS_OPTIONS = [
  { label: '休学', value: STUDENT_STATUS.SUSPENDED },
  { label: '在读', value: STUDENT_STATUS.ACTIVE },
  { label: '毕业', value: STUDENT_STATUS.GRADUATED },
  { label: '退学', value: STUDENT_STATUS.DROPPED }
]

export const USER_TYPE_MAP: Record<number, { text: string; type: 'success' | 'warning' | 'danger' }> = {
  [USER_TYPE.STUDENT]: { text: '研究生', type: 'success' },
  [USER_TYPE.TUTOR]: { text: '导师', type: 'warning' },
  [USER_TYPE.ADMIN]: { text: '管理员', type: 'danger' }
}

export const USER_STATUS_MAP: Record<number, { text: string; type: 'info' | 'success' }> = {
  [USER_STATUS.DISABLED]: { text: '禁用', type: 'info' },
  [USER_STATUS.ENABLED]: { text: '启用', type: 'success' }
}

export const GENDER_TEXT_MAP: Record<number, string> = {
  [GENDER.FEMALE]: '女',
  [GENDER.MALE]: '男'
}

export const EDUCATION_LEVEL_TEXT_MAP: Record<number, string> = {
  [EDUCATION_LEVEL.MASTER]: '硕士',
  [EDUCATION_LEVEL.PHD]: '博士'
}

export const STUDENT_STATUS_TEXT_MAP: Record<number, string> = {
  [STUDENT_STATUS.SUSPENDED]: '休学',
  [STUDENT_STATUS.ACTIVE]: '在读',
  [STUDENT_STATUS.GRADUATED]: '毕业',
  [STUDENT_STATUS.DROPPED]: '退学'
}

export const STUDENT_STATUS_TAG_TYPE_MAP: Record<number, '' | 'success' | 'info' | 'danger'> = {
  [STUDENT_STATUS.SUSPENDED]: 'info',
  [STUDENT_STATUS.ACTIVE]: 'success',
  [STUDENT_STATUS.GRADUATED]: '',
  [STUDENT_STATUS.DROPPED]: 'danger'
}
