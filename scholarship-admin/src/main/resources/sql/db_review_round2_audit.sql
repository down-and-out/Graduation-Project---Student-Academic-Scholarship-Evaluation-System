-- 数据库联审第二轮审计 SQL
-- 作用：
-- 1. 盘点主库中混入的 audit_/bak_ 归档表
-- 2. 复核空转表 application_achievement / review_record / result_appeal 的启用状态
-- 3. 审计 scholarship_application 与 evaluation_result 的结果字段职责重叠
-- 4. 复核 sys_setting 的单生效版本使用现状

-- 1. 主库中的归档/备份表
SELECT
  table_name,
  table_rows,
  create_time,
  update_time
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND (table_name LIKE 'audit\\_20260419\\_%' ESCAPE '\\'
    OR table_name LIKE 'bak\\_%' ESCAPE '\\')
ORDER BY table_name;

-- 2. 空转表记录数
SELECT 'application_achievement' AS table_name, COUNT(*) AS row_count FROM application_achievement
UNION ALL
SELECT 'review_record' AS table_name, COUNT(*) AS row_count FROM review_record
UNION ALL
SELECT 'result_appeal' AS table_name, COUNT(*) AS row_count FROM result_appeal;

-- 3. review_record 双轨字段现状
SHOW COLUMNS FROM review_record;

-- 4. scholarship_application 与 evaluation_result 的结果字段重叠审计
SELECT
  sa.id AS application_id,
  sa.batch_id,
  sa.student_id,
  sa.ranking AS application_ranking,
  sa.award_level AS application_award_level,
  sa.scholarship_amount AS application_amount,
  er.id AS result_id,
  er.ranking AS result_ranking,
  er.department_rank,
  er.major_rank,
  er.award_level AS result_award_level,
  er.award_amount,
  er.result_status
FROM scholarship_application sa
LEFT JOIN evaluation_result er
  ON er.batch_id = sa.batch_id
 AND er.student_id = sa.student_id
WHERE sa.deleted = 0
  AND (
    sa.ranking IS NOT NULL
    OR sa.award_level IS NOT NULL
    OR sa.scholarship_amount IS NOT NULL
    OR er.id IS NOT NULL
  )
ORDER BY sa.batch_id, sa.student_id;

-- 5. evaluation_result 中旧分数字段与当前主链字段并存情况
SHOW COLUMNS FROM evaluation_result;

-- 6. sys_setting 生效版本现状
SELECT
  setting_key,
  version,
  is_active,
  create_time,
  update_time
FROM sys_setting
ORDER BY setting_key, version;

SELECT
  setting_key,
  COUNT(*) AS total_rows,
  SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_rows,
  GROUP_CONCAT(version ORDER BY version) AS versions
FROM sys_setting
GROUP BY setting_key
ORDER BY setting_key;
