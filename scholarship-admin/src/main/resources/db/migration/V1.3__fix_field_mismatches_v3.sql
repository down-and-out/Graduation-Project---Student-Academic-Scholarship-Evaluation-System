-- ========================================
-- Flyway 数据库版本管理 - V1.3 修复字段不匹配问题（最终版）
-- ========================================
-- 根据实际数据库结构，只添加缺失的字段
-- 执行说明：使用 --force 参数执行，忽略已存在列的错误
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. research_patent 表添加缺失字段
-- ========================================

ALTER TABLE `research_patent`
ADD COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER `status`,
ADD COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人 ID' AFTER `audit_status`,
ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `auditor_id`,
ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`,
ADD COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER `audit_comment`,
ADD COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数' AFTER `proof_materials`;

-- ========================================
-- 2. research_project 表添加缺失字段
-- ========================================

ALTER TABLE `research_project`
ADD COLUMN `leader_id` BIGINT DEFAULT NULL COMMENT '项目负责人 ID' AFTER `project_source`,
ADD COLUMN `leader_name_new` VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名' AFTER `leader_id`,
ADD COLUMN `member_rank` INT DEFAULT NULL COMMENT '成员排名' AFTER `leader_name_new`,
ADD COLUMN `project_status` TINYINT DEFAULT 1 COMMENT '项目状态：1-在研 2-已结题 3-已终止' AFTER `end_date`,
ADD COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER `project_status`,
ADD COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人 ID' AFTER `audit_status`,
ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `auditor_id`,
ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`,
ADD COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER `audit_comment`,
ADD COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数' AFTER `proof_materials`;

-- 数据迁移：将 leader 复制到 leader_name_new
UPDATE `research_project` SET `leader_name_new` = `leader` WHERE `leader_name_new` IS NULL AND `leader` IS NOT NULL;

-- ========================================
-- 3. competition_award 表添加缺失字段
-- ========================================

ALTER TABLE `competition_award`
ADD COLUMN `competition_level` TINYINT DEFAULT 1 COMMENT '竞赛级别：1-国家级 2-省级 3-校级' AFTER `competition_name`,
ADD COLUMN `award_rank_new` VARCHAR(50) DEFAULT NULL COMMENT '获奖名次' AFTER `award_level`,
ADD COLUMN `award_type` TINYINT DEFAULT 1 COMMENT '团队/个人：1-个人 2-团队' AFTER `award_rank_new`,
ADD COLUMN `member_rank` INT DEFAULT NULL COMMENT '团队成员排名' AFTER `award_type`,
ADD COLUMN `instructor` VARCHAR(100) DEFAULT NULL COMMENT '指导老师' AFTER `member_rank`,
ADD COLUMN `issuing_unit` VARCHAR(200) DEFAULT NULL COMMENT '颁发单位' AFTER `instructor`,
ADD COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回' AFTER `status`,
ADD COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人 ID' AFTER `audit_status`,
ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `auditor_id`,
ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`,
ADD COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径' AFTER `audit_comment`,
ADD COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数' AFTER `proof_materials`;

-- ========================================
-- 4. evaluation_result 表添加缺失字段
-- ========================================

ALTER TABLE `evaluation_result`
ADD COLUMN `research_score` DECIMAL(6,2) DEFAULT 0 COMMENT '科研成果分数' AFTER `academic_score`,
ADD COLUMN `competition_score` DECIMAL(6,2) DEFAULT 0 COMMENT '竞赛获奖分数' AFTER `research_score`,
ADD COLUMN `quality_score` DECIMAL(6,2) DEFAULT 0 COMMENT '综合素质分数' AFTER `competition_score`,
ADD COLUMN `department_rank` INT DEFAULT NULL COMMENT '院系排名' AFTER `ranking`,
ADD COLUMN `major_rank` INT DEFAULT NULL COMMENT '专业排名' AFTER `department_rank`,
ADD COLUMN `publicity_date` DATETIME DEFAULT NULL COMMENT '公示日期' AFTER `publish_date`,
ADD COLUMN `result_status` TINYINT DEFAULT 1 COMMENT '结果状态：1-公示中 2-已确定 3-有异议' AFTER `publicity_date`,
ADD COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名' AFTER `student_id`,
ADD COLUMN `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号' AFTER `student_name`,
ADD COLUMN `department` VARCHAR(100) DEFAULT NULL COMMENT '院系' AFTER `student_no`,
ADD COLUMN `major` VARCHAR(100) DEFAULT NULL COMMENT '专业' AFTER `department`,
ADD COLUMN `award_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '奖学金金额' AFTER `scholarship_amount`;

-- 数据迁移：将 academic_score 复制到 research_score
UPDATE `evaluation_result` SET `research_score` = `academic_score` WHERE `research_score` = 0 AND `academic_score` IS NOT NULL;

-- 数据迁移：将 publish_date 复制到 publicity_date
UPDATE `evaluation_result` SET `publicity_date` = `publish_date` WHERE `publicity_date` IS NULL AND `publish_date` IS NOT NULL;

-- ========================================
-- 5. result_appeal 表添加缺失字段
-- ========================================

ALTER TABLE `result_appeal`
ADD COLUMN `batch_id` BIGINT DEFAULT NULL COMMENT '批次 ID' AFTER `result_id`,
ADD COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名' AFTER `student_id`,
ADD COLUMN `appeal_type` TINYINT DEFAULT 1 COMMENT '异议类型：1-分数错误 2-材料遗漏 3-计算错误 4-其他' AFTER `student_name`,
ADD COLUMN `appeal_title` VARCHAR(200) DEFAULT NULL COMMENT '异议标题' AFTER `appeal_type`,
ADD COLUMN `attachment_path` VARCHAR(500) DEFAULT NULL COMMENT '附件路径' AFTER `appeal_content`,
ADD COLUMN `appeal_status` TINYINT DEFAULT 1 COMMENT '申诉状态：1-待处理 2-处理中 3-已处理 4-已驳回' AFTER `attachment_path`,
ADD COLUMN `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名' AFTER `handler_id`;

-- 数据迁移：将 attachment_url 复制到 attachment_path
UPDATE `result_appeal` SET `attachment_path` = `attachment_url` WHERE `attachment_path` IS NULL AND `attachment_url` IS NOT NULL;

-- ========================================
-- 6. sys_notification 表添加缺失字段
-- ========================================

ALTER TABLE `sys_notification`
ADD COLUMN `business_id` BIGINT DEFAULT NULL COMMENT '关联业务 ID' AFTER `receiver_type`,
ADD COLUMN `sender_id` BIGINT DEFAULT NULL COMMENT '发送人 ID' AFTER `is_read`,
ADD COLUMN `sender_name` VARCHAR(50) DEFAULT NULL COMMENT '发送人姓名' AFTER `sender_id`;

-- 数据迁移：将 publisher_id 复制到 sender_id
UPDATE `sys_notification` SET `sender_id` = `publisher_id` WHERE `sender_id` IS NULL AND `publisher_id` IS NOT NULL;

-- ========================================
-- 7. sys_operation_log 表添加缺失字段
-- ========================================

ALTER TABLE `sys_operation_log`
ADD COLUMN `operator_id` BIGINT DEFAULT NULL COMMENT '操作人 ID' AFTER `user_id`,
ADD COLUMN `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名' AFTER `operator_id`,
ADD COLUMN `module` VARCHAR(100) DEFAULT NULL COMMENT '操作模块' AFTER `operator_name`,
ADD COLUMN `operation_type` TINYINT DEFAULT 1 COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-审核 6-导出' AFTER `module`,
ADD COLUMN `description` VARCHAR(200) DEFAULT NULL COMMENT '操作描述' AFTER `operation_type`,
ADD COLUMN `request_url` VARCHAR(200) DEFAULT NULL COMMENT '请求 URL' AFTER `ip`,
ADD COLUMN `response_data` TEXT DEFAULT NULL COMMENT '返回结果' AFTER `request_url`,
ADD COLUMN `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）' AFTER `execute_time`;

-- 数据迁移
UPDATE `sys_operation_log` SET `operator_id` = `user_id` WHERE `operator_id` IS NULL AND `user_id` IS NOT NULL;
UPDATE `sys_operation_log` SET `operator_name` = `username` WHERE `operator_name` IS NULL AND `username` IS NOT NULL;
UPDATE `sys_operation_log` SET `execution_time` = `execute_time` WHERE `execution_time` IS NULL AND `execute_time` IS NOT NULL;

-- ========================================
-- 8. review_record 表添加缺失字段
-- ========================================

ALTER TABLE `review_record`
ADD COLUMN `review_stage` TINYINT DEFAULT 1 COMMENT '评审阶段：1-导师审核 2-院系审核 3-学校审核' AFTER `application_id`,
ADD COLUMN `review_result` TINYINT DEFAULT 1 COMMENT '评审结果：1-通过 2-驳回 3-待定' AFTER `review_stage`,
ADD COLUMN `review_score` DECIMAL(6,2) DEFAULT NULL COMMENT '评审分数' AFTER `review_result`,
ADD COLUMN `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '评审意见' AFTER `review_score`;

-- ========================================
-- 9. application_achievement 表添加缺失字段
-- ========================================

ALTER TABLE `application_achievement`
ADD COLUMN `score_comment` VARCHAR(500) DEFAULT NULL COMMENT '评分说明' AFTER `score`;

COMMIT;
