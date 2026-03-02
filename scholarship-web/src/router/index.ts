/**
 * Vue Router 路由配置文件
 * 定义应用的所有路由规则
 * @see https://router.vuejs.org/zh/
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { tokenStore, userInfoStore } from '@/utils/secureStorage'
import type { UserInfo } from '@/utils/secureStorage'

// 存储前缀，与 secureStorage.ts 保持一致
const STORAGE_PREFIX = 'scholarship_'

/**
 * 路由元信息接口
 */
export interface RouteMeta {
  title?: string
  icon?: string
  requiresAuth?: boolean
  role?: App.UserRole
}

/**
 * 用户类型与角色映射
 */
const USER_ROLE_MAP: Record<number, App.UserRole> = {
  1: 'student',  // 研究生
  2: 'tutor',    // 导师
  3: 'admin'     // 管理员
}

/**
 * 检查是否已登录（同时验证 token 和用户信息）
 */
function checkAuth(): boolean {
  const token = tokenStore.get()
  const userInfo = userInfoStore.get()

  // 需要同时有 token 和用户信息才认为已登录
  return !!(token && userInfo)
}

/**
 * 获取用户角色
 */
function getUserRole(): App.UserRole | null {
  const userInfo: UserInfo | null = userInfoStore.get()
  if (!userInfo?.userType) return null
  return USER_ROLE_MAP[userInfo.userType]
}

/**
 * 清除认证状态
 * 用于 token 过期或无效时
 */
function clearAuth(): void {
  tokenStore.clear()
  userInfoStore.clear()
}

/**
 * 路由配置数组
 */
const routes: RouteRecordRaw[] = [
  // ========== 重定向 ==========
  {
    path: '/',
    redirect: '/login'
  },

  // ========== 登录页面 ==========
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false  // 不需要认证即可访问
    }
  },

  // ========== 主应用布局 ==========
  {
    path: '/app',
    component: () => import('@/views/layout/AppLayout.vue'),
    meta: {
      requiresAuth: true  // 需要登录认证
    },
    children: [
      // ========== 首页 ==========
      {
        path: '',
        redirect: '/app/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '系统首页',
          icon: 'HomeFilled'
        }
      },

      // ========== 研究生端路由 ==========
      {
        path: 'student',
        name: 'Student',
        meta: {
          title: '研究生管理',
          icon: 'User',
          role: 'student'  // 仅限学生角色访问
        },
        children: [
          {
            path: 'profile',
            name: 'StudentProfile',
            component: () => import('@/views/student/Profile.vue'),
            meta: { title: '个人信息' }
          },
          {
            path: 'achievements',
            name: 'StudentAchievements',
            component: () => import('@/views/student/Achievements.vue'),
            meta: { title: '科研成果管理' }
          },
          {
            path: 'application',
            name: 'StudentApplication',
            component: () => import('@/views/student/Application.vue'),
            meta: { title: '奖学金申请' }
          },
          {
            path: 'result',
            name: 'StudentResult',
            component: () => import('@/views/student/Result.vue'),
            meta: { title: '评定结果查询' }
          }
        ]
      },

      // ========== 导师端路由 ==========
      {
        path: 'tutor',
        name: 'Tutor',
        meta: {
          title: '导师管理',
          icon: 'Reading',
          role: 'tutor'  // 仅限导师角色访问
        },
        children: [
          {
            path: 'review',
            name: 'TutorReview',
            component: () => import('@/views/tutor/Review.vue'),
            meta: { title: '科研成果审核' }
          },
          {
            path: 'students',
            name: 'TutorStudents',
            component: () => import('@/views/tutor/Students.vue'),
            meta: { title: '指导学生管理' }
          }
        ]
      },

      // ========== 管理员路由 ==========
      {
        path: 'admin',
        name: 'Admin',
        meta: {
          title: '系统管理',
          icon: 'Setting',
          role: 'admin'  // 仅限管理员角色访问
        },
        children: [
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('@/views/admin/Users.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'students',
            name: 'AdminStudents',
            component: () => import('@/views/admin/Students.vue'),
            meta: { title: '研究生信息管理' }
          },
          {
            path: 'rules',
            name: 'AdminRules',
            component: () => import('@/views/admin/Rules.vue'),
            meta: { title: '评分规则管理' }
          },
          {
            path: 'evaluation',
            name: 'AdminEvaluation',
            component: () => import('@/views/admin/Evaluation.vue'),
            meta: { title: '评定管理' }
          },
          {
            path: 'results',
            name: 'AdminResults',
            component: () => import('@/views/admin/Results.vue'),
            meta: { title: '结果管理' }
          },
          {
            path: 'system',
            name: 'AdminSystem',
            component: () => import('@/views/admin/System.vue'),
            meta: { title: '系统设置' }
          }
        ]
      }
    ]
  },

  // ========== 404 页面 ==========
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  // 使用 HTML5 History 模式
  history: createWebHistory(),
  routes
})

/**
 * 全局前置守卫
 * 在路由跳转前进行权限验证
 */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 研究生学业奖学金评定系统`
  } else {
    document.title = '研究生学业奖学金评定系统'
  }

  // 检查是否已登录（同时验证 token 和用户信息）
  const isLoggedIn = checkAuth()

  // 判断是否需要认证
  if (to.meta?.requiresAuth) {
    if (!isLoggedIn) {
      // 未登录，清除残留数据并跳转到登录页
      clearAuth()
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 检查路由是否需要特定角色
    if (to.meta?.role) {
      const userRole = getUserRole()
      if (!userRole || userRole !== to.meta.role) {
        // 无权限，重定向到首页
        ElMessage?.error('无权访问该页面')
        next({ name: 'Dashboard' })
        return
      }
    }

    // 已登录且有权限，允许访问
    next()
  } else {
    // 不需要认证，直接放行
    // 如果已登录且访问登录页，重定向到首页
    if (to.name === 'Login' && isLoggedIn) {
      next({ name: 'Dashboard' })
      return
    }
    next()
  }
})

/**
 * 全局后置守卫
 * 在路由跳转后执行
 */
router.afterEach((to, from) => {
  // 滚动到页面顶部
  window.scrollTo(0, 0)
})

export default router
