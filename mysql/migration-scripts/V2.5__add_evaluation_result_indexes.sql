-- 为 evaluation_result 表添加索引以优化查询性能
-- 创建时间: 2026-04-07
-- 说明: 为模糊查询字段和常用筛选条件添加索引

-- 为模糊查询字段添加索引
ALTER TABLE `evaluation_result` ADD INDEX `idx_student_no` (`student_no`);
ALTER TABLE `evaluation_result` ADD INDEX `idx_student_name` (`student_name`);
ALTER TABLE `evaluation_result` ADD INDEX `idx_result_status` (`result_status`);

-- 添加复合索引优化常用查询
-- 适用于按批次、状态筛选并按总分排序的场景
ALTER TABLE `evaluation_result` ADD INDEX `idx_batch_status_score` (`batch_id`, `result_status`, `total_score`);

-- 添加学生 ID 索引（用于关联查询）
ALTER TABLE `evaluation_result` ADD INDEX `idx_student_id` (`student_id`);

-- 添加批次 ID 单独索引（用于仅按批次筛选的场景）
ALTER TABLE `evaluation_result` ADD INDEX `idx_batch_id` (`batch_id`);
