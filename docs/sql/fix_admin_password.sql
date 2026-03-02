-- ========================================
-- 修复 admin 用户密码
-- 密码：123456
-- BCrypt 哈希：$2a$10$u2oAenwdifnXBJeyTvHvRONEwTzTqfX6W2zYHJpzcZjW4A6EbtP/W
-- ========================================

USE `scholarship`;

-- 更新 admin 用户的密码
UPDATE `sys_user` SET
    `password` = '$2a$10$u2oAenwdifnXBJeyTvHvRONEwTzTqfX6W2zYHJpzcZjW4A6EbtP/W'
WHERE `username` = 'admin';

-- 验证更新结果
SELECT `id`, `username`, `password` FROM `sys_user` WHERE `username` = 'admin';
