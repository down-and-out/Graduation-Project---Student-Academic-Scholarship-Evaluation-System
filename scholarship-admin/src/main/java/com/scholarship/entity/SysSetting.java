package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统设置实体类
 * <p>
 * 对应数据库表：sys_setting
 * 使用 Key-Value 模式存储系统配置，支持版本控制
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("sys_setting")
@Schema(description = "系统设置")
public class SysSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "设置 ID")
    private Long id;

    /**
     * 设置键，唯一标识
     * 例如：basic、weight、awards
     */
    @Schema(description = "设置键")
    private String settingKey;

    /**
     * 设置值，JSON 格式存储
     */
    @Schema(description = "设置值（JSON格式）")
    private String settingValue;

    /**
     * 版本号，支持多版本管理
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 是否生效：0-否，1-是
     */
    @Schema(description = "是否生效")
    private Integer isActive;

    /**
     * 设置项描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
