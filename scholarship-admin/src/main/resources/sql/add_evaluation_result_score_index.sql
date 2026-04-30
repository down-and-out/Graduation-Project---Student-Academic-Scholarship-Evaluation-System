-- 为 evaluation_result 表添加 (batch_id, total_score DESC) 联合索引
-- 覆盖 ORDER BY total_score DESC WHERE batch_id = ? 查询场景
CREATE INDEX IF NOT EXISTS idx_eval_result_batch_score ON evaluation_result (batch_id, total_score DESC);
