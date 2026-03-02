package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评分规则实体类
 * <p>
 * 对应数据库表：score_rule
 * 存储具体的评分规则配置
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("score_rule")
@Schema(description = "评分规则")
public class ScoreRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID")
    private Long categoryId;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码")
    private String ruleCode;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 规则类型：1-论文 2-专利 3-项目 4-竞赛 5-课程成绩 6-德育表现
     */
    @Schema(description = "规则类型")
    private Integer ruleType;

    /**
     * 分值
     */
    @Schema(description = "分值")
    private BigDecimal score;

    /**
     * 最高分（NULL表示无上限）
     */
    @Schema(description = "最高分")
    private BigDecimal maxScore;

    /**
     * 等级要求（如SCI一区、国家级等）
     */
    @Schema(description = "等级要求")
    private String level;

    /**
     * 评分条件说明
     */
    @Schema(description = "评分条件说明")
    @TableField("`condition`")
    private String condition;

    /**
     * 是否可用：0-不可用 1-可用
     */
    @Schema(description = "是否可用")
    private Integer isAvailable;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
