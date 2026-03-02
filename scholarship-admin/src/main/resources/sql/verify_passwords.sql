-- ========================================
-- 密码验证测试 SQL
-- ========================================
-- 用于验证数据库中的密码是否正确更新
-- ========================================

-- 查看所有用户密码前缀（应均为 $2a$10$WVvpc6tp）
SELECT
    id,
    username,
    real_name,
    user_type,
    status,
    SUBSTRING(password, 1, 15) AS password_prefix,
    LENGTH(password) AS password_length
FROM sys_user
WHERE deleted = 0
ORDER BY id;

-- 统计密码分布
SELECT
    SUBSTRING(password, 1, 15) AS password_prefix,
    COUNT(*) AS user_count
FROM sys_user
WHERE deleted = 0
GROUP BY SUBSTRING(password, 1, 15);

-- 验证结果：
-- 所有用户的密码前缀应为 $2a$10$WVvpc6tp
-- 密码长度应为 60 (BCrypt 标准长度)
-- ========================================
