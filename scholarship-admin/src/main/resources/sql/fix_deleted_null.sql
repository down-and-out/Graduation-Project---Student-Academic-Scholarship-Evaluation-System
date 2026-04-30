-- ================================================================
-- 逻辑删除字段审计脚本：修复 deleted IS NULL 并加固 NOT NULL 约束
-- ================================================================
-- 用途：新环境部署后一键执行，确保所有 @TableLogic 表的 deleted 字段
--       数据完整且具备正确的 NOT NULL DEFAULT 0 约束。
-- 覆盖：全部 18 张使用 @TableLogic 的表
-- ================================================================

-- 1. research_patent (科研成果-专利)
UPDATE research_patent SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE research_patent MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 2. research_project (科研成果-项目)
UPDATE research_project SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE research_project MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 3. research_paper (科研成果-论文)
UPDATE research_paper SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE research_paper MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 4. competition_award (竞赛获奖)
UPDATE competition_award SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE competition_award MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 5. course_score (课程成绩)
UPDATE course_score SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE course_score MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 6. moral_performance (德育表现)
UPDATE moral_performance SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE moral_performance MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 7. scholarship_application (奖学金申请)
UPDATE scholarship_application SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE scholarship_application MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 8. evaluation_batch (评审批次)
UPDATE evaluation_batch SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE evaluation_batch MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 9. evaluation_result (评审结果)
UPDATE evaluation_result SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE evaluation_result MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 10. result_appeal (结果申诉)
UPDATE result_appeal SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE result_appeal MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 11. review_record (评审记录)
UPDATE review_record SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE review_record MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 12. rule_category (规则分类)
UPDATE rule_category SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE rule_category MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 13. score_rule (评分规则)
UPDATE score_rule SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE score_rule MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 14. student_info (学生信息)
UPDATE student_info SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE student_info MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 15. sys_notification (系统通知)
UPDATE sys_notification SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE sys_notification MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 16. sys_permission (系统权限)
UPDATE sys_permission SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE sys_permission MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 17. sys_role (系统角色)
UPDATE sys_role SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE sys_role MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;

-- 18. sys_user (系统用户)
UPDATE sys_user SET deleted = 0 WHERE deleted IS NULL;
ALTER TABLE sys_user MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;
