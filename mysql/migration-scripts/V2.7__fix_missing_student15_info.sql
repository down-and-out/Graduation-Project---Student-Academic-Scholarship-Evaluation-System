-- =========================================
-- 修复 user_id=22 (student15, 马十七) 缺失的 student_info 记录
-- =========================================
-- 说明：为马十七添加 student_info 记录
-- 创建时间：2026-04-08
-- =========================================

USE scholarship;

-- 查看 student15 的用户信息
SELECT id, username, real_name, email, phone
FROM sys_user
WHERE username = 'student15';

-- 查看当前 student_info 中是否已有马十七的记录
SELECT * FROM student_info WHERE student_no = '2024015';

-- 查看导师信息（使用已有的导师）
SET @tutor_zhang = (SELECT id FROM sys_user WHERE username = 'tutor1' LIMIT 1);
SET @tutor_li = (SELECT id FROM sys_user WHERE username = 'tutor2' LIMIT 1);
SET @tutor_wang = (SELECT id FROM sys_user WHERE username = 'tutor3' LIMIT 1);
SET @tutor_zhao = (SELECT id FROM sys_user WHERE username = 'tutor4' LIMIT 1);
SET @tutor_liu = (SELECT id FROM sys_user WHERE username = 'tutor5' LIMIT 1);

-- 获取 student15 的 user_id
SET @student15_id = (SELECT id FROM sys_user WHERE username = 'student15' LIMIT 1);

-- 插入马十七的 student_info 记录
-- 注意：使用 INSERT IGNORE 避免重复插入错误
INSERT IGNORE INTO student_info (
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
    u.id,
    '2024015',
    '马十七',
    0,
    2024,
    1,
    1,
    '计算机学院',
    '大数据',
    '研 2401 班',
    COALESCE(@tutor_zhang, @tutor_li, @tutor_wang, 1),
    '数据仓库',
    '共青团员',
    '汉族',
    1,
    1,
    0,
    NOW(),
    NOW()
FROM sys_user u
WHERE u.username = 'student15';

-- 验证插入结果
SELECT '插入后的 student_info 记录' as check_item;
SELECT * FROM student_info WHERE student_no = '2024015';

-- 检查是否还有孤立的 student_id 引用
SELECT '检查 course_score 中无效的 student_id' as check_item;
SELECT cs.id, cs.student_id, cs.student_name
FROM course_score cs
LEFT JOIN student_info si ON cs.student_id = si.id
WHERE si.id IS NULL;

SELECT '检查 research_paper 中无效的 student_id' as check_item;
SELECT rp.id, rp.student_id, rp.paper_title
FROM research_paper rp
LEFT JOIN student_info si ON rp.student_id = si.id
WHERE si.id IS NULL;

SELECT '检查 moral_performance 中无效的 student_id' as check_item;
SELECT mp.id, mp.student_id, mp.student_name
FROM moral_performance mp
LEFT JOIN student_info si ON mp.student_id = si.id
WHERE si.id IS NULL;

-- =========================================
-- 完成
-- =========================================
