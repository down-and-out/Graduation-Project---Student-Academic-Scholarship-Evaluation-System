-- =========================================
-- 修复缺失的 student_info 记录
-- =========================================
-- 说明：为 student5 (钱七) 和 student15 (马十七) 添加 student_info 记录
-- 创建时间：2026-04-08
-- =========================================

USE scholarship;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =========================================
-- 步骤 1: 获取导师 ID
-- =========================================

SET @tutor_zhang = (SELECT id FROM sys_user WHERE username = 'tutor1' LIMIT 1);
SET @tutor_li = (SELECT id FROM sys_user WHERE username = 'tutor2' LIMIT 1);
SET @tutor_wang = (SELECT id FROM sys_user WHERE username = 'tutor3' LIMIT 1);
SET @tutor_zhao = (SELECT id FROM sys_user WHERE username = 'tutor4' LIMIT 1);
SET @tutor_liu = (SELECT id FROM sys_user WHERE username = 'tutor5' LIMIT 1);

SELECT '导师ID获取情况' as info,
    @tutor_zhang as tutor_zhang,
    @tutor_li as tutor_li,
    @tutor_wang as tutor_wang,
    @tutor_zhao as tutor_zhao,
    @tutor_liu as tutor_liu;

-- =========================================
-- 步骤 2: 获取学生 user_id
-- =========================================

SET @student5_id = (SELECT id FROM sys_user WHERE username = 'student5' LIMIT 1);
SET @student15_id = (SELECT id FROM sys_user WHERE username = 'student15' LIMIT 1);

SELECT '学生ID获取情况' as info,
    @student5_id as student5_id,
    @student15_id as student15_id;

-- =========================================
-- 步骤 3: 插入钱七 (student5) 的 student_info 记录
-- =========================================

SELECT '插入钱七(student5)的 student_info 记录' as action;

INSERT INTO student_info (
    user_id,
    student_no,
    name,
    gender,
    enrollment_year,
    education_level,
    training_mode,
    department,
    major,
    class_name,
    tutor_id,
    direction,
    political_status,
    nation,
    status,
    version,
    deleted,
    create_time,
    update_time
)
SELECT
    @student5_id,
    '2024005',
    '钱七',
    1,
    2024,
    1,
    1,
    '计算机学院',
    '软件工程',
    '研 2401 班',
    COALESCE(@tutor_zhang, 1),
    '软件架构',
    '共青团员',
    '汉族',
    1,
    1,
    0,
    NOW(),
    NOW()
FROM DUAL
WHERE @student5_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    name = '钱七',
    update_time = NOW();

-- =========================================
-- 步骤 4: 插入马十七 (student15) 的 student_info 记录
-- =========================================

SELECT '插入马十七(student15)的 student_info 记录' as action;

INSERT INTO student_info (
    user_id,
    student_no,
    name,
    gender,
    enrollment_year,
    education_level,
    training_mode,
    department,
    major,
    class_name,
    tutor_id,
    direction,
    political_status,
    nation,
    status,
    version,
    deleted,
    create_time,
    update_time
)
SELECT
    @student15_id,
    '2024015',
    '马十七',
    0,
    2024,
    1,
    1,
    '计算机学院',
    '大数据',
    '研 2401 班',
    COALESCE(@tutor_zhang, 1),
    '数据仓库',
    '共青团员',
    '汉族',
    1,
    1,
    0,
    NOW(),
    NOW()
FROM DUAL
WHERE @student15_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    name = '马十七',
    update_time = NOW();

-- =========================================
-- 步骤 5: 验证插入结果
-- =========================================

SELECT '插入后的 student_info 记录' as info;

SELECT
    si.id,
    si.user_id,
    si.student_no,
    si.name,
    si.department,
    si.major,
    si.class_name,
    si.direction,
    si.create_time
FROM student_info si
WHERE si.student_no IN ('2024005', '2024015')
ORDER BY si.student_no;

-- =========================================
-- 步骤 6: 检查是否还有缺失 student_info 的学生用户
-- =========================================

SELECT '最终检查：仍缺少 student_info 的学生用户' as info;

SELECT
    u.id as user_id,
    u.username,
    u.real_name
FROM sys_user u
LEFT JOIN student_info si ON u.id = si.user_id
WHERE u.user_type = 1
  AND si.id IS NULL;

-- =========================================
-- 完成
-- =========================================
