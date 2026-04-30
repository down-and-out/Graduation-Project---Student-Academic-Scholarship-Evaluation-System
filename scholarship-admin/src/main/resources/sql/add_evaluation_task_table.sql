-- ========================================
-- 异步评定任务表：追踪批次评定任务状态
-- ========================================

CREATE TABLE IF NOT EXISTS `evaluation_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `batch_id` BIGINT NOT NULL COMMENT '批次ID',
    `task_type` VARCHAR(32) NOT NULL DEFAULT 'BATCH_EVALUATION' COMMENT '任务类型',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态：0-等待执行 1-执行中 2-执行成功 3-执行失败',
    `active_flag` TINYINT GENERATED ALWAYS AS (
        CASE
            WHEN `status` IN (0, 1) THEN 1
            ELSE NULL
        END
    ) STORED COMMENT '活动任务标记：仅等待执行/执行中为1',
    `triggered_by` BIGINT NULL COMMENT '触发人ID',
    `triggered_by_name` VARCHAR(64) NULL COMMENT '触发人姓名',
    `error_message` VARCHAR(1024) NULL COMMENT '错误信息',
    `result_summary_json` TEXT NULL COMMENT '结果摘要JSON',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `started_at` DATETIME NULL COMMENT '开始执行时间',
    `finished_at` DATETIME NULL COMMENT '完成时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_evaluation_task_active` (`batch_id`, `task_type`, `active_flag`),
    INDEX `idx_status` (`status`),
    INDEX `idx_task_type` (`task_type`),
    INDEX `idx_batch_type_status` (`batch_id`, `task_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步评定任务表';
