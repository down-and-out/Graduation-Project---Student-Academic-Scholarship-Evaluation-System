-- ========================================
-- V1.8 迁移诊断脚本
-- 用于检查哪些冗余列实际存在于数据库中
-- ========================================

-- 检查冗余列是否存在
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    '存在' AS status
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
AND (
    (TABLE_NAME = 'research_patent' AND COLUMN_NAME = 'authorization_date_new')
    OR (TABLE_NAME = 'research_project' AND COLUMN_NAME = 'leader_name_new')
    OR (TABLE_NAME = 'evaluation_result' AND COLUMN_NAME = 'publish_date')
    OR (TABLE_NAME = 'result_appeal' AND COLUMN_NAME = 'status')
    OR (TABLE_NAME = 'result_appeal' AND COLUMN_NAME = 'attachment_url')
    OR (TABLE_NAME = 'sys_operation_log' AND COLUMN_NAME = 'operator_id')
    OR (TABLE_NAME = 'sys_operation_log' AND COLUMN_NAME = 'execute_time')
)
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 显示预期结果说明
SELECT '以上列出的是数据库中实际存在的冗余列' AS note;
SELECT '如果没有返回任何行，说明所有冗余列已被删除或不存在' AS note2;