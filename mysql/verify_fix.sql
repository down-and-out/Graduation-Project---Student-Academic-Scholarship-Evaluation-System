-- =========================================
-- 验证 student_info 表字段修复
-- =========================================
-- 创建时间：2026-04-08
-- 说明：验证 phone 和 email 字段是否成功添加
-- =========================================

USE scholarship;

-- 检查 student_info 表结构中的 phone 和 email 字段
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM
    INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_SCHEMA = 'scholarship'
    AND TABLE_NAME = 'student_info'
    AND COLUMN_NAME IN ('phone', 'email', 'address', 'status')
ORDER BY
    ORDINAL_POSITION;

-- 显示完整的表结构（可选）
-- DESCRIBE student_info;
