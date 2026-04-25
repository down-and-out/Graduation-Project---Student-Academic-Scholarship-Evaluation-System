package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程成绩导入结果。
 */
@Data
@Schema(description = "课程成绩导入结果")
public class CourseScoreImportResult {

    @Schema(description = "新增数量")
    private Integer imported;

    @Schema(description = "更新数量")
    private Integer updated;

    @Schema(description = "跳过数量")
    private Integer skipped;

    @Schema(description = "结果消息")
    private String message;
}
