-- ========================================
-- 修复 sys_user 表缺失字段
-- ========================================
-- 说明：添加 MyBatis-Plus 乐观锁需要的 version 字段
-- ========================================

-- 查看当前表结构
DESC sys_user;

-- 添加 version 字段（乐观锁版本号）
ALTER TABLE sys_user
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;

-- 添加 update_time 索引（可选）
-- ALTER TABLE sys_user ADD INDEX idx_update_time (update_time);

-- 验证修改结果
DESC sys_user;

-- 查看表结构
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '可为空',
    COLUMN_KEY AS '键',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND TABLE_NAME = 'sys_user'
ORDER BY ORDINAL_POSITION;

-- ========================================
-- 完成
-- ========================================
