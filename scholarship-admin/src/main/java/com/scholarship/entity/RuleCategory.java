package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则分类实体类
 * <p>
 * 对应数据库表：rule_category
 * 存储评分规则的分类信息，如科研成果类、课程成绩类等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("rule_category")
@Schema(description = "规则分类")
public class RuleCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 分类编码
     */
    @Schema(description = "分类编码")
    private String categoryCode;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述")
    private String description;

    /**
     * 父分类ID（0表示顶级分类）
     */
    @Schema(description = "父分类ID")
    private Long parentId;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号")
    private Integer sortOrder;

    /**
     * 状态
     * 0-禁用 1-正常
     */
    @Schema(description = "状态：0-禁用 1-正常")
    private Integer status;

    /**
     * 逻辑删除标记
     * 0-未删除 1-已删除
     */
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
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
