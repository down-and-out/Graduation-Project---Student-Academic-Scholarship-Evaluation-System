# 研究生学业奖学金评定系统 - 测试指南

## 测试环境准备

### 1. 环境要求

| 组件 | 版本要求 | 必需 |
|------|---------|------|
| JDK | 17+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Redis | 7.x | ✅ |
| Node.js | 18+ | ✅ |
| Maven | 3.8+ | ✅ |

### 2. 启动服务

```bash
# 1. 启动 MySQL
# (根据系统自行启动)

# 2. 启动 Redis
redis-server

# 3. 初始化数据库
mysql -u root -p < docs/sql/scholarship.sql
mysql -u root -p < docs/sql/schema_index.sql

# 4. 启动后端
cd scholarship-admin
mvn spring-boot:run

# 5. 启动前端
cd scholarship-web
npm install
npm run dev
```

---

## 后端测试

### 1. 登录限流功能测试

**测试工具**: Postman 或 curl

```bash
# 连续发送 5 次错误密码请求
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong"}'
  echo ""
  echo "第 $i 次请求"
  echo "-------------------"
done
```

**预期结果**:
- 第 1-5 次：返回 `401 用户名或密码错误`
- 第 6 次及之后：返回 `429 登录失败次数过多，请 XX 分钟后再试`

---

### 2. Token 注销（黑名单）功能测试

```bash
# 1. 登录获取 Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | jq -r '.data.token')

echo "获取到的 Token: $TOKEN"

# 2. 使用 Token 访问受保护接口（应成功）
curl -X GET http://localhost:8080/api/auth/current-user \
  -H "Authorization: Bearer $TOKEN"

# 3. 登出（Token 应加入黑名单）
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# 4. 再次使用同一 Token 访问（应失败）
echo ""
echo "登出后再次使用该 Token:"
curl -X GET http://localhost:8080/api/auth/current-user \
  -H "Authorization: Bearer $TOKEN"
```

**预期结果**:
- 步骤 2 返回用户信息（成功）
- 步骤 4 返回 `401 未授权` 或 `登录已失效`

---

### 3. 用户名验证（防 SQL 注入）测试

```bash
# 测试非法用户名（应拒绝）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin; DROP TABLE users;--","password":"123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"123admin","password":"123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ab","password":"123"}'
```

**预期结果**: 返回 `用户名只能包含字母、数字和下划线，且必须以字母开头`

---

### 4. Redis 数据验证

```bash
# 查看登录失败计数
redis-cli
> keys login:*
> get login:attempt:admin:127.0.0.1

# 查看 Token 黑名单
> keys token:blacklist:*
```

---

## 前端测试

### 1. RSA 密码加密测试

**测试步骤**:

1. 打开浏览器开发者工具（F12）
2. 进入 Network 标签页
3. 访问登录页面 `http://localhost:3000`
4. 输入用户名 `admin` 和密码 `123456`
5. 点击登录
6. 查看 `/api/auth/login` 请求的 Payload

**预期结果**:
```json
{
  "username": "admin",
  "password": "MIxx...加密后的长字符串..."
}
```
密码字段应为加密后的密文，而非明文 `123456`

---

### 2. 防止重复提交测试

**测试步骤**:

1. 访问登录页面
2. 点击登录按钮后，快速连续点击多次
3. 观察按钮状态

**预期结果**:
- 首次点击后按钮进入 `loading` 状态
- 按钮 `disabled`，无法再次点击
- 只发送一次登录请求

---

### 3. 角色权限控制测试

**测试步骤**:

1. 使用学生账号（userType=1）登录
2. 在浏览器地址栏手动访问管理员路由：
   - `http://localhost:3000/app/admin/users`
   - `http://localhost:3000/app/admin/rules`

**预期结果**:
- 自动重定向到首页 `/app/dashboard`
- 显示错误提示 `无权访问该页面`

---

### 4. Token 过期自动登出测试

**测试步骤**:

1. 登录系统
2. 打开浏览器 localStorage，手动删除 token
3. 尝试访问需要认证的页面

**预期结果**:
- 自动跳转到登录页
- 显示提示 `未授权，请先登录`

---

## 单元测试（可选）

### 1. 后端单元测试

创建测试类 `src/test/java/com/scholarship/service/LoginAttemptServiceTest.java`:

```java
package com.scholarship.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoginAttemptServiceTest {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Test
    void testRecordFailure() {
        String identifier = "test_user:127.0.0.1";
        loginAttemptService.resetFailures(identifier);

        // 记录 5 次失败
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailure(identifier);
        }

        // 第 6 次应被锁定
        assertTrue(loginAttemptService.isLocked(identifier));
    }
}
```

运行测试:
```bash
mvn test -Dtest=LoginAttemptServiceTest
```

---

## API 接口完整测试清单

### 认证接口

| 接口 | 方法 | 测试状态 | 备注 |
|------|------|---------|------|
| `/auth/login` | POST | □ | 含限流测试 |
| `/auth/logout` | POST | □ | 含 Token 黑名单测试 |
| `/auth/current-user` | GET | □ | 需认证 |

### 业务接口（需认证）

| 接口 | 方法 | 测试状态 |
|------|------|---------|
| `/application/page` | GET | □ |
| `/application/submit` | POST | □ |
| `/application/review/{id}` | PUT | □ |

---

## 测试检查清单

### 后端检查项
- [ ] 登录限流功能正常
- [ ] Token 注销后无法访问
- [ ] 用户名验证阻止非法输入
- [ ] Redis 正常存储计数和黑名单
- [ ] 后端编译无错误

### 前端检查项
- [ ] 密码加密后传输
- [ ] 登录按钮防重复提交
- [ ] 角色权限校验生效
- [ ] Token 过期自动跳转登录
- [ ] 前端代码语法检查通过

### 数据库检查项
- [ ] 所有索引创建成功
- [ ] 逻辑删除字段类型统一

---

## 常见问题排查

### 问题 1: 登录限流不生效

**排查步骤**:
```bash
# 1. 检查 Redis 是否启动
redis-cli ping  # 应返回 PONG

# 2. 检查 Redis 配置
redis-cli keys login:*

# 3. 查看后端日志
tail -f logs/scholarship-dev.log | grep "登录失败"
```

### 问题 2: Token 注销后仍可访问

**排查步骤**:
```bash
# 1. 检查黑名单是否写入
redis-cli keys token:blacklist:*

# 2. 检查 JwtAuthenticationFilter 日志
tail -f logs/scholarship-dev.log | grep "黑名单"
```

### 问题 3: 前端密码未加密

**排查步骤**:
1. 检查 `package.json` 中是否安装 `jsencrypt`
2. 检查 `Login.vue` 中是否导入 `encryptPassword`
3. 浏览器控制台查看是否有 JS 错误

---

## 性能测试（可选）

### 使用 JMeter 测试登录接口

1. 创建线程组：100 用户并发
2. 添加 HTTP 请求：`POST /api/auth/login`
3. 添加断言：响应码 = 200
4. 运行测试，查看聚合报告

**预期**:
- 平均响应时间 < 500ms
- 错误率 < 1%

---

## 测试报告模板

```markdown
## 测试报告

**测试日期**: 2026-02-21
**测试人员**: [姓名]
**环境**: 开发环境

### 测试结果

| 测试项 | 通过/失败 | 备注 |
|--------|----------|------|
| 登录限流 | □ 通过 □ 失败 | |
| Token 注销 | □ 通过 □ 失败 | |
| 用户名验证 | □ 通过 □ 失败 | |
| 密码加密 | □ 通过 □ 失败 | |
| 权限控制 | □ 通过 □ 失败 | |

### 发现问题

1. [问题描述]
2. [问题描述]

### 建议

1. [改进建议]
```
