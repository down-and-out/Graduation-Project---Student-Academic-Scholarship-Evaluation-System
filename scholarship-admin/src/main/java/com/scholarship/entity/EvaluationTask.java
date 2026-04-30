package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评定任务实体类
 * <p>
 * 对应数据库表：evaluation_task
 * 用于追踪异步评定任务的状态与结果
 * </p>
 */
@Data
@TableName("evaluation_task")
public class EvaluationTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 任务类型，固定为 BATCH_EVALUATION
     */
    private String taskType;

    /**
     * 任务状态：0-等待执行 1-执行中 2-执行成功 3-执行失败
     */
    private Integer status;

    /**
     * 触发人ID
     */
    private Long triggeredBy;

    /**
     * 触发人姓名
     */
    private String triggeredByName;

    /**
     * 错误信息（任务失败时记录）
     */
    private String errorMessage;

    /**
     * 结果摘要JSON
     */
    private String resultSummaryJson;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 开始执行时间
     */
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    private LocalDateTime finishedAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
