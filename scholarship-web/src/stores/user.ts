/**
 * 用户状态管理
 * 使用 Pinia 管理用户的登录状态和信息
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import type { LoginData, LoginParams, UserInfo as UserInfoAPI } from '@/api/auth'
import { tokenStore, userInfoStore } from '@/utils/secureStorage'
import type { UserInfo } from '@/utils/secureStorage'

/**
 * 用户 Store
 */
export const useUserStore = defineStore('user', () => {
  // ========== 状态 ==========
  // 直接在 ref 初始化时从存储读取，避免执行副作用函数
  const token = ref(tokenStore.get())
  const userInfo = ref<UserInfo>(userInfoStore.get() || {
    userId: 0,
    username: '',
    realName: '',
    userType: 1
  })

  // ========== 计算属性 ==========
  /**
   * 是否已登录
   */
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 用户角色
   */
  const userRole = computed((): App.UserRole => {
    const roleMap: Record<number, App.UserRole> = {
      1: 'student',
      2: 'tutor',
      3: 'admin'
    }
    return roleMap[userInfo.value?.userType] || 'student'
  })

  // ========== 方法 ==========

  /**
   * 用户登录
   * @param loginForm - 登录表单
   * @returns 登录结果
   */
  async function login(loginForm: LoginParams): Promise<{ success: boolean; error?: string }> {
    try {
      const res = await loginApi(loginForm)

      // 后端返回格式：Result.success("登录成功", loginResponse)
      // 所以数据结构是：{ code: 200, message: "...", data: { token, userId, ... } }
      // request.ts 返回的是 response，所以 res.data 是后端响应体
      const raw = res as unknown as {
        data?: LoginData | { data?: LoginData }
      } & Partial<LoginData>
      const loginData = raw.data && 'data' in raw.data
        ? raw.data.data as LoginData
        : (raw.data as LoginData | undefined) || (raw as LoginData)

      // 安全存储 token（内存存储）
      token.value = loginData.token
      tokenStore.set(loginData.token)

      // 安全存储用户信息（加密存储到 sessionStorage）
      userInfo.value = {
        userId: loginData.userId,
        username: loginData.username,
        realName: loginData.realName,
        userType: loginData.userType,
        avatar: loginData.avatar
      }
      userInfoStore.set(userInfo.value)

      return { success: true }
    } catch (error) {
      console.error('登录失败:', error)
      return {
        success: false,
        error: (error as Error).message || '登录失败，请检查网络连接'
      }
    }
  }

  /**
   * 用户登出
   */
  async function logout(): Promise<void> {
    try {
      await logoutApi()
      // 登出成功后清除所有敏感数据
      clearUserState()
    } catch (error) {
      console.error('登出请求失败:', error)
      // 即使登出请求失败，也清除本地状态，避免状态不一致
      clearUserState()
      // 抛出错误让调用者知道登出失败
      throw error
    }
  }

  /**
   * 获取当前用户信息
   * @returns 获取结果
   */
  async function getUserInfo(): Promise<{ success: boolean; error?: string }> {
    try {
      const res = await getCurrentUser()

      userInfo.value = res.data
      userInfoStore.set(userInfo.value)

      return { success: true }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return {
        success: false,
        error: (error as Error).message || '获取用户信息失败'
      }
    }
  }

  /**
   * 更新 token
   * @param newToken - 新 token
   */
  function setToken(newToken: string): void {
    token.value = newToken
    tokenStore.set(newToken)
  }

  /**
   * 检查并刷新用户信息
   * @returns 是否成功
   */
  async function checkAndUpdateUserInfo(): Promise<boolean> {
    if (!token.value) {
      return false
    }

    const result = await getUserInfo()
    return result.success
  }

  /**
   * 清除用户状态（用于会话过期）
   */
  function clearUserState(): void {
    token.value = ''
    userInfo.value = {
      userId: 0,
      username: '',
      realName: '',
      userType: 1
    }
    tokenStore.clear()
    userInfoStore.clear()
  }

  return {
    // 状态
    token,
    userInfo,
    isLoggedIn,
    userRole,
    // 方法
    login,
    logout,
    getUserInfo,
    setToken,
    checkAndUpdateUserInfo,
    clearUserState
  }
})

export default useUserStore
