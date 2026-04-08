package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评定结果查询参数
 * <p>
 * 用于分页查询评定结果
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EvaluationResultQuery extends PageQuery {

    /**
     * 批次 ID
     */
    private Long batchId;

    /**
     * 学生 ID
     */
    private Long studentId;

    /**
     * 结果状态：1-公示中 2-已确定 3-有异议
     */
    private Integer resultStatus;

    /**
     * 获奖等级：1-特等 2-一等 3-二等 4-三等 5-未获奖
     */
    private Integer awardLevel;

    /**
     * 关键词（学号/姓名）
     */
    private String keyword;
}
