-- =========================================
-- 修复 student_info 表 user_id 映射问题
-- =========================================
-- 说明：修复所有学生 user_id 与 student_info 的错位问题
-- 创建时间：2026-04-08
-- =========================================

USE scholarship;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =========================================
-- 步骤 1: 修复 student1-4 的 user_id 映射
-- =========================================

SELECT '=== 修复 student1 (张三) ===';
-- id=1 当前 user_id=4 (tutor2)，应该改为 user_id=5 (student1)
UPDATE student_info SET user_id = 5 WHERE id = 1;

SELECT '=== 修复 student2 (李四) ===';
-- id=2 当前 user_id=5，应该改为 user_id=6 (student2)
UPDATE student_info SET user_id = 6 WHERE id = 2;

SELECT '=== 修复 student3 (王五) ===';
-- id=3 当前 user_id=6，应该改为 user_id=7 (student3)
UPDATE student_info SET user_id = 7 WHERE id = 3;

SELECT '=== 修复 student4 (赵六) ===';
-- id=4 当前 user_id=7，应该改为 user_id=8 (student4)
UPDATE student_info SET user_id = 8 WHERE id = 4;

-- =========================================
-- 步骤 2: 验证修复结果
-- =========================================

SELECT '';
SELECT '=== 修复后的对应关系 ===';
SELECT
    u.id as uid,
    u.username,
    u.real_name,
    si.id as si_id,
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

-- =========================================
-- 步骤 3: 最终检查
-- =========================================

SELECT '';
SELECT '=== 最终检查：仍有问题的情况 ===';
SELECT
    u.id,
    u.username,
    u.real_name,
    si.id as si_id,
    si.name as si_name
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1
  AND (si.id IS NULL OR u.real_name != si.name);

SELECT '';
SELECT '=== 统计 ===';
SELECT
    '学生总数' as item,
    COUNT(*) as count
FROM sys_user
WHERE user_type = 1;

SELECT
    '有 student_info 的学生' as item,
    COUNT(*) as count
FROM sys_user u
INNER JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1;

-- =========================================
-- 完成
-- =========================================
