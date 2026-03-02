package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统角色实体类
 * <p>
 * 对应数据库表：sys_role
 * 存储系统中的角色信息，如管理员、导师、研究生等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("sys_role")
@Schema(description = "系统角色")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "角色ID")
    private Long id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述")
    private String description;

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
