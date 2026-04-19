-- =========================================
-- 完整修复 student_info 表
-- =========================================
-- 说明：删除导师的错误记录，修复所有学生数据
-- =========================================

USE scholarship;
SET NAMES utf8mb4;

-- 步骤 1: 删除导师的错误记录 (id=6, user_id=12 是 tutor5)
SELECT '=== 删除导师的错误记录 ===';
DELETE FROM student_info WHERE user_id = 12;

-- 步骤 2: 更新 student6-13 的记录（前移一位）
-- id=7 改为 student6 (孙八, user_id=13, 2024006)
UPDATE student_info SET user_id = 13, student_no = '2024006', name = '孙八' WHERE id = 7;

-- id=8 改为 student7 (周九, user_id=14, 2024007)
UPDATE student_info SET user_id = 14, student_no = '2024007', name = '周九' WHERE id = 8;

-- id=9 改为 student8 (吴十, user_id=15, 2024008)
UPDATE student_info SET user_id = 15, student_no = '2024008', name = '吴十' WHERE id = 9;

-- id=10 改为 student9 (郑十一, user_id=16, 2024009)
UPDATE student_info SET user_id = 16, student_no = '2024009', name = '郑十一' WHERE id = 10;

-- id=11 改为 student10 (陈十二, user_id=17, 2024010)
UPDATE student_info SET user_id = 17, student_no = '2024010', name = '陈十二' WHERE id = 11;

-- id=12 改为 student11 (林十三, user_id=18, 2024011)
UPDATE student_info SET user_id = 18, student_no = '2024011', name = '林十三' WHERE id = 12;

-- id=13 改为 student12 (黄十四, user_id=19, 2024012)
UPDATE student_info SET user_id = 19, student_no = '2024012', name = '黄十四' WHERE id = 13;

-- id=14 改为 student13 (何十五, user_id=20, 2024013)
UPDATE student_info SET user_id = 20, student_no = '2024013', name = '何十五' WHERE id = 14;

-- 步骤 3: 为 student14 (高十六) 插入新记录
SELECT '=== 插入 student14 记录 ===';
INSERT INTO student_info (
    user_id, student_no, name, gender, enrollment_year, education_level,
    training_mode, department, major, class_name, tutor_id, direction,
    political_status, nation, status, version, deleted, create_time, update_time
) VALUES (
    21, '2024014', '高十六', 1, 2024, 2, 1, '计算机学院', '计算机科学与技术', '研 2402 班',
    12, '物联网', '中共党员', '蒙古族', 1, 1, 0, NOW(), NOW()
);

-- 步骤 4: 最终验证
SELECT '';
SELECT '=== 最终验证 ===';
SELECT
    u.id as uid,
    u.username,
    u.real_name,
    si.user_id as si_uid,
    si.student_no,
    si.name as si_name,
    CASE
        WHEN u.real_name = si.name THEN '✓ 正确'
        WHEN si.id IS NULL THEN '✗ 缺失'
        ELSE '✗ 不匹配'
    END as check_status
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1
ORDER BY u.id;

-- 步骤 5: 统计
SELECT '';
SELECT '=== 最终统计 ===';
SELECT '学生用户总数' as item, COUNT(*) as count FROM sys_user WHERE user_type = 1;
SELECT '有 student_info 的学生' as item, COUNT(*) as count
FROM sys_user u INNER JOIN student_info si ON u.id = si.user_id WHERE u.user_type = 1;
SELECT '缺少 student_info 的学生' as item, COUNT(*) as count
FROM sys_user u LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1 AND si.id IS NULL;
