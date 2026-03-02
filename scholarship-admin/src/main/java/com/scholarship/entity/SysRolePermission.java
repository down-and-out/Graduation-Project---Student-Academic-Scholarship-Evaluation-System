package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 * <p>
 * 对应数据库表：sys_role_permission
 * 存储角色与权限的多对多关联关系
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_role_id ON sys_role_permission(role_id);
 * CREATE INDEX idx_permission_id ON sys_role_permission(permission_id);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("sys_role_permission")
@Schema(description = "角色权限关联")
public class SysRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "关联 ID")
    private Long id;

    /**
     * 角色 ID
     */
    @NotNull(message = "角色 ID 不能为空")
    @Schema(description = "角色 ID")
    private Long roleId;

    /**
     * 权限 ID
     */
    @NotNull(message = "权限 ID 不能为空")
    @Schema(description = "权限 ID")
    private Long permissionId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;
}
