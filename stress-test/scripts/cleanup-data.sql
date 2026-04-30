-- ========================================
-- Scholarship stress-test cleanup script
-- 用途：
--   清理 stress-test/prepare-data.sql 与压测执行过程中产生的压测数据
-- 约束：
--   仅清理 PT-* / pt_test_* 范围内的数据，避免误删正常业务数据
-- 执行方式：
--   mysql -u root -p < stress-test/scripts/cleanup-data.sql
-- ========================================

SET NAMES utf8mb4;
USE `scholarship`;

-- ========================================
-- 0. 收集压测批次、学生、用户 ID
-- ========================================
DROP TEMPORARY TABLE IF EXISTS tmp_stress_batch_ids;
CREATE TEMPORARY TABLE tmp_stress_batch_ids (
    id BIGINT PRIMARY KEY
);

INSERT INTO tmp_stress_batch_ids (id)
SELECT id
FROM evaluation_batch
WHERE batch_code IN ('PT-SMALL', 'PT-MEDIUM', 'PT-LARGE', 'PT-QUERY');

DROP TEMPORARY TABLE IF EXISTS tmp_stress_student_ids;
CREATE TEMPORARY TABLE tmp_stress_student_ids (
    id BIGINT PRIMARY KEY
);

INSERT INTO tmp_stress_student_ids (id)
SELECT id
FROM student_info
WHERE student_no LIKE 'PT-%';

DROP TEMPORARY TABLE IF EXISTS tmp_stress_user_ids;
CREATE TEMPORARY TABLE tmp_stress_user_ids (
    id BIGINT PRIMARY KEY
);

INSERT INTO tmp_stress_user_ids (id)
SELECT id
FROM sys_user
WHERE username LIKE 'pt_test_%';

-- ========================================
-- 1. 先清理评定与申请链路衍生数据
-- ========================================

-- 评定任务
DELETE FROM evaluation_task
WHERE batch_id IN (SELECT id FROM tmp_stress_batch_ids);

-- 评定结果
DELETE FROM evaluation_result
WHERE batch_id IN (SELECT id FROM tmp_stress_batch_ids)
   OR student_id IN (SELECT id FROM tmp_stress_student_ids)
   OR student_no LIKE 'PT-%';

-- 审核记录
DELETE FROM review_record
WHERE application_id IN (
    SELECT id
    FROM scholarship_application
    WHERE batch_id IN (SELECT id FROM tmp_stress_batch_ids)
       OR student_id IN (SELECT id FROM tmp_stress_student_ids)
       OR application_no LIKE 'PT-%'
);

-- 申请成果关联
DELETE FROM application_achievement
WHERE application_id IN (
    SELECT id
    FROM scholarship_application
    WHERE batch_id IN (SELECT id FROM tmp_stress_batch_ids)
       OR student_id IN (SELECT id FROM tmp_stress_student_ids)
       OR application_no LIKE 'PT-%'
);

-- 申请表
DELETE FROM scholarship_application
WHERE batch_id IN (SELECT id FROM tmp_stress_batch_ids)
   OR student_id IN (SELECT id FROM tmp_stress_student_ids)
   OR application_no LIKE 'PT-%';

-- 结果异议（该表无 application_id，通过 batch_id + student_id 清理）
DELETE FROM result_appeal
WHERE batch_id IN (SELECT id FROM tmp_stress_batch_ids)
   OR student_id IN (SELECT id FROM tmp_stress_student_ids);

-- ========================================
-- 2. 清理学生基础数据与成绩/德育
-- ========================================

DELETE FROM moral_performance
WHERE student_id IN (SELECT id FROM tmp_stress_student_ids)
   OR student_no LIKE 'PT-%';

DELETE FROM course_score
WHERE student_id IN (SELECT id FROM tmp_stress_student_ids)
   OR student_no LIKE 'PT-%';

-- 如果未来压测脚本补充了科研成果数据，这里按 student_id 范围一起清
DELETE FROM competition_award
WHERE student_id IN (SELECT id FROM tmp_stress_student_ids);

DELETE FROM research_project
WHERE student_id IN (SELECT id FROM tmp_stress_student_ids);

DELETE FROM research_patent
WHERE student_id IN (SELECT id FROM tmp_stress_student_ids);

DELETE FROM research_paper
WHERE student_id IN (SELECT id FROM tmp_stress_student_ids);

-- 学生信息
DELETE FROM student_info
WHERE id IN (SELECT id FROM tmp_stress_student_ids)
   OR student_no LIKE 'PT-%';

-- 用户角色关联
DELETE FROM sys_user_role
WHERE user_id IN (SELECT id FROM tmp_stress_user_ids);

-- 学生用户
DELETE FROM sys_user
WHERE id IN (SELECT id FROM tmp_stress_user_ids)
   OR username LIKE 'pt_test_%';

-- ========================================
-- 2.5 清理压测操作日志与通知
-- ========================================

-- 系统操作日志：按 operator_id（FK → sys_user.id）
DELETE FROM sys_operation_log
WHERE operator_id IN (SELECT id FROM tmp_stress_user_ids);

-- 系统通知：按 sender_id / receiver_id
DELETE FROM sys_notification
WHERE sender_id IN (SELECT id FROM tmp_stress_user_ids)
   OR receiver_id IN (SELECT id FROM tmp_stress_user_ids);

-- ========================================
-- 3. 清理压测批次
-- ========================================

DELETE FROM evaluation_batch
WHERE id IN (SELECT id FROM tmp_stress_batch_ids)
   OR batch_code IN ('PT-SMALL', 'PT-MEDIUM', 'PT-LARGE', 'PT-QUERY');

-- ========================================
-- 4. 输出清理后核验信息
-- ========================================

SELECT 'remaining_pt_batches' AS metric, COUNT(*) AS cnt
FROM evaluation_batch
WHERE batch_code IN ('PT-SMALL', 'PT-MEDIUM', 'PT-LARGE', 'PT-QUERY')

UNION ALL

SELECT 'remaining_pt_students' AS metric, COUNT(*) AS cnt
FROM student_info
WHERE student_no LIKE 'PT-%'

UNION ALL

SELECT 'remaining_pt_users' AS metric, COUNT(*) AS cnt
FROM sys_user
WHERE username LIKE 'pt_test_%'

UNION ALL

SELECT 'remaining_pt_applications' AS metric, COUNT(*) AS cnt
FROM scholarship_application
WHERE application_no LIKE 'PT-%'
   OR student_id IN (
       SELECT id
       FROM student_info
       WHERE student_no LIKE 'PT-%'
   )

UNION ALL

SELECT 'remaining_pt_results' AS metric, COUNT(*) AS cnt
FROM evaluation_result
WHERE student_no LIKE 'PT-%'

UNION ALL

SELECT 'remaining_pt_operation_logs' AS metric, COUNT(*) AS cnt
FROM sys_operation_log
WHERE operator_id IN (
    SELECT id FROM sys_user WHERE username LIKE 'pt_test_%'
)

UNION ALL

SELECT 'remaining_pt_notifications' AS metric, COUNT(*) AS cnt
FROM sys_notification
WHERE sender_id IN (
    SELECT id FROM sys_user WHERE username LIKE 'pt_test_%'
)
   OR receiver_id IN (
    SELECT id FROM sys_user WHERE username LIKE 'pt_test_%'
);

-- ========================================
-- 5. 清理临时表
-- ========================================
DROP TEMPORARY TABLE IF EXISTS tmp_stress_batch_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_stress_student_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_stress_user_ids;
