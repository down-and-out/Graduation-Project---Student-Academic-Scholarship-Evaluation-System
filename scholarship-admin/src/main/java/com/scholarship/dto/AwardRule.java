package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 奖项规则 DTO
 */
@Data
@Schema(description = "奖项规则")
public class AwardRule {

    /**
     * 规则唯一标识
     */
    @Schema(description = "规则ID")
    private String id;

    /**
     * 奖项名称
     */
    @Schema(description = "奖项名称")
    private String name;

    /**
     * 名额比例（百分比）
     */
    @Schema(description = "名额比例")
    private Integer ratio;

    /**
     * 奖励金额（元）
     */
    @Schema(description = "奖励金额")
    private Integer amount;

    /**
     * 分数范围
     */
    @Schema(description = "分数范围")
    private ScoreRange scoreRange;

    /**
     * 附加条件列表
     */
    @Schema(description = "附加条件")
    private List<Condition> conditions;

    /**
     * 优先级，数字越小优先级越高
     */
    @Schema(description = "优先级")
    private Integer priority;
}
