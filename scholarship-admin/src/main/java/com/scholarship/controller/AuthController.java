package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.common.util.UsernameValidator;
import com.scholarship.common.util.PasswordValidator;
import com.scholarship.common.util.RsaUtils;
import com.scholarship.config.JwtUtil;
import com.scholarship.dto.LoginRequest;
import com.scholarship.entity.SysUser;
import com.scholarship.security.LoginUser;
import com.scholarship.service.LoginAttemptService;
import com.scholarship.service.TokenBlacklistService;
import com.scholarship.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 处理用户认证相关的请求，包括：
 * - 登录：验证用户名密码，生成并返回 JWT Token
 * - 登出：清除当前用户的认证信息
 * - 获取当前用户信息
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "01-认证管理", description = "用户登录、登出、获取当前用户信息等认证相关接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 用户登录接口
     * <p>
     * 登录流程：
     * 1. 验证用户名格式（防止 SQL 注入）
     * 2. 检查账户是否被锁定
     * 3. 解密 RSA 加密的密码
     * 4. 使用 AuthenticationManager 进行认证
     * 5. 认证成功后生成 JWT Token
     * 6. 重置失败计数并返回 Token 和用户基本信息
     * </p>
     *
     * @param request 登录请求（包含用户名和加密的密码）
     * @param http  HTTP 请求对象
     * @return 登录响应（包含 Token 和用户信息）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码进行登录，成功后返回 JWT Token 和用户基本信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "429", description = "登录失败次数过多，账户被锁定")
    })
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest http) {
        // 仅限调试用
//        log.info("用户登录：username={}, passwordLength={}", request.username(),
//                request.password() != null ? request.password().length() : 0);

        // 1. 验证用户名格式（防止 SQL 注入）
        UsernameValidator.validate(request.username());

        // 2. 获取客户端 IP 作为标识符
        String clientIp = getClientIp(http);
        String identifier = request.username() + ":" + clientIp;

        // 3. 检查账户是否被锁定
        if (loginAttemptService.isLocked(identifier)) {
            long remainingTime = loginAttemptService.getRemainingLockTime(identifier);
            log.warn("账户已被锁定：identifier={}, 剩余时间={}秒", identifier, remainingTime);
            return Result.error(429, "登录失败次数过多，请" + (remainingTime / 60) + "分钟后再试");
        }

        try {
            // 4. 解密 RSA 加密的密码（如果已加密）
            String rawPassword = RsaUtils.decryptPassword(request.password());

            // 仅限调试用
//            log.info("RSA 解密完成，加密前长度：{}, 解密后长度：{}", request.password().length(), rawPassword.length());

            // 4.1 验证密码格式（新增：确保密码符合安全规则）
            PasswordValidator.validate(rawPassword);

            // 5. 创建认证对象（包含用户名和解密后的密码）
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.username(), rawPassword);

            // 6. 使用 AuthenticationManager 进行认证
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 7. 认证成功，重置失败计数
            loginAttemptService.resetFailures(identifier);

            // 8. 将认证信息设置到 SecurityContext 中
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 9. 从认证对象中获取 LoginUser
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysUser sysUser = loginUser.getSysUser();

            // 10. 生成 JWT Token
            String token = jwtUtil.generateToken(sysUser.getUsername(), sysUser.getId());

            log.info("用户登录成功：username={}, userId={}", sysUser.getUsername(), sysUser.getId());

            // 11. 构建登录响应数据
            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .userId(sysUser.getId())
                    .username(sysUser.getUsername())
                    .realName(sysUser.getRealName())
                    .userType(sysUser.getUserType())
                    .avatar(sysUser.getAvatar())
                    .build();

            return Result.success("登录成功", loginResponse);

        } catch (BadCredentialsException e) {
            // 认证失败，记录失败次数
            loginAttemptService.recordFailure(identifier);
            log.warn("登录失败：username={}, ip={}", request.username(), clientIp);
            return Result.error(401, "用户名或密码错误");
        }
    }

    /**
     * 用户登出接口
     * <p>
     * 登出流程：
     * 1. 获取当前用户的 Token
     * 2. 将 Token 加入黑名单（使其立即失效）
     * 3. 清除 SecurityContext
     * </p>
     *
     * @param http HTTP 请求对象
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录，将当前 Token 加入黑名单使其失效")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登出成功"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    public Result<Void> logout(HttpServletRequest http) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            log.info("用户登出：username={}, userId={}", loginUser.getUsername(), loginUser.getUserId());

            // 从请求头中获取 Token 并加入黑名单
            String token = jwtUtil.parseToken(http.getHeader("Authorization"));
            if (token != null) {
                // 计算 Token 剩余有效时间
                long expireTime = jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    tokenBlacklistService.addToBlacklist(token, expireTime / 1000);
                }
            }
        }

        // 清除 SecurityContext
        SecurityContextHolder.clearContext();
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     * <p>
     * 从 SecurityContext 中获取当前登录用户的信息
     * </p>
     *
     * @return 当前用户信息
     */
    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息（不含 Token）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    public Result<LoginResponse> getCurrentUser() {
        // 从 SecurityContext 中获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return Result.error("未登录");
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser sysUser = loginUser.getSysUser();

        // 构建用户信息（不含 Token）
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .userType(sysUser.getUserType())
                .avatar(sysUser.getAvatar())
                .build();

        return Result.success(loginResponse);
    }

    /**
     * 获取客户端 IP 地址
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
