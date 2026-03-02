-- ========================================
-- Flyway 数据库版本管理 - V1.2 添加缺失的列
-- ========================================
-- 修复实体类与数据库表结构不一致的问题
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. research_patent 表添加缺失列
-- ========================================

-- 添加 applicant_rank 列（申请人排名）
ALTER TABLE `research_patent`
ADD COLUMN IF NOT EXISTS `applicant_rank` INT DEFAULT NULL COMMENT '申请人排名'
AFTER `patent_type`;

-- 添加 authorization_date 列（授权日期）
ALTER TABLE `research_patent`
ADD COLUMN IF NOT EXISTS `authorization_date` DATE DEFAULT NULL COMMENT '授权日期'
AFTER `application_date`;

-- ========================================
-- 2. research_project 表添加缺失列
-- ========================================

-- 添加 project_source 列（项目来源）
ALTER TABLE `research_project`
ADD COLUMN IF NOT EXISTS `project_source` VARCHAR(200) DEFAULT NULL COMMENT '项目来源'
AFTER `project_type`;

-- 添加 leader_id 列（项目负责人 ID）
ALTER TABLE `research_project`
ADD COLUMN IF NOT EXISTS `leader_id` BIGINT DEFAULT NULL COMMENT '项目负责人 ID'
AFTER `project_source`;

-- 添加 leader_name 列（项目负责人姓名）
ALTER TABLE `research_project`
ADD COLUMN IF NOT EXISTS `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名'
AFTER `leader_id`;

-- 添加 member_rank 列（成员排名）
ALTER TABLE `research_project`
ADD COLUMN IF NOT EXISTS `member_rank` INT DEFAULT NULL COMMENT '成员排名'
AFTER `leader_name`;

-- 添加 funding 列（项目经费）
ALTER TABLE `research_project`
ADD COLUMN IF NOT EXISTS `funding` DECIMAL(10,2) DEFAULT NULL COMMENT '项目经费（万元）'
AFTER `member_rank`;
