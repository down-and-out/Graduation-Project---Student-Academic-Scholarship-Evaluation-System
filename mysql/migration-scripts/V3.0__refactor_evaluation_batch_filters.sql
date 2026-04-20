-- ========================================
-- 评定管理筛选与 evaluation_batch 结构收口
-- 生成日期: 2026-04-20
-- 版本: V3.0
-- ========================================

SET @dbname = 'scholarship';

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'status'
        ) AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'batch_status'
        )
        THEN 'ALTER TABLE evaluation_batch CHANGE COLUMN status batch_status TINYINT NOT NULL DEFAULT 1 COMMENT ''批次状态：1-未开始 2-申请中 3-评审中 4-公示中 5-已完成'''
        ELSE 'SELECT ''[skip] evaluation_batch.status already normalized'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'scholarship_amount'
        ) AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'total_amount'
        )
        THEN 'ALTER TABLE evaluation_batch CHANGE COLUMN scholarship_amount total_amount DECIMAL(10,2) DEFAULT NULL COMMENT ''奖学金总额'''
        ELSE 'SELECT ''[skip] evaluation_batch.scholarship_amount already normalized'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'start_date'
        )
        THEN 'ALTER TABLE evaluation_batch DROP COLUMN start_date'
        ELSE 'SELECT ''[skip] evaluation_batch.start_date does not exist'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'end_date'
        )
        THEN 'ALTER TABLE evaluation_batch DROP COLUMN end_date'
        ELSE 'SELECT ''[skip] evaluation_batch.end_date does not exist'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND INDEX_NAME = 'idx_status'
        )
        THEN 'ALTER TABLE evaluation_batch DROP INDEX idx_status'
        ELSE 'SELECT ''[skip] evaluation_batch.idx_status does not exist'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND INDEX_NAME = 'idx_start_date'
        )
        THEN 'ALTER TABLE evaluation_batch DROP INDEX idx_start_date'
        ELSE 'SELECT ''[skip] evaluation_batch.idx_start_date does not exist'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN NOT EXISTS (
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND INDEX_NAME = 'idx_batch_filter'
        )
        THEN 'ALTER TABLE evaluation_batch ADD INDEX idx_batch_filter (academic_year, semester, batch_status, create_time)'
        ELSE 'SELECT ''[skip] evaluation_batch.idx_batch_filter already exists'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND COLUMN_NAME = 'batch_type'
        ) AND NOT EXISTS (
            SELECT 1
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'evaluation_batch'
              AND INDEX_NAME = 'idx_batch_type_filter'
        )
        THEN 'ALTER TABLE evaluation_batch ADD INDEX idx_batch_type_filter (batch_type, batch_status, create_time)'
        ELSE 'SELECT ''[skip] evaluation_batch.idx_batch_type_filter not needed'' AS info'
    END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migration V3.0 completed successfully!' AS status;
