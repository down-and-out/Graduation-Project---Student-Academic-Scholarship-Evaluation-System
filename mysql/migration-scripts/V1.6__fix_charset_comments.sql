-- ========================================
-- Flyway 数据库版本管理 - V1.6 修复字符集乱码注释
-- ========================================
-- 修复问题:
-- V1.3 迁移脚本执行时字符集不正确，导致中文注释乱码
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. 修复表注释
-- ========================================

ALTER TABLE `course_score` COMMENT '课程成绩表';
ALTER TABLE `moral_performance` COMMENT '德育表现表';

-- ========================================
-- 2. application_achievement 表列注释修复
-- ========================================

ALTER TABLE `application_achievement` MODIFY COLUMN `score_comment` VARCHAR(500) DEFAULT NULL COMMENT '评分说明';

-- ========================================
-- 3. competition_award 表列注释修复
-- ========================================

ALTER TABLE `competition_award` MODIFY COLUMN `competition_level` TINYINT DEFAULT '1' COMMENT '竞赛级别：1-国家级 2-省级 3-校级';
ALTER TABLE `competition_award` MODIFY COLUMN `award_type` TINYINT DEFAULT '1' COMMENT '团队/个人：1-个人 2-团队';
ALTER TABLE `competition_award` MODIFY COLUMN `member_rank` INT DEFAULT NULL COMMENT '团队成员排名';
ALTER TABLE `competition_award` MODIFY COLUMN `instructor` VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '指导老师';
ALTER TABLE `competition_award` MODIFY COLUMN `issuing_unit` VARCHAR(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '颁发单位';
ALTER TABLE `competition_award` MODIFY COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数';
ALTER TABLE `competition_award` MODIFY COLUMN `audit_status` TINYINT DEFAULT '0' COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回';
ALTER TABLE `competition_award` MODIFY COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID';
ALTER TABLE `competition_award` MODIFY COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE `competition_award` MODIFY COLUMN `audit_comment` VARCHAR(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见';
ALTER TABLE `competition_award` MODIFY COLUMN `proof_materials` VARCHAR(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证明材料路径';
ALTER TABLE `competition_award` MODIFY COLUMN `version` INT DEFAULT NULL COMMENT '乐观锁版本号';

-- ========================================
-- 4. course_score 表列注释修复
-- ========================================

ALTER TABLE `course_score` MODIFY COLUMN `student_id` BIGINT DEFAULT NULL COMMENT '学生ID';
ALTER TABLE `course_score` MODIFY COLUMN `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号';
ALTER TABLE `course_score` MODIFY COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名';
ALTER TABLE `course_score` MODIFY COLUMN `course_id` BIGINT DEFAULT NULL COMMENT '课程ID';
ALTER TABLE `course_score` MODIFY COLUMN `course_name` VARCHAR(200) DEFAULT NULL COMMENT '课程名称';
ALTER TABLE `course_score` MODIFY COLUMN `course_code` VARCHAR(50) DEFAULT NULL COMMENT '课程代码';
ALTER TABLE `course_score` MODIFY COLUMN `course_type` TINYINT DEFAULT NULL COMMENT '课程类型：1-必修 2-选修 3-实践';
ALTER TABLE `course_score` MODIFY COLUMN `credit` DECIMAL(4,1) DEFAULT NULL COMMENT '学分';
ALTER TABLE `course_score` MODIFY COLUMN `score` DECIMAL(5,2) DEFAULT NULL COMMENT '成绩';
ALTER TABLE `course_score` MODIFY COLUMN `gpa` DECIMAL(3,1) DEFAULT NULL COMMENT '绩点';
ALTER TABLE `course_score` MODIFY COLUMN `academic_year` VARCHAR(20) DEFAULT NULL COMMENT '学年';
ALTER TABLE `course_score` MODIFY COLUMN `semester` TINYINT DEFAULT NULL COMMENT '学期：1-第一学期 2-第二学期';
ALTER TABLE `course_score` MODIFY COLUMN `exam_date` DATE DEFAULT NULL COMMENT '考试日期';
ALTER TABLE `course_score` MODIFY COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `course_score` MODIFY COLUMN `deleted` TINYINT DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除';
ALTER TABLE `course_score` MODIFY COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `course_score` MODIFY COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 5. evaluation_result 表列注释修复
-- ========================================

ALTER TABLE `evaluation_result` MODIFY COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名';
ALTER TABLE `evaluation_result` MODIFY COLUMN `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号';
ALTER TABLE `evaluation_result` MODIFY COLUMN `department` VARCHAR(100) DEFAULT NULL COMMENT '院系';
ALTER TABLE `evaluation_result` MODIFY COLUMN `major` VARCHAR(100) DEFAULT NULL COMMENT '专业';
ALTER TABLE `evaluation_result` MODIFY COLUMN `research_score` DECIMAL(6,2) DEFAULT 0 COMMENT '科研成果分数';
ALTER TABLE `evaluation_result` MODIFY COLUMN `competition_score` DECIMAL(6,2) DEFAULT 0 COMMENT '竞赛获奖分数';
ALTER TABLE `evaluation_result` MODIFY COLUMN `quality_score` DECIMAL(6,2) DEFAULT 0 COMMENT '综合素质分数';
ALTER TABLE `evaluation_result` MODIFY COLUMN `department_rank` INT DEFAULT NULL COMMENT '院系排名';
ALTER TABLE `evaluation_result` MODIFY COLUMN `major_rank` INT DEFAULT NULL COMMENT '专业排名';
ALTER TABLE `evaluation_result` MODIFY COLUMN `award_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '奖学金金额';
ALTER TABLE `evaluation_result` MODIFY COLUMN `publicity_date` DATETIME DEFAULT NULL COMMENT '公示日期';
ALTER TABLE `evaluation_result` MODIFY COLUMN `confirm_date` DATETIME DEFAULT NULL COMMENT '确定日期';
ALTER TABLE `evaluation_result` MODIFY COLUMN `result_status` TINYINT DEFAULT 1 COMMENT '结果状态：1-公示中 2-已确定 3-有异议';
ALTER TABLE `evaluation_result` MODIFY COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注';

-- ========================================
-- 6. moral_performance 表列注释修复
-- ========================================

ALTER TABLE `moral_performance` MODIFY COLUMN `student_id` BIGINT DEFAULT NULL COMMENT '学生ID';
ALTER TABLE `moral_performance` MODIFY COLUMN `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号';
ALTER TABLE `moral_performance` MODIFY COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名';
ALTER TABLE `moral_performance` MODIFY COLUMN `moral_score` DECIMAL(6,2) DEFAULT NULL COMMENT '道德品质分数';
ALTER TABLE `moral_performance` MODIFY COLUMN `honor_title` VARCHAR(200) DEFAULT NULL COMMENT '荣誉称号';
ALTER TABLE `moral_performance` MODIFY COLUMN `honor_level` TINYINT DEFAULT NULL COMMENT '荣誉级别';
ALTER TABLE `moral_performance` MODIFY COLUMN `honor_date` DATE DEFAULT NULL COMMENT '获奖日期';
ALTER TABLE `moral_performance` MODIFY COLUMN `volunteer_hours` INT DEFAULT NULL COMMENT '志愿时长（小时）';
ALTER TABLE `moral_performance` MODIFY COLUMN `social_work` VARCHAR(500) DEFAULT NULL COMMENT '社会工作';
ALTER TABLE `moral_performance` MODIFY COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数';
ALTER TABLE `moral_performance` MODIFY COLUMN `audit_status` TINYINT DEFAULT '0' COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回';
ALTER TABLE `moral_performance` MODIFY COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID';
ALTER TABLE `moral_performance` MODIFY COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE `moral_performance` MODIFY COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见';
ALTER TABLE `moral_performance` MODIFY COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径';
ALTER TABLE `moral_performance` MODIFY COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `moral_performance` MODIFY COLUMN `deleted` TINYINT DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除';
ALTER TABLE `moral_performance` MODIFY COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `moral_performance` MODIFY COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 7. research_paper 表列注释修复
-- ========================================

ALTER TABLE `research_paper` MODIFY COLUMN `version` INT DEFAULT NULL COMMENT '乐观锁版本号';

-- ========================================
-- 8. research_patent 表列注释修复
-- ========================================

ALTER TABLE `research_patent` MODIFY COLUMN `authorization_date` DATE DEFAULT NULL COMMENT '授权日期';
ALTER TABLE `research_patent` MODIFY COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回';
ALTER TABLE `research_patent` MODIFY COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID';
ALTER TABLE `research_patent` MODIFY COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE `research_patent` MODIFY COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见';
ALTER TABLE `research_patent` MODIFY COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径';
ALTER TABLE `research_patent` MODIFY COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数';
ALTER TABLE `research_patent` MODIFY COLUMN `version` INT DEFAULT NULL COMMENT '乐观锁版本号';

-- ========================================
-- 9. research_project 表列注释修复
-- ========================================

ALTER TABLE `research_project` MODIFY COLUMN `project_source` VARCHAR(200) DEFAULT NULL COMMENT '项目来源';
ALTER TABLE `research_project` MODIFY COLUMN `leader_id` BIGINT DEFAULT NULL COMMENT '项目负责人ID';
ALTER TABLE `research_project` MODIFY COLUMN `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '项目负责人姓名';
ALTER TABLE `research_project` MODIFY COLUMN `member_rank` INT DEFAULT NULL COMMENT '成员排名';
ALTER TABLE `research_project` MODIFY COLUMN `project_status` TINYINT DEFAULT 1 COMMENT '项目状态：1-在研 2-已结题 3-已终止';
ALTER TABLE `research_project` MODIFY COLUMN `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回';
ALTER TABLE `research_project` MODIFY COLUMN `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID';
ALTER TABLE `research_project` MODIFY COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE `research_project` MODIFY COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见';
ALTER TABLE `research_project` MODIFY COLUMN `proof_materials` VARCHAR(500) DEFAULT NULL COMMENT '证明材料路径';
ALTER TABLE `research_project` MODIFY COLUMN `score` DECIMAL(6,2) DEFAULT NULL COMMENT '获得分数';
ALTER TABLE `research_project` MODIFY COLUMN `version` INT DEFAULT NULL COMMENT '乐观锁版本号';

-- ========================================
-- 10. result_appeal 表列注释修复
-- ========================================

ALTER TABLE `result_appeal` MODIFY COLUMN `batch_id` BIGINT DEFAULT NULL COMMENT '批次ID';
ALTER TABLE `result_appeal` MODIFY COLUMN `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名';
ALTER TABLE `result_appeal` MODIFY COLUMN `appeal_type` TINYINT DEFAULT 1 COMMENT '异议类型：1-分数错误 2-材料遗漏 3-计算错误 4-其他';
ALTER TABLE `result_appeal` MODIFY COLUMN `appeal_title` VARCHAR(200) DEFAULT NULL COMMENT '异议标题';
ALTER TABLE `result_appeal` MODIFY COLUMN `attachment_path` VARCHAR(500) DEFAULT NULL COMMENT '附件路径';
ALTER TABLE `result_appeal` MODIFY COLUMN `appeal_status` TINYINT DEFAULT 1 COMMENT '申诉状态：1-待处理 2-处理中 3-已处理 4-已驳回';
ALTER TABLE `result_appeal` MODIFY COLUMN `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名';

-- ========================================
-- 11. review_record 表列注释修复
-- ========================================

ALTER TABLE `review_record` MODIFY COLUMN `review_stage` TINYINT DEFAULT 1 COMMENT '评审阶段：1-导师审核 2-院系审核 3-学校审核';
ALTER TABLE `review_record` MODIFY COLUMN `review_result` TINYINT DEFAULT 1 COMMENT '评审结果：1-通过 2-驳回 3-待定';
ALTER TABLE `review_record` MODIFY COLUMN `review_score` DECIMAL(6,2) DEFAULT NULL COMMENT '评审分数';
ALTER TABLE `review_record` MODIFY COLUMN `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '评审意见';

-- ========================================
-- 12. sys_notification 表列注释修复
-- ========================================

ALTER TABLE `sys_notification` MODIFY COLUMN `business_id` BIGINT DEFAULT NULL COMMENT '关联业务ID';
ALTER TABLE `sys_notification` MODIFY COLUMN `sender_id` BIGINT DEFAULT NULL COMMENT '发送人ID';
ALTER TABLE `sys_notification` MODIFY COLUMN `sender_name` VARCHAR(50) DEFAULT NULL COMMENT '发送人姓名';

-- ========================================
-- 13. sys_operation_log 表列注释修复
-- ========================================

ALTER TABLE `sys_operation_log` MODIFY COLUMN `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `module` VARCHAR(100) DEFAULT NULL COMMENT '操作模块';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `operation_type` TINYINT DEFAULT 1 COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-审核 6-导出';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `description` VARCHAR(200) DEFAULT NULL COMMENT '操作描述';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `request_url` VARCHAR(200) DEFAULT NULL COMMENT '请求URL';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `response_data` TEXT DEFAULT NULL COMMENT '返回结果';
ALTER TABLE `sys_operation_log` MODIFY COLUMN `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）';

-- ========================================
-- 14. sys_user 表列注释修复
-- ========================================

ALTER TABLE `sys_user` MODIFY COLUMN `version` INT DEFAULT NULL COMMENT '乐观锁版本号';

COMMIT;