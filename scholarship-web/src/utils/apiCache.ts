/**
 * API 请求级缓存工具
 * 基于 sessionStorage 缓存慢变化数据（如院系列表、学年选项），减少重复 HTTP 请求
 */

interface CacheEntry<T> {
  data: T
  expiresAt: number
}

const CACHE_PREFIX = 'api_cache_'

/**
 * 从 sessionStorage 读取缓存条目
 */
function readCache<T>(key: string): T | null {
  try {
    const raw = sessionStorage.getItem(CACHE_PREFIX + key)
    if (!raw) return null
    const entry = JSON.parse(raw) as CacheEntry<T>
    if (Date.now() >= entry.expiresAt) {
      sessionStorage.removeItem(CACHE_PREFIX + key)
      return null
    }
    return entry.data
  } catch {
    return null
  }
}

/**
 * 写入缓存条目
 */
function writeCache<T>(key: string, data: T, ttlMs: number): void {
  try {
    const entry: CacheEntry<T> = {
      data,
      expiresAt: Date.now() + ttlMs
    }
    sessionStorage.setItem(CACHE_PREFIX + key, JSON.stringify(entry))
  } catch {
    // sessionStorage 满或不可用时静默失败
  }
}

/**
 * 清除指定 key 的缓存
 */
export function clearApiCache(key: string): void {
  try {
    sessionStorage.removeItem(CACHE_PREFIX + key)
  } catch {
    // 静默失败
  }
}

/**
 * 创建带缓存的请求函数
 * @param fn  实际 API 请求函数
 * @param key 缓存键（全局唯一）
 * @param ttlMs 缓存有效期（毫秒）
 * @returns 包装后的请求函数（返回类型与原函数一致）
 */
export function cachedRequest<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  key: string,
  ttlMs: number
): T {
  return (async (...args: any[]) => {
    const cached = readCache<Awaited<ReturnType<T>>>(key)
    if (cached !== null) {
      return cached
    }

    const result = await fn(...args)
    // 仅缓存成功的响应（非 null/undefined）
    if (result !== null && result !== undefined) {
      writeCache(key, result, ttlMs)
    }
    return result
  }) as T
}

export default { cachedRequest, clearApiCache }
