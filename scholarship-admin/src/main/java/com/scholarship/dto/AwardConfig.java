package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 奖项配置 DTO
 * 对应 sys_setting 表中 awards 的 JSON 结构
 */
@Data
@Schema(description = "奖项配置")
public class AwardConfig {

    /**
     * 配置版本号
     */
    @Schema(description = "配置版本号")
    private String version;

    /**
     * 配置名称
     */
    @Schema(description = "配置名称")
    private String name;

    /**
     * 奖项规则列表
     */
    @Schema(description = "奖项规则列表")
    private List<AwardRule> rules;

    /**
     * 分配策略
     * 例如：scorePriority（分数优先）
     */
    @Schema(description = "分配策略")
    private String allocationStrategy;
}
