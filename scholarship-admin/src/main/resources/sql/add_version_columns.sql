-- ========================================
-- 为所有业务表添加 version 字段（乐观锁）
-- ========================================
-- 说明：MyBatis-Plus 乐观锁插件需要 version 字段
-- ========================================

-- 查看当前缺少 version 字段的表
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME NOT LIKE 'sys\_%'  -- 排除 sys_ 开头的系统表
GROUP BY TABLE_NAME
HAVING SUM(CASE WHEN COLUMN_NAME = 'version' THEN 1 ELSE 0 END) = 0;

-- 为 research_paper 表添加 version 字段
ALTER TABLE research_paper
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 research_patent 表添加 version 字段
ALTER TABLE research_patent
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 research_project 表添加 version 字段
ALTER TABLE research_project
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 competition_award 表添加 version 字段
ALTER TABLE competition_award
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 application_achievement 表添加 version 字段
ALTER TABLE application_achievement
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 scholarship_application 表添加 version 字段
ALTER TABLE scholarship_application
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 evaluation_result 表添加 version 字段
ALTER TABLE evaluation_result
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 result_appeal 表添加 version 字段
ALTER TABLE result_appeal
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 review_record 表添加 version 字段
ALTER TABLE review_record
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 student_info 表添加 version 字段
ALTER TABLE student_info
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 rule_category 表添加 version 字段
ALTER TABLE rule_category
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 score_rule 表添加 version 字段
ALTER TABLE score_rule
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 evaluation_batch 表添加 version 字段
ALTER TABLE evaluation_batch
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 为 sys_notification 表添加 version 字段
ALTER TABLE sys_notification
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 验证修改结果 - 检查所有表的 version 字段
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND COLUMN_NAME = 'version'
ORDER BY TABLE_NAME;

-- ========================================
-- 完成
-- ========================================
