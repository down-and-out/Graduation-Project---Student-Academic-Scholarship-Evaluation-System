package com.scholarship.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评定结果查询参数
 * <p>
 * 封装评定结果分页查询的参数，包含参数校验注解
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Schema(description = "评定结果查询参数")
public class EvaluationResultQueryParam {

    /**
     * 当前页
     */
    @Min(value = 1, message = "当前页必须大于等于1")
    @Schema(description = "当前页", example = "1")
    private Long current = 1L;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    /**
     * 批次 ID
     */
    @Schema(description = "批次 ID", example = "1")
    private Long batchId;

    /**
     * 学年
     */
    @Size(max = 20, message = "学年长度不能超过20个字符")
    @Schema(description = "学年", example = "2024")
    private String academicYear;

    /**
     * 学期（1-第一学期, 2-第二学期）
     */
    @Min(value = 1, message = "学期值必须在1-3之间")
    @Max(value = 3, message = "学期值必须在1-3之间")
    @Schema(description = "学期：1-第一学期, 2-第二学期, 3-夏季学期", example = "1")
    private Integer semester;

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID", example = "1")
    private Long studentId;

    /**
     * 状态（1-公示中, 2-已确定, 3-有异议）
     */
    @Min(value = 1, message = "状态值必须在1-3之间")
    @Max(value = 3, message = "状态值必须在1-3之间")
    @Schema(description = "状态：1-公示中, 2-已确定, 3-有异议", example = "1")
    private Integer status;

    /**
     * 关键词（学号/姓名模糊查询）
     */
    @Size(max = 50, message = "关键词长度不能超过50个字符")
    @Schema(description = "关键词（学号/姓名）", example = "张三")
    private String keyword;
}
