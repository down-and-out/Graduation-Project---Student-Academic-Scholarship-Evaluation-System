-- ========================================
-- Flyway 数据库版本管理 - V1.7 添加测试数据
-- ========================================
-- 说明：为奖学金评定系统添加完整的测试数据
-- 创建时间：2026-03-04
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. 添加评定批次数据 (evaluation_batch)
-- ========================================

-- 检查并添加 2023年秋季评定批次（已完成）
INSERT INTO evaluation_batch (batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date, review_start_date, review_end_date,
    publicity_start_date, publicity_end_date, status, scholarship_amount, winner_count,
    description, deleted, create_time, update_time)
SELECT '2023年秋季奖学金评定', 'BATCH-2023-1', '2023', 1,
    '2023-09-01', '2023-10-31', '2023-11-01', '2023-11-15',
    '2023-11-16', '2023-11-30', 5, 80000.00, 3,
    '2023年秋季学期研究生学业奖学金评定', 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2023-1');

-- 检查并添加 2024年春季评定批次（已完成）
INSERT INTO evaluation_batch (batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date, review_start_date, review_end_date,
    publicity_start_date, publicity_end_date, status, scholarship_amount, winner_count,
    description, deleted, create_time, update_time)
SELECT '2024年春季奖学金评定', 'BATCH-2024-2', '2024', 2,
    '2024-03-01', '2024-04-30', '2024-05-01', '2024-05-15',
    '2024-05-16', '2024-05-30', 5, 150000.00, 5,
    '2024年春季学期研究生学业奖学金评定', 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2');

-- 检查并添加 2025年秋季评定批次（申请中）
INSERT INTO evaluation_batch (batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date, review_start_date, review_end_date,
    publicity_start_date, publicity_end_date, status, scholarship_amount, winner_count,
    description, deleted, create_time, update_time)
SELECT '2025年秋季奖学金评定', 'BATCH-2025-1', '2025', 1,
    '2025-09-01', '2025-10-31', '2025-11-01', '2025-11-15',
    '2025-11-16', '2025-11-30', 2, 96000.00, 0,
    '2025年秋季学期研究生学业奖学金评定', 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1');

-- ========================================
-- 2. 添加课程成绩数据 (course_score)
-- ========================================

-- 张三的课程成绩 (student_id=1, student_no=2024001)
INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 1, '2024001', '张三', '高级算法设计', 'CS601', 1, 3.0, 92.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 1 AND course_code = 'CS601');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 1, '2024001', '张三', '机器学习', 'CS602', 1, 4.0, 95.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 1 AND course_code = 'CS602');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 1, '2024001', '张三', '深度学习', 'CS603', 1, 3.0, 88.0, 3.7, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 1 AND course_code = 'CS603');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 1, '2024001', '张三', '学术英语写作', 'GE601', 2, 2.0, 85.0, 3.5, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 1 AND course_code = 'GE601');

-- 李四的课程成绩 (student_id=2, student_no=2024002)
INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 2, '2024002', '李四', '高级算法设计', 'CS601', 1, 3.0, 88.0, 3.7, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 2 AND course_code = 'CS601');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 2, '2024002', '李四', '软件工程', 'SE601', 1, 4.0, 90.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 2 AND course_code = 'SE601');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 2, '2024002', '李四', '大数据技术', 'CS604', 1, 3.0, 87.0, 3.7, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 2 AND course_code = 'CS604');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 2, '2024002', '李四', '学术英语写作', 'GE601', 2, 2.0, 82.0, 3.3, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 2 AND course_code = 'GE601');

-- 王五的课程成绩 (student_id=3, student_no=2024003)
INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 3, '2024003', '王五', '网络安全', 'CS605', 1, 3.0, 91.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 3 AND course_code = 'CS605');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 3, '2024003', '王五', '分布式系统', 'CS606', 1, 4.0, 89.0, 3.7, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 3 AND course_code = 'CS606');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 3, '2024003', '王五', '机器学习', 'CS602', 1, 3.0, 86.0, 3.5, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 3 AND course_code = 'CS602');

-- 赵六的课程成绩 (student_id=4, student_no=2024004)
INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 4, '2024004', '赵六', '软件架构', 'SE602', 1, 3.0, 93.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 4 AND course_code = 'SE602');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 4, '2024004', '赵六', '云计算技术', 'CS607', 1, 4.0, 91.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 4 AND course_code = 'CS607');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 4, '2024004', '赵六', '项目管理', 'SE603', 1, 2.0, 88.0, 3.7, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 4 AND course_code = 'SE603');

-- 钱七的课程成绩 (student_id=5, student_no=2024005)
INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 5, '2024005', '钱七', '人工智能导论', 'AI601', 1, 3.0, 94.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 5 AND course_code = 'AI601');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 5, '2024005', '钱七', '计算机视觉', 'AI602', 1, 4.0, 92.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 5 AND course_code = 'AI602');

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code,
    course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT 5, '2024005', '钱七', '自然语言处理', 'AI603', 1, 3.0, 90.0, 4.0, '2024', 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM course_score WHERE student_id = 5 AND course_code = 'AI603');

-- ========================================
-- 3. 添加思想品德表现数据 (moral_performance)
-- ========================================

-- 张三的思想品德表现
INSERT INTO moral_performance (student_id, student_no, student_name, performance_type,
    performance_name, description, level, score, academic_year, semester, audit_status,
    deleted, create_time, update_time)
SELECT 1, '2024001', '张三', 1, '志愿服务',
    '参与社区志愿服务活动，累计服务50小时', 2, 90.0, '2024', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM moral_performance WHERE student_id = 1 AND performance_name = '志愿服务');

-- 李四的思想品德表现
INSERT INTO moral_performance (student_id, student_no, student_name, performance_type,
    performance_name, description, level, score, academic_year, semester, audit_status,
    deleted, create_time, update_time)
SELECT 2, '2024002', '李四', 4, '学生工作',
    '担任学生会副主席，组织多项校园活动', 1, 92.0, '2024', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM moral_performance WHERE student_id = 2 AND performance_name = '学生工作');

-- 王五的思想品德表现
INSERT INTO moral_performance (student_id, student_no, student_name, performance_type,
    performance_name, description, level, score, academic_year, semester, audit_status,
    deleted, create_time, update_time)
SELECT 3, '2024003', '王五', 2, '社会实践',
    '担任研究生会干事，积极参与社会实践活动', 2, 88.0, '2024', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM moral_performance WHERE student_id = 3 AND performance_name = '社会实践');

-- 赵六的思想品德表现
INSERT INTO moral_performance (student_id, student_no, student_name, performance_type,
    performance_name, description, level, score, academic_year, semester, audit_status,
    deleted, create_time, update_time)
SELECT 4, '2024004', '赵六', 1, '志愿服务',
    '参与疫情防控志愿服务，累计服务80小时', 1, 95.0, '2024', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM moral_performance WHERE student_id = 4 AND performance_name = '志愿服务');

-- 钱七的思想品德表现
INSERT INTO moral_performance (student_id, student_no, student_name, performance_type,
    performance_name, description, level, score, academic_year, semester, audit_status,
    deleted, create_time, update_time)
SELECT 5, '2024005', '钱七', 4, '学生工作',
    '担任班级班长，带领班级获得优秀班集体称号', 1, 93.0, '2024', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM moral_performance WHERE student_id = 5 AND performance_name = '学生工作');

-- ========================================
-- 4. 添加奖学金申请数据 (scholarship_application)
-- ========================================

-- 获取 2024年春季评定批次的ID（假设batch_code='BATCH-2024-2'）
-- 为 2024年春季奖学金评定添加申请记录
INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2'),
    1, 'APP-2024-001', 92.5, 1, 1, 10000.00,
    '2024-03-15 10:00:00', '2024-03-15 11:30:00', 3,
    '同意推荐，该生学习刻苦，科研成果突出', 2, '2024-03-20 14:00:00', '同意评定', 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-001');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2'),
    2, 'APP-2024-002', 90.0, 2, 1, 10000.00,
    '2024-03-15 10:30:00', '2024-03-15 12:00:00', 3,
    '同意推荐，该生综合素质优秀', 2, '2024-03-20 15:00:00', '同意评定', 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-002');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2'),
    3, 'APP-2024-003', 88.5, 3, 2, 8000.00,
    '2024-03-16 09:00:00', '2024-03-16 10:00:00', 3,
    '同意推荐', 3, '2024-03-21 10:00:00', '同意评定', 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-003');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2'),
    4, 'APP-2024-004', 91.0, 4, 1, 10000.00,
    '2024-03-16 10:00:00', '2024-03-16 11:00:00', 3,
    '同意推荐，该生志愿服务表现突出', 3, '2024-03-21 11:00:00', '同意评定', 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-004');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2'),
    5, 'APP-2024-005', 93.0, 5, 1, 10000.00,
    '2024-03-17 09:00:00', '2024-03-17 10:00:00', 3,
    '同意推荐，该生学习成绩优异', 2, '2024-03-22 09:00:00', '同意评定', 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2024-005');

-- 为 2025年春季评定批次添加申请记录（如果有2025年春季的批次）
INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1'),
    1, 'APP-2025-001', 94.5, NULL, NULL, NULL,
    '2025-02-15 10:00:00', '2025-02-15 11:30:00', 1,
    NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-001')
AND EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1'),
    2, 'APP-2025-002', 91.0, NULL, NULL, NULL,
    '2025-02-15 10:30:00', '2025-02-15 12:00:00', 1,
    NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-002')
AND EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1'),
    3, 'APP-2025-003', 89.0, NULL, NULL, NULL,
    '2025-02-16 09:00:00', '2025-02-16 10:00:00', 1,
    NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-003')
AND EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1'),
    4, 'APP-2025-004', 92.0, NULL, NULL, NULL,
    '2025-02-16 10:00:00', '2025-02-16 11:00:00', 1,
    NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-004')
AND EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1');

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score,
    ranking, award_level, scholarship_amount, application_time, submit_time, status,
    tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT
    (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1'),
    5, 'APP-2025-005', 95.0, NULL, NULL, NULL,
    '2025-02-17 09:00:00', '2025-02-17 10:00:00', 1,
    NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM scholarship_application WHERE application_no = 'APP-2025-005')
AND EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1');

-- ========================================
-- 5. 更新现有评定批次的 winner_count
-- ========================================

-- 更新 2024年春季 的获奖人数
UPDATE evaluation_batch
SET winner_count = 5
WHERE batch_code = 'BATCH-2024-2';

-- 更新 2023年秋季 的获奖人数
UPDATE evaluation_batch
SET winner_count = 3
WHERE batch_code = 'BATCH-2023-1';

-- ========================================
-- 完成
-- ========================================
COMMIT;