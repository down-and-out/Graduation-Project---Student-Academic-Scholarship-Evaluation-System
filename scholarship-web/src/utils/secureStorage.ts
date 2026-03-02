/**
 * 安全存储工具模块
 * 提供比 localStorage 更安全的存储方案
 *
 * 安全说明：
 * - 本模块的"加密"仅为数据混淆，不是真正的加密
 * - 目的是防止 XSS 攻击者直接读取明文数据
 * - 无法抵御有意的解密攻击，密钥在前端代码中公开
 * - 如需更强的安全性，应使用 Web Crypto API 或后端加密服务
 */

const STORAGE_PREFIX = 'scholarship_'

/**
 * 用户信息类型
 */
export interface UserInfo {
  userId: number
  username: string
  realName: string
  userType: number
  avatar?: string
}

/**
 * 简单的异或混淆/解混淆（用于防止明文存储）
 * ⚠️ 注意：这不是强加密，仅用于增加数据读取难度
 */
function xorObfuscate(data: string): string {
  const key = import.meta.env.VITE_APP_TITLE || 'scholarship'
  const result = data.split('').map((char, index) => {
    return String.fromCharCode(char.charCodeAt(0) ^ key.charCodeAt(index % key.length))
  }).join('')
  // 使用 encodeURIComponent 处理 UTF-8 字符，然后再 Base64 编码
  return btoa(encodeURIComponent(result).replace(/%([0-9A-F]{2})/g,
    (match, p1) => String.fromCharCode(Number.parseInt('0x' + p1))))
}

function xorDeobfuscate(obfuscated: string): string | null {
  try {
    const key = import.meta.env.VITE_APP_TITLE || 'scholarship'
    // 先 Base64 解码，然后处理 UTF-8 字符
    const decoded = atob(obfuscated)
    const utf8String = decodeURIComponent(decoded.split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''))
    const result = utf8String.split('').map((char, index) => {
      return String.fromCharCode(char.charCodeAt(0) ^ key.charCodeAt(index % key.length))
    }).join('')
    return result
  } catch (error) {
    console.error('解混淆失败:', error)
    return null
  }
}

/**
 * 内存存储接口（用于 Token 缓存）
 */
interface MemoryStore {
  _data: Map<string, any>
  set(key: string, value: any): void
  get(key: string): any
  remove(key: string): void
  clear(): void
}

const memoryStore: MemoryStore = {
  _data: new Map(),

  set(key: string, value: any) {
    this._data.set(key, value)
  },

  get(key: string): any {
    return this._data.get(key)
  },

  remove(key: string) {
    this._data.delete(key)
  },

  clear() {
    this._data.clear()
  }
}

/**
 * Token 存储管理（使用 sessionStorage）
 * 使用 sessionStorage 而非 localStorage，关闭浏览器后自动清除
 * 同时支持内存备份，提高读取效率
 */
const TOKEN_KEY = STORAGE_PREFIX + 'token'

export const tokenStore = {
  set(token: string) {
    try {
      sessionStorage.setItem(TOKEN_KEY, token)
      memoryStore.set('token', token)  // 内存备份
    } catch (error) {
      console.error('存储 Token 失败:', error)
    }
  },

  get(): string {
    // 优先从 sessionStorage 读取
    const token = sessionStorage.getItem(TOKEN_KEY)
    if (token) {
      memoryStore.set('token', token)  // 更新内存备份
      return token
    }
    // 如果 sessionStorage 中没有，尝试从内存读取
    return memoryStore.get('token') || ''
  },

  remove() {
    sessionStorage.removeItem(TOKEN_KEY)
    memoryStore.remove('token')
  },

  clear() {
    sessionStorage.removeItem(TOKEN_KEY)
    memoryStore.clear()
  }
}

/**
 * 用户信息存储管理（混淆后存储到 sessionStorage）
 * 使用 sessionStorage 而非 localStorage，关闭标签页后自动清除
 */
export const userInfoStore = {
  set(userInfo: UserInfo) {
    try {
      const obfuscated = xorObfuscate(JSON.stringify(userInfo))
      sessionStorage.setItem(STORAGE_PREFIX + 'userInfo', obfuscated)
    } catch (error) {
      console.error('存储用户信息失败:', error)
    }
  },

  get(): UserInfo | null {
    try {
      const obfuscated = sessionStorage.getItem(STORAGE_PREFIX + 'userInfo')
      if (!obfuscated) return null

      const deobfuscated = xorDeobfuscate(obfuscated)
      return deobfuscated ? JSON.parse(deobfuscated) as UserInfo : null
    } catch (error) {
      console.error('读取用户信息失败:', error)
      this.remove()
      return null
    }
  },

  remove() {
    sessionStorage.removeItem(STORAGE_PREFIX + 'userInfo')
  },

  clear() {
    // 只清除带前缀的键，避免误删其他应用的数据
    Object.keys(sessionStorage).forEach(key => {
      if (key.startsWith(STORAGE_PREFIX)) {
        sessionStorage.removeItem(key)
      }
    })
  }
}

/**
 * 安全清除所有存储
 */
export function clearAllStorage(): void {
  tokenStore.clear()
  userInfoStore.clear()
  // 清除所有带前缀的 localStorage 项
  Object.keys(localStorage).forEach(key => {
    if (key.startsWith(STORAGE_PREFIX)) {
      localStorage.removeItem(key)
    }
  })
}

export default {
  tokenStore,
  userInfoStore,
  clearAllStorage
}
