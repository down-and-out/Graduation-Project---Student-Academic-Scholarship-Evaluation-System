package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 条件规则 DTO
 * 用于定义奖项的附加条件
 */
@Data
@Schema(description = "条件规则")
public class Condition {

    /**
     * 条件类型
     * 例如：score（成绩）、paper（论文）、competition（竞赛）
     */
    @Schema(description = "条件类型")
    private String type;

    /**
     * 字段名
     */
    @Schema(description = "字段名")
    private String field;

    /**
     * 操作符
     * 例如：>、>=、==、<、<=
     */
    @Schema(description = "操作符")
    private String operator;

    /**
     * 比较值
     */
    @Schema(description = "比较值")
    private Object value;
}
