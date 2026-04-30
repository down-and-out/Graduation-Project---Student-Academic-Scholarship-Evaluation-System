-- ========================================
-- P0 并发保护：申请表唯一约束与查询索引
-- ========================================

-- 上线前先检查是否存在重复未删除申请
SELECT student_id, batch_id, COUNT(*) AS duplicate_count
FROM scholarship_application
WHERE deleted = 0
GROUP BY student_id, batch_id
HAVING COUNT(*) > 1;

-- 如上方查询存在结果，请先清理重复数据，再执行下面的 ALTER
ALTER TABLE scholarship_application
ADD CONSTRAINT uk_scholarship_application_student_batch_deleted
UNIQUE (student_id, batch_id, deleted);

ALTER TABLE scholarship_application
ADD INDEX idx_scholarship_application_batch_status (batch_id, status);

ALTER TABLE scholarship_application
ADD INDEX idx_scholarship_application_student_batch_deleted (student_id, batch_id, deleted);

ALTER TABLE evaluation_result
ADD INDEX idx_evaluation_result_batch_student (batch_id, student_id);

ALTER TABLE evaluation_task
ADD INDEX idx_evaluation_task_batch_type_status (batch_id, task_type, status);
