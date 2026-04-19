-- ========================================
-- Flyway 数据库版本管理 - V1.4 修复缺失字段
-- ========================================
-- 修复问题:
-- 1. research_patent 表缺少 authorization_date 字段
-- 2. research_project 表缺少 leader_name 字段
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. research_patent 表添加 authorization_date 字段
-- ========================================

ALTER TABLE `research_patent`
ADD COLUMN `authorization_date` DATE DEFAULT NULL COMMENT '授权日期' AFTER `application_date`;

-- ========================================
-- 2. research_project 表添加 leader_name 字段
-- ========================================

ALTER TABLE `research_project`
ADD COLUMN `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名' AFTER `leader_id`;

-- 数据迁移：将原来的 leader 字段数据复制到 leader_name
UPDATE `research_project` SET `leader_name` = `leader` WHERE `leader_name` IS NULL AND `leader` IS NOT NULL;

COMMIT;
