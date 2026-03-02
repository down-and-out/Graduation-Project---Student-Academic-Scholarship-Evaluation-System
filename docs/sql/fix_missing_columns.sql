-- ========================================
-- 数据库修复脚本 - 手动执行版本
-- ========================================
-- 用途：修复 research_patent 和 research_project 表缺失字段
-- 执行方式：在 MySQL 客户端中执行
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. 修复 research_patent 表
-- ========================================
-- 添加 authorization_date 字段（如果不存在）

ALTER TABLE `research_patent`
ADD COLUMN `authorization_date` DATE DEFAULT NULL COMMENT '授权日期' AFTER `application_date`;

-- ========================================
-- 2. 修复 research_project 表
-- ========================================
-- 添加 leader_name 字段（如果不存在）

ALTER TABLE `research_project`
ADD COLUMN `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名' AFTER `leader_id`;

-- 数据迁移：将原来的 leader 字段数据复制到 leader_name
UPDATE `research_project` SET `leader_name` = `leader` WHERE `leader_name` IS NULL AND `leader` IS NOT NULL;

COMMIT;

-- ========================================
-- 验证修复结果
-- ========================================
-- 执行以下命令查看表结构是否正确

SELECT 'research_patent 表结构：' AS '';
DESC `research_patent`;

SELECT 'research_project 表结构：' AS '';
DESC `research_project`;
