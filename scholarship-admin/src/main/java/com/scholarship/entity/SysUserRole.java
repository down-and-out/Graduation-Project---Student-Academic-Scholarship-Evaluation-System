package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * <p>
 * 对应数据库表：sys_user_role
 * 存储用户与角色的多对多关联关系
 * </p>
 *
 * 数据库索引建议：
 * <pre>
 * CREATE INDEX idx_user_id ON sys_user_role(user_id);
 * CREATE INDEX idx_role_id ON sys_user_role(role_id);
 * </pre>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联")
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "关联 ID")
    private Long id;

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 角色 ID
     */
    @NotNull(message = "角色 ID 不能为空")
    @Schema(description = "角色 ID")
    private Long roleId;

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
