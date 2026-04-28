package com.scholarship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Size(min = 8, max = 64, message = "新密码长度必须在 8-64 个字符之间")
    @Schema(description = "新密码（不填则使用系统默认密码）", example = "a123456789")
    private String password;
}
