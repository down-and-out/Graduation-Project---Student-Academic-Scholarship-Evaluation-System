# 前端加密函数修复说明

## 问题描述
登录成功后，浏览器控制台报错：
```
存储用户信息失败：InvalidCharacterError: Failed to execute 'btoa' on 'Window':
The string to be encoded contains characters outside of the Latin1 range.
```

导致登录成功后无法正常跳转页面。

## 问题原因
`secureStorage.js` 中的 `simpleEncrypt()` 函数使用 `btoa()` 进行 Base64 编码，但 `btoa()` 只能处理 Latin1 字符（ISO-8859-1），无法处理 UTF-8 字符（如中文）。

当用户信息中包含中文字符（如 `VITE_APP_TITLE = "研究生学业奖学金评定系统"`）时，加密后的数据包含非 Latin1 字符，导致 `btoa()` 失败。

## 解决方案

### 修复文件
`scholarship-web/src/utils/secureStorage.js`

### 修改内容

#### 修复前
```javascript
function simpleEncrypt(data) {
  const key = import.meta.env.VITE_APP_TITLE || 'scholarship'
  const result = data.split('').map((char, index) => {
    return String.fromCharCode(char.charCodeAt(0) ^ key.charCodeAt(index % key.length))
  }).join('')
  return btoa(result) // Base64 编码
}
```

#### 修复后
```javascript
function simpleEncrypt(data) {
  const key = import.meta.env.VITE_APP_TITLE || 'scholarship'
  const result = data.split('').map((char, index) => {
    return String.fromCharCode(char.charCodeAt(0) ^ key.charCodeAt(index % key.length))
  }).join('')
  // 使用 encodeURIComponent 处理 UTF-8 字符，然后再 Base64 编码
  return btoa(encodeURIComponent(result).replace(/%([0-9A-F]{2})/g,
    (match, p1) => String.fromCharCode('0x' + p1)))
}

function simpleDecrypt(encrypted) {
  try {
    const key = import.meta.env.VITE_APP_TITLE || 'scholarship'
    // 先 Base64 解码，然后处理 UTF-8 字符
    const decoded = atob(encrypted)
    const utf8String = decodeURIComponent(decoded.split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''))
    const result = utf8String.split('').map((char, index) => {
      return String.fromCharCode(char.charCodeAt(0) ^ key.charCodeAt(index % key.length))
    }).join('')
    return result
  } catch (error) {
    console.error('解密失败:', error)
    return null
  }
}
```

### 修复原理

1. **加密时**:
   - 先使用 `encodeURIComponent()` 将 UTF-8 字符转换为 `%XX` 格式
   - 然后将 `%XX` 转换为对应的字节字符
   - 最后使用 `btoa()` 进行 Base64 编码

2. **解密时**:
   - 先使用 `atob()` 进行 Base64 解码
   - 将字节字符转换回 `%XX` 格式
   - 使用 `decodeURIComponent()` 解码为原始 UTF-8 字符
   - 最后进行异或解密

## 验证步骤

### 1. 重启前端服务
```bash
cd scholarship-web
npm run dev
```

### 2. 测试登录
1. 打开浏览器访问 http://localhost:3001/
2. 输入账号密码登录
3. 检查浏览器控制台是否有错误
4. 验证是否成功跳转到首页

### 3. 检查存储
打开浏览器开发者工具，查看 Application → Session Storage：
```
scholarship_userInfo: [加密后的数据]
```

## 相关文件

### 修改文件
- `scholarship-web/src/utils/secureStorage.js`

### 关联文件
- `scholarship-web/src/stores/user.js` - 用户状态管理
- `scholarship-web/src/views/Login.vue` - 登录页面

## 完成时间
2026-02-22
