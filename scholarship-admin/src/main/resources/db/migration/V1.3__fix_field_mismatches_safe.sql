-- ========================================
-- Flyway 数据库版本管理 - V1.3 修复字段不匹配问题
-- ========================================
-- 修复实体类与数据库表字段名称和语义不一致的问题
-- 说明：使用 ALTER TABLE IGNORE 语法安全添加列
-- ========================================

USE `scholarship`;

-- ========================================
-- 0. evaluation_batch 表修复
-- ========================================

ALTER TABLE `evaluation_batch`
ADD COLUMN `batch_code` VARCHAR(50) DEFAULT NULL COMMENT '批次编码' AFTER `batch_name`,
ADD COLUMN `academic_year` VARCHAR(50) DEFAULT NULL COMMENT '学年' AFTER `batch_code`,
ADD COLUMN `semester` TINYINT DEFAULT NULL COMMENT '学期：1-第一学期 2-第二学期 3-全年' AFTER `academic_year`,
ADD COLUMN `application_start_date` DATE DEFAULT NULL COMMENT '申请开始日期' AFTER `semester`,
ADD COLUMN `application_end_date` DATE DEFAULT NULL COMMENT '申请结束日期' AFTER `application_start_date`,
ADD COLUMN `review_start_date` DATE DEFAULT NULL COMMENT '评审开始日期' AFTER `application_end_date`,
ADD COLUMN `review_end_date` DATE DEFAULT NULL COMMENT '评审结束日期' AFTER `review_start_date`,
ADD COLUMN `publicity_start_date` DATE DEFAULT NULL COMMENT '公示开始日期' AFTER `review_end_date`,
ADD COLUMN `publicity_end_date` DATE DEFAULT NULL COMMENT '公示结束日期' AFTER `publicity_start_date`,
ADD COLUMN `batch_status` TINYINT DEFAULT 1 COMMENT '批次状态：1-未开始 2-申请中 3-评审中 4-公示中 5-已完成' AFTER `publicity_end_date`,
ADD COLUMN `total_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '奖学金总额' AFTER `batch_status`,
ADD COLUMN `winner_count` INT DEFAULT NULL COMMENT '获奖人数' AFTER `total_amount`;

-- ========================================
-- 1. research_patent 表修复
-- ========================================

ALTER TABLE `research_patent`
ADD COLUMN `applicant_rank` INT DEFAULT NULL COMMENT '申请人排名' AFTER `patent_type`,
ADD COLUMN `authorization_date` DATE DEFAULT NULL COMMENT '授权日期' AFTER `application_date`,
ADD COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER `patent_status`,
ADD COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人 ID' AFTER `audit_status`,
ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `auditor_id`,
ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`,
ADD COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER `audit_comment`,
ADD COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数' AFTER `proof_materials`;

-- ========================================
-- 2. research_project 表修复
-- ========================================

ALTER TABLE `research_project`
ADD COLUMN `project_source` VARCHAR(200) DEFAULT NULL COMMENT '项目来源' AFTER `project_type`,
ADD COLUMN `leader_id` BIGINT DEFAULT NULL COMMENT '项目负责人 ID' AFTER `project_source`,
ADD COLUMN `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名' AFTER `leader_id`,
ADD COLUMN `member_rank` INT DEFAULT NULL COMMENT '成员排名' AFTER `leader_name`,
ADD COLUMN `project_status` TINYINT DEFAULT 1 COMMENT '项目状态：1-在研 2-已结题 3-已终止' AFTER `end_date`,
ADD COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER `project_status`,
ADD COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人 ID' AFTER `audit_status`,
ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `auditor_id`,
ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`,
ADD COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER `audit_comment`,
ADD COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数' AFTER `proof_materials`;

-- ========================================
-- 3. competition_award 表修复
-- ========================================

ALTER TABLE `competition_award`
ADD COLUMN `competition_level` TINYINT DEFAULT 1 COMMENT '竞赛级别：1-国家级 2-省级 3-校级' AFTER `competition_name`,
ADD COLUMN `award_rank` VARCHAR(50) DEFAULT NULL COMMENT '获奖名次' AFTER `award_level`,
ADD COLUMN `award_type` TINYINT DEFAULT 1 COMMENT '团队/个人：1-个人 2-团队' AFTER `award_rank`,
ADD COLUMN `member_rank` INT DEFAULT NULL COMMENT '团队成员排名' AFTER `award_type`,
ADD COLUMN `instructor` VARCHAR(100) DEFAULT NULL COMMENT '指导老师' AFTER `member_rank`,
ADD COLUMN `issuing_unit` VARCHAR(200) DEFAULT NULL COMMENT '颁发单位' AFTER `instructor`,
ADD COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER `score`,
ADD COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人 ID' AFTER `audit_status`,
ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `auditor_id`,
ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`,
ADD COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER `audit_comment`;

-- ========================================
-- 4. evaluation_result 表修复
-- ========================================

ALTER TABLE `evaluation_result`
ADD COLUMN `research_score` DECIMAL(6,2) DEFAULT 0 COMMENT '科研成果分数' AFTER `course_score`,
ADD COLUMN `competition_score` DECIMAL(6,2) DEFAULT 0 COMMENT '竞赛获奖分数' AFTER `research_score`,
ADD COLUMN `quality_score` DECIMAL(6,2) DEFAULT 0 COMMENT '综合素质分数' AFTER `competition_score`,
ADD COLUMN `department_rank` INT DEFAULT NULL COMMENT '院系排名' AFTER `total_score`,
ADD COLUMN `major_rank` INT DEFAULT NULL COMMENT '专业排名' AFTER `department_rank`,
ADD COLUMN `publicity_date` DATETIME DEFAULT NULL COMMENT '公示日期' AFTER `confirm_date`,
ADD COLUMN `result_status` TINYINT DEFAULT 1 COMMENT '结果状态：1-公示中 2-已确定 3-有异议' AFTER `publicity_date`,
ADD COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名' AFTER `student_id`,
ADD COLUMN `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号' AFTER `student_name`,
ADD COLUMN `department` VARCHAR(100) DEFAULT NULL COMMENT '院系' AFTER `student_no`,
ADD COLUMN `major` VARCHAR(100) DEFAULT NULL COMMENT '专业' AFTER `department`,
ADD COLUMN `award_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '奖学金金额' AFTER `award_level`;

-- ========================================
-- 5. result_appeal 表修复
-- ========================================

ALTER TABLE `result_appeal`
ADD COLUMN `batch_id` BIGINT DEFAULT NULL COMMENT '批次 ID' AFTER `result_id`,
ADD COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名' AFTER `student_id`,
ADD COLUMN `appeal_type` TINYINT DEFAULT 1 COMMENT '异议类型：1-分数错误 2-材料遗漏 3-计算错误 4-其他' AFTER `student_name`,
ADD COLUMN `appeal_title` VARCHAR(200) DEFAULT NULL COMMENT '异议标题' AFTER `appeal_type`,
ADD COLUMN `attachment_path` VARCHAR(500) DEFAULT NULL COMMENT '附件路径' AFTER `appeal_content`,
ADD COLUMN `appeal_status` TINYINT DEFAULT 1 COMMENT '申诉状态：1-待处理 2-处理中 3-已处理 4-已驳回' AFTER `attachment_path`,
ADD COLUMN `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名' AFTER `handler_id`,
ADD COLUMN `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果' AFTER `handler_name`;

-- ========================================
-- 6. sys_notification 表修复
-- ========================================

ALTER TABLE `sys_notification`
ADD COLUMN `business_id` BIGINT DEFAULT NULL COMMENT '关联业务 ID' AFTER `receiver_type`,
ADD COLUMN `sender_id` BIGINT DEFAULT NULL COMMENT '发送人 ID' AFTER `is_read`,
ADD COLUMN `sender_name` VARCHAR(50) DEFAULT NULL COMMENT '发送人姓名' AFTER `sender_id`;

-- ========================================
-- 7. sys_operation_log 表修复
-- ========================================

ALTER TABLE `sys_operation_log`
ADD COLUMN `operator_id` BIGINT DEFAULT NULL COMMENT '操作人 ID' AFTER `id`,
ADD COLUMN `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名' AFTER `operator_id`,
ADD COLUMN `module` VARCHAR(100) DEFAULT NULL COMMENT '操作模块' AFTER `operator_name`,
ADD COLUMN `operation_type` TINYINT DEFAULT 1 COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-审核 6-导出' AFTER `module`,
ADD COLUMN `description` VARCHAR(200) DEFAULT NULL COMMENT '操作描述' AFTER `operation_type`,
ADD COLUMN `request_url` VARCHAR(200) DEFAULT NULL COMMENT '请求 URL' AFTER `request_uri`,
ADD COLUMN `response_data` TEXT DEFAULT NULL COMMENT '返回结果' AFTER `request_url`,
ADD COLUMN `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）' AFTER `response_data`,
ADD COLUMN `operator_ip` VARCHAR(50) DEFAULT NULL COMMENT '操作 IP' AFTER `execution_time`,
ADD COLUMN `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息' AFTER `status`;

-- ========================================
-- 8. review_record 表修复
-- ========================================

ALTER TABLE `review_record`
ADD COLUMN `review_stage` TINYINT DEFAULT 1 COMMENT '评审阶段：1-导师审核 2-院系审核 3-学校审核' AFTER `application_id`,
ADD COLUMN `review_result` TINYINT DEFAULT 1 COMMENT '评审结果：1-通过 2-驳回 3-待定' AFTER `review_stage`,
ADD COLUMN `review_score` DECIMAL(6,2) DEFAULT NULL COMMENT '评审分数' AFTER `review_result`,
ADD COLUMN `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '评审意见' AFTER `review_score`;

-- ========================================
-- 9. application_achievement 表修复
-- ========================================

ALTER TABLE `application_achievement`
ADD COLUMN `score_comment` VARCHAR(500) DEFAULT NULL COMMENT '评分说明' AFTER `score`;

-- ========================================
-- 10. 数据迁移（将旧字段数据复制到新字段）
-- ========================================

-- research_patent: 将 inventor_rank 复制到 applicant_rank
UPDATE `research_patent` SET `applicant_rank` = `inventor_rank` WHERE `applicant_rank` IS NULL AND `inventor_rank` IS NOT NULL;

-- research_patent: 将 grant_date 复制到 authorization_date
UPDATE `research_patent` SET `authorization_date` = `grant_date` WHERE `authorization_date` IS NULL AND `grant_date` IS NOT NULL;

-- research_project: 将 leader 复制到 leader_name
UPDATE `research_project` SET `leader_name` = `leader` WHERE `leader_name` IS NULL AND `leader` IS NOT NULL;

-- evaluation_result: 将 academic_score 复制到 research_score
UPDATE `evaluation_result` SET `research_score` = `academic_score` WHERE `research_score` = 0 AND `academic_score` IS NOT NULL;

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
