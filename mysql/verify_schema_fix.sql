-- =========================================
-- 数据库修复验证脚本
-- =========================================
-- 说明：验证 V2.3 修复的数据库结构问题
-- 创建时间：2026-03-08
-- 使用方法：在 MySQL 中执行此脚本
-- =========================================

USE scholarship;

-- =========================================
-- 验证 1: review_record 表结构
-- =========================================

SELECT '\n=== 验证 1: review_record 表结构 ===' as section;

-- 检查应存在的字段
SELECT
    '应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    DATA_TYPE as data_type,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '✅ 存在' ELSE '❌ 缺失' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'review_record'
  AND COLUMN_NAME IN ('review_stage', 'review_result', 'review_score', 'review_comment', 'deleted')
ORDER BY FIELD(COLUMN_NAME, 'review_stage', 'review_result', 'review_score', 'review_comment', 'deleted');

-- 检查不应存在的字段
SELECT
    '不应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '❌ 仍存在（应删除）' ELSE '✅ 已删除' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'review_record'
  AND COLUMN_NAME IN ('score', 'opinion', 'version');

-- 完整字段列表
SELECT
    '\nreview_record 完整字段列表：' as info;
SELECT
    COLUMN_NAME as field_name,
    DATA_TYPE as data_type,
    IS_NULLABLE as nullable,
    COLUMN_DEFAULT as default_value,
    COLUMN_COMMENT as comment
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'review_record'
ORDER BY ORDINAL_POSITION;

-- =========================================
-- 验证 2: 字符集统一
-- =========================================

SELECT '\n=== 验证 2: 字符集统一 ===' as section;

SELECT
    TABLE_NAME as table_name,
    TABLE_COLLATION as collation,
    CASE
        WHEN TABLE_COLLATION = 'utf8mb4_unicode_ci' THEN '✅ 已统一'
        ELSE '❌ 需要修复'
    END as status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- =========================================
-- 验证 3: evaluation_batch 表结构
-- =========================================

SELECT '\n=== 验证 3: evaluation_batch 表结构 ===' as section;

-- 检查应存在的字段
SELECT
    '应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '✅ 存在' ELSE '❌ 缺失' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'evaluation_batch'
  AND COLUMN_NAME IN ('application_start_date', 'application_end_date')
ORDER BY COLUMN_NAME;

-- 检查不应存在的字段
SELECT
    '不应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '❌ 仍存在（应删除）' ELSE '✅ 已删除' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'evaluation_batch'
  AND COLUMN_NAME IN ('start_date', 'end_date');

-- =========================================
-- 验证 4: research_project 表结构
-- =========================================

SELECT '\n=== 验证 4: research_project 表结构 ===' as section;

-- 检查应存在的字段
SELECT
    '应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '✅ 存在' ELSE '❌ 缺失' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'research_project'
  AND COLUMN_NAME IN ('leader_name', 'leader_id', 'project_source', 'member_rank', 'project_status', 'audit_status', 'deleted')
ORDER BY COLUMN_NAME;

-- 检查不应存在的字段
SELECT
    '不应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '❌ 仍存在（应删除）' ELSE '✅ 已删除' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'research_project'
  AND COLUMN_NAME = 'leader';

-- =========================================
-- 验证 5: sys_notification 表结构
-- =========================================

SELECT '\n=== 验证 5: sys_notification 表结构 ===' as section;

-- 检查应存在的字段
SELECT
    '应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '✅ 存在' ELSE '❌ 缺失' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'sys_notification'
  AND COLUMN_NAME IN ('sender_id', 'sender_name', 'business_id', 'deleted')
ORDER BY COLUMN_NAME;

-- 检查不应存在的字段
SELECT
    '不应存在的字段' as check_type,
    COLUMN_NAME as field_name,
    CASE WHEN COLUMN_NAME IS NOT NULL THEN '❌ 仍存在（应删除）' ELSE '✅ 已删除' END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'sys_notification'
  AND COLUMN_NAME = 'publisher_id';

-- =========================================
-- 验证 6: 外键完整性检查
-- =========================================

SELECT '\n=== 验证 6: 外键完整性检查 ===' as section;

SELECT
    'scholarship_application 无效 student_id' as check_item,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END as status
FROM scholarship_application sa
LEFT JOIN student_info si ON sa.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'scholarship_application 无效 batch_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM scholarship_application sa
LEFT JOIN evaluation_batch eb ON sa.batch_id = eb.id
WHERE eb.id IS NULL

UNION ALL

SELECT
    'competition_award 无效 student_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM competition_award ca
LEFT JOIN student_info si ON ca.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'research_patent 无效 student_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM research_patent rp
LEFT JOIN student_info si ON rp.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'research_project 无效 student_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM research_project rpj
LEFT JOIN student_info si ON rpj.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'course_score 无效 student_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM course_score cs
LEFT JOIN student_info si ON cs.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'moral_performance 无效 student_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM moral_performance mp
LEFT JOIN student_info si ON mp.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'research_paper 无效 student_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM research_paper rp
LEFT JOIN student_info si ON rp.student_id = si.id
WHERE si.id IS NULL

UNION ALL

SELECT
    'score_rule 无效 category_id',
    COUNT(*),
    CASE WHEN COUNT(*) = 0 THEN '✅ 正常' ELSE '❌ 有问题' END
FROM score_rule sr
LEFT JOIN rule_category rc ON sr.category_id = rc.id
WHERE rc.id IS NULL;

-- =========================================
-- 验证 7: 数据库统计信息
-- =========================================

SELECT '\n=== 验证 7: 数据库统计信息 ===' as section;

SELECT
    TABLE_NAME as table_name,
    TABLE_ROWS as record_count,
    CASE
        WHEN TABLE_ROWS = 0 THEN '⚪ 空表'
        WHEN TABLE_ROWS < 10 THEN '🟢 少量数据'
        WHEN TABLE_ROWS < 100 THEN '🟡 中等数据'
        ELSE '🔵 大量数据'
    END as data_status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- =========================================
-- 验证完成
-- =========================================

SELECT '\n=== 验证完成 ===' as section;
SELECT '如果所有检查项都显示 ✅，说明数据库修复成功！' as message;
