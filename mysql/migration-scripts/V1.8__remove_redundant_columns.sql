-- ========================================
-- 清理冗余字段迁移脚本 (兼容所有 MySQL 版本)
-- 生成日期: 2026-03-05
-- 版本: v2 (兼容 MySQL 5.7+)
--
-- 说明: 只删除数据库中存在但 Java 代码未使用的字段
-- 警告: 执行前请备份数据库！
-- 执行命令: mysql -u root -p scholarship < V1.8__remove_redundant_columns.sql
-- ========================================

SET @dbname = 'scholarship';

-- ========================================
-- 1. research_patent 表
-- 删除: authorization_date_new (注释乱码，代码未定义此属性)
-- 保留: grant_date (代码使用)
-- ========================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'research_patent' AND COLUMN_NAME = 'authorization_date_new') > 0,
    'ALTER TABLE research_patent DROP COLUMN authorization_date_new',
    'SELECT ''[跳过] research_patent.authorization_date_new 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 2. research_project 表
-- 删除: leader_name_new (注释乱码，代码未定义此属性)
-- 保留: leader_name (代码使用)
-- ========================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'research_project' AND COLUMN_NAME = 'leader_name_new') > 0,
    'ALTER TABLE research_project DROP COLUMN leader_name_new',
    'SELECT ''[跳过] research_project.leader_name_new 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 3. evaluation_result 表
-- 删除: publish_date (代码未定义 publishDate，使用 publicity_date)
-- 保留: publicity_date (代码使用)
-- ========================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'evaluation_result' AND COLUMN_NAME = 'publish_date') > 0,
    'ALTER TABLE evaluation_result DROP COLUMN publish_date',
    'SELECT ''[跳过] evaluation_result.publish_date 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 4. result_appeal 表
-- 删除: status (代码使用 appeal_status)
-- 删除: attachment_url (代码使用 attachment_path)
-- 保留: appeal_status, attachment_path (代码使用)
-- ========================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'result_appeal' AND COLUMN_NAME = 'status') > 0,
    'ALTER TABLE result_appeal DROP COLUMN status',
    'SELECT ''[跳过] result_appeal.status 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'result_appeal' AND COLUMN_NAME = 'attachment_url') > 0,
    'ALTER TABLE result_appeal DROP COLUMN attachment_url',
    'SELECT ''[跳过] result_appeal.attachment_url 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 5. sys_operation_log 表
-- 删除: operator_id (代码使用 operatorId 映射到 user_id)
-- 删除: execute_time (代码使用 executionTime 映射到 response_time)
-- 保留: user_id, response_time (代码使用)
-- ========================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'sys_operation_log' AND COLUMN_NAME = 'operator_id') > 0,
    'ALTER TABLE sys_operation_log DROP COLUMN operator_id',
    'SELECT ''[跳过] sys_operation_log.operator_id 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'sys_operation_log' AND COLUMN_NAME = 'execute_time') > 0,
    'ALTER TABLE sys_operation_log DROP COLUMN execute_time',
    'SELECT ''[跳过] sys_operation_log.execute_time 不存在'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 执行完成提示
-- ========================================
SELECT 'Migration V1.8 completed successfully!' AS status;
SELECT 'Removed redundant columns that were not used by Java code.' AS message;