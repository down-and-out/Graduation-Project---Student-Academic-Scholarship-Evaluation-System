package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权重设置 DTO
 * 对应 sys_setting 表中 weight 的 JSON 结构
 */
@Data
@Schema(description = "权重设置")
public class WeightSetting {

    /**
     * 课程成绩权重（百分比）
     */
    @Schema(description = "课程成绩权重")
    private Integer courseWeight;

    /**
     * 科研成果权重（百分比）
     */
    @Schema(description = "科研成果权重")
    private Integer researchWeight;

    /**
     * 综合素质权重（百分比）
     */
    @Schema(description = "综合素质权重")
    private Integer comprehensiveWeight;

    /**
     * 竞赛成绩权重（百分比）
     */
    @Schema(description = "竞赛成绩权重")
    private Integer competitionWeight;
}
