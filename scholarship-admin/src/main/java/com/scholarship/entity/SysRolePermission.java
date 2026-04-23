package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体。
 */
@Data
@TableName("sys_role_permission")
@Schema(description = "角色权限关联")
public class SysRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "关联 ID")
    private Long id;

    @NotNull(message = "角色 ID 不能为空")
    @Schema(description = "角色 ID")
    private Long roleId;

    @NotNull(message = "权限 ID 不能为空")
    @Schema(description = "权限 ID")
    private Long permissionId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
