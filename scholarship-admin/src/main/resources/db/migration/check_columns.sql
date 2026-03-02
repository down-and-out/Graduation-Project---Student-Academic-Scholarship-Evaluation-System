-- ========================================
-- 检查数据库表字段是否存在
-- ========================================

USE `scholarship`;

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME IN (
      'evaluation_batch',
      'research_patent',
      'research_project',
      'competition_award',
      'evaluation_result',
      'result_appeal',
      'sys_notification',
      'sys_operation_log',
      'review_record',
      'application_achievement'
  )
ORDER BY TABLE_NAME, ORDINAL_POSITION;
