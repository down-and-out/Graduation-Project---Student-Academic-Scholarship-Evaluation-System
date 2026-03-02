-- ========================================
-- 研究生学业奖学金评定系统 - 索引优化脚本
-- 版本：1.0
-- 说明：为所有表添加必要的索引以优化查询性能
-- 使用方法：在创建表之后执行此脚本
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. 用户与权限模块索引优化
-- ========================================

-- sys_user 表索引
ALTER TABLE `sys_user` ADD INDEX `idx_username_status` (`username`, `status`);
ALTER TABLE `sys_user` ADD INDEX `idx_user_type_status` (`user_type`, `status`);

-- sys_role 表索引
ALTER TABLE `sys_role` ADD INDEX `idx_role_code_status` (`role_code`, `status`);

-- sys_user_role 表索引
ALTER TABLE `sys_user_role` ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE `sys_user_role` ADD INDEX `idx_role_id` (`role_id`);

-- sys_role_permission 表索引
ALTER TABLE `sys_role_permission` ADD INDEX `idx_role_id` (`role_id`);
ALTER TABLE `sys_role_permission` ADD INDEX `idx_permission_id` (`permission_id`);

-- ========================================
-- 2. 研究生信息管理模块索引优化
-- ========================================

-- student_info 表索引
ALTER TABLE `student_info` ADD INDEX `idx_student_no_status` (`student_no`, `status`);
ALTER TABLE `student_info` ADD INDEX `idx_enrollment_year_major` (`enrollment_year`, `major`);
ALTER TABLE `student_info` ADD INDEX `idx_tutor_id_status` (`tutor_id`, `status`);

-- ========================================
-- 3. 科研成果管理模块索引优化
-- ========================================

-- research_paper 表索引
ALTER TABLE `research_paper` ADD INDEX `idx_student_id_status` (`student_id`, `status`);
ALTER TABLE `research_paper` ADD INDEX `idx_journal_level` (`journal_level`);

-- research_patent 表索引
ALTER TABLE `research_patent` ADD INDEX `idx_student_id_status` (`student_id`, `status`);
ALTER TABLE `research_patent` ADD INDEX `idx_patent_type` (`patent_type`);
ALTER TABLE `research_patent` ADD INDEX `idx_application_date` (`application_date`);

-- research_project 表索引
ALTER TABLE `research_project` ADD INDEX `idx_student_id_status` (`student_id`, `status`);
ALTER TABLE `research_project` ADD INDEX `idx_project_level` (`project_level`);

-- competition_award 表索引
ALTER TABLE `competition_award` ADD INDEX `idx_student_id_status` (`student_id`, `status`);
ALTER TABLE `competition_award` ADD INDEX `idx_award_level` (`award_level`);
ALTER TABLE `competition_award` ADD INDEX `idx_competition_date` (`competition_date`);

-- ========================================
-- 4. 评分规则管理模块索引优化
-- ========================================

-- rule_category 表索引
ALTER TABLE `rule_category` ADD INDEX `idx_parent_id` (`parent_id`);
ALTER TABLE `rule_category` ADD INDEX `idx_sort_order` (`sort_order`);

-- score_rule 表索引
ALTER TABLE `score_rule` ADD INDEX `idx_category_id` (`category_id`);
ALTER TABLE `score_rule` ADD INDEX `idx_rule_type` (`rule_type`);

-- ========================================
-- 5. 奖学金评定管理模块索引优化
-- ========================================

-- evaluation_batch 表索引
ALTER TABLE `evaluation_batch` ADD INDEX `idx_batch_year` (`batch_year`);
ALTER TABLE `evaluation_batch` ADD INDEX `idx_status` (`status`);
ALTER TABLE `evaluation_batch` ADD INDEX `idx_start_date` (`start_date`);

-- scholarship_application 表索引
ALTER TABLE `scholarship_application` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `scholarship_application` ADD INDEX `idx_batch_id` (`batch_id`);
ALTER TABLE `scholarship_application` ADD INDEX `idx_status` (`status`);
ALTER TABLE `scholarship_application` ADD INDEX `idx_application_no` (`application_no`);
ALTER TABLE `scholarship_application` ADD INDEX `idx_create_time` (`create_time`);

-- application_achievement 表索引
ALTER TABLE `application_achievement` ADD INDEX `idx_application_id` (`application_id`);
ALTER TABLE `application_achievement` ADD INDEX `idx_achievement_type` (`achievement_type`);

-- review_record 表索引
ALTER TABLE `review_record` ADD INDEX `idx_application_id` (`application_id`);
ALTER TABLE `review_record` ADD INDEX `idx_reviewer_id` (`reviewer_id`);
ALTER TABLE `review_record` ADD INDEX `idx_review_stage` (`review_stage`);

-- evaluation_result 表索引
ALTER TABLE `evaluation_result` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `evaluation_result` ADD INDEX `idx_batch_id` (`batch_id`);
ALTER TABLE `evaluation_result` ADD INDEX `idx_total_score` (`total_score`);
ALTER TABLE `evaluation_result` ADD INDEX `idx_rank` (`rank`);

-- ========================================
-- 6. 结果异议管理模块索引优化
-- ========================================

-- result_appeal 表索引
ALTER TABLE `result_appeal` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `result_appeal` ADD INDEX `idx_evaluation_result_id` (`evaluation_result_id`);
ALTER TABLE `result_appeal` ADD INDEX `idx_status` (`status`);

-- ========================================
-- 7. 系统日志模块索引优化
-- ========================================

-- sys_operation_log 表索引
ALTER TABLE `sys_operation_log` ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE `sys_operation_log` ADD INDEX `idx_operation_time` (`operation_time`);
ALTER TABLE `sys_operation_log` ADD INDEX `idx_operation_type` (`operation_type`);

-- sys_notification 表索引
ALTER TABLE `sys_notification` ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE `sys_notification` ADD INDEX `idx_is_read` (`is_read`);
ALTER TABLE `sys_notification` ADD INDEX `idx_create_time` (`create_time`);

-- ========================================
-- 索引优化说明
-- ========================================
-- 1. 为常用查询条件字段添加索引
-- 2. 为外键字段添加索引以提高关联查询性能
-- 3. 为排序字段添加索引以提高排序性能
-- 4. 使用复合索引覆盖常用查询组合
-- ========================================
