-- ========================================
-- Flyway 数据库版本管理 - V1.5 添加缺失的日期字段
-- ========================================
-- 修复问题:
-- evaluation_batch 表缺少 publicity_start_date 和 publicity_end_date 字段
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. evaluation_batch 表添加 publicity_start_date 字段
-- ========================================

ALTER TABLE `evaluation_batch`
ADD COLUMN `publicity_start_date` DATE DEFAULT NULL COMMENT '公示开始日期' AFTER `review_end_date`;

-- ========================================
-- 2. evaluation_batch 表添加 publicity_end_date 字段
-- ========================================

ALTER TABLE `evaluation_batch`
ADD COLUMN `publicity_end_date` DATE DEFAULT NULL COMMENT '公示结束日期' AFTER `publicity_start_date`;

COMMIT;
