package com.scholarship.dto;

import com.scholarship.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

/**
 * 用户创建请求 DTO
 * 包含用户信息及学生档案额外字段
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    /**
     * 用户信息
     */
    @Valid
    @Schema(description = "用户信息")
    private SysUser user;

    /**
     * 专业（仅学生类型需要）
     */
    @Schema(description = "专业（仅学生类型需要）")
    private String major;
}
