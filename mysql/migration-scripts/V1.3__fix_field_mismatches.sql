-- ========================================
-- Flyway 数据库版本管理 - V1.3 修复字段不匹配问题
-- ========================================
-- 修复实体类与数据库表字段名称和语义不一致的问题
-- 说明：使用存储过程检查字段是否存在，避免重复添加
-- ========================================

USE `scholarship`;

-- 设置分隔符，避免与存储过程中的分号冲突
DELIMITER $$

-- 创建存储过程：检查列是否存在，不存在则添加
CREATE PROCEDURE IF NOT EXISTS `add_column_if_not_exists`(
    IN table_name VARCHAR(50),
    IN column_name VARCHAR(50),
    IN column_definition VARCHAR(500),
    IN after_column VARCHAR(50)
)
BEGIN
    DECLARE column_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO column_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'scholarship'
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;

    IF column_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` ADD COLUMN `', column_name, '` ', column_definition);
        IF after_column IS NOT NULL AND after_column != '' THEN
            SET @sql = CONCAT(@sql, ' AFTER `', after_column, '`');
        END IF;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

-- 恢复分隔符
DELIMITER ;

-- ========================================
-- 0. evaluation_batch 表修复
-- ========================================

CALL add_column_if_not_exists('evaluation_batch', 'batch_code', 'VARCHAR(50) DEFAULT NULL COMMENT ''批次编码''', 'batch_name');
CALL add_column_if_not_exists('evaluation_batch', 'academic_year', 'VARCHAR(50) DEFAULT NULL COMMENT ''学年''', 'batch_code');
CALL add_column_if_not_exists('evaluation_batch', 'semester', 'TINYINT DEFAULT NULL COMMENT ''学期：1-第一学期 2-第二学期 3-全年''', 'academic_year');
CALL add_column_if_not_exists('evaluation_batch', 'application_start_date', 'DATE DEFAULT NULL COMMENT ''申请开始日期''', 'semester');
CALL add_column_if_not_exists('evaluation_batch', 'application_end_date', 'DATE DEFAULT NULL COMMENT ''申请结束日期''', 'application_start_date');
CALL add_column_if_not_exists('evaluation_batch', 'review_start_date', 'DATE DEFAULT NULL COMMENT ''评审开始日期''', 'application_end_date');
CALL add_column_if_not_exists('evaluation_batch', 'review_end_date', 'DATE DEFAULT NULL COMMENT ''评审结束日期''', 'review_start_date');
CALL add_column_if_not_exists('evaluation_batch', 'publicity_start_date', 'DATE DEFAULT NULL COMMENT ''公示开始日期''', 'review_end_date');
CALL add_column_if_not_exists('evaluation_batch', 'publicity_end_date', 'DATE DEFAULT NULL COMMENT ''公示结束日期''', 'publicity_start_date');
CALL add_column_if_not_exists('evaluation_batch', 'batch_status', 'TINYINT DEFAULT 1 COMMENT ''批次状态：1-未开始 2-申请中 3-评审中 4-公示中 5-已完成''', 'publicity_end_date');
CALL add_column_if_not_exists('evaluation_batch', 'total_amount', 'DECIMAL(12,2) DEFAULT NULL COMMENT ''奖学金总额''', 'batch_status');
CALL add_column_if_not_exists('evaluation_batch', 'winner_count', 'INT DEFAULT NULL COMMENT ''获奖人数''', 'total_amount');

-- ========================================
-- 1. research_patent 表修复
-- ========================================

CALL add_column_if_not_exists('research_patent', 'applicant_rank', 'INT DEFAULT NULL COMMENT ''申请人排名''', 'patent_type');
CALL add_column_if_not_exists('research_patent', 'authorization_date', 'DATE DEFAULT NULL COMMENT ''授权日期''', 'application_date');
CALL add_column_if_not_exists('research_patent', 'audit_status', 'TINYINT DEFAULT 0 COMMENT ''审核状态：0-待审核 1-审核通过 2-审核驳回''', 'patent_status');
CALL add_column_if_not_exists('research_patent', 'auditor_id', 'BIGINT DEFAULT NULL COMMENT ''审核人 ID''', 'audit_status');
CALL add_column_if_not_exists('research_patent', 'audit_time', 'DATETIME DEFAULT NULL COMMENT ''审核时间''', 'auditor_id');
CALL add_column_if_not_exists('research_patent', 'audit_comment', 'VARCHAR(500) DEFAULT NULL COMMENT ''审核意见''', 'audit_time');
CALL add_column_if_not_exists('research_patent', 'proof_materials', 'VARCHAR(500) DEFAULT NULL COMMENT ''证明材料路径''', 'audit_comment');
CALL add_column_if_not_exists('research_patent', 'score', 'DECIMAL(6,2) DEFAULT NULL COMMENT ''获得分数''', 'proof_materials');

-- ========================================
-- 2. research_project 表修复
-- ========================================

CALL add_column_if_not_exists('research_project', 'project_source', 'VARCHAR(200) DEFAULT NULL COMMENT ''项目来源''', 'project_type');
CALL add_column_if_not_exists('research_project', 'leader_id', 'BIGINT DEFAULT NULL COMMENT ''项目负责人 ID''', 'project_source');
CALL add_column_if_not_exists('research_project', 'leader_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''项目负责人姓名''', 'leader_id');
CALL add_column_if_not_exists('research_project', 'member_rank', 'INT DEFAULT NULL COMMENT ''成员排名''', 'leader_name');
CALL add_column_if_not_exists('research_project', 'project_status', 'TINYINT DEFAULT 1 COMMENT ''项目状态：1-在研 2-已结题 3-已终止''', 'end_date');
CALL add_column_if_not_exists('research_project', 'audit_status', 'TINYINT DEFAULT 0 COMMENT ''审核状态：0-待审核 1-审核通过 2-审核驳回''', 'project_status');
CALL add_column_if_not_exists('research_project', 'auditor_id', 'BIGINT DEFAULT NULL COMMENT ''审核人 ID''', 'audit_status');
CALL add_column_if_not_exists('research_project', 'audit_time', 'DATETIME DEFAULT NULL COMMENT ''审核时间''', 'auditor_id');
CALL add_column_if_not_exists('research_project', 'audit_comment', 'VARCHAR(500) DEFAULT NULL COMMENT ''审核意见''', 'audit_time');
CALL add_column_if_not_exists('research_project', 'proof_materials', 'VARCHAR(500) DEFAULT NULL COMMENT ''证明材料路径''', 'audit_comment');
CALL add_column_if_not_exists('research_project', 'score', 'DECIMAL(6,2) DEFAULT NULL COMMENT ''获得分数''', 'proof_materials');

-- ========================================
-- 3. competition_award 表修复
-- ========================================

CALL add_column_if_not_exists('competition_award', 'competition_level', 'TINYINT DEFAULT 1 COMMENT ''竞赛级别：1-国家级 2-省级 3-校级''', 'competition_name');
CALL add_column_if_not_exists('competition_award', 'award_rank', 'VARCHAR(50) DEFAULT NULL COMMENT ''获奖名次''', 'award_level');
CALL add_column_if_not_exists('competition_award', 'award_type', 'TINYINT DEFAULT 1 COMMENT ''团队/个人：1-个人 2-团队''', 'award_rank');
CALL add_column_if_not_exists('competition_award', 'member_rank', 'INT DEFAULT NULL COMMENT ''团队成员排名''', 'award_type');
CALL add_column_if_not_exists('competition_award', 'instructor', 'VARCHAR(100) DEFAULT NULL COMMENT ''指导老师''', 'member_rank');
CALL add_column_if_not_exists('competition_award', 'issuing_unit', 'VARCHAR(200) DEFAULT NULL COMMENT ''颁发单位''', 'instructor');
CALL add_column_if_not_exists('competition_award', 'audit_status', 'TINYINT DEFAULT 0 COMMENT ''审核状态：0-待审核 1-审核通过 2-审核驳回''', 'score');
CALL add_column_if_not_exists('competition_award', 'auditor_id', 'BIGINT DEFAULT NULL COMMENT ''审核人 ID''', 'audit_status');
CALL add_column_if_not_exists('competition_award', 'audit_time', 'DATETIME DEFAULT NULL COMMENT ''审核时间''', 'auditor_id');
CALL add_column_if_not_exists('competition_award', 'audit_comment', 'VARCHAR(500) DEFAULT NULL COMMENT ''审核意见''', 'audit_time');
CALL add_column_if_not_exists('competition_award', 'proof_materials', 'VARCHAR(500) DEFAULT NULL COMMENT ''证明材料路径''', 'audit_comment');

-- ========================================
-- 4. evaluation_result 表修复
-- ========================================

CALL add_column_if_not_exists('evaluation_result', 'research_score', 'DECIMAL(6,2) DEFAULT 0 COMMENT ''科研成果分数''', 'course_score');
CALL add_column_if_not_exists('evaluation_result', 'competition_score', 'DECIMAL(6,2) DEFAULT 0 COMMENT ''竞赛获奖分数''', 'research_score');
CALL add_column_if_not_exists('evaluation_result', 'quality_score', 'DECIMAL(6,2) DEFAULT 0 COMMENT ''综合素质分数''', 'competition_score');
CALL add_column_if_not_exists('evaluation_result', 'department_rank', 'INT DEFAULT NULL COMMENT ''院系排名''', 'total_score');
CALL add_column_if_not_exists('evaluation_result', 'major_rank', 'INT DEFAULT NULL COMMENT ''专业排名''', 'department_rank');
CALL add_column_if_not_exists('evaluation_result', 'publicity_date', 'DATETIME DEFAULT NULL COMMENT ''公示日期''', 'confirm_date');
CALL add_column_if_not_exists('evaluation_result', 'result_status', 'TINYINT DEFAULT 1 COMMENT ''结果状态：1-公示中 2-已确定 3-有异议''', 'publicity_date');
CALL add_column_if_not_exists('evaluation_result', 'student_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''学生姓名''', 'student_id');
CALL add_column_if_not_exists('evaluation_result', 'student_no', 'VARCHAR(20) DEFAULT NULL COMMENT ''学号''', 'student_name');
CALL add_column_if_not_exists('evaluation_result', 'department', 'VARCHAR(100) DEFAULT NULL COMMENT ''院系''', 'student_no');
CALL add_column_if_not_exists('evaluation_result', 'major', 'VARCHAR(100) DEFAULT NULL COMMENT ''专业''', 'department');
CALL add_column_if_not_exists('evaluation_result', 'award_amount', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''奖学金金额''', 'award_level');

-- ========================================
-- 5. result_appeal 表修复
-- ========================================

CALL add_column_if_not_exists('result_appeal', 'batch_id', 'BIGINT DEFAULT NULL COMMENT ''批次 ID''', 'result_id');
CALL add_column_if_not_exists('result_appeal', 'student_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''学生姓名''', 'student_id');
CALL add_column_if_not_exists('result_appeal', 'appeal_type', 'TINYINT DEFAULT 1 COMMENT ''异议类型：1-分数错误 2-材料遗漏 3-计算错误 4-其他''', 'student_name');
CALL add_column_if_not_exists('result_appeal', 'appeal_title', 'VARCHAR(200) DEFAULT NULL COMMENT ''异议标题''', 'appeal_type');
CALL add_column_if_not_exists('result_appeal', 'attachment_path', 'VARCHAR(500) DEFAULT NULL COMMENT ''附件路径''', 'appeal_content');
CALL add_column_if_not_exists('result_appeal', 'appeal_status', 'TINYINT DEFAULT 1 COMMENT ''申诉状态：1-待处理 2-处理中 3-已处理 4-已驳回''', 'attachment_path');
CALL add_column_if_not_exists('result_appeal', 'handler_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''处理人姓名''', 'handler_id');
CALL add_column_if_not_exists('result_appeal', 'handle_result', 'VARCHAR(500) DEFAULT NULL COMMENT ''处理结果''', 'handler_name');

-- ========================================
-- 6. sys_notification 表修复
-- ========================================

CALL add_column_if_not_exists('sys_notification', 'business_id', 'BIGINT DEFAULT NULL COMMENT ''关联业务 ID''', 'receiver_type');
CALL add_column_if_not_exists('sys_notification', 'sender_id', 'BIGINT DEFAULT NULL COMMENT ''发送人 ID''', 'is_read');
CALL add_column_if_not_exists('sys_notification', 'sender_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''发送人姓名''', 'sender_id');

-- ========================================
-- 7. sys_operation_log 表修复
-- ========================================

CALL add_column_if_not_exists('sys_operation_log', 'operator_id', 'BIGINT DEFAULT NULL COMMENT ''操作人 ID''', 'id');
CALL add_column_if_not_exists('sys_operation_log', 'operator_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''操作人姓名''', 'operator_id');
CALL add_column_if_not_exists('sys_operation_log', 'module', 'VARCHAR(100) DEFAULT NULL COMMENT ''操作模块''', 'operator_name');
CALL add_column_if_not_exists('sys_operation_log', 'operation_type', 'TINYINT DEFAULT 1 COMMENT ''操作类型：1-查询 2-新增 3-修改 4-删除 5-审核 6-导出''', 'module');
CALL add_column_if_not_exists('sys_operation_log', 'description', 'VARCHAR(200) DEFAULT NULL COMMENT ''操作描述''', 'operation_type');
CALL add_column_if_not_exists('sys_operation_log', 'request_url', 'VARCHAR(200) DEFAULT NULL COMMENT ''请求 URL''', 'request_uri');
CALL add_column_if_not_exists('sys_operation_log', 'response_data', 'TEXT DEFAULT NULL COMMENT ''返回结果''', 'request_url');
CALL add_column_if_not_exists('sys_operation_log', 'execution_time', 'BIGINT DEFAULT NULL COMMENT ''执行时长（毫秒）''', 'response_data');
CALL add_column_if_not_exists('sys_operation_log', 'operator_ip', 'VARCHAR(50) DEFAULT NULL COMMENT ''操作 IP''', 'execution_time');
CALL add_column_if_not_exists('sys_operation_log', 'error_msg', 'VARCHAR(500) DEFAULT NULL COMMENT ''错误信息''', 'status');

-- ========================================
-- 8. review_record 表修复
-- ========================================

CALL add_column_if_not_exists('review_record', 'review_stage', 'TINYINT DEFAULT 1 COMMENT ''评审阶段：1-导师审核 2-院系审核 3-学校审核''', 'application_id');
CALL add_column_if_not_exists('review_record', 'review_result', 'TINYINT DEFAULT 1 COMMENT ''评审结果：1-通过 2-驳回 3-待定''', 'review_stage');
CALL add_column_if_not_exists('review_record', 'review_score', 'DECIMAL(6,2) DEFAULT NULL COMMENT ''评审分数''', 'review_result');
CALL add_column_if_not_exists('review_record', 'review_comment', 'VARCHAR(500) DEFAULT NULL COMMENT ''评审意见''', 'review_score');

-- ========================================
-- 9. application_achievement 表修复
-- ========================================

CALL add_column_if_not_exists('application_achievement', 'score_comment', 'VARCHAR(500) DEFAULT NULL COMMENT ''评分说明''', 'score');

-- ========================================
-- 10. 删除临时存储过程
-- ========================================

DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;

-- ========================================
-- 11. 数据迁移（将旧字段数据复制到新字段）
-- ========================================

-- research_patent: 将 inventor_rank 复制到 applicant_rank
UPDATE `research_patent` SET `applicant_rank` = `inventor_rank` WHERE `applicant_rank` IS NULL AND `inventor_rank` IS NOT NULL;

-- research_patent: 将 grant_date 复制到 authorization_date
UPDATE `research_patent` SET `authorization_date` = `grant_date` WHERE `authorization_date` IS NULL AND `grant_date` IS NOT NULL;

-- research_project: 将 leader 复制到 leader_name
UPDATE `research_project` SET `leader_name` = `leader` WHERE `leader_name` IS NULL AND `leader` IS NOT NULL;

-- evaluation_result: 将 academic_score 复制到 research_score
UPDATE `evaluation_result` SET `research_score` = `academic_score` WHERE `research_score` = 0 AND `academic_score` IS NOT NULL;

-- evaluation_result: 将 publish_date 复制到 publicity_date（如果存在）
UPDATE `evaluation_result` SET `publicity_date` = `publish_date` WHERE `publicity_date` IS NULL AND EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'scholarship' AND TABLE_NAME = 'evaluation_result' AND COLUMN_NAME = 'publish_date');

-- result_appeal: 将 attachment_url 复制到 attachment_path
UPDATE `result_appeal` SET `attachment_path` = `attachment_url` WHERE `attachment_path` IS NULL AND `attachment_url` IS NOT NULL;

-- sys_notification: 将 publisher_id 复制到 sender_id
UPDATE `sys_notification` SET `sender_id` = `publisher_id` WHERE `sender_id` IS NULL AND `publisher_id` IS NOT NULL;

-- sys_operation_log: 将 user_id 复制到 operator_id
UPDATE `sys_operation_log` SET `operator_id` = `user_id` WHERE `operator_id` IS NULL AND `user_id` IS NOT NULL;

-- sys_operation_log: 将 username 复制到 operator_name
UPDATE `sys_operation_log` SET `operator_name` = `username` WHERE `operator_name` IS NULL AND `username` IS NOT NULL;

-- sys_operation_log: 将 ip 复制到 operator_ip
UPDATE `sys_operation_log` SET `operator_ip` = `ip` WHERE `operator_ip` IS NULL AND `ip` IS NOT NULL;

-- sys_operation_log: 将 execute_time 复制到 execution_time
UPDATE `sys_operation_log` SET `execution_time` = `execute_time` WHERE `execution_time` IS NULL AND `execute_time` IS NOT NULL;

-- sys_operation_log: 将 method 复制到 request_method
UPDATE `sys_operation_log` SET `request_method` = `method` WHERE `request_method` IS NULL AND `method` IS NOT NULL;

-- sys_operation_log: 将 params 复制到 request_params
UPDATE `sys_operation_log` SET `request_params` = `params` WHERE `request_params` IS NULL AND `params` IS NOT NULL;

-- sys_operation_log: 将 operation 复制到 description
UPDATE `sys_operation_log` SET `description` = `operation` WHERE `description` IS NULL AND `operation` IS NOT NULL;

COMMIT;
