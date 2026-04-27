package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评分规则分类实体。
 */
@Data
@TableName("rule_category")
@Schema(description = "规则分类")
public class RuleCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "分类 ID")
    private Long id;

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @Schema(description = "分类编码")
    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Version
    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "状态：0-禁用 1-正常")
    private Integer status;

    @TableLogic
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
