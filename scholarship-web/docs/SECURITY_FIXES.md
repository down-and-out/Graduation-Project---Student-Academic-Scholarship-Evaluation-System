# 安全性修复报告

## 修复日期
2026-02-22

## 修复概述
对 scholarship-web 前端项目进行了全面的安全性加固，修复了 4 个主要安全问题。

---

## 修复详情

### 1. RSA 公钥硬编码问题 ✅

**问题描述**: RSA 公钥直接硬编码在 `src/utils/rsa.js` 文件中，存在安全风险。

**修复方案**:
- 创建 `.env` 和 `.env.example` 文件，将公钥移至环境变量 `VITE_RSA_PUBLIC_KEY`
- 更新 `rsa.js` 从环境变量读取公钥
- 添加公钥格式处理（支持 `\n` 换行符）
- 添加公钥缺失时的警告提示

**修复文件**:
- `src/utils/rsa.js` - 修改
- `.env` - 新建
- `.env.example` - 新建
- `.gitignore` - 新建（确保 .env 不被提交）

---

### 2. Token 存储不安全问题 ✅

**问题描述**: 使用 localStorage 存储 JWT Token，易受 XSS 攻击。

**修复方案**:
- 创建 `src/utils/secureStorage.js` 安全存储模块
- Token 使用内存存储（memoryStore），页面刷新后自动清除
- 用户信息加密后存储到 sessionStorage，关闭标签页后自动清除
- 使用简单的异或加密 + Base64 编码防止明文存储
- 更新 `stores/user.js` 使用新的安全存储
- 更新 `router/index.js` 从 sessionStorage 读取用户信息
- 更新 `request.js` 从 tokenStore 获取 Token

**修复文件**:
- `src/utils/secureStorage.js` - 新建
- `src/stores/user.js` - 修改
- `src/router/index.js` - 修改
- `src/utils/request.js` - 修改

**安全改进效果**:
- Token 不再持久化存储，即使有 XSS 也无法持久窃取
- 用户信息加密存储，增加数据窃取难度
- 会话生命周期限制在浏览器标签页打开期间

---

### 3. 登录页面测试账号提示 ✅

**问题描述**: 登录页面显示弱密码测试账号 `admin / 123456`，不符合安全最佳实践。

**修复方案**:
- 移除测试账号提示
- 替换为安全提示语："请使用学校统一账号密码登录"
- 添加密码强度验证规则（需包含大小写字母和数字）

**修复文件**:
- `src/views/Login.vue` - 修改

---

### 4. 缺少安全头配置 ✅

**问题描述**: HTML 头部缺少 CSP、X-Frame-Options 等安全相关的 meta 标签。

**修复方案**:
- 在 `index.html` 添加以下安全 meta 标签：
  - X-Content-Type-Options: nosniff
  - X-Frame-Options: SAMEORIGIN
  - X-XSS-Protection: 1; mode=block
  - Referrer-Policy: strict-origin-when-cross-origin
  - Content-Security-Policy: 限制资源加载
  - Permissions-Policy: 禁用 interest-cohort
- 在 `vite.config.js` 添加开发服务器响应头
- 将 minify 从 terser 改为 esbuild（性能优化）

**修复文件**:
- `index.html` - 修改
- `vite.config.js` - 修改

---

## 构建验证

```bash
npm run build
```

**结果**: ✅ 构建成功，无错误

```
✓ built in 7.62s
dist/index.html                                     1.93 kB │ gzip:   1.23 kB
[... 所有组件构建成功 ...]
```

---

## 剩余建议

### 短期建议
1. **生产环境部署**: 确保生产环境的 Web 服务器（Nginx/Apache）配置更严格的 CSP 响应头
2. **后端配合**: Token 失效后，确保后端将 Token 加入黑名单
3. **HTTPS**: 生产环境必须使用 HTTPS

### 长期建议
1. **使用 httpOnly Cookie**: 与后端配合，使用 httpOnly Cookie 存储 Token
2. **实施 SRI**: 为第三方 CDN 资源添加完整性校验
3. **定期依赖审计**: 运行 `npm audit` 检查依赖漏洞

---

## 文件清单

### 新建文件
- `.env` - 环境变量配置
- `.env.example` - 环境变量示例
- `.gitignore` - Git 忽略配置
- `src/utils/secureStorage.js` - 安全存储工具

### 修改文件
- `src/utils/rsa.js` - RSA 加密工具
- `src/stores/user.js` - 用户状态管理
- `src/router/index.js` - 路由配置
- `src/utils/request.js` - HTTP 请求封装
- `src/views/Login.vue` - 登录页面
- `index.html` - HTML 入口
- `vite.config.js` - Vite 配置

---

## 测试建议

1. **登录功能测试**: 验证登录流程是否正常
2. **刷新测试**: 验证页面刷新后是否需要重新登录（预期行为）
3. **跨标签页测试**: 验证多个标签页是否共享登录状态
4. **关闭测试**: 验证关闭浏览器后是否需要重新登录
