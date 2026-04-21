-- ========================================
-- 研究生学业奖学金评定系统 - 测试数据初始化脚本
-- 适用库结构：当前 scholarship 数据库（已完成第二批旧字段删除）
-- 更新时间：2026-04-21
-- 说明：
-- 1. 本脚本仅使用当前仍存在的表字段
-- 2. 批次、申请记录采用幂等写法，重复执行不会按业务编号重复插入
-- 3. 课程成绩和德育表现使用自然键去重，便于本地反复初始化
-- ========================================

SET NAMES utf8mb4;
USE `scholarship`;

-- ========================================
-- 1. 评定批次数据 (evaluation_batch)
-- 当前结构使用 total_amount / winner_count / batch_status
-- 不再写入已删除的 batch_year / total_quota / scholarship_amount / status / version
-- ========================================

INSERT INTO evaluation_batch (
    batch_name,
    batch_code,
    academic_year,
    semester,
    application_start_date,
    application_end_date,
    review_start_date,
    review_end_date,
    publicity_start_date,
    publicity_end_date,
    total_amount,
    winner_count,
    description,
    batch_status,
    deleted
)
SELECT
    '2023年秋季奖学金评定',
    'BATCH-2023-1',
    '2023',
    1,
    '2023-09-01',
    '2023-10-31',
    '2023-11-01',
    '2023-11-15',
    '2023-11-16',
    '2023-11-30',
    80000.00,
    3,
    '2023年秋季学期研究生学业奖学金评定批次',
    5,
    0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2023-1'
);

INSERT INTO evaluation_batch (
    batch_name,
    batch_code,
    academic_year,
    semester,
    application_start_date,
    application_end_date,
    review_start_date,
    review_end_date,
    publicity_start_date,
    publicity_end_date,
    total_amount,
    winner_count,
    description,
    batch_status,
    deleted
)
SELECT
    '2024年春季奖学金评定',
    'BATCH-2024-2',
    '2024',
    2,
    '2024-03-01',
    '2024-04-30',
    '2024-05-01',
    '2024-05-15',
    '2024-05-16',
    '2024-05-30',
    150000.00,
    5,
    '2024年春季学期研究生学业奖学金评定批次',
    5,
    0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2'
);

INSERT INTO evaluation_batch (
    batch_name,
    batch_code,
    academic_year,
    semester,
    application_start_date,
    application_end_date,
    review_start_date,
    review_end_date,
    publicity_start_date,
    publicity_end_date,
    total_amount,
    winner_count,
    description,
    batch_status,
    deleted
)
SELECT
    '2025年秋季奖学金评定',
    'BATCH-2025-1',
    '2025',
    1,
    '2025-09-01',
    '2025-10-31',
    '2025-11-01',
    '2025-11-15',
    '2025-11-16',
    '2025-11-30',
    96000.00,
    12,
    '2025年秋季学期研究生学业奖学金评定批次',
    2,
    0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1'
);

-- ========================================
-- 2. 课程成绩数据 (course_score)
-- 自然键：student_no + course_code + academic_year + semester
-- ========================================

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 1, '2024001', '张三', '高级算法设计', 'CS601', 1, 3.0, 92.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024001' AND course_code = 'CS601' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 1, '2024001', '张三', '机器学习', 'CS602', 1, 4.0, 95.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024001' AND course_code = 'CS602' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 1, '2024001', '张三', '深度学习', 'CS603', 1, 3.0, 88.0, 3.7, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024001' AND course_code = 'CS603' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 1, '2024001', '张三', '学术英语写作', 'GE601', 2, 2.0, 85.0, 3.5, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024001' AND course_code = 'GE601' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 2, '2024002', '李四', '高级算法设计', 'CS601', 1, 3.0, 88.0, 3.7, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024002' AND course_code = 'CS601' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 2, '2024002', '李四', '软件工程', 'SE601', 1, 4.0, 90.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024002' AND course_code = 'SE601' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 2, '2024002', '李四', '大数据技术', 'CS604', 1, 3.0, 87.0, 3.7, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024002' AND course_code = 'CS604' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 2, '2024002', '李四', '学术英语写作', 'GE601', 2, 2.0, 82.0, 3.3, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024002' AND course_code = 'GE601' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 3, '2024003', '王五', '网络安全', 'CS605', 1, 3.0, 91.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024003' AND course_code = 'CS605' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 3, '2024003', '王五', '分布式系统', 'CS606', 1, 4.0, 89.0, 3.7, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024003' AND course_code = 'CS606' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 3, '2024003', '王五', '机器学习', 'CS602', 1, 3.0, 86.0, 3.5, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024003' AND course_code = 'CS602' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 4, '2024004', '赵六', '软件架构', 'SE602', 1, 3.0, 93.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024004' AND course_code = 'SE602' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 4, '2024004', '赵六', '云计算技术', 'CS607', 1, 4.0, 91.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024004' AND course_code = 'CS607' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 4, '2024004', '赵六', '项目管理', 'SE603', 1, 2.0, 88.0, 3.7, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024004' AND course_code = 'SE603' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 5, '2024005', '钱七', '人工智能导论', 'AI601', 1, 3.0, 94.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024005' AND course_code = 'AI601' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 5, '2024005', '钱七', '计算机视觉', 'AI602', 1, 4.0, 92.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024005' AND course_code = 'AI602' AND academic_year = '2024' AND semester = 1);

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted)
SELECT 5, '2024005', '钱七', '自然语言处理', 'AI603', 1, 3.0, 90.0, 4.0, '2024', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_no = '2024005' AND course_code = 'AI603' AND academic_year = '2024' AND semester = 1);

-- ========================================
-- 3. 德育表现数据 (moral_performance)
-- 当前结构只使用 score，不再写入已删除的 moral_score
-- 自然键：student_no + performance_name + academic_year + semester
-- ========================================

INSERT INTO moral_performance (
    student_id, student_no, student_name, performance_type, performance_name,
    description, level, honor_title, honor_level, honor_date, volunteer_hours,
    social_work, score, academic_year, semester, audit_status, deleted
)
SELECT
    1, '2024001', '张三', 1, '志愿服务',
    '参与社区志愿服务活动，累计服务 50 小时', 2, '优秀志愿者', 2, '2024-10-01', 50,
    '班级学习委员', 85.0, '2024', 1, 1, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM moral_performance
    WHERE student_no = '2024001' AND performance_name = '志愿服务' AND academic_year = '2024' AND semester = 1
);

INSERT INTO moral_performance (
    student_id, student_no, student_name, performance_type, performance_name,
    description, level, honor_title, honor_level, honor_date, volunteer_hours,
    social_work, score, academic_year, semester, audit_status, deleted
)
SELECT
    2, '2024002', '李四', 4, '学生工作',
    '担任学生会副主席，组织多项校园活动', 1, '优秀学生干部', 1, '2024-11-01', 30,
    '学生会副主席', 88.0, '2024', 1, 1, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM moral_performance
    WHERE student_no = '2024002' AND performance_name = '学生工作' AND academic_year = '2024' AND semester = 1
);

INSERT INTO moral_performance (
    student_id, student_no, student_name, performance_type, performance_name,
    description, level, honor_title, honor_level, honor_date, volunteer_hours,
    social_work, score, academic_year, semester, audit_status, deleted
)
SELECT
    3, '2024003', '王五', 2, '社会实践',
    '担任研究生会干事，积极参与社会实践活动', 2, '社会工作先进个人', 2, '2024-09-01', 40,
    '研究生会干事', 82.0, '2024', 1, 1, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM moral_performance
    WHERE student_no = '2024003' AND performance_name = '社会实践' AND academic_year = '2024' AND semester = 1
);

INSERT INTO moral_performance (
    student_id, student_no, student_name, performance_type, performance_name,
    description, level, honor_title, honor_level, honor_date, volunteer_hours,
    social_work, score, academic_year, semester, audit_status, deleted
)
SELECT
    4, '2024004', '赵六', 1, '志愿服务',
    '参与疫情防控志愿服务，累计服务 80 小时', 1, '疫情防控先进个人', 1, '2024-08-01', 80,
    '班级组织委员', 90.0, '2024', 1, 1, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM moral_performance
    WHERE student_no = '2024004' AND performance_name = '志愿服务' AND academic_year = '2024' AND semester = 1
);

INSERT INTO moral_performance (
    student_id, student_no, student_name, performance_type, performance_name,
    description, level, honor_title, honor_level, honor_date, volunteer_hours,
    social_work, score, academic_year, semester, audit_status, deleted
)
SELECT
    5, '2024005', '钱七', 4, '学生工作',
    '担任班级班长，带领班级获得优秀班集体称号', 1, '三好学生', 1, '2024-12-01', 35,
    '班级班长', 87.0, '2024', 1, 1, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM moral_performance
    WHERE student_no = '2024005' AND performance_name = '学生工作' AND academic_year = '2024' AND semester = 1
);

-- ========================================
-- 4. 奖学金申请数据 (scholarship_application)
-- 当前表仍保留 ranking / award_level / scholarship_amount
-- 但插入语句不再显式写入旧版 version 字段，使用库默认值
-- 自然键：application_no
-- ========================================

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    4, 1, 'APP-2024-001', 92.5, 1, 1,
    10000.00, '2024-03-15 10:00:00', '2024-03-15 11:30:00', 3,
    '同意推荐，该生学习刻苦，科研成果突出', 2, '2024-03-20 14:00:00', '同意评定', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-001'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    4, 2, 'APP-2024-002', 90.0, 2, 1,
    10000.00, '2024-03-15 10:30:00', '2024-03-15 12:00:00', 3,
    '同意推荐，该生综合素质优秀', 2, '2024-03-20 15:00:00', '同意评定', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-002'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    4, 3, 'APP-2024-003', 88.5, 3, 2,
    8000.00, '2024-03-16 09:00:00', '2024-03-16 10:00:00', 3,
    '同意推荐', 3, '2024-03-21 10:00:00', '同意评定', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-003'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    4, 4, 'APP-2024-004', 91.0, 4, 1,
    10000.00, '2024-03-16 10:00:00', '2024-03-16 11:00:00', 3,
    '同意推荐，该生志愿服务表现突出', 3, '2024-03-21 11:00:00', '同意评定', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-004'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    4, 5, 'APP-2024-005', 93.0, 5, 1,
    10000.00, '2024-03-17 09:00:00', '2024-03-17 10:00:00', 3,
    '同意推荐，该生成绩优秀', 2, '2024-03-22 09:00:00', '同意评定', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-005'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    5, 1, 'APP-2025-001', 94.5, NULL, NULL,
    NULL, '2025-02-15 10:00:00', '2025-02-15 11:30:00', 1,
    NULL, NULL, NULL, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-001'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    5, 2, 'APP-2025-002', 91.0, NULL, NULL,
    NULL, '2025-02-15 10:30:00', '2025-02-15 12:00:00', 1,
    NULL, NULL, NULL, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-002'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    5, 3, 'APP-2025-003', 89.0, NULL, NULL,
    NULL, '2025-02-16 09:00:00', '2025-02-16 10:00:00', 1,
    NULL, NULL, NULL, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-003'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    5, 4, 'APP-2025-004', 92.0, NULL, NULL,
    NULL, '2025-02-16 10:00:00', '2025-02-16 11:00:00', 1,
    NULL, NULL, NULL, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-004'
);

INSERT INTO scholarship_application (
    batch_id, student_id, application_no, total_score, ranking, award_level,
    scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, deleted
)
SELECT
    5, 5, 'APP-2025-005', 95.0, NULL, NULL,
    NULL, '2025-02-17 09:00:00', '2025-02-17 10:00:00', 1,
    NULL, NULL, NULL, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-005'
);

-- ========================================
-- 5. 数据校验
-- ========================================

SELECT '=== 测试数据初始化完成 ===' AS message;

SELECT 'evaluation_batch' AS table_name, COUNT(*) AS row_count FROM evaluation_batch
UNION ALL
SELECT 'course_score', COUNT(*) FROM course_score
UNION ALL
SELECT 'moral_performance', COUNT(*) FROM moral_performance
UNION ALL
SELECT 'scholarship_application', COUNT(*) FROM scholarship_application;
