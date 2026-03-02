-- 备份原有数据
CREATE TABLE IF NOT EXISTS sys_user_backup_20260222 AS SELECT * FROM sys_user WHERE deleted = 0;

-- 更新所有用户密码为 a123456789 (BCrypt 加密)
UPDATE sys_user SET password = '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u' WHERE deleted = 0;

-- 验证更新结果
SELECT id, username, real_name, user_type, status, SUBSTRING(password, 1, 15) AS password_prefix
FROM sys_user
WHERE deleted = 0
ORDER BY id;
