package com.scholarship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 * <p>
 * 返回给前端的登录成功响应数据，包含：
 * - JWT Token：用于后续请求的身份认证
 * - 用户基本信息：用户ID、用户名、真实姓名等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * JWT Token
     * 前端需要在请求头中携带此Token：Authorization: Bearer {token}
     */
    @Schema(description = "JWT Token")
    private String token;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 用户类型
     * 1-研究生 2-导师 3-管理员
     */
    @Schema(description = "用户类型：1-研究生 2-导师 3-管理员")
    private Integer userType;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;
}
