/**
 * RSA 加密工具类
 * 用于前端密码加密传输
 * 依赖：jsencrypt
 */
import JSEncrypt from 'jsencrypt'

/**
 * 从环境变量获取 RSA 公钥
 */
const PUBLIC_KEY = import.meta.env.VITE_RSA_PUBLIC_KEY

// 验证公钥是否配置
if (!PUBLIC_KEY) {
  console.warn('警告：未配置 RSA 公钥 (VITE_RSA_PUBLIC_KEY)，密码将以明文传输')
}

// 缓存加密器实例
let encryptor: JSEncrypt | null = null

/**
 * 获取加密器实例（单例模式）
 * @returns 加密器实例
 */
function getEncryptor(): JSEncrypt {
  if (!encryptor) {
    encryptor = new JSEncrypt()
    // 处理公钥格式：将 \n 转换为实际换行符
    const formattedKey = PUBLIC_KEY ? PUBLIC_KEY.replace(/\\n/g, '\n') : ''
    encryptor.setPublicKey(formattedKey)
  }
  return encryptor
}

/**
 * RSA 加密配置选项
 */
export interface EncryptOptions {
  throwError?: boolean
}

/**
 * RSA 加密
 * @param data - 要加密的数据
 * @param options - 配置选项
 * @returns 加密后的数据（Base64），失败时返回 null 或原始数据
 */
export function encrypt(data: string, options: EncryptOptions = {}): string | null {
  const { throwError = false } = options

  if (!data) return data

  // 未配置公钥时根据选项处理
  if (!PUBLIC_KEY) {
    const msg = 'RSA 公钥未配置，无法加密数据'
    if (throwError) {
      throw new Error(msg)
    }
    console.warn(msg)
    return data
  }

  try {
    const result = getEncryptor().encrypt(data)
    if (result === false) {
      const msg = 'RSA 加密失败，数据过长或格式错误'
      if (throwError) {
        throw new Error(msg)
      }
      console.warn(msg)
      return data
    }
    return result
  } catch (error) {
    console.error('RSA 加密异常:', error)
    if (throwError) {
      throw error
    }
    return data
  }
}

/**
 * RSA 加密密码
 * @param password - 密码
 * @param throwError - 加密失败时是否抛出异常
 * @returns 加密后的密码（Base64）
 */
export function encryptPassword(password: string, throwError = true): string {
  return encrypt(password, { throwError }) as string
}

/**
 * 检查 RSA 是否已启用
 */
export const isRSAEnabled = !!PUBLIC_KEY

export default {
  encrypt,
  encryptPassword,
  isRSAEnabled
}
