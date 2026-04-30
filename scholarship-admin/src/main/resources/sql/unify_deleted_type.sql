-- ================================================================
-- 统一 deleted 字段类型：将 int 改为 tinyint
-- ================================================================
-- 背景：research_patent 和 research_project 的 deleted 字段为 int，
--       其余 16 张表的 deleted 字段为 tinyint，统一为 tinyint。
-- 影响：tinyint(1 字节) 比 int(4 字节) 更节省存储空间，语义也更清晰。
-- ================================================================

ALTER TABLE research_patent MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE research_project MODIFY COLUMN deleted TINYINT NOT NULL DEFAULT 0;
