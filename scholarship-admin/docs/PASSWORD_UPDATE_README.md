# 密码规则更新说明

## 更新日期
2026-02-22

## 更新概述
将系统密码规则统一修改为：**必须包含字母和数字，长度至少 10 个字符**

---

## 一、新密码规则

### 规则要求
1. **长度要求**: 至少 10 个字符
2. **字符要求**: 必须同时包含字母和数字
3. **允许字符**: 只能包含字母（a-z, A-Z）和数字（0-9）

### 默认密码
- 统一密码：`a123456789`
- BCrypt 哈希：`$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u`

---

## 二、后端修改

### 1. 新增密码验证工具类
**文件**: `src/main/java/com/scholarship/common/util/PasswordValidator.java`

```java
/**
 * 密码验证工具类
 * 规则：
 * - 长度至少 10 个字符
 * - 必须包含数字
 * - 必须包含字母
 * - 只能包含字母和数字
 */
public static void validate(String password) {
    // 验证逻辑实现
}
```

### 2. 更新登录接口
**文件**: `src/main/java/com/scholarship/controller/AuthController.java`

在登录接口中添加了密码格式验证：
```java
// 解密 RSA 加密的密码
String rawPassword = RsaUtils.decryptPassword(request.password());

// 验证密码格式（新增）
PasswordValidator.validate(rawPassword);
```

### 3. 更新默认密码配置
**文件**: `src/main/resources/application-dev.yml`

```yaml
scholarship:
  system:
    default-password: ${DEFAULT_PASSWORD:a123456789}
```

### 4. BCrypt 密码生成器
**文件**: `src/main/java/com/scholarship/BCryptGenerator.java`

用于生成 BCrypt 哈希密码，可直接用于数据库更新。

---

## 三、前端修改

### 1. 更新登录页面密码验证
**文件**: `src/views/Login.vue`

```javascript
password: [
  { required: true, message: '请输入密码', trigger: 'blur' },
  {
    validator: (rule, value, callback) => {
      if (value.length < 10) {
        callback(new Error('密码长度至少为 10 个字符'))
      } else if (!/(?=.*[0-9])(?=.*[a-zA-Z])/.test(value)) {
        callback(new Error('密码必须包含字母和数字'))
      } else if (!/^[a-zA-Z0-9]+$/.test(value)) {
        callback(new Error('密码只能包含字母和数字'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]
```

### 2. 更新工具函数
**文件**: `src/utils/helpers.js`

```javascript
export function isValidPassword(password) {
  if (!password || password.length < 10) return false
  // 必须包含字母和数字
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasDigit = /[0-9]/.test(password)
  // 只能包含字母和数字
  const isValidChars = /^[a-zA-Z0-9]+$/.test(password)
  return hasLetter && hasDigit && isValidChars
}
```

---

## 四、数据库更新

### SQL 更新脚本
**文件**: `src/main/resources/sql/update_passwords.sql`

```sql
-- 更新所有用户的密码为 a123456789 (BCrypt 加密后)
UPDATE sys_user
SET password = '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u'
WHERE deleted = 0;
```

### 执行步骤

1. **连接数据库**
```bash
mysql -u root -p
```

2. **选择数据库**
```sql
USE scholarship;
```

3. **备份原数据（可选但推荐）**
```sql
CREATE TABLE sys_user_backup_20260222 AS SELECT * FROM sys_user;
```

4. **执行密码更新**
```sql
UPDATE sys_user
SET password = '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u'
WHERE deleted = 0;
```

5. **验证更新结果**
```sql
SELECT id, username, real_name, user_type,
       SUBSTRING(password, 1, 15) AS password_prefix,
       status
FROM sys_user
WHERE deleted = 0
ORDER BY id;
```

---

## 五、验证测试

### 1. 测试登录
使用统一密码 `a123456789` 测试登录：
- 管理员账号
- 导师账号
- 学生账号

### 2. 测试密码验证
尝试使用以下密码登录，应被拒绝：
- `123456` (长度不足)
- `1234567890` (只有数字)
- `abcdefghij` (只有字母)
- `abc123` (长度不足)

### 3. 前端验证测试
在登录页面输入不符合规则的密码，应显示验证错误提示。

---

## 六、注意事项

1. **密码长度**: 确保所有用户的密码长度至少为 10 位
2. **密码复杂度**: 密码必须同时包含字母和数字
3. **加密方式**: 密码使用 BCrypt 加密存储，不可逆
4. **前端验证**: 前端已添加密码强度验证，但最终验证在后端完成

---

## 七、相关文件清单

### 后端文件
- `src/main/java/com/scholarship/common/util/PasswordValidator.java` (新增)
- `src/main/java/com/scholarship/common/util/UsernameValidator.java` (已有)
- `src/main/java/com/scholarship/controller/AuthController.java` (修改)
- `src/main/java/com/scholarship/BCryptGenerator.java` (新增)
- `src/main/resources/application-dev.yml` (修改)
- `src/main/resources/sql/update_passwords.sql` (新增)

### 前端文件
- `src/views/Login.vue` (修改)
- `src/utils/helpers.js` (修改)

---

## 八、默认密码信息

| 项目 | 值 |
|------|-----|
| 默认密码 | `a123456789` |
| BCrypt 哈希 | `$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u` |
| 密码长度 | 10 位 |
| 包含字母 | 是 (a) |
| 包含数字 | 是 (123456789) |

---

**更新完成时间**: 2026-02-22
**测试状态**: 待验证
