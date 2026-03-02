package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统权限实体类
 * <p>
 * 对应数据库表：sys_permission
 * 存储系统中的权限信息，用于控制用户访问资源的权限
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("sys_permission")
@Schema(description = "系统权限")
public class SysPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "权限ID")
    private Long id;

    /**
     * 父权限ID（0表示顶级权限）
     */
    @Schema(description = "父权限ID")
    private Long parentId;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称")
    private String permissionName;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码")
    private String permissionCode;

    /**
     * 权限类型
     * 1-目录 2-菜单 3-按钮
     */
    @Schema(description = "权限类型：1-目录 2-菜单 3-按钮")
    private Integer permissionType;

    /**
     * 请求路径
     */
    @Schema(description = "请求路径")
    private String path;

    /**
     * 权限标识（用于Spring Security的@PreAuthorize）
     */
    @Schema(description = "权限标识")
    private String permission;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

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
