-- ========================================
-- 研究生学业奖学金评定系统 - 测试数据初始化脚本
-- ========================================
-- 用于初始化完整的测试数据，包含所有功能的验证
-- ========================================

USE `scholarship`;

-- ========================================
-- 1. 角色数据
-- ========================================
-- 先清空现有数据（按依赖顺序）
DELETE FROM `sys_user_role`;
DELETE FROM `sys_role_permission`;
DELETE FROM `sys_permission`;
DELETE FROM `sys_role`;

DELETE FROM `sys_user`;

DELETE FROM `student_info`;
DELETE FROM `research_paper`;
DELETE FROM `research_patent`;
DELETE FROM `research_project`;
DELETE FROM `competition_award`;
DELETE FROM `scholarship_application`;
DELETE FROM `application_achievement`;
DELETE FROM `evaluation_batch`;
DELETE FROM `evaluation_result`;
DELETE FROM `result_appeal`;
DELETE FROM `review_record`;
DELETE FROM `rule_category`;
DELETE FROM `score_rule`;

-- 重置角色数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `sort_order`, `status`) VALUES
('ROLE_STUDENT', '学生', '研究生角色', 1, 1),
('ROLE_TUTOR', '导师', '导师角色', 2, 1),
('ROLE_ADMIN', '管理员', '系统管理员角色', 3, 1);

-- ========================================
-- 2. 用户数据
-- ========================================
-- 密码说明：
-- admin/123456 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- student1/123456 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- tutor1/123456 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `user_type`, `email`, `phone`, `status`) VALUES
-- 管理员
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 3, 'admin@scholarship.com', '13800138000', 1),
-- 导师用户
('tutor1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张教授', 2, 'tutor1@scholarship.com', '13800138001', 1),
('tutor2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李副教授', 2, 'tutor2@scholarship.com', '13800138002', 1),
-- 研究生用户
('student1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张三', 1, 'student1@scholarship.com', '13800138010', 1),
('student2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李四', 1, 'student2@scholarship.com', '13800138011', 1),
('student3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王五', 1, 'student3@scholarship.com', '13800138012', 1),
('student4', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵六', 1, 'student4@scholarship.com', '13800138013', 1),
('student5', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '钱七', 1, 'student5@scholarship.com', '13800138014', 1);

-- ========================================
-- 3. 用户角色关联
-- ========================================
-- 管理员 (ID=1) -> 管理员角色 (ID=3)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 3);
-- 导师 (ID=2,3) -> 导师角色 (ID=2)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (2, 2), (3, 2);
-- 学生 (ID=4,5,6,7,8) -> 学生角色 (ID=1)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (4, 1), (5, 1), (6, 1), (7, 1), (8, 1);

-- ========================================
-- 4. 权限数据
-- ========================================
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`) VALUES
-- 一级菜单
(0, 'DASHBOARD', '仪表盘', 1, '/dashboard', 'dashboard/index', 'House', 1, 1),
(0, 'APPLICATION', '奖学金申请', 1, '/application', 'application/index', 'Document', 2, 1),
(0, 'RESEARCH', '科研成果', 1, '/research', 'research/index', 'Medal', 3, 1),
(0, 'RULE', '评分规则管理', 1, '/rule', 'rule/index', 'Setting', 4, 1),
(0, 'BATCH', '评定批次管理', 1, '/batch', 'batch/index', 'Calendar', 5, 1),
(0, 'REVIEW', '评审管理', 1, '/review', 'review/index', 'Checked', 6, 1),
(0, 'RESULT', '评定结果', 1, '/result', 'result/index', 'Trophy', 7, 1),
(0, 'USER', '用户管理', 1, '/user', 'user/index', 'User', 8, 1),
(0, 'SYSTEM', '系统管理', 1, '/system', 'system/index', 'Tools', 9, 1);

-- ========================================
-- 5. 研究生信息
-- ========================================
INSERT INTO `student_info` (`user_id`, `student_no`, `name`, `gender`, `enrollment_year`, `education_level`, `training_mode`, `department`, `major`, `class_name`, `tutor_id`, `direction`, `political_status`, `nation`, `status`) VALUES
(4, '2024001', '张三', 1, 2024, 1, 1, '计算机学院', '计算机科学与技术', '研 2401 班', 2, '人工智能', '中共党员', '汉族', 1),
(5, '2024002', '李四', 0, 2024, 1, 1, '计算机学院', '软件工程', '研 2401 班', 2, '大数据', '共青团员', '汉族', 1),
(6, '2024003', '王五', 1, 2024, 2, 1, '计算机学院', '计算机科学与技术', '研 2401 班', 3, '网络安全', '中共党员', '汉族', 1),
(7, '2024004', '赵六', 0, 2024, 1, 1, '软件学院', '软件工程', '研 2401 班', 3, '云计算', '共青团员', '汉族', 1),
(8, '2024005', '钱七', 1, 2024, 2, 1, '计算机学院', '人工智能', '研 2401 班', 2, '机器学习', '中共党员', '汉族', 1);

-- ========================================
-- 6. 规则分类
-- ========================================
INSERT INTO `rule_category` (`category_code`, `category_name`, `description`, `sort_order`, `status`) VALUES
('ACADEMIC', '学术成果', '论文、专利、项目等学术成果评分规则', 1, 1),
('COURSE', '课程成绩', '研究生课程成绩评分规则', 2, 1),
('MORAL', '德育表现', '德育表现评分规则', 3, 1),
('COMPETITION', '学科竞赛', '学科竞赛获奖评分规则', 4, 1);

-- ========================================
-- 7. 评分规则（完整版）
-- ========================================
-- 论文类规则
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `max_score`, `level`, `condition`, `is_available`, `sort_order`) VALUES
(1, 'PAPER_SCI_1', 'SCI 一区论文', 1, 30.00, 60.00, 'SCI 一区', '第一作者', 1, 1),
(1, 'PAPER_SCI_2', 'SCI 二区论文', 1, 20.00, 40.00, 'SCI 二区', '第一作者', 1, 2),
(1, 'PAPER_SCI_3', 'SCI 三区论文', 1, 15.00, 30.00, 'SCI 三区', '第一作者', 1, 3),
(1, 'PAPER_SCI_4', 'SCI 四区论文', 1, 10.00, 20.00, 'SCI 四区', '第一作者', 1, 4),
(1, 'PAPER_EI', 'EI 论文', 1, 10.00, 20.00, 'EI', '第一作者', 1, 5),
(1, 'PAPER_CORE', '核心期刊', 1, 5.00, 10.00, '核心期刊', '第一作者', 1, 6);

-- 专利类规则
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `max_score`, `level`, `condition`, `is_available`, `sort_order`) VALUES
(1, 'PATENT_INVENT', '发明专利', 2, 15.00, 30.00, '发明专利', '第一发明人', 1, 1),
(1, 'PATENT_UTILITY', '实用新型专利', 2, 5.00, 10.00, '实用新型', '第一发明人', 1, 2),
(1, 'PATENT_DESIGN', '外观设计专利', 2, 3.00, 6.00, '外观设计', '第一设计人', 1, 3);

-- 项目类规则
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `max_score`, `level`, `condition`, `is_available`, `sort_order`) VALUES
(1, 'PROJECT_NATIONAL', '国家级项目', 3, 20.00, 40.00, '国家级', '主要参与人（前 3）', 1, 1),
(1, 'PROJECT_PROVINCE', '省部级项目', 3, 10.00, 20.00, '省部级', '主要参与人（前 3）', 1, 2),
(1, 'PROJECT_CITY', '厅局级项目', 3, 5.00, 10.00, '厅局级', '主要参与人（前 3）', 1, 3);

-- 竞赛类规则
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `max_score`, `level`, `condition`, `is_available`, `sort_order`) VALUES
(4, 'COMP_NATION_1', '国家级竞赛一等奖', 4, 25.00, 50.00, '国家级', '一等奖', 1, 1),
(4, 'COMP_NATION_2', '国家级竞赛二等奖', 4, 20.00, 40.00, '国家级', '二等奖', 1, 2),
(4, 'COMP_NATION_3', '国家级竞赛三等奖', 4, 15.00, 30.00, '国家级', '三等奖', 1, 3),
(4, 'COMP_PROVINCE_1', '省部级竞赛一等奖', 4, 15.00, 30.00, '省部级', '一等奖', 1, 4),
(4, 'COMP_PROVINCE_2', '省部级竞赛二等奖', 4, 10.00, 20.00, '省部级', '二等奖', 1, 5),
(4, 'COMP_PROVINCE_3', '省部级竞赛三等奖', 4, 6.00, 12.00, '省部级', '三等奖', 1, 6);

-- 课程成绩类规则
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `max_score`, `level`, `condition`, `is_available`, `sort_order`) VALUES
(2, 'COURSE_EXCELLENT', '课程优秀', 5, 20.00, 20.00, '优秀', '平均成绩≥90 分', 1, 1),
(2, 'COURSE_GOOD', '课程良好', 5, 15.00, 15.00, '良好', '平均成绩 80-89 分', 1, 2),
(2, 'COURSE_PASS', '课程及格', 5, 10.00, 10.00, '及格', '平均成绩 60-79 分', 1, 3);

-- 德育表现类规则
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `max_score`, `level`, `condition`, `is_available`, `sort_order`) VALUES
(3, 'MORAL_EXCELLENT', '德育优秀', 6, 15.00, 15.00, '优秀', '德育考核优秀', 1, 1),
(3, 'MORAL_GOOD', '德育良好', 6, 10.00, 10.00, '良好', '德育考核良好', 1, 2),
(3, 'MORAL_PASS', '德育合格', 6, 5.00, 5.00, '合格', '德育考核合格', 1, 3);

-- ========================================
-- 8. 评定批次
-- ========================================
INSERT INTO `evaluation_batch` (`batch_name`, `batch_year`, `batch_type`, `start_date`, `end_date`, `review_start_date`, `review_end_date`, `total_quota`, `scholarship_amount`, `status`) VALUES
('2024 年秋季奖学金评定', 2024, 2, '2024-09-01', '2024-09-30', '2024-10-01', '2024-10-15', 10, 50.00, 1),
('2025 年春季奖学金评定', 2025, 1, '2025-03-01', '2025-03-31', '2025-04-01', '2025-04-15', 15, 75.00, 0);

-- ========================================
-- 9. 科研论文（测试数据）
-- ========================================
INSERT INTO `research_paper` (`student_id`, `paper_title`, `authors`, `author_rank`, `journal_name`, `journal_level`, `impact_factor`, `publication_date`, `status`) VALUES
(4, '基于深度学习的人工智能算法研究', '张三，李四，王五', 1, '计算机学报', 6, 2.5, '2024-06-01', 1),
(4, '云计算环境下的数据安全保护机制', '张三，赵六', 1, '软件学报', 6, 1.8, '2024-08-15', 1),
(5, '大数据分析在金融风控中的应用', '李四，张三', 2, '计算机研究与发展', 6, 1.2, '2024-07-20', 1);

-- ========================================
-- 10. 科研专利（测试数据）
-- ========================================
INSERT INTO `research_patent` (`student_id`, `patent_name`, `patent_type`, `patent_no`, `inventors`, `inventor_rank`, `applicant`, `application_date`, `patent_status`, `status`) VALUES
(4, '一种基于机器学习的图像识别方法', 1, 'ZL202410001', '张三，李四', 1, '某某大学', '2024-01-15', 1, 1),
(6, '一种网络安全防护系统', 1, 'ZL202410002', '王五，赵六', 1, '某某大学', '2024-02-20', 2, 1);

-- ========================================
-- 11. 科研项目（测试数据）
-- ========================================
INSERT INTO `research_project` (`student_id`, `project_name`, `project_no`, `project_level`, `project_type`, `project_role`, `leader`, `start_date`, `end_date`, `funding`, `status`) VALUES
(4, '人工智能关键技术研究', '2024YFB001', 1, 1, 2, '张教授', '2024-01-01', '2026-12-31', 500.00, 1),
(5, '大数据分析平台研发', '2024YFB002', 2, 2, 1, '李副教授', '2024-03-01', '2025-12-31', 100.00, 1),
(6, '网络安全防护体系研究', '2024YFB003', 1, 1, 3, '张教授', '2024-01-01', '2026-12-31', 800.00, 1);

-- ========================================
-- 12. 学科竞赛（测试数据）
-- ========================================
INSERT INTO `competition_award` (`student_id`, `competition_name`, `award_level`, `award_rank`, `award_date`, `organizer`, `status`) VALUES
(4, '全国大学生计算机设计大赛', 1, 2, '2024-08-01', '教育部', 1),
(5, '全国软件设计大赛', 1, 3, '2024-09-01', '教育部', 1),
(7, '省级程序设计竞赛', 2, 1, '2024-07-01', '省教育厅', 1);

COMMIT;

-- ========================================
-- 测试账号汇总
-- ========================================
-- 管理员：admin/123456
-- 导师：tutor1/123456, tutor2/123456
-- 学生：student1/123456, student2/123456, student3/123456, student4/123456, student5/123456
-- ========================================
