-- ========================================
-- Flyway 数据库版本管理 - V1.1__add_indexes.sql
-- 版本：1.1
-- 说明：添加索引优化
-- 执行时机：在 V1.0 初始表结构创建之后执行
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. 用户与权限模块索引优化
-- ========================================

-- sys_user 表索引（如果不存在则添加）
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_user' AND index_name = 'idx_username_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_user` ADD INDEX `idx_username_status` (`username`, `status`)',
    'SELECT "Index idx_username_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_user' AND index_name = 'idx_user_type_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_user` ADD INDEX `idx_user_type_status` (`user_type`, `status`)',
    'SELECT "Index idx_user_type_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- sys_role 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_role' AND index_name = 'idx_role_code_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_role` ADD INDEX `idx_role_code_status` (`role_code`, `status`)',
    'SELECT "Index idx_role_code_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- sys_user_role 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_user_role' AND index_name = 'idx_user_id');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_user_role` ADD INDEX `idx_user_id` (`user_id`)',
    'SELECT "Index idx_user_id already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- sys_role_permission 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_role_permission' AND index_name = 'idx_role_id');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_role_permission` ADD INDEX `idx_role_id` (`role_id`)',
    'SELECT "Index idx_role_id already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- ========================================
-- 2. 研究生信息管理模块索引优化
-- ========================================

-- student_info 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'student_info' AND index_name = 'idx_student_no_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `student_info` ADD INDEX `idx_student_no_status` (`student_no`, `status`)',
    'SELECT "Index idx_student_no_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- ========================================
-- 3. 科研成果管理模块索引优化
-- ========================================

-- research_paper 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'research_paper' AND index_name = 'idx_student_id_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `research_paper` ADD INDEX `idx_student_id_status` (`student_id`, `status`)',
    'SELECT "Index idx_student_id_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- research_patent 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'research_patent' AND index_name = 'idx_student_id_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `research_patent` ADD INDEX `idx_student_id_status` (`student_id`, `status`)',
    'SELECT "Index idx_student_id_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- research_project 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'research_project' AND index_name = 'idx_student_id_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `research_project` ADD INDEX `idx_student_id_status` (`student_id`, `status`)',
    'SELECT "Index idx_student_id_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- competition_award 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'competition_award' AND index_name = 'idx_student_id_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `competition_award` ADD INDEX `idx_student_id_status` (`student_id`, `status`)',
    'SELECT "Index idx_student_id_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- ========================================
-- 4. 奖学金评定管理模块索引优化
-- ========================================

-- scholarship_application 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'scholarship_application' AND index_name = 'idx_student_id');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `scholarship_application` ADD INDEX `idx_student_id` (`student_id`)',
    'SELECT "Index idx_student_id already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'scholarship_application' AND index_name = 'idx_status');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `scholarship_application` ADD INDEX `idx_status` (`status`)',
    'SELECT "Index idx_status already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- ========================================
-- 5. 系统日志模块索引优化
-- ========================================

-- sys_operation_log 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_operation_log' AND index_name = 'idx_user_id');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_operation_log` ADD INDEX `idx_user_id` (`user_id`)',
    'SELECT "Index idx_user_id already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;

-- sys_notification 表索引
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = 'scholarship' AND table_name = 'sys_notification' AND index_name = 'idx_user_id');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE `sys_notification` ADD INDEX `idx_user_id` (`user_id`)',
    'SELECT "Index idx_user_id already exists"');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
