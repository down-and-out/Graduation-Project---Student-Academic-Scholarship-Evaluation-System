import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi, logout as logoutApi } from '@/api/auth'
import type { LoginData, LoginParams } from '@/api/auth'
import { tokenStore, userInfoStore } from '@/utils/secureStorage'
import type { UserInfo } from '@/utils/secureStorage'

export const useUserStore = defineStore('user', () => {
  const token = ref(tokenStore.get())
  const userInfo = ref<UserInfo>(userInfoStore.get() || {
    userId: 0,
    username: '',
    realName: '',
    userType: 1
  })

  const isLoggedIn = computed(() => !!token.value)

  const userRole = computed((): App.UserRole => {
    const roleMap: Record<number, App.UserRole> = {
      1: 'student',
      2: 'tutor',
      3: 'admin'
    }
    return roleMap[userInfo.value?.userType] || 'student'
  })

  async function login(loginForm: LoginParams): Promise<{ success: boolean; error?: string }> {
    try {
      const res = await loginApi(loginForm)
      const raw = res as unknown as {
        data?: LoginData | { data?: LoginData }
      } & Partial<LoginData>

      const loginData = raw.data && 'data' in raw.data
        ? raw.data.data as LoginData
        : (raw.data as LoginData | undefined) || (raw as LoginData)

      token.value = loginData.token
      tokenStore.set(loginData.token)

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

  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      clearUserState()
    }
  }

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

  function setToken(newToken: string): void {
    token.value = newToken
    tokenStore.set(newToken)
  }

  async function checkAndUpdateUserInfo(): Promise<boolean> {
    if (!token.value) {
      return false
    }

    const result = await getUserInfo()
    return result.success
  }

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
    token,
    userInfo,
    isLoggedIn,
    userRole,
    login,
    logout,
    getUserInfo,
    setToken,
    checkAndUpdateUserInfo,
    clearUserState
  }
})

export default useUserStore
