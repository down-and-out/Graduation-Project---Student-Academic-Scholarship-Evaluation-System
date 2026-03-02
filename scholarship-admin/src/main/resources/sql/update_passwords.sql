-- ========================================
-- 密码更新 SQL 脚本
-- ========================================
-- 说明：将所有用户的密码统一更新为符合新规则的密码
-- 新密码规则:
-- 1. 必须包含数字和字母
-- 2. 长度至少 10 个字符
--
-- 更新后默认密码：a123456789
-- BCrypt 加密后：$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u
-- ========================================

-- 建议：先备份原有密码数据
-- CREATE TABLE sys_user_backup_20260222 AS SELECT * FROM sys_user;

-- 查看当前用户列表
SELECT id, username, real_name, user_type, status
FROM sys_user
WHERE deleted = 0
ORDER BY id;

-- 更新所有用户的密码为 a123456789 (BCrypt 加密后)
-- 原始密码：a123456789
-- BCrypt 哈希：$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u
UPDATE sys_user
SET password = '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u'
WHERE deleted = 0;

-- 验证更新结果
SELECT id, username, real_name, user_type,
       SUBSTRING(password, 1, 15) AS password_prefix,
       status
FROM sys_user
WHERE deleted = 0
ORDER BY id;

-- ========================================
-- 密码验证说明
-- ========================================
-- BCrypt 加密后的密码格式：$2a$10$[22 位盐值][31 位哈希]
-- 总长度：60 个字符
--
-- 新密码规则验证：
-- 1. 长度至少 10 个字符 ✓ (a123456789 = 10 位)
-- 2. 包含字母 ✓ (a)
-- 3. 包含数字 ✓ (123456789)
--
-- 默认密码：a123456789
-- 所有用户统一使用此密码登录
-- ========================================
