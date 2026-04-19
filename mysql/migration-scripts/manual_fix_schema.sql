-- =========================================
-- 数据库结构手动修复脚本
-- =========================================
-- 说明：当 Flyway 迁移失败时的手动修复脚本
-- 创建时间：2026-03-08
-- 使用方法：
--   1. 备份数据库：mysqldump -u root -p scholarship > backup_$(date +%Y%m%d_%H%M%S).sql
--   2. 执行此脚本：mysql -u root -p scholarship < mysql/manual_fix_schema.sql
--   3. 验证修复结果：mysql -u root -p scholarship < mysql/verify_schema_fix.sql
-- =========================================

USE scholarship;

-- 设置执行变量
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'ALLOW_INVALID_DATES';

-- =========================================
-- 步骤 1: 修复 review_record 表
-- =========================================

SELECT '=== 修复 review_record 表 ===' as status;

-- 1.1 检查当前字段状态并处理
-- 如果存在 score 字段，重命名为 review_score
SET @rename_score = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'review_record' AND COLUMN_NAME = 'score'
        ) AND NOT EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'review_record' AND COLUMN_NAME = 'review_score'
        ),
        'ALTER TABLE review_record CHANGE COLUMN score review_score DECIMAL(6,2) DEFAULT NULL COMMENT \'评审分数\'',
        'SELECT \'score -> review_score: 无需修改\' as status'
    )
);
PREPARE stmt FROM @rename_score;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 如果既不存在 score 也不存在 review_score，添加 review_score
SET @add_review_score = (
    SELECT IF(
        NOT EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'review_record'
            AND COLUMN_NAME IN ('score', 'review_score')
        ),
        'ALTER TABLE review_record ADD COLUMN review_score DECIMAL(6,2) DEFAULT NULL COMMENT \'评审分数\' AFTER review_result',
        'SELECT \'review_score: 已存在\' as status'
    )
);
PREPARE stmt FROM @add_review_score;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 如果存在 opinion 字段，重命名为 review_comment
SET @rename_opinion = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'review_record' AND COLUMN_NAME = 'opinion'
        ) AND NOT EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'review_record' AND COLUMN_NAME = 'review_comment'
        ),
        'ALTER TABLE review_record CHANGE COLUMN opinion review_comment VARCHAR(500) DEFAULT NULL COMMENT \'评审意见\'',
        'SELECT \'opinion -> review_comment: 无需修改\' as status'
    )
);
PREPARE stmt FROM @rename_opinion;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 如果既不存在 opinion 也不存在 review_comment，添加 review_comment
SET @add_review_comment = (
    SELECT IF(
        NOT EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'review_record'
            AND COLUMN_NAME IN ('opinion', 'review_comment')
        ),
        'ALTER TABLE review_record ADD COLUMN review_comment VARCHAR(500) DEFAULT NULL COMMENT \'评审意见\' AFTER review_score',
        'SELECT \'review_comment: 已存在\' as status'
    )
);
PREPARE stmt FROM @add_review_comment;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.5 添加缺失字段
ALTER TABLE review_record
    ADD COLUMN IF NOT EXISTS review_stage TINYINT NOT NULL DEFAULT 1 COMMENT '评审阶段：1-导师审核 2-院系审核 3-学校审核' AFTER application_id,
    ADD COLUMN IF NOT EXISTS review_result TINYINT NOT NULL DEFAULT 1 COMMENT '评审结果：1-通过 2-驳回 3-待定' AFTER reviewer_name,
    ADD COLUMN IF NOT EXISTS deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除' AFTER review_time;

-- 1.6 删除 version 字段（如果存在）
ALTER TABLE review_record DROP COLUMN IF EXISTS version;

SELECT 'review_record 表修复完成' as status;

-- =========================================
-- 步骤 2: 统一字符集
-- =========================================

SELECT '=== 统一字符集为 utf8mb4_unicode_ci ===' as status;

ALTER TABLE course_score CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE moral_performance CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE evaluation_batch CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE evaluation_result CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE review_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE research_project CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE research_patent CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE competition_award CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE application_achievement CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE result_appeal CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE scholarship_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE score_rule CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE rule_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE research_paper CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_notification CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_operation_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_user_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_role_permission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_permission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE student_info CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SELECT '字符集统一完成' as status;

-- =========================================
-- 步骤 3: 清理 evaluation_batch 冗余字段
-- =========================================

SELECT '=== 清理 evaluation_batch 冗余字段 ===' as status;

ALTER TABLE evaluation_batch DROP COLUMN IF EXISTS start_date;
ALTER TABLE evaluation_batch DROP COLUMN IF EXISTS end_date;

SELECT 'evaluation_batch 冗余字段清理完成' as status;

-- =========================================
-- 步骤 4: 修复 research_project 表
-- =========================================

SELECT '=== 修复 research_project 表 ===' as status;

-- 4.1 如果存在 leader 字段但不存在 leader_name，重命名
SET @rename_leader = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'research_project' AND COLUMN_NAME = 'leader'
        ) AND NOT EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'research_project' AND COLUMN_NAME = 'leader_name'
        ),
        'ALTER TABLE research_project CHANGE COLUMN leader leader_name VARCHAR(50) DEFAULT NULL COMMENT \'项目负责人姓名\'',
        'SELECT \'leader -> leader_name: 无需修改\' as status'
    )
);
PREPARE stmt FROM @rename_leader;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.2 添加缺失字段
ALTER TABLE research_project
    ADD COLUMN IF NOT EXISTS leader_name VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名' AFTER project_no,
    ADD COLUMN IF NOT EXISTS project_source VARCHAR(100) DEFAULT NULL COMMENT '项目来源' AFTER project_type,
    ADD COLUMN IF NOT EXISTS leader_id BIGINT DEFAULT NULL COMMENT '项目负责人ID' AFTER project_source,
    ADD COLUMN IF NOT EXISTS member_rank INT DEFAULT NULL COMMENT '成员排名' AFTER leader_name,
    ADD COLUMN IF NOT EXISTS project_status INT DEFAULT 1 COMMENT '项目状态：1-在研 2-已结题 3-已终止' AFTER end_date,
    ADD COLUMN IF NOT EXISTS audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER score,
    ADD COLUMN IF NOT EXISTS audit_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER audit_status,
    ADD COLUMN IF NOT EXISTS auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID' AFTER audit_comment,
    ADD COLUMN IF NOT EXISTS audit_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER auditor_id,
    ADD COLUMN IF NOT EXISTS proof_materials VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER audit_time,
    ADD COLUMN IF NOT EXISTS deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除' AFTER remark;

-- 4.3 删除 leader 字段（如果仍存在）
ALTER TABLE research_project DROP COLUMN IF EXISTS leader;

SELECT 'research_project 表修复完成' as status;

-- =========================================
-- 步骤 5: 修复 sys_notification 表
-- =========================================

SELECT '=== 修复 sys_notification 表 ===' as status;

-- 5.1 如果存在 publisher_id 但不存在 sender_id，重命名
SET @rename_publisher = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'sys_notification' AND COLUMN_NAME = 'publisher_id'
        ) AND NOT EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'sys_notification' AND COLUMN_NAME = 'sender_id'
        ),
        'ALTER TABLE sys_notification CHANGE COLUMN publisher_id sender_id BIGINT DEFAULT NULL COMMENT \'发送人ID\'',
        'SELECT \'publisher_id -> sender_id: 无需修改\' as status'
    )
);
PREPARE stmt FROM @rename_publisher;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.2 添加缺失字段
ALTER TABLE sys_notification
    ADD COLUMN IF NOT EXISTS sender_id BIGINT DEFAULT NULL COMMENT '发送人ID' AFTER is_read,
    ADD COLUMN IF NOT EXISTS sender_name VARCHAR(50) DEFAULT NULL COMMENT '发送人姓名' AFTER sender_id,
    ADD COLUMN IF NOT EXISTS business_id BIGINT DEFAULT NULL COMMENT '关联业务ID' AFTER sender_name,
    ADD COLUMN IF NOT EXISTS deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除' AFTER sender_name;

-- 5.3 删除 publisher_id 字段（如果仍存在）
ALTER TABLE sys_notification DROP COLUMN IF EXISTS publisher_id;

SELECT 'sys_notification 表修复完成' as status;

-- =========================================
-- 恢复设置
-- =========================================

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 修复完成
-- =========================================

SELECT '========================================' as separator;
SELECT '数据库结构手动修复完成！' as status;
SELECT '请运行 mysql/verify_schema_fix.sql 验证修复结果' as next_step;
SELECT '========================================' as separator;
