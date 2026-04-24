-- ========================================
-- 研究生学业奖学金评定系统 - 数据库初始化脚本
-- ========================================
-- 数据库: scholarship
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scholarship`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `scholarship`;

-- ========================================
-- 1. 用户与权限管理模块
-- ========================================

-- 用户表
-- 存储系统所有用户的基本信息
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（登录账号）',
    `password` VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型：1-研究生 2-导师 3-管理员',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 角色表
-- 存储系统角色信息
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID（主键）',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 用户角色关联表
-- 多对多关系：一个用户可以有多个角色
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 权限表
-- 存储系统权限/菜单信息
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID（主键）',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID（0为顶级）',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_type` TINYINT NOT NULL COMMENT '权限类型：1-菜单 2-按钮',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- 角色权限关联表
-- 多对多关系：一个角色可以有多个权限
CREATE TABLE `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ========================================
-- 2. 研究生信息管理模块
-- ========================================

-- 研究生信息表
-- 存储研究生的详细学籍信息
CREATE TABLE `student_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `student_no` VARCHAR(20) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT NOT NULL COMMENT '性别：0-女 1-男',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `enrollment_year` INT NOT NULL COMMENT '入学年份',
    `education_level` TINYINT NOT NULL COMMENT '学历层次：1-硕士 2-博士',
    `training_mode` TINYINT NOT NULL COMMENT '培养方式：1-全日制 2-非全日制',
    `department` VARCHAR(100) NOT NULL COMMENT '院系',
    `major` VARCHAR(100) NOT NULL COMMENT '专业',
    `class_name` VARCHAR(50) DEFAULT NULL COMMENT '班级',
    `tutor_id` BIGINT DEFAULT NULL COMMENT '导师ID（关联导师用户表）',
    `direction` VARCHAR(100) DEFAULT NULL COMMENT '研究方向',
    `political_status` VARCHAR(50) DEFAULT NULL COMMENT '政治面貌',
    `nation` VARCHAR(50) DEFAULT NULL COMMENT '民族',
    `native_place` VARCHAR(100) DEFAULT NULL COMMENT '籍贯',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '家庭住址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '学籍状态：0-休学 1-在读 2-毕业 3-退学',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_tutor_id` (`tutor_id`),
    KEY `idx_department` (`department`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研究生信息表';

-- ========================================
-- 3. 科研成果管理模块
-- ========================================

-- 科研论文表
-- 存储研究生发表的论文信息
CREATE TABLE `research_paper` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `paper_title` VARCHAR(200) NOT NULL COMMENT '论文标题',
    `authors` VARCHAR(500) NOT NULL COMMENT '作者列表（多个作者用逗号分隔）',
    `author_rank` TINYINT NOT NULL COMMENT '学生作者排名：1-第一作者 2-第二作者 3-通讯作者',
    `journal_name` VARCHAR(200) NOT NULL COMMENT '期刊名称',
    `journal_level` TINYINT NOT NULL COMMENT '期刊级别：1-SCI一区 2-SCI二区 3-SCI三区 4-SCI四区 5-EI 6-核心期刊 7-普通期刊',
    `impact_factor` DECIMAL(5,2) DEFAULT NULL COMMENT '影响因子',
    `publication_date` DATE DEFAULT NULL COMMENT '发表日期',
    `volume` VARCHAR(50) DEFAULT NULL COMMENT '卷号',
    `issue` VARCHAR(50) DEFAULT NULL COMMENT '期号',
    `pages` VARCHAR(50) DEFAULT NULL COMMENT '页码范围',
    `doi` VARCHAR(100) DEFAULT NULL COMMENT 'DOI编号',
    `indexing` VARCHAR(200) DEFAULT NULL COMMENT '收录情况（SCI/EI/ISTP等）',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL（论文PDF）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过',
    `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_publication_date` (`publication_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研论文表';

-- 科研专利表
-- 存储研究生申请的专利信息
CREATE TABLE `research_patent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `patent_name` VARCHAR(200) NOT NULL COMMENT '专利名称',
    `patent_type` TINYINT NOT NULL COMMENT '专利类型：1-发明专利 2-实用新型 3-外观设计',
    `patent_no` VARCHAR(50) DEFAULT NULL COMMENT '专利号',
    `inventors` VARCHAR(500) NOT NULL COMMENT '发明人列表',
    `inventor_rank` TINYINT NOT NULL COMMENT '学生发明人排名',
    `applicant` VARCHAR(200) NOT NULL COMMENT '申请人',
    `application_date` DATE DEFAULT NULL COMMENT '申请日期',
    `grant_date` DATE DEFAULT NULL COMMENT '授权日期',
    `patent_status` TINYINT NOT NULL DEFAULT 1 COMMENT '专利状态：1-申请中 2-已授权 3-已失效',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过',
    `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_application_date` (`application_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研专利表';

-- 科研项目表
-- 存储研究生参与的科研项目信息
CREATE TABLE `research_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `project_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `project_no` VARCHAR(50) DEFAULT NULL COMMENT '项目编号',
    `project_level` TINYINT NOT NULL COMMENT '项目级别：1-国家级 2-省部级 3-厅局级 4-横向项目',
    `project_type` TINYINT NOT NULL COMMENT '项目类型：1-基础研究 2-应用研究 3-开发研究',
    `project_role` TINYINT NOT NULL COMMENT '学生角色：1-项目负责人 2-主要参与人 3-一般参与人',
    `leader` VARCHAR(50) NOT NULL COMMENT '项目负责人',
    `participants` VARCHAR(500) DEFAULT NULL COMMENT '参与人员',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `funding` DECIMAL(12,2) DEFAULT NULL COMMENT '项目经费（万元）',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过',
    `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_date` (`start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研项目表';

-- 学科竞赛表
-- 存储研究生参加的学科竞赛获奖信息
CREATE TABLE `competition_award` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `competition_name` VARCHAR(200) NOT NULL COMMENT '竞赛名称',
    `award_level` TINYINT NOT NULL COMMENT '获奖级别：1-国家级 2-省部级 3-校级',
    `award_rank` TINYINT NOT NULL COMMENT '获奖等级：1-特等奖 2-一等奖 3-二等奖 4-三等奖 5-优秀奖',
    `award_date` DATE DEFAULT NULL COMMENT '获奖日期',
    `organizer` VARCHAR(200) DEFAULT NULL COMMENT '主办单位',
    `team_members` VARCHAR(500) DEFAULT NULL COMMENT '团队成员',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-导师审核通过 2-院系审核通过 3-审核不通过',
    `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学科竞赛表';

-- ========================================
-- 4. 评分规则管理模块
-- ========================================

-- 规则分类表
-- 存储评分规则的分类信息
CREATE TABLE `rule_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `category_code` VARCHAR(50) NOT NULL COMMENT '分类编码',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则分类表';

-- 评分规则表
-- 存储具体的评分规则
CREATE TABLE `score_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `category_id` BIGINT NOT NULL COMMENT '所属分类ID',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` TINYINT NOT NULL COMMENT '规则类型：1-论文 2-专利 3-项目 4-竞赛 5-课程成绩 6-德育表现',
    `score` DECIMAL(6,2) NOT NULL COMMENT '分值',
    `max_score` DECIMAL(6,2) DEFAULT NULL COMMENT '最高分（NULL表示无上限）',
    `level` VARCHAR(50) DEFAULT NULL COMMENT '等级要求（如SCI一区、国家级等）',
    `condition` VARCHAR(500) DEFAULT NULL COMMENT '评分条件说明',
    `is_available` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可用：0-不可用 1-可用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分规则表';

-- ========================================
-- 5. 评审与评定管理模块
-- ========================================

-- 评定批次表
-- 存储奖学金评定的批次信息（如2024年春季评定）
CREATE TABLE `evaluation_batch` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `batch_name` VARCHAR(100) NOT NULL COMMENT '批次名称',
    `batch_code` VARCHAR(50) DEFAULT NULL COMMENT '批次编码',
    `academic_year` VARCHAR(20) NOT NULL COMMENT '学年，如 2025-2026',
    `semester` TINYINT NOT NULL COMMENT '学期：1-第一学期 2-第二学期 3-全年',
    `batch_type` TINYINT DEFAULT NULL COMMENT '批次类型：1-春季奖学金 2-秋季奖学金 3-专项奖学金',
    `application_start_date` DATE NOT NULL COMMENT '申请开始日期',
    `application_end_date` DATE NOT NULL COMMENT '申请结束日期',
    `review_start_date` DATE NOT NULL COMMENT '评审开始日期',
    `review_end_date` DATE NOT NULL COMMENT '评审结束日期',
    `publicity_start_date` DATE DEFAULT NULL COMMENT '公示开始日期',
    `publicity_end_date` DATE DEFAULT NULL COMMENT '公示结束日期',
    `winner_count` INT DEFAULT NULL COMMENT '获奖人数',
    `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '奖学金总额（元）',
    `award_configs` JSON DEFAULT NULL COMMENT '批次奖项配置JSON',
    `selected_rule_ids` VARCHAR(1000) DEFAULT NULL COMMENT '批次参与评定的规则ID列表，逗号分隔',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '批次说明',
    `batch_status` TINYINT NOT NULL DEFAULT 1 COMMENT '批次状态：1-未开始 2-申请中 3-评审中 4-公示中 5-已完成',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_batch_filter` (`academic_year`, `semester`, `batch_status`, `create_time`),
    KEY `idx_batch_type_filter` (`batch_type`, `batch_status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评定批次表';

-- 奖学金申请表
-- 存储学生的奖学金申请记录
CREATE TABLE `scholarship_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `batch_id` BIGINT NOT NULL COMMENT '评定批次ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `application_no` VARCHAR(50) NOT NULL COMMENT '申请编号',
    `total_score` DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '总评分',
    `ranking` INT DEFAULT NULL COMMENT '排名',
    `award_level` TINYINT DEFAULT NULL COMMENT '获奖等级：1-一等奖学金 2-二等奖学金 3-三等奖学金 4-未获奖',
    `scholarship_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '奖学金金额',
    `application_time` DATETIME NOT NULL COMMENT '申请时间',
    `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已提交 2-审核中 3-评审完成 4-已公示 5-已发放 6-已拒绝',
    `tutor_opinion` VARCHAR(500) DEFAULT NULL COMMENT '导师意见',
    `tutor_id` BIGINT DEFAULT NULL COMMENT '导师审核人ID',
    `tutor_review_time` DATETIME DEFAULT NULL COMMENT '导师审核时间',
    `college_opinion` VARCHAR(500) DEFAULT NULL COMMENT '院系意见',
    `college_reviewer_id` BIGINT DEFAULT NULL COMMENT '院系审核人ID',
    `college_review_time` DATETIME DEFAULT NULL COMMENT '院系审核时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_application_no` (`application_no`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_ranking` (`ranking`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖学金申请表';

-- 申请成果关联表
-- 关联申请与具体的科研成果（用于计分）
CREATE TABLE `application_achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `application_id` BIGINT NOT NULL COMMENT '申请ID',
    `achievement_type` TINYINT NOT NULL COMMENT '成果类型：1-论文 2-专利 3-项目 4-竞赛',
    `achievement_id` BIGINT NOT NULL COMMENT '成果ID',
    `score` DECIMAL(6,2) NOT NULL COMMENT '得分',
    `rule_id` BIGINT DEFAULT NULL COMMENT '使用的评分规则ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_achievement` (`achievement_type`, `achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请成果关联表';

-- 评审记录表
-- 存储评审专家的评审记录
CREATE TABLE `review_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `application_id` BIGINT NOT NULL COMMENT '申请ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '评审人ID',
    `reviewer_name` VARCHAR(50) NOT NULL COMMENT '评审人姓名',
    `score` DECIMAL(6,2) NOT NULL COMMENT '评审分数',
    `opinion` VARCHAR(500) DEFAULT NULL COMMENT '评审意见',
    `review_time` DATETIME NOT NULL COMMENT '评审时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审记录表';

-- 评定结果表
-- 存储最终的评定结果
CREATE TABLE `evaluation_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `batch_id` BIGINT NOT NULL COMMENT '评定批次ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `application_id` BIGINT NOT NULL COMMENT '申请ID',
    `total_score` DECIMAL(6,2) NOT NULL COMMENT '总分',
    `academic_score` DECIMAL(6,2) DEFAULT 0 COMMENT '学术成果得分',
    `course_score` DECIMAL(6,2) DEFAULT 0 COMMENT '课程成绩得分',
    `moral_score` DECIMAL(6,2) DEFAULT 0 COMMENT '德育得分',
    `ranking` INT NOT NULL COMMENT '排名',
    `award_level` TINYINT NOT NULL COMMENT '获奖等级：0-未获奖 1-一等奖学金 2-二等奖学金 3-三等奖学金',
    `scholarship_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '奖学金金额',
    `publish_date` DATE DEFAULT NULL COMMENT '公示日期',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待公示 1-公示中 2-公示完成 3-已完成',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_student` (`batch_id`, `student_id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_ranking` (`ranking`),
    KEY `idx_award_level` (`award_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评定结果表';

-- 结果异议表
-- 存储学生对评定结果的异议申诉
CREATE TABLE `result_appeal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `result_id` BIGINT NOT NULL COMMENT '结果ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `appeal_reason` VARCHAR(500) NOT NULL COMMENT '申诉理由',
    `appeal_content` TEXT NOT NULL COMMENT '申诉内容',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理 1-处理中 2-已处理 3-已驳回',
    `handle_opinion` VARCHAR(500) DEFAULT NULL COMMENT '处理意见',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_result_id` (`result_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结果异议表';

-- ========================================
-- 6. 系统日志模块
-- ========================================

-- 操作日志表
-- 记录用户的重要操作日志
CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `method` VARCHAR(200) NOT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT NOT NULL COMMENT '状态：0-失败 1-成功',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `execute_time` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 系统通知表
-- 存储系统通知消息
CREATE TABLE `sys_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID（主键）',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `notification_type` TINYINT NOT NULL COMMENT '通知类型：1-系统通知 2-审核通知 3-评定通知',
    `receiver_type` TINYINT NOT NULL COMMENT '接收者类型：1-全部用户 2-指定用户 3-指定角色',
    `receiver_id` BIGINT DEFAULT NULL COMMENT '接收者ID（指定用户时使用）',
    `role_id` BIGINT DEFAULT NULL COMMENT '角色ID（指定角色时使用）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ========================================
-- 初始化数据
-- ========================================

-- 初始化角色数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `sort_order`) VALUES
('ROLE_STUDENT', '研究生', '研究生角色', 1),
('ROLE_TUTOR', '导师', '导师角色', 2),
('ROLE_ADMIN', '管理员', '系统管理员角色', 3);

-- 初始化管理员用户
-- 密码为：123456（BCrypt加密后的值）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `user_type`, `email`, `phone`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 3, 'admin@scholarship.com', '13800138000');

-- 关联管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 3);

-- 初始化规则分类
INSERT INTO `rule_category` (`category_code`, `category_name`, `description`, `sort_order`) VALUES
('ACADEMIC', '学术成果', '论文、专利、项目等学术成果评分规则', 1),
('COURSE', '课程成绩', '研究生课程成绩评分规则', 2),
('MORAL', '德育表现', '德育表现评分规则', 3),
('COMPETITION', '学科竞赛', '学科竞赛获奖评分规则', 4);

-- 初始化部分评分规则（论文类）
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `level`, `condition`, `sort_order`) VALUES
(1, 'PAPER_SCI_1', 'SCI一区论文', 1, 30.00, 'SCI一区', '第一作者', 1),
(1, 'PAPER_SCI_2', 'SCI二区论文', 1, 20.00, 'SCI二区', '第一作者', 2),
(1, 'PAPER_SCI_3', 'SCI三区论文', 1, 15.00, 'SCI三区', '第一作者', 3),
(1, 'PAPER_EI', 'EI论文', 1, 10.00, 'EI', '第一作者', 4),
(1, 'PAPER_CORE', '核心期刊', 1, 5.00, '核心期刊', '第一作者', 5);

-- 初始化评分规则（竞赛类）
INSERT INTO `score_rule` (`category_id`, `rule_code`, `rule_name`, `rule_type`, `score`, `level`, `condition`, `sort_order`) VALUES
(4, 'COMP_NATION_1', '国家级竞赛一等奖', 4, 25.00, '国家级', '一等奖', 1),
(4, 'COMP_NATION_2', '国家级竞赛二等奖', 4, 20.00, '国家级', '二等奖', 2),
(4, 'COMP_PROVINCE_1', '省部级竞赛一等奖', 4, 15.00, '省部级', '一等奖', 3),
(4, 'COMP_PROVINCE_2', '省部级竞赛二等奖', 4, 10.00, '省部级', '二等奖', 4);

COMMIT;
