-- =========================================
-- 检查 student_info 表和 sys_user 表数据完整性
-- =========================================
-- 创建时间：2026-04-08
-- =========================================

USE scholarship;

-- =========================================
-- 1. 检查 sys_user 表中学生用户及其对应的 student_info
-- =========================================

SELECT '=== 1. 学生用户与 student_info 关联检查 ===' as section;

SELECT
    u.id as user_id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    CASE u.user_type
        WHEN 1 THEN '研究生'
        WHEN 2 THEN '导师'
        WHEN 3 THEN '管理员'
    END as user_type,
    CASE WHEN si.id IS NOT NULL THEN '有' ELSE '无' END as has_student_info,
    si.id as student_info_id,
    si.student_no,
    si.department,
    si.major,
    si.class_name
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1  -- 只查询学生用户
ORDER BY u.id;

-- =========================================
-- 2. 检查 student_info 中 user_id 的有效性
-- =========================================

SELECT '=== 2. student_info 中 user_id 有效性检查 ===' as section;

SELECT
    si.id as student_info_id,
    si.user_id,
    si.student_no,
    si.name,
    CASE WHEN u.id IS NOT NULL THEN '有效' ELSE '无效' END as user_id_status,
    u.username,
    u.real_name as user_real_name
FROM student_info si
LEFT JOIN sys_user u ON si.user_id = u.id
ORDER BY si.id;

-- =========================================
-- 3. 检查孤立的 student_info 记录（user_id 在 sys_user 中不存在）
-- =========================================

SELECT '=== 3. 孤立的 student_info 记录检查 ===' as section;

SELECT
    si.id,
    si.user_id,
    si.student_no,
    si.name
FROM student_info si
LEFT JOIN sys_user u ON si.user_id = u.id
WHERE u.id IS NULL;

-- =========================================
-- 4. 检查 student_info 缺失的学生用户
-- =========================================

SELECT '=== 4. 缺少 student_info 的学生用户 ===' as section;

SELECT
    u.id as user_id,
    u.username,
    u.real_name,
    u.email,
    u.phone
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1
  AND si.id IS NULL
ORDER BY u.id;

-- =========================================
-- 5. 统计信息
-- =========================================

SELECT '=== 5. 统计信息 ===' as section;

SELECT
    'sys_user 中学生用户总数' as stat_item,
    COUNT(*) as count
FROM sys_user
WHERE user_type = 1;

SELECT
    'student_info 记录总数' as stat_item,
    COUNT(*) as count
FROM student_info;

SELECT
    '有 student_info 的学生用户' as stat_item,
    COUNT(*) as count
FROM sys_user u
INNER JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1;

SELECT
    '缺少 student_info 的学生用户' as stat_item,
    COUNT(*) as count
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1
  AND si.id IS NULL;

-- =========================================
-- 6. 特别检查 student15 (马十七)
-- =========================================

SELECT '=== 6. 马十七 (student15) 详细信息 ===' as section;

SELECT
    u.id as user_id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    si.id as student_info_id,
    si.student_no,
    si.name as student_name,
    si.gender,
    si.department,
    si.major,
    si.class_name,
    si.direction,
    si.political_status,
    si.nation,
    si.status
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.username = 'student15';

-- =========================================
-- 完成
-- =========================================
