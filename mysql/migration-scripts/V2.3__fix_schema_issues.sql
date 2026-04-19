-- =========================================
-- Flyway 数据库版本管理 - V2.3 修复数据库结构问题
-- =========================================
-- 说明：修复数据库表结构问题
-- 创建时间：2026-03-08
-- 问题列表：
-- 1. review_record 表字段与实体类对齐
-- 2. 字符集统一为 utf8mb4_unicode_ci
-- 3. evaluation_batch 冗余字段清理
-- 4. research_project 重复字段清理
-- =========================================

USE scholarship;

-- =========================================
-- 步骤 1: 修复 review_record 表字段
-- =========================================

-- 根据 ReviewRecord 实体类，需要的字段：
-- id, application_id, review_stage, reviewer_id, reviewer_name, review_result
-- review_score, review_comment, review_time, deleted, create_time

-- 数据库可能的状态：
-- 状态A: 有 score, opinion (旧字段)
-- 状态B: 有 review_score, review_comment (已重命名)
-- 状态C: 混合状态

-- 处理策略：
-- 1. 如果存在旧字段 score，重命名为 review_score
-- 2. 如果存在旧字段 opinion，重命名为 review_comment
-- 3. 添加缺失的新字段 (review_stage, review_result, deleted)
-- 4. 删除不需要的 version 字段

-- 1. 重命名 score -> review_score (如果存在 score 且不存在 review_score)
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'score'
        ) AND NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'review_score'
        )
        THEN 'ALTER TABLE review_record CHANGE COLUMN score review_score DECIMAL(6,2) DEFAULT NULL COMMENT \'评审分数\''
        ELSE 'SELECT \'score field already handled\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 如果存在 review_score 但不存在 score，则添加 score 的数据类型兼容（跳过）
-- 3. 如果不存在 review_score 且不存在 score，则添加 review_score
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME IN ('score', 'review_score')
        )
        THEN 'ALTER TABLE review_record ADD COLUMN review_score DECIMAL(6,2) DEFAULT NULL COMMENT \'评审分数\' AFTER review_result'
        ELSE 'SELECT \'review_score field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 重命名 opinion -> review_comment (如果存在 opinion 且不存在 review_comment)
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'opinion'
        ) AND NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'review_comment'
        )
        THEN 'ALTER TABLE review_record CHANGE COLUMN opinion review_comment VARCHAR(500) DEFAULT NULL COMMENT \'评审意见\''
        ELSE 'SELECT \'opinion field already handled\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 如果不存在 review_comment 且不存在 opinion，则添加 review_comment
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME IN ('opinion', 'review_comment')
        )
        THEN 'ALTER TABLE review_record ADD COLUMN review_comment VARCHAR(500) DEFAULT NULL COMMENT \'评审意见\' AFTER review_score'
        ELSE 'SELECT \'review_comment field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 添加 review_stage (如果不存在)
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'review_stage'
        )
        THEN 'ALTER TABLE review_record ADD COLUMN review_stage TINYINT NOT NULL DEFAULT 1 COMMENT \'评审阶段：1-导师审核 2-院系审核 3-学校审核\' AFTER application_id'
        ELSE 'SELECT \'review_stage field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 添加 review_result (如果不存在)
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'review_result'
        )
        THEN 'ALTER TABLE review_record ADD COLUMN review_result TINYINT NOT NULL DEFAULT 1 COMMENT \'评审结果：1-通过 2-驳回 3-待定\' AFTER reviewer_name'
        ELSE 'SELECT \'review_result field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 添加 deleted (如果不存在)
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'deleted'
        )
        THEN 'ALTER TABLE review_record ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT \'逻辑删除：0-未删除 1-已删除\' AFTER review_time'
        ELSE 'SELECT \'deleted field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 9. 删除 version 字段（实体类未使用）
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'review_record'
              AND COLUMN_NAME = 'version'
        )
        THEN 'ALTER TABLE review_record DROP COLUMN version'
        ELSE 'SELECT \'version field does not exist\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- 步骤 2: 统一字符集为 utf8mb4_unicode_ci
-- =========================================

-- 修改 course_score 表字符集
ALTER TABLE course_score CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改 moral_performance 表字符集
ALTER TABLE moral_performance CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改其他可能不一致的表
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

-- =========================================
-- 步骤 3: 清理 evaluation_batch 冗余字段
-- =========================================

-- evaluation_batch 实体类使用的字段：application_start_date, application_end_date
-- 数据库中可能多余的字段：start_date, end_date

-- 删除 start_date 字段（与 application_start_date 重复）
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'start_date'
        )
        THEN 'ALTER TABLE evaluation_batch DROP COLUMN start_date'
        ELSE 'SELECT \'start_date field does not exist\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除 end_date 字段（与 application_end_date 重复）
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'end_date'
        )
        THEN 'ALTER TABLE evaluation_batch DROP COLUMN end_date'
        ELSE 'SELECT \'end_date field does not exist\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- 步骤 4: 修复 research_project 表字段
-- =========================================

-- ResearchProject 实体类需要：leader_name, leader_id, project_source, project_status, audit_status等

-- 1. 如果存在 leader 字段但不存在 leader_name，将 leader 重命名为 leader_name
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'leader'
        ) AND NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'leader_name'
        )
        THEN 'ALTER TABLE research_project CHANGE COLUMN leader leader_name VARCHAR(50) DEFAULT NULL COMMENT \'项目负责人姓名\''
        ELSE 'SELECT \'leader field already handled\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 如果不存在 leader_name 且不存在 leader，添加 leader_name
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME IN ('leader', 'leader_name')
        )
        THEN 'ALTER TABLE research_project ADD COLUMN leader_name VARCHAR(50) DEFAULT NULL COMMENT \'项目负责人姓名\' AFTER project_no'
        ELSE 'SELECT \'leader_name field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加其他缺失字段（根据实体类）
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'project_source'
        )
        THEN 'ALTER TABLE research_project ADD COLUMN project_source VARCHAR(100) DEFAULT NULL COMMENT \'项目来源\' AFTER project_type'
        ELSE 'SELECT \'project_source field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'leader_id'
        )
        THEN 'ALTER TABLE research_project ADD COLUMN leader_id BIGINT DEFAULT NULL COMMENT \'项目负责人ID\' AFTER project_source'
        ELSE 'SELECT \'leader_id field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'member_rank'
        )
        THEN 'ALTER TABLE research_project ADD COLUMN member_rank INT DEFAULT NULL COMMENT \'成员排名\' AFTER leader_name'
        ELSE 'SELECT \'member_rank field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'project_status'
        )
        THEN 'ALTER TABLE research_project ADD COLUMN project_status INT DEFAULT 1 COMMENT \'项目状态：1-在研 2-已结题 3-已终止\' AFTER end_date'
        ELSE 'SELECT \'project_status field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'audit_status'
        )
        THEN 'ALTER TABLE research_project ADD COLUMN audit_status TINYINT DEFAULT 0 COMMENT \'审核状态：0-待审核 1-审核通过 2-审核驳回\' AFTER score'
        ELSE 'SELECT \'audit_status field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'research_project'
              AND COLUMN_NAME = 'deleted'
        )
        THEN 'ALTER TABLE research_project ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT \'逻辑删除：0-未删除 1-已删除\' AFTER remark'
        ELSE 'SELECT \'deleted field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- 步骤 5: 修复 sys_notification 表字段
-- =========================================

-- SysNotification 实体类需要：sender_id, sender_name, business_id, deleted

-- 1. 如果存在 publisher_id 但不存在 sender_id，将 publisher_id 重命名为 sender_id
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME = 'publisher_id'
        ) AND NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME = 'sender_id'
        )
        THEN 'ALTER TABLE sys_notification CHANGE COLUMN publisher_id sender_id BIGINT DEFAULT NULL COMMENT \'发送人ID\''
        ELSE 'SELECT \'publisher_id field already handled\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 如果不存在 sender_id 且不存在 publisher_id，添加 sender_id
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME IN ('publisher_id', 'sender_id')
        )
        THEN 'ALTER TABLE sys_notification ADD COLUMN sender_id BIGINT DEFAULT NULL COMMENT \'发送人ID\' AFTER is_read'
        ELSE 'SELECT \'sender_id field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 删除 publisher_id（如果还存在）
SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME = 'publisher_id'
        )
        THEN 'ALTER TABLE sys_notification DROP COLUMN publisher_id'
        ELSE 'SELECT \'publisher_id field does not exist\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 添加 sender_name（如果不存在）
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME = 'sender_name'
        )
        THEN 'ALTER TABLE sys_notification ADD COLUMN sender_name VARCHAR(50) DEFAULT NULL COMMENT \'发送人姓名\' AFTER sender_id'
        ELSE 'SELECT \'sender_name field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 添加 business_id（如果不存在）
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME = 'business_id'
        )
        THEN 'ALTER TABLE sys_notification ADD COLUMN business_id BIGINT DEFAULT NULL COMMENT \'关联业务ID\' AFTER sender_name'
        ELSE 'SELECT \'business_id field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 添加 deleted（如果不存在）
SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'scholarship'
              AND TABLE_NAME = 'sys_notification'
              AND COLUMN_NAME = 'deleted'
        )
        THEN 'ALTER TABLE sys_notification ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT \'逻辑删除：0-未删除 1-已删除\' AFTER sender_name'
        ELSE 'SELECT \'deleted field already exists\' as status'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- 验证修复结果
-- =========================================

-- 验证 1: 检查 review_record 表结构
-- DESCRIBE review_record;
-- 应包含：review_stage, review_result, review_score, review_comment, deleted
-- 不应包含：version

-- 验证 2: 检查字符集统一
-- SELECT TABLE_NAME, TABLE_COLLATION
-- FROM INFORMATION_SCHEMA.TABLES
-- WHERE TABLE_SCHEMA = 'scholarship'
--   AND TABLE_NAME IN ('course_score', 'moral_performance', 'student_info');
-- 所有表应使用 utf8mb4_unicode_ci

-- 验证 3: 检查 evaluation_batch 表结构
-- DESCRIBE evaluation_batch;
-- 应不存在 start_date, end_date 字段

-- 验证 4: 检查 research_project 表结构
-- DESCRIBE research_project;
-- 应包含：leader_name, leader_id, project_source 等

-- 验证 5: 检查 sys_notification 表结构
-- DESCRIBE sys_notification;
-- 应包含：sender_id, sender_name, business_id, deleted
-- 不应包含：publisher_id

-- =========================================
-- 完成
-- =========================================
