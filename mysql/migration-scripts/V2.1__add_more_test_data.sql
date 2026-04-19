-- ========================================
-- Flyway 数据库版本管理 - V2.1 添加更多测试数据
-- ========================================
-- 说明：为奖学金评定系统添加更多测试数据
-- 创建时间：2026-03-08
-- ========================================

USE scholarship;

-- ========================================
-- 1. 添加更多系统用户 (sys_user)
-- ========================================

-- 添加更多导师用户
INSERT INTO sys_user (username, password, real_name, user_type, email, phone, status) VALUES
('tutor3', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '王教授', 2, 'tutor3@scholarship.com', '13800138003', 1),
('tutor4', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '赵教授', 2, 'tutor4@scholarship.com', '13800138004', 1),
('tutor5', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '刘副教授', 2, 'tutor5@scholarship.com', '13800138005', 1)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 添加更多学生用户
INSERT INTO sys_user (username, password, real_name, user_type, email, phone, status) VALUES
('student6', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '孙八', 1, 'student6@scholarship.com', '13800138015', 1),
('student7', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '周九', 1, 'student7@scholarship.com', '13800138016', 1),
('student8', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '吴十', 1, 'student8@scholarship.com', '13800138017', 1),
('student9', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '郑十一', 1, 'student9@scholarship.com', '13800138018', 1),
('student10', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '陈十二', 1, 'student10@scholarship.com', '13800138019', 1),
('student11', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '林十三', 1, 'student11@scholarship.com', '13800138020', 1),
('student12', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '黄十四', 1, 'student12@scholarship.com', '13800138021', 1),
('student13', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '何十五', 1, 'student13@scholarship.com', '13800138022', 1),
('student14', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '高十六', 1, 'student14@scholarship.com', '13800138023', 1),
('student15', '$2a$10$WVvpc6tp0XMkfNgTwKITke8BoZ4ZRCIWh1hKd71hw4easy5lzWT0u', '马十七', 1, 'student15@scholarship.com', '13800138024', 1)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 2. 关联用户角色 (sys_user_role)
-- ========================================

-- 使用变量存储角色ID
SET @role_student = (SELECT id FROM sys_role WHERE role_code = 'ROLE_STUDENT' LIMIT 1);
SET @role_tutor = (SELECT id FROM sys_role WHERE role_code = 'ROLE_TUTOR' LIMIT 1);

-- 为新导师分配角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, @role_tutor FROM sys_user u WHERE u.username IN ('tutor3', 'tutor4', 'tutor5') AND @role_tutor IS NOT NULL
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 为新学生分配角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, @role_student FROM sys_user u
WHERE u.username IN ('student6', 'student7', 'student8', 'student9', 'student10', 'student11', 'student12', 'student13', 'student14', 'student15')
AND @role_student IS NOT NULL
ON DUPLICATE KEY UPDATE user_id = user_id;

-- ========================================
-- 3. 添加更多学生信息 (student_info)
-- ========================================

-- 查找导师ID
SET @tutor_zhang = (SELECT id FROM sys_user WHERE username = 'tutor1' LIMIT 1);
SET @tutor_li = (SELECT id FROM sys_user WHERE username = 'tutor2' LIMIT 1);
SET @tutor_wang = (SELECT id FROM sys_user WHERE username = 'tutor3' LIMIT 1);
SET @tutor_zhao = (SELECT id FROM sys_user WHERE username = 'tutor4' LIMIT 1);
SET @tutor_liu = (SELECT id FROM sys_user WHERE username = 'tutor5' LIMIT 1);

-- 孙八
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024006', '孙八', 1, 2024, 1, 1, '计算机学院', '计算机科学与技术', '研 2402 班', @tutor_wang, '数据挖掘', '共青团员', '汉族', 1
FROM sys_user u WHERE u.username = 'student6'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 周九
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024007', '周九', 0, 2024, 1, 1, '软件学院', '软件工程', '研 2401 班', @tutor_zhao, '区块链', '中共党员', '汉族', 1
FROM sys_user u WHERE u.username = 'student7'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 吴十
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024008', '吴十', 1, 2024, 2, 1, '计算机学院', '人工智能', '研 2402 班', @tutor_wang, '计算机视觉', '共青团员', '满族', 1
FROM sys_user u WHERE u.username = 'student8'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 郑十一
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024009', '郑十一', 0, 2024, 1, 1, '计算机学院', '计算机科学与技术', '研 2401 班', @tutor_zhang, '自然语言处理', '中共党员', '汉族', 1
FROM sys_user u WHERE u.username = 'student9'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 陈十二
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024010', '陈十二', 1, 2024, 1, 1, '软件学院', '软件工程', '研 2402 班', @tutor_li, '嵌入式系统', '共青团员', '回族', 1
FROM sys_user u WHERE u.username = 'student10'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 林十三
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024011', '林十三', 0, 2024, 2, 1, '计算机学院', '人工智能', '研 2401 班', @tutor_liu, '深度学习', '中共党员', '汉族', 1
FROM sys_user u WHERE u.username = 'student11'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 黄十四
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024012', '黄十四', 1, 2024, 1, 1, '计算机学院', '网络安全', '研 2402 班', @tutor_wang, '密码学', '共青团员', '汉族', 1
FROM sys_user u WHERE u.username = 'student12'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 何十五
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024013', '何十五', 0, 2024, 1, 1, '软件学院', '软件工程', '研 2401 班', @tutor_zhao, '软件测试', '预备党员', '汉族', 1
FROM sys_user u WHERE u.username = 'student13'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 高十六
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024014', '高十六', 1, 2024, 2, 1, '计算机学院', '计算机科学与技术', '研 2402 班', @tutor_liu, '物联网', '中共党员', '蒙古族', 1
FROM sys_user u WHERE u.username = 'student14'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 马十七
INSERT INTO student_info (user_id, student_no, name, gender, enrollment_year, education_level, training_mode, department, major, class_name, tutor_id, direction, political_status, nation, status)
SELECT u.id, '2024015', '马十七', 0, 2024, 1, 1, '计算机学院', '大数据', '研 2401 班', @tutor_zhang, '数据仓库', '共青团员', '汉族', 1
FROM sys_user u WHERE u.username = 'student15'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 4. 添加课程成绩 (course_score)
-- ========================================

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, '数据挖掘技术', 'CS608', 1, 3.0, 90.0, 4.0, '2024', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, '数据库系统', 'CS609', 1, 3.0, 87.0, 3.7, '2024', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, '机器学习', 'CS602', 1, 4.0, 89.0, 3.7, '2024', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, '区块链技术', 'CS610', 1, 3.0, 91.0, 4.0, '2024', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024007'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, '计算机视觉', 'AI602', 1, 4.0, 93.0, 4.0, '2024', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024008'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO course_score (student_id, student_no, student_name, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, '深度学习', 'CS603', 1, 3.0, 94.0, 4.0, '2024', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024011'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 5. 添加德育表现 (moral_performance)
-- ========================================

INSERT INTO moral_performance (student_id, student_no, student_name, performance_type, performance_name, description, level, score, academic_year, semester, audit_status, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, 1, '志愿服务', '参与校园图书馆志愿服务，累计服务40小时', 2, 88.0, '2024', 1, 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO moral_performance (student_id, student_no, student_name, performance_type, performance_name, description, level, score, academic_year, semester, audit_status, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, 3, '荣誉称号', '获得校级三好学生称号', 3, 90.0, '2024', 1, 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024007'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO moral_performance (student_id, student_no, student_name, performance_type, performance_name, description, level, score, academic_year, semester, audit_status, deleted, create_time, update_time)
SELECT s.id, s.student_no, s.name, 3, '荣誉称号', '获得国家奖学金', 1, 95.0, '2024', 1, 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024011'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 6. 添加科研论文 (research_paper)
-- ========================================

INSERT INTO research_paper (student_id, paper_title, authors, author_rank, journal_name, journal_level, impact_factor, publication_date, status, deleted, create_time, update_time)
SELECT s.id, '基于深度学习的文本分类算法研究', '孙八，张三，李四', 1, '计算机学报', 6, 2.8, '2024-07-15', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO research_paper (student_id, paper_title, authors, author_rank, journal_name, journal_level, impact_factor, publication_date, status, deleted, create_time, update_time)
SELECT s.id, '基于区块链的供应链金融系统研究', '周九，王五', 1, '软件学报', 6, 2.2, '2024-08-20', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024007'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO research_paper (student_id, paper_title, authors, author_rank, journal_name, journal_level, impact_factor, publication_date, status, deleted, create_time, update_time)
SELECT s.id, 'Deep Reinforcement Learning for Robot Navigation', 'Lin Shisan, Zhang San', 1, 'IEEE Transactions on Robotics', 1, 6.8, '2024-05-20', 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024011'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 7. 添加科研专利 (research_patent)
-- ========================================

INSERT INTO research_patent (student_id, patent_name, patent_type, patent_no, inventors, inventor_rank, applicant, application_date, authorization_date, patent_status, audit_status, deleted, create_time, update_time)
SELECT s.id, '一种数据挖掘方法及系统', 1, 'ZL202410006', '孙八，张三', 1, '某某大学', '2024-03-10', NULL, 1, 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO research_patent (student_id, patent_name, patent_type, patent_no, inventors, inventor_rank, applicant, application_date, authorization_date, patent_status, audit_status, deleted, create_time, update_time)
SELECT s.id, '一种神经网络训练方法', 1, 'ZL202410010', '林十三，李四', 1, '某某大学', '2024-01-10', '2024-10-20', 2, 1, 0, NOW(), NOW() FROM student_info s WHERE s.student_no = '2024011'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 8. 添加科研项目 (research_project)
-- ========================================

INSERT INTO research_project (student_id, project_name, project_no, project_level, project_type, project_source, leader_id, leader_name, member_rank, project_role, participants, start_date, end_date, funding, project_status, audit_status, deleted, create_time, update_time)
SELECT s.id, '面向电商的数据挖掘技术研究', '2024YFB006', 2, 2, '省教育厅', @tutor_wang, '王教授', 2, 2, '王教授，孙八，张三', '2024-01-01', '2025-12-31', 50.00, 1, 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO research_project (student_id, project_name, project_no, project_level, project_type, project_source, leader_id, leader_name, member_rank, project_role, participants, start_date, end_date, funding, project_status, audit_status, deleted, create_time, update_time)
SELECT s.id, '深度强化学习理论与应用', '2024YFB010', 1, 1, '国家重点研发计划', @tutor_liu, '刘副教授', 2, 2, '刘副教授，林十三', '2024-01-01', '2028-12-31', 1200.00, 1, 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024011'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 9. 添加学科竞赛 (competition_award)
-- ========================================

INSERT INTO competition_award (student_id, competition_name, award_level, award_rank, award_date, organizer, team_members, status, deleted, create_time, update_time)
SELECT s.id, '全国大学生数学建模竞赛', 1, 2, '2024-09-15', '中国工业与应用数学学会', '孙八，周九，吴十', 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024006'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO competition_award (student_id, competition_name, award_level, award_rank, award_date, organizer, team_members, status, deleted, create_time, update_time)
SELECT s.id, 'Kaggle数据挖掘竞赛', 1, 1, '2024-09-01', 'Kaggle', '林十三', 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024011'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 10. 添加奖学金申请 (scholarship_application)
-- ========================================

SET @batch_2025 = (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2025-1' LIMIT 1);
SET @batch_2024 = (SELECT id FROM evaluation_batch WHERE batch_code = 'BATCH-2024-2' LIMIT 1);

-- 2025年秋季申请
INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score, ranking, award_level, scholarship_amount, application_time, submit_time, status, tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT @batch_2025, s.id, 'APP-2025-006', NULL, NULL, NULL, NULL, '2025-02-18 10:00:00', '2025-02-18 11:00:00', 1, NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024006' AND @batch_2025 IS NOT NULL
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score, ranking, award_level, scholarship_amount, application_time, submit_time, status, tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT @batch_2025, s.id, 'APP-2025-011', NULL, NULL, NULL, NULL, '2025-02-20 09:00:00', '2025-02-20 10:00:00', 1, NULL, NULL, NULL, NULL, 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024011' AND @batch_2025 IS NOT NULL
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 2024年春季已完成申请（林十三获得一等奖）
INSERT INTO scholarship_application (batch_id, student_id, application_no, total_score, ranking, award_level, scholarship_amount, application_time, submit_time, status, tutor_opinion, tutor_id, tutor_review_time, college_opinion, version, deleted, create_time, update_time)
SELECT @batch_2024, s.id, 'APP-2024-006', 96.5, 1, 1, 10000.00, '2024-03-15 09:00:00', '2024-03-15 10:30:00', 3, '该生学术成果突出，多篇高水平论文，强烈推荐', @tutor_liu, '2024-03-20 10:00:00', '同意评定', 1, 0, NOW(), NOW()
FROM student_info s WHERE s.student_no = '2024011' AND @batch_2024 IS NOT NULL
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 11. 添加评审结果 (evaluation_result)
-- ========================================

INSERT INTO evaluation_result (batch_id, student_id, application_id, total_score, academic_score, research_score, competition_score, quality_score, course_score, moral_score, ranking, award_level, award_amount, publicity_date, result_status, deleted, create_time, update_time)
SELECT @batch_2024, s.id, sa.id, 96.5, 35.0, 30.0, 20.0, 11.5, 18.0, 13.0, 1, 1, 10000.00, '2024-05-20 10:00:00', 2, 0, NOW(), NOW()
FROM student_info s JOIN scholarship_application sa ON s.id = sa.student_id AND sa.application_no = 'APP-2024-006'
WHERE @batch_2024 IS NOT NULL
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 12. 添加系统通知 (sys_notification)
-- ========================================

INSERT INTO sys_notification (user_id, title, content, type, is_read, read_time, deleted, create_time, update_time)
SELECT u.id, '奖学金评定开始', '2025年秋季奖学金评定已启动，请在规定时间内完成申请。', 1, 0, NULL, 0, NOW(), NOW()
FROM sys_user u WHERE u.username = 'student6'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO sys_notification (user_id, title, content, type, is_read, read_time, deleted, create_time, update_time)
SELECT u.id, '奖学金评定开始', '2025年秋季奖学金评定已启动，请在规定时间内完成申请。', 1, 0, NULL, 0, NOW(), NOW()
FROM sys_user u WHERE u.username = 'student11'
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO sys_notification (user_id, title, content, type, is_read, read_time, deleted, create_time, update_time)
SELECT u.id, '奖学金评定结果通知', '恭喜您在2024年春季奖学金评定中获得一等奖学金！', 3, 1, '2024-05-21 09:00:00', 0, '2024-05-20 10:00:00', NOW()
FROM sys_user u WHERE u.username = 'student11'
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ========================================
-- 13. 添加操作日志 (sys_operation_log)
-- ========================================

INSERT INTO sys_operation_log (user_id, username, operation_type, operation_desc, request_uri, request_method, request_params, response_status, response_time, ip_address, create_time) VALUES
(1, 'admin', 'LOGIN', '管理员登录系统', '/api/auth/login', 'POST', '{"username":"admin"}', 200, 150, '192.168.1.100', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'student1', 'LOGIN', '学生登录系统', '/api/auth/login', 'POST', '{"username":"student1"}', 200, 120, '192.168.1.101', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'admin', 'BATCH_CREATE', '创建评定批次', '/api/batches', 'POST', '{"batchName":"2025年秋季奖学金评定"}', 200, 400, '192.168.1.100', DATE_SUB(NOW(), INTERVAL 5 DAY))
ON DUPLICATE KEY UPDATE user_id = user_id;

-- ========================================
-- 完成
-- ========================================
COMMIT;
