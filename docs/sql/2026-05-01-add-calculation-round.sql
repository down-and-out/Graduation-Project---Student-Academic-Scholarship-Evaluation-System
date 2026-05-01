-- 评定结果版本化写入：新增计算轮次字段
-- 用于支持分批覆盖写入，避免清空-重写之间的不一致窗口期
ALTER TABLE evaluation_result
    ADD COLUMN calculation_round INT DEFAULT 1 COMMENT '计算轮次，用于版本化写入' AFTER award_amount;
