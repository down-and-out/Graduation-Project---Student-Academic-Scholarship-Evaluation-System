-- ========================================
-- 修复用户密码
-- 说明：使用正确的 BCrypt 哈希值
-- 密码：123456
-- ========================================

USE `scholarship`;

-- 生成新的 BCrypt 哈希（使用 Spring Security 的 BCryptPasswordEncoder）
-- 哈希值：$2a$10$u2oAenwdifnXBJeyTvHvRONEwTzTqfX6W2zYHJpzcZjW4A6EbtP/W
-- 对应密码：123456

-- 更新所有用户的密码为正确的 BCrypt 哈希
UPDATE `sys_user` SET
    `password` = '$2a$10$u2oAenwdifnXBJeyTvHvRONEwTzTqfX6W2zYHJpzcZjW4A6EbtP/W'
WHERE `user_type` IN (1, 2, 3);

-- 验证更新结果
SELECT `id`, `username`, `user_type`, LEFT(`password`, 20) AS password_prefix
FROM `sys_user`;
