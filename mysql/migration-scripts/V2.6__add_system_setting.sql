-- ========================================
-- Flyway Migration: V2.6__add_system_setting.sql
-- ========================================
-- 创建时间: 2026-04-07
-- 功能: 添加系统设置表，支持 Key-Value 存储模式和版本控制
-- ========================================

USE `scholarship`;

-- ----------------------------------------
-- 1. 创建系统设置表
-- ----------------------------------------

CREATE TABLE IF NOT EXISTS `sys_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 核心字段
    `setting_key` VARCHAR(100) NOT NULL COMMENT '设置键，唯一标识，如: basic, weight, awards',
    `setting_value` TEXT COMMENT '设置值，JSON格式存储',

    -- 版本控制字段
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，支持多版本管理',
    `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否生效：0-否，1-是',

    -- 辅助字段
    `description` VARCHAR(200) DEFAULT NULL COMMENT '设置项描述',

    -- 时间字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 约束
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key_version` (`setting_key`, `version`),
    KEY `idx_key_active` (`setting_key`, `is_active`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表 - Key-Value存储模式，支持版本控制';

-- ----------------------------------------
-- 2. 初始化默认设置数据
-- ----------------------------------------

-- 2.1 基本设置（使用单行JSON避免解析问题）
INSERT INTO `sys_setting` (`setting_key`, `setting_value`, `version`, `is_active`, `description`) VALUES
('basic', '{"systemName":"研究生学业奖学金评定系统","systemShortName":"奖学金评定系统","currentSemester":"2025-1","adminEmail":"admin@example.com","adminPhone":"010-12345678","announcement":""}', 1, 1, '基本设置 - 系统名称、学期、联系方式等');

-- 2.2 评分权重设置（使用单行JSON）
INSERT INTO `sys_setting` (`setting_key`, `setting_value`, `version`, `is_active`, `description`) VALUES
('weight', '{"courseWeight":40,"researchWeight":35,"comprehensiveWeight":25}', 1, 1, '评分权重设置 - 课程成绩、科研成果、综合素质的权重分配');

-- 2.3 奖项设置（使用单行JSON）
INSERT INTO `sys_setting` (`setting_key`, `setting_value`, `version`, `is_active`, `description`) VALUES
('awards', '{"version":"1.0","name":"2025年春季奖项配置","rules":[{"id":"special","name":"特等奖学金","ratio":5,"amount":15000,"scoreRange":{"min":95,"max":100},"conditions":[],"priority":1},{"id":"first","name":"一等奖学金","ratio":10,"amount":10000,"scoreRange":{"min":90,"max":94.99},"conditions":[],"priority":2},{"id":"second","name":"二等奖学金","ratio":25,"amount":5000,"scoreRange":{"min":80,"max":89.99},"conditions":[],"priority":3},{"id":"third","name":"三等奖学金","ratio":40,"amount":2000,"scoreRange":{"min":70,"max":79.99},"conditions":[],"priority":4}],"allocationStrategy":"scorePriority"}', 1, 1, '奖项设置 - 各等级奖学金的配置规则，支持分数线和附加条件');
