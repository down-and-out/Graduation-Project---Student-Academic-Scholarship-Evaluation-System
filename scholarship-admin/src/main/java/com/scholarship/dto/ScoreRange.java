package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分数范围 DTO
 */
@Data
@Schema(description = "分数范围")
public class ScoreRange {

    /**
     * 最低分数
     */
    @Schema(description = "最低分数")
    private Double min;

    /**
     * 最高分数
     */
    @Schema(description = "最高分数")
    private Double max;
}
