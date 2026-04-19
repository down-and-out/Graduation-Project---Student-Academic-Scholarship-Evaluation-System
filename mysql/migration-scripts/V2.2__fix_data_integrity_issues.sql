-- ========================================-- Flyway 数据库版本管理 - V2.2 修复数据完整性问题-- ========================================-- 说明：修复奖学金评定系统中的数据完整性问题-- 创建时间：2026-03-08-- 问题列表：-- 1. score_rule 关联到不存在的 category_id-- 2. course_score 关联到不存在的 student_id-- 3. research_paper 关联到不存在的 student_id-- 4. moral_performance 关联到不存在的 student_id-- 5. evaluation_batch 日期逻辑错误-- 6. course_score 中 GPA 与成绩不匹配-- ========================================

USE scholarship;

-- ========================================-- 步骤 0: 查看当前问题状态（调试用，执行前检查）-- ========================================

-- 检查 score_rule 中的无效 category_id
-- SELECT sr.id, sr.name, sr.category_id, rc.id as valid_category
-- FROM score_rule sr
-- LEFT JOIN rule_category rc ON sr.category_id = rc.id
-- WHERE rc.id IS NULL;

-- 检查 course_score 中的无效 student_id
-- SELECT cs.id, cs.student_id, si.id as valid_student
-- FROM course_score cs
-- LEFT JOIN student_info si ON cs.student_id = si.id
-- WHERE si.id IS NULL;

-- 检查 research_paper 中的无效 student_id
-- SELECT rp.id, rp.student_id, si.id as valid_student
-- FROM research_paper rp
-- LEFT JOIN student_info si ON rp.student_id = si.id
-- WHERE si.id IS NULL;

-- 检查 moral_performance 中的无效 student_id
-- SELECT mp.id, mp.student_id, si.id as valid_student
-- FROM moral_performance mp
-- LEFT JOIN student_info si ON mp.student_id = si.id
-- WHERE si.id IS NULL;

-- ========================================-- 步骤 1: 修复 score_rule 分类关联-- ========================================

-- 首先检查 rule_category 现有数据，确保有有效的分类存在
-- 如果不存在任何分类，则创建默认分类
INSERT INTO rule_category (category_name, category_code, description, sort_order, version, status, deleted, create_time, update_time)
SELECT '默认分类', 'DEFAULT', '系统自动创建的默认分类', 99, 1, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rule_category WHERE id = 1);

-- 将无效的 category_id 更新为有效的分类（假设使用ID=1的分类）
UPDATE score_rule sr
SET category_id = (SELECT id FROM rule_category ORDER BY id LIMIT 1)
WHERE sr.category_id NOT IN (SELECT id FROM rule_category);

-- ========================================-- 步骤 2: 清理无效的外键关联记录 - course_score-- ========================================

-- 删除 course_score 中 student_id 无效的记录
DELETE FROM course_score
WHERE student_id NOT IN (SELECT id FROM student_info);

-- ========================================-- 步骤 3: 清理无效的外键关联记录 - research_paper-- ========================================

-- 删除 research_paper 中 student_id 无效的记录
DELETE FROM research_paper
WHERE student_id NOT IN (SELECT id FROM student_info);

-- ========================================-- 步骤 4: 清理无效的外键关联记录 - moral_performance-- ========================================

-- 删除 moral_performance 中 student_id 无效的记录
DELETE FROM moral_performance
WHERE student_id NOT IN (SELECT id FROM student_info);

-- ========================================-- 步骤 5: 修复批次日期逻辑-- ========================================

-- 批次 BATCH-2024-2: 确保评审开始日期在申请结束日期之后
-- 原数据：申请 2024-03-01 至 2024-04-30，评审 2024-05-01 至 2024-05-15
-- 当前数据逻辑正确，无需修改

-- 批次 BATCH-2025-1: 确保评审开始日期在申请结束日期之后
-- 原数据：申请 2025-09-01 至 2025-10-31，评审 2025-11-01 至 2025-11-15
-- 当前数据逻辑正确，无需修改

-- 修正任何日期逻辑错误的批次（如果有的话）
UPDATE evaluation_batch
SET review_start_date = DATE_ADD(application_end_date, INTERVAL 1 DAY),
    review_end_date = DATE_ADD(application_end_date, INTERVAL 15 DAY)
WHERE review_start_date < application_end_date;

-- ========================================-- 步骤 6: 修正 GPA 计算-- ========================================

-- 根据中国高校常见的4.0 GPA标准重新计算 GPA
-- 90-100: 4.0
-- 85-89: 3.7
-- 82-84: 3.3
-- 78-81: 3.0
-- 75-77: 2.7
-- 72-74: 2.3
-- 68-71: 2.0
-- 64-67: 1.5
-- 60-63: 1.0
-- 0-59: 0.0

UPDATE course_score
SET gpa = CASE
    WHEN score >= 90 THEN 4.0
    WHEN score >= 85 THEN 3.7
    WHEN score >= 82 THEN 3.3
    WHEN score >= 78 THEN 3.0
    WHEN score >= 75 THEN 2.7
    WHEN score >= 72 THEN 2.3
    WHEN score >= 68 THEN 2.0
    WHEN score >= 64 THEN 1.5
    WHEN score >= 60 THEN 1.0
    ELSE 0.0
END
WHERE score IS NOT NULL;

-- ========================================-- 验证修复结果-- ========================================

-- 验证 1: score_rule 是否还有无效的分类关联
-- SELECT 'Invalid score_rule categories' as check_item, COUNT(*) as count
-- FROM score_rule sr
-- LEFT JOIN rule_category rc ON sr.category_id = rc.id
-- WHERE rc.id IS NULL;

-- 验证 2: course_score 是否还有无效的学生关联
-- SELECT 'Invalid course_score students' as check_item, COUNT(*) as count
-- FROM course_score cs
-- LEFT JOIN student_info si ON cs.student_id = si.id
-- WHERE si.id IS NULL;

-- 验证 3: research_paper 是否还有无效的学生关联
-- SELECT 'Invalid research_paper students' as check_item, COUNT(*) as count
-- FROM research_paper rp
-- LEFT JOIN student_info si ON rp.student_id = si.id
-- WHERE si.id IS NULL;

-- 验证 4: moral_performance 是否还有无效的学生关联
-- SELECT 'Invalid moral_performance students' as check_item, COUNT(*) as count
-- FROM moral_performance mp
-- LEFT JOIN student_info si ON mp.student_id = si.id
-- WHERE si.id IS NULL;

-- 验证 5: evaluation_batch 日期逻辑是否正确
-- SELECT 'Batches with invalid date logic' as check_item, COUNT(*) as count
-- FROM evaluation_batch
-- WHERE review_start_date < application_end_date;

-- ========================================-- 完成-- ========================================
COMMIT;
