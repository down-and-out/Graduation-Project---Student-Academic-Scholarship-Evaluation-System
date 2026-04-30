-- ============================================================
-- 高并发优化第二期：数据库索引补充
-- 针对高频查询场景添加复合/单列索引
-- ============================================================

-- 1. course_score: 评定计算按学生+学年+学期查询成绩（最频繁）
CREATE INDEX idx_course_score_stu_year_sem ON course_score (student_id, academic_year, semester);

-- 2. moral_performance: 德育评分按学生+学年+审核状态查询
CREATE INDEX idx_moral_perf_stu_year_status ON moral_performance (student_id, academic_year, audit_status);

-- 3. score_rule: 规则加载按类型+可用状态查询
CREATE INDEX idx_score_rule_type_available ON score_rule (rule_type, is_available);

-- 4. review_record: 审核记录按申请ID查询
CREATE INDEX idx_review_record_app_id ON review_record (application_id);

-- 5. application_achievement: 申请成果关联按申请ID查询
CREATE INDEX idx_app_achievement_app_id ON application_achievement (application_id);

-- 6. sys_setting: 系统设置按key+激活状态查询
CREATE INDEX idx_sys_setting_key_active ON sys_setting (setting_key, is_active);

-- 7. evaluation_batch: 批次按状态筛选（列表页/下拉选择）
CREATE INDEX idx_eval_batch_status ON evaluation_batch (batch_status);
