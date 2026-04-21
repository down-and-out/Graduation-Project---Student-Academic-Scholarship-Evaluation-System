-- 数据库联审第二轮：review_record 结构收口
-- 目标：
-- 1. 保留 review_score / review_comment 作为唯一评审分数与意见字段
-- 2. 下线历史重复字段 score / opinion
-- 3. 仅建议在 review_record 仍为空表时执行

SELECT COUNT(*) AS review_record_count FROM review_record;

ALTER TABLE review_record
  DROP COLUMN score,
  DROP COLUMN opinion;
