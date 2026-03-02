package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * <p>
 * 用于接收用户登录时提交的数据
 * 使用@Valid注解自动校验参数
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Schema(description = "登录请求")
public record LoginRequest(

        /**
         * 用户名
         */
        @NotBlank(message = "用户名不能为空")
        @Schema(description = "用户名", example = "admin")
        String username,

        /**
         * 密码
         */
        @NotBlank(message = "密码不能为空")
        @Schema(description = "密码", example = "123456")
        String password
) {
}
