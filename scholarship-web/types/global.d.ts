/// <reference types="vite/client" />

// Vue 宏类型声明
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 环境变量类型
interface ImportMetaEnv {
  readonly VITE_RSA_PUBLIC_KEY: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_API_TARGET: string
  readonly VITE_APP_TITLE: string
  readonly VITE_USE_MOCK: string
  readonly VITE_LOG_LEVEL: string
  readonly VITE_ENABLE_ERROR_REPORTING: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 通用类型
declare namespace App {
  // 用户角色
  type UserRole = 'student' | 'tutor' | 'admin'

  // 用户类型（后端返回）
  type UserType = 1 | 2 | 3

  // 性别
  type Gender = 0 | 1

  // 学历层次
  type EducationLevel = 1 | 2

  // 申请状态
  type ApplicationStatus = 0 | 1 | 2 | 3 | 4 | 5 | 6

  // 用户状态
  type UserStatus = 0 | 1
}

// API 响应类型
declare namespace API {
  interface Response<T = any> {
    code: number
    message: string
    data: T
  }

  interface PageResponse<T = any> {
    records: T[]
    total: number
    current: number
    size: number
  }

  interface PageParams {
    current: number
    size: number
    [key: string]: any
  }
}

// 工具类型
declare type Nullable<T> = T | null
declare type NonNullable<T> = Exclude<T, null>
declare type Optional<T> = T | undefined
