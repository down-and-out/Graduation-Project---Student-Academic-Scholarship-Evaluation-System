-- =========================================
-- V2.4 添加 student_info 表联系方式字段
-- =========================================
-- 说明：为 student_info 表添加 phone 和 email 字段
-- 创建时间：2026-04-08
-- 问题：Java 实体类有字段但数据库表缺失
-- =========================================

USE scholarship;

-- 添加 phone 字段（如果不存在）
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'student_info'
              AND COLUMN_NAME = 'phone'
        )
        THEN 'ALTER TABLE student_info ADD COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT "电话" AFTER address'
        ELSE 'SELECT "phone 字段已存在" as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 email 字段（如果不存在）
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'student_info'
              AND COLUMN_NAME = 'email'
        )
        THEN 'ALTER TABLE student_info ADD COLUMN email VARCHAR(100) DEFAULT NULL COMMENT "邮箱" AFTER phone'
        ELSE 'SELECT "email 字段已存在" as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
