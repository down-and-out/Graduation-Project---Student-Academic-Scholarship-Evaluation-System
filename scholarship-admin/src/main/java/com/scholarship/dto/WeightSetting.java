package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 0, message = "课程成绩权重必须在 0-100 之间")
    @Max(value = 100, message = "课程成绩权重必须在 0-100 之间")
    @Schema(description = "课程成绩权重")
    private Integer courseWeight;

    /**
     * 科研成果权重（百分比）
     */
    @Min(value = 0, message = "科研成果权重必须在 0-100 之间")
    @Max(value = 100, message = "科研成果权重必须在 0-100 之间")
    @Schema(description = "科研成果权重")
    private Integer researchWeight;

    /**
     * 综合素质权重（百分比）
     */
    @Min(value = 0, message = "综合素质权重必须在 0-100 之间")
    @Max(value = 100, message = "综合素质权重必须在 0-100 之间")
    @Schema(description = "综合素质权重")
    private Integer comprehensiveWeight;

    /**
     * 竞赛成绩权重（百分比）
     */
    @Min(value = 0, message = "竞赛成绩权重必须在 0-100 之间")
    @Max(value = 100, message = "竞赛成绩权重必须在 0-100 之间")
    @Schema(description = "竞赛成绩权重")
    private Integer competitionWeight;

    @AssertTrue(message = "四项权重总和必须等于 100")
    public boolean isTotalWeightValid() {
        if (courseWeight == null || researchWeight == null || comprehensiveWeight == null || competitionWeight == null) {
            return true;
        }
        return courseWeight + researchWeight + comprehensiveWeight + competitionWeight == 100;
    }
}
