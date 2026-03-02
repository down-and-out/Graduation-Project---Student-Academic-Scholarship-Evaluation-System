package com.scholarship.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * <p>
 * 对应数据库表：sys_user
 * 存储系统所有用户的基本信息，包括研究生、导师和管理员
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用户 ID")
    private Long id;

    /**
     * 用户名（登录账号）
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码（BCrypt 加密）
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 用户类型
     * 1-研究生 2-导师 3-管理员
     */
    @NotNull(message = "用户类型不能为空")
    @Schema(description = "用户类型：1-研究生 2-导师 3-管理员")
    private Integer userType;

    /**
     * 电子邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "电子邮箱")
    private String email;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 头像 URL
     */
    @Schema(description = "头像 URL")
    private String avatar;

    /**
     * 状态
     * 0-禁用 1-正常
     */
    @NotNull(message = "用户状态不能为空")
    @Schema(description = "状态：0-禁用 1-正常")
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;

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
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
