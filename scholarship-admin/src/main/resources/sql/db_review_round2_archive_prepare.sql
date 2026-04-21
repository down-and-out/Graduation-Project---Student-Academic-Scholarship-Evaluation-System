-- 数据库联审第二轮：归档表迁出准备脚本
-- 说明：
-- 1. 本脚本默认不直接删除主库中的 audit_/bak_ 表
-- 2. 先创建归档库 scholarship_archive
-- 3. 输出可执行的 CREATE TABLE LIKE / INSERT SELECT / 校验 SQL
-- 4. 等复制与校验通过后，再人工执行 drop 模板

CREATE DATABASE IF NOT EXISTS scholarship_archive
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- 生成归档表结构复制语句
SELECT CONCAT(
  'CREATE TABLE IF NOT EXISTS scholarship_archive.`', table_name,
  '` LIKE `', DATABASE(), '`.`', table_name, '`;'
) AS create_table_sql
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND (table_name LIKE 'audit\\_20260419\\_%' ESCAPE '\\'
    OR table_name LIKE 'bak\\_%' ESCAPE '\\')
ORDER BY table_name;

-- 生成归档数据复制语句
SELECT CONCAT(
  'INSERT INTO scholarship_archive.`', table_name,
  '` SELECT * FROM `', DATABASE(), '`.`', table_name, '`;'
) AS copy_data_sql
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND (table_name LIKE 'audit\\_20260419\\_%' ESCAPE '\\'
    OR table_name LIKE 'bak\\_%' ESCAPE '\\')
ORDER BY table_name;

-- 生成复制后的行数核对语句
SELECT CONCAT(
  'SELECT ''', table_name, ''' AS table_name, ',
  '(SELECT COUNT(*) FROM `', DATABASE(), '`.`', table_name, '`) AS source_count, ',
  '(SELECT COUNT(*) FROM scholarship_archive.`', table_name, '`) AS archive_count;'
) AS verify_sql
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND (table_name LIKE 'audit\\_20260419\\_%' ESCAPE '\\'
    OR table_name LIKE 'bak\\_%' ESCAPE '\\')
ORDER BY table_name;

-- 生成确认无误后的删除模板（本脚本不会自动执行）
SELECT CONCAT(
  'DROP TABLE `', DATABASE(), '`.`', table_name, '`;'
) AS drop_source_sql
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND (table_name LIKE 'audit\\_20260419\\_%' ESCAPE '\\'
    OR table_name LIKE 'bak\\_%' ESCAPE '\\')
ORDER BY table_name;
